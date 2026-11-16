package com.bosswise.model;

public class ProgressionGoal {
    public enum GoalType {
        SKILL,
        QUEST,
        GEAR,
        UNLOCK
    }

    private final String id;
    private final GoalType type;
    private final String title;
    private final String description;
    private final int currentProgress;
    private final int targetProgress;
    private final boolean completed;
    private final int priority; // 1 (highest) to 10
    private final String associatedBoss;

    public ProgressionGoal(
            String id,
            GoalType type,
            String title,
            String description,
            int currentProgress,
            int targetProgress,
            boolean completed,
            int priority,
            String associatedBoss) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.currentProgress = currentProgress;
        this.targetProgress = targetProgress;
        this.completed = completed;
        this.priority = priority;
        this.associatedBoss = associatedBoss;
    }

    public String getId() { return id; }
    public GoalType getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getCurrentProgress() { return currentProgress; }
    public int getTargetProgress() { return targetProgress; }
    public boolean isCompleted() { return completed; }
    public int getPriority() { return priority; }
    public String getAssociatedBoss() { return associatedBoss; }

    public int getPercentage() {
        if (completed || targetProgress <= 0) return 100;
        int pct = (int) Math.round(((double) currentProgress / targetProgress) * 100.0);
        return Math.min(100, Math.max(0, pct));
    }
}
