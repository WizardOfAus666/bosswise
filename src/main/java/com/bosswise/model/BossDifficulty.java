package com.bosswise.model;

import java.awt.Color;

public enum BossDifficulty {
    BEGINNER("Beginner", new Color(0x43, 0xA0, 0x47)),      // Green
    INTERMEDIATE("Intermediate", new Color(0xFB, 0x8C, 0x00)), // Amber/Orange
    ADVANCED("Advanced", new Color(0xE5, 0x39, 0x35)),        // Red
    MASTER("Master", new Color(0x8E, 0x24, 0xAA)),          // Purple
    GRANDMASTER("Grandmaster", new Color(0xD8, 0x1B, 0x60));  // Magenta

    private final String displayName;
    private final Color badgeColor;

    BossDifficulty(String displayName, Color badgeColor) {
        this.displayName = displayName;
        this.badgeColor = badgeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
