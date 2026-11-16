package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizedGearSetup {
    private final String setupName;
    private final BossData boss;
    private final MonsterData monster;
    private final CombatStyle combatStyle;
    private final Map<EquipmentSlot, EquipmentItem> equipment;
    private final long totalCost;
    private final long budget;
    private final long remainingBudget;
    private final int totalMeleeStrength;
    private final int totalRangedStrength;
    private final int totalMagicDamagePercent;
    private final int totalOffenceBonus;
    private final int totalDefenceBonus;
    private final int totalPrayerBonus;
    private final Map<EquipmentSlot, String> slotReasons;
    private final List<String> highlights;
    private final CombatResult combatResult;
    private final String recommendationCategory; // "WIKI_RECOMMENDED", "BOSSWISE_OPTIMISED", "BUDGET_ALTERNATIVE", "PLAYER_OWNED"
    private final String wikiSourceUrl;
    private final String verificationStatus;

    public OptimizedGearSetup(
            String setupName,
            CombatStyle combatStyle,
            Map<EquipmentSlot, EquipmentItem> equipment,
            long totalCost,
            long budget,
            Map<EquipmentSlot, String> slotReasons,
            List<String> highlights) {
        this(setupName, null, null, combatStyle, equipment, totalCost, budget, slotReasons, highlights, null, "BOSSWISE_OPTIMISED", "", "VERIFIED");
    }

    public OptimizedGearSetup(
            String setupName,
            BossData boss,
            CombatStyle combatStyle,
            Map<EquipmentSlot, EquipmentItem> equipment,
            long totalCost,
            long budget,
            Map<EquipmentSlot, String> slotReasons,
            List<String> highlights) {
        this(setupName, boss, null, combatStyle, equipment, totalCost, budget, slotReasons, highlights, null, "BOSSWISE_OPTIMISED", "", "VERIFIED");
    }

    public OptimizedGearSetup(
            String setupName,
            BossData boss,
            MonsterData monster,
            CombatStyle combatStyle,
            Map<EquipmentSlot, EquipmentItem> equipment,
            long totalCost,
            long budget,
            Map<EquipmentSlot, String> slotReasons,
            List<String> highlights,
            CombatResult combatResult) {
        this(setupName, boss, monster, combatStyle, equipment, totalCost, budget, slotReasons, highlights, combatResult, "BOSSWISE_OPTIMISED", "", "VERIFIED");
    }

    public OptimizedGearSetup(
            String setupName,
            BossData boss,
            MonsterData monster,
            CombatStyle combatStyle,
            Map<EquipmentSlot, EquipmentItem> equipment,
            long totalCost,
            long budget,
            Map<EquipmentSlot, String> slotReasons,
            List<String> highlights,
            CombatResult combatResult,
            String recommendationCategory,
            String wikiSourceUrl,
            String verificationStatus) {
        this.setupName = setupName;
        this.boss = boss;
        this.monster = monster;
        this.combatStyle = combatStyle;
        this.equipment = Collections.unmodifiableMap(new HashMap<>(equipment));
        this.totalCost = totalCost;
        this.budget = budget;
        this.remainingBudget = Math.max(0, budget - totalCost);
        this.slotReasons = Collections.unmodifiableMap(new HashMap<>(slotReasons));
        this.highlights = Collections.unmodifiableList(new ArrayList<>(highlights));
        this.combatResult = combatResult;
        this.recommendationCategory = recommendationCategory != null ? recommendationCategory : "BOSSWISE_OPTIMISED";
        this.wikiSourceUrl = wikiSourceUrl != null ? wikiSourceUrl : "";
        this.verificationStatus = verificationStatus != null ? verificationStatus : "VERIFIED";

        // Calculate totals
        int mStr = 0;
        int rStr = 0;
        int mDmg = 0;
        int off = 0;
        int def = 0;
        int pray = 0;

        for (EquipmentItem item : equipment.values()) {
            if (item != null) {
                mStr += item.getMeleeStrength();
                rStr += item.getRangedStrength();
                mDmg += item.getMagicDamagePercent();
                off += item.getStyleOffenceBonus(combatStyle);
                def += item.getAverageDefenceBonus();
                pray += item.getPrayerBonus();
            }
        }

        this.totalMeleeStrength = mStr;
        this.totalRangedStrength = rStr;
        this.totalMagicDamagePercent = mDmg;
        this.totalOffenceBonus = off;
        this.totalDefenceBonus = def;
        this.totalPrayerBonus = pray;
    }

    public String getSetupName() { return setupName; }
    public String getTier() { return setupName; }
    public String getTierName() { return setupName; }
    public BossData getBoss() { return boss; }
    public MonsterData getMonster() { return monster; }
    public CombatStyle getCombatStyle() { return combatStyle; }
    public Map<EquipmentSlot, EquipmentItem> getEquipment() { return equipment; }
    public EquipmentItem getItem(EquipmentSlot slot) { return equipment.get(slot); }
    public long getTotalCost() { return totalCost; }
    public long getTotalPrice() { return totalCost; }
    public long getBudget() { return budget; }
    public long getRemainingBudget() { return remainingBudget; }
    public int getTotalMeleeStrength() { return totalMeleeStrength; }
    public int getTotalRangedStrength() { return totalRangedStrength; }
    public int getTotalMagicDamagePercent() { return totalMagicDamagePercent; }
    public int getTotalOffenceBonus() { return totalOffenceBonus; }
    public int getTotalDefenceBonus() { return totalDefenceBonus; }
    public int getTotalPrayerBonus() { return totalPrayerBonus; }
    public Map<EquipmentSlot, String> getSlotReasons() { return slotReasons; }
    public String getReason(EquipmentSlot slot) { return slotReasons.getOrDefault(slot, ""); }
    public List<String> getHighlights() { return highlights; }
    public CombatResult getCombatResult() { return combatResult; }

    public double getCalculatedDps() {
        return (combatResult != null) ? combatResult.getDps() : 0.0;
    }

    public int getMaxHit() {
        return (combatResult != null) ? combatResult.getMaxHit() : 0;
    }

    public String getHitChance() {
        return (combatResult != null) ? combatResult.getFormattedHitChance() : "0.0%";
    }

    public String getRecommendationCategory() { return recommendationCategory; }
    public String getWikiSourceUrl() { return wikiSourceUrl; }
    public String getVerificationStatus() { return verificationStatus; }

    public boolean isWikiRecommended() {
        return "WIKI_RECOMMENDED".equalsIgnoreCase(recommendationCategory);
    }

    public boolean isBudgetAlternative() {
        return "BUDGET_ALTERNATIVE".equalsIgnoreCase(recommendationCategory);
    }

    public boolean isPlayerOwned() {
        return "PLAYER_OWNED".equalsIgnoreCase(recommendationCategory);
    }

    public boolean isBossWiseOptimised() {
        return "BOSSWISE_OPTIMISED".equalsIgnoreCase(recommendationCategory);
    }

    public String getCategoryBadgeLabel() {
        if (isWikiRecommended()) return "WIKI RECOMMENDED";
        if (isBudgetAlternative()) return "BUDGET ALTERNATIVE";
        if (isPlayerOwned()) return "PLAYER OWNED";
        return "BOSSWISE OPTIMISED";
    }
}
