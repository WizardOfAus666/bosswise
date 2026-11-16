package com.bosswise.model;

import java.awt.Color;

public enum BossEligibilityStatus {
    READY("Ready", new Color(0x2E, 0x7D, 0x32), new Color(0x81, 0xC7, 0x84)),            // Green
    ALMOST_READY("Almost Ready", new Color(0xF5, 0x7F, 0x17), new Color(0xFF, 0xD5, 0x4F)), // Yellow / Amber
    LOCKED("Locked", new Color(0xC6, 0x28, 0x28), new Color(0xE5, 0x73, 0x73));           // Red

    private final String label;
    private final Color primaryColor;
    private final Color accentColor;

    BossEligibilityStatus(String label, Color primaryColor, Color accentColor) {
        this.label = label;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
    }

    public String getLabel() {
        return label;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }
}
