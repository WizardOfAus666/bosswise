package com.bosswise.model;

public enum MonsterCategory {
    ALL("All Categories"),
    REGULAR("Regular Monsters"),
    SLAYER("Slayer Monsters"),
    SUPERIOR("Superior Slayer"),
    WILDERNESS("Wilderness"),
    BOSS("Bosses"),
    DEMI_BOSS("Demi-Bosses"),
    RAID("Raid Encounters"),
    DUNGEON_MINIGAME("Dungeons & Minigames"),
    QUEST("Quest Encounters");

    private final String displayName;

    MonsterCategory(String displayName) {
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
