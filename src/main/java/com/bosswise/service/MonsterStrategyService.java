package com.bosswise.service;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.MonsterData;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.PersonalizedStrategyScore;
import com.bosswise.model.strategy.StrategyMethod;
import com.bosswise.model.strategy.StrategyTier;
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
public class MonsterStrategyService {
    private final MonsterStrategyDatabase strategyDatabase;
    private final GearOptimizerService gearOptimizerService;
    private final InventoryBuilderService inventoryBuilderService;
    private final MonsterDatabase monsterDatabase;
    private final BossDatabase bossDatabase;

    @Inject
    public MonsterStrategyService(
            MonsterStrategyDatabase strategyDatabase,
            GearOptimizerService gearOptimizerService,
            InventoryBuilderService inventoryBuilderService,
            MonsterDatabase monsterDatabase,
            BossDatabase bossDatabase) {
        this.strategyDatabase = strategyDatabase;
        this.gearOptimizerService = gearOptimizerService;
        this.inventoryBuilderService = inventoryBuilderService;
        this.monsterDatabase = monsterDatabase;
        this.bossDatabase = bossDatabase;
    }

    /**
     * Personalize all available strategy methods for a monster given the player's account and budget.
     */
    public List<PersonalizedStrategyScore> personalize(MonsterStrategy strategy, PlayerAccount account, long userBudget) {
        if (strategy == null || strategy.getMethods().isEmpty()) {
            return Collections.emptyList();
        }

        PlayerAccount acc = account != null ? account : PlayerAccount.createDefault();
        List<PersonalizedStrategyScore> scoredList = new ArrayList<>();

        for (StrategyMethod method : strategy.getMethods()) {
            List<String> missing = new ArrayList<>();
            boolean meetsAll = true;

            // Check skills
            for (Map.Entry<Skill, Integer> req : method.getRequiredSkills().entrySet()) {
                int playerLvl = acc.getSkillLevel(req.getKey());
                if (playerLvl < req.getValue()) {
                    meetsAll = false;
                    missing.add(req.getKey().getName() + " " + req.getValue() + " (Current: " + playerLvl + ")");
                }
            }

            // Check Slayer requirement if applicable
            if (strategy.getSlayerLevel() > 1) {
                int playerSlayer = acc.getSkillLevel(Skill.SLAYER);
                if (playerSlayer < strategy.getSlayerLevel()) {
                    meetsAll = false;
                    missing.add("Slayer " + strategy.getSlayerLevel() + " (Current: " + playerSlayer + ")");
                }
            }

            // Check quests
            for (String quest : method.getRequiredQuests()) {
                if (!acc.hasCompletedQuest(quest)) {
                    meetsAll = false;
                    missing.add("Quest: " + quest);
                }
            }

            // Calculate base viability score
            double score = 100.0;

            // Combat style affinity based on player levels
            if (method.getCombatStyle() == CombatStyle.RANGED) {
                int range = acc.getSkillLevel(Skill.RANGED);
                score += (range - 70) * 2.0;
                if (acc.getSkillLevel(Skill.PRAYER) >= 74) score += 15.0; // Rigour unlock
            } else if (method.getCombatStyle() == CombatStyle.MAGIC) {
                int mage = acc.getSkillLevel(Skill.MAGIC);
                score += (mage - 70) * 2.0;
                if (acc.getSkillLevel(Skill.PRAYER) >= 77) score += 15.0; // Augury
            } else {
                int atk = acc.getSkillLevel(Skill.ATTACK);
                int str = acc.getSkillLevel(Skill.STRENGTH);
                score += ((atk + str) / 2.0 - 70) * 2.0;
                if (acc.getSkillLevel(Skill.PRAYER) >= 70) score += 15.0; // Piety
            }

            // Budget adjustment
            if (userBudget > 0) {
                if (method.getEstimatedCostGp() <= userBudget) {
                    score += 25.0;
                } else {
                    double deficitRatio = (double) method.getEstimatedCostGp() / Math.max(1L, userBudget);
                    score -= Math.min(80.0, (deficitRatio - 1.0) * 30.0);
                }
            }

            // Tier weight
            score += method.getTier().getSortOrder() * 10.0;

            if (!meetsAll) {
                score -= 100.0; // Penalty for missing requirements
            }

            scoredList.add(new PersonalizedStrategyScore(
                    method, score, meetsAll, missing,
                    false, false, false, false, false, ""
            ));
        }

        // Sort by score descending
        scoredList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Assign special badges: Best Overall, Best Budget, Best DPS, Best Survival, Best Value
        PersonalizedStrategyScore bestOverall = null;
        PersonalizedStrategyScore bestBudget = null;
        PersonalizedStrategyScore bestDps = null;
        PersonalizedStrategyScore bestSurvival = null;
        PersonalizedStrategyScore bestValue = null;

        for (PersonalizedStrategyScore s : scoredList) {
            if (s.isMeetsAllRequirements() && bestOverall == null) {
                bestOverall = s;
                break;
            }
        }
        if (bestOverall == null && !scoredList.isEmpty()) {
            bestOverall = scoredList.get(0);
        }

        // Find Best Budget
        long lowestCost = Long.MAX_VALUE;
        for (PersonalizedStrategyScore s : scoredList) {
            if (s.getMethod().getEstimatedCostGp() < lowestCost) {
                lowestCost = s.getMethod().getEstimatedCostGp();
                bestBudget = s;
            }
        }

        // Find Best DPS (highest tier among viable)
        int highestTier = -1;
        for (PersonalizedStrategyScore s : scoredList) {
            if (s.isMeetsAllRequirements() && s.getMethod().getTier().getSortOrder() > highestTier) {
                highestTier = s.getMethod().getTier().getSortOrder();
                bestDps = s;
            }
        }
        if (bestDps == null && !scoredList.isEmpty()) {
            bestDps = scoredList.get(0);
        }

        // Find Best Survival (Ranged/Magic or High Defence)
        for (PersonalizedStrategyScore s : scoredList) {
            if (s.getMethod().getCombatStyle() == CombatStyle.RANGED || s.getMethod().getTitle().toLowerCase().contains("safespot") || s.getMethod().getTier() == StrategyTier.HIGH_LEVEL) {
                bestSurvival = s;
                break;
            }
        }
        if (bestSurvival == null && !scoredList.isEmpty()) {
            bestSurvival = scoredList.get(0);
        }

        // Best Value (Mid-Game or Best Value tier)
        for (PersonalizedStrategyScore s : scoredList) {
            if (s.getMethod().getTier() == StrategyTier.MID_GAME || s.getMethod().getTier() == StrategyTier.BEGINNER) {
                bestValue = s;
                break;
            }
        }
        if (bestValue == null && !scoredList.isEmpty()) {
            bestValue = scoredList.get(0);
        }

        // Re-construct with badges and explanations
        List<PersonalizedStrategyScore> finalized = new ArrayList<>();
        for (PersonalizedStrategyScore s : scoredList) {
            boolean isBo = s == bestOverall;
            boolean isBb = s == bestBudget;
            boolean isBd = s == bestDps;
            boolean isBs = s == bestSurvival;
            boolean isBv = s == bestValue;

            StringBuilder reason = new StringBuilder();
            if (isBo) {
                reason.append("Best overall match for your combat stats (");
                reason.append(s.getMethod().getCombatStyle().getDisplayName()).append("). ");
            }
            if (isBb) {
                reason.append("Most affordable setup with minimal financial barrier. ");
            }
            if (isBd) {
                reason.append("Maximal damage output potential. ");
            }
            if (isBs) {
                reason.append("Provides highest survivability and defensive safety. ");
            }
            if (isBv) {
                reason.append("Best performance-to-cost efficiency ratio. ");
            }
            if (!s.isMeetsAllRequirements()) {
                reason.append("Locked: Missing ").append(s.getMissingRequirements().size()).append(" requirements.");
            } else if (reason.length() == 0) {
                reason.append("Viable alternative method for ").append(strategy.getMonsterName()).append(".");
            }

            finalized.add(new PersonalizedStrategyScore(
                    s.getMethod(), s.getScore(), s.isMeetsAllRequirements(),
                    s.getMissingRequirements(), isBo, isBb, isBd, isBs, isBv,
                    reason.toString().trim()
            ));
        }

        return finalized;
    }

