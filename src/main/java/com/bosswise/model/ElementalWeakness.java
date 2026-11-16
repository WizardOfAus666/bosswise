package com.bosswise.model;

public enum ElementalWeakness {
    NONE("None"),
    AIR("Air"),
    WATER("Water"),
    EARTH("Earth"),
    FIRE("Fire");

    private final String displayName;

    ElementalWeakness(String displayName) {
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
