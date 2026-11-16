package com.bosswise.model;

import net.runelite.api.Skill;

public class SkillRequirement {
    private final Skill skill;
    private final int level;
    private final boolean boostable;
    private final String boostNote;

    public SkillRequirement(Skill skill, int level) {
        this(skill, level, false, null);
    }

    public SkillRequirement(Skill skill, int level, boolean boostable, String boostNote) {
        this.skill = skill;
        this.level = level;
        this.boostable = boostable;
        this.boostNote = boostNote;
    }

    public Skill getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }

    public boolean isBoostable() {
        return boostable;
    }

    public String getBoostNote() {
        return boostNote;
    }

    public String formatRequirement() {
        if (boostable && boostNote != null && !boostNote.isEmpty()) {
            return skill.getName() + " " + level + " (Boostable: " + boostNote + ")";
        }
        return skill.getName() + " " + level;
    }

    @Override
    public String toString() {
        return formatRequirement();
    }
}
