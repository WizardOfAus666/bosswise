package com.bosswise.service;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.BossDifficulty;
import com.bosswise.model.BossEligibility;
import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.QuestRequirement;
import com.bosswise.model.SkillRequirement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class BossRecommendationEngine {
    private final BossDatabase bossDatabase;

    public enum SortOrder {
        RECOMMENDED("Recommended"),
        EASIEST_FIRST("Easiest First"),
        HIGHEST_GP("Highest GP/hr"),
        LOWEST_GEAR_COST("Lowest Gear Cost"),
        BEST_SUITABILITY("Best Match %"),
        CLOSEST_TO_UNLOCKING("Closest to Unlock");

        private final String label;
        SortOrder(String label) { this.label = label; }
        public String getLabel() { return label; }
        @Override public String toString() { return label; }
    }

    @Inject
    public BossRecommendationEngine(BossDatabase bossDatabase) {
        this.bossDatabase = bossDatabase;
    }

    public BossEligibility evaluateEligibility(BossData boss, PlayerAccount account) {
        if (boss == null) {
            return new BossEligibility(BossEligibilityStatus.LOCKED, 0,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "Unknown Boss", 0, 0);
        }

        if (account == null) {
            account = PlayerAccount.createDefault();
        }

        List<String> missingRequirements = new ArrayList<>();
        List<String> missingQuests = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        int totalMissingSkillLevels = 0;

        // Check Hard Skill Requirements
        for (SkillRequirement req : boss.getMinimumSkillRequirements()) {
            if (!account.meetsSkill(req)) {
                int actual = req.isBoostable() ? account.getBoostedLevel(req.getSkill()) : account.getRealLevel(req.getSkill());
                int diff = req.getLevel() - actual;
                totalMissingSkillLevels += diff;
                String desc = req.getSkill().getName() + " " + req.getLevel() + " (Current: " + actual + ")";
                missingSkills.add(desc);
                missingRequirements.add(desc);
            }
        }

        // Check Quest Requirements
        for (QuestRequirement qReq : boss.getQuestRequirements()) {
            if (qReq.isMandatory() && !account.isQuestCompleted(qReq.getQuest(), qReq.getQuestName())) {
                String qDesc = "Quest: " + qReq.getQuestName();
                missingQuests.add(qDesc);
                missingRequirements.add(qDesc);
            }
        }

        // Determine Status
        BossEligibilityStatus status;
        if (missingRequirements.isEmpty()) {
            status = BossEligibilityStatus.READY;
        } else if (missingQuests.size() <= 1 && totalMissingSkillLevels <= 5) {
            status = BossEligibilityStatus.ALMOST_READY;
        } else {
            status = BossEligibilityStatus.LOCKED;
        }

        // Calculate Intelligent Recommendation Score (0 to 100)
        int score = calculateScore(boss, account, status, missingRequirements);

        // Generate human-readable reason
        String reason = generateReason(boss, account, status, score);

        return new BossEligibility(
                status,
                score,
                missingRequirements,
                missingQuests,
                missingSkills,
                reason,
                boss.getRecommendedCombatLevel(),
                account.getCombatLevel()
        );
    }

    private int calculateScore(BossData boss, PlayerAccount account, BossEligibilityStatus status, List<String> missing) {
        int score = 0;

        // 1. Base readiness points
        if (status == BossEligibilityStatus.READY) {
            score += 40;
        } else if (status == BossEligibilityStatus.ALMOST_READY) {
            score += 15;
        } else {
            score += 0;
        }

        // 2. Recommended stats match (up to 30 points)
        if (!boss.getRecommendedSkills().isEmpty()) {
            double statSum = 0;
            int count = 0;
            for (Map.Entry<Skill, Integer> entry : boss.getRecommendedSkills().entrySet()) {
                int actual = account.getRealLevel(entry.getKey());
                int target = entry.getValue();
                double ratio = Math.min(1.0, (double) actual / target);
                statSum += ratio;
                count++;
            }
            double avgRatio = (count > 0) ? (statSum / count) : 1.0;
            score += (int) Math.round(avgRatio * 30.0);
        } else {
            score += 25;
        }

        // 3. Combat level factor (up to 15 points)
        double cbRatio = Math.min(1.2, (double) account.getCombatLevel() / Math.max(1, boss.getRecommendedCombatLevel()));
        score += (int) Math.round(Math.min(1.0, cbRatio) * 15.0);

        // 4. GP potential bonus (up to 10 points)
        long gpHour = boss.getEstimatedGpHour();
        if (gpHour >= 3_000_000L) {
            score += 10;
        } else if (gpHour >= 1_500_000L) {
            score += 7;
        } else {
            score += 4;
        }

        // 5. Penalty for missing requirements
        if (!missing.isEmpty()) {
            score = Math.max(5, score - (missing.size() * 10));
        }

        return Math.min(100, Math.max(0, score));
    }

    private String generateReason(BossData boss, PlayerAccount account, BossEligibilityStatus status, int score) {
        if (status == BossEligibilityStatus.READY) {
            if (account.getCombatLevel() >= boss.getRecommendedCombatLevel() + 10) {
                return "Your combat stats and gear access heavily exceed requirements. Highly profitable and safe encounter.";
            } else if (account.getCombatLevel() >= boss.getRecommendedCombatLevel()) {
                return "Meets all mandatory requirements with well-matched combat levels. Prime candidate for bossing.";
            } else {
                return "Meets all access requirements. Combat stats are slightly below recommended, but fully feasible.";
            }
        } else if (status == BossEligibilityStatus.ALMOST_READY) {
            return "Very close to unlocking. Training a few missing skill levels or finishing 1 prerequisite will grant immediate access.";
        } else {
            return "Missing mandatory quests or high slayer/skill levels. Focused progression required before fighting.";
        }
    }

    public List<BossData> getFilteredBosses(
            PlayerAccount account,
            BossEligibilityStatus filterStatus,
            BossCategory filterCategory,
            BossDifficulty filterDifficulty,
            Boolean soloOnly,
            SortOrder sortOrder) {

        List<BossData> results = new ArrayList<>();
        Map<String, BossEligibility> eligibilityMap = new HashMap<>();

        for (BossData boss : bossDatabase.getAllBosses().values()) {
            BossEligibility el = evaluateEligibility(boss, account);
            eligibilityMap.put(boss.getId(), el);

            if (filterStatus != null && el.getStatus() != filterStatus) {
                continue;
            }
            if (filterCategory != null && filterCategory != BossCategory.ALL && boss.getCategory() != filterCategory) {
                continue;
            }
            if (filterDifficulty != null && boss.getDifficulty() != filterDifficulty) {
                continue;
            }
            if (Boolean.TRUE.equals(soloOnly) && !boss.isSolo()) {
                continue;
            }
            results.add(boss);
        }

        // Sorting
        Comparator<BossData> comparator;
        switch (sortOrder != null ? sortOrder : SortOrder.RECOMMENDED) {
            case EASIEST_FIRST:
                comparator = Comparator.comparing(BossData::getDifficulty)
                        .thenComparingInt(BossData::getRecommendedCombatLevel);
                break;
            case HIGHEST_GP:
                comparator = Comparator.comparingLong(BossData::getEstimatedGpHour).reversed();
                break;
            case LOWEST_GEAR_COST:
                comparator = Comparator.comparingLong(BossData::getSuggestedGearBudget);
                break;
            case BEST_SUITABILITY:
                comparator = (b1, b2) -> Integer.compare(
                        eligibilityMap.get(b2.getId()).getRecommendationScore(),
                        eligibilityMap.get(b1.getId()).getRecommendationScore());
                break;
            case CLOSEST_TO_UNLOCKING:
                comparator = (b1, b2) -> {
                    BossEligibility e1 = eligibilityMap.get(b1.getId());
                    BossEligibility e2 = eligibilityMap.get(b2.getId());
                    if (e1.getStatus() != e2.getStatus()) {
                        return e1.getStatus().compareTo(e2.getStatus());
                    }
                    return Integer.compare(e1.getMissingRequirements().size(), e2.getMissingRequirements().size());
                };
                break;
            case RECOMMENDED:
            default:
                comparator = (b1, b2) -> {
                    BossEligibility e1 = eligibilityMap.get(b1.getId());
                    BossEligibility e2 = eligibilityMap.get(b2.getId());
                    if (e1.getStatus() != e2.getStatus()) {
                        return e1.getStatus().compareTo(e2.getStatus());
                    }
                    return Integer.compare(e2.getRecommendationScore(), e1.getRecommendationScore());
                };
                break;
        }

        results.sort(comparator);
        return results;
    }
}
