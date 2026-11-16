package com.bosswise.model.strategy;

public class StrategyCombatMechanic {
    private final String name;
    private final String description;
    private final String avoidanceTip;

    public StrategyCombatMechanic(String name, String description, String avoidanceTip) {
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.avoidanceTip = avoidanceTip != null ? avoidanceTip : "";
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAvoidanceTip() {
        return avoidanceTip;
    }
}
