package com.bosswise.model;

public class CombatAchievementTask {
    public enum CaTier {
        EASY("Easy", "#4CAF50"),
        MEDIUM("Medium", "#2196F3"),
        HARD("Hard", "#9C27B0"),
        ELITE("Elite", "#FF9800"),
        MASTER("Master", "#F44336"),
        GRANDMASTER("Grandmaster", "#E91E63");

        private final String displayName;
        private final String hexColor;

        CaTier(String displayName, String hexColor) {
            this.displayName = displayName;
            this.hexColor = hexColor;
        }

        public String getDisplayName() { return displayName; }
        public String getHexColor() { return hexColor; }
    }

    private final CaTier tier;
    private final String bossName;
    private final String taskName;
    private final String description;
    private final String strategyTip;
    private boolean completed;

    public CombatAchievementTask(
            CaTier tier,
            String bossName,
            String taskName,
            String description,
            String strategyTip,
            boolean completed) {
        this.tier = tier != null ? tier : CaTier.MEDIUM;
        this.bossName = bossName != null ? bossName : "General";
        this.taskName = taskName;
        this.description = description;
        this.strategyTip = strategyTip != null ? strategyTip : "";
        this.completed = completed;
    }

    public CaTier getTier() { return tier; }
    public String getBossName() { return bossName; }
    public String getTaskName() { return taskName; }
    public String getDescription() { return description; }
    public String getStrategyTip() { return strategyTip; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
