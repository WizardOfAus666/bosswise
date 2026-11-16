package com.bosswise.model;

public class WatchlistItem {
    private final int itemId;
    private final String itemName;
    private long currentPrice;
    private long targetPrice;
    private long lastCheckedTime;

    public WatchlistItem(int itemId, String itemName, long currentPrice, long targetPrice) {
        this.itemId = itemId;
        this.itemName = itemName != null ? itemName : "Item #" + itemId;
        this.currentPrice = currentPrice;
        this.targetPrice = targetPrice;
        this.lastCheckedTime = System.currentTimeMillis();
    }

    public int getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public long getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(long currentPrice) {
        this.currentPrice = currentPrice;
        this.lastCheckedTime = System.currentTimeMillis();
    }
    public long getTargetPrice() { return targetPrice; }
    public void setTargetPrice(long targetPrice) { this.targetPrice = targetPrice; }
    public long getLastCheckedTime() { return lastCheckedTime; }

    public boolean isTargetReached() {
        return targetPrice > 0 && currentPrice > 0 && currentPrice <= targetPrice;
    }

    public String getFormattedCurrentPrice() {
        if (currentPrice >= 1_000_000) {
            return String.format("%.2fM GP", currentPrice / 1_000_000.0);
        } else if (currentPrice >= 1_000) {
            return String.format("%dK GP", currentPrice / 1_000);
        }
        return currentPrice + " GP";
    }

    public String getFormattedTargetPrice() {
        if (targetPrice <= 0) return "No Target";
        if (targetPrice >= 1_000_000) {
            return String.format("%.2fM GP", targetPrice / 1_000_000.0);
        } else if (targetPrice >= 1_000) {
            return String.format("%dK GP", targetPrice / 1_000);
        }
        return targetPrice + " GP";
    }
}
