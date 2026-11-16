package com.bosswise.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LootSession {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final String id;
    private final String monsterName;
    private final long startTime;
    private long endTime;
    private int kills;
    private final List<LootDropItem> drops = new ArrayList<>();
    private long estimatedSupplyCosts;

    public LootSession(String monsterName) {
        this.id = "session_" + System.currentTimeMillis();
        this.monsterName = monsterName != null ? monsterName : "Unknown Monster";
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime;
        this.kills = 0;
        this.estimatedSupplyCosts = 0L;
    }

    public synchronized void recordKill(long supplyCostPerKill) {
        this.kills++;
        this.endTime = System.currentTimeMillis();
        this.estimatedSupplyCosts += Math.max(0, supplyCostPerKill);
    }

    public synchronized void addDrop(LootDropItem item) {
        if (item != null) {
            this.drops.add(item);
            this.endTime = System.currentTimeMillis();
        }
    }

    public synchronized void addDrops(List<LootDropItem> items) {
        if (items != null) {
            this.drops.addAll(items);
            this.endTime = System.currentTimeMillis();
        }
    }

    public String getId() { return id; }
    public String getMonsterName() { return monsterName; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getKills() { return kills; }
    public List<LootDropItem> getDrops() { return Collections.unmodifiableList(drops); }
    public long getEstimatedSupplyCosts() { return estimatedSupplyCosts; }

    public long getDurationSeconds() {
        return Math.max(1, (endTime - startTime) / 1000);
    }

    public String getFormattedDuration() {
        long sec = getDurationSeconds();
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        if (h > 0) {
            return String.format("%dh %02dm %02ds", h, m, s);
        }
        return String.format("%02dm %02ds", m, s);
    }

    public String getFormattedStartTime() {
        return TIME_FMT.format(Instant.ofEpochMilli(startTime));
    }

    public long getTotalLootValue() {
        long sum = 0L;
        for (LootDropItem item : drops) {
            sum += item.getTotalValue();
        }
        return sum;
    }

    public long getNetProfit() {
        return getTotalLootValue() - estimatedSupplyCosts;
    }

    public long getGpPerKill() {
        if (kills <= 0) return 0L;
        return getTotalLootValue() / kills;
    }

    public long getProfitPerKill() {
        if (kills <= 0) return 0L;
        return getNetProfit() / kills;
    }

    public double getKillsPerHour() {
        double hours = getDurationSeconds() / 3600.0;
        if (hours <= 0) return 0.0;
        return kills / hours;
    }

    public long getProfitPerHour() {
        double hours = getDurationSeconds() / 3600.0;
        if (hours <= 0) return 0L;
        return (long) (getNetProfit() / hours);
    }

    public long getLootPerHour() {
        double hours = getDurationSeconds() / 3600.0;
        if (hours <= 0) return 0L;
        return (long) (getTotalLootValue() / hours);
    }

    public Map<LootDropItem.DropRarity, Integer> getRarityCounts() {
        Map<LootDropItem.DropRarity, Integer> counts = new HashMap<>();
        for (LootDropItem.DropRarity r : LootDropItem.DropRarity.values()) {
            counts.put(r, 0);
        }
        for (LootDropItem item : drops) {
            counts.put(item.getRarity(), counts.get(item.getRarity()) + 1);
        }
        return counts;
    }
}
