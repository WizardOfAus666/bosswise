package com.bosswise.model;

public class DeathRiskProfile {
    private final String locationType;
    private final long gravestoneFee;
    private final long retrievalServiceFee;
    private final long estimatedSupplyLoss;
    private final boolean pvpDangerous;
    private final String explanation;

    public DeathRiskProfile(
            String locationType,
            long gravestoneFee,
            long retrievalServiceFee,
            long estimatedSupplyLoss,
            boolean pvpDangerous,
            String explanation) {
        this.locationType = locationType != null ? locationType : "Standard Overworld";
        this.gravestoneFee = gravestoneFee;
        this.retrievalServiceFee = retrievalServiceFee;
        this.estimatedSupplyLoss = estimatedSupplyLoss;
        this.pvpDangerous = pvpDangerous;
        this.explanation = explanation != null ? explanation : "";
    }

    public static DeathRiskProfile forMonster(String monsterName, boolean isWilderness, long gearValue) {
        String n = monsterName != null ? monsterName.toLowerCase() : "";
        if (isWilderness || n.contains("wilderness") || n.contains("callisto") || n.contains("vet'ion")
                || n.contains("venenatis") || n.contains("chaos elemental") || n.contains("artio")
                || n.contains("spindel") || n.contains("calvar'ion") || n.contains("revenant")) {
            long pvpLoss = Math.min(gearValue, 500_000L) + 50_000L; // untradeable repair fees + supplies
            return new DeathRiskProfile(
                    "Wilderness (Dangerous PvP)",
                    0L,
                    0L,
                    pvpLoss,
                    true,
                    "Items above 3 kept on death are converted to coins for the killer. Untradeables broken."
            );
        } else if (n.contains("vorkath") || n.contains("zulrah") || n.contains("phantom muspah")
                || n.contains("the whisperer") || n.contains("vardorvis") || n.contains("duke sucellus")
                || n.contains("the leviathan") || n.contains("hydra") || n.contains("nex")) {
            return new DeathRiskProfile(
                    "Item Retrieval Service",
                    0L,
                    100_000L,
                    35_000L,
                    false,
                    "Flat 100,000 GP reclaim fee collected by the retrieval NPC outside the encounter."
            );
        } else if (n.contains("theatre of blood") || n.contains("tombs of amascut")) {
            return new DeathRiskProfile(
                    "Raid Wipe Chest",
                    0L,
                    100_000L,
                    45_000L,
                    false,
                    "Party wipe fee paid to the chest to recover untradeables and equipment."
            );
        } else {
            // Standard gravestone mechanics: items over 10M = 100K fee, 1M-10M = 10K fee, 100K-1M = 1K fee. Cap 500K
            long fee = Math.min(500_000L, (long) (gearValue * 0.005));
            return new DeathRiskProfile(
                    "Standard Gravestone (15 Min)",
                    fee,
                    0L,
                    20_000L,
                    false,
                    "Gravestone spawns for 15 minutes. Items over 100k incur a tiered recovery fee capped at 500k."
            );
        }
    }

    public String getLocationType() { return locationType; }
    public long getGravestoneFee() { return gravestoneFee; }
    public long getRetrievalServiceFee() { return retrievalServiceFee; }
    public long getEstimatedSupplyLoss() { return estimatedSupplyLoss; }
    public boolean isPvpDangerous() { return pvpDangerous; }
    public String getExplanation() { return explanation; }

    public long getTotalEstimatedDeathCost() {
        return gravestoneFee + retrievalServiceFee + estimatedSupplyLoss;
    }

    public String getFormattedTotalCost() {
        long total = getTotalEstimatedDeathCost();
        if (total >= 1_000_000) {
            return String.format("%.1fM GP", total / 1_000_000.0);
        } else if (total >= 1_000) {
            return String.format("%dK GP", total / 1_000);
        }
        return total + " GP";
    }
}
