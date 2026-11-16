package com.bosswise.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;

public class MonsterData {
    private final String id;
    private final String name;
    private final MonsterCategory category;
    private final CombatStyle primaryStyle;
    private final int combatLevel;
    private final long suggestedBudget;
    private final List<MonsterVariant> variants;
    private final MonsterCombatProfile combatProfile;

    // Requirements & Slayer
    private final int requiredSlayerLevel;
    private final boolean slayerTaskOnly;
    private final String slayerCategory;
    private final String superiorVariantName;
    private final int superiorNpcId;

    // Strategy & Guide
    private final String weakness;
    private final String overheadPrayer;
    private final String offensivePrayer;
    private final String travel;
    private final String strategy;
    private final String mechanics;
    private final String warnings;
    private final List<String> checklist;
    private final List<String> notableDrops;
    private final Map<String, InventorySetup> inventoryPresets;

    private MonsterData(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.category = builder.category;
        this.primaryStyle = builder.primaryStyle;
        this.combatLevel = builder.combatLevel;
        this.suggestedBudget = builder.suggestedBudget;
        this.variants = Collections.unmodifiableList(new ArrayList<>(builder.variants));
        this.combatProfile = builder.combatProfile;

        this.requiredSlayerLevel = builder.requiredSlayerLevel;
        this.slayerTaskOnly = builder.slayerTaskOnly;
        this.slayerCategory = builder.slayerCategory;
        this.superiorVariantName = builder.superiorVariantName;
        this.superiorNpcId = builder.superiorNpcId;

        this.weakness = builder.weakness;
        this.overheadPrayer = builder.overheadPrayer;
        this.offensivePrayer = builder.offensivePrayer;
        this.travel = builder.travel;
        this.strategy = builder.strategy;
        this.mechanics = builder.mechanics;
        this.warnings = builder.warnings;
        this.checklist = Collections.unmodifiableList(new ArrayList<>(builder.checklist));
        this.notableDrops = Collections.unmodifiableList(new ArrayList<>(builder.notableDrops));
        this.inventoryPresets = Collections.unmodifiableMap(new HashMap<>(builder.inventoryPresets));
    }

