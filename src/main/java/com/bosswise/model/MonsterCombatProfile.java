package com.bosswise.model;

import com.bosswise.model.EquipmentItem.AttackType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class MonsterCombatProfile {
    private final String monsterId;
    private final String monsterName;
    private final CombatStyle recommendedStyle;
    private final Set<CombatStyle> allowedStyles;
    private final AttackType favoredAttackType;
    private final MonsterClassification classification;
    private final ElementalWeakness elementalWeakness;
    private final int elementalWeaknessPercent;

    // Immunities
    private final boolean poisonImmune;
    private final boolean venomImmune;
    private final boolean cannonImmune;
    private final boolean thrallImmune;

    // Specific equipment / protection requirements
    private final boolean requiresDragonfire;
    private final boolean requiresWyvernShield;
    private final boolean requiresMirrorShield;
    private final boolean requiresLeafBladed;
    private final boolean requiresRockHammer;
    private final boolean requiresBootsOfStone;
    private final boolean requiresCrush;
    private final boolean requiresSpear;
    private final boolean favorsVenomProtection;
    private final boolean favorsRecoil;
    private final boolean favorsVoid;
    private final boolean wilderness;

    // Slayer info
    private final String slayerCategory;
    private final int requiredSlayerLevel;
    private final boolean slayerTaskOnly;

    // Data Provenance
    private final String dataConfidence; // "HIGH", "MEDIUM", "LOW"
    private final String lastVerified;
    private final String notes;

    private MonsterCombatProfile(Builder builder) {
        this.monsterId = builder.monsterId;
        this.monsterName = builder.monsterName;
        this.recommendedStyle = builder.recommendedStyle;
        this.allowedStyles = Collections.unmodifiableSet(builder.allowedStyles);
        this.favoredAttackType = builder.favoredAttackType;
        this.classification = builder.classification;
        this.elementalWeakness = builder.elementalWeakness;
        this.elementalWeaknessPercent = builder.elementalWeaknessPercent;

        this.poisonImmune = builder.poisonImmune;
        this.venomImmune = builder.venomImmune;
        this.cannonImmune = builder.cannonImmune;
        this.thrallImmune = builder.thrallImmune;

        this.requiresDragonfire = builder.requiresDragonfire;
        this.requiresWyvernShield = builder.requiresWyvernShield;
        this.requiresMirrorShield = builder.requiresMirrorShield;
        this.requiresLeafBladed = builder.requiresLeafBladed;
        this.requiresRockHammer = builder.requiresRockHammer;
        this.requiresBootsOfStone = builder.requiresBootsOfStone;
        this.requiresCrush = builder.requiresCrush;
        this.requiresSpear = builder.requiresSpear;
        this.favorsVenomProtection = builder.favorsVenomProtection;
        this.favorsRecoil = builder.favorsRecoil;
        this.favorsVoid = builder.favorsVoid;
        this.wilderness = builder.wilderness;

        this.slayerCategory = builder.slayerCategory;
        this.requiredSlayerLevel = builder.requiredSlayerLevel;
        this.slayerTaskOnly = builder.slayerTaskOnly;

        this.dataConfidence = builder.dataConfidence;
        this.lastVerified = builder.lastVerified;
        this.notes = builder.notes;
    }

    public static Builder builder(String monsterId, String monsterName, CombatStyle recommendedStyle) {
        return new Builder(monsterId, monsterName, recommendedStyle);
    }

    public String getMonsterId() { return monsterId; }
    public String getMonsterName() { return monsterName; }
    public CombatStyle getRecommendedStyle() { return recommendedStyle; }
    public Set<CombatStyle> getAllowedStyles() { return allowedStyles; }
    public AttackType getFavoredAttackType() { return favoredAttackType; }
    public MonsterClassification getClassification() { return classification; }
    public ElementalWeakness getElementalWeakness() { return elementalWeakness; }
    public int getElementalWeaknessPercent() { return elementalWeaknessPercent; }

    public boolean isPoisonImmune() { return poisonImmune; }
    public boolean isVenomImmune() { return venomImmune; }
    public boolean isCannonImmune() { return cannonImmune; }
    public boolean isThrallImmune() { return thrallImmune; }

    public boolean isRequiresDragonfire() { return requiresDragonfire; }
    public boolean isRequiresWyvernShield() { return requiresWyvernShield; }
    public boolean isRequiresMirrorShield() { return requiresMirrorShield; }
    public boolean isRequiresLeafBladed() { return requiresLeafBladed; }
    public boolean isRequiresRockHammer() { return requiresRockHammer; }
    public boolean isRequiresBootsOfStone() { return requiresBootsOfStone; }
    public boolean isRequiresCrush() { return requiresCrush; }
    public boolean isRequiresSpear() { return requiresSpear; }
    public boolean isFavorsVenomProtection() { return favorsVenomProtection; }
    public boolean isFavorsRecoil() { return favorsRecoil; }
    public boolean isFavorsVoid() { return favorsVoid; }
    public boolean isWilderness() { return wilderness; }

    public boolean isDragon() { return classification == MonsterClassification.DRAGON; }
    public boolean isDemon() { return classification == MonsterClassification.DEMON; }
    public boolean isUndead() { return classification == MonsterClassification.UNDEAD; }
    public boolean isKalphite() { return classification == MonsterClassification.KALPHITE; }
    public boolean isRat() { return classification == MonsterClassification.RAT; }
    public boolean isGiant() { return classification == MonsterClassification.GIANT; }
    public boolean isVampyre() { return classification == MonsterClassification.VAMPYRE; }

    public String getSlayerCategory() { return slayerCategory; }
    public int getRequiredSlayerLevel() { return requiredSlayerLevel; }
    public boolean isSlayerTaskOnly() { return slayerTaskOnly; }

    public String getDataConfidence() { return dataConfidence; }
    public String getLastVerified() { return lastVerified; }
    public String getNotes() { return notes; }

    public String getOverheadPrayer() {
        if (notes != null && notes.contains("Protect from")) {
            int idx = notes.indexOf("Protect from");
            int end = notes.indexOf(".", idx);
            if (end == -1) end = notes.indexOf(";", idx);
            if (end == -1) end = notes.length();
            return notes.substring(idx, Math.min(idx + 24, end)).trim();
        }
        return "Protect from Melee";
    }

    public boolean isStyleAllowed(CombatStyle style) {
        return allowedStyles.contains(style);
    }

    public static class Builder {
        private final String monsterId;
        private final String monsterName;
        private final CombatStyle recommendedStyle;
        private Set<CombatStyle> allowedStyles = EnumSet.of(CombatStyle.MELEE, CombatStyle.RANGED, CombatStyle.MAGIC);
        private AttackType favoredAttackType = AttackType.NONE;
        private MonsterClassification classification = MonsterClassification.NONE;
        private ElementalWeakness elementalWeakness = ElementalWeakness.NONE;
        private int elementalWeaknessPercent = 0;

        private boolean poisonImmune;
        private boolean venomImmune;
        private boolean cannonImmune;
        private boolean thrallImmune;

        private boolean requiresDragonfire;
        private boolean requiresWyvernShield;
        private boolean requiresMirrorShield;
        private boolean requiresLeafBladed;
        private boolean requiresRockHammer;
        private boolean requiresBootsOfStone;
        private boolean requiresCrush;
        private boolean requiresSpear;
        private boolean favorsVenomProtection;
        private boolean favorsRecoil;
        private boolean favorsVoid;
        private boolean wilderness;

        private String slayerCategory = "";
        private int requiredSlayerLevel = 1;
        private boolean slayerTaskOnly = false;

        private String dataConfidence = "HIGH";
        private String lastVerified = "2026-11";
        private String notes = "";

        public Builder(String monsterId, String monsterName, CombatStyle recommendedStyle) {
            this.monsterId = monsterId;
            this.monsterName = monsterName;
            this.recommendedStyle = recommendedStyle;
        }

        public Builder allowedStyles(CombatStyle... styles) {
            this.allowedStyles = EnumSet.noneOf(CombatStyle.class);
            Collections.addAll(this.allowedStyles, styles);
            return this;
        }

        public Builder allowedStyles(Set<CombatStyle> styles) {
            if (styles != null && !styles.isEmpty()) {
                this.allowedStyles = EnumSet.copyOf(styles);
            }
            return this;
        }

        public Builder favoredAttackType(AttackType type) { this.favoredAttackType = type; return this; }
        public Builder classification(MonsterClassification c) { this.classification = c; return this; }
        public Builder dragon() { this.classification = MonsterClassification.DRAGON; return this; }
        public Builder demon() { this.classification = MonsterClassification.DEMON; return this; }
        public Builder undead() { this.classification = MonsterClassification.UNDEAD; return this; }
        public Builder kalphite() { this.classification = MonsterClassification.KALPHITE; return this; }
        public Builder rat() { this.classification = MonsterClassification.RAT; return this; }
        public Builder giant() { this.classification = MonsterClassification.GIANT; return this; }
        public Builder vampyre() { this.classification = MonsterClassification.VAMPYRE; return this; }

        public Builder elementalWeakness(ElementalWeakness weakness, int percent) {
            this.elementalWeakness = weakness;
            this.elementalWeaknessPercent = percent;
            return this;
        }

        public Builder poisonImmune() { this.poisonImmune = true; return this; }
        public Builder venomImmune() { this.venomImmune = true; return this; }
        public Builder cannonImmune() { this.cannonImmune = true; return this; }
        public Builder thrallImmune() { this.thrallImmune = true; return this; }

        public Builder requiresDragonfire() { this.requiresDragonfire = true; return this; }
        public Builder requiresWyvernShield() { this.requiresWyvernShield = true; return this; }
        public Builder requiresMirrorShield() { this.requiresMirrorShield = true; return this; }
        public Builder requiresLeafBladed() { this.requiresLeafBladed = true; return this; }
        public Builder requiresRockHammer() { this.requiresRockHammer = true; return this; }
        public Builder requiresBootsOfStone() { this.requiresBootsOfStone = true; return this; }
        public Builder requiresCrush() { this.requiresCrush = true; return this; }
        public Builder requiresSpear() { this.requiresSpear = true; return this; }
        public Builder favorsVenomProtection() { this.favorsVenomProtection = true; return this; }
        public Builder favorsRecoil() { this.favorsRecoil = true; return this; }
        public Builder favorsVoid() { this.favorsVoid = true; return this; }
        public Builder wilderness() { this.wilderness = true; return this; }

        public Builder slayer(String category, int level, boolean taskOnly) {
            this.slayerCategory = category;
            this.requiredSlayerLevel = level;
            this.slayerTaskOnly = taskOnly;
            return this;
        }

        public Builder confidence(String confidence) { this.dataConfidence = confidence; return this; }
        public Builder lastVerified(String date) { this.lastVerified = date; return this; }
        public Builder notes(String notes) { this.notes = notes; return this; }

        public MonsterCombatProfile build() {
            return new MonsterCombatProfile(this);
        }
    }
}
