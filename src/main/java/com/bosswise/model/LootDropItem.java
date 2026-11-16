package com.bosswise.model;

public class LootDropItem {
    public enum DropRarity {
        UNIQUE("Unique", "#FF9800"),
        RARE("Rare", "#9C27B0"),
        VALUABLE("Valuable", "#4CAF50"),
        COMMON("Common", "#B0BEC5");

        private final String label;
        private final String hexColor;

        DropRarity(String label, String hexColor) {
            this.label = label;
            this.hexColor = hexColor;
        }

        public String getLabel() { return label; }
        public String getHexColor() { return hexColor; }
    }

    private final int itemId;
    private final String name;
    private final int quantity;
    private final long unitPrice;
    private final long totalValue;
    private final DropRarity rarity;

    public LootDropItem(int itemId, String name, int quantity, long unitPrice, DropRarity rarity) {
        this.itemId = itemId;
        this.name = name != null ? name : "Item #" + itemId;
        this.quantity = Math.max(1, quantity);
        this.unitPrice = Math.max(0, unitPrice);
        this.totalValue = (long) this.quantity * this.unitPrice;
        this.rarity = rarity != null ? rarity : classify(this.name, this.totalValue);
    }

    public static DropRarity classify(String name, long totalValue) {
        String n = name != null ? name.toLowerCase() : "";
        if (n.contains("pet") || n.contains("jar of") || n.contains("visage") || n.contains("tassets")
                || n.contains("chestplate") || n.contains("hilts") || n.contains("hilt")
                || n.contains("fang") || n.contains("shadow") || n.contains("bowfa")
                || n.contains("enhanced") || n.contains("vestige") || n.contains("scythe")
                || n.contains("twisted bow") || n.contains("head") && !n.contains("arrow")) {
            return DropRarity.UNIQUE;
        } else if (totalValue >= 500_000) {
            return DropRarity.RARE;
        } else if (totalValue >= 40_000) {
            return DropRarity.VALUABLE;
        }
        return DropRarity.COMMON;
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public long getUnitPrice() { return unitPrice; }
    public long getTotalValue() { return totalValue; }
    public DropRarity getRarity() { return rarity; }

    public String getFormattedTotalValue() {
        if (totalValue >= 1_000_000) {
            return String.format("%.2fM GP", totalValue / 1_000_000.0);
        } else if (totalValue >= 1_000) {
            return String.format("%dK GP", totalValue / 1_000);
        }
        return totalValue + " GP";
    }
}
