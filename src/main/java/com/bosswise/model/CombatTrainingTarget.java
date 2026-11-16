package com.bosswise.model;

import net.runelite.api.Skill;

public class CombatTrainingTarget {
    private final String monsterName;
    private final Skill trainedSkill;
    private final int minRecommendedLevel;
    private final int maxOptimalLevel;
    private final int estimatedXpPerHour;
    private final long costPerHour;
    private final int afkRating; // 1-10 (10 = highest AFK)
    private final String location;
    private final String recommendedGear;
    private final String methodDescription;
    private final String requirements;

    public CombatTrainingTarget(
            String monsterName,
            Skill trainedSkill,
            int minRecommendedLevel,
            int maxOptimalLevel,
            int estimatedXpPerHour,
            long costPerHour,
            int afkRating,
            String location,
            String recommendedGear,
            String methodDescription,
            String requirements) {
        this.monsterName = monsterName;
        this.trainedSkill = trainedSkill;
        this.minRecommendedLevel = minRecommendedLevel;
        this.maxOptimalLevel = maxOptimalLevel;
        this.estimatedXpPerHour = estimatedXpPerHour;
        this.costPerHour = costPerHour;
        this.afkRating = Math.max(1, Math.min(10, afkRating));
        this.location = location;
        this.recommendedGear = recommendedGear;
        this.methodDescription = methodDescription;
        this.requirements = requirements != null ? requirements : "None";
    }

    public String getMonsterName() { return monsterName; }
    public Skill getTrainedSkill() { return trainedSkill; }
    public int getMinRecommendedLevel() { return minRecommendedLevel; }
    public int getMaxOptimalLevel() { return maxOptimalLevel; }
    public int getEstimatedXpPerHour() { return estimatedXpPerHour; }
    public long getCostPerHour() { return costPerHour; }
    public int getAfkRating() { return afkRating; }
    public String getLocation() { return location; }
    public String getRecommendedGear() { return recommendedGear; }
    public String getMethodDescription() { return methodDescription; }
    public String getRequirements() { return requirements; }

    public double estimateHoursToTarget(int currentXp, int targetXp) {
        int diff = Math.max(0, targetXp - currentXp);
        if (estimatedXpPerHour <= 0 || diff <= 0) return 0.0;
        return (double) diff / (double) estimatedXpPerHour;
    }
}
