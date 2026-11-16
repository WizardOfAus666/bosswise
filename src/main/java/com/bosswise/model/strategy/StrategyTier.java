package com.bosswise.model.strategy;

public enum StrategyTier {
    BUDGET("Budget", 0),
    BEGINNER("Beginner", 1),
    MID_GAME("Mid-Game", 2),
    HIGH_LEVEL("High-Level", 3),
    ENDGAME("Endgame BiS", 4),
    SPECIALISED("Specialised", 5);

    private final String displayName;
    private final int sortOrder;

    StrategyTier(String displayName, int sortOrder) {
        this.displayName = displayName;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
