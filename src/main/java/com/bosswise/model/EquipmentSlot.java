package com.bosswise.model;

public enum EquipmentSlot {
    HEAD("Head"),
    CAPE("Cape"),
    AMULET("Neck"),
    AMMO("Ammo"),
    WEAPON("Weapon"),
    BODY("Body"),
    SHIELD("Shield"),
    LEGS("Legs"),
    GLOVES("Gloves"),
    BOOTS("Boots"),
    RING("Ring"),
    SPEC_WEAPON("Spec Weapon");

    private final String displayName;

    EquipmentSlot(String displayName) {
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
