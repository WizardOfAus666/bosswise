package com.bosswise.model;

public class CombatResult {
    private final CombatStyle combatStyle;
    private final EquipmentItem.AttackType attackType;
    private final double hitChance;
    private final int maxHit;
    private final double expectedDamagePerHit;
    private final int attackSpeedTicks;
    private final double dps;
    private final long attackRoll;
    private final long defenceRoll;
    private final String formulaDetails;

    public CombatResult(
            CombatStyle combatStyle,
            EquipmentItem.AttackType attackType,
            double hitChance,
            int maxHit,
            double expectedDamagePerHit,
            int attackSpeedTicks,
            double dps,
            long attackRoll,
            long defenceRoll,
            String formulaDetails) {
        this.combatStyle = combatStyle;
        this.attackType = attackType;
        this.hitChance = hitChance;
        this.maxHit = maxHit;
        this.expectedDamagePerHit = expectedDamagePerHit;
        this.attackSpeedTicks = attackSpeedTicks > 0 ? attackSpeedTicks : 4;
        this.dps = dps;
        this.attackRoll = attackRoll;
        this.defenceRoll = defenceRoll;
        this.formulaDetails = formulaDetails != null ? formulaDetails : "";
    }

    public CombatStyle getCombatStyle() { return combatStyle; }
    public EquipmentItem.AttackType getAttackType() { return attackType; }
    public double getHitChance() { return hitChance; }
    public int getMaxHit() { return maxHit; }
    public double getExpectedDamagePerHit() { return expectedDamagePerHit; }
    public int getAttackSpeedTicks() { return attackSpeedTicks; }
    public double getDps() { return dps; }
    public long getAttackRoll() { return attackRoll; }
    public long getDefenceRoll() { return defenceRoll; }
    public String getFormulaDetails() { return formulaDetails; }

    public String getFormattedHitChance() {
        return String.format("%.1f%%", hitChance * 100.0);
    }

    public String getFormattedDps() {
        return String.format("%.2f", dps);
    }
}
