package com.bosswise.service;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.BossEligibility;
import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.ProgressionGoal;
import com.bosswise.model.QuestRequirement;
import com.bosswise.model.SkillRequirement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class ProgressionService {
    private final BossDatabase bossDatabase;
    private final BossRecommendationEngine recommendationEngine;

    @Inject
    public ProgressionService(BossDatabase bossDatabase, BossRecommendationEngine recommendationEngine) {
        this.bossDatabase = bossDatabase;
        this.recommendationEngine = recommendationEngine;
    }

    public List<ProgressionGoal> generateRoadmap(PlayerAccount account) {
        if (account == null) {
            account = PlayerAccount.createDefault();
        }

        List<ProgressionGoal> goals = new ArrayList<>();
        Set<String> seenGoals = new HashSet<>();

        // 1. Scan for missing quest unlocks on ALMOST_READY or high-value bosses
        for (BossData boss : bossDatabase.getAllBosses().values()) {
            for (QuestRequirement qReq : boss.getQuestRequirements()) {
                if (qReq.isMandatory() && !account.isQuestCompleted(qReq.getQuest(), qReq.getQuestName())) {
                    String key = "QUEST_" + qReq.getQuestName();
                    if (!seenGoals.contains(key)) {
                        seenGoals.add(key);
                        goals.add(new ProgressionGoal(
                                key,
                                ProgressionGoal.GoalType.QUEST,
                                "Complete " + qReq.getQuestName(),
                                "Unlocks access to " + boss.getName() + " and vital endgame gear.",
                                0, 1, false,
                                1, boss.getName()
                        ));
                    }
                }
            }
        }

        // 2. Scan for missing skill requirements
        for (BossData boss : bossDatabase.getAllBosses().values()) {
            BossEligibility el = recommendationEngine.evaluateEligibility(boss, account);
            if (el.getStatus() == BossEligibilityStatus.ALMOST_READY) {
                for (SkillRequirement req : boss.getMinimumSkillRequirements()) {
                    int cur = account.getRealLevel(req.getSkill());
                    if (cur < req.getLevel()) {
                        String key = "SKILL_" + req.getSkill().getName() + "_" + req.getLevel();
                        if (!seenGoals.contains(key)) {
                            seenGoals.add(key);
                            goals.add(new ProgressionGoal(
                                    key,
                                    ProgressionGoal.GoalType.SKILL,
                                    "Train " + req.getSkill().getName() + " (" + cur + " -> " + req.getLevel() + ")",
                                    "Required entry level for " + boss.getName() + ".",
                                    cur, req.getLevel(), false,
                                    2, boss.getName()
                            ));
                        }
                    }
                }
            }
        }

        // 3. High-impact milestone unlocks
        int prayer = account.getRealLevel(Skill.PRAYER);
        if (prayer < 70) {
            goals.add(new ProgressionGoal(
                    "PRAYER_PIETY",
                    ProgressionGoal.GoalType.SKILL,
                    "Train Prayer to 70 for Piety",
                    "Piety grants +25% Defence, +23% Strength, and +20% Attack bonus.",
                    prayer, 70, false,
                    2, "All Melee Bosses"
            ));
        }

        int slayer = account.getRealLevel(Skill.SLAYER);
        if (slayer < 85) {
            goals.add(new ProgressionGoal(
                    "SLAYER_85",
                    ProgressionGoal.GoalType.SKILL,
                    "Train Slayer to 85 for Abyssal Sire",
                    "Unlocks Abyssal Demons, Abyssal Whip, and Abyssal Sire.",
                    slayer, 85, false,
                    3, "Abyssal Sire"
            ));
        } else if (slayer < 87) {
            goals.add(new ProgressionGoal(
                    "SLAYER_87",
                    ProgressionGoal.GoalType.SKILL,
                    "Train Slayer to 87 for Kraken",
                    "Unlocks Cave Krakens and Trident of the Seas.",
                    slayer, 87, false,
                    3, "Kraken"
            ));
        } else if (slayer < 95) {
            goals.add(new ProgressionGoal(
                    "SLAYER_95",
                    ProgressionGoal.GoalType.SKILL,
                    "Train Slayer to 95 for Alchemical Hydra",
                    "Unlocks Alchemical Hydra and Hydra's Claw (Ferocious Gloves).",
                    slayer, 95, false,
                    3, "Alchemical Hydra"
            ));
        }

        int ranged = account.getRealLevel(Skill.RANGED);
        if (ranged < 75) {
            goals.add(new ProgressionGoal(
                    "RANGED_75",
                    ProgressionGoal.GoalType.SKILL,
                    "Train Ranged to 75 for Toxic Blowpipe",
                    "Enables Toxic Blowpipe, Dragon Hunter Crossbow, and higher boss DPS.",
                    ranged, 75, false,
                    2, "Vorkath, Zulrah, ToA"
            ));
        }

        // Sort by priority (lowest number = highest priority)
        goals.sort(Comparator.comparingInt(ProgressionGoal::getPriority));

        return Collections.unmodifiableList(goals);
    }
}
