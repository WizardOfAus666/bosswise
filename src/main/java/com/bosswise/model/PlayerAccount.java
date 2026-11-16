package com.bosswise.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;

public class PlayerAccount {
    private final String username;
    private final int combatLevel;
    private final int totalLevel;
    private final Map<Skill, Integer> realLevels;
    private final Map<Skill, Integer> boostedLevels;
    private final Map<Quest, QuestState> questStates;
    private final Set<String> manualCompletedQuests;
    private final boolean loggedIn;

    public PlayerAccount(
            String username,
            int combatLevel,
            int totalLevel,
            Map<Skill, Integer> realLevels,
            Map<Skill, Integer> boostedLevels,
            Map<Quest, QuestState> questStates,
            Set<String> manualCompletedQuests,
            boolean loggedIn) {
        this.username = username != null ? username : "Player";
        this.combatLevel = combatLevel;
        this.totalLevel = totalLevel;
        this.realLevels = Collections.unmodifiableMap(new EnumMap<>(realLevels));
        this.boostedLevels = Collections.unmodifiableMap(new EnumMap<>(boostedLevels));
        this.questStates = Collections.unmodifiableMap(new HashMap<>(questStates));
        this.manualCompletedQuests = Collections.unmodifiableSet(new HashSet<>(manualCompletedQuests));
        this.loggedIn = loggedIn;
    }

    public static PlayerAccount createDefault() {
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            reals.put(s, 75);
            boosteds.put(s, 75);
        }
        reals.put(Skill.ATTACK, 80);
        reals.put(Skill.STRENGTH, 85);
        reals.put(Skill.DEFENCE, 75);
        reals.put(Skill.RANGED, 82);
        reals.put(Skill.MAGIC, 80);
        reals.put(Skill.PRAYER, 70);
        reals.put(Skill.HITPOINTS, 83);
        reals.put(Skill.SLAYER, 72);

        boosteds.putAll(reals);

        Map<Quest, QuestState> qStates = new HashMap<>();
        Set<String> completed = new HashSet<>();
        completed.add("Dragon Slayer I");
        completed.add("Monkey Madness I");
        completed.add("Priest in Peril");
        completed.add("Horror from the Deep");
        completed.add("The Fremennik Trials");
        completed.add("The Fremennik Isles");

        return new PlayerAccount("Waiting for Login...", 102, 1680, reals, boosteds, qStates, completed, false);
    }

    public String getUsername() { return username; }
    public int getCombatLevel() { return combatLevel; }
    public int getTotalLevel() { return totalLevel; }
    public boolean isLoggedIn() { return loggedIn; }
    public Map<Skill, Integer> getRealLevels() { return realLevels; }
    public Map<Skill, Integer> getBoostedLevels() { return boostedLevels; }

    public int getRealLevel(Skill skill) {
        return realLevels.getOrDefault(skill, 1);
    }

    public int getSkillLevel(Skill skill) {
        return getRealLevel(skill);
    }

    public boolean hasCompletedQuest(String questName) {
        return isQuestCompleted(null, questName);
    }

    public int getBoostedLevel(Skill skill) {
        return boostedLevels.getOrDefault(skill, 1);
    }

    public QuestState getQuestState(Quest quest) {
        if (quest == null) return QuestState.NOT_STARTED;
        return questStates.getOrDefault(quest, QuestState.NOT_STARTED);
    }

    public boolean isQuestCompleted(Quest quest, String questName) {
        if (quest != null) {
            QuestState state = questStates.get(quest);
            if (state == QuestState.FINISHED) return true;
        }
        if (questName != null && manualCompletedQuests.contains(questName)) {
            return true;
        }
        return false;
    }

    public boolean meetsSkill(SkillRequirement req) {
        if (req == null) return true;
        // Hard requirements must check real levels, or consider boost if boostable
        int level = req.isBoostable() ? getBoostedLevel(req.getSkill()) : getRealLevel(req.getSkill());
        return level >= req.getLevel();
    }

    public static Builder builder(String username) {
        return new Builder(username);
    }

    public static class Builder {
        private final String username;
        private int combatLevel = 100;
        private int totalLevel = 1500;
        private final Map<Skill, Integer> realLevels = new EnumMap<>(Skill.class);
        private final Map<Skill, Integer> boostedLevels = new EnumMap<>(Skill.class);
        private final Map<Quest, QuestState> questStates = new HashMap<>();
        private final Set<String> manualCompletedQuests = new HashSet<>();
        private boolean loggedIn = true;

        public Builder(String username) {
            this.username = username;
            for (Skill s : Skill.values()) {
                realLevels.put(s, 70);
                boostedLevels.put(s, 70);
            }
        }

        public Builder level(Skill skill, int level) {
            realLevels.put(skill, level);
            boostedLevels.put(skill, level);
            return this;
        }

        public Builder combatLevel(int combatLevel) {
            this.combatLevel = combatLevel;
            return this;
        }

        public Builder quest(String questName) {
            manualCompletedQuests.add(questName);
            return this;
        }

        public PlayerAccount build() {
            return new PlayerAccount(username, combatLevel, totalLevel, realLevels, boostedLevels, questStates, manualCompletedQuests, loggedIn);
        }
    }
}
