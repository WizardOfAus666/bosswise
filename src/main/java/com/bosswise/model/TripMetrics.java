package com.bosswise.model;

public class TripMetrics {
    private final int estimatedKills;
    private final int tripDurationMinutes;
    private final long supplyCost;
    private final long costPerKill;
    private final int foodHealing;
    private final int prayerRestored;
    private final int ammoConsumed;
    private final int slotsUsed;
    private final boolean valid;

    public TripMetrics(
            int estimatedKills,
            int tripDurationMinutes,
            long supplyCost,
            long costPerKill,
            int foodHealing,
            int prayerRestored,
            int ammoConsumed,
            int slotsUsed,
            boolean valid) {
        this.estimatedKills = estimatedKills;
        this.tripDurationMinutes = tripDurationMinutes;
        this.supplyCost = supplyCost;
        this.costPerKill = costPerKill;
        this.foodHealing = foodHealing;
        this.prayerRestored = prayerRestored;
        this.ammoConsumed = ammoConsumed;
        this.slotsUsed = slotsUsed;
        this.valid = valid;
    }

    public int getEstimatedKills() { return estimatedKills; }
    public int getTripDurationMinutes() { return tripDurationMinutes; }
    public long getSupplyCost() { return supplyCost; }
    public long getCostPerKill() { return costPerKill; }
    public int getFoodHealing() { return foodHealing; }
    public int getPrayerRestored() { return prayerRestored; }
    public int getAmmoConsumed() { return ammoConsumed; }
    public int getSlotsUsed() { return slotsUsed; }
    public boolean isValid() { return valid; }

    public String getFormattedSupplyCost() {
        if (supplyCost >= 1_000_000) {
            return String.format("%.1fM GP", supplyCost / 1_000_000.0);
        } else if (supplyCost >= 1_000) {
            return String.format("%dK GP", supplyCost / 1_000);
        }
        return supplyCost + " GP";
    }

    public String getFormattedCostPerKill() {
        if (costPerKill >= 1_000_000) {
            return String.format("%.1fM GP", costPerKill / 1_000_000.0);
        } else if (costPerKill >= 1_000) {
            return String.format("%dK GP", costPerKill / 1_000);
        }
        return costPerKill + " GP";
    }
}
