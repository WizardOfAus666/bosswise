package com.bosswise.service;

import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.ElementalWeakness;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.PlayerAccount;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class CombatCalculationService {
    private final SpecialEffectService specialEffectService;

    public CombatCalculationService() {
        this(new SpecialEffectService());
    }

    @Inject
    public CombatCalculationService(SpecialEffectService specialEffectService) {
        this.specialEffectService = specialEffectService != null ? specialEffectService : new SpecialEffectService();
    }

    public CombatResult calculateCombat(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            CombatStyle requestedStyle,
            boolean onSlayerTask) {
        return calculateCombat(equipment, monster, profile, account, requestedStyle, onSlayerTask, null, null);
    }

    public CombatResult calculateCombat(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            CombatStyle requestedStyle,
            boolean onSlayerTask,
            String prayerOverride,
            String potionOverride) {

        if (requestedStyle == CombatStyle.RANGED) {
            return calculateRanged(equipment, monster, profile, account, onSlayerTask, prayerOverride, potionOverride);
        } else if (requestedStyle == CombatStyle.MAGIC) {
            return calculateMagic(equipment, monster, profile, account, onSlayerTask, prayerOverride, potionOverride);
        } else {
            return calculateMelee(equipment, monster, profile, account, onSlayerTask, prayerOverride, potionOverride);
        }
    }

    // =========================================================================
    // 1. MELEE COMBAT CALCULATIONS
    // =========================================================================

    public CombatResult calculateMelee(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask) {
        return calculateMelee(equipment, monster, profile, account, onSlayerTask, null, null);
    }

    public CombatResult calculateMelee(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask,
            String prayerOverride,
            String potionOverride) {

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        int weaponTicks = (weapon != null) ? weapon.getAttackSpeed() : 4;
        if (weaponTicks <= 0) weaponTicks = 4;

        AttackType attackType = (weapon != null) ? weapon.getAttackType() : AttackType.SLASH;
        if (attackType == AttackType.NONE || attackType == AttackType.RANGED || attackType == AttackType.MAGIC) {
            attackType = AttackType.SLASH;
        }

        // 1. Aggregate Gear Stats
        int gearAtkBonus = 0;
        int gearStrBonus = 0;
        for (EquipmentItem item : equipment.values()) {
            if (item == null) continue;
            gearStrBonus += item.getMeleeStrength();
            if (attackType == AttackType.STAB) gearAtkBonus += item.getStabAttack();
            else if (attackType == AttackType.CRUSH) gearAtkBonus += item.getCrushAttack();
            else gearAtkBonus += item.getSlashAttack();
        }

        // 2. Player Effective Levels
        int baseAtk = account != null ? account.getRealLevel(Skill.ATTACK) : 80;
        int baseStr = account != null ? account.getRealLevel(Skill.STRENGTH) : 85;
        int prayerLvl = account != null ? account.getRealLevel(Skill.PRAYER) : 70;

        // Potions
        int potAtkBoost;
        int potStrBoost;
        if ("None".equalsIgnoreCase(potionOverride)) {
            potAtkBoost = 0;
            potStrBoost = 0;
        } else if ("Regular".equalsIgnoreCase(potionOverride)) {
            potAtkBoost = (int) (3 + (baseAtk * 0.10));
            potStrBoost = (int) (3 + (baseStr * 0.10));
        } else if ("Overload".equalsIgnoreCase(potionOverride)) {
            potAtkBoost = (int) (6 + (baseAtk * 0.16));
            potStrBoost = (int) (6 + (baseStr * 0.16));
        } else {
            potAtkBoost = (int) (5 + (baseAtk * 0.15));
            potStrBoost = (int) (5 + (baseStr * 0.15));
        }

        // Prayer Multiplier
        double prayAtkMult;
        double prayStrMult;
        if ("None".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.0;
            prayStrMult = 1.0;
        } else if ("15%".equalsIgnoreCase(prayerOverride) || "Incredible Reflexes".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.15;
            prayStrMult = 1.15;
        } else if ("Chivalry".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.15;
            prayStrMult = 1.18;
        } else if ("Piety".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.20;
            prayStrMult = 1.23;
        } else {
            prayAtkMult = (prayerLvl >= 70) ? 1.20 : (prayerLvl >= 60 ? 1.15 : (prayerLvl >= 31 ? 1.15 : 1.0)); // Piety / Chivalry / Incredible Reflexes
            prayStrMult = (prayerLvl >= 70) ? 1.23 : (prayerLvl >= 60 ? 1.18 : (prayerLvl >= 31 ? 1.15 : 1.0)); // Piety / Chivalry / Ultimate Strength
        }

        int styleBonus = 3; // Aggressive / Accurate stance

        int effectiveAtk = (int) Math.floor((baseAtk + potAtkBoost) * prayAtkMult) + styleBonus + 8;
        int effectiveStr = (int) Math.floor((baseStr + potStrBoost) * prayStrMult) + styleBonus + 8;

        // 3. Max Attack Roll
        SpecialEffectService.CombatMultipliers multipliers = specialEffectService.getMeleeMultipliers(equipment, profile, onSlayerTask);
        double atkGearMult = multipliers.getAccuracyMultiplier();
        double strGearMult = multipliers.getDamageMultiplier();

        long maxAtkRoll = (long) Math.floor((effectiveAtk * (gearAtkBonus + 64)) * atkGearMult);

        // 4. Monster Defence Roll
        int monsterDefLvl = (monster != null) ? monster.getDefenceLevel() : 100;
        int monsterSpecificDef = (monster != null) ? monster.getDefenceBonusForStyle(CombatStyle.MELEE, attackType) : 0;

        long maxDefRoll = (long) (monsterDefLvl + 9) * (monsterSpecificDef + 64);

        // 5. Hit Chance
        double hitChance = computeHitChance(maxAtkRoll, maxDefRoll);

        // Osmumten's fang double accuracy roll
        boolean isFang = weapon != null && weapon.getName().toLowerCase().contains("fang");
        if (isFang) {
            hitChance = 1.0 - Math.pow(1.0 - hitChance, 2.0);
        }

        // 6. Max Hit
        int baseMaxHit = (int) Math.floor(0.5 + effectiveStr * (gearStrBonus + 64) / 640.0);
        int maxHit = (int) Math.floor(baseMaxHit * strGearMult);

        // Special monster adjustments
        if (profile != null) {
            if (profile.isRequiresSpear() && (weapon == null || (!weapon.isSpear() && !isFang))) {
                maxHit = (int) Math.floor(maxHit * 0.50); // 50% Corporeal Beast damage reduction
            }
        }
        maxHit += multipliers.getFlatMaxHitBonus();

        // 7. Expected Damage & DPS
        double expectedDmg = hitChance * (maxHit / 2.0);
        double dps = expectedDmg / (weaponTicks * 0.6);

        String details = String.format("Melee (%s): Hit %s, Max Hit %d, DPS %.2f",
                attackType.name(), String.format("%.1f%%", hitChance * 100), maxHit, dps);

        return new CombatResult(CombatStyle.MELEE, attackType, hitChance, maxHit, expectedDmg, weaponTicks, dps, maxAtkRoll, maxDefRoll, details);
    }

    // =========================================================================
    // 2. RANGED COMBAT CALCULATIONS
    // =========================================================================

    public CombatResult calculateRanged(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask) {
        return calculateRanged(equipment, monster, profile, account, onSlayerTask, null, null);
    }

    public CombatResult calculateRanged(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask,
            String prayerOverride,
            String potionOverride) {

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        int weaponTicks = (weapon != null) ? weapon.getAttackSpeed() : 4;
        if (weaponTicks <= 0) weaponTicks = 4;

        // 1. Aggregate Gear Stats
        int gearRangedAtk = 0;
        int gearRangedStr = 0;
        for (EquipmentItem item : equipment.values()) {
            if (item == null) continue;
            gearRangedAtk += item.getRangedAttack();
            gearRangedStr += item.getRangedStrength();
        }

        int baseRng = account != null ? account.getRealLevel(Skill.RANGED) : 82;
        int prayerLvl = account != null ? account.getRealLevel(Skill.PRAYER) : 70;

        int potRngBoost;
        if ("None".equalsIgnoreCase(potionOverride)) {
            potRngBoost = 0;
        } else if ("Regular".equalsIgnoreCase(potionOverride)) {
            potRngBoost = (int) (4 + (baseRng * 0.10));
        } else if ("Overload".equalsIgnoreCase(potionOverride)) {
            potRngBoost = (int) (6 + (baseRng * 0.16));
        } else {
            potRngBoost = (int) (4 + (baseRng * 0.10)); // Ranging potion / Divine ranging
        }

        double prayAtkMult;
        double prayStrMult;
        if ("None".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.0;
            prayStrMult = 1.0;
        } else if ("Hawk Eye".equalsIgnoreCase(prayerOverride) || "10%".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.10;
            prayStrMult = 1.10;
        } else if ("Eagle Eye".equalsIgnoreCase(prayerOverride) || "15%".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.15;
            prayStrMult = 1.15;
        } else if ("Rigour".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.20;
            prayStrMult = 1.23;
        } else {
            prayAtkMult = (prayerLvl >= 74) ? 1.20 : (prayerLvl >= 44 ? 1.15 : (prayerLvl >= 26 ? 1.10 : 1.0)); // Rigour / Eagle Eye / Hawk Eye
            prayStrMult = (prayerLvl >= 74) ? 1.23 : (prayerLvl >= 44 ? 1.15 : (prayerLvl >= 26 ? 1.10 : 1.0));
        }

        int effectiveRngAtk = (int) Math.floor((baseRng + potRngBoost) * prayAtkMult) + 3 + 8; // Rapid style
        int effectiveRngStr = (int) Math.floor((baseRng + potRngBoost) * prayStrMult) + 3 + 8;

        SpecialEffectService.CombatMultipliers multipliers = specialEffectService.getRangedMultipliers(equipment, profile, onSlayerTask);
        double atkGearMult = multipliers.getAccuracyMultiplier();
        double strGearMult = multipliers.getDamageMultiplier();

        long maxAtkRoll = (long) Math.floor((effectiveRngAtk * (gearRangedAtk + 64)) * atkGearMult);

        int monsterDefLvl = (monster != null) ? monster.getDefenceLevel() : 100;
        int monsterRngDef = (monster != null) ? monster.getRangedDefence() : 0;
        long maxDefRoll = (long) (monsterDefLvl + 9) * (monsterRngDef + 64);

        double hitChance = computeHitChance(maxAtkRoll, maxDefRoll);

        int baseMaxHit = (int) Math.floor(0.5 + effectiveRngStr * (gearRangedStr + 64) / 640.0);
        int maxHit = (int) Math.floor(baseMaxHit * strGearMult);

        // Twisted bow scaling against high magic targets
        if (weapon != null && weapon.getName().contains("Twisted bow")) {
            int targetMag = (monster != null) ? Math.max(monster.getMagicLevel(), monster.getMagicDefence()) : 100;
            double tbowDamageMod = Math.min(2.5, 1.0 + (targetMag * 0.005));
            maxHit = (int) Math.floor(maxHit * tbowDamageMod);
        }

        maxHit += multipliers.getFlatMaxHitBonus();

        double expectedDmg = hitChance * (maxHit / 2.0);
        double dps = expectedDmg / (weaponTicks * 0.6);

        String details = String.format("Ranged: Hit %s, Max Hit %d, DPS %.2f",
                String.format("%.1f%%", hitChance * 100), maxHit, dps);

        return new CombatResult(CombatStyle.RANGED, AttackType.RANGED, hitChance, maxHit, expectedDmg, weaponTicks, dps, maxAtkRoll, maxDefRoll, details);
    }

    // =========================================================================
    // 3. MAGIC COMBAT CALCULATIONS
    // =========================================================================

    public CombatResult calculateMagic(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask) {
        return calculateMagic(equipment, monster, profile, account, onSlayerTask, null, null);
    }

    public CombatResult calculateMagic(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterVariant monster,
            MonsterCombatProfile profile,
            PlayerAccount account,
            boolean onSlayerTask,
            String prayerOverride,
            String potionOverride) {

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        int weaponTicks = (weapon != null) ? weapon.getAttackSpeed() : 4;
        if (weaponTicks <= 0) weaponTicks = 4;

        int gearMagicAtk = 0;
        int gearMagicDmgPct = 0;
        for (EquipmentItem item : equipment.values()) {
            if (item == null) continue;
            gearMagicAtk += item.getMagicAttack();
            gearMagicDmgPct += item.getMagicDamagePercent();
        }

        int baseMag = account != null ? account.getRealLevel(Skill.MAGIC) : 80;
        int prayerLvl = account != null ? account.getRealLevel(Skill.PRAYER) : 70;

        int potMagBoost;
        if ("None".equalsIgnoreCase(potionOverride)) {
            potMagBoost = 0;
        } else if ("Imbued Heart".equalsIgnoreCase(potionOverride) || "Heart".equalsIgnoreCase(potionOverride)) {
            potMagBoost = (int) (1 + (baseMag * 0.10));
        } else if ("Overload".equalsIgnoreCase(potionOverride)) {
            potMagBoost = (int) (6 + (baseMag * 0.16));
        } else {
            potMagBoost = 4; // Magic potion / Forgotten brew baseline
        }

        double prayAtkMult;
        if ("None".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.0;
        } else if ("Mystic Might".equalsIgnoreCase(prayerOverride) || "15%".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.15;
        } else if ("Augury".equalsIgnoreCase(prayerOverride)) {
            prayAtkMult = 1.25;
        } else {
            prayAtkMult = (prayerLvl >= 77) ? 1.25 : (prayerLvl >= 45 ? 1.15 : (prayerLvl >= 27 ? 1.10 : 1.0)); // Augury / Mystic Might / Mystic Lore
        }

        int effectiveMag = (int) Math.floor((baseMag + potMagBoost) * prayAtkMult) + 9;

        // Tumeken's Shadow: Triples gear magic attack and damage bonuses outside raids
        if (weapon != null && weapon.getName().contains("Shadow")) {
            gearMagicAtk *= 3;
            gearMagicDmgPct *= 3;
        }

        SpecialEffectService.CombatMultipliers multipliers = specialEffectService.getMagicMultipliers(equipment, profile, onSlayerTask);
        double atkGearMult = multipliers.getAccuracyMultiplier();
        double dmgGearMult = multipliers.getDamageMultiplier();

        long maxAtkRoll = (long) Math.floor((effectiveMag * (gearMagicAtk + 64)) * atkGearMult);

        // In OSRS, monster magic defence is based on MONSTER MAGIC LEVEL!
        int monsterMagLvl = (monster != null) ? monster.getMagicLevel() : 100;
        int monsterMagDef = (monster != null) ? monster.getMagicDefence() : 0;
        long maxDefRoll = (long) (monsterMagLvl + 9) * (monsterMagDef + 64);

        // Project Rebalance: Elemental Weakness modifiers
        ElementalWeakness weakness = (profile != null) ? profile.getElementalWeakness() : ElementalWeakness.NONE;
        int weaknessPct = (profile != null) ? profile.getElementalWeaknessPercent() : 0;
        if (weakness != ElementalWeakness.NONE && weaknessPct > 0) {
            // Amplifies accuracy and max hit
            maxAtkRoll = (long) Math.floor(maxAtkRoll * (1.0 + (weaknessPct / 100.0)));
        }

        double hitChance = computeHitChance(maxAtkRoll, maxDefRoll);

        // Base Magic Spell Damage
        int baseSpellDmg;
        String weaponName = (weapon != null) ? weapon.getName().toLowerCase() : "";
        if (weaponName.contains("trident of the swamp")) {
            baseSpellDmg = (int) Math.floor((baseMag - 75) / 3.0) + 23;
        } else if (weaponName.contains("trident of the seas")) {
            baseSpellDmg = (int) Math.floor((baseMag - 75) / 3.0) + 20;
        } else if (weaponName.contains("sanguinesti")) {
            baseSpellDmg = (int) Math.floor((baseMag - 75) / 3.0) + 24;
        } else if (weaponName.contains("warped sceptre")) {
            baseSpellDmg = (int) Math.floor((baseMag - 62) / 3.0) + 14;
        } else if (weaponName.contains("iban")) {
            baseSpellDmg = 25; // Iban blast
        } else if (weaponName.contains("shadow")) {
            baseSpellDmg = (int) Math.floor(baseMag / 3.0) + 1;
        } else {
            baseSpellDmg = 24; // Standard Fire Surge / Wave
        }

        int maxHit = (int) Math.floor(baseSpellDmg * (1.0 + (gearMagicDmgPct / 100.0)) * dmgGearMult);

        if (weakness != ElementalWeakness.NONE && weaknessPct > 0) {
            maxHit = (int) Math.floor(maxHit * (1.0 + (weaknessPct / 100.0)));
        }

        maxHit += multipliers.getFlatMaxHitBonus();

        double expectedDmg = hitChance * (maxHit / 2.0);
        double dps = expectedDmg / (weaponTicks * 0.6);

        String details = String.format("Magic: Hit %s, Max Hit %d, DPS %.2f",
                String.format("%.1f%%", hitChance * 100), maxHit, dps);

        return new CombatResult(CombatStyle.MAGIC, AttackType.MAGIC, hitChance, maxHit, expectedDmg, weaponTicks, dps, maxAtkRoll, maxDefRoll, details);
    }

    // =========================================================================
    // 4. CORE HIT CHANCE FORMULA
    // =========================================================================

    public static double computeHitChance(long attackRoll, long defenceRoll) {
        if (attackRoll <= 0) return 0.0;
        if (defenceRoll <= 0) return 1.0;

        if (attackRoll > defenceRoll) {
            return 1.0 - ((defenceRoll + 2.0) / (2.0 * (attackRoll + 1.0)));
        } else {
            return (double) attackRoll / (2.0 * (defenceRoll + 1.0));
        }
    }
}
