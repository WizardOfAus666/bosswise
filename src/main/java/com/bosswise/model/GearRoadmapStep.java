package com.bosswise.model;

import java.util.Collections;
import java.util.List;

public class GearRoadmapStep {
    private final int priorityRank;
    private final EquipmentSlot slot;
    private final EquipmentItem currentItem;
    private final EquipmentItem suggestedItem;
    private final long currentPrice;
    private final long suggestedPrice;
    private final long upgradeCost;
    private final double dpsImprovement;
    private final int defenceImprovement;
    private final List<String> requirements;
    private final String reason;
    private final boolean canEquipNow;

    public GearRoadmapStep(
            int priorityRank,
            EquipmentSlot slot,
            EquipmentItem currentItem,
            EquipmentItem suggestedItem,
            long currentPrice,
            long suggestedPrice,
            long upgradeCost,
            double dpsImprovement,
            int defenceImprovement,
            List<String> requirements,
            String reason,
            boolean canEquipNow) {
        this.priorityRank = priorityRank;
        this.slot = slot;
        this.currentItem = currentItem;
        this.suggestedItem = suggestedItem;
        this.currentPrice = currentPrice;
        this.suggestedPrice = suggestedPrice;
        this.upgradeCost = upgradeCost;
        this.dpsImprovement = dpsImprovement;
        this.defenceImprovement = defenceImprovement;
        this.requirements = requirements != null ? requirements : Collections.emptyList();
        this.reason = reason != null ? reason : "";
        this.canEquipNow = canEquipNow;
    }

    public int getPriorityRank() { return priorityRank; }
    public EquipmentSlot getSlot() { return slot; }
    public EquipmentItem getCurrentItem() { return currentItem; }
    public EquipmentItem getSuggestedItem() { return suggestedItem; }
    public long getCurrentPrice() { return currentPrice; }
    public long getSuggestedPrice() { return suggestedPrice; }
    public long getUpgradeCost() { return upgradeCost; }
    public double getDpsImprovement() { return dpsImprovement; }
    public int getDefenceImprovement() { return defenceImprovement; }
    public List<String> getRequirements() { return requirements; }
    public String getReason() { return reason; }
    public boolean isCanEquipNow() { return canEquipNow; }

    public String getFormattedUpgradeCost() {
        if (upgradeCost >= 1_000_000) {
            return String.format("%.1fM GP", upgradeCost / 1_000_000.0);
        } else if (upgradeCost >= 1_000) {
            return String.format("%dK GP", upgradeCost / 1_000);
        }
        return upgradeCost + " GP";
    }
}
