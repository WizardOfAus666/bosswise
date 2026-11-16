package com.bosswise.model;

import java.util.Collections;
import java.util.List;

public class SlayerTaskInfo {
    public static class TaskLocation {
        private final String name;
        private final boolean cannonable;
        private final boolean multiCombat;
        private final boolean prayerRestoreBonus; // e.g. Catacombs bury prayer restore
        private final boolean slayerXpBonus;       // e.g. Slayer Tower diary bonus
        private final boolean wilderness;
        private final String description;

        public TaskLocation(String name, boolean cannonable, boolean multiCombat, boolean prayerRestoreBonus, boolean slayerXpBonus, boolean wilderness, String description) {
            this.name = name;
            this.cannonable = cannonable;
            this.multiCombat = multiCombat;
            this.prayerRestoreBonus = prayerRestoreBonus;
            this.slayerXpBonus = slayerXpBonus;
            this.wilderness = wilderness;
            this.description = description != null ? description : "";
        }

        public String getName() { return name; }
        public boolean isCannonable() { return cannonable; }
        public boolean isMultiCombat() { return multiCombat; }
        public boolean isPrayerRestoreBonus() { return prayerRestoreBonus; }
        public boolean isSlayerXpBonus() { return slayerXpBonus; }
        public boolean isWilderness() { return wilderness; }
        public String getDescription() { return description; }

        @Override public String toString() { return name; }
    }

    private final String taskName;
    private final int slayerLevelReq;
    private final CombatStyle recommendedStyle;
    private final String recommendedWeapon;
    private final int estimatedXpPerHour;
    private final int estimatedGpPerHour;
    private final int killsPerHour;
    private final List<TaskLocation> locations;
    private final String superiorVariantName;
    private final String superiorUniqueDrops;
    private final String strategyNotes;

    public SlayerTaskInfo(
            String taskName,
            int slayerLevelReq,
            CombatStyle recommendedStyle,
            String recommendedWeapon,
            int estimatedXpPerHour,
            int estimatedGpPerHour,
            int killsPerHour,
            List<TaskLocation> locations,
            String superiorVariantName,
            String superiorUniqueDrops,
            String strategyNotes) {
        this.taskName = taskName;
        this.slayerLevelReq = slayerLevelReq;
        this.recommendedStyle = recommendedStyle != null ? recommendedStyle : CombatStyle.MELEE;
        this.recommendedWeapon = recommendedWeapon != null ? recommendedWeapon : "Whip / Fang";
        this.estimatedXpPerHour = estimatedXpPerHour;
        this.estimatedGpPerHour = estimatedGpPerHour;
        this.killsPerHour = killsPerHour;
        this.locations = locations != null ? locations : Collections.emptyList();
        this.superiorVariantName = superiorVariantName != null ? superiorVariantName : "None";
        this.superiorUniqueDrops = superiorUniqueDrops != null ? superiorUniqueDrops : "None";
        this.strategyNotes = strategyNotes != null ? strategyNotes : "";
    }

    public String getTaskName() { return taskName; }
    public int getSlayerLevelReq() { return slayerLevelReq; }
    public CombatStyle getRecommendedStyle() { return recommendedStyle; }
    public String getRecommendedWeapon() { return recommendedWeapon; }
    public int getEstimatedXpPerHour() { return estimatedXpPerHour; }
    public int getEstimatedGpPerHour() { return estimatedGpPerHour; }
    public int getKillsPerHour() { return killsPerHour; }
    public List<TaskLocation> getLocations() { return locations; }
    public String getSuperiorVariantName() { return superiorVariantName; }
    public String getSuperiorUniqueDrops() { return superiorUniqueDrops; }
    public String getStrategyNotes() { return strategyNotes; }

    public int estimateTaskDurationMinutes(int quantityRemaining) {
        if (killsPerHour <= 0 || quantityRemaining <= 0) return 0;
        return (int) Math.ceil(((double) quantityRemaining / (double) killsPerHour) * 60.0);
    }
}
