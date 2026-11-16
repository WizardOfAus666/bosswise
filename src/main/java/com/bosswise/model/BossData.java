package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;

public class BossData {
    private final String id;
    private final String name;
    private final BossCategory category;
    private final BossDifficulty difficulty;
    private final CombatStyle primaryStyle;
    private final int recommendedCombatLevel;
    private final long suggestedGearBudget;

    // Requirements
    private final List<SkillRequirement> minimumSkillRequirements;
    private final Map<Skill, Integer> recommendedSkills;
    private final List<QuestRequirement> questRequirements;
    private final List<String> entryPrerequisites;
    private final boolean solo;
    private final boolean group;

    // Economics
    private final long estimatedGpPerKill;
    private final long estimatedGpHour;

    // Strategy & Guide
    private final String weakness;
    private final String overheadPrayer;
    private final String offensivePrayer;
    private final String howToGetThere;
    private final String fightStrategy;
    private final String mechanicsSummary;
    private final String phaseChanges;
    private final String importantWarnings;
    private final String beginnerTips;
    private final String advancedTips;
    private final List<String> mechanicChecklist;
    private final List<String> notableDrops;

    // Inventory templates
    private final Map<String, InventorySetup> inventoryPresets;

    private BossData(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.difficulty = builder.difficulty;
        this.primaryStyle = builder.primaryStyle;
        this.recommendedCombatLevel = builder.recommendedCombatLevel;
        this.suggestedGearBudget = builder.suggestedGearBudget;
        this.minimumSkillRequirements = Collections.unmodifiableList(builder.minimumSkillRequirements);
        this.recommendedSkills = Collections.unmodifiableMap(builder.recommendedSkills);
        this.questRequirements = Collections.unmodifiableList(builder.questRequirements);
        this.entryPrerequisites = Collections.unmodifiableList(builder.entryPrerequisites);
        this.solo = builder.solo;
        this.group = builder.group;
        this.estimatedGpPerKill = builder.estimatedGpPerKill;
        this.estimatedGpHour = builder.estimatedGpHour;
        this.weakness = builder.weakness;
        this.overheadPrayer = builder.overheadPrayer;
        this.offensivePrayer = builder.offensivePrayer;
        this.howToGetThere = builder.howToGetThere;
        this.fightStrategy = builder.fightStrategy;
        this.mechanicsSummary = builder.mechanicsSummary;
        this.phaseChanges = builder.phaseChanges;
        this.importantWarnings = builder.importantWarnings;
        this.beginnerTips = builder.beginnerTips;
        this.advancedTips = builder.advancedTips;
        this.mechanicChecklist = Collections.unmodifiableList(builder.mechanicChecklist);
        this.notableDrops = Collections.unmodifiableList(builder.notableDrops);
        this.inventoryPresets = Collections.unmodifiableMap(builder.inventoryPresets);
    }

