package com.bosswise.model;

import java.util.Collections;
import java.util.Map;

public class BankGearCheckResult {
    private final Map<EquipmentSlot, EquipmentItem> ownedItems;
    private final Map<EquipmentSlot, EquipmentItem> missingItems;
    private final long missingGearCost;
    private final int ownedSlotsCount;
    private final int totalSlotsCount;
    private final boolean bankScanned;

    public BankGearCheckResult(
            Map<EquipmentSlot, EquipmentItem> ownedItems,
            Map<EquipmentSlot, EquipmentItem> missingItems,
            long missingGearCost,
            int ownedSlotsCount,
            int totalSlotsCount,
            boolean bankScanned) {
        this.ownedItems = ownedItems != null ? ownedItems : Collections.emptyMap();
        this.missingItems = missingItems != null ? missingItems : Collections.emptyMap();
        this.missingGearCost = missingGearCost;
        this.ownedSlotsCount = ownedSlotsCount;
        this.totalSlotsCount = totalSlotsCount;
        this.bankScanned = bankScanned;
    }

    public Map<EquipmentSlot, EquipmentItem> getOwnedItems() { return ownedItems; }
    public Map<EquipmentSlot, EquipmentItem> getMissingItems() { return missingItems; }
    public long getMissingGearCost() { return missingGearCost; }
    public int getOwnedSlotsCount() { return ownedSlotsCount; }
    public int getTotalSlotsCount() { return totalSlotsCount; }
    public boolean isBankScanned() { return bankScanned; }

    public double getOwnedPercentage() {
        if (totalSlotsCount == 0) return 0.0;
        return ((double) ownedSlotsCount / (double) totalSlotsCount) * 100.0;
    }

    public String getFormattedMissingCost() {
        if (missingGearCost >= 1_000_000) {
            return String.format("%.2fM GP", missingGearCost / 1_000_000.0);
        } else if (missingGearCost >= 1_000) {
            return String.format("%dK GP", missingGearCost / 1_000);
        }
        return missingGearCost + " GP";
    }
}
