package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventorySetup {
    private final String tierName;
    private final List<InventoryItem> items;

    public InventorySetup(String tierName, List<InventoryItem> items) {
        this.tierName = tierName;
        this.items = new ArrayList<>(items);
    }

    public String getTierName() { return tierName; }
    public List<InventoryItem> getItems() { return items; }

    public long getTotalCost() {
        long sum = 0;
        for (InventoryItem item : items) {
            sum += item.getTotalCost();
        }
        return sum;
    }

    public int getUsedSlots() {
        int slots = 0;
        for (InventoryItem item : items) {
            if (item.getQuantity() <= 0) continue;
            String cat = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            String name = item.getName() != null ? item.getName().toLowerCase() : "";

            // Stackable categories: Rune, Ammo, Teleport tab, Rune pouch
            boolean isStackable = cat.contains("rune") || cat.contains("ammo") || name.contains("tab")
                    || name.contains("pouch") || name.contains("scroll") || name.contains("coins");

            if (isStackable) {
                slots += 1;
            } else {
                slots += item.getQuantity();
            }
        }
        return slots;
    }

    public boolean isValid() {
        return getUsedSlots() <= 28;
    }

    public int getRemainingSlots() {
        return Math.max(0, 28 - getUsedSlots());
    }

    public int getTotalHealAmount() {
        int total = 0;
        for (InventoryItem item : items) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            int qty = item.getQuantity();
            if (name.contains("anglerfish")) total += qty * 22;
            else if (name.contains("manta ray")) total += qty * 22;
            else if (name.contains("shark")) total += qty * 20;
            else if (name.contains("karambwan")) total += qty * 18;
            else if (name.contains("monkfish")) total += qty * 16;
            else if (name.contains("lobster")) total += qty * 12;
            else if (name.contains("saradomin brew")) total += qty * (16 * 4); // 4 doses
            else if (item.getCategory() != null && item.getCategory().equalsIgnoreCase("Food")) {
                total += qty * 16;
            }
        }
        return total;
    }

    public int getTotalPrayerRestore() {
        int total = 0;
        for (InventoryItem item : items) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            int qty = item.getQuantity();
            if (name.contains("prayer potion") || name.contains("super restore")) {
                total += qty * (31 * 4); // 4 doses @ 31 points avg
            } else if (name.contains("sanfew")) {
                total += qty * (31 * 4);
            }
        }
        return total;
    }

    public TripMetrics calculateTripMetrics(int bossHp, double playerDps, int bossMaxHit) {
        int slots = getUsedSlots();
        boolean valid = slots <= 28;
        long supplyCost = getTotalCost();

        double safeDps = Math.max(0.5, playerDps);
        int killTimeSeconds = (int) Math.ceil(bossHp / safeDps) + 15; // 15s transit/spawn delay
        if (killTimeSeconds <= 0) killTimeSeconds = 60;

        int totalHeal = getTotalHealAmount();
        int estimatedIncomingDamagePerKill = Math.min(totalHeal, Math.max(10, (int) (bossMaxHit * 1.5)));
        int killsByFood = (estimatedIncomingDamagePerKill > 0) ? Math.max(1, totalHeal / estimatedIncomingDamagePerKill) : 5;

        int totalPray = getTotalPrayerRestore();
        int prayerDrainPerKill = Math.max(5, (killTimeSeconds / 60) * 15); // ~15 prayer pts per minute
        int killsByPrayer = (prayerDrainPerKill > 0 && totalPray > 0) ? Math.max(1, totalPray / prayerDrainPerKill) : killsByFood;

        int estimatedKills = Math.min(killsByFood, killsByPrayer);
        if (estimatedKills > 30) estimatedKills = 30; // realistic cap per inventory trip
        if (estimatedKills <= 0) estimatedKills = 1;

        int totalTripSeconds = (estimatedKills * killTimeSeconds) + 120; // 2 min banking
        int tripDurationMinutes = (int) Math.ceil(totalTripSeconds / 60.0);

        long costPerKill = (estimatedKills > 0) ? (supplyCost / estimatedKills) : supplyCost;
        int ammoConsumed = (int) (killTimeSeconds * estimatedKills * 0.4); // approx shots

        return new TripMetrics(
                estimatedKills,
                tripDurationMinutes,
                supplyCost,
                costPerKill,
                totalHeal,
                totalPray,
                ammoConsumed,
                slots,
                valid
        );
    }
}
