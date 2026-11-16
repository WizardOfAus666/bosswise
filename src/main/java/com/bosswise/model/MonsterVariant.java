package com.bosswise.model;

import java.util.Collections;
import java.util.List;

public class MonsterVariant {
    private final int npcId;
    private final String variantName;
    private final String location;
    private final int combatLevel;
    private final int hitpoints;
    private final int attackLevel;
    private final int strengthLevel;
    private final int defenceLevel;
    private final int magicLevel;
    private final int rangedLevel;

    // Defensive bonuses
    private final int stabDefence;
    private final int slashDefence;
    private final int crushDefence;
    private final int magicDefence;
    private final int rangedDefence;

    // Combat attributes
    private final int attackSpeed;
    private final List<String> attackStyles;
    private final boolean wilderness;
    private final boolean multiCombat;
    private final boolean cannonable;

    public MonsterVariant(
            int npcId,
            String variantName,
            String location,
            int combatLevel,
            int hitpoints,
            int attackLevel,
            int strengthLevel,
            int defenceLevel,
            int magicLevel,
            int rangedLevel,
            int stabDefence,
            int slashDefence,
            int crushDefence,
            int magicDefence,
            int rangedDefence,
            int attackSpeed,
            List<String> attackStyles,
            boolean wilderness,
            boolean multiCombat,
            boolean cannonable) {
        this.npcId = npcId;
        this.variantName = variantName != null ? variantName : "Standard";
        this.location = location != null ? location : "Various";
        this.combatLevel = combatLevel;
        this.hitpoints = hitpoints;
        this.attackLevel = attackLevel;
        this.strengthLevel = strengthLevel;
        this.defenceLevel = defenceLevel;
        this.magicLevel = magicLevel;
        this.rangedLevel = rangedLevel;
        this.stabDefence = stabDefence;
        this.slashDefence = slashDefence;
        this.crushDefence = crushDefence;
        this.magicDefence = magicDefence;
        this.rangedDefence = rangedDefence;
        this.attackSpeed = attackSpeed > 0 ? attackSpeed : 4;
        this.attackStyles = attackStyles != null ? Collections.unmodifiableList(attackStyles) : Collections.emptyList();
        this.wilderness = wilderness;
        this.multiCombat = multiCombat;
        this.cannonable = cannonable;
    }

    public int getNpcId() { return npcId; }
    public String getVariantName() { return variantName; }
    public String getLocation() { return location; }
    public int getCombatLevel() { return combatLevel; }
    public int getHitpoints() { return hitpoints; }
    public int getAttackLevel() { return attackLevel; }
    public int getStrengthLevel() { return strengthLevel; }
    public int getDefenceLevel() { return defenceLevel; }
    public int getMagicLevel() { return magicLevel; }
    public int getRangedLevel() { return rangedLevel; }
    public int getStabDefence() { return stabDefence; }
    public int getSlashDefence() { return slashDefence; }
    public int getCrushDefence() { return crushDefence; }
    public int getMagicDefence() { return magicDefence; }
    public int getRangedDefence() { return rangedDefence; }
    public int getAttackSpeed() { return attackSpeed; }
    public List<String> getAttackStyles() { return attackStyles; }
    public boolean isWilderness() { return wilderness; }
    public boolean isMultiCombat() { return multiCombat; }
    public boolean isCannonable() { return cannonable; }

    public int getDefenceBonusForStyle(CombatStyle style, EquipmentItem.AttackType attackType) {
        if (style == CombatStyle.RANGED) return rangedDefence;
        if (style == CombatStyle.MAGIC) return magicDefence;
        // Melee
        if (attackType == EquipmentItem.AttackType.STAB) return stabDefence;
        if (attackType == EquipmentItem.AttackType.SLASH) return slashDefence;
        if (attackType == EquipmentItem.AttackType.CRUSH) return crushDefence;
        return Math.min(stabDefence, Math.min(slashDefence, crushDefence));
    }
}
