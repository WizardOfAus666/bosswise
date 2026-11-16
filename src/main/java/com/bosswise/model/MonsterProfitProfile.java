package com.bosswise.model;

public class MonsterProfitProfile {
    private final String monsterId;
    private final String name;
    private final MonsterCategory category;
    private final CombatStyle primaryStyle;
    private final int combatLevel;
    private final long gpPerKill;
    private final int killsPerHour;
    private final long conservativeGpPerHour;
    private final long optimisticGpPerHour;
    private final long supplyCostPerHour;
    private final long ammoCostPerHour;
    private final long minGearInvestment;
    private final boolean afkSuitable;
    private final int killTimeSeconds;
    private final int respawnSeconds;
    private final DeathRiskProfile deathRisk;
    private final String assumptions;

    public MonsterProfitProfile(
            String monsterId,
            String name,
            MonsterCategory category,
            CombatStyle primaryStyle,
            int combatLevel,
            long gpPerKill,
            int killsPerHour,
            long conservativeGpPerHour,
            long optimisticGpPerHour,
            long supplyCostPerHour,
            long ammoCostPerHour,
            long minGearInvestment,
            boolean afkSuitable,
            int killTimeSeconds,
            int respawnSeconds,
            DeathRiskProfile deathRisk,
            String assumptions) {
        this.monsterId = monsterId;
        this.name = name;
        this.category = category != null ? category : MonsterCategory.REGULAR;
        this.primaryStyle = primaryStyle != null ? primaryStyle : CombatStyle.MELEE;
        this.combatLevel = combatLevel;
        this.gpPerKill = gpPerKill;
        this.killsPerHour = killsPerHour;
        this.conservativeGpPerHour = conservativeGpPerHour;
        this.optimisticGpPerHour = optimisticGpPerHour;
        this.supplyCostPerHour = supplyCostPerHour;
        this.ammoCostPerHour = ammoCostPerHour;
        this.minGearInvestment = minGearInvestment;
        this.afkSuitable = afkSuitable;
        this.killTimeSeconds = killTimeSeconds;
        this.respawnSeconds = respawnSeconds;
        this.deathRisk = deathRisk != null ? deathRisk : new DeathRiskProfile("Standard", 50000, 0, 20000, false, "");
        this.assumptions = assumptions != null ? assumptions : "";
    }

    public String getMonsterId() { return monsterId; }
    public String getName() { return name; }
    public String getMonsterName() { return name; }
    public MonsterCategory getCategory() { return category; }
    public CombatStyle getPrimaryStyle() { return primaryStyle; }
    public int getCombatLevel() { return combatLevel; }
    public long getGpPerKill() { return gpPerKill; }
    public int getKillsPerHour() { return killsPerHour; }
    public long getConservativeGpPerHour() { return conservativeGpPerHour; }
    public long getOptimisticGpPerHour() { return optimisticGpPerHour; }
    public long getSupplyCostPerHour() { return supplyCostPerHour; }
    public long getAmmoCostPerHour() { return ammoCostPerHour; }
    public long getTotalCostsPerHour() { return supplyCostPerHour + ammoCostPerHour; }
    public long getNetProfitPerHour() { return Math.max(0, conservativeGpPerHour - getTotalCostsPerHour()); }
    public long getOptimisticNetProfitPerHour() { return Math.max(0, optimisticGpPerHour - getTotalCostsPerHour()); }
    public long getMinGearInvestment() { return minGearInvestment; }
    public boolean isAfkSuitable() { return afkSuitable; }
    public int getKillTimeSeconds() { return killTimeSeconds; }
    public int getRespawnSeconds() { return respawnSeconds; }
    public DeathRiskProfile getDeathRisk() { return deathRisk; }
    public String getAssumptions() { return assumptions; }

    public String getFormattedNetProfit() {
        long net = getNetProfitPerHour();
        if (net >= 1_000_000) {
            return String.format("%.2fM GP/hr", net / 1_000_000.0);
        } else if (net >= 1_000) {
            return String.format("%dK GP/hr", net / 1_000);
        }
        return net + " GP/hr";
    }

    public String getFormattedGpPerKill() {
        if (gpPerKill >= 1_000_000) {
            return String.format("%.2fM", gpPerKill / 1_000_000.0);
        } else if (gpPerKill >= 1_000) {
            return String.format("%dK", gpPerKill / 1_000);
        }
        return gpPerKill + " GP";
    }

    public String getFormattedMinGear() {
        if (minGearInvestment >= 1_000_000) {
            return String.format("%.1fM GP", minGearInvestment / 1_000_000.0);
        } else if (minGearInvestment >= 1_000) {
            return String.format("%dK GP", minGearInvestment / 1_000);
        }
        return minGearInvestment + " GP";
    }
}
