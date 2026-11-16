package com.bosswise.model;

import com.bosswise.model.EquipmentItem.AttackType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class BossCombatProfile {
    private final String bossId;
    private final String bossName;
    private final CombatStyle recommendedStyle;
    private final Set<CombatStyle> allowedStyles;
    private final AttackType favoredAttackType;
    private final boolean dragon;
    private final boolean demon;
    private final boolean undead;
    private final boolean kalphite;
    private final boolean rat;
    private final boolean wilderness;
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
    private final String notes;

    private BossCombatProfile(Builder builder) {
        this.bossId = builder.bossId;
        this.bossName = builder.bossName;
        this.recommendedStyle = builder.recommendedStyle;
        this.allowedStyles = Collections.unmodifiableSet(builder.allowedStyles);
        this.favoredAttackType = builder.favoredAttackType;
        this.dragon = builder.dragon;
        this.demon = builder.demon;
        this.undead = builder.undead;
        this.kalphite = builder.kalphite;
        this.rat = builder.rat;
        this.wilderness = builder.wilderness;
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
        this.notes = builder.notes;
    }

    public static Builder builder(String bossId, String bossName, CombatStyle recommendedStyle) {
        return new Builder(bossId, bossName, recommendedStyle);
    }

    public String getBossId() { return bossId; }
    public String getBossName() { return bossName; }
    public CombatStyle getRecommendedStyle() { return recommendedStyle; }
    public Set<CombatStyle> getAllowedStyles() { return allowedStyles; }
    public AttackType getFavoredAttackType() { return favoredAttackType; }
    public boolean isDragon() { return dragon; }
    public boolean isDemon() { return demon; }
    public boolean isUndead() { return undead; }
    public boolean isKalphite() { return kalphite; }
    public boolean isRat() { return rat; }
    public boolean isWilderness() { return wilderness; }
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
    public String getNotes() { return notes; }

    public boolean isStyleAllowed(CombatStyle style) {
        return allowedStyles.contains(style);
    }

    public MonsterCombatProfile toMonsterCombatProfile() {
        MonsterClassification classification = MonsterClassification.NONE;
        if (dragon) classification = MonsterClassification.DRAGON;
        else if (demon) classification = MonsterClassification.DEMON;
        else if (undead) classification = MonsterClassification.UNDEAD;
        else if (kalphite) classification = MonsterClassification.KALPHITE;
        else if (rat) classification = MonsterClassification.RAT;

        MonsterCombatProfile.Builder b = MonsterCombatProfile.builder(bossId, bossName, recommendedStyle)
                .allowedStyles(allowedStyles)
                .favoredAttackType(favoredAttackType)
                .classification(classification);
        if (requiresDragonfire) b.requiresDragonfire();
        if (requiresWyvernShield) b.requiresWyvernShield();
        if (requiresMirrorShield) b.requiresMirrorShield();
        if (requiresLeafBladed) b.requiresLeafBladed();
        if (requiresRockHammer) b.requiresRockHammer();
        if (requiresBootsOfStone) b.requiresBootsOfStone();
        if (requiresCrush) b.requiresCrush();
        if (requiresSpear) b.requiresSpear();
        if (favorsVenomProtection) b.favorsVenomProtection();
        if (favorsRecoil) b.favorsRecoil();
        if (favorsVoid) b.favorsVoid();
        if (wilderness) b.wilderness();
        b.notes(notes);
        return b.build();
    }

    public static class Builder {
        private final String bossId;
        private final String bossName;
        private final CombatStyle recommendedStyle;
        private Set<CombatStyle> allowedStyles = EnumSet.of(CombatStyle.MELEE, CombatStyle.RANGED, CombatStyle.MAGIC);
        private AttackType favoredAttackType = AttackType.NONE;
        private boolean dragon;
        private boolean demon;
        private boolean undead;
        private boolean kalphite;
        private boolean rat;
        private boolean wilderness;
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
        private String notes = "";

        public Builder(String bossId, String bossName, CombatStyle recommendedStyle) {
            this.bossId = bossId;
            this.bossName = bossName;
            this.recommendedStyle = recommendedStyle;
        }

        public Builder allowedStyles(CombatStyle... styles) {
            this.allowedStyles = EnumSet.noneOf(CombatStyle.class);
            Collections.addAll(this.allowedStyles, styles);
            return this;
        }

        public Builder allowedStylesSet(Set<CombatStyle> styles) {
            if (styles != null && !styles.isEmpty()) {
                this.allowedStyles = EnumSet.copyOf(styles);
            }
            return this;
        }

        public Builder favoredAttackType(AttackType type) {
            this.favoredAttackType = type;
            return this;
        }

        public Builder dragon() { this.dragon = true; return this; }
        public Builder demon() { this.demon = true; return this; }
        public Builder undead() { this.undead = true; return this; }
        public Builder kalphite() { this.kalphite = true; return this; }
        public Builder rat() { this.rat = true; return this; }
        public Builder wilderness() { this.wilderness = true; return this; }
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
        public Builder notes(String notes) { this.notes = notes; return this; }

        public BossCombatProfile build() {
            return new BossCombatProfile(this);
        }
    }
}
