package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.BossCombatProfile;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentItem.AmmoType;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.StrategyMethod;
import com.bosswise.model.strategy.StrategyTier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class GearOptimizerService {
    private final EquipmentDatabase equipmentDatabase;
    private final GePriceService priceService;
    private final MonsterDatabase monsterDatabase;
    private final CombatCalculationService calculationService;
    private final MonsterStrategyDatabase strategyDatabase;
    private final Map<String, BossCombatProfile> bossProfiles = new HashMap<>();

    public enum PriorityMode {
        BALANCED("Balanced"),
        MAX_DPS("Max DPS"),
        BEST_VALUE("Best DPS/GP"),
        SURVIVABILITY("High Defence"),
        CHEAPEST("Cheapest Effective"),
        SLAYER_EFFICIENCY("Slayer Task");

        private final String label;
        PriorityMode(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public GearOptimizerService(EquipmentDatabase equipmentDatabase, GePriceService priceService) {
        this(equipmentDatabase, priceService, new MonsterDatabase(), new CombatCalculationService(), new MonsterStrategyDatabase());
    }

    public GearOptimizerService(
            EquipmentDatabase equipmentDatabase,
            GePriceService priceService,
            MonsterDatabase monsterDatabase,
            CombatCalculationService calculationService) {
        this(equipmentDatabase, priceService, monsterDatabase, calculationService, new MonsterStrategyDatabase());
    }

    @Inject
    public GearOptimizerService(
            EquipmentDatabase equipmentDatabase,
            GePriceService priceService,
            MonsterDatabase monsterDatabase,
            CombatCalculationService calculationService,
            MonsterStrategyDatabase strategyDatabase) {
        this.equipmentDatabase = equipmentDatabase;
        this.priceService = priceService;
        this.monsterDatabase = monsterDatabase;
        this.calculationService = calculationService;
        this.strategyDatabase = strategyDatabase != null ? strategyDatabase : new MonsterStrategyDatabase();
        initBossProfiles();
    }

        private void initBossProfiles() {
        try (InputStream is = getClass().getResourceAsStream("/data/boss_profiles.json")) {
            if (is == null) return;
            JsonElement parsed = new JsonParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonArray arr = (parsed != null && parsed.isJsonArray()) ? parsed.getAsJsonArray() : null;
            if (arr == null) return;
            for (JsonElement elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject o = elem.getAsJsonObject();
                BossCombatProfile.Builder b = BossCombatProfile.builder(
                        o.get("bossId").getAsString(),
                        o.get("bossName").getAsString(),
                        CombatStyle.valueOf(o.get("recommendedStyle").getAsString())
                );
                if (o.has("allowedStyles") && o.get("allowedStyles").isJsonArray()) {
                    List<CombatStyle> styles = new ArrayList<>();
                    for (JsonElement se : o.getAsJsonArray("allowedStyles")) {
                        styles.add(CombatStyle.valueOf(se.getAsString()));
                    }
                    b.allowedStyles(styles.toArray(new CombatStyle[0]));
                }
                if (o.has("favoredAttackType")) b.favoredAttackType(AttackType.valueOf(o.get("favoredAttackType").getAsString()));
                if (o.has("dragon") && o.get("dragon").getAsBoolean()) b.dragon();
                if (o.has("demon") && o.get("demon").getAsBoolean()) b.demon();
                if (o.has("undead") && o.get("undead").getAsBoolean()) b.undead();
                if (o.has("kalphite") && o.get("kalphite").getAsBoolean()) b.kalphite();
                if (o.has("rat") && o.get("rat").getAsBoolean()) b.rat();
                if (o.has("wilderness") && o.get("wilderness").getAsBoolean()) b.wilderness();
                if (o.has("requiresDragonfire") && o.get("requiresDragonfire").getAsBoolean()) b.requiresDragonfire();
                if (o.has("requiresWyvernShield") && o.get("requiresWyvernShield").getAsBoolean()) b.requiresWyvernShield();
                if (o.has("requiresMirrorShield") && o.get("requiresMirrorShield").getAsBoolean()) b.requiresMirrorShield();
                if (o.has("requiresLeafBladed") && o.get("requiresLeafBladed").getAsBoolean()) b.requiresLeafBladed();
                if (o.has("requiresRockHammer") && o.get("requiresRockHammer").getAsBoolean()) b.requiresRockHammer();
                if (o.has("requiresBootsOfStone") && o.get("requiresBootsOfStone").getAsBoolean()) b.requiresBootsOfStone();
                if (o.has("requiresCrush") && o.get("requiresCrush").getAsBoolean()) b.requiresCrush();
                if (o.has("requiresSpear") && o.get("requiresSpear").getAsBoolean()) b.requiresSpear();
                if (o.has("favorsVenomProtection") && o.get("favorsVenomProtection").getAsBoolean()) b.favorsVenomProtection();
                if (o.has("favorsRecoil") && o.get("favorsRecoil").getAsBoolean()) b.favorsRecoil();
                if (o.has("favorsVoid") && o.get("favorsVoid").getAsBoolean()) b.favorsVoid();
                if (o.has("notes")) b.notes(o.get("notes").getAsString());
                registerProfile(b.build());
            }
        } catch (Exception ignored) {}
    }

    private void registerProfile(BossCombatProfile profile) {
        bossProfiles.put(profile.getBossId().toLowerCase(), profile);
    }

    public BossCombatProfile getBossProfile(BossData boss) {
        if (boss == null) {
            return BossCombatProfile.builder("default", "General Encounter", CombatStyle.MELEE).build();
        }
        BossCombatProfile profile = bossProfiles.get(boss.getId().toLowerCase());
        if (profile != null) return profile;

        MonsterData m = monsterDatabase.getMonster(boss.getId());
        if (m == null && boss.getName() != null) {
            m = monsterDatabase.getMonsterByName(boss.getName());
        }
        if (m != null) {
            MonsterCombatProfile cp = m.getCombatProfile();
            BossCombatProfile.Builder b = BossCombatProfile.builder(m.getId(), m.getName(), cp.getRecommendedStyle());
            b.allowedStylesSet(cp.getAllowedStyles());
            b.favoredAttackType(cp.getFavoredAttackType());
            if (cp.isDragon()) b.dragon();
            if (cp.isDemon()) b.demon();
            if (cp.isUndead()) b.undead();
            if (cp.isKalphite()) b.kalphite();
            if (cp.isRat()) b.rat();
            if (cp.isWilderness()) b.wilderness();
            if (cp.isRequiresDragonfire()) b.requiresDragonfire();
            if (cp.isRequiresWyvernShield()) b.requiresWyvernShield();
            if (cp.isRequiresMirrorShield()) b.requiresMirrorShield();
            if (cp.isRequiresLeafBladed()) b.requiresLeafBladed();
            if (cp.isRequiresRockHammer()) b.requiresRockHammer();
            if (cp.isRequiresBootsOfStone()) b.requiresBootsOfStone();
            if (cp.isRequiresCrush()) b.requiresCrush();
            if (cp.isRequiresSpear()) b.requiresSpear();
            if (cp.isFavorsVenomProtection()) b.favorsVenomProtection();
            if (cp.isFavorsRecoil()) b.favorsRecoil();
            if (cp.isFavorsVoid()) b.favorsVoid();
            if (cp.getNotes() != null && !cp.getNotes().isEmpty()) b.notes(cp.getNotes());
            return b.build();
        }

        // Auto-detect based on boss attributes
        String name = boss.getName().toLowerCase();
        BossCombatProfile.Builder builder = BossCombatProfile.builder(boss.getId(), boss.getName(), boss.getPrimaryStyle());
        if (name.contains("dragon") || name.contains("vorkath") || name.contains("hydra")) {
            builder.dragon();
            builder.requiresDragonfire();
        }
        if (name.contains("demon") || name.contains("k'ril") || name.contains("cerberus") || name.contains("sire")) {
            builder.demon();
        }
        if (name.contains("skeleton") || name.contains("zombie") || name.contains("ghost") || name.contains("vet'ion") || name.contains("calvar")) {
            builder.undead();
        }
        return builder.build();
    }

    public boolean canEquip(EquipmentItem item, PlayerAccount account) {
        if (item == null) return false;
        if (account == null) return true;

        for (Map.Entry<Skill, Integer> entry : item.getSkillRequirements().entrySet()) {
            if (account.getRealLevel(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        if (item.getQuestRequirement() != null && !item.getQuestRequirement().isEmpty()) {
            if (!account.isQuestCompleted(null, item.getQuestRequirement())) {
                return false;
            }
        }

        String name = item.getName();
        if ("Dragon defender".equalsIgnoreCase(name)) {
            int atk = account.getRealLevel(Skill.ATTACK);
            int str = account.getRealLevel(Skill.STRENGTH);
            if ((atk + str < 130) && atk < 99 && str < 99) {
                return false;
            }
        } else if ("Fire cape".equalsIgnoreCase(name)) {
            if (account.getRealLevel(Skill.PRAYER) < 43 || account.getCombatLevel() < 65) {
                return false;
            }
        } else if ("Infernal cape".equalsIgnoreCase(name)) {
            if (account.getCombatLevel() < 100) {
                return false;
            }
        }

        return true;
    }

    public long getItemPrice(EquipmentItem item) {
        if (item == null) return 0L;
        if (item.isUntradeable()) return item.getDefaultPrice();
        return priceService.getPrice(item.getItemId(), item.getDefaultPrice());
    }

    // Overload for BossData (backward compatibility)
    public OptimizedGearSetup generateSetup(
            String setupTier,
            BossData boss,
            CombatStyle combatStyle,
            long budget,
            PlayerAccount account,
            PriorityMode priority) {

        MonsterData monster = null;
        if (boss != null) {
            monster = monsterDatabase.getMonster(boss.getId());
            if (monster == null) {
                monster = monsterDatabase.getMonsterByName(boss.getName());
            }
        }
        return generateSetup(setupTier, boss, monster, null, combatStyle, budget, account, priority, false);
    }

    // Overload for universal MonsterData
    public OptimizedGearSetup generateSetup(
            String setupTier,
            MonsterData monster,
            MonsterVariant variant,
            CombatStyle combatStyle,
            long budget,
            PlayerAccount account,
            PriorityMode priority,
            boolean onSlayerTask) {

        BossData boss = (monster != null) ? monster.toBossData() : null;
        return generateSetup(setupTier, boss, monster, variant, combatStyle, budget, account, priority, onSlayerTask);
    }

    public List<OptimizedGearSetup> generateAllViableSetups(
            MonsterData monster,
            PlayerAccount account,
            long userBudget,
            boolean onSlayerTask) {
        BossData boss = (monster != null) ? monster.toBossData() : null;
        return generateAllViableSetups(boss, monster, account, userBudget, onSlayerTask);
    }

    public List<OptimizedGearSetup> generateAllViableSetups(
            BossData boss,
            MonsterData monster,
            PlayerAccount account,
            long userBudget,
            boolean onSlayerTask) {

        List<OptimizedGearSetup> results = new ArrayList<>();
        if (boss == null && monster == null) return results;

        // 1. Look up authentic OSRS Wiki strategy setups from MonsterStrategyDatabase
        String monsterKey = "";
        if (monster != null && monster.getId() != null) {
            monsterKey = monster.getId();
        } else if (boss != null && boss.getName() != null) {
            monsterKey = boss.getName().toLowerCase().replace(" ", "_").replace("'", "");
        }

        MonsterStrategy strategy = (strategyDatabase != null) ? strategyDatabase.getStrategy(monsterKey) : null;
        if (strategy == null && boss != null && boss.getName() != null) {
            strategy = (strategyDatabase != null) ? strategyDatabase.getStrategy(boss.getName()) : null;
        }

        if (strategy != null) {
            for (StrategyMethod method : strategy.getMethods()) {
                OptimizedGearSetup stratSetup = createSetupFromStrategyMethod(method, boss, monster, account, onSlayerTask);
                if (stratSetup != null) {
                    results.add(stratSetup);
                }
            }
        }

        BossCombatProfile profile = getBossProfile(boss != null ? boss : monster.toBossData());
        Set<CombatStyle> allowedStyles = profile.getAllowedStyles();

        // Ensure userBudget has a reasonable value
        long activeBudget = userBudget > 0 ? userBudget : 5_000_000L;

        // For each allowed combat style, generate tiers:
        for (CombatStyle style : allowedStyles) {
            // 1. Budget setup (1.5M cap)
            OptimizedGearSetup budgetSetup = generateSetup("Budget (" + style.getDisplayName() + ")", boss, monster, null, style, 1_500_000L, account, PriorityMode.BALANCED, onSlayerTask);
            if (budgetSetup != null) results.add(budgetSetup);

            // 2. Best Value setup (25M cap)
            OptimizedGearSetup valueSetup = generateSetup("Best Value (" + style.getDisplayName() + ")", boss, monster, null, style, 25_000_000L, account, PriorityMode.BEST_VALUE, onSlayerTask);
            if (valueSetup != null) results.add(valueSetup);

            // 3. High Tier setup (120M cap)
            OptimizedGearSetup highSetup = generateSetup("High Tier (" + style.getDisplayName() + ")", boss, monster, null, style, 120_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (highSetup != null) results.add(highSetup);

            // 4. Maximum BiS setup (no budget limit / 2B)
            OptimizedGearSetup bisSetup = generateSetup("Max BiS (" + style.getDisplayName() + ")", boss, monster, null, style, 2_000_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (bisSetup != null) results.add(bisSetup);
        }

        // 5. Specialised setups
        if (profile.isDemon()) {
            OptimizedGearSetup demonbane = generateSetup("Demonbane Specialised", boss, monster, null, profile.getRecommendedStyle(), 2_000_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (demonbane != null) results.add(demonbane);
        } else if (profile.isDragon()) {
            OptimizedGearSetup dragonbane = generateSetup("Dragonbane Specialised", boss, monster, null, profile.getRecommendedStyle(), 2_000_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (dragonbane != null) results.add(dragonbane);
        } else if (profile.isUndead()) {
            OptimizedGearSetup undead = generateSetup("Undead Specialised", boss, monster, null, profile.getRecommendedStyle(), 2_000_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (undead != null) results.add(undead);
        } else if (profile.isKalphite()) {
            OptimizedGearSetup kalphite = generateSetup("Kalphitebane Specialised", boss, monster, null, CombatStyle.MELEE, 2_000_000_000L, account, PriorityMode.MAX_DPS, onSlayerTask);
            if (kalphite != null) results.add(kalphite);
        }

        // 6. User Custom Budget setup (if user provided a specific custom budget distinct from default presets)
        if (userBudget > 0 && userBudget != 1_500_000L && userBudget != 25_000_000L && userBudget != 120_000_000L && userBudget != 2_000_000_000L) {
            OptimizedGearSetup custom = generateSetup("Custom (" + formatGp(userBudget) + ")", boss, monster, null, profile.getRecommendedStyle(), userBudget, account, PriorityMode.BEST_VALUE, onSlayerTask);
            if (custom != null) results.add(0, custom);
        }

        return results;
    }

    public OptimizedGearSetup createSetupFromStrategyMethod(
            StrategyMethod method,
            BossData boss,
            MonsterData monster,
            PlayerAccount account,
            boolean onSlayerTask) {
        if (method == null || method.getGearSetup() == null || method.getGearSetup().isEmpty()) {
            return null;
        }

        Map<EquipmentSlot, EquipmentItem> equipment = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, String> slotReasons = new EnumMap<>(EquipmentSlot.class);
        long totalCost = 0;

        for (Map.Entry<EquipmentSlot, String> entry : method.getGearSetup().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            String itemName = entry.getValue();
            if (itemName == null || itemName.trim().isEmpty() || "None".equalsIgnoreCase(itemName)) {
                continue;
            }
            EquipmentItem item = equipmentDatabase.getItemByName(itemName);
            if (item != null) {
                equipment.put(slot, item);
                long price = priceService.getPrice(item.getItemId(), item.getDefaultPrice());
                totalCost += price;
                slotReasons.put(slot, "OSRS Wiki strategy (" + method.getTitle() + ")");
            }
        }

        if (equipment.isEmpty()) {
            return null;
        }

        CombatResult combatResult = null;
        try {
            MonsterVariant variant = (monster != null) ? monster.getPrimaryVariant() : null;
            MonsterCombatProfile monsterProfile = (monster != null) ? monster.getCombatProfile() : null;
            combatResult = calculationService.calculateCombat(
                    equipment,
                    variant,
                    monsterProfile,
                    account,
                    method.getCombatStyle(),
                    onSlayerTask
            );
        } catch (Exception ignored) {}

        List<String> highlights = new ArrayList<>(method.getKeyItems());
        if (highlights.isEmpty() && method.getWhyThisWorks() != null && !method.getWhyThisWorks().isEmpty()) {
            highlights.add(method.getWhyThisWorks());
        }

        String recCategory = method.getRecommendationType();
        if (recCategory == null || recCategory.isEmpty()) {
            recCategory = (method.getTier() == StrategyTier.BUDGET) ? "BUDGET_ALTERNATIVE" : "WIKI_RECOMMENDED";
        }

        return new OptimizedGearSetup(
                method.getTitle(),
                boss,
                monster,
                method.getCombatStyle(),
                equipment,
                totalCost,
                totalCost,
                slotReasons,
                highlights,
                combatResult,
                recCategory,
                method.getWikiSourceUrl(),
                "VERIFIED"
        );
    }

    public List<String> getMissingRequirements(EquipmentItem item, PlayerAccount account) {
        List<String> missing = new ArrayList<>();
        if (item == null || account == null) return missing;

        for (Map.Entry<Skill, Integer> entry : item.getSkillRequirements().entrySet()) {
            int currentLvl = account.getRealLevel(entry.getKey());
            if (currentLvl < entry.getValue()) {
                missing.add(String.format("Requires %d %s (Current: %d)", entry.getValue(), entry.getKey().getName(), currentLvl));
            }
        }

        if (item.getQuestRequirement() != null && !item.getQuestRequirement().isEmpty()) {
            if (!account.isQuestCompleted(null, item.getQuestRequirement())) {
                missing.add("Requires Quest: " + item.getQuestRequirement());
            }
        }

        String name = item.getName();
        if ("Dragon defender".equalsIgnoreCase(name)) {
            int atk = account.getRealLevel(Skill.ATTACK);
            int str = account.getRealLevel(Skill.STRENGTH);
            if ((atk + str < 130) && atk < 99 && str < 99) {
                missing.add("Requires 130 combined Attack + Strength for Warriors' Guild access");
            }
        } else if ("Fire cape".equalsIgnoreCase(name)) {
            if (account.getRealLevel(Skill.PRAYER) < 43 || account.getCombatLevel() < 65) {
                missing.add("Requires 43+ Prayer and ~65+ Combat Level to defeat TzTok-Jad");
            }
        } else if ("Infernal cape".equalsIgnoreCase(name)) {
            if (account.getCombatLevel() < 100) {
                missing.add("Recommended 100+ Combat Level to complete the Mor Ul Rek Inferno");
            }
        }

        return missing;
    }

    public OptimizedGearSetup generateSetup(
            String setupTier,
            BossData boss,
            MonsterData monster,
            MonsterVariant variant,
            CombatStyle combatStyle,
            long budget,
            PlayerAccount account,
            PriorityMode priority,
            boolean onSlayerTask) {

        BossCombatProfile profile = getBossProfile(boss);
        if (variant == null && monster != null) {
            variant = monster.getPrimaryVariant();
        }

        // Enforce valid combat style for boss
        if (combatStyle == null || !profile.isStyleAllowed(combatStyle)) {
            combatStyle = profile.getRecommendedStyle();
        }

        boolean isBudget = "Budget".equalsIgnoreCase(setupTier) || "Cheapest Effective".equalsIgnoreCase(setupTier) || "Cheapest".equalsIgnoreCase(setupTier);
        boolean isBestValue = "Best Value".equalsIgnoreCase(setupTier) || "Value".equalsIgnoreCase(setupTier);
        boolean isHighTier = "High Tier".equalsIgnoreCase(setupTier) || "High".equalsIgnoreCase(setupTier);
        boolean isBestAvailable = "Best Available".equalsIgnoreCase(setupTier) || "Max Budget".equalsIgnoreCase(setupTier) || "Maximum Budget".equalsIgnoreCase(setupTier) || "Max".equalsIgnoreCase(setupTier) || "Max BiS".equalsIgnoreCase(setupTier) || "BiS".equalsIgnoreCase(setupTier);
        if (!isBudget && !isBestValue && !isHighTier && !isBestAvailable) {
            isBestValue = true;
        }

        EquipmentSlot[] slotOrder = new EquipmentSlot[] {
                EquipmentSlot.WEAPON,
                EquipmentSlot.SHIELD,
                EquipmentSlot.AMULET,
                EquipmentSlot.BODY,
                EquipmentSlot.LEGS,
                EquipmentSlot.HEAD,
                EquipmentSlot.GLOVES,
                EquipmentSlot.BOOTS,
                EquipmentSlot.RING,
                EquipmentSlot.CAPE,
                EquipmentSlot.AMMO,
                EquipmentSlot.SPEC_WEAPON
        };

        Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : slotOrder) {
            List<EquipmentItem> candidates = new ArrayList<>();
            for (EquipmentItem item : equipmentDatabase.getItemsForSlot(slot)) {
                if (!canEquip(item, account)) continue;
                if (!isItemStyleCompatible(item, slot, combatStyle, profile)) continue;
                if (!isBossMechanicCompatible(item, slot, profile)) continue;

                candidates.add(item);
            }
            candidatesBySlot.put(slot, candidates);
        }

        Map<EquipmentSlot, EquipmentItem> chosenGear = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, String> slotReasons = new EnumMap<>(EquipmentSlot.class);
        List<String> highlights = new ArrayList<>();

        if (isBudget) {
            generateBudgetSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);
        } else if (isBestValue) {
            generateBestValueSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);
        } else if (isHighTier) {
            generateHighTierSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);
        } else {
            generateBestAvailableSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);
        }

        enforceCompatibility(chosenGear, candidatesBySlot, combatStyle, profile, budget);
        enforceBudgetLimit(chosenGear, candidatesBySlot, combatStyle, profile, budget);

        MonsterCombatProfile monsterProfile = (monster != null) ? monster.getCombatProfile() : null;

        // Stop guessing: re-pick every slot against the real combat maths for THIS
        // target, then re-apply the compatibility and budget rules on the result.
        // Keep Budget tier cost-bounded by its baseline so Budget <= Best Value holds.
        long refineBudget = isBudget ? calculateTotalCost(chosenGear) : (budget > 0 ? budget : -1L);
        refineByMeasuredDps(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile,
                refineBudget, variant, monsterProfile, account, onSlayerTask);
        enforceCompatibility(chosenGear, candidatesBySlot, combatStyle, profile, budget);
        enforceBudgetLimit(chosenGear, candidatesBySlot, combatStyle, profile, budget);

        // Perform genuine OSRS Combat Calculations
        CombatResult combatResult = calculationService.calculateCombat(
                chosenGear,
                variant,
                monsterProfile,
                account,
                combatStyle,
                onSlayerTask
        );

        generateSlotExplanations(chosenGear, slotReasons, candidatesBySlot, combatStyle, profile, boss);

        long totalCost = calculateTotalCost(chosenGear);

        String targetName = (monster != null) ? monster.getName() : (boss != null ? boss.getName() : "Target");
        highlights.add("Tailored for " + targetName + " using " + combatStyle.getDisplayName() + " combat.");
        highlights.add(String.format("Calculated Performance: %.2f DPS (Hit Chance: %s, Max Hit: %d).",
                combatResult.getDps(), combatResult.getFormattedHitChance(), combatResult.getMaxHit()));

        if (profile.isDragon()) highlights.add("Dragon encounter: Dragonfire protection & dragonbane bonuses active.");
        if (profile.isUndead()) highlights.add("Undead encounter: Salve amulet (ei) prioritized (+20% dmg/acc).");
        if (profile.isDemon()) highlights.add("Demon encounter: Demonbane bonuses prioritized (+70% accuracy).");
        if (profile.isRequiresCrush()) highlights.add("Crush weakness: Crush-specialized weaponry strictly enforced.");
        if (profile.isRequiresSpear()) highlights.add("Corporeal Beast mechanic: Spear weaponry strictly enforced (100% damage).");
        if (onSlayerTask) highlights.add("On Slayer Task: Slayer helmet (i) bonuses applied (+16.67% melee / +15% range/mage).");

        highlights.add("Total loadout cost: " + formatGp(totalCost) + " GP (Remaining: " + formatGp(Math.max(0, budget - totalCost)) + " GP).");

        OptimizedGearSetup setup = new OptimizedGearSetup(
                setupTier,
                boss,
                monster,
                combatStyle,
                chosenGear,
                totalCost,
                budget,
                slotReasons,
                highlights,
                combatResult
        );

        validateSetup(setup, boss, combatStyle, budget, account);

        return setup;
    }

    private void generateBudgetSetup(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            EquipmentSlot[] slotOrder,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        for (EquipmentSlot slot : slotOrder) {
            if (slot == EquipmentSlot.SPEC_WEAPON) continue;

            List<EquipmentItem> candidates = candidatesBySlot.getOrDefault(slot, Collections.emptyList());
            if (candidates.isEmpty()) continue;

            List<EquipmentItem> sorted = new ArrayList<>(candidates);
            sorted.sort(Comparator.comparingLong(this::getItemPrice)
                    .thenComparing(Comparator.comparingDouble((EquipmentItem item) -> calculateItemScore(item, profile, combatStyle)).reversed()));

            EquipmentItem selected = null;
            for (EquipmentItem item : sorted) {
                if (calculateItemScore(item, profile, combatStyle) > 0) {
                    selected = item;
                    break;
                }
            }
            if (selected == null && !sorted.isEmpty()) {
                selected = sorted.get(0);
            }
            if (selected != null) {
                chosenGear.put(slot, selected);
            }
        }
    }

    private void generateBestValueSetup(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            EquipmentSlot[] slotOrder,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        generateBudgetSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);

        List<EquipmentItem> specCandidates = candidatesBySlot.getOrDefault(EquipmentSlot.SPEC_WEAPON, Collections.emptyList());
        for (EquipmentItem spec : specCandidates) {
            if (spec.getName().contains("dagger") && getItemPrice(spec) <= 50_000) {
                chosenGear.put(EquipmentSlot.SPEC_WEAPON, spec);
                break;
            }
        }

        boolean improved = true;
        while (improved) {
            improved = false;
            EquipmentSlot bestSlotToUpgrade = null;
            EquipmentItem bestCandidate = null;
            double bestEfficiency = -1.0;

            long currentTotal = calculateTotalCost(chosenGear);

            for (EquipmentSlot slot : slotOrder) {
                EquipmentItem current = chosenGear.get(slot);
                double currentScore = (current != null) ? calculateItemScore(current, profile, combatStyle) : 0.0;
                long currentPrice = (current != null) ? getItemPrice(current) : 0L;

                for (EquipmentItem candidate : candidatesBySlot.getOrDefault(slot, Collections.emptyList())) {
                    if (candidate == current) continue;
                    if (slot == EquipmentSlot.AMMO && candidate.getName().toLowerCase().contains("dragon")) {
                        EquipmentItem currentWeapon = chosenGear.get(EquipmentSlot.WEAPON);
                        if (currentWeapon != null && !currentWeapon.getName().contains("Dragon") && !currentWeapon.getName().contains("Armadyl") && !currentWeapon.getName().contains("hunter") && !currentWeapon.getName().contains("Zaryte")) {
                            continue;
                        }
                    }
                    if (candidate.getTier() > 7 && !candidate.isUntradeable() && getItemPrice(candidate) > 0) continue;
                    if (slot == EquipmentSlot.AMMO && candidate.getName().toLowerCase().contains("dragon")) {
                        EquipmentItem currentWeapon = chosenGear.get(EquipmentSlot.WEAPON);
                        if (currentWeapon != null && !currentWeapon.getName().contains("Dragon") && !currentWeapon.getName().contains("Armadyl") && !currentWeapon.getName().contains("hunter") && !currentWeapon.getName().contains("Zaryte")) {
                            continue;
                        }
                    }

                    double candidateScore = calculateItemScore(candidate, profile, combatStyle);
                    if (candidateScore <= currentScore) continue;

                    long candidatePrice = getItemPrice(candidate);
                    long netCost = candidatePrice - currentPrice;
                    if (currentTotal + netCost > budget) continue;

                    double efficiency = (candidateScore - currentScore) / (Math.max(1_000L, netCost) / 1_000.0);
                    if (netCost <= 0) efficiency = 999_999.0;

                    if (efficiency > bestEfficiency) {
                        bestEfficiency = efficiency;
                        bestSlotToUpgrade = slot;
                        bestCandidate = candidate;
                    }
                }
            }

            if (bestCandidate != null && bestEfficiency > 0.05) {
                chosenGear.put(bestSlotToUpgrade, bestCandidate);
                improved = true;
            }
        }
    }

    private void generateHighTierSetup(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            EquipmentSlot[] slotOrder,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        generateBestValueSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);

        boolean improved = true;
        while (improved) {
            improved = false;
            EquipmentSlot bestSlotToUpgrade = null;
            EquipmentItem bestCandidate = null;
            double bestScoreGain = 0.0;

            long currentTotal = calculateTotalCost(chosenGear);

            for (EquipmentSlot slot : slotOrder) {
                EquipmentItem current = chosenGear.get(slot);
                double currentScore = (current != null) ? calculateItemScore(current, profile, combatStyle) : 0.0;
                long currentPrice = (current != null) ? getItemPrice(current) : 0L;

                for (EquipmentItem candidate : candidatesBySlot.getOrDefault(slot, Collections.emptyList())) {
                    if (candidate == current) continue;
                    if (slot == EquipmentSlot.AMMO && candidate.getName().toLowerCase().contains("dragon")) {
                        EquipmentItem currentWeapon = chosenGear.get(EquipmentSlot.WEAPON);
                        if (currentWeapon != null && !currentWeapon.getName().contains("Dragon") && !currentWeapon.getName().contains("Armadyl") && !currentWeapon.getName().contains("hunter") && !currentWeapon.getName().contains("Zaryte")) {
                            continue;
                        }
                    }
                    if (candidate.getTier() > 8) continue; // High-tier cap (Tier 8)
                    if (slot == EquipmentSlot.AMMO && candidate.getName().toLowerCase().contains("dragon")) {
                        EquipmentItem currentWeapon = chosenGear.get(EquipmentSlot.WEAPON);
                        if (currentWeapon != null && !currentWeapon.getName().contains("Dragon") && !currentWeapon.getName().contains("Armadyl") && !currentWeapon.getName().contains("hunter") && !currentWeapon.getName().contains("Zaryte")) {
                            continue;
                        }
                    }

                    double candidateScore = calculateItemScore(candidate, profile, combatStyle);
                    double scoreGain = candidateScore - currentScore;
                    if (scoreGain <= 0.001) continue;

                    long candidatePrice = getItemPrice(candidate);
                    if (candidatePrice < currentPrice) continue; // Best Available only upgrades investment
                    long netCost = candidatePrice - currentPrice;
                    if (currentTotal + netCost > budget) continue;

                    if (scoreGain > bestScoreGain) {
                        bestScoreGain = scoreGain;
                        bestSlotToUpgrade = slot;
                        bestCandidate = candidate;
                    }
                }
            }

            if (bestCandidate != null) {
                chosenGear.put(bestSlotToUpgrade, bestCandidate);
                improved = true;
            }
        }
    }

    private void generateBestAvailableSetup(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            EquipmentSlot[] slotOrder,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        generateBestValueSetup(chosenGear, slotOrder, candidatesBySlot, combatStyle, profile, budget);

        boolean improved = true;
        while (improved) {
            improved = false;
            EquipmentSlot bestSlotToUpgrade = null;
            EquipmentItem bestCandidate = null;
            double bestScoreGain = 0.0;

            long currentTotal = calculateTotalCost(chosenGear);

            for (EquipmentSlot slot : slotOrder) {
                EquipmentItem current = chosenGear.get(slot);
                double currentScore = (current != null) ? calculateItemScore(current, profile, combatStyle) : 0.0;
                long currentPrice = (current != null) ? getItemPrice(current) : 0L;

                for (EquipmentItem candidate : candidatesBySlot.getOrDefault(slot, Collections.emptyList())) {
                    if (candidate == current) continue;
                    if (slot == EquipmentSlot.AMMO && candidate.getName().toLowerCase().contains("dragon")) {
                        EquipmentItem currentWeapon = chosenGear.get(EquipmentSlot.WEAPON);
                        if (currentWeapon != null && !currentWeapon.getName().contains("Dragon") && !currentWeapon.getName().contains("Armadyl") && !currentWeapon.getName().contains("hunter") && !currentWeapon.getName().contains("Zaryte")) {
                            continue;
                        }
                    }

                    double candidateScore = calculateItemScore(candidate, profile, combatStyle);
                    double scoreGain = candidateScore - currentScore;
                    if (scoreGain <= 0.001) continue;

                    long candidatePrice = getItemPrice(candidate);
                    if (candidatePrice < currentPrice) continue; // Best Available only upgrades investment
                    long netCost = candidatePrice - currentPrice;
                    if (currentTotal + netCost > budget) continue;

                    if (scoreGain > bestScoreGain) {
                        bestScoreGain = scoreGain;
                        bestSlotToUpgrade = slot;
                        bestCandidate = candidate;
                    }
                }
            }

            if (bestCandidate != null) {
                chosenGear.put(bestSlotToUpgrade, bestCandidate);
                improved = true;
            }
        }
    }

    private void enforceCompatibility(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        EquipmentItem weapon = chosenGear.get(EquipmentSlot.WEAPON);

        if (weapon != null && weapon.isTwoHanded()) {
            chosenGear.remove(EquipmentSlot.SHIELD);
        } else if (weapon != null && !weapon.isTwoHanded()) {
            if (!chosenGear.containsKey(EquipmentSlot.SHIELD) || chosenGear.get(EquipmentSlot.SHIELD) == null) {
                List<EquipmentItem> shieldCandidates = candidatesBySlot.getOrDefault(EquipmentSlot.SHIELD, Collections.emptyList());
                for (EquipmentItem s : shieldCandidates) {
                    if (calculateTotalCost(chosenGear) + getItemPrice(s) <= budget) {
                        chosenGear.put(EquipmentSlot.SHIELD, s);
                        break;
                    }
                }
            }
        }

        if (weapon != null && combatStyle == CombatStyle.RANGED) {
            AmmoType neededAmmo = weapon.getAmmoType();
            if (neededAmmo == AmmoType.BOLT) {
                EquipmentItem ammo = chosenGear.get(EquipmentSlot.AMMO);
                boolean isDragonCrossbow = weapon.getName().contains("Dragon") || weapon.getName().contains("Armadyl") || weapon.getName().contains("hunter");
                if (ammo == null || ammo.getAmmoType() != AmmoType.BOLT || (!isDragonCrossbow && ammo.getName().contains("dragon"))) {
                    List<EquipmentItem> boltCandidates = new ArrayList<>(candidatesBySlot.getOrDefault(EquipmentSlot.AMMO, Collections.emptyList()));
                    boltCandidates.sort(Comparator.comparingDouble((EquipmentItem it) -> calculateItemScore(it, profile, combatStyle)).reversed());
                    for (EquipmentItem candidate : boltCandidates) {
                        if (candidate.getAmmoType() == AmmoType.BOLT) {
                            if (!isDragonCrossbow && candidate.getName().contains("dragon")) continue;
                            chosenGear.put(EquipmentSlot.AMMO, candidate);
                            break;
                        }
                    }
                }
            } else if (neededAmmo == AmmoType.ARROW) {
                EquipmentItem ammo = chosenGear.get(EquipmentSlot.AMMO);
                if (ammo == null || ammo.getAmmoType() != AmmoType.ARROW) {
                    for (EquipmentItem candidate : candidatesBySlot.getOrDefault(EquipmentSlot.AMMO, Collections.emptyList())) {
                        if (candidate.getAmmoType() == AmmoType.ARROW) {
                            chosenGear.put(EquipmentSlot.AMMO, candidate);
                            break;
                        }
                    }
                }
            } else {
                EquipmentItem ammo = chosenGear.get(EquipmentSlot.AMMO);
                if (ammo != null && ammo.getAmmoType() != AmmoType.BLESSING) {
                    chosenGear.remove(EquipmentSlot.AMMO);
                }
            }
        } else if (combatStyle == CombatStyle.MELEE || combatStyle == CombatStyle.MAGIC) {
            EquipmentItem ammo = chosenGear.get(EquipmentSlot.AMMO);
            if (ammo != null && ammo.getAmmoType() != AmmoType.BLESSING) {
                chosenGear.remove(EquipmentSlot.AMMO);
            }
        }
    }

    private void enforceBudgetLimit(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget) {

        long total = calculateTotalCost(chosenGear);
        if (total <= budget) return;

        EquipmentSlot[] downgradeSlots = new EquipmentSlot[] {
                EquipmentSlot.SPEC_WEAPON,
                EquipmentSlot.RING,
                EquipmentSlot.CAPE,
                EquipmentSlot.BOOTS,
                EquipmentSlot.HEAD,
                EquipmentSlot.LEGS,
                EquipmentSlot.BODY,
                EquipmentSlot.SHIELD,
                EquipmentSlot.AMULET,
                EquipmentSlot.WEAPON
        };

        for (EquipmentSlot slot : downgradeSlots) {
            if (total <= budget) break;

            if (slot == EquipmentSlot.SPEC_WEAPON) {
                chosenGear.remove(slot);
                total = calculateTotalCost(chosenGear);
                continue;
            }

            EquipmentItem current = chosenGear.get(slot);
            if (current == null) continue;

            List<EquipmentItem> candidates = new ArrayList<>(candidatesBySlot.getOrDefault(slot, Collections.emptyList()));
            candidates.sort(Comparator.comparingLong(this::getItemPrice));

            for (EquipmentItem cheaper : candidates) {
                long cheaperPrice = getItemPrice(cheaper);
                if (cheaperPrice < getItemPrice(current)) {
                    total = total - getItemPrice(current) + cheaperPrice;
                    chosenGear.put(slot, cheaper);
                    current = cheaper;
                    if (total <= budget) break;
                }
            }

            if (total > budget && (slot == EquipmentSlot.RING || slot == EquipmentSlot.CAPE || slot == EquipmentSlot.BOOTS || slot == EquipmentSlot.AMMO)) {
                EquipmentItem removed = chosenGear.remove(slot);
                if (removed != null) {
                    total -= getItemPrice(removed);
                }
            }
        }

        if (total > budget) {
            for (EquipmentSlot slot : downgradeSlots) {
                if (total <= budget) break;
                if (slot == EquipmentSlot.WEAPON) continue;
                EquipmentItem removed = chosenGear.remove(slot);
                if (removed != null) {
                    total -= getItemPrice(removed);
                }
            }
        }
    }

    public double calculateItemScore(EquipmentItem item, BossCombatProfile profile, CombatStyle style) {
        if (item == null) return 0.0;

        double score = 0.0;

        double speedMult = 1.0;
        if (item.getSlot() == EquipmentSlot.WEAPON) {
            int speed = item.getAttackSpeed();
            if (speed == 2) speedMult = 1.60;
            else if (speed == 3) speedMult = 1.35;
            else if (speed == 4) speedMult = 1.25;
            else if (speed == 5) speedMult = 1.00;
            else speedMult = 0.75;
        }

        if (style == CombatStyle.MELEE) {
            score += item.getMeleeStrength() * 3.5;

            if (profile.isRequiresCrush()) {
                score += item.getCrushAttack() * 1.5;
                if (item.getSlot() == EquipmentSlot.WEAPON && item.getCrushAttack() <= 0) {
                    score -= 200.0;
                }
            } else if (profile.getFavoredAttackType() == AttackType.STAB) {
                score += item.getStabAttack() * 1.5;
            } else if (profile.getFavoredAttackType() == AttackType.SLASH) {
                score += item.getSlashAttack() * 1.4;
            } else {
                score += Math.max(item.getSlashAttack(), Math.max(item.getStabAttack(), item.getCrushAttack())) * 1.0;
            }

            if (profile.getFavoredAttackType() == AttackType.SLASH && item.getName().contains("Bellator")) {
                score += 35.0; // BiS slash ring
            }
        } else if (style == CombatStyle.RANGED) {
            score += item.getRangedStrength() * 3.5;
            score += item.getRangedAttack() * 1.2;

            if (item.getRangedAttack() < 0) {
                score += item.getRangedAttack() * 3.0;
            }

            if (item.getName().contains("Venator ring")) {
                score += 25.0;
            }

            // Crossbow bolts mechanics
            if (item.getName().contains("Sapphire dragon bolts (e)") || item.getName().contains("Sapphire bolts (e)")) {
                if (profile.getBossId().contains("muspah")) {
                    score += 120.0; // Crucial shield-phase mechanic
                }
            } else if (item.getName().contains("Ruby dragon bolts (e)") || item.getName().contains("Ruby bolts (e)")) {
                if (profile.getBossId().contains("vorkath") || profile.getBossId().contains("nex") || profile.getBossId().contains("leviathan")) {
                    score += 80.0; // High HP boss execution
                }
            }
        } else if (style == CombatStyle.MAGIC) {
            score += item.getMagicDamagePercent() * 10.0;
            score += item.getMagicAttack() * 1.2;

            if (item.getMagicAttack() < 0) {
                score += item.getMagicAttack() * 3.0;
            }

            if (item.getName().contains("Magus ring")) {
                score += 30.0;
            }
        }

        score *= speedMult;

        if (item.isUndeadBonus()) {
            if (profile.isUndead()) {
                score += 250.0;
            } else {
                score = -500.0;
            }
        }

        if (item.isDragonbane()) {
            if (profile.isDragon()) {
                score += 280.0;
            }
        }

        if (item.isDemonbane()) {
            if (profile.isDemon()) {
                score += 480.0;
            }
        }

        if (item.isRatbane() || item.getName().toLowerCase().contains("bone ")) {
            if (profile.isRat()) {
                score += 200.0;
            } else {
                score = -500.0;
            }
        }

        if (item.isWildernessBonus()) {
            if (profile.isWilderness()) {
                score += 150.0;
            }
        }

        if (profile.isRequiresSpear()) {
            if (item.getSlot() == EquipmentSlot.WEAPON) {
                if (item.isSpear()) {
                    score += 200.0;
                } else if (item.getName().contains("fang")) {
                    score += 50.0;
                } else {
                    score *= 0.50;
                }
            }
        }

        if (profile.isRequiresLeafBladed() && item.isLeafBladed()) {
            score += 200.0;
        }

        if (profile.isRequiresDragonfire() && item.isDragonfireProtection()) {
            score += 300.0;
        }

        if (profile.isRequiresWyvernShield() && (item.getName().toLowerCase().contains("wyvern") || item.getName().toLowerCase().contains("elemental") || item.isDragonfireProtection())) {
            score += 250.0;
        }

        if (profile.isRequiresMirrorShield() && item.isMirrorShield()) {
            score += 300.0;
        }

        if (profile.isRequiresBootsOfStone() && item.isBootsOfStone()) {
            score += 300.0;
        }

        if (profile.isFavorsVenomProtection() && item.isVenomProtection()) {
            score += 40.0;
        }

        if (profile.isFavorsRecoil() && item.getName().contains("Ring of suffering")) {
            score += 80.0;
        }

        if (item.getName().contains("Blood fury")) {
            score += 25.0; // High sustain
        }

        score += item.getPrayerBonus() * 2.0;
        score += item.getAverageDefenceBonus() * 0.15;

        return Math.max(0.1, score);
    }

    private boolean isItemStyleCompatible(EquipmentItem item, EquipmentSlot slot, CombatStyle style, BossCombatProfile profile) {
        if (slot == EquipmentSlot.WEAPON) {
            return item.getPrimaryStyle() == style;
        }

        if (slot == EquipmentSlot.SPEC_WEAPON) {
            return item.getPrimaryStyle() == CombatStyle.MELEE || item.getPrimaryStyle() == style;
        }

        if (slot == EquipmentSlot.AMMO) {
            if (style == CombatStyle.RANGED) {
                return item.getAmmoType() == AmmoType.BOLT || item.getAmmoType() == AmmoType.ARROW || item.getAmmoType() == AmmoType.BLESSING;
            }
            return item.getAmmoType() == AmmoType.BLESSING;
        }

        if (slot == EquipmentSlot.SHIELD) {
            if (profile.isRequiresDragonfire()) {
                return item.isDragonfireProtection();
            }
            if (style == CombatStyle.MELEE) {
                return item.getPrimaryStyle() == CombatStyle.MELEE || item.getPrimaryStyle() == CombatStyle.HYBRID;
            }
            if (style == CombatStyle.RANGED) {
                return item.getPrimaryStyle() == CombatStyle.RANGED || item.getPrimaryStyle() == CombatStyle.HYBRID;
            }
            if (style == CombatStyle.MAGIC) {
                return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getPrimaryStyle() == CombatStyle.HYBRID;
            }
        }

        if (slot == EquipmentSlot.BODY || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.HEAD) {
            if (style == CombatStyle.MELEE) {
                return item.getPrimaryStyle() == CombatStyle.MELEE || item.getPrimaryStyle() == CombatStyle.HYBRID;
            }
            if (style == CombatStyle.RANGED) {
                return item.getPrimaryStyle() == CombatStyle.RANGED || item.getRangedAttack() >= -5;
            }
            if (style == CombatStyle.MAGIC) {
                return item.getPrimaryStyle() == CombatStyle.MAGIC || item.getMagicAttack() >= -5;
            }
        }

        if (item.isUndeadBonus() && !profile.isUndead()) {
            return false;
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

        return true;
    }

    private boolean isBossMechanicCompatible(EquipmentItem item, EquipmentSlot slot, BossCombatProfile profile) {
        if (slot == EquipmentSlot.WEAPON) {
            if ((profile.isRequiresMirrorShield() || profile.isRequiresWyvernShield()) && item.isTwoHanded()) {
                return false;
            }
            // Ratbane weapons (Bone mace, Bone bow, Bone shortbow, Bone staff) are only for rats (Scurrius)
            if ((item.isRatbane() || item.getName().toLowerCase().contains("bone ")) && !profile.isRat()) {
                return false;
            }
            // Demonbane weapons with poor base stats (Arclight, Darklight, Silverlight) are only for demons
            if (item.isDemonbane() && !profile.isDemon() && (item.getName().contains("Arclight") || item.getName().contains("Darklight") || item.getName().contains("Silverlight"))) {
                return false;
            }
            if (profile.isRequiresSpear()) {
                return item.isSpear() || item.getName().contains("fang");
            }
            if (profile.isRequiresLeafBladed()) {
                return item.isLeafBladed();
            }
            if (profile.isRequiresCrush() && item.getPrimaryStyle() == CombatStyle.MELEE) {
                return item.getCrushAttack() > 0 || item.getAttackType() == AttackType.CRUSH;
            }
            if (profile.getFavoredAttackType() == AttackType.SLASH && profile.getAllowedStyles().size() == 1 && profile.getAllowedStyles().contains(CombatStyle.MELEE)) {
                return item.getSlashAttack() > 0 || item.getAttackType() == AttackType.SLASH;
            }
        }
        if (slot == EquipmentSlot.AMULET) {
            if (item.isUndeadBonus() && !profile.isUndead()) {
                return false;
            }
        }
        if (slot == EquipmentSlot.SHIELD) {
            if (profile.isRequiresDragonfire()) {
                return item.isDragonfireProtection();
            }
            if (profile.isRequiresWyvernShield()) {
                String n = item.getName().toLowerCase();
                return n.contains("wyvern") || n.contains("elemental") || n.contains("mind") || item.isDragonfireProtection();
            }
            if (profile.isRequiresMirrorShield()) {
                return item.isMirrorShield();
            }
        }
        if (slot == EquipmentSlot.BOOTS) {
            if (profile.isRequiresBootsOfStone()) {
                return item.isBootsOfStone();
            }
        }
        return true;
    }

    private void generateSlotExplanations(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            Map<EquipmentSlot, String> slotReasons,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle style,
            BossCombatProfile profile,
            BossData boss) {

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            EquipmentItem item = chosenGear.get(slot);
            if (item == null) {
                if (slot == EquipmentSlot.SHIELD && chosenGear.containsKey(EquipmentSlot.WEAPON) && chosenGear.get(EquipmentSlot.WEAPON).isTwoHanded()) {
                    slotReasons.put(slot, "Slot empty: Two-handed weapon equipped.");
                } else if (slot == EquipmentSlot.SPEC_WEAPON) {
                    slotReasons.put(slot, "None: Budget prioritized for primary equipment.");
                } else {
                    slotReasons.put(slot, "None / Empty.");
                }
                continue;
            }

            StringBuilder sb = new StringBuilder();

            if (item.isUntradeable()) {
                sb.append("Untradeable staple; 0 GP cost with top-tier performance.");
            } else if (!item.getSpecialEffect().isEmpty()) {
                sb.append(item.getSpecialEffect());
            } else if (slot == EquipmentSlot.WEAPON) {
                sb.append("High-efficiency ").append(item.getAttackSpeed()).append("-tick ").append(item.getAttackType().name().toLowerCase()).append(" weapon.");
            } else if (item.getMeleeStrength() > 0 && style == CombatStyle.MELEE) {
                sb.append("Provides +").append(item.getMeleeStrength()).append(" Melee Strength for high max hits.");
            } else if (item.getRangedStrength() > 0 && style == CombatStyle.RANGED) {
                sb.append("Provides +").append(item.getRangedStrength()).append(" Ranged Strength.");
            } else if (item.getMagicDamagePercent() > 0 && style == CombatStyle.MAGIC) {
                sb.append("Amplifies magic damage by +").append(item.getMagicDamagePercent()).append("%.");
            } else {
                sb.append("Optimal stat-to-cost ratio for this slot.");
            }

            EquipmentItem cheaperAlt = findCheaperAlternative(item, candidatesBySlot.getOrDefault(slot, Collections.emptyList()), profile, style);
            if (cheaperAlt != null && getItemPrice(cheaperAlt) < getItemPrice(item)) {
                long savings = getItemPrice(item) - getItemPrice(cheaperAlt);
                sb.append(" Cheaper alt: ").append(cheaperAlt.getName())
                        .append(" (saves ").append(formatGp(savings)).append(" GP; ")
                        .append(describeTradeoff(item, cheaperAlt, style)).append(").");
            }

            slotReasons.put(slot, sb.toString());
        }
    }

    private EquipmentItem findCheaperAlternative(
            EquipmentItem current,
            List<EquipmentItem> candidates,
            BossCombatProfile profile,
            CombatStyle style) {

        long currentPrice = getItemPrice(current);
        if (currentPrice <= 15_000L || current.isUntradeable()) return null;

        EquipmentItem bestAlt = null;
        for (EquipmentItem cand : candidates) {
            if (cand == current) continue;
            long candPrice = getItemPrice(cand);
            if (candPrice < currentPrice) {
                if (bestAlt == null || candPrice > getItemPrice(bestAlt)) {
                    bestAlt = cand;
                }
            }
        }
        return bestAlt;
    }

    private String describeTradeoff(EquipmentItem current, EquipmentItem alt, CombatStyle style) {
        int curStr = (style == CombatStyle.RANGED) ? current.getRangedStrength() : current.getMeleeStrength();
        int altStr = (style == CombatStyle.RANGED) ? alt.getRangedStrength() : alt.getMeleeStrength();
        int strDiff = curStr - altStr;
        if (strDiff > 0) return "-" + strDiff + " Strength";

        int curMag = current.getMagicDamagePercent();
        int altMag = alt.getMagicDamagePercent();
        if (curMag - altMag > 0) return "-" + (curMag - altMag) + "% Magic Dmg";

        int curAtk = current.getStyleOffenceBonus(style);
        int altAtk = alt.getStyleOffenceBonus(style);
        if (curAtk - altAtk > 0) return "-" + (curAtk - altAtk) + " Accuracy";

        return "-" + Math.max(1, current.getTier() - alt.getTier()) + " Tier Power";
    }

    // =========================================================================
    // DPS-MEASURED SLOT SELECTION
    //
    // calculateItemScore() is a heuristic: it ranks items by stat bonuses and a
    // pile of name checks, which is why it used to hand out gear that loses DPS
    // against a specific monster's defences. These methods keep the heuristic as
    // the starting point and then test every legal candidate with the same combat
    // maths the DPS readout uses, keeping only swaps that actually measure higher.
    // =========================================================================

    /** Runs the real combat calculation for a gear map and returns its DPS (0 if it cannot be scored). */
    private double measureDps(
            Map<EquipmentSlot, EquipmentItem> gear,
            MonsterVariant variant,
            MonsterCombatProfile monsterProfile,
            PlayerAccount account,
            CombatStyle combatStyle,
            boolean onSlayerTask) {

        if (gear == null || gear.get(EquipmentSlot.WEAPON) == null) return 0.0;
        try {
            CombatResult result = calculationService.calculateCombat(
                    gear, variant, monsterProfile, account, combatStyle, onSlayerTask);
            if (result == null) return 0.0;
            double dps = result.getDps();
            return (Double.isNaN(dps) || Double.isInfinite(dps) || dps < 0) ? 0.0 : dps;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /** Copy of the gear map with one slot swapped, respecting two-handed weapons. Null when the swap is illegal. */
    private Map<EquipmentSlot, EquipmentItem> gearWith(
            Map<EquipmentSlot, EquipmentItem> gear,
            EquipmentSlot slot,
            EquipmentItem item,
            CombatStyle combatStyle) {

        Map<EquipmentSlot, EquipmentItem> trial = new EnumMap<>(gear);
        if (slot == EquipmentSlot.SHIELD) {
            EquipmentItem weapon = trial.get(EquipmentSlot.WEAPON);
            if (weapon != null && weapon.isTwoHanded()) return null;
        }
        if (slot == EquipmentSlot.AMMO) {
            EquipmentItem weapon = trial.get(EquipmentSlot.WEAPON);
            if (combatStyle != CombatStyle.RANGED || weapon == null) return null;
            if (item.getAmmoType() == null || item.getAmmoType() != weapon.getAmmoType()) return null;
            boolean dragonCapable = weapon.getName().contains("Dragon")
                    || weapon.getName().contains("Armadyl")
                    || weapon.getName().contains("hunter");
            if (!dragonCapable && item.getName().toLowerCase().contains("dragon")) return null;
        }
        trial.put(slot, item);
        if (slot == EquipmentSlot.WEAPON && item.isTwoHanded()) {
            trial.remove(EquipmentSlot.SHIELD);
        }
        return trial;
    }

    /**
     * Re-picks each slot by measured DPS instead of heuristic score. Two passes, so
     * slots that depend on each other (two-hander vs shield, bow vs ammo) settle.
     * Only swaps that stay inside the budget and raise DPS are kept, so the
     * heuristic setup is the floor, never the ceiling.
     */
    private void refineByMeasuredDps(
            Map<EquipmentSlot, EquipmentItem> chosenGear,
            EquipmentSlot[] slotOrder,
            Map<EquipmentSlot, List<EquipmentItem>> candidatesBySlot,
            CombatStyle combatStyle,
            BossCombatProfile profile,
            long budget,
            MonsterVariant variant,
            MonsterCombatProfile monsterProfile,
            PlayerAccount account,
            boolean onSlayerTask) {

        if (variant == null || chosenGear.isEmpty()) return;

        double bestDps = measureDps(chosenGear, variant, monsterProfile, account, combatStyle, onSlayerTask);
        if (bestDps <= 0) return;

        for (int pass = 0; pass < 2; pass++) {
            boolean improved = false;
            for (EquipmentSlot slot : slotOrder) {
                if (slot == EquipmentSlot.SPEC_WEAPON) continue;

                List<EquipmentItem> candidates = candidatesBySlot.getOrDefault(slot, Collections.emptyList());
                if (candidates.isEmpty()) continue;

                for (EquipmentItem candidate : candidates) {
                    if (candidate == null || candidate == chosenGear.get(slot)) continue;

                    Map<EquipmentSlot, EquipmentItem> trial = gearWith(chosenGear, slot, candidate, combatStyle);
                    if (trial == null) continue;
                    if (budget >= 0 && calculateTotalCost(trial) > budget) continue;

                    double dps = measureDps(trial, variant, monsterProfile, account, combatStyle, onSlayerTask);
                    if (dps > bestDps + 0.0005) {
                        bestDps = dps;
                        chosenGear.clear();
                        chosenGear.putAll(trial);
                        improved = true;
                    }
                }
            }
            if (!improved) break;
        }
    }

    private long calculateTotalCost(Map<EquipmentSlot, EquipmentItem> gear) {
        long total = 0L;
        for (EquipmentItem item : gear.values()) {
            if (item != null) {
                total += getItemPrice(item);
            }
        }
        return total;
    }

    public void validateSetup(
            OptimizedGearSetup setup,
            BossData boss,
            CombatStyle style,
            long budget,
            PlayerAccount account) {

        if (setup == null) {
            throw new IllegalStateException("Setup cannot be null");
        }

        if (setup.getTotalCost() > budget) {
            throw new IllegalStateException("Total cost " + setup.getTotalCost() + " GP exceeded budget " + budget + " GP!");
        }

        BossCombatProfile profile = getBossProfile(boss);

        for (Map.Entry<EquipmentSlot, EquipmentItem> entry : setup.getEquipment().entrySet()) {
            EquipmentItem item = entry.getValue();
            if (item == null) continue;

            if (!canEquip(item, account)) {
                throw new IllegalStateException("Player cannot equip item: " + item.getName());
            }

            if (item.isUndeadBonus() && !profile.isUndead()) {
                throw new IllegalStateException("Salve amulet (ei) equipped for non-undead boss: " + (boss != null ? boss.getName() : "Unknown"));
            }
        }

        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        if (weapon != null && weapon.isTwoHanded()) {
            if (setup.getItem(EquipmentSlot.SHIELD) != null) {
                throw new IllegalStateException("Shield must be null when two-handed weapon is equipped!");
            }
        }

        if (weapon != null && style == CombatStyle.RANGED) {
            EquipmentItem ammo = setup.getItem(EquipmentSlot.AMMO);
            if (weapon.getAmmoType() == AmmoType.BOLT) {
                if (ammo == null || ammo.getAmmoType() != AmmoType.BOLT) {
                    throw new IllegalStateException("Crossbow weapon requires bolt ammunition!");
                }
                boolean isDragonXbow = weapon.getName().contains("Dragon") || weapon.getName().contains("Armadyl") || weapon.getName().contains("hunter");
                if (!isDragonXbow && ammo.getName().contains("dragon")) {
                    throw new IllegalStateException("Rune crossbow cannot fire dragon bolts!");
                }
            } else if (weapon.getAmmoType() == AmmoType.ARROW) {
                if (ammo == null || ammo.getAmmoType() != AmmoType.ARROW) {
                    throw new IllegalStateException("Bow weapon requires arrow ammunition!");
                }
            }
        }

        if (profile.isRequiresDragonfire()) {
            if (weapon != null && !weapon.isTwoHanded()) {
                EquipmentItem shield = setup.getItem(EquipmentSlot.SHIELD);
                if (shield == null || !shield.isDragonfireProtection()) {
                    throw new IllegalStateException("Boss " + profile.getBossName() + " requires dragonfire protection shield!");
                }
            }
        }

        if (profile.isRequiresSpear() && weapon != null) {
            if (!weapon.isSpear() && !weapon.getName().contains("fang")) {
                throw new IllegalStateException("Corporeal Beast requires spear weapon!");
            }
        }

        if (profile.isRequiresCrush() && weapon != null && style == CombatStyle.MELEE) {
            if (weapon.getCrushAttack() <= 0 && weapon.getAttackType() != AttackType.CRUSH) {
                throw new IllegalStateException("Boss " + profile.getBossName() + " requires crush weapon!");
            }
        }
    }

    public static String formatGp(long gp) {
        if (gp >= 1_000_000_000L) {
            return String.format("%.1fB", gp / 1_000_000_000.0);
        } else if (gp >= 1_000_000L) {
            return String.format("%.1fM", gp / 1_000_000.0);
        } else if (gp >= 1_000L) {
            return String.format("%dK", gp / 1_000L);
        }
        return String.valueOf(gp);
    }
}
