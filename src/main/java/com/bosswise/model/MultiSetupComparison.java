package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSetupComparison {
    private final List<OptimizedGearSetup> setups;
    private final Map<EquipmentSlot, List<EquipmentItem>> slotMatrix;
    private final Map<Integer, InventorySetup> matchedInventories;
    private final List<String> insights;

    public static class SetupComparisonMetric {
        private final String metricName;
        private final List<String> values;
        private final int bestIndex;

        public SetupComparisonMetric(String metricName, List<String> values, int bestIndex) {
            this.metricName = metricName;
            this.values = Collections.unmodifiableList(new ArrayList<>(values));
            this.bestIndex = bestIndex;
        }

        public String getMetricName() { return metricName; }
        public List<String> getValues() { return values; }
        public int getBestIndex() { return bestIndex; }
    }

    private final List<SetupComparisonMetric> metrics;

    public MultiSetupComparison(
            List<OptimizedGearSetup> setups,
            List<SetupComparisonMetric> metrics,
            Map<EquipmentSlot, List<EquipmentItem>> slotMatrix,
            Map<Integer, InventorySetup> matchedInventories,
            List<String> insights) {
        this.setups = Collections.unmodifiableList(new ArrayList<>(setups));
        this.metrics = Collections.unmodifiableList(new ArrayList<>(metrics));
        this.slotMatrix = Collections.unmodifiableMap(new EnumMap<>(slotMatrix));
        this.matchedInventories = Collections.unmodifiableMap(new HashMap<>(matchedInventories));
        this.insights = Collections.unmodifiableList(new ArrayList<>(insights));
    }

    public List<OptimizedGearSetup> getSetups() { return setups; }
    public List<SetupComparisonMetric> getMetrics() { return metrics; }
    public Map<EquipmentSlot, List<EquipmentItem>> getSlotMatrix() { return slotMatrix; }
    public Map<Integer, InventorySetup> getMatchedInventories() { return matchedInventories; }
    public List<String> getInsights() { return insights; }
}
