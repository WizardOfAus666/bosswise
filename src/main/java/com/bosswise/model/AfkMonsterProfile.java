package com.bosswise.model;

public class AfkMonsterProfile {
    private final String name;
    private final String location;
    private final int afkScore; // 1-10 (10 = most AFK)
    private final int attentionIntervalMinutes; // e.g. 10 min for crabs, 20 min for NMZ
    private final CombatStyle recommendedStyle;
    private final boolean aggressive;
    private final boolean multiCombat;
    private final String survivabilityMethod; // e.g. "100% safe (low hits)", "Protect from Melee + Altar"
    private final int expectedCombatXpPerHour;
    private final long estimatedProfitPerHour;
    private final String reason;

    public AfkMonsterProfile(
            String name,
            String location,
            int afkScore,
            int attentionIntervalMinutes,
            CombatStyle recommendedStyle,
            boolean aggressive,
            boolean multiCombat,
            String survivabilityMethod,
            int expectedCombatXpPerHour,
            long estimatedProfitPerHour,
            String reason) {
        this.name = name;
        this.location = location;
        this.afkScore = afkScore;
        this.attentionIntervalMinutes = attentionIntervalMinutes;
        this.recommendedStyle = recommendedStyle != null ? recommendedStyle : CombatStyle.MELEE;
        this.aggressive = aggressive;
        this.multiCombat = multiCombat;
        this.survivabilityMethod = survivabilityMethod != null ? survivabilityMethod : "Standard food";
        this.expectedCombatXpPerHour = expectedCombatXpPerHour;
        this.estimatedProfitPerHour = estimatedProfitPerHour;
        this.reason = reason != null ? reason : "";
    }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getAfkScore() { return afkScore; }
    public int getAttentionIntervalMinutes() { return attentionIntervalMinutes; }
    public CombatStyle getRecommendedStyle() { return recommendedStyle; }
    public boolean isAggressive() { return aggressive; }
    public boolean isMultiCombat() { return multiCombat; }
    public String getSurvivabilityMethod() { return survivabilityMethod; }
    public int getExpectedCombatXpPerHour() { return expectedCombatXpPerHour; }
    public long getEstimatedProfitPerHour() { return estimatedProfitPerHour; }
    public String getReason() { return reason; }
}
