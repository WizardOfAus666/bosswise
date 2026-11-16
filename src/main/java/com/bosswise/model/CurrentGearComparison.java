package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CurrentGearComparison {
    private final Map<EquipmentSlot, EquipmentItem> currentGear;
    private final OptimizedGearSetup recommendedSetup;
    private final CombatResult currentCombat;
    private final CombatResult recommendedCombat;
    private final long currentGearValue;
    private final long recommendedGearValue;
    private final List<String> missingProtections;
    private final List<SlotUpgrade> slotUpgrades;

    public CurrentGearComparison(
            Map<EquipmentSlot, EquipmentItem> currentGear,
            OptimizedGearSetup recommendedSetup,
            CombatResult currentCombat,
            CombatResult recommendedCombat,
            long currentGearValue,
            long recommendedGearValue,
            List<String> missingProtections,
            List<SlotUpgrade> slotUpgrades) {
        this.currentGear = currentGear != null ? currentGear : Collections.emptyMap();
        this.recommendedSetup = recommendedSetup;
        this.currentCombat = currentCombat;
        this.recommendedCombat = recommendedCombat;
        this.currentGearValue = currentGearValue;
        this.recommendedGearValue = recommendedGearValue;
        this.missingProtections = missingProtections != null ? missingProtections : Collections.emptyList();
        this.slotUpgrades = slotUpgrades != null ? slotUpgrades : Collections.emptyList();
    }

    public Map<EquipmentSlot, EquipmentItem> getCurrentGear() { return currentGear; }
    public OptimizedGearSetup getRecommendedSetup() { return recommendedSetup; }
    public CombatResult getCurrentCombat() { return currentCombat; }
    public CombatResult getRecommendedCombat() { return recommendedCombat; }
    public long getCurrentGearValue() { return currentGearValue; }
    public long getRecommendedGearValue() { return recommendedGearValue; }
    public List<String> getMissingProtections() { return missingProtections; }
    public List<SlotUpgrade> getSlotUpgrades() { return slotUpgrades; }

    public double getDpsDifference() {
        if (currentCombat == null || recommendedCombat == null) return 0.0;
        return recommendedCombat.getDps() - currentCombat.getDps();
    }

    public double getDpsPercentageImprovement() {
        if (currentCombat == null || recommendedCombat == null || currentCombat.getDps() <= 0) return 0.0;
        return ((recommendedCombat.getDps() - currentCombat.getDps()) / currentCombat.getDps()) * 100.0;
    }

    public int getMaxHitDifference() {
        if (currentCombat == null || recommendedCombat == null) return 0;
        return recommendedCombat.getMaxHit() - currentCombat.getMaxHit();
    }

    public double getAccuracyDifference() {
        if (currentCombat == null || recommendedCombat == null) return 0.0;
        return (recommendedCombat.getHitChance() - currentCombat.getHitChance()) * 100.0;
    }

    public long getCostDifference() {
        return Math.max(0, recommendedGearValue - currentGearValue);
    }

    public static class SlotUpgrade {
        private final EquipmentSlot slot;
        private final EquipmentItem currentItem;
        private final EquipmentItem upgradeItem;
        private final long costDifference;
        private final String impactDescription;

        public SlotUpgrade(
                EquipmentSlot slot,
                EquipmentItem currentItem,
                EquipmentItem upgradeItem,
                long costDifference,
                String impactDescription) {
            this.slot = slot;
            this.currentItem = currentItem;
            this.upgradeItem = upgradeItem;
            this.costDifference = costDifference;
            this.impactDescription = impactDescription != null ? impactDescription : "";
        }

        public EquipmentSlot getSlot() { return slot; }
        public EquipmentItem getCurrentItem() { return currentItem; }
        public EquipmentItem getUpgradeItem() { return upgradeItem; }
        public long getCostDifference() { return costDifference; }
        public String getImpactDescription() { return impactDescription; }
    }
}
