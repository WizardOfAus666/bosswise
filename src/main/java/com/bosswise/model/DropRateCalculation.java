package com.bosswise.model;

public class DropRateCalculation {
    private final String itemName;
    private final String monsterName;
    private final double baseProbability;
    private final int expectedKills;
    private final int medianKills;
    private final int ninetyPercentKills;
    private final int ninetyNinePercentKills;
    private final double chanceAt10;
    private final double chanceAt50;
    private final double chanceAt100;
    private final double chanceAt500;
    private final double chanceAt1000;
    private final int guaranteedThreshold; // e.g. 50 for Vorkath head, 256 for KQ head, -1 if none
    private final String specialMechanicDescription;

    public DropRateCalculation(
            String itemName,
            String monsterName,
            double baseProbability,
            int guaranteedThreshold,
            String specialMechanicDescription) {
        this.itemName = itemName != null ? itemName : "Unique Drop";
        this.monsterName = monsterName != null ? monsterName : "Boss";
        this.baseProbability = Math.max(0.0000001, Math.min(1.0, baseProbability));
        this.guaranteedThreshold = guaranteedThreshold;
        this.specialMechanicDescription = specialMechanicDescription != null ? specialMechanicDescription : "";

        this.expectedKills = (int) Math.round(1.0 / this.baseProbability);
        this.medianKills = (int) Math.ceil(Math.log(0.5) / Math.log(1.0 - this.baseProbability));
        this.ninetyPercentKills = (int) Math.ceil(Math.log(0.1) / Math.log(1.0 - this.baseProbability));
        this.ninetyNinePercentKills = (int) Math.ceil(Math.log(0.01) / Math.log(1.0 - this.baseProbability));

        this.chanceAt10 = calculateChanceAtKc(10);
        this.chanceAt50 = calculateChanceAtKc(50);
        this.chanceAt100 = calculateChanceAtKc(100);
        this.chanceAt500 = calculateChanceAtKc(500);
        this.chanceAt1000 = calculateChanceAtKc(1000);
    }

    public double calculateChanceAtKc(int kc) {
        if (kc <= 0) return 0.0;
        if (guaranteedThreshold > 0 && kc >= guaranteedThreshold) {
            return 1.0; // 100% pity guarantee
        }
        return 1.0 - Math.pow(1.0 - baseProbability, kc);
    }

    public double calculateDryChanceAtKc(int kc) {
        if (guaranteedThreshold > 0 && kc >= guaranteedThreshold) {
            return 0.0;
        }
        return Math.max(0.0, 1.0 - calculateChanceAtKc(kc));
    }

    public String getItemName() { return itemName; }
    public String getMonsterName() { return monsterName; }
    public double getBaseProbability() { return baseProbability; }
    public int getExpectedKills() { return expectedKills; }
    public int getMedianKills() { return medianKills; }
    public int getNinetyPercentKills() { return ninetyPercentKills; }
    public int getNinetyNinePercentKills() { return ninetyNinePercentKills; }
    public double getChanceAt10() { return chanceAt10; }
    public double getChanceAt50() { return chanceAt50; }
    public double getChanceAt100() { return chanceAt100; }
    public double getChanceAt500() { return chanceAt500; }
    public double getChanceAt1000() { return chanceAt1000; }
    public int getGuaranteedThreshold() { return guaranteedThreshold; }
    public String getSpecialMechanicDescription() { return specialMechanicDescription; }

    public String getFormattedFraction() {
        return "1 in " + expectedKills;
    }
}
