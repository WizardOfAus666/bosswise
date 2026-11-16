package com.bosswise.model.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalizedStrategyScore {
    private final StrategyMethod method;
    private final double score;
    private final boolean meetsAllRequirements;
    private final List<String> missingRequirements;
    private final boolean isBestOverall;
    private final boolean isBestBudget;
    private final boolean isBestDps;
    private final boolean isBestSurvival;
    private final boolean isBestValue;
    private final String recommendationReason;

    public PersonalizedStrategyScore(
            StrategyMethod method,
            double score,
            boolean meetsAllRequirements,
            List<String> missingRequirements,
            boolean isBestOverall,
            boolean isBestBudget,
            boolean isBestDps,
            boolean isBestSurvival,
            boolean isBestValue,
            String recommendationReason) {
        this.method = method;
        this.score = score;
        this.meetsAllRequirements = meetsAllRequirements;
        this.missingRequirements = missingRequirements != null ? Collections.unmodifiableList(new ArrayList<>(missingRequirements)) : Collections.emptyList();
        this.isBestOverall = isBestOverall;
        this.isBestBudget = isBestBudget;
        this.isBestDps = isBestDps;
        this.isBestSurvival = isBestSurvival;
        this.isBestValue = isBestValue;
        this.recommendationReason = recommendationReason != null ? recommendationReason : "";
    }

    public StrategyMethod getMethod() { return method; }
    public double getScore() { return score; }
    public boolean isMeetsAllRequirements() { return meetsAllRequirements; }
    public List<String> getMissingRequirements() { return missingRequirements; }
    public boolean isBestOverall() { return isBestOverall; }
    public boolean isBestBudget() { return isBestBudget; }
    public boolean isBestDps() { return isBestDps; }
    public boolean isBestSurvival() { return isBestSurvival; }
    public boolean isBestValue() { return isBestValue; }
    public String getRecommendationReason() { return recommendationReason; }
}
