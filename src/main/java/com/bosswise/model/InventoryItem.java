package com.bosswise.model;

public class InventoryItem {
    private final int itemId;
    private final String name;
    private int quantity;
    private final String category; // Food, Combat Potion, Prayer Restore, Utility, Rune, Ammo, Teleport, Protection, Special Attack
    private final int unitPrice;
    private final boolean mandatory;
    private final String note;

    public InventoryItem(int itemId, String name, int quantity, String category, int unitPrice, boolean mandatory, String note) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.unitPrice = unitPrice;
        this.mandatory = mandatory;
        this.note = note != null ? note : "";
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }
    public String getCategory() { return category; }
    public int getUnitPrice() { return unitPrice; }
    public boolean isMandatory() { return mandatory; }
    public String getNote() { return note; }

    public long getTotalCost() {
        return (long) quantity * unitPrice;
    }
}
