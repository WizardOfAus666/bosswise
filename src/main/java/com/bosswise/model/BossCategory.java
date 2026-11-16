package com.bosswise.model;

public enum BossCategory {
    ALL("All Types"),
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    ENDGAME("Endgame"),
    SLAYER("Slayer"),
    WILDERNESS("Wilderness"),
    GROUP("Group"),
    RAID("Raid"),
    DEMI_BOSS("Demi-Boss");

    private final String displayName;

    BossCategory(String displayName) {
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