    /**
     * Interoperate with GearOptimizerService to generate authoritative calculated gear setup.
     */
    public OptimizedGearSetup getCalculatedGearSetup(StrategyMethod method, MonsterStrategy strategy, PlayerAccount account, long budget) {
        if (gearOptimizerService == null || strategy == null || method == null) return null;

        BossData boss = bossDatabase != null ? bossDatabase.getBoss(strategy.getMonsterId()) : null;
        MonsterData monster = monsterDatabase != null ? monsterDatabase.getMonster(strategy.getMonsterId()) : null;

        if (boss == null && monster != null) {
            boss = monster.toBossData();
        }

        String tierName = method.getTitle();
        CombatStyle style = method.getCombatStyle();
        long loadoutBudget = budget > 0 ? budget : method.getEstimatedCostGp();

        return gearOptimizerService.generateSetup(
                tierName, boss, monster, null, style,
                loadoutBudget > 0 ? loadoutBudget : 10_000_000L,
                account != null ? account : PlayerAccount.createDefault(),
                GearOptimizerService.PriorityMode.BALANCED,
                false
        );
    }

    /**
     * Interoperate with InventoryBuilderService to generate matched inventory recommendations.
     */
    public InventorySetup getCalculatedInventorySetup(StrategyMethod method, MonsterStrategy strategy, PlayerAccount account) {
        if (inventoryBuilderService == null || strategy == null || method == null) return null;

        BossData boss = bossDatabase != null ? bossDatabase.getBoss(strategy.getMonsterId()) : null;
        if (boss == null && monsterDatabase != null) {
            MonsterData m = monsterDatabase.getMonster(strategy.getMonsterId());
            if (m != null) boss = m.toBossData();
        }

        if (boss == null) return null;

        return inventoryBuilderService.getInventorySetup(
                boss,
                method.getTier().getDisplayName()
        );
    }

    public MonsterStrategyDatabase getStrategyDatabase() {
        return strategyDatabase;
    }
}
