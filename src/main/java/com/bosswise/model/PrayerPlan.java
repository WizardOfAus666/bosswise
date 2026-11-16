package com.bosswise.model;

import java.util.Collections;
import java.util.List;

public class PrayerPlan {
    private final String planName;
    private final String offensivePrayer;
    private final String defensivePrayer;
    private final double offensiveDamageMultiplier;
    private final double offensiveAccuracyMultiplier;
    private final double drainPerMinute;
    private final double prayerPotionsPerHour;
    private final long prayerCostPerHour;
    private final String recommendedPotion;
    private final long potionCostPerHour;
    private final List<String> notes;

    public PrayerPlan(
            String planName,
            String offensivePrayer,
            String defensivePrayer,
            double offensiveDamageMultiplier,
            double offensiveAccuracyMultiplier,
            double drainPerMinute,
            double prayerPotionsPerHour,
            long prayerCostPerHour,
            String recommendedPotion,
            long potionCostPerHour,
            List<String> notes) {
        this.planName = planName;
        this.offensivePrayer = offensivePrayer;
        this.defensivePrayer = defensivePrayer;
        this.offensiveDamageMultiplier = offensiveDamageMultiplier;
        this.offensiveAccuracyMultiplier = offensiveAccuracyMultiplier;
        this.drainPerMinute = drainPerMinute;
        this.prayerPotionsPerHour = prayerPotionsPerHour;
        this.prayerCostPerHour = prayerCostPerHour;
        this.recommendedPotion = recommendedPotion;
        this.potionCostPerHour = potionCostPerHour;
        this.notes = notes != null ? notes : Collections.emptyList();
    }

    public String getPlanName() { return planName; }
    public String getOffensivePrayer() { return offensivePrayer; }
    public String getDefensivePrayer() { return defensivePrayer; }
    public double getOffensiveDamageMultiplier() { return offensiveDamageMultiplier; }
    public double getOffensiveAccuracyMultiplier() { return offensiveAccuracyMultiplier; }
    public double getDrainPerMinute() { return drainPerMinute; }
    public double getPrayerPotionsPerHour() { return prayerPotionsPerHour; }
    public long getPrayerCostPerHour() { return prayerCostPerHour; }
    public String getRecommendedPotion() { return recommendedPotion; }
    public long getPotionCostPerHour() { return potionCostPerHour; }
    public List<String> getNotes() { return notes; }

    public long getTotalSuppliesCostPerHour() {
        return prayerCostPerHour + potionCostPerHour;
    }
}
