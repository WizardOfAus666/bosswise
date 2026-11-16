package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.BossCombatProfile;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.GearRoadmapStep;
import com.bosswise.model.PlayerAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class GearRoadmapService {
    private final EquipmentDatabase equipmentDatabase;
    private final GearOptimizerService optimizerService;
    private final GePriceService priceService;

    private final List<EquipmentItem> wishlist = new CopyOnWriteArrayList<>();
    private long savingsGoalGp = 10_000_000L;
    private String savingsGoalTargetName = "Bandos Chestplate";

    @Inject
    public GearRoadmapService(
            EquipmentDatabase equipmentDatabase,
            GearOptimizerService optimizerService,
            GePriceService priceService) {
        this.equipmentDatabase = equipmentDatabase;
        this.optimizerService = optimizerService;
        this.priceService = priceService;
    }

    public List<GearRoadmapStep> generateRoadmap(
            PlayerAccount account,
            Map<EquipmentSlot, EquipmentItem> currentGear,
            CombatStyle style,
            BossData boss) {

        if (currentGear == null || currentGear.isEmpty()) {
            return Collections.emptyList();
        }

        BossCombatProfile profile = (boss != null) ? optimizerService.getBossProfile(boss) : null;
        List<GearRoadmapStep> steps = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.SPEC_WEAPON) continue;
            EquipmentItem current = currentGear.get(slot);
            if (current == null) continue;

            List<EquipmentItem> candidates = equipmentDatabase.getItemsForSlot(slot);
            for (EquipmentItem candidate : candidates) {
                if (candidate.getItemId() == current.getItemId()) continue;
                if (candidate.getTier() <= current.getTier()) continue;

                // Style check
                if (style == CombatStyle.MELEE && candidate.getPrimaryStyle() != CombatStyle.MELEE && candidate.getMeleeStrength() <= 0 && slot != EquipmentSlot.AMMO) continue;
                if (style == CombatStyle.RANGED && candidate.getPrimaryStyle() != CombatStyle.RANGED && candidate.getRangedAttack() <= 0 && slot != EquipmentSlot.RING) continue;
                if (style == CombatStyle.MAGIC && candidate.getPrimaryStyle() != CombatStyle.MAGIC && candidate.getMagicAttack() <= 0 && slot != EquipmentSlot.RING) continue;

                long curPrice = optimizerService.getItemPrice(current);
                long candPrice = optimizerService.getItemPrice(candidate);
                long upgradeCost = Math.max(0, candPrice - curPrice);

                boolean canEquip = optimizerService.canEquip(candidate, account);
                List<String> unmetReqs = new ArrayList<>();
                for (Map.Entry<Skill, Integer> req : candidate.getSkillRequirements().entrySet()) {
                    int pLvl = account != null ? account.getRealLevel(req.getKey()) : 70;
                    if (pLvl < req.getValue()) {
                        unmetReqs.add(req.getKey().getName() + " " + req.getValue() + " (cur: " + pLvl + ")");
                    }
                }
                String q = candidate.getQuestRequirement();
                if (q != null && !q.trim().isEmpty()) {
                    if (account != null && !account.isQuestCompleted(null, q)) {
                        unmetReqs.add("Quest: " + q);
                    }
                }

                double dpsGain = calculateEstimatedDpsGain(current, candidate, style);
                int defGain = calculateDefenceGain(current, candidate);

                String reason = String.format("Tier %d -> %d (+%d Str/Atk, +%d Def)",
                        current.getTier(), candidate.getTier(),
                        Math.max(0, candidate.getMeleeStrength() - current.getMeleeStrength()),
                        Math.max(0, defGain));

                steps.add(new GearRoadmapStep(
                        0, // will rank after sorting
                        slot,
                        current,
                        candidate,
                        curPrice,
                        candPrice,
                        upgradeCost,
                        dpsGain,
                        defGain,
                        unmetReqs,
                        reason,
                        canEquip
                ));
            }
        }

        // Filter: keep only the best 1-2 upgrades per slot to avoid clutter
        Map<EquipmentSlot, List<GearRoadmapStep>> bySlot = new java.util.EnumMap<>(EquipmentSlot.class);
        for (GearRoadmapStep step : steps) {
            bySlot.computeIfAbsent(step.getSlot(), k -> new ArrayList<>()).add(step);
        }

        List<GearRoadmapStep> filtered = new ArrayList<>();
        for (List<GearRoadmapStep> slotSteps : bySlot.values()) {
            slotSteps.sort(Comparator.comparingInt(s -> s.getSuggestedItem().getTier()));
            if (!slotSteps.isEmpty()) {
                filtered.add(slotSteps.get(0)); // Next immediate upgrade
                if (slotSteps.size() > 1) {
                    filtered.add(slotSteps.get(slotSteps.size() - 1)); // BiS upgrade
                }
            }
        }

        // Rank by efficiency: Can equip first, then by upgrade cost
        filtered.sort((a, b) -> {
            if (a.isCanEquipNow() != b.isCanEquipNow()) {
                return a.isCanEquipNow() ? -1 : 1;
            }
            return Long.compare(a.getUpgradeCost(), b.getUpgradeCost());
        });

        List<GearRoadmapStep> ranked = new ArrayList<>();
        for (int i = 0; i < filtered.size(); i++) {
            GearRoadmapStep s = filtered.get(i);
            ranked.add(new GearRoadmapStep(
                    i + 1,
                    s.getSlot(),
                    s.getCurrentItem(),
                    s.getSuggestedItem(),
                    s.getCurrentPrice(),
                    s.getSuggestedPrice(),
                    s.getUpgradeCost(),
                    s.getDpsImprovement(),
                    s.getDefenceImprovement(),
                    s.getRequirements(),
                    s.getReason(),
                    s.isCanEquipNow()
            ));
        }

        return ranked;
    }

    private double calculateEstimatedDpsGain(EquipmentItem cur, EquipmentItem up, CombatStyle style) {
        if (style == CombatStyle.MELEE) {
            int strGain = up.getMeleeStrength() - cur.getMeleeStrength();
            return Math.max(0.0, strGain * 0.12);
        } else if (style == CombatStyle.RANGED) {
            int strGain = up.getRangedStrength() - cur.getRangedStrength();
            return Math.max(0.0, strGain * 0.14);
        } else {
            int dmgGain = up.getMagicDamagePercent() - cur.getMagicDamagePercent();
            return Math.max(0.0, dmgGain * 0.15);
        }
    }

    private int calculateDefenceGain(EquipmentItem cur, EquipmentItem up) {
        int curDef = cur.getStabDefence() + cur.getSlashDefence() + cur.getCrushDefence() + cur.getRangedDefence() + cur.getMagicDefence();
        int upDef = up.getStabDefence() + up.getSlashDefence() + up.getCrushDefence() + up.getRangedDefence() + up.getMagicDefence();
        return Math.max(0, upDef - curDef);
    }

    // Wishlist management
    public List<EquipmentItem> getWishlist() {
        return Collections.unmodifiableList(wishlist);
    }

    public void addToWishlist(EquipmentItem item) {
        if (item != null && !wishlist.contains(item)) {
            wishlist.add(item);
        }
    }

    public void removeFromWishlist(EquipmentItem item) {
        wishlist.remove(item);
    }

    // Savings goal management
    public long getSavingsGoalGp() { return savingsGoalGp; }
    public void setSavingsGoalGp(long gp) { this.savingsGoalGp = Math.max(0, gp); }
    public String getSavingsGoalTargetName() { return savingsGoalTargetName; }
    public void setSavingsGoalTargetName(String name) { this.savingsGoalTargetName = name; }

    public double getSavingsProgress(long currentGp) {
        if (savingsGoalGp <= 0) return 1.0;
        return Math.min(1.0, (double) currentGp / (double) savingsGoalGp);
    }
}
