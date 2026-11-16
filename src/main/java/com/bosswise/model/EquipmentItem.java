package com.bosswise.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Skill;

public class EquipmentItem {
    public enum AmmoType {
        NONE,
        BOLT,
        ARROW,
        DART,
        BLESSING
    }

    public enum AttackType {
        NONE,
        STAB,
        SLASH,
        CRUSH,
        RANGED,
        MAGIC
    }

    private final int itemId;
    private final String name;
    private final EquipmentSlot slot;
    private final CombatStyle primaryStyle;
    private final Map<Skill, Integer> skillRequirements;
    private final String questRequirement;
    private final int defaultPrice;
    private final boolean untradeable;
    private final boolean twoHanded;

    // Stat bonuses
    private final int meleeStrength;
    private final int rangedStrength;
    private final int magicDamagePercent;
    private final int stabAttack;
    private final int slashAttack;
    private final int crushAttack;
    private final int magicAttack;
    private final int rangedAttack;
    private final int stabDefence;
    private final int slashDefence;
    private final int crushDefence;
    private final int magicDefence;
    private final int rangedDefence;
    private final int prayerBonus;

    // Weapon & Boss Mechanic attributes
    private final int attackSpeed; // Game ticks (e.g. 2 for blowpipe, 4 for whip/trident, 5 for godsword)
    private final AttackType attackType;
    private final AmmoType ammoType;
    private final boolean dragonbane;
    private final boolean demonbane;
    private final boolean ratbane;
    private final boolean undeadBonus;
    private final boolean slayerTaskBonus;
    private final boolean dragonfireProtection;
    private final boolean venomProtection;
    private final boolean wildernessBonus;
    private final boolean spear;
    private final boolean mirrorShield;
    private final boolean leafBladed;
    private final boolean bootsOfStone;
    private final boolean voidKnight;
    private final boolean kalphitebane;

    // Special attack metadata
    private final boolean hasSpecialAttack;
    private final int specEnergyCost;
    private final String specEffect;

    // Wiki & Verification metadata
    private final String source;
    private final String verifiedDate;
    private final String weaponCategory;

    private final String specialEffect;
    private final int tier; // 1 to 10 power rating

    private EquipmentItem(Builder builder) {
        this.itemId = builder.itemId;
        this.name = builder.name;
        this.slot = builder.slot;
        this.primaryStyle = builder.primaryStyle;
        this.skillRequirements = Collections.unmodifiableMap(builder.skillRequirements);
        this.questRequirement = builder.questRequirement;
        this.defaultPrice = builder.defaultPrice;
        this.untradeable = builder.untradeable;
        this.twoHanded = builder.twoHanded;
        this.meleeStrength = builder.meleeStrength;
        this.rangedStrength = builder.rangedStrength;
        this.magicDamagePercent = builder.magicDamagePercent;
        this.stabAttack = builder.stabAttack;
        this.slashAttack = builder.slashAttack;
        this.crushAttack = builder.crushAttack;
        this.magicAttack = builder.magicAttack;
        this.rangedAttack = builder.rangedAttack;
        this.stabDefence = builder.stabDefence;
        this.slashDefence = builder.slashDefence;
        this.crushDefence = builder.crushDefence;
        this.magicDefence = builder.magicDefence;
        this.rangedDefence = builder.rangedDefence;
        this.prayerBonus = builder.prayerBonus;

        this.attackSpeed = builder.attackSpeed;
        this.attackType = builder.attackType;
        this.ammoType = builder.ammoType;
        this.dragonbane = builder.dragonbane;
        this.demonbane = builder.demonbane;
        this.ratbane = builder.ratbane;
        this.undeadBonus = builder.undeadBonus;
        this.slayerTaskBonus = builder.slayerTaskBonus;
        this.dragonfireProtection = builder.dragonfireProtection;
        this.venomProtection = builder.venomProtection;
        this.wildernessBonus = builder.wildernessBonus;
        this.spear = builder.spear;
        this.mirrorShield = builder.mirrorShield;
        this.leafBladed = builder.leafBladed;
        this.bootsOfStone = builder.bootsOfStone;
        this.voidKnight = builder.voidKnight;
        this.kalphitebane = builder.kalphitebane;

        this.hasSpecialAttack = builder.hasSpecialAttack;
        this.specEnergyCost = builder.specEnergyCost;
        this.specEffect = builder.specEffect != null ? builder.specEffect : "";

        this.source = builder.source != null ? builder.source : "OSRS Wiki";
        this.verifiedDate = builder.verifiedDate != null ? builder.verifiedDate : "2026-11-16";
        this.weaponCategory = builder.weaponCategory != null ? builder.weaponCategory : "";

        this.specialEffect = builder.specialEffect;
        this.tier = builder.tier;
    }

