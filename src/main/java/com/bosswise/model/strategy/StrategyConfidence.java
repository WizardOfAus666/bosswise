package com.bosswise.model.strategy;

public enum StrategyConfidence {
    VERIFIED("Verified (OSRS Wiki)", "#00FF88"),
    PARTIALLY_VERIFIED("Partially Verified", "#FFAA00"),
    UNVERIFIED("Unverified", "#FF8844"),
    UNAVAILABLE("Unavailable", "#FF4444");

    private final String label;
    private final String colorHex;

    StrategyConfidence(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() {
        return label;
    }

    public String getColorHex() {
        return colorHex;
    }
}
