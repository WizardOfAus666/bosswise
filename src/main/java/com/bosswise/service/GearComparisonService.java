package com.bosswise.service;

import com.bosswise.model.BossData;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.MultiSetupComparison;
import com.bosswise.model.MultiSetupComparison.SetupComparisonMetric;
import com.bosswise.model.OptimizedGearSetup;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GearComparisonService {
    private final InventoryBuilderService inventoryBuilderService;

    @Inject
    public GearComparisonService(InventoryBuilderService inventoryBuilderService) {
        this.inventoryBuilderService = inventoryBuilderService;
    }

    public MultiSetupComparison compareSetups(List<OptimizedGearSetup> setups) {
        if (setups == null || setups.isEmpty()) {
            return new MultiSetupComparison(
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new EnumMap<>(EquipmentSlot.class),
                    new HashMap<>(),
                    new ArrayList<>()
            );
        }

        // Limit to at most 5 setups for side-by-side comparison
        int limit = Math.min(5, setups.size());
        List<OptimizedGearSetup> activeSetups = new ArrayList<>(setups.subList(0, limit));

        List<SetupComparisonMetric> metrics = new ArrayList<>();

        // Metric: Setup Name
        List<String> names = new ArrayList<>();
        for (OptimizedGearSetup s : activeSetups) {
            names.add(s.getSetupName());
        }
        metrics.add(new SetupComparisonMetric("Setup Tier", names, -1));

        // Metric: Combat Style
        List<String> styles = new ArrayList<>();
        for (OptimizedGearSetup s : activeSetups) {
            styles.add(s.getCombatStyle().getDisplayName());
        }
        metrics.add(new SetupComparisonMetric("Combat Style", styles, -1));

        // Metric: Calculated DPS
        List<String> dpsValues = new ArrayList<>();
        int bestDpsIdx = 0;
        double maxDps = -1.0;
        for (int i = 0; i < activeSetups.size(); i++) {
            double dps = activeSetups.get(i).getCalculatedDps();
            dpsValues.add(String.format("%.2f DPS", dps));
            if (dps > maxDps) {
                maxDps = dps;
                bestDpsIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Calculated DPS", dpsValues, bestDpsIdx));

        // Metric: Hit Chance
        List<String> hitChances = new ArrayList<>();
        int bestHitIdx = 0;
        double maxHitChance = -1.0;
        for (int i = 0; i < activeSetups.size(); i++) {
            double hc = (activeSetups.get(i).getCombatResult() != null)
                    ? activeSetups.get(i).getCombatResult().getHitChance() : 0.0;
            hitChances.add(String.format("%.1f%%", hc * 100));
            if (hc > maxHitChance) {
                maxHitChance = hc;
                bestHitIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Hit Chance", hitChances, bestHitIdx));

        // Metric: Max Hit
        List<String> maxHits = new ArrayList<>();
        int bestMaxHitIdx = 0;
        int highestMaxHit = -1;
        for (int i = 0; i < activeSetups.size(); i++) {
            int mh = activeSetups.get(i).getMaxHit();
            maxHits.add(String.valueOf(mh));
            if (mh > highestMaxHit) {
                highestMaxHit = mh;
                bestMaxHitIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Max Hit", maxHits, bestMaxHitIdx));

        // Metric: Total Loadout Cost
        List<String> costs = new ArrayList<>();
        int lowestCostIdx = 0;
        long lowestCost = Long.MAX_VALUE;
        for (int i = 0; i < activeSetups.size(); i++) {
            long cost = activeSetups.get(i).getTotalCost();
            costs.add(formatGp(cost));
            if (cost < lowestCost) {
                lowestCost = cost;
                lowestCostIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Total Cost", costs, lowestCostIdx));

        // Metric: Prayer Bonus
        List<String> prayers = new ArrayList<>();
        int bestPrayerIdx = 0;
        int maxPrayer = -1;
        for (int i = 0; i < activeSetups.size(); i++) {
            int pray = activeSetups.get(i).getTotalPrayerBonus();
            prayers.add("+" + pray);
            if (pray > maxPrayer) {
                maxPrayer = pray;
                bestPrayerIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Prayer Bonus", prayers, bestPrayerIdx));

        // Metric: Total Defence
        List<String> defences = new ArrayList<>();
        int bestDefIdx = 0;
        int maxDef = -1;
        for (int i = 0; i < activeSetups.size(); i++) {
            int def = activeSetups.get(i).getTotalDefenceBonus();
            defences.add("+" + def);
            if (def > maxDef) {
                maxDef = def;
                bestDefIdx = i;
            }
        }
        metrics.add(new SetupComparisonMetric("Avg Defence", defences, bestDefIdx));

        // Metric: DPS per Million GP (relative to lowest cost setup)
        List<String> dpsPerM = new ArrayList<>();
        OptimizedGearSetup base = activeSetups.get(lowestCostIdx);
        double baseDps = base.getCalculatedDps();
        for (OptimizedGearSetup s : activeSetups) {
            if (s == base) {
                dpsPerM.add("Baseline");
            } else {
                double deltaDps = s.getCalculatedDps() - baseDps;
                long deltaCost = s.getTotalCost() - base.getTotalCost();
                if (deltaCost > 0 && deltaDps > 0) {
                    double val = deltaDps / (deltaCost / 1_000_000.0);
                    dpsPerM.add(String.format("+%.2f /M GP", val));
                } else if (deltaDps <= 0) {
                    dpsPerM.add("Lower DPS");
                } else {
                    dpsPerM.add("Free Upgrade");
                }
            }
        }
        metrics.add(new SetupComparisonMetric("DPS Efficiency", dpsPerM, -1));

        // Slot Matrix
        EquipmentSlot[] allSlots = new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CAPE, EquipmentSlot.AMULET, EquipmentSlot.AMMO,
                EquipmentSlot.WEAPON, EquipmentSlot.BODY, EquipmentSlot.SHIELD, EquipmentSlot.LEGS,
                EquipmentSlot.GLOVES, EquipmentSlot.BOOTS, EquipmentSlot.RING, EquipmentSlot.SPEC_WEAPON
        };

        Map<EquipmentSlot, List<EquipmentItem>> slotMatrix = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : allSlots) {
            List<EquipmentItem> row = new ArrayList<>();
            for (OptimizedGearSetup s : activeSetups) {
                row.add(s.getItem(slot));
            }
            slotMatrix.put(slot, row);
        }

        // Matched Inventories
        Map<Integer, InventorySetup> inventories = new HashMap<>();
        if (inventoryBuilderService != null && !activeSetups.isEmpty()) {
            BossData boss = activeSetups.get(0).getBoss();
            if (boss == null && activeSetups.get(0).getMonster() != null) {
                boss = activeSetups.get(0).getMonster().toBossData();
            }
            if (boss != null) {
                for (int i = 0; i < activeSetups.size(); i++) {
                    String tierName = activeSetups.get(i).getSetupName();
                    inventories.put(i, inventoryBuilderService.getInventorySetup(boss, mapSetupTierToInventoryTier(tierName)));
                }
            }
        }

        // Insights
        List<String> insights = generateInsights(activeSetups, bestDpsIdx, lowestCostIdx);

        return new MultiSetupComparison(activeSetups, metrics, slotMatrix, inventories, insights);
    }

    private List<String> generateInsights(List<OptimizedGearSetup> setups, int bestDpsIdx, int lowestCostIdx) {
        List<String> insights = new ArrayList<>();
        if (setups.isEmpty()) return insights;

        OptimizedGearSetup bestDps = setups.get(bestDpsIdx);
        OptimizedGearSetup lowestCost = setups.get(lowestCostIdx);

        insights.add(String.format("Top DPS: %s with %.2f DPS (Max Hit: %d, Accuracy: %s).",
                bestDps.getSetupName(), bestDps.getCalculatedDps(), bestDps.getMaxHit(), bestDps.getHitChance()));

        if (bestDps != lowestCost) {
            double dpsDiff = bestDps.getCalculatedDps() - lowestCost.getCalculatedDps();
            double pctDiff = lowestCost.getCalculatedDps() > 0 ? (dpsDiff / lowestCost.getCalculatedDps()) * 100 : 0.0;
            long costDiff = bestDps.getTotalCost() - lowestCost.getTotalCost();
            insights.add(String.format("Upgrade from %s to %s yields +%.1f%% DPS for +%s GP.",
                    lowestCost.getSetupName(), bestDps.getSetupName(), pctDiff, formatGp(costDiff)));
        }

        // Search for best value (highest DPS per GP)
        int bestValueIdx = -1;
        double highestEfficiency = -1.0;
        for (int i = 0; i < setups.size(); i++) {
            if (i == lowestCostIdx) continue;
            OptimizedGearSetup s = setups.get(i);
            double deltaDps = s.getCalculatedDps() - lowestCost.getCalculatedDps();
            long deltaCost = s.getTotalCost() - lowestCost.getTotalCost();
            if (deltaCost > 0 && deltaDps > 0) {
                double eff = deltaDps / (deltaCost / 1_000_000.0);
                if (eff > highestEfficiency) {
                    highestEfficiency = eff;
                    bestValueIdx = i;
                }
            }
        }

        if (bestValueIdx >= 0) {
            insights.add(String.format("Best Value Pick: %s delivers the highest DPS gain per GP spent.",
                    setups.get(bestValueIdx).getSetupName()));
        }

        return insights;
    }

    private String mapSetupTierToInventoryTier(String setupTier) {
        if (setupTier == null) return "Standard";
        String lower = setupTier.toLowerCase();
        if (lower.contains("budget") || lower.contains("entry")) return "Budget";
        if (lower.contains("value") || lower.contains("mid")) return "Standard";
        if (lower.contains("high")) return "Advanced";
        if (lower.contains("bis") || lower.contains("max")) return "Maximum Efficiency";
        return "Standard";
    }

    private String formatGp(long gp) {
        if (gp >= 1_000_000_000L) return String.format("%.2fB", gp / 1_000_000_000.0);
        if (gp >= 1_000_000L) return String.format("%.1fM", gp / 1_000_000.0);
        if (gp >= 1_000L) return String.format("%dK", gp / 1_000L);
        return gp + " GP";
    }
}
