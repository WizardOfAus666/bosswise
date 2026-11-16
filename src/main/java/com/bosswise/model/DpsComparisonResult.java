package com.bosswise.model;

import java.util.Collections;
import java.util.Map;

public class DpsComparisonResult {
    private final String monsterName;
    private final CombatStyle combatStyle;
    private final SetupEvaluation setupA;
    private final SetupEvaluation setupB;

    public DpsComparisonResult(
            String monsterName,
            CombatStyle combatStyle,
            SetupEvaluation setupA,
            SetupEvaluation setupB) {
        this.monsterName = monsterName != null ? monsterName : "Target Monster";
        this.combatStyle = combatStyle != null ? combatStyle : CombatStyle.MELEE;
        this.setupA = setupA;
        this.setupB = setupB;
    }

    public String getMonsterName() { return monsterName; }
    public CombatStyle getCombatStyle() { return combatStyle; }
    public SetupEvaluation getSetupA() { return setupA; }
    public SetupEvaluation getSetupB() { return setupB; }

    public double getDpsDifference() {
        if (setupA == null || setupB == null) return 0.0;
        return setupB.getDps() - setupA.getDps();
    }

    public double getDpsPercentageImprovement() {
        if (setupA == null || setupB == null || setupA.getDps() <= 0) return 0.0;
        return ((setupB.getDps() - setupA.getDps()) / setupA.getDps()) * 100.0;
    }

    public int getMaxHitDifference() {
        if (setupA == null || setupB == null) return 0;
        return setupB.getMaxHit() - setupA.getMaxHit();
    }

    public double getAccuracyDifference() {
        if (setupA == null || setupB == null) return 0.0;
        return (setupB.getHitChance() - setupA.getHitChance()) * 100.0;
    }

    public long getCostDifference() {
        if (setupA == null || setupB == null) return 0L;
        return setupB.getTotalPrice() - setupA.getTotalPrice();
    }

    public double getDpsPerMillionGp() {
        long costDiff = getCostDifference();
        if (costDiff <= 0) return 0.0;
        double dpsDiff = getDpsDifference();
        return (dpsDiff / (double) costDiff) * 1_000_000.0;
    }

    public String getVerdict() {
        double dpsDiff = getDpsDifference();
        long costDiff = getCostDifference();
        if (dpsDiff > 0.05) {
            if (costDiff <= 0) {
                return String.format("Setup B is +%.2f DPS (+%.1f%%) stronger and saves %s GP!",
                        dpsDiff, getDpsPercentageImprovement(), formatGp(Math.abs(costDiff)));
            } else {
                return String.format("Setup B yields +%.2f DPS (+%.1f%%) for %s GP extra (%.3f DPS/M GP).",
                        dpsDiff, getDpsPercentageImprovement(), formatGp(costDiff), getDpsPerMillionGp());
            }
        } else if (dpsDiff < -0.05) {
            return String.format("Setup A outperforms Setup B by +%.2f DPS.", Math.abs(dpsDiff));
        } else {
            return "Both setups provide essentially identical combat damage output.";
        }
    }

    private static String formatGp(long amount) {
        if (amount >= 1_000_000) {
            return String.format("%.1fM", amount / 1_000_000.0);
        } else if (amount >= 1_000) {
            return String.format("%dK", amount / 1_000);
        }
        return amount + " GP";
    }

    public static class SetupEvaluation {
        private final String name;
        private final Map<EquipmentSlot, EquipmentItem> equipment;
        private final CombatResult combatResult;
        private final long totalPrice;

        public SetupEvaluation(
                String name,
                Map<EquipmentSlot, EquipmentItem> equipment,
                CombatResult combatResult,
                long totalPrice) {
            this.name = name;
            this.equipment = equipment != null ? equipment : Collections.emptyMap();
            this.combatResult = combatResult;
            this.totalPrice = totalPrice;
        }

        public String getName() { return name; }
        public Map<EquipmentSlot, EquipmentItem> getEquipment() { return equipment; }
        public CombatResult getCombatResult() { return combatResult; }
        public long getTotalPrice() { return totalPrice; }

        public double getDps() {
            return (combatResult != null) ? combatResult.getDps() : 0.0;
        }

        public int getMaxHit() {
            return (combatResult != null) ? combatResult.getMaxHit() : 0;
        }

        public double getHitChance() {
            return (combatResult != null) ? combatResult.getHitChance() : 0.0;
        }

        public double getAttacksPerSecond() {
            if (combatResult == null || combatResult.getAttackSpeedTicks() <= 0) return 0.417; // 4 ticks = 2.4s = 0.417/s
            return 1.0 / (combatResult.getAttackSpeedTicks() * 0.6);
        }

        public long getAttackRoll() {
            return (combatResult != null) ? combatResult.getAttackRoll() : 0L;
        }

        public long getDefenceRoll() {
            return (combatResult != null) ? combatResult.getDefenceRoll() : 0L;
        }
    }
}
