package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.BossCombatProfile;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.GearUpgradeRecommendation;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GearUpgradeAdvisor {
    private final EquipmentDatabase equipmentDatabase;
    private final GearOptimizerService optimizerService;
    private final GePriceService priceService;

    @Inject
    public GearUpgradeAdvisor(
            EquipmentDatabase equipmentDatabase,
            GearOptimizerService optimizerService,
            GePriceService priceService) {
        this.equipmentDatabase = equipmentDatabase;
        this.optimizerService = optimizerService;
        this.priceService = priceService;
    }

    public List<GearUpgradeRecommendation> findUpgrades(
            OptimizedGearSetup currentSetup,
            long availableBudget,
            PlayerAccount account) {
        BossData boss = (currentSetup != null) ? currentSetup.getBoss() : null;
        return findUpgrades(currentSetup, boss, availableBudget, account);
    }

    public List<GearUpgradeRecommendation> findUpgrades(
            OptimizedGearSetup currentSetup,
            BossData boss,
            long availableBudget,
            PlayerAccount account) {

        if (currentSetup == null) return Collections.emptyList();

        List<GearUpgradeRecommendation> recommendations = new ArrayList<>();
        CombatStyle style = currentSetup.getCombatStyle();
        BossCombatProfile profile = optimizerService.getBossProfile(boss);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            EquipmentItem current = currentSetup.getItem(slot);
            if (current == null) continue;

            double curScore = optimizerService.calculateItemScore(current, profile, style);
            long curPrice = optimizerService.getItemPrice(current);

            List<EquipmentItem> candidates = equipmentDatabase.getItemsForSlot(slot);
            EquipmentItem bestUpgrade = null;
            double bestScoreGain = 0.0;
            long bestPriceDiff = Long.MAX_VALUE;

            for (EquipmentItem candidate : candidates) {
                if (candidate.getItemId() == current.getItemId()) continue;
                if (!optimizerService.canEquip(candidate, account)) continue;

                // 1. Strict combat style matching
                if (!isStyleCompatible(candidate, slot, style)) continue;

                // 2. Boss mechanic matching
                if (candidate.isUndeadBonus() && !profile.isUndead()) continue;
                if (profile.isRequiresCrush() && slot == EquipmentSlot.WEAPON && candidate.getCrushAttack() <= 0) continue;
                if (profile.isRequiresSpear() && slot == EquipmentSlot.WEAPON && !candidate.isSpear() && !candidate.getName().contains("fang")) continue;
                if (profile.isRequiresDragonfire() && slot == EquipmentSlot.SHIELD && !candidate.isDragonfireProtection()) continue;

                // 3. Score must be strictly superior and tier must be an upgrade
                if (candidate.getTier() <= current.getTier()) continue;

                double candScore = optimizerService.calculateItemScore(candidate, profile, style);
                if (candScore <= curScore) continue;

                long newPrice = optimizerService.getItemPrice(candidate);
                long diff = Math.max(0, newPrice - curPrice);

                double gain = candScore - curScore;
                if (gain > bestScoreGain || (Math.abs(gain - bestScoreGain) < 0.01 && diff < bestPriceDiff)) {
                    bestUpgrade = candidate;
                    bestScoreGain = gain;
                    bestPriceDiff = diff;
                }
            }

            if (bestUpgrade != null) {
                String statDiff = buildStatDiff(current, bestUpgrade, style);
                String why = buildWhyMatters(current, bestUpgrade, profile, style);
                boolean fits = bestPriceDiff <= availableBudget;

                recommendations.add(new GearUpgradeRecommendation(
                        slot,
                        current,
                        bestUpgrade,
                        bestPriceDiff,
                        statDiff,
                        why,
                        fits
                ));
            }
        }

        // Sort by affordability first, then by highest impact
        recommendations.sort(Comparator.comparing(GearUpgradeRecommendation::isFitsBudget).reversed()
                .thenComparingLong(GearUpgradeRecommendation::getPriceDifference));

        return recommendations;
    }

    private boolean isStyleCompatible(EquipmentItem item, EquipmentSlot slot, CombatStyle style) {
        if (slot == EquipmentSlot.WEAPON) {
            return item.getPrimaryStyle() == style;
        }

        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.BODY || slot == EquipmentSlot.LEGS) {
            if (style == CombatStyle.MELEE) return item.getPrimaryStyle() == CombatStyle.MELEE || item.getPrimaryStyle() == CombatStyle.HYBRID;
            if (style == CombatStyle.RANGED) return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedAttack() >= -5;
            if (style == CombatStyle.MAGIC) return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicAttack() >= -5;
        }

        if (slot == EquipmentSlot.GLOVES) {
            if (style == CombatStyle.MELEE) return item.getPrimaryStyle() == CombatStyle.MELEE || item.getMeleeStrength() > 0;
            if (style == CombatStyle.RANGED) return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedAttack() > 0;
            if (style == CombatStyle.MAGIC) return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicDamagePercent() > 0 || item.getMagicAttack() > 0;
        }

        if (slot == EquipmentSlot.BOOTS) {
            if (style == CombatStyle.MELEE) return item.getPrimaryStyle() == CombatStyle.MELEE || item.getMeleeStrength() > 0;
            if (style == CombatStyle.RANGED) return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedAttack() > 0;
            if (style == CombatStyle.MAGIC) return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicAttack() > 0;
        }

        if (slot == EquipmentSlot.AMULET) {
            if (style == CombatStyle.MELEE) return item.getPrimaryStyle() == CombatStyle.MELEE || item.getMeleeStrength() > 0 || item.getName().contains("fury") || item.getName().contains("glory");
            if (style == CombatStyle.RANGED) return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedStrength() > 0 || item.getName().contains("fury") || item.getName().contains("glory");
            if (style == CombatStyle.MAGIC) return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicDamagePercent() > 0 || item.getName().contains("fury") || item.getName().contains("glory");
        }

        if (slot == EquipmentSlot.RING) {
            if (style == CombatStyle.MELEE) return item.getPrimaryStyle() == CombatStyle.MELEE || item.getMeleeStrength() > 0 || item.getName().contains("Lightbearer") || item.getName().contains("suffering");
            if (style == CombatStyle.RANGED) return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedStrength() > 0 || item.getRangedAttack() > 0 || item.getName().contains("Lightbearer");
            if (style == CombatStyle.MAGIC) return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicAttack() > 0 || item.getName().contains("Lightbearer");
        }

        return true;
    }

    private String buildStatDiff(EquipmentItem cur, EquipmentItem up, CombatStyle style) {
        StringBuilder sb = new StringBuilder();
        int strDiff = (style == CombatStyle.RANGED)
                ? (up.getRangedStrength() - cur.getRangedStrength())
                : (up.getMeleeStrength() - cur.getMeleeStrength());

        if (strDiff > 0) {
            sb.append("+").append(strDiff).append(" Strength");
        }

        int magPct = up.getMagicDamagePercent() - cur.getMagicDamagePercent();
        if (magPct > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("+").append(magPct).append("% Magic Dmg");
        }

        int offDiff = up.getStyleOffenceBonus(style) - cur.getStyleOffenceBonus(style);
        if (offDiff > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("+").append(offDiff).append(" Accuracy");
        }

        if (sb.length() == 0) {
            sb.append("+ Tier ").append(Math.max(1, up.getTier() - cur.getTier())).append(" Power");
        }

        return sb.toString();
    }

    private String buildWhyMatters(EquipmentItem cur, EquipmentItem up, BossCombatProfile profile, CombatStyle style) {
        if (!up.getSpecialEffect().isEmpty()) {
            return up.getSpecialEffect();
        }
        if (up.getMeleeStrength() > cur.getMeleeStrength() && style == CombatStyle.MELEE) {
            return "Provides a permanent melee max hit increase.";
        }
        if (up.getRangedStrength() > cur.getRangedStrength() && style == CombatStyle.RANGED) {
            return "Increases maximum hit for ranged ammunition.";
        }
        if (up.getMagicDamagePercent() > cur.getMagicDamagePercent()) {
            return "Directly scales spell max hits across all magic attacks.";
        }
        if (up.getPrayerBonus() > cur.getPrayerBonus()) {
            return "Significantly slows prayer drain during long boss encounters.";
        }
        return "Higher accuracy rolls yield faster kill times and lower supply consumption.";
    }
}
