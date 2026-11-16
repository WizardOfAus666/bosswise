package com.bosswise.model.strategy;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.ElementalWeakness;
import com.bosswise.model.MonsterCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonsterStrategy {
    private final String monsterId;
    private final String monsterName;
    private final MonsterCategory category;
    private final int combatLevel;
    private final int hitpoints;
    private final int slayerLevel;
    private final String slayerCategory;
    private final boolean slayerTaskOnly;
    private final List<String> locations;
    private final List<String> accessRequirements;
    private final String overview;
    private final String aggression;
    private final String respawnTime;
    private final boolean multiCombat;
    private final CombatStyle primaryStyle;
    private final List<String> weaknesses;
    private final ElementalWeakness elementalWeakness;
    private final int elementalWeaknessPercent;
    private final List<String> immunities;
    private final String overheadPrayer;
    private final String offensivePrayer;
    private final String prayerNotes;
    private final List<StrategyCombatMechanic> combatMechanics;
    private final List<String> phases;
    private final String safespotInformation;
    private final List<String> specialEquipmentAdvantage;
    private final List<String> protectionAndSurvival;
    private final List<String> commonMistakes;
    private final List<String> notableDrops;
    private final List<String> importantTips;
    private final String wikiUrl;
    private final String wikiStrategyUrl;
    private final String lastVerified;
    private final StrategyConfidence confidence;
    private final List<StrategyMethod> methods;

    private MonsterStrategy(Builder builder) {
        this.monsterId = builder.monsterId;
        this.monsterName = builder.monsterName;
        this.category = builder.category;
        this.combatLevel = builder.combatLevel;
        this.hitpoints = builder.hitpoints;
        this.slayerLevel = builder.slayerLevel;
        this.slayerCategory = builder.slayerCategory;
        this.slayerTaskOnly = builder.slayerTaskOnly;
        this.locations = Collections.unmodifiableList(new ArrayList<>(builder.locations));
        this.accessRequirements = Collections.unmodifiableList(new ArrayList<>(builder.accessRequirements));
        this.overview = builder.overview;
        this.aggression = builder.aggression;
        this.respawnTime = builder.respawnTime;
        this.multiCombat = builder.multiCombat;
        this.primaryStyle = builder.primaryStyle;
        this.weaknesses = Collections.unmodifiableList(new ArrayList<>(builder.weaknesses));
        this.elementalWeakness = builder.elementalWeakness;
        this.elementalWeaknessPercent = builder.elementalWeaknessPercent;
        this.immunities = Collections.unmodifiableList(new ArrayList<>(builder.immunities));
        this.overheadPrayer = builder.overheadPrayer;
        this.offensivePrayer = builder.offensivePrayer;
        this.prayerNotes = builder.prayerNotes;
        this.combatMechanics = Collections.unmodifiableList(new ArrayList<>(builder.combatMechanics));
        this.phases = Collections.unmodifiableList(new ArrayList<>(builder.phases));
        this.safespotInformation = builder.safespotInformation;
        this.specialEquipmentAdvantage = Collections.unmodifiableList(new ArrayList<>(builder.specialEquipmentAdvantage));
        this.protectionAndSurvival = Collections.unmodifiableList(new ArrayList<>(builder.protectionAndSurvival));
        this.commonMistakes = Collections.unmodifiableList(new ArrayList<>(builder.commonMistakes));
        this.notableDrops = Collections.unmodifiableList(new ArrayList<>(builder.notableDrops));
        this.importantTips = Collections.unmodifiableList(new ArrayList<>(builder.importantTips));
        this.wikiUrl = builder.wikiUrl;
        this.wikiStrategyUrl = builder.wikiStrategyUrl;
        this.lastVerified = builder.lastVerified;
        this.confidence = builder.confidence;
        this.methods = Collections.unmodifiableList(new ArrayList<>(builder.methods));
    }

    public String getMonsterId() { return monsterId; }
    public String getMonsterName() { return monsterName; }
    public MonsterCategory getCategory() { return category; }
    public int getCombatLevel() { return combatLevel; }
    public int getHitpoints() { return hitpoints; }
    public int getSlayerLevel() { return slayerLevel; }
    public String getSlayerCategory() { return slayerCategory; }
    public boolean isSlayerTaskOnly() { return slayerTaskOnly; }
    public List<String> getLocations() { return locations; }
    public List<String> getAccessRequirements() { return accessRequirements; }
    public String getOverview() { return overview; }
    public String getAggression() { return aggression; }
    public String getRespawnTime() { return respawnTime; }
    public boolean isMultiCombat() { return multiCombat; }
    public CombatStyle getPrimaryStyle() { return primaryStyle; }
    public List<String> getWeaknesses() { return weaknesses; }
    public ElementalWeakness getElementalWeakness() { return elementalWeakness; }
    public int getElementalWeaknessPercent() { return elementalWeaknessPercent; }
    public List<String> getImmunities() { return immunities; }
    public String getOverheadPrayer() { return overheadPrayer; }
    public String getOffensivePrayer() { return offensivePrayer; }
    public String getPrayerNotes() { return prayerNotes; }
    public List<StrategyCombatMechanic> getCombatMechanics() { return combatMechanics; }
    public List<String> getPhases() { return phases; }
    public String getSafespotInformation() { return safespotInformation; }
    public List<String> getSpecialEquipmentAdvantage() { return specialEquipmentAdvantage; }
    public List<String> getProtectionAndSurvival() { return protectionAndSurvival; }
    public List<String> getCommonMistakes() { return commonMistakes; }
    public List<String> getNotableDrops() { return notableDrops; }
    public List<String> getImportantTips() { return importantTips; }
    public String getWikiUrl() { return wikiUrl; }
    public String getWikiStrategyUrl() { return wikiStrategyUrl; }
    public String getLastVerified() { return lastVerified; }
    public StrategyConfidence getConfidence() { return confidence; }
    public List<StrategyMethod> getMethods() { return methods; }

    public StrategyMethod getMethodByTier(StrategyTier tier) {
        for (StrategyMethod m : methods) {
            if (m.getTier() == tier) return m;
        }
        return methods.isEmpty() ? null : methods.get(0);
    }

    public static Builder builder(String id, String name, MonsterCategory category, CombatStyle style) {
        return new Builder(id, name, category, style);
    }

    public static class Builder {
        private final String monsterId;
        private final String monsterName;
        private final MonsterCategory category;
        private final CombatStyle primaryStyle;
        private int combatLevel = 50;
        private int hitpoints = 100;
        private int slayerLevel = 1;
        private String slayerCategory = "";
        private boolean slayerTaskOnly = false;
        private final List<String> locations = new ArrayList<>();
        private final List<String> accessRequirements = new ArrayList<>();
        private String overview = "";
        private String aggression = "Non-aggressive";
        private String respawnTime = "Normal (30s)";
        private boolean multiCombat = false;
        private final List<String> weaknesses = new ArrayList<>();
        private ElementalWeakness elementalWeakness = ElementalWeakness.NONE;
        private int elementalWeaknessPercent = 0;
        private final List<String> immunities = new ArrayList<>();
        private String overheadPrayer = "None";
        private String offensivePrayer = "None";
        private String prayerNotes = "";
        private final List<StrategyCombatMechanic> combatMechanics = new ArrayList<>();
        private final List<String> phases = new ArrayList<>();
        private String safespotInformation = "";
        private final List<String> specialEquipmentAdvantage = new ArrayList<>();
        private final List<String> protectionAndSurvival = new ArrayList<>();
        private final List<String> commonMistakes = new ArrayList<>();
        private final List<String> notableDrops = new ArrayList<>();
        private final List<String> importantTips = new ArrayList<>();
        private String wikiUrl = "";
        private String wikiStrategyUrl = "";
        private String lastVerified = "2026-11-16";
        private StrategyConfidence confidence = StrategyConfidence.VERIFIED;
        private final List<StrategyMethod> methods = new ArrayList<>();

        public Builder(String id, String name, MonsterCategory category, CombatStyle style) {
            this.monsterId = id;
            this.monsterName = name;
            this.category = category;
            this.primaryStyle = style;
        }

        public Builder combatLevel(int level) { this.combatLevel = level; return this; }
        public Builder hitpoints(int hp) { this.hitpoints = hp; return this; }
        public Builder slayer(int level, String category, boolean taskOnly) {
            this.slayerLevel = level;
            this.slayerCategory = category;
            this.slayerTaskOnly = taskOnly;
            return this;
        }
        public Builder addLocation(String loc) { this.locations.add(loc); return this; }
        public Builder addAccessRequirement(String req) { this.accessRequirements.add(req); return this; }
        public Builder overview(String ov) { this.overview = ov; return this; }
        public Builder aggression(String agg) { this.aggression = agg; return this; }
        public Builder respawnTime(String rt) { this.respawnTime = rt; return this; }
        public Builder multiCombat(boolean multi) { this.multiCombat = multi; return this; }
        public Builder addWeakness(String w) { this.weaknesses.add(w); return this; }
        public Builder elementalWeakness(ElementalWeakness ew, int pct) {
            this.elementalWeakness = ew;
            this.elementalWeaknessPercent = pct;
            return this;
        }
        public Builder addImmunity(String imm) { this.immunities.add(imm); return this; }
        public Builder prayers(String overhead, String offensive, String notes) {
            this.overheadPrayer = overhead;
            this.offensivePrayer = offensive;
            this.prayerNotes = notes;
            return this;
        }
        public Builder addMechanic(StrategyCombatMechanic mech) { this.combatMechanics.add(mech); return this; }
        public Builder addMechanic(String name, String desc, String tip) {
            this.combatMechanics.add(new StrategyCombatMechanic(name, desc, tip));
            return this;
        }
        public Builder addPhase(String phase) { this.phases.add(phase); return this; }
        public Builder safespot(String info) { this.safespotInformation = info; return this; }
        public Builder addSpecialEquipment(String eq) { this.specialEquipmentAdvantage.add(eq); return this; }
        public Builder addProtection(String prot) { this.protectionAndSurvival.add(prot); return this; }
        public Builder addCommonMistake(String mistake) { this.commonMistakes.add(mistake); return this; }
        public Builder addDrop(String drop) { this.notableDrops.add(drop); return this; }
        public Builder addTip(String tip) { this.importantTips.add(tip); return this; }
        public Builder wikiUrls(String mainUrl, String strategyUrl) {
            this.wikiUrl = mainUrl;
            this.wikiStrategyUrl = strategyUrl;
            return this;
        }
        public Builder lastVerified(String date) { this.lastVerified = date; return this; }
        public Builder confidence(StrategyConfidence conf) { this.confidence = conf; return this; }
        public Builder addMethod(StrategyMethod method) { this.methods.add(method); return this; }

        public MonsterStrategy build() {
            return new MonsterStrategy(this);
        }
    }
}
