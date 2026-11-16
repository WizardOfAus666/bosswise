package com.bosswise.model;

public class GearUpgradeRecommendation {
    private final EquipmentSlot slot;
    private final EquipmentItem currentItem;
    private final EquipmentItem upgradeItem;
    private final long priceDifference;
    private final String statComparison;
    private final String whyItMatters;
    private final boolean fitsBudget;

    public GearUpgradeRecommendation(
            EquipmentSlot slot,
            EquipmentItem currentItem,
            EquipmentItem upgradeItem,
            long priceDifference,
            String statComparison,
            String whyItMatters,
            boolean fitsBudget) {
        this.slot = slot;
        this.currentItem = currentItem;
        this.upgradeItem = upgradeItem;
        this.priceDifference = priceDifference;
        this.statComparison = statComparison;
        this.whyItMatters = whyItMatters;
        this.fitsBudget = fitsBudget;
    }

    public EquipmentSlot getSlot() { return slot; }
    public EquipmentItem getCurrentItem() { return currentItem; }
    public EquipmentItem getUpgradeItem() { return upgradeItem; }
    public long getPriceDifference() { return priceDifference; }
    public String getStatComparison() { return statComparison; }
    public String getWhyItMatters() { return whyItMatters; }
    public boolean isFitsBudget() { return fitsBudget; }
}
