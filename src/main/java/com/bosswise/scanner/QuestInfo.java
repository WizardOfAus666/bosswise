package com.bosswise.scanner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

public class QuestInfo {
    private final Quest quest;
    private final String name;
    private final Map<Skill, Integer> skillRequirements;
    private final List<String> prerequisiteQuests;
    private final List<String> unlockedBosses;
    private final int questPoints;

    public QuestInfo(
            Quest quest,
            String name,
            Map<Skill, Integer> skillRequirements,
            List<String> prerequisiteQuests,
            List<String> unlockedBosses,
            int questPoints) {
        this.quest = quest;
        this.name = name;
        this.skillRequirements = Collections.unmodifiableMap(skillRequirements);
        this.prerequisiteQuests = Collections.unmodifiableList(prerequisiteQuests);
        this.unlockedBosses = Collections.unmodifiableList(unlockedBosses);
        this.questPoints = questPoints;
    }

    public Quest getQuest() { return quest; }
    public String getName() { return name; }
    public Map<Skill, Integer> getSkillRequirements() { return skillRequirements; }
    public List<String> getPrerequisiteQuests() { return prerequisiteQuests; }
    public List<String> getUnlockedBosses() { return unlockedBosses; }
    public int getQuestPoints() { return questPoints; }
}
