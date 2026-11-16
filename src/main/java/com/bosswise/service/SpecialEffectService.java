package com.bosswise.service;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class SpecialEffectService {

    public static class CombatMultipliers {
        private final double accuracyMultiplier;
        private final double damageMultiplier;
        private final int flatMaxHitBonus;
        private final String effectSummary;

        public CombatMultipliers(double accuracyMultiplier, double damageMultiplier, int flatMaxHitBonus, String effectSummary) {
            this.accuracyMultiplier = accuracyMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.flatMaxHitBonus = flatMaxHitBonus;
            this.effectSummary = effectSummary != null ? effectSummary : "";
        }

        public double getAccuracyMultiplier() { return accuracyMultiplier; }
        public double getDamageMultiplier() { return damageMultiplier; }
        public int getFlatMaxHitBonus() { return flatMaxHitBonus; }
        public String getEffectSummary() { return effectSummary; }
    }

    public CombatMultipliers getMeleeMultipliers(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterCombatProfile profile,
            boolean onSlayerTask) {

        double accMult = 1.0;
        double dmgMult = 1.0;
        int flatBonus = 0;
        StringBuilder notes = new StringBuilder();

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        EquipmentItem neck = equipment.get(EquipmentSlot.AMULET);
        EquipmentItem helm = equipment.get(EquipmentSlot.HEAD);

        // 1. Undead vs Slayer Stacking Rule: Salve does NOT stack with Slayer helm
        boolean salveApplied = false;
        if (profile != null && profile.isUndead() && neck != null) {
            String neckName = neck.getName().toLowerCase();
            if (neckName.contains("salve amulet (ei)") || neckName.contains("salve amulet(ei)") ||
                neckName.contains("salve amulet (e)") || neckName.contains("salve amulet(e)")) {
                accMult *= 1.20;
                dmgMult *= 1.20;
                salveApplied = true;
                notes.append("Salve (e) active (+20% acc/dmg). ");
            } else if (neckName.contains("salve amulet")) {
                accMult *= 1.15;
                dmgMult *= 1.15;
                salveApplied = true;
                notes.append("Salve active (+15% acc/dmg). ");
            }
        }

        // 2. Slayer Helm (only if Salve didn't take priority)
        if (!salveApplied && onSlayerTask && helm != null) {
            String helmName = helm.getName().toLowerCase();
            if (helmName.contains("slayer helmet") || helmName.contains("black mask")) {
                accMult *= (7.0 / 6.0); // +16.67%
                dmgMult *= (7.0 / 6.0);
                notes.append("Slayer helm active (+16.67% acc/dmg). ");
            }
        }

        // 3. Void Knight Melee (Stacks with Salve!)
        if (isFullVoid(equipment, "melee")) {
            accMult *= 1.10;
            dmgMult *= 1.10;
            notes.append("Void Knight Melee active (+10% acc/dmg). ");
        }

        // 4. Weapon Target Multipliers
        if (weapon != null && profile != null) {
            String weaponName = weapon.getName().toLowerCase();

            // Dragonbane
            if (profile.isDragon() && weapon.isDragonbane()) {
                accMult *= 1.20;
                dmgMult *= 1.20;
                notes.append("Dragonbane (DHL) active (+20% acc/dmg). ");
            }

            // Demonbane
            if (profile.isDemon() && weapon.isDemonbane()) {
                if (weaponName.contains("emberlight")) {
                    accMult *= 1.70;
                    dmgMult *= 1.36;
                    notes.append("Emberlight active (+70% acc, +36% dmg). ");
                } else {
                    accMult *= 1.70;
                    dmgMult *= 1.30;
                    notes.append("Arclight active (+70% acc, +30% dmg). ");
                }
            }

            // Kalphitebane
            if (profile.isKalphite() && (weaponName.contains("keris") || weapon.isKalphitebane())) {
                // Keris Partisan provides +33% pierce damage with 1/51 proc chance of 3x damage
                accMult *= 1.0;
                dmgMult *= 1.372; // Effective average expected damage multiplier
                notes.append("Keris Partisan active (+37.2% expected dmg). ");
            }

            // Ratbane
            if (profile.isRat() && weapon.isRatbane()) {
                flatBonus += 10;
                notes.append("Bone mace active (+10 flat max hit). ");
            }

            // Wilderness Weapons
            if (profile.isWilderness() && weapon.isWildernessBonus()) {
                accMult *= 1.50;
                dmgMult *= 1.50;
                notes.append("Wilderness buff active (+50% acc/dmg). ");
            }
        }

        return new CombatMultipliers(accMult, dmgMult, flatBonus, notes.toString().trim());
    }

    public CombatMultipliers getRangedMultipliers(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterCombatProfile profile,
            boolean onSlayerTask) {

        double accMult = 1.0;
        double dmgMult = 1.0;
        int flatBonus = 0;
        StringBuilder notes = new StringBuilder();

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        EquipmentItem neck = equipment.get(EquipmentSlot.AMULET);
        EquipmentItem helm = equipment.get(EquipmentSlot.HEAD);

        // 1. Salve vs Slayer
        boolean salveApplied = false;
        if (profile != null && profile.isUndead() && neck != null) {
            String neckName = neck.getName().toLowerCase();
            if (neckName.contains("salve amulet (ei)") || neckName.contains("salve amulet(ei)")) {
                accMult *= 1.20;
                dmgMult *= 1.20;
                salveApplied = true;
                notes.append("Salve (ei) active (+20% ranged acc/dmg). ");
            } else if (neckName.contains("salve amulet (i)") || neckName.contains("salve amulet(i)")) {
                accMult *= 1.15;
                dmgMult *= 1.15;
                salveApplied = true;
                notes.append("Salve (i) active (+15% ranged acc/dmg). ");
            }
        }

        if (!salveApplied && onSlayerTask && helm != null) {
            String helmName = helm.getName().toLowerCase();
            if (helmName.contains("slayer helmet (i)") || helmName.contains("slayer helm (i)") || helmName.contains("black mask (i)")) {
                accMult *= 1.15;
                dmgMult *= 1.15;
                notes.append("Slayer helm (i) active (+15% ranged acc/dmg). ");
            }
        }

        // 2. Void Knight Ranged
        if (isFullVoid(equipment, "ranged")) {
            boolean isElite = isEliteVoid(equipment);
            accMult *= 1.10;
            dmgMult *= (isElite ? 1.125 : 1.10);
            notes.append(isElite ? "Elite Void Ranged (+10% acc, +12.5% dmg). " : "Void Ranged (+10% acc/dmg). ");
        }

        // 3. Crystal Armour with Bowfa
        if (weapon != null && weapon.getName().toLowerCase().contains("faerdhinen")) {
            EquipmentItem body = equipment.get(EquipmentSlot.BODY);
            EquipmentItem legs = equipment.get(EquipmentSlot.LEGS);
            EquipmentItem head = equipment.get(EquipmentSlot.HEAD);

            double crystalAtk = 1.0;
            double crystalStr = 1.0;
            if (body != null && body.getName().contains("Crystal")) { crystalAtk += 0.15; crystalStr += 0.075; }
            if (legs != null && legs.getName().contains("Crystal")) { crystalAtk += 0.10; crystalStr += 0.050; }
            if (head != null && head.getName().contains("Crystal")) { crystalAtk += 0.05; crystalStr += 0.025; }

            accMult *= crystalAtk;
            dmgMult *= crystalStr;
            notes.append(String.format("Crystal armour scaling (Atk: +%.1f%%, Dmg: +%.1f%%). ", (crystalAtk - 1.0) * 100, (crystalStr - 1.0) * 100));
        }

        // 4. Weapon Target Multipliers
        if (weapon != null && profile != null) {
            String weaponName = weapon.getName().toLowerCase();

            // Dragonbane (DHCB)
            if (profile.isDragon() && weapon.isDragonbane()) {
                accMult *= 1.30;
                dmgMult *= 1.30;
                notes.append("Dragon hunter crossbow (+30% acc/dmg). ");
            }

            // Demonbane (Scorching bow)
            if (profile.isDemon() && (weapon.isDemonbane() || weaponName.contains("scorching"))) {
                accMult *= 1.30;
                dmgMult *= 1.30;
                notes.append("Scorching bow (+30% acc/dmg). ");
            }

            // Wilderness (Webweaver bow)
            if (profile.isWilderness() && weapon.isWildernessBonus()) {
                accMult *= 1.50;
                dmgMult *= 1.50;
                notes.append("Webweaver bow wildy buff (+50% acc/dmg). ");
            }

            // Ratbane
            if (profile.isRat() && weapon.isRatbane()) {
                flatBonus += 10;
                notes.append("Bone shortbow (+10 flat max hit). ");
            }
        }

        return new CombatMultipliers(accMult, dmgMult, flatBonus, notes.toString().trim());
    }

    public CombatMultipliers getMagicMultipliers(
            Map<EquipmentSlot, EquipmentItem> equipment,
            MonsterCombatProfile profile,
            boolean onSlayerTask) {

        double accMult = 1.0;
        double dmgMult = 1.0;
        int flatBonus = 0;
        StringBuilder notes = new StringBuilder();

        EquipmentItem weapon = equipment.get(EquipmentSlot.WEAPON);
        EquipmentItem neck = equipment.get(EquipmentSlot.AMULET);
        EquipmentItem helm = equipment.get(EquipmentSlot.HEAD);

        // 1. Salve vs Slayer
        boolean salveApplied = false;
        if (profile != null && profile.isUndead() && neck != null) {
            String neckName = neck.getName().toLowerCase();
            if (neckName.contains("salve amulet (ei)") || neckName.contains("salve amulet(ei)")) {
                accMult *= 1.20;
                dmgMult *= 1.20;
                salveApplied = true;
                notes.append("Salve (ei) active (+20% magic acc/dmg). ");
            } else if (neckName.contains("salve amulet (i)") || neckName.contains("salve amulet(i)")) {
                accMult *= 1.15;
                dmgMult *= 1.15;
                salveApplied = true;
                notes.append("Salve (i) active (+15% magic acc/dmg). ");
            }
        }

        if (!salveApplied && onSlayerTask && helm != null) {
            String helmName = helm.getName().toLowerCase();
            if (helmName.contains("slayer helmet (i)") || helmName.contains("slayer helm (i)") || helmName.contains("black mask (i)")) {
                accMult *= 1.15;
                dmgMult *= 1.15;
                notes.append("Slayer helm (i) active (+15% magic acc/dmg). ");
            }
        }

        // 2. Void Knight Magic
        if (isFullVoid(equipment, "magic")) {
            boolean isElite = isEliteVoid(equipment);
            accMult *= 1.45;
            dmgMult *= (isElite ? 1.025 : 1.0);
            notes.append(isElite ? "Elite Void Magic (+45% acc, +2.5% dmg). " : "Void Magic (+45% acc). ");
        }

        // 3. Weapon Specifics (Purging staff, Accursed sceptre)
        if (weapon != null && profile != null) {
            String weaponName = weapon.getName().toLowerCase();
            if (profile.isDemon() && (weapon.isDemonbane() || weaponName.contains("purging"))) {
                accMult *= 1.30;
                dmgMult *= 1.30;
                notes.append("Purging staff (+30% acc/dmg vs demons). ");
            }
            if (profile.isWilderness() && weapon.isWildernessBonus()) {
                accMult *= 1.50;
                dmgMult *= 1.50;
                notes.append("Accursed sceptre wildy buff (+50% acc/dmg). ");
            }
            if (profile.isRat() && weapon.isRatbane()) {
                flatBonus += 10;
                notes.append("Bone staff (+10 flat max hit). ");
            }
        }

        return new CombatMultipliers(accMult, dmgMult, flatBonus, notes.toString().trim());
    }

    private boolean isFullVoid(Map<EquipmentSlot, EquipmentItem> equipment, String style) {
        EquipmentItem body = equipment.get(EquipmentSlot.BODY);
        EquipmentItem legs = equipment.get(EquipmentSlot.LEGS);
        EquipmentItem gloves = equipment.get(EquipmentSlot.GLOVES);
        EquipmentItem helm = equipment.get(EquipmentSlot.HEAD);

        if (body == null || legs == null || gloves == null || helm == null) return false;

        boolean hasBody = body.getName().contains("Void knight top") || body.getName().contains("Elite void top");
        boolean hasLegs = legs.getName().contains("Void knight robe") || legs.getName().contains("Elite void robe");
        boolean hasGloves = gloves.getName().contains("Void knight gloves");

        if (!hasBody || !hasLegs || !hasGloves) return false;

        String helmName = helm.getName().toLowerCase();
        if ("melee".equalsIgnoreCase(style)) return helmName.contains("void melee helm");
        if ("ranged".equalsIgnoreCase(style)) return helmName.contains("void ranger helm");
        if ("magic".equalsIgnoreCase(style)) return helmName.contains("void mage helm");

        return false;
    }

    private boolean isEliteVoid(Map<EquipmentSlot, EquipmentItem> equipment) {
        EquipmentItem body = equipment.get(EquipmentSlot.BODY);
        EquipmentItem legs = equipment.get(EquipmentSlot.LEGS);
        return body != null && legs != null &&
               body.getName().contains("Elite void top") &&
               legs.getName().contains("Elite void robe");
    }
}