    public static Builder builder(int itemId, String name, EquipmentSlot slot, CombatStyle primaryStyle) {
        return new Builder(itemId, name, slot, primaryStyle);
    }

    public int getItemId() { return itemId; }
    public String getName() { return name; }
    public EquipmentSlot getSlot() { return slot; }
    public CombatStyle getPrimaryStyle() { return primaryStyle; }
    public Map<Skill, Integer> getSkillRequirements() { return skillRequirements; }
    public String getQuestRequirement() { return questRequirement; }
    public int getDefaultPrice() { return defaultPrice; }
    public boolean isUntradeable() { return untradeable; }
    public boolean isTwoHanded() { return twoHanded; }
    public int getMeleeStrength() { return meleeStrength; }
    public int getRangedStrength() { return rangedStrength; }
    public int getMagicDamagePercent() { return magicDamagePercent; }
    public int getStabAttack() { return stabAttack; }
    public int getSlashAttack() { return slashAttack; }
    public int getCrushAttack() { return crushAttack; }
    public int getMagicAttack() { return magicAttack; }
    public int getRangedAttack() { return rangedAttack; }
    public int getStabDefence() { return stabDefence; }
    public int getSlashDefence() { return slashDefence; }
    public int getCrushDefence() { return crushDefence; }
    public int getMagicDefence() { return magicDefence; }
    public int getRangedDefence() { return rangedDefence; }
    public int getPrayerBonus() { return prayerBonus; }

    public int getAttackSpeed() { return attackSpeed; }
    public AttackType getAttackType() { return attackType; }
    public AmmoType getAmmoType() { return ammoType; }
    public boolean isDragonbane() { return dragonbane; }
    public boolean isDemonbane() { return demonbane; }
    public boolean isRatbane() { return ratbane; }
    public boolean isUndeadBonus() { return undeadBonus; }
    public boolean isSlayerTaskBonus() { return slayerTaskBonus; }
    public boolean isDragonfireProtection() { return dragonfireProtection; }
    public boolean isVenomProtection() { return venomProtection; }
    public boolean isWildernessBonus() { return wildernessBonus; }
    public boolean isSpear() { return spear; }
    public boolean isMirrorShield() { return mirrorShield; }
    public boolean isLeafBladed() { return leafBladed; }
    public boolean isBootsOfStone() { return bootsOfStone; }
    public boolean isVoidKnight() { return voidKnight; }
    public boolean isKalphitebane() { return kalphitebane; }

    public boolean hasSpecialAttack() { return hasSpecialAttack; }
    public int getSpecEnergyCost() { return specEnergyCost; }
    public String getSpecEffect() { return specEffect; }

    public String getSource() { return source; }
    public String getVerifiedDate() { return verifiedDate; }
    public String getWeaponCategory() { return weaponCategory; }
    public boolean isTradeable() { return !untradeable; }

    public String getSpecialEffect() { return specialEffect; }
    public int getTier() { return tier; }
    public long getPrice() { return defaultPrice; }

    public static EquipmentItem createSimple(int itemId, String name, EquipmentSlot slot, long price, int tier, CombatStyle style) {
        return EquipmentItem.builder(itemId, name, slot, style != null ? style : CombatStyle.MELEE)
                .price((int) Math.min(Integer.MAX_VALUE, price))
                .tier(tier)
                .build();
    }

    public int getStyleOffenceBonus(CombatStyle style) {
        switch (style) {
            case MELEE:
                return Math.max(slashAttack, Math.max(stabAttack, crushAttack));
            case RANGED:
                return rangedAttack;
            case MAGIC:
                return magicAttack;
            default:
                return Math.max(meleeStrength, Math.max(rangedAttack, magicAttack));
        }
    }

    public int getAverageDefenceBonus() {
        return (stabDefence + slashDefence + crushDefence + rangedDefence + magicDefence) / 5;
    }

    public static class Builder {
        private final int itemId;
        private final String name;
        private final EquipmentSlot slot;
        private final CombatStyle primaryStyle;
        private final Map<Skill, Integer> skillRequirements = new HashMap<>();
        private String questRequirement;
        private int defaultPrice = 0;
        private boolean untradeable = false;
        private boolean twoHanded = false;
        private int meleeStrength = 0;
        private int rangedStrength = 0;
        private int magicDamagePercent = 0;
        private int stabAttack = 0;
        private int slashAttack = 0;
        private int crushAttack = 0;
        private int magicAttack = 0;
        private int rangedAttack = 0;
        private int stabDefence = 0;
        private int slashDefence = 0;
        private int crushDefence = 0;
        private int magicDefence = 0;
        private int rangedDefence = 0;
        private int prayerBonus = 0;

