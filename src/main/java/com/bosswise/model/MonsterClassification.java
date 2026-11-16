package com.bosswise.model;

public enum MonsterClassification {
    NONE("None"),
    DRAGON("Dragon"),
    DEMON("Demon"),
    UNDEAD("Undead"),
    KALPHITE("Kalphite"),
    RAT("Rat"),
    GIANT("Giant"),
    GARGOYLE("Gargoyle"),
    VAMPYRE("Vampyre"),
    FIEND("Fiend"),
    REPTILE("Reptile"),
    ICY("Icy Creature"),
    SHADOW("Shadow Creature");

    private final String displayName;

    MonsterClassification(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