    public static Builder builder(String id, String name, BossCategory category, BossDifficulty difficulty, CombatStyle primaryStyle) {
        return new Builder(id, name, category, difficulty, primaryStyle);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BossCategory getCategory() { return category; }
    public BossDifficulty getDifficulty() { return difficulty; }
    public CombatStyle getPrimaryStyle() { return primaryStyle; }
    public int getRecommendedCombatLevel() { return recommendedCombatLevel; }
    public long getSuggestedGearBudget() { return suggestedGearBudget; }
    public List<SkillRequirement> getMinimumSkillRequirements() { return minimumSkillRequirements; }
    public Map<Skill, Integer> getRecommendedSkills() { return recommendedSkills; }
    public List<QuestRequirement> getQuestRequirements() { return questRequirements; }
    public List<String> getEntryPrerequisites() { return entryPrerequisites; }
    public boolean isSolo() { return solo; }
    public boolean isGroup() { return group; }
    public long getEstimatedGpPerKill() { return estimatedGpPerKill; }
    public long getEstimatedGpHour() { return estimatedGpHour; }
    public String getWeakness() { return weakness; }
    public String getOverheadPrayer() { return overheadPrayer; }
    public String getOffensivePrayer() { return offensivePrayer; }
    public String getHowToGetThere() { return howToGetThere; }
    public String getFightStrategy() { return fightStrategy; }
    public String getMechanicsSummary() { return mechanicsSummary; }
    public String getPhaseChanges() { return phaseChanges; }
    public String getImportantWarnings() { return importantWarnings; }
    public String getBeginnerTips() { return beginnerTips; }
    public String getAdvancedTips() { return advancedTips; }
    public List<String> getMechanicChecklist() { return mechanicChecklist; }
    public List<String> getNotableDrops() { return notableDrops; }
    public Map<String, InventorySetup> getInventoryPresets() { return inventoryPresets; }
    public InventorySetup getInventoryPreset(String tier) { return inventoryPresets.get(tier); }

    public int getHitpoints() { return 750; }
    public int getMaxHit() { return 30; }

    public MonsterVariant toVariant() {
        return new MonsterVariant(
                0,
                name,
                howToGetThere != null ? howToGetThere : "Boss Lair",
                recommendedCombatLevel,
                750,
                150, 150, 150, 150, 150,
                100, 100, 100, 100, 100,
                4,
                Collections.emptyList(),
                category == BossCategory.WILDERNESS,
                group,
                false
        );
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final BossCategory category;
        private final BossDifficulty difficulty;
        private final CombatStyle primaryStyle;
        private int recommendedCombatLevel = 75;
        private long suggestedGearBudget = 2_500_000L;
        private final List<SkillRequirement> minimumSkillRequirements = new ArrayList<>();
        private final Map<Skill, Integer> recommendedSkills = new HashMap<>();
        private final List<QuestRequirement> questRequirements = new ArrayList<>();
        private final List<String> entryPrerequisites = new ArrayList<>();
        private boolean solo = true;
        private boolean group = false;
        private long estimatedGpPerKill = 50_000L;
        private long estimatedGpHour = 1_000_000L;
        private String weakness = "None";
        private String overheadPrayer = "Protect from Missiles";
        private String offensivePrayer = "Rigour / Eagle Eye";
        private String howToGetThere = "Teleport";
        private String fightStrategy = "General strategy";
        private String mechanicsSummary = "Mechanics details";
        private String phaseChanges = "None";
        private String importantWarnings = "Stay alert";
        private String beginnerTips = "Bring combo food";
        private String advancedTips = "Maximize DPS ticks";
        private final List<String> mechanicChecklist = new ArrayList<>();
        private final List<String> notableDrops = new ArrayList<>();
        private final Map<String, InventorySetup> inventoryPresets = new HashMap<>();

        public Builder(String id, String name, BossCategory category, BossDifficulty difficulty, CombatStyle primaryStyle) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.difficulty = difficulty;
            this.primaryStyle = primaryStyle;
        }

        public Builder combatLevel(int level) { this.recommendedCombatLevel = level; return this; }
        public Builder budget(long gp) { this.suggestedGearBudget = gp; return this; }
        public Builder minSkill(Skill skill, int level) { this.minimumSkillRequirements.add(new SkillRequirement(skill, level)); return this; }
        public Builder minSkill(Skill skill, int level, boolean boostable, String note) { this.minimumSkillRequirements.add(new SkillRequirement(skill, level, boostable, note)); return this; }
        public Builder recSkill(Skill skill, int level) { this.recommendedSkills.put(skill, level); return this; }
        public Builder quest(QuestRequirement quest) { this.questRequirements.add(quest); return this; }
        public Builder prerequisite(String prereq) { this.entryPrerequisites.add(prereq); return this; }
        public Builder solo(boolean val) { this.solo = val; return this; }
        public Builder group(boolean val) { this.group = val; return this; }
        public Builder gp(long perKill, long perHour) { this.estimatedGpPerKill = perKill; this.estimatedGpHour = perHour; return this; }
        public Builder weakness(String w) { this.weakness = w; return this; }
        public Builder prayers(String overhead, String offensive) { this.overheadPrayer = overhead; this.offensivePrayer = offensive; return this; }
        public Builder travel(String t) { this.howToGetThere = t; return this; }
        public Builder strategy(String s) { this.fightStrategy = s; return this; }
        public Builder mechanics(String m) { this.mechanicsSummary = m; return this; }
        public Builder phases(String p) { this.phaseChanges = p; return this; }
        public Builder warnings(String w) { this.importantWarnings = w; return this; }
        public Builder tips(String beginner, String advanced) { this.beginnerTips = beginner; this.advancedTips = advanced; return this; }
        public Builder addChecklist(String item) { this.mechanicChecklist.add(item); return this; }
        public Builder addDrop(String drop) { this.notableDrops.add(drop); return this; }
        public Builder addInventoryPreset(String name, InventorySetup setup) { this.inventoryPresets.put(name, setup); return this; }

        public BossData build() {
            return new BossData(this);
        }
    }
}