        private int attackSpeed = 4;
        private AttackType attackType = AttackType.NONE;
        private AmmoType ammoType = AmmoType.NONE;
        private boolean dragonbane = false;
        private boolean demonbane = false;
        private boolean ratbane = false;
        private boolean undeadBonus = false;
        private boolean slayerTaskBonus = false;
        private boolean dragonfireProtection = false;
        private boolean venomProtection = false;
        private boolean wildernessBonus = false;
        private boolean spear = false;
        private boolean mirrorShield = false;
        private boolean leafBladed = false;
        private boolean bootsOfStone = false;
        private boolean voidKnight = false;
        private boolean kalphitebane = false;

        private boolean hasSpecialAttack = false;
        private int specEnergyCost = 0;
        private String specEffect = "";

        private String source = "OSRS Wiki";
        private String verifiedDate = "2026-11-16";
        private String weaponCategory = "";

        private String specialEffect = "";
        private int tier = 1;

        public Builder(int itemId, String name, EquipmentSlot slot, CombatStyle primaryStyle) {
            this.itemId = itemId;
            this.name = name;
            this.slot = slot;
            this.primaryStyle = primaryStyle;
        }

        public Builder req(Skill skill, int level) {
            this.skillRequirements.put(skill, level);
            return this;
        }

        public Builder questReq(String quest) {
            this.questRequirement = quest;
            return this;
        }

        public Builder price(int price) {
            this.defaultPrice = price;
            return this;
        }

        public Builder untradeable() {
            this.untradeable = true;
            return this;
        }

        public Builder twoHanded() {
            this.twoHanded = true;
            return this;
        }

        public Builder str(int meleeStr, int rangedStr, int magicDmgPct) {
            this.meleeStrength = meleeStr;
            this.rangedStrength = rangedStr;
            this.magicDamagePercent = magicDmgPct;
            return this;
        }

        public Builder atk(int stab, int slash, int crush, int magic, int range) {
            this.stabAttack = stab;
            this.slashAttack = slash;
            this.crushAttack = crush;
            this.magicAttack = magic;
            this.rangedAttack = range;
            return this;
        }

        public Builder def(int stab, int slash, int crush, int magic, int range) {
            this.stabDefence = stab;
            this.slashDefence = slash;
            this.crushDefence = crush;
            this.magicDefence = magic;
            this.rangedDefence = range;
            return this;
        }

        public Builder prayer(int prayer) {
            this.prayerBonus = prayer;
            return this;
        }

        public Builder speed(int ticks) {
            this.attackSpeed = ticks;
            return this;
        }

        public Builder attackType(AttackType type) {
            this.attackType = type;
            return this;
        }

        public Builder ammoType(AmmoType type) {
            this.ammoType = type;
            return this;
        }

        public Builder dragonbane() {
            this.dragonbane = true;
            return this;
        }

        public Builder demonbane() {
            this.demonbane = true;
            return this;
        }

        public Builder ratbane() {
            this.ratbane = true;
            return this;
        }

        public Builder undeadBonus() {
            this.undeadBonus = true;
            return this;
        }

        public Builder slayerTaskBonus() {
            this.slayerTaskBonus = true;
            return this;
        }

        public Builder dragonfireProtection() {
            this.dragonfireProtection = true;
            return this;
        }

        public Builder venomProtection() {
            this.venomProtection = true;
            return this;
        }

        public Builder wildernessBonus() {
            this.wildernessBonus = true;
            return this;
        }

        public Builder spear() {
            this.spear = true;
            return this;
        }

        public Builder mirrorShield() {
            this.mirrorShield = true;
            return this;
        }

        public Builder leafBladed() {
            this.leafBladed = true;
            return this;
        }

        public Builder bootsOfStone() {
            this.bootsOfStone = true;
            return this;
        }

        public Builder voidKnight() {
            this.voidKnight = true;
            return this;
        }

        public Builder kalphitebane() {
            this.kalphitebane = true;
            return this;
        }

        public Builder specialAttack(int energy, String effect) {
            this.hasSpecialAttack = true;
            this.specEnergyCost = energy;
            this.specEffect = effect;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder verifiedDate(String date) {
            this.verifiedDate = date;
            return this;
        }

        public Builder weaponCategory(String category) {
            this.weaponCategory = category;
            return this;
        }

        public Builder effect(String effect) {
            this.specialEffect = effect;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public EquipmentItem build() {
            return new EquipmentItem(this);
        }
    }
}
