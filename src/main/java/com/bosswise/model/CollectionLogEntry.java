package com.bosswise.model;

public class CollectionLogEntry {
    private final String category; // Bosses, Raids, Slayer, Clues, Minigames
    private final String encounterName;
    private final String itemName;
    private final int itemId;
    private final String dropRate;
    private boolean obtained;

    public CollectionLogEntry(String category, String encounterName, String itemName, int itemId, String dropRate, boolean obtained) {
        this.category = category != null ? category : "Bosses";
        this.encounterName = encounterName;
        this.itemName = itemName;
        this.itemId = itemId;
        this.dropRate = dropRate;
        this.obtained = obtained;
    }

    public String getCategory() { return category; }
    public String getEncounterName() { return encounterName; }
    public String getItemName() { return itemName; }
    public int getItemId() { return itemId; }
    public String getDropRate() { return dropRate; }
    public boolean isObtained() { return obtained; }
    public void setObtained(boolean obtained) { this.obtained = obtained; }
}
