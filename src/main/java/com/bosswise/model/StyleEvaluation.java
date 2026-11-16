package com.bosswise.model;

public class StyleEvaluation {
    private final CombatStyle style;
    private final EquipmentItem.AttackType attackType;
    private final CombatResult combatResult;
    private final OptimizedGearSetup setup;
    private final double dps;
    private final String reason;
    private final boolean recommended;

    public StyleEvaluation(
            CombatStyle style,
            EquipmentItem.AttackType attackType,
            CombatResult combatResult,
            OptimizedGearSetup setup,
            double dps,
            String reason,
            boolean recommended) {
        this.style = style;
        this.attackType = attackType;
        this.combatResult = combatResult;
        this.setup = setup;
        this.dps = dps;
        this.reason = reason != null ? reason : "";
        this.recommended = recommended;
    }

    public CombatStyle getStyle() { return style; }
    public EquipmentItem.AttackType getAttackType() { return attackType; }
    public CombatResult getCombatResult() { return combatResult; }
    public OptimizedGearSetup getSetup() { return setup; }
    public double getDps() { return dps; }
    public String getReason() { return reason; }
    public boolean isRecommended() { return recommended; }

    public String getFormattedDps() {
        return String.format("%.2f DPS", dps);
    }
}