    public static Builder builder(String id, String name, MonsterCategory category, CombatStyle primaryStyle) {
        return new Builder(id, name, category, primaryStyle);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public MonsterCategory getCategory() { return category; }
    public CombatStyle getPrimaryStyle() { return primaryStyle; }
    public int getCombatLevel() { return combatLevel; }
    public long getSuggestedBudget() { return suggestedBudget; }
    public List<MonsterVariant> getVariants() { return variants; }
    public MonsterCombatProfile getCombatProfile() { return combatProfile; }

    public int getRequiredSlayerLevel() { return requiredSlayerLevel; }
    public boolean isSlayerTaskOnly() { return slayerTaskOnly; }
    public String getSlayerCategory() { return slayerCategory; }
    public String getSuperiorVariantName() { return superiorVariantName; }
    public int getSuperiorNpcId() { return superiorNpcId; }

    public String getWeakness() { return weakness; }
    public String getOverheadPrayer() { return overheadPrayer; }
    public String getOffensivePrayer() { return offensivePrayer; }
    public String getTravel() { return travel; }
    public String getStrategy() { return strategy; }
    public String getMechanics() { return mechanics; }
    public String getWarnings() { return warnings; }
    public List<String> getChecklist() { return checklist; }
    public List<String> getNotableDrops() { return notableDrops; }
    public Map<String, InventorySetup> getInventoryPresets() { return inventoryPresets; }

    public MonsterVariant getPrimaryVariant() {
        if (!variants.isEmpty()) {
            return variants.get(0);
        }
        return new MonsterVariant(0, "Standard", "Various", combatLevel, 100, 50, 50, 50, 50, 50, 0, 0, 0, 0, 0, 4, Collections.emptyList(), false, false, true);
    }

    public MonsterVariant getVariantByNpcId(int npcId) {
        for (MonsterVariant v : variants) {
            if (v.getNpcId() == npcId) {
                return v;
            }
        }
        return getPrimaryVariant();
    }

    public BossData toBossData() {
        BossCategory bCat = BossCategory.INTERMEDIATE;
        if (category == MonsterCategory.REGULAR || category == MonsterCategory.SLAYER) {
            bCat = BossCategory.BEGINNER;
        } else if (category == MonsterCategory.RAID || category == MonsterCategory.BOSS) {
            bCat = BossCategory.ENDGAME;
        }

        BossDifficulty bDiff = BossDifficulty.INTERMEDIATE;
        if (combatLevel < 70) bDiff = BossDifficulty.BEGINNER;
        else if (combatLevel >= 250) bDiff = BossDifficulty.GRANDMASTER;
        else if (combatLevel >= 150) bDiff = BossDifficulty.MASTER;

        return BossData.builder(id, name, bCat, bDiff, primaryStyle)
                .combatLevel(combatLevel)
                .budget(suggestedBudget)
                .weakness(weakness)
                .prayers(overheadPrayer, offensivePrayer)
                .travel(travel)
                .strategy(strategy)
                .mechanics(mechanics)
                .warnings(warnings)
                .build();
    }

    public static class Builder {
        private final String id;
        private final String name;
        private final MonsterCategory category;
        private final CombatStyle primaryStyle;
        private int combatLevel = 50;
        private long suggestedBudget = 500_000L;
        private final List<MonsterVariant> variants = new ArrayList<>();
        private MonsterCombatProfile combatProfile;

        private int requiredSlayerLevel = 1;
        private boolean slayerTaskOnly = false;
        private String slayerCategory = "";
        private String superiorVariantName = "";
        private int superiorNpcId = -1;

        private String weakness = "";
        private String overheadPrayer = "";
        private String offensivePrayer = "";
        private String travel = "";
        private String strategy = "";
        private String mechanics = "";
        private String warnings = "";
        private final List<String> checklist = new ArrayList<>();
        private final List<String> notableDrops = new ArrayList<>();
        private final Map<String, InventorySetup> inventoryPresets = new HashMap<>();

        public Builder(String id, String name, MonsterCategory category, CombatStyle primaryStyle) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.primaryStyle = primaryStyle;
        }

        public Builder combatLevel(int level) { this.combatLevel = level; return this; }
        public Builder budget(long budget) { this.suggestedBudget = budget; return this; }
        public Builder addVariant(MonsterVariant variant) { this.variants.add(variant); return this; }
        public Builder combatProfile(MonsterCombatProfile profile) { this.combatProfile = profile; return this; }

        public Builder slayer(int reqLevel, String category, boolean taskOnly) {
            this.requiredSlayerLevel = reqLevel;
            this.slayerCategory = category;
            this.slayerTaskOnly = taskOnly;
            return this;
        }

        public Builder superior(String name, int npcId) {
            this.superiorVariantName = name;
            this.superiorNpcId = npcId;
            return this;
        }

        public Builder weakness(String w) { this.weakness = w; return this; }
        public Builder prayers(String overhead, String offensive) {
            this.overheadPrayer = overhead;
            this.offensivePrayer = offensive;
            return this;
        }
        public Builder travel(String t) { this.travel = t; return this; }
        public Builder strategy(String s) { this.strategy = s; return this; }
        public Builder mechanics(String m) { this.mechanics = m; return this; }
        public Builder warnings(String w) { this.warnings = w; return this; }
        public Builder addChecklist(String c) { this.checklist.add(c); return this; }
        public Builder addDrop(String d) { this.notableDrops.add(d); return this; }

        public MonsterData build() {
            if (combatProfile == null) {
                combatProfile = MonsterCombatProfile.builder(id, name, primaryStyle).build();
            }
            return new MonsterData(this);
        }
    }
}
