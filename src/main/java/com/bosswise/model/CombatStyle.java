package com.bosswise.model;

public enum CombatStyle {
    MELEE("Melee"),
    RANGED("Ranged"),
    MAGIC("Magic"),
    HYBRID("Hybrid"),
    TRIBRID("Tribrid");

    private final String displayName;

    CombatStyle(String displayName) {
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
