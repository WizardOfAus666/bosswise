package com.bosswise.model;

import net.runelite.api.Quest;

public class QuestRequirement {
    private final Quest quest;
    private final String questName;
    private final boolean mandatory;
    private final String notes;

    public QuestRequirement(Quest quest, String questName) {
        this(quest, questName, true, null);
    }

    public QuestRequirement(Quest quest, String questName, boolean mandatory, String notes) {
        this.quest = quest;
        this.questName = questName != null ? questName : (quest != null ? quest.getName() : "Unknown Quest");
        this.mandatory = mandatory;
        this.notes = notes;
    }

    public Quest getQuest() {
        return quest;
    }

    public String getQuestName() {
        return questName;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        if (notes != null && !notes.isEmpty()) {
            return questName + " (" + notes + ")";
        }
        return questName;
    }
}
