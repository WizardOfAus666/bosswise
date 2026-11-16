package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BossEligibility {
    private final BossEligibilityStatus status;
    private final int recommendationScore; // 0 to 100
    private final List<String> missingRequirements;
    private final List<String> missingQuests;
    private final List<String> missingSkills;
    private final String suitabilityReason;
    private final int recommendedCombatLevel;
    private final int playerCombatLevel;

    public BossEligibility(
            BossEligibilityStatus status,
            int recommendationScore,
            List<String> missingRequirements,
            List<String> missingQuests,
            List<String> missingSkills,
            String suitabilityReason,
            int recommendedCombatLevel,
            int playerCombatLevel) {
        this.status = status;
        this.recommendationScore = recommendationScore;
        this.missingRequirements = Collections.unmodifiableList(new ArrayList<>(missingRequirements));
        this.missingQuests = Collections.unmodifiableList(new ArrayList<>(missingQuests));
        this.missingSkills = Collections.unmodifiableList(new ArrayList<>(missingSkills));
        this.suitabilityReason = suitabilityReason;
        this.recommendedCombatLevel = recommendedCombatLevel;
        this.playerCombatLevel = playerCombatLevel;
    }

    public BossEligibilityStatus getStatus() { return status; }
    public int getRecommendationScore() { return recommendationScore; }
    public List<String> getMissingRequirements() { return missingRequirements; }
    public List<String> getMissingQuests() { return missingQuests; }
    public List<String> getMissingSkills() { return missingSkills; }
    public String getSuitabilityReason() { return suitabilityReason; }
    public int getRecommendedCombatLevel() { return recommendedCombatLevel; }
    public int getPlayerCombatLevel() { return playerCombatLevel; }

    public boolean isReady() {
        return status == BossEligibilityStatus.READY;
    }

    public boolean isAlmostReady() {
        return status == BossEligibilityStatus.ALMOST_READY;
    }

    public boolean isLocked() {
        return status == BossEligibilityStatus.LOCKED;
    }
}
