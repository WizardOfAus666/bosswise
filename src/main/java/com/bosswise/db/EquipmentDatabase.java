package com.bosswise.db;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentItem.AmmoType;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.EquipmentSlot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EquipmentDatabase {
    private static final Logger log = LoggerFactory.getLogger(EquipmentDatabase.class);
    private final List<EquipmentItem> allItems = new ArrayList<>();
    private final Map<EquipmentSlot, List<EquipmentItem>> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
    private final Map<Integer, EquipmentItem> itemsById = new HashMap<>();
    private final Map<String, EquipmentItem> itemsByName = new HashMap<>();

    public EquipmentDatabase() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            itemsBySlot.put(slot, new ArrayList<>());
        }
        initEquipment();
        loadEquipmentFromJson();
    }

    private void addItem(EquipmentItem item) {
        if (item == null) return;
        EquipmentItem existing = itemsById.get(item.getItemId());
        if (existing != null) {
            allItems.remove(existing);
            itemsBySlot.get(existing.getSlot()).remove(existing);
        }
        allItems.add(item);
        itemsBySlot.get(item.getSlot()).add(item);
        itemsById.put(item.getItemId(), item);
        itemsByName.put(item.getName().toLowerCase(), item);
    }

    private void loadEquipmentFromJson() {
        try (InputStream is = getClass().getResourceAsStream("/data/equipment.json")) {
            if (is == null) {
                log.info("[BossWise] /data/equipment.json not found on classpath, retaining programmatic defaults");
                return;
            }

            Gson gson = new Gson();
            JsonArray array = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonArray.class);
            if (array == null) return;

            int loaded = 0;
            for (JsonElement elem : array) {
                if (!elem.isJsonObject()) continue;
                try {
                    JsonObject obj = elem.getAsJsonObject();

                    int itemId = obj.get("itemId").getAsInt();
                    String name = obj.get("name").getAsString();
                    EquipmentSlot slot = EquipmentSlot.valueOf(obj.get("slot").getAsString());
                    CombatStyle style;
                    try {
                        style = CombatStyle.valueOf(obj.get("primaryStyle").getAsString());
                    } catch (Exception e) {
                        style = CombatStyle.HYBRID;
                    }

                    EquipmentItem.Builder builder = EquipmentItem.builder(itemId, name, slot, style);

                if (obj.has("attackType")) {
                    try {
                        builder.attackType(AttackType.valueOf(obj.get("attackType").getAsString()));
                    } catch (Exception ignored) {}
                }
                if (obj.has("ammoType")) {
                    try {
                        builder.ammoType(AmmoType.valueOf(obj.get("ammoType").getAsString()));
                    } catch (Exception ignored) {}
                }
                if (obj.has("weaponCategory")) builder.weaponCategory(obj.get("weaponCategory").getAsString());
                if (obj.has("attackSpeed")) builder.speed(obj.get("attackSpeed").getAsInt());
                if (obj.has("twoHanded") && obj.get("twoHanded").getAsBoolean()) builder.twoHanded();
                if (obj.has("defaultPrice")) builder.price(obj.get("defaultPrice").getAsInt());
                if (obj.has("tier")) builder.tier(obj.get("tier").getAsInt());
                if (obj.has("untradeable") && obj.get("untradeable").getAsBoolean()) builder.untradeable();
                if (obj.has("questReq")) builder.questReq(obj.get("questReq").getAsString());

                if (obj.has("reqSkills") && obj.get("reqSkills").isJsonObject()) {
                    JsonObject skills = obj.getAsJsonObject("reqSkills");
                    for (Map.Entry<String, JsonElement> entry : skills.entrySet()) {
                        try {
                            Skill skill = Skill.valueOf(entry.getKey().toUpperCase());
                            builder.req(skill, entry.getValue().getAsInt());
                        } catch (Exception ignored) {}
                    }
                }

                int stabAtk = obj.has("stabAttack") ? obj.get("stabAttack").getAsInt() : 0;
                int slashAtk = obj.has("slashAttack") ? obj.get("slashAttack").getAsInt() : 0;
                int crushAtk = obj.has("crushAttack") ? obj.get("crushAttack").getAsInt() : 0;
                int magicAtk = obj.has("magicAttack") ? obj.get("magicAttack").getAsInt() : 0;
                int rangedAtk = obj.has("rangedAttack") ? obj.get("rangedAttack").getAsInt() : 0;
                builder.atk(stabAtk, slashAtk, crushAtk, magicAtk, rangedAtk);

                int stabDef = obj.has("stabDefence") ? obj.get("stabDefence").getAsInt() : 0;
                int slashDef = obj.has("slashDefence") ? obj.get("slashDefence").getAsInt() : 0;
                int crushDef = obj.has("crushDefence") ? obj.get("crushDefence").getAsInt() : 0;
                int magicDef = obj.has("magicDefence") ? obj.get("magicDefence").getAsInt() : 0;
                int rangedDef = obj.has("rangedDefence") ? obj.get("rangedDefence").getAsInt() : 0;
                builder.def(stabDef, slashDef, crushDef, magicDef, rangedDef);

                int meleeStr = obj.has("meleeStrength") ? obj.get("meleeStrength").getAsInt() : 0;
                int rangedStr = obj.has("rangedStrength") ? obj.get("rangedStrength").getAsInt() : 0;
                int magicDmg = obj.has("magicDamagePercent") ? obj.get("magicDamagePercent").getAsInt() : 0;
                builder.str(meleeStr, rangedStr, magicDmg);

                if (obj.has("prayerBonus")) builder.prayer(obj.get("prayerBonus").getAsInt());

                if (obj.has("dragonbane") && obj.get("dragonbane").getAsBoolean()) builder.dragonbane();
                if (obj.has("demonbane") && obj.get("demonbane").getAsBoolean()) builder.demonbane();
                if (obj.has("ratbane") && obj.get("ratbane").getAsBoolean()) builder.ratbane();
                if (obj.has("undeadBonus") && obj.get("undeadBonus").getAsBoolean()) builder.undeadBonus();
                if (obj.has("slayerTaskBonus") && obj.get("slayerTaskBonus").getAsBoolean()) builder.slayerTaskBonus();
                if (obj.has("dragonfireProtection") && obj.get("dragonfireProtection").getAsBoolean()) builder.dragonfireProtection();
                if (obj.has("venomProtection") && obj.get("venomProtection").getAsBoolean()) builder.venomProtection();
                if (obj.has("wildernessBonus") && obj.get("wildernessBonus").getAsBoolean()) builder.wildernessBonus();
                if (obj.has("spear") && obj.get("spear").getAsBoolean()) builder.spear();
                if (obj.has("mirrorShield") && obj.get("mirrorShield").getAsBoolean()) builder.mirrorShield();
                if (obj.has("leafBladed") && obj.get("leafBladed").getAsBoolean()) builder.leafBladed();
                if (obj.has("bootsOfStone") && obj.get("bootsOfStone").getAsBoolean()) builder.bootsOfStone();
                if (obj.has("voidKnight") && obj.get("voidKnight").getAsBoolean()) builder.voidKnight();
                if (obj.has("kalphitebane") && obj.get("kalphitebane").getAsBoolean()) builder.kalphitebane();

                if (obj.has("hasSpecialAttack") && obj.get("hasSpecialAttack").getAsBoolean()) {
                    int energy = obj.has("specEnergyCost") ? obj.get("specEnergyCost").getAsInt() : 50;
                    String specDesc = obj.has("specEffect") ? obj.get("specEffect").getAsString() : "";
                    builder.specialAttack(energy, specDesc);
                }

                if (obj.has("specialEffect")) builder.effect(obj.get("specialEffect").getAsString());
                if (obj.has("source")) builder.source(obj.get("source").getAsString());
                if (obj.has("verifiedDate")) builder.verifiedDate(obj.get("verifiedDate").getAsString());

                addItem(builder.build());
                loaded++;
            } catch (Exception itemEx) {
                log.warn("[BossWise] Failed parsing equipment item from JSON: {}", itemEx.getMessage());
            }
        }
            log.info("[BossWise] Loaded {} equipment items from JSON (Total indexed: {})", loaded, allItems.size());
        } catch (Exception e) {
            log.error("[BossWise] Error loading equipment from JSON", e);
        }
    }

    private void initEquipment() {
        // ==========================================
        // 1. WEAPONS - MELEE (Stab, Slash, Crush, Demonbane, Dragonbane, Spears)
        // ==========================================
        addItem(EquipmentItem.builder(1333, "Rune scimitar", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 40).price(15_000).tier(1).speed(4).attackType(AttackType.SLASH)
                .str(44, 0, 0).atk(7, 45, -2, 0, 0)
                .effect("Budget 4-tick slash weapon for level 40 Attack").build());

        addItem(EquipmentItem.builder(4587, "Dragon scimitar", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).questReq("Monkey Madness I").price(60_000).tier(4).speed(4).attackType(AttackType.SLASH)
                .str(66, 0, 0).atk(8, 67, -2, 0, 0)
                .effect("Premier 4-tick slash weapon; Monkey Madness I unlock").build());

        addItem(EquipmentItem.builder(21009, "Dragon sword", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).price(40_000).tier(3).speed(4).attackType(AttackType.STAB)
                .str(63, 0, 0).atk(65, 55, -2, 0, 0)
                .effect("Reliable 4-tick one-handed stab weapon for dragons and metal monsters").build());

        addItem(EquipmentItem.builder(1434, "Dragon mace", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).price(35_000).tier(3).speed(4).attackType(AttackType.CRUSH)
                .str(66, 0, 0).atk(40, -4, 60, 0, 0).prayer(5)
                .effect("High-value 4-tick crush weapon with +5 Prayer bonus (Calvar'ion & Sarachnis staple)").build());

        addItem(EquipmentItem.builder(4153, "Granite maul", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 50).req(Skill.STRENGTH, 50).price(30_000).tier(2).speed(7).twoHanded().attackType(AttackType.CRUSH)
                .str(79, 0, 0).atk(0, 0, 81, 0, 0)
                .effect("Heavy 2-handed crush weapon").build());

        addItem(EquipmentItem.builder(4158, "Leaf-bladed sword", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 50).req(Skill.SLAYER, 55).price(40_000).tier(2).speed(4).leafBladed().attackType(AttackType.STAB)
                .str(50, 0, 0).atk(67, 0, 0, 0, 0)
                .effect("Fast 4-tick stab weapon; damages Kurasks and Turoths").build());

        addItem(EquipmentItem.builder(20727, "Leaf-bladed battleaxe", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 65).req(Skill.SLAYER, 55).price(85_000).tier(4).speed(5).leafBladed().attackType(AttackType.CRUSH)
                .str(92, 0, 0).atk(0, 72, 92, 0, 0)
                .effect("Heavy crush/slash axe with massive +92 Strength; excels on Kurasks & Turoths").build());

        addItem(EquipmentItem.builder(11902, "Leaf-bladed spear", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 50).req(Skill.SLAYER, 55).price(30_000).tier(2).speed(4).twoHanded().spear().leafBladed().attackType(AttackType.STAB)
                .str(50, 0, 0).atk(50, 50, 50, 0, 0)
                .effect("Spear capable of harming Kurasks and Turoths").build());

        addItem(EquipmentItem.builder(23509, "Sarachnis cudgel", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 65).price(400_000).tier(4).speed(4).attackType(AttackType.CRUSH)
                .str(70, 0, 0).atk(0, 0, 70, 0, 0)
                .effect("Top-tier fast 4-tick crush weapon; excels against crush-weak foes (Sarachnis, Spindel)").build());

        addItem(EquipmentItem.builder(28678, "Zombie axe", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 65).price(1_200_000).tier(5).speed(5).attackType(AttackType.SLASH)
                .str(85, 0, 0).atk(0, 105, 90, 0, 0)
                .effect("High-damage 5-tick weapon with exceptional +105 Slash and +90 Crush accuracy").build());

        addItem(EquipmentItem.builder(4151, "Abyssal whip", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).price(1_600_000).tier(5).speed(4).attackType(AttackType.SLASH)
                .str(82, 0, 0).atk(0, 82, 0, 0, 0)
                .effect("Standard 4-tick general slash weapon for mid-to-high PvM").build());

        addItem(EquipmentItem.builder(12006, "Abyssal tentacle", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(2_500_000).tier(6).speed(4).attackType(AttackType.SLASH)
                .str(86, 0, 0).atk(0, 90, 0, 0, 0)
                .effect("High DPS upgrade to Abyssal whip; inflicts poison (Vardorvis & ToB staple)").build());

        addItem(EquipmentItem.builder(13263, "Abyssal bludgeon", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).req(Skill.STRENGTH, 70).price(12_000_000).tier(7).speed(4).twoHanded().attackType(AttackType.CRUSH)
                .str(85, 0, 0).atk(0, 0, 102, 0, 0).prayer(1)
                .effect("Two-handed 4-tick crush weapon with +102 Crush accuracy (Spindel, Gargoyles, Nightmare)").build());

        addItem(EquipmentItem.builder(11889, "Zamorakian spear", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(3_500_000).tier(6).speed(4).twoHanded().spear().attackType(AttackType.STAB)
                .str(75, 0, 0).atk(65, 65, 65, 0, 0).def(13, 13, 13, 0, 0).prayer(2)
                .effect("Spear: Deals full 100% damage to Corporeal Beast (ignores 50% damage reduction)").build());

        addItem(EquipmentItem.builder(11824, "Zamorakian hasta", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).price(3_200_000).tier(6).speed(4).attackType(AttackType.STAB)
                .str(75, 0, 0).atk(65, 65, 65, 0, 0).def(13, 13, 13, 0, 0).prayer(2)
                .effect("Versatile 1-handed stab/crush weapon allowing shield use; Dragon & ToA staple").build());

        addItem(EquipmentItem.builder(28698, "Bone mace", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 50).price(25_000).tier(2).speed(4).ratbane().attackType(AttackType.CRUSH)
                .str(50, 0, 0).atk(0, 0, 52, 0, 0)
                .effect("Ratbane: +10 max hit and massive accuracy against Scurrius and giant rats").build());

        addItem(EquipmentItem.builder(19675, "Arclight", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).untradeable().price(0).tier(7).speed(4).demonbane().attackType(AttackType.SLASH)
                .str(38, 0, 0).atk(0, 10, 0, 0, 0)
                .effect("Demonbane: Grants +70% accuracy and +30% damage against all demons (K'ril, Cerberus, Duke, Sire)").build());

        addItem(EquipmentItem.builder(29597, "Emberlight", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).questReq("While Guthix Sleeps").price(35_000_000).tier(9).speed(4).demonbane().attackType(AttackType.SLASH)
                .str(75, 0, 0).atk(0, 90, 0, 0, 0)
                .effect("Ultimate Demonbane: Permanent infusion granting +70% accuracy & +36% damage vs demons").build());

        addItem(EquipmentItem.builder(26819, "Keris partisan", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 65).questReq("Beneath Cursed Sands").untradeable().price(0).tier(5).speed(4).attackType(AttackType.STAB)
                .str(67, 0, 0).atk(58, -2, -2, 0, 0)
                .effect("+33% damage against Kalphites, Scarabs, and Kephri; special 3x damage proc").build());

        addItem(EquipmentItem.builder(26219, "Osmumten's fang", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 82).price(14_000_000).tier(8).speed(5).attackType(AttackType.STAB)
                .str(103, 0, 0).atk(105, 75, 0, 0, 0)
                .effect("Rerolls accuracy on missed hits; unmatched precision against high-defense bosses (ToA, Nex, Vardorvis)").build());

        addItem(EquipmentItem.builder(22978, "Dragon hunter lance", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 78).price(70_000_000).tier(9).speed(4).dragonbane().attackType(AttackType.STAB)
                .str(85, 0, 0).atk(85, 65, 65, 0, 0)
                .effect("Dragonbane: +20% damage and +20% accuracy against all dragons (Vorkath, KBD, Olm, Hydra)").build());

        addItem(EquipmentItem.builder(22324, "Ghrazi rapier", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 80).price(50_000_000).tier(9).speed(4).attackType(AttackType.STAB)
                .str(89, 0, 0).atk(94, 55, 0, 0, 0)
                .effect("Best-in-slot 4-tick one-handed stab weapon for Slayer and low-defense bosses").build());

        addItem(EquipmentItem.builder(24417, "Inquisitor's mace", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(180_000_000).tier(9).speed(4).attackType(AttackType.CRUSH)
                .str(89, 0, 0).atk(0, 0, 95, 0, 0).prayer(2)
                .effect("Best-in-slot 4-tick crush weapon; devastating against crush-weak bosses with +2 Prayer").build());

        addItem(EquipmentItem.builder(25867, "Blade of saeldor", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 80).questReq("Song of the Elves").price(90_000_000).tier(9).speed(4).attackType(AttackType.SLASH)
                .str(89, 0, 0).atk(0, 94, 0, 0, 0)
                .effect("Best-in-slot 4-tick one-handed slash weapon (Vardorvis, Duke, ToB)").build());

        addItem(EquipmentItem.builder(27690, "Soulreaper axe", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 80).req(Skill.STRENGTH, 90).questReq("Desert Treasure II").price(350_000_000).tier(10).speed(5).twoHanded().attackType(AttackType.SLASH)
                .str(135, 0, 0).atk(0, 121, 0, 0, 0)
                .effect("Builds Soul Stacks dealing up to +24% extra damage and heals with special attack (Vardorvis & Duke BiS)").build());

        addItem(EquipmentItem.builder(22325, "Scythe of vitur", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 80).req(Skill.STRENGTH, 90).price(1_100_000_000).tier(10).speed(5).twoHanded().attackType(AttackType.SLASH)
                .str(75, 0, 0).atk(30, 110, 30, -6, 0)
                .effect("Strikes up to 3 times per swing against large (3x3+) bosses; highest raw melee DPS in OSRS").build());

        addItem(EquipmentItem.builder(21003, "Elder maul", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).req(Skill.STRENGTH, 75).price(80_000_000).tier(9).speed(6).twoHanded().attackType(AttackType.CRUSH)
                .str(135, 0, 0).atk(0, 0, 135, 0, 0)
                .effect("Colossal two-handed crush maul; drains 50% target defense on special attack").build());

        addItem(EquipmentItem.builder(4718, "Dharok's greataxe", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).req(Skill.STRENGTH, 70).price(2_500_000).tier(6).speed(7).twoHanded().attackType(AttackType.SLASH)
                .str(105, 0, 0).atk(-4, 103, 95, 0, 0)
                .effect("Dharok set effect: Hits astronomical damage up to 100+ as player HP decreases (Mole staple)").build());

        addItem(EquipmentItem.builder(22545, "Viggora's chainmace", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).price(1_500_000).tier(5).speed(4).wildernessBonus().attackType(AttackType.CRUSH)
                .str(66, 0, 0).atk(0, 0, 67, 0, 0)
                .effect("Wilderness: Deals +50% damage and +50% accuracy inside Wilderness").build());

        addItem(EquipmentItem.builder(27655, "Ursine chainmace", EquipmentSlot.WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).price(10_000_000).tier(7).speed(4).wildernessBonus().attackType(AttackType.CRUSH)
                .str(72, 0, 0).atk(0, 0, 77, 0, 0)
                .effect("Wilderness: Deals +50% damage and +50% accuracy inside the Wilderness (Calvar'ion, Spindel)").build());

        // ==========================================
        // 2. WEAPONS - RANGED
        // ==========================================
        addItem(EquipmentItem.builder(861, "Magic shortbow (i)", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 50).price(20_000).tier(4).speed(3).twoHanded().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 75)
                .effect("Rapid 3-tick bow on rapid style; uses arrows").build());

        addItem(EquipmentItem.builder(28700, "Bone shortbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 50).price(25_000).tier(2).speed(3).twoHanded().ratbane().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 70)
                .effect("Ratbane: +10 max hit and massive accuracy against Scurrius and giant rats").build());

        addItem(EquipmentItem.builder(9185, "Rune crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 61).price(10_000).tier(4).speed(5).ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 90)
                .effect("One-handed crossbow capable of firing enchanted bolts (Ruby/Diamond/Broad)").build());

        addItem(EquipmentItem.builder(21902, "Dragon crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 64).price(1_800_000).tier(5).speed(5).ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 94)
                .effect("Fires Dragon bolts (e) with high ranged accuracy").build());

        addItem(EquipmentItem.builder(11785, "Armadyl crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).price(25_000_000).tier(7).speed(5).ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 100).prayer(1)
                .effect("+100 Ranged attack and doubles special attack accuracy").build());

        addItem(EquipmentItem.builder(26374, "Zaryte crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).price(280_000_000).tier(10).speed(5).ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 110).prayer(2)
                .effect("Best-in-slot crossbow: +10% max hit & accuracy on enchanted bolt procs (Nex, Leviathan BiS)").build());

        addItem(EquipmentItem.builder(12926, "Toxic blowpipe", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).price(3_500_000).tier(7).speed(2).twoHanded().attackType(AttackType.RANGED)
                .str(0, 40, 0).atk(0, 0, 0, 0, 60)
                .effect("Blistering 2-tick attack speed on rapid; special attack heals 50% damage (Zulrah, GGs staple)").build());

        addItem(EquipmentItem.builder(21012, "Dragon hunter crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).price(55_000_000).tier(9).speed(5).dragonbane().ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 95)
                .effect("Dragonbane: +30% damage and +30% accuracy against all dragons (Vorkath, KBD, Olm)").build());

        addItem(EquipmentItem.builder(25865, "Bow of faerdhinen", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).req(Skill.AGILITY, 70).questReq("Song of the Elves").price(120_000_000).tier(9).speed(4).twoHanded().attackType(AttackType.RANGED)
                .str(0, 106, 0).atk(0, 0, 0, 0, 128)
                .effect("Accurate 4-tick longbow; damage and accuracy massively amplified by Crystal Armour (Muspah, Leviathan)").build());

        addItem(EquipmentItem.builder(27610, "Venator bow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).price(45_000_000).tier(8).speed(4).twoHanded().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 0, 0).atk(0, 0, 0, 0, 85)
                .effect("Fires ricocheting arrows striking up to 3 targets simultaneously (Leviathan & Slayer multi-combat)").build());

        addItem(EquipmentItem.builder(29600, "Scorching bow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).price(50_000_000).tier(9).speed(4).twoHanded().demonbane().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 60, 0).atk(0, 0, 0, 0, 110)
                .effect("Demonbane Bow: +30% damage & +30% accuracy against all demons (Cerberus, K'ril, Tormented demons)").build());

        addItem(EquipmentItem.builder(22550, "Craw's bow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 60).price(1_500_000).tier(5).speed(3).twoHanded().wildernessBonus().attackType(AttackType.RANGED)
                .str(0, 60, 0).atk(0, 0, 0, 0, 75)
                .effect("Wilderness: Deals +50% damage and +50% accuracy inside Wilderness").build());

        addItem(EquipmentItem.builder(27652, "Webweaver bow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).price(15_000_000).tier(7).speed(3).twoHanded().wildernessBonus().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 60, 0).atk(0, 0, 0, 0, 95)
                .effect("Wilderness: Deals +50% damage and +50% accuracy inside Wilderness (Artio, Callisto, Chaos Elemental)").build());

        addItem(EquipmentItem.builder(20997, "Twisted bow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 85).price(1_400_000_000).tier(10).speed(5).twoHanded().ammoType(AmmoType.ARROW).attackType(AttackType.RANGED)
                .str(0, 20, 0).atk(0, 0, 0, 0, 70)
                .effect("Scales accuracy and damage up to +250% against bosses with high Magic stats (Zulrah, Muspah, CoX)").build());

        addItem(EquipmentItem.builder(4734, "Karil's crossbow", EquipmentSlot.WEAPON, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).price(150_000).tier(5).speed(3).twoHanded().ammoType(AmmoType.BOLT).attackType(AttackType.RANGED)
                .str(0, 84, 0).atk(0, 0, 0, 0, 84)
                .effect("Rapid 3-tick two-handed crossbow firing bolt racks with high DPS").build());

        // ==========================================
        // 3. WEAPONS - MAGIC
        // ==========================================
        addItem(EquipmentItem.builder(1409, "Iban's staff (u)", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 50).req(Skill.ATTACK, 50).questReq("Underground Pass").price(100_000).tier(4).speed(5).attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 10, 0)
                .effect("Casts Iban Blast hitting up to 25 base damage at low magic levels (Barrows & Rex staple)").build());

        addItem(EquipmentItem.builder(28702, "Bone staff", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 50).price(25_000).tier(2).speed(4).ratbane().attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 15, 0)
                .effect("Ratbane: +10 max hit and massive accuracy against Scurrius and giant rats").build());

        addItem(EquipmentItem.builder(4675, "Ancient staff", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 50).req(Skill.ATTACK, 50).questReq("Desert Treasure I").price(60_000).tier(3).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 15, 0)
                .effect("Auto-casts Ancient Magicks (Ice Burst / Ice Barrage for Slayer & Whisperer)").build());

        addItem(EquipmentItem.builder(28682, "Warped sceptre", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 62).req(Skill.ATTACK, 62).price(150_000).tier(4).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 15, 0)
                .effect("Budget 4-tick powered staff; casts built-in spell without spellbook runes").build());

        addItem(EquipmentItem.builder(11907, "Trident of the seas", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).price(40_000).tier(5).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 15, 0)
                .effect("Fast 4-tick powered staff; essential for Barrows, Zulrah, and Kraken").build());

        addItem(EquipmentItem.builder(12899, "Trident of the swamp", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).price(2_500_000).tier(6).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 3).atk(0, 0, 0, 25, 0)
                .effect("+3 higher base max hit than Trident of the seas and inflicts venom").build());

        addItem(EquipmentItem.builder(22323, "Sanguinesti staff", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 82).price(65_000_000).tier(8).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 25, 0)
                .effect("4-tick powered staff with 1/6 chance to heal 50% of damage dealt (Whisperer & ToA staple)").build());

        addItem(EquipmentItem.builder(21006, "Kodai wand", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).price(95_000_000).tier(9).speed(4).attackType(AttackType.MAGIC)
                .str(0, 0, 15).atk(0, 0, 0, 28, 0)
                .effect("Top-tier wand with +15% magic damage, unlimited water runes, and 15% rune saving").build());

        addItem(EquipmentItem.builder(29603, "Purging staff", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).price(40_000_000).tier(9).speed(4).demonbane().attackType(AttackType.MAGIC)
                .str(0, 0, 10).atk(0, 0, 0, 25, 0)
                .effect("Demonbane Staff: Deals amplified magic damage and accuracy against demon foes").build());

        addItem(EquipmentItem.builder(22555, "Thammaron's sceptre", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 60).price(1_500_000).tier(5).speed(4).wildernessBonus().attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 20, 0)
                .effect("Wilderness: Deals +50% magic damage and +50% magic accuracy inside Wilderness").build());

        addItem(EquipmentItem.builder(27665, "Accursed sceptre", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 70).price(12_000_000).tier(7).speed(4).wildernessBonus().attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 22, 0)
                .effect("Wilderness: +50% magic damage and +50% magic accuracy inside the Wilderness (Artio, Scorpia)").build());

        addItem(EquipmentItem.builder(27275, "Tumeken's shadow", EquipmentSlot.WEAPON, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 85).price(1_300_000_000).tier(10).speed(5).twoHanded().attackType(AttackType.MAGIC)
                .str(0, 0, 0).atk(0, 0, 0, 35, 0)
                .effect("Triples magic attack and magic damage bonuses from your equipped gear inside PvM! (Whisperer, ToA BiS)").build());

        // ==========================================
        // 4. SHIELDS / OFFHANDS
        // ==========================================
        addItem(EquipmentItem.builder(1540, "Anti-dragon shield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .questReq("Dragon Slayer I").price(200).tier(1).dragonfireProtection()
                .def(9, 10, 8, 2, 8)
                .effect("Absorbs dragonfire breath; mandatory against Vorkath and King Black Dragon").build());

        addItem(EquipmentItem.builder(2890, "Elemental shield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .questReq("Elemental Workshop I").price(500).tier(1).dragonfireProtection()
                .def(6, 6, 6, 6, 6)
                .effect("Absorbs icy dragonfire breath; protects against Skeletal Wyverns").build());

        addItem(EquipmentItem.builder(4156, "Mirror shield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 20).req(Skill.SLAYER, 25).price(5_000).tier(2).mirrorShield()
                .def(10, 12, 10, -1, 10)
                .effect("Reflects gaze of Basilisks and Basilisk Knights; mandatory to prevent stat drain").build());

        addItem(EquipmentItem.builder(24268, "V's shield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 60).questReq("The Fremennik Exiles").price(20_000).tier(5).mirrorShield()
                .def(44, 48, 46, -1, 46).prayer(1)
                .effect("Upgraded mirror shield providing high defense & prayer bonus; ideal for Basilisk Knights").build());

        addItem(EquipmentItem.builder(11283, "Dragonfire shield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 75).price(3_500_000).tier(6).dragonfireProtection()
                .str(7, 0, 0).atk(-10, -10, -10, -5, -5).def(70, 75, 72, 10, 72)
                .effect("Melee dragonfire protection with +7 Melee Strength and exceptional defensive stats").build());

        addItem(EquipmentItem.builder(22002, "Dragonfire ward", EquipmentSlot.SHIELD, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).req(Skill.DEFENCE, 75).price(6_000_000).tier(7).dragonfireProtection()
                .str(0, 0, 0).atk(-10, -10, -10, -5, 15).def(70, 75, 72, 10, 72)
                .effect("Ranged dragonfire protection with +15 Ranged Attack and heavy defense (Vorkath staple)").build());

        addItem(EquipmentItem.builder(21633, "Ancient wyvern shield", EquipmentSlot.SHIELD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(8_000_000).tier(8).dragonfireProtection()
                .str(0, 0, 2).atk(-10, -10, -10, 15, -5).def(60, 62, 60, 15, 60)
                .effect("Magic dragonfire protection with +2% Magic damage bonus and +15 Magic attack").build());

        addItem(EquipmentItem.builder(8850, "Rune defender", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.ATTACK, 40).req(Skill.DEFENCE, 40).untradeable().price(0).tier(3)
                .str(4, 0, 0).atk(19, 18, 17, -3, -2).def(19, 18, 17, -3, -2)
                .effect("Budget melee offhand: +4 Strength and +19 Attack bonuses for 0 GP").build());

        addItem(EquipmentItem.builder(1201, "Rune kiteshield", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).price(32_000).tier(2)
                .def(44, 48, 46, -1, 46).atk(-8, -8, -8, -12, -2)
                .effect("Budget physical tank shield with high melee defence").build());

        addItem(EquipmentItem.builder(12954, "Dragon defender", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).req(Skill.DEFENCE, 60).untradeable().price(0).tier(5)
                .str(6, 0, 0).atk(25, 24, 23, -3, -2).def(25, 24, 23, -3, -2)
                .effect("Best-in-slot budget melee offhand: +6 Strength and +25 Attack bonuses for 0 GP").build());

        addItem(EquipmentItem.builder(22322, "Avernic defender", EquipmentSlot.SHIELD, CombatStyle.MELEE)
                .req(Skill.ATTACK, 70).req(Skill.DEFENCE, 70).price(60_000_000).tier(9)
                .str(8, 0, 0).atk(30, 29, 28, -5, -4).def(30, 29, 28, -5, -4)
                .effect("Premier melee defender; +8 Melee Strength and +30 accuracy").build());

        addItem(EquipmentItem.builder(12610, "Book of law", EquipmentSlot.SHIELD, CombatStyle.RANGED)
                .questReq("Horror from the Deep").price(50_000).tier(3)
                .atk(0, 0, 0, 0, 10).prayer(5)
                .effect("Lightweight ranged offhand with +10 Ranged attack and +5 Prayer bonus").build());

        addItem(EquipmentItem.builder(12612, "Book of darkness", EquipmentSlot.SHIELD, CombatStyle.MAGIC)
                .questReq("Horror from the Deep").price(50_000).tier(3)
                .atk(0, 0, 0, 10, 0).prayer(5)
                .effect("Budget magic offhand with +10 Magic attack and +5 Prayer bonus").build());

        addItem(EquipmentItem.builder(11924, "Odium ward", EquipmentSlot.SHIELD, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 60).price(1_500_000).tier(6)
                .atk(0, 0, 0, -10, 12).def(36, 38, 42, 0, 38)
                .effect("Mid-tier offensive ranged shield with +12 Ranged attack").build());

        addItem(EquipmentItem.builder(21000, "Twisted buckler", EquipmentSlot.SHIELD, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).req(Skill.DEFENCE, 75).price(18_000_000).tier(8)
                .str(0, 10, 0).atk(0, 0, 0, -10, 26).def(18, 22, 20, 26, 24)
                .effect("Best-in-slot Ranged offhand with +10 Ranged Strength and +26 Ranged attack").build());

        addItem(EquipmentItem.builder(6889, "Mage's book", EquipmentSlot.SHIELD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 60).price(3_000_000).tier(5)
                .atk(0, 0, 0, 15, 0).def(0, 0, 0, 15, 0)
                .effect("High magic accuracy offhand with +15 Magic attack and Magic defence").build());

        addItem(EquipmentItem.builder(11926, "Malediction ward", EquipmentSlot.SHIELD, CombatStyle.MAGIC)
                .req(Skill.DEFENCE, 60).price(1_200_000).tier(6)
                .atk(0, 0, 0, 12, 0).def(36, 38, 42, 10, 38)
                .effect("Offensive magic shield with +12 Magic attack").build());

        addItem(EquipmentItem.builder(25985, "Elidinis' ward", EquipmentSlot.SHIELD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 80).req(Skill.DEFENCE, 80).price(4_000_000).tier(7)
                .str(0, 0, 3).atk(0, 0, 0, 25, 0).def(53, 55, 73, 2, 52).prayer(1)
                .effect("+3% Magic damage bonus and +25 Magic attack").build());

        // ==========================================
        // 5. HELMETS
        // ==========================================
        addItem(EquipmentItem.builder(1163, "Rune full helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).price(21_000).tier(1)
                .def(30, 32, 27, -6, 30).atk(-3, -3, -3, -6, -2)
                .effect("Solid level 40 defence melee full helm").build());

        addItem(EquipmentItem.builder(3751, "Berserker helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 45).questReq("The Fremennik Trials").price(50_000).tier(2)
                .str(3, 0, 0).def(31, 29, 33, -4, 30)
                .effect("Fremennik trials reward providing +3 Melee Strength").build());

        addItem(EquipmentItem.builder(10828, "Helm of neitiznot", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 55).questReq("The Fremennik Isles").price(50_000).tier(4)
                .str(3, 0, 0).def(31, 29, 34, 3, 30).prayer(3)
                .effect("Premier hybrid/melee helm; +3 Strength, +3 Prayer and zero negative attack penalties").build());

        addItem(EquipmentItem.builder(24271, "Neitiznot faceguard", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).questReq("The Fremennik Exiles").price(18_000_000).tier(8)
                .str(6, 0, 0).def(36, 34, 38, 3, 34).prayer(3)
                .effect("Top-tier melee helm with +6 Melee Strength and +3 Prayer").build());

        addItem(EquipmentItem.builder(26382, "Torva full helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 80).req(Skill.STRENGTH, 80).price(300_000_000).tier(10)
                .str(8, 0, 0).def(68, 65, 71, -5, 65).prayer(1)
                .effect("Best-in-slot Melee helm with +8 Melee Strength and towering physical defence").build());

        addItem(EquipmentItem.builder(24419, "Inquisitor's great helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 30).req(Skill.STRENGTH, 70).price(50_000_000).tier(8)
                .str(4, 0, 0).atk(0, 0, 8, -5, -5).def(18, 14, 20, -2, 16)
                .effect("+8 Crush accuracy and +4 Strength; boosts crush damage with set").build());

        addItem(EquipmentItem.builder(12931, "Serpentine helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 75).price(4_500_000).tier(7).venomProtection()
                .str(5, 0, 0).atk(0, 0, 0, -5, -5).def(52, 57, 54, 0, 50)
                .effect("Grants complete immunity to poison and venom (Zulrah & Vorkath staple)").build());

        addItem(EquipmentItem.builder(11864, "Slayer helmet (i)", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 10).req(Skill.SLAYER, 55).untradeable().price(0).tier(8).slayerTaskBonus()
                .str(0, 0, 0).def(30, 32, 27, -1, 30)
                .effect("Slayer Task: Grants +15% damage & accuracy across all styles on assigned tasks").build());

        addItem(EquipmentItem.builder(4716, "Dharok's helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(1_200_000).tier(6)
                .def(45, 48, 44, -3, 51)
                .effect("Dharok set piece with high physical defense").build());

        addItem(EquipmentItem.builder(4753, "Verac's helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(1_200_000).tier(6)
                .def(55, 58, 54, 0, 56).prayer(3)
                .effect("High defensive helm with +3 Prayer bonus and no magic penalty").build());

        addItem(EquipmentItem.builder(11664, "Void ranger helm", EquipmentSlot.HEAD, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 42).req(Skill.RANGED, 42).untradeable().price(0).tier(7).voidKnight()
                .def(6, 6, 6, 6, 6)
                .effect("Completes Void Knight set: +10% Ranged accuracy and damage (+12.5% with Elite)").build());

        addItem(EquipmentItem.builder(11665, "Void melee helm", EquipmentSlot.HEAD, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 42).req(Skill.ATTACK, 42).req(Skill.STRENGTH, 42).untradeable().price(0).tier(7).voidKnight()
                .def(6, 6, 6, 6, 6)
                .effect("Completes Void Knight set: +10% Melee accuracy and damage").build());

        addItem(EquipmentItem.builder(11663, "Void mage helm", EquipmentSlot.HEAD, CombatStyle.MAGIC)
                .req(Skill.DEFENCE, 42).req(Skill.MAGIC, 42).untradeable().price(0).tier(7).voidKnight()
                .def(6, 6, 6, 6, 6)
                .effect("Completes Void Knight set: +45% Magic accuracy").build());

        addItem(EquipmentItem.builder(10400, "Blessed d'hide coif", EquipmentSlot.HEAD, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 40).price(50_000).tier(4)
                .atk(0, 0, 0, -5, 7).def(4, 5, 7, 6, 4).prayer(1)
                .effect("Cost-effective ranged coif with +7 Ranged attack and +1 Prayer").build());

        addItem(EquipmentItem.builder(23971, "Crystal helm", EquipmentSlot.HEAD, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 70).questReq("Song of the Elves").price(8_000_000).tier(7)
                .atk(0, 0, 0, -10, 10).def(14, 12, 16, 18, 14)
                .effect("Amplifies Bow of Faerdhinen damage by +2.5% and accuracy by +5%").build());

        addItem(EquipmentItem.builder(27235, "Masori mask (f)", EquipmentSlot.HEAD, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).req(Skill.DEFENCE, 80).price(45_000_000).tier(9)
                .str(0, 4, 0).atk(0, 0, 0, -4, 15).def(18, 16, 20, 25, 20)
                .effect("Best-in-slot Ranged helm with +4 Ranged Strength and +15 Ranged attack").build());

        addItem(EquipmentItem.builder(4089, "Mystic hat", EquipmentSlot.HEAD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 40).req(Skill.DEFENCE, 20).price(15_000).tier(2)
                .atk(0, 0, 0, 9, 0).def(0, 0, 0, 9, 0)
                .effect("Affordable early magic hat with +9 Magic attack").build());

        addItem(EquipmentItem.builder(4708, "Ahrim's hood", EquipmentSlot.HEAD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 70).req(Skill.DEFENCE, 70).price(1_500_000).tier(5)
                .atk(0, 0, 0, 15, -7).def(12, 11, 15, 15, 0)
                .effect("Solid magic accuracy hood with +15 Magic attack").build());

        addItem(EquipmentItem.builder(28319, "Virtus mask", EquipmentSlot.HEAD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).questReq("Desert Treasure II").price(20_000_000).tier(8)
                .str(0, 0, 3).atk(0, 0, 0, 12, 0).def(15, 12, 18, 15, 0).prayer(1)
                .effect("+3% Magic damage bonus (+4% on Ancient Magicks)").build());

        addItem(EquipmentItem.builder(21018, "Ancestral hat", EquipmentSlot.HEAD, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(85_000_000).tier(9)
                .str(0, 0, 2).atk(0, 0, 0, 15, 0).def(0, 0, 0, 5, 0)
                .effect("+2% Magic damage bonus and +15 Magic attack").build());

        // ==========================================
        // 6. BODY & LEGS
        // ==========================================
        addItem(EquipmentItem.builder(1127, "Rune platebody", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).questReq("Dragon Slayer I").price(40_000).tier(1)
                .def(82, 80, 72, -6, 80).atk(-30, -30, -30, -30, -10)
                .effect("Reliable level 40 tank chestplate").build());

        addItem(EquipmentItem.builder(1079, "Rune platelegs", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).price(35_000).tier(1)
                .def(51, 49, 47, -4, 49).atk(-21, -21, -21, -21, -7)
                .effect("Reliable level 40 tank platelegs").build());

        addItem(EquipmentItem.builder(10551, "Fighter torso", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).untradeable().price(0).tier(5)
                .str(4, 0, 0).def(62, 85, 62, 0, 62)
                .effect("Barbarian Assault: +4 Melee Strength matching Bandos chestplate for 0 GP").build());

        addItem(EquipmentItem.builder(4749, "Torag's platelegs", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(600_000).tier(5)
                .def(85, 82, 83, -7, 84).atk(-21, -21, -21, -21, -7)
                .effect("High-defense barrows tank platelegs").build());

        addItem(EquipmentItem.builder(4720, "Dharok's platebody", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(2_000_000).tier(6)
                .def(122, 120, 115, -30, 120).atk(-30, -30, -30, -30, -10)
                .effect("Dharok set body with heavy physical tank armor").build());

        addItem(EquipmentItem.builder(4722, "Dharok's platelegs", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(1_800_000).tier(6)
                .def(85, 82, 83, -7, 84).atk(-21, -21, -21, -21, -7)
                .effect("Dharok set platelegs with top-tier physical defense").build());

        addItem(EquipmentItem.builder(4757, "Verac's brassard", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(1_500_000).tier(6)
                .def(82, 80, 72, 0, 80).prayer(5)
                .effect("+5 Prayer bonus and strong defense with no magic penalty").build());

        addItem(EquipmentItem.builder(4759, "Verac's plateskirt", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 70).price(1_200_000).tier(6)
                .def(68, 65, 67, 0, 65).prayer(4)
                .effect("+4 Prayer bonus and high defense with no magic penalty").build());

        addItem(EquipmentItem.builder(8839, "Void knight top", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 42).untradeable().price(0).tier(7).voidKnight()
                .def(45, 45, 45, 45, 45)
                .effect("Pest Control: Part of Void Knight armor set").build());

        addItem(EquipmentItem.builder(8840, "Void knight robe", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 42).untradeable().price(0).tier(7).voidKnight()
                .def(30, 30, 30, 30, 30)
                .effect("Pest Control: Part of Void Knight armor set").build());

        addItem(EquipmentItem.builder(13072, "Elite void top", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 42).questReq("Western Provinces Hard Diary").untradeable().price(0).tier(9).voidKnight()
                .def(45, 45, 45, 45, 45).prayer(3)
                .effect("+2.5% damage boost to Ranged and Magic (+12.5% total) with +3 Prayer (Vorkath BiS)").build());

        addItem(EquipmentItem.builder(13073, "Elite void robe", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.DEFENCE, 42).questReq("Western Provinces Hard Diary").untradeable().price(0).tier(9).voidKnight()
                .def(30, 30, 30, 30, 30).prayer(3)
                .effect("+2.5% damage boost to Ranged and Magic (+12.5% total) with +3 Prayer (Vorkath BiS)").build());

        addItem(EquipmentItem.builder(11832, "Bandos chestplate", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 65).req(Skill.STRENGTH, 65).price(28_000_000).tier(8)
                .str(4, 0, 0).def(98, 93, 105, -6, 133).prayer(1)
                .effect("+4 Melee Strength, high defence, and +1 Prayer").build());

        addItem(EquipmentItem.builder(11834, "Bandos tassets", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 65).req(Skill.STRENGTH, 65).price(17_000_000).tier(8)
                .str(2, 0, 0).def(71, 63, 66, -4, 93).prayer(1)
                .effect("+2 Melee Strength and excellent physical defense").build());

        addItem(EquipmentItem.builder(26384, "Torva platebody", EquipmentSlot.BODY, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 80).req(Skill.STRENGTH, 80).price(400_000_000).tier(10)
                .str(6, 0, 0).def(135, 125, 145, -8, 140).prayer(1)
                .effect("Best-in-slot Melee body with +6 Melee Strength and massive defense").build());

        addItem(EquipmentItem.builder(26386, "Torva platelegs", EquipmentSlot.LEGS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 80).req(Skill.STRENGTH, 80).price(350_000_000).tier(10)
                .str(4, 0, 0).def(115, 105, 125, -6, 120).prayer(1)
                .effect("Best-in-slot Melee legs with +4 Melee Strength and massive defense").build());

        addItem(EquipmentItem.builder(2503, "Black d'hide body", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 40).price(10_000).tier(3)
                .atk(0, 0, 0, -15, 30).def(45, 15, 45, 50, 45)
                .effect("Exceptional budget ranged and magic defence armor").build());

        addItem(EquipmentItem.builder(2497, "Black d'hide chaps", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).price(5_000).tier(3)
                .atk(0, 0, 0, -10, 17).def(26, 8, 26, 30, 26)
                .effect("Budget ranged chaps with +17 Ranged attack").build());

        addItem(EquipmentItem.builder(10386, "Blessed d'hide body", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 40).price(900_000).tier(5)
                .atk(0, 0, 0, -15, 30).def(45, 15, 45, 50, 45).prayer(1)
                .effect("+30 Ranged attack and +1 Prayer bonus").build());

        addItem(EquipmentItem.builder(10388, "Blessed d'hide chaps", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).price(500_000).tier(5)
                .atk(0, 0, 0, -10, 17).def(26, 8, 26, 30, 26).prayer(1)
                .effect("+17 Ranged attack and +1 Prayer bonus").build());

        addItem(EquipmentItem.builder(23975, "Crystal body", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 70).questReq("Song of the Elves").price(20_000_000).tier(8)
                .atk(0, 0, 0, -10, 36).def(62, 54, 70, 72, 60).prayer(1)
                .effect("Boosts Bow of Faerdhinen damage by +7.5% and accuracy by +15%").build());

        addItem(EquipmentItem.builder(23979, "Crystal legs", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 70).questReq("Song of the Elves").price(15_000_000).tier(8)
                .atk(0, 0, 0, -10, 24).def(42, 36, 48, 50, 42).prayer(1)
                .effect("Boosts Bow of Faerdhinen damage by +5.0% and accuracy by +10%").build());

        addItem(EquipmentItem.builder(27238, "Masori body (f)", EquipmentSlot.BODY, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).req(Skill.DEFENCE, 80).price(90_000_000).tier(10)
                .str(0, 4, 0).atk(0, 0, 0, -7, 43).def(72, 59, 81, 95, 75)
                .effect("Best-in-slot Ranged body with +4 Ranged Strength and +43 Ranged attack").build());

        addItem(EquipmentItem.builder(27241, "Masori chaps (f)", EquipmentSlot.LEGS, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).req(Skill.DEFENCE, 80).price(80_000_000).tier(10)
                .str(0, 2, 0).atk(0, 0, 0, -4, 27).def(46, 38, 52, 60, 48)
                .effect("Best-in-slot Ranged chaps with +2 Ranged Strength and +27 Ranged attack").build());

        addItem(EquipmentItem.builder(4091, "Mystic robe top", EquipmentSlot.BODY, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 40).req(Skill.DEFENCE, 20).price(75_000).tier(2)
                .atk(0, 0, 0, 20, 0).def(0, 0, 0, 20, 0)
                .effect("Budget magic robe top with +20 Magic attack").build());

        addItem(EquipmentItem.builder(4093, "Mystic robe bottom", EquipmentSlot.LEGS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 40).req(Skill.DEFENCE, 20).price(45_000).tier(2)
                .atk(0, 0, 0, 15, 0).def(0, 0, 0, 15, 0)
                .effect("Budget magic robe bottom with +15 Magic attack").build());

        addItem(EquipmentItem.builder(4712, "Ahrim's robetop", EquipmentSlot.BODY, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 70).req(Skill.DEFENCE, 70).price(2_800_000).tier(6)
                .atk(0, 0, 0, 30, -10).def(52, 37, 65, 30, 0)
                .effect("High magic accuracy and physical defense; PvM staple").build());

        addItem(EquipmentItem.builder(4714, "Ahrim's robeskirt", EquipmentSlot.LEGS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 70).req(Skill.DEFENCE, 70).price(1_700_000).tier(6)
                .atk(0, 0, 0, 22, -7).def(37, 26, 46, 22, 0)
                .effect("High magic accuracy skirt with +22 Magic attack").build());

        addItem(EquipmentItem.builder(28323, "Virtus robe top", EquipmentSlot.BODY, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(45_000_000).tier(8)
                .str(0, 0, 4).atk(0, 0, 0, 32, 0).def(45, 30, 55, 25, 0).prayer(1)
                .effect("+4% Magic damage bonus (+7% on Ancient Magicks)").build());

        addItem(EquipmentItem.builder(28325, "Virtus robe bottom", EquipmentSlot.LEGS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(40_000_000).tier(8)
                .str(0, 0, 3).atk(0, 0, 0, 24, 0).def(32, 22, 40, 18, 0).prayer(1)
                .effect("+3% Magic damage bonus (+6% on Ancient Magicks)").build());

        addItem(EquipmentItem.builder(21021, "Ancestral robe top", EquipmentSlot.BODY, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(180_000_000).tier(9)
                .str(0, 0, 2).atk(0, 0, 0, 35, 0).def(0, 0, 0, 12, 0)
                .effect("+2% Magic damage bonus and +35 Magic attack (Shadow amplifier)").build());

        addItem(EquipmentItem.builder(21024, "Ancestral robe bottom", EquipmentSlot.LEGS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(130_000_000).tier(9)
                .str(0, 0, 2).atk(0, 0, 0, 26, 0).def(0, 0, 0, 8, 0)
                .effect("+2% Magic damage bonus and +26 Magic attack (Shadow amplifier)").build());

        // ==========================================
        // 7. AMULETS & NECKLACES
        // ==========================================
        addItem(EquipmentItem.builder(1704, "Amulet of glory", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .price(15_000).tier(2)
                .str(6, 0, 0).atk(10, 10, 10, 10, 10).def(3, 3, 3, 3, 3).prayer(3)
                .effect("Solid all-around budget amulet: +10 accuracy in all styles and +6 Strength").build());

        addItem(EquipmentItem.builder(1725, "Amulet of strength", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .price(2_000).tier(1)
                .str(10, 0, 0)
                .effect("Max hit specialist: +10 Melee Strength for low budget").build());

        addItem(EquipmentItem.builder(6585, "Amulet of fury", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .price(1_800_000).tier(5)
                .str(8, 0, 0).atk(10, 10, 10, 10, 10).def(15, 15, 15, 15, 15).prayer(5)
                .effect("Premier balanced amulet: +8 Strength, +15 all defence, and +5 Prayer").build());

        addItem(EquipmentItem.builder(24780, "Amulet of blood fury", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .price(16_000_000).tier(9)
                .str(8, 0, 0).atk(10, 10, 10, 10, 10).def(15, 15, 15, 15, 15).prayer(5)
                .effect("20% chance on melee hit to heal 30% of damage dealt (Nex, Vardorvis, Cerberus lifesaver)").build());

        addItem(EquipmentItem.builder(19553, "Amulet of torture", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .req(Skill.HITPOINTS, 75).price(12_000_000).tier(8)
                .str(10, 0, 0).atk(15, 15, 15, 0, 0).prayer(2)
                .effect("Best-in-slot offensive melee amulet with +10 Melee Strength and +15 Attack").build());

        addItem(EquipmentItem.builder(19547, "Necklace of anguish", EquipmentSlot.AMULET, CombatStyle.RANGED)
                .req(Skill.HITPOINTS, 75).price(12_000_000).tier(8)
                .str(0, 5, 0).atk(0, 0, 0, 0, 15).prayer(2)
                .effect("Best-in-slot Ranged necklace: +5 Ranged Strength and +15 Ranged Attack").build());

        addItem(EquipmentItem.builder(12002, "Occult necklace", EquipmentSlot.AMULET, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 70).price(700_000).tier(6)
                .str(0, 0, 5).atk(0, 0, 0, 12, 0).prayer(2)
                .effect("High-value magic amplifier: +5% Magic damage boost for only 700K GP").build());

        addItem(EquipmentItem.builder(12018, "Salve amulet (ei)", EquipmentSlot.AMULET, CombatStyle.MELEE)
                .questReq("Haunted Mine").untradeable().price(0).tier(9).undeadBonus()
                .str(0, 0, 0).atk(0, 0, 0, 0, 0)
                .effect("Undead: Grants +20% damage and +20% accuracy in Melee, Ranged, and Magic vs undead (Vorkath, Calvar'ion, Vet'ion)").build());

        // ==========================================
        // 8. GLOVES
        // ==========================================
        addItem(EquipmentItem.builder(11118, "Combat bracelet", EquipmentSlot.GLOVES, CombatStyle.MELEE)
                .price(15_000).tier(2)
                .str(6, 0, 0).atk(7, 7, 7, 3, 7).def(5, 5, 5, 0, 0)
                .effect("Budget offensive glove with zero quest requirements").build());

        addItem(EquipmentItem.builder(8842, "Void knight gloves", EquipmentSlot.GLOVES, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 42).untradeable().price(0).tier(7).voidKnight()
                .def(6, 6, 6, 6, 6)
                .effect("Pest Control: Essential piece for all Void Knight combat sets").build());

        addItem(EquipmentItem.builder(7462, "Barrows gloves", EquipmentSlot.GLOVES, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 40).questReq("Recipe for Disaster").untradeable().price(130_000).tier(7)
                .str(12, 0, 0).atk(12, 12, 12, 6, 12).def(12, 12, 12, 6, 12)
                .effect("Best value gloves in OSRS: +12 Strength and +12 all defense for Melee, Ranged, and Magic").build());

        addItem(EquipmentItem.builder(22981, "Ferocious gloves", EquipmentSlot.GLOVES, CombatStyle.MELEE)
                .req(Skill.ATTACK, 80).req(Skill.DEFENCE, 80).questReq("Dragon Slayer II").price(5_000_000).tier(9)
                .str(14, 0, 0).atk(16, 16, 16, -16, -16)
                .effect("Best-in-slot Melee gloves with +14 Melee Strength").build());

        addItem(EquipmentItem.builder(10382, "Blessed d'hide vambraces", EquipmentSlot.GLOVES, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 40).price(150_000).tier(5)
                .atk(0, 0, 0, -10, 11).def(6, 5, 7, 8, 6).prayer(1)
                .effect("+11 Ranged attack and +1 Prayer bonus").build());

        addItem(EquipmentItem.builder(26227, "Zaryte vambraces", EquipmentSlot.GLOVES, CombatStyle.RANGED)
                .req(Skill.RANGED, 80).req(Skill.DEFENCE, 45).price(180_000_000).tier(10)
                .str(0, 2, 0).atk(0, 0, 0, -10, 18).prayer(2)
                .effect("Best-in-slot Ranged gloves: +2 Ranged Strength and +18 Ranged attack").build());

        addItem(EquipmentItem.builder(19544, "Tormented bracelet", EquipmentSlot.GLOVES, CombatStyle.MAGIC)
                .req(Skill.HITPOINTS, 75).price(11_000_000).tier(8)
                .str(0, 0, 5).atk(0, 0, 0, 10, 0)
                .effect("Best-in-slot Magic bracelet with +5% Magic damage bonus").build());

        // ==========================================
        // 9. BOOTS
        // ==========================================
        addItem(EquipmentItem.builder(3105, "Climbing boots", EquipmentSlot.BOOTS, CombatStyle.MELEE)
                .price(1_000).tier(1)
                .str(2, 0, 0)
                .effect("Cheap +2 Strength boots for entry-level PvM").build());

        addItem(EquipmentItem.builder(22951, "Boots of stone", EquipmentSlot.BOOTS, CombatStyle.MELEE)
                .req(Skill.SLAYER, 1).price(1_000).tier(2).bootsOfStone()
                .def(5, 5, 5, 0, 0)
                .effect("Protects against burning ground in Mount Karuulm (Alchemical Hydra, Drakes, Wyrms)").build());

        addItem(EquipmentItem.builder(22954, "Boots of brimstone", EquipmentSlot.BOOTS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 44).req(Skill.MAGIC, 44).req(Skill.RANGED, 44).price(2_500_000).tier(6).bootsOfStone()
                .str(2, 0, 0).atk(3, 3, 3, 3, 3).def(7, 7, 7, 5, 5)
                .effect("Hybrid boots providing +3 all attack, +2 Melee Strength, and Karuulm heat protection").build());

        addItem(EquipmentItem.builder(11840, "Dragon boots", EquipmentSlot.BOOTS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 60).price(150_000).tier(4)
                .str(4, 0, 0).def(16, 17, 18, -3, 0)
                .effect("Peak value melee boots: +4 Strength for 150K GP").build());

        addItem(EquipmentItem.builder(13239, "Primordial boots", EquipmentSlot.BOOTS, CombatStyle.MELEE)
                .req(Skill.DEFENCE, 75).req(Skill.STRENGTH, 75).price(28_000_000).tier(8)
                .str(5, 0, 0).atk(2, 2, 2, -4, -1).def(22, 23, 24, -1, 0)
                .effect("Best-in-slot Melee boots with +5 Strength").build());

        addItem(EquipmentItem.builder(6328, "Snakehide boots", EquipmentSlot.BOOTS, CombatStyle.RANGED)
                .req(Skill.RANGED, 30).req(Skill.DEFENCE, 30).price(1_500).tier(1)
                .atk(0, 0, 0, -10, 3).def(2, 2, 2, 3, 2)
                .effect("Budget level 30 ranged boots with +3 Ranged attack").build());

        addItem(EquipmentItem.builder(10390, "Blessed d'hide boots", EquipmentSlot.BOOTS, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).req(Skill.DEFENCE, 40).price(500_000).tier(5)
                .atk(0, 0, 0, -10, 7).def(5, 5, 7, 7, 5).prayer(1)
                .effect("Best value ranged boots with +7 Ranged attack and +1 Prayer").build());

        addItem(EquipmentItem.builder(13237, "Pegasian boots", EquipmentSlot.BOOTS, CombatStyle.RANGED)
                .req(Skill.RANGED, 75).req(Skill.DEFENCE, 75).price(35_000_000).tier(8)
                .atk(0, 0, 0, -12, 12).def(5, 5, 7, 7, 5)
                .effect("Best-in-slot Ranged boots with +12 Ranged attack").build());

        addItem(EquipmentItem.builder(4097, "Mystic boots", EquipmentSlot.BOOTS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 40).req(Skill.DEFENCE, 20).price(15_000).tier(2)
                .atk(0, 0, 0, 4, 0).def(0, 0, 0, 4, 0)
                .effect("Cheap magic boots with +4 Magic attack").build());

        addItem(EquipmentItem.builder(6920, "Infinity boots", EquipmentSlot.BOOTS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 25).req(Skill.DEFENCE, 25).price(1_200_000).tier(5)
                .atk(0, 0, 0, 5, 0).def(0, 0, 0, 5, 0)
                .effect("Solid mid-tier magic boots with +5 Magic attack and defence").build());

        addItem(EquipmentItem.builder(13235, "Eternal boots", EquipmentSlot.BOOTS, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).req(Skill.DEFENCE, 75).price(6_000_000).tier(7)
                .atk(0, 0, 0, 8, 0).def(5, 5, 5, 8, 0)
                .effect("+8 Magic attack and solid magic defense").build());

        // ==========================================
        // 10. RINGS
        // ==========================================
        addItem(EquipmentItem.builder(2552, "Ring of dueling", EquipmentSlot.RING, CombatStyle.MELEE)
                .price(1_500).tier(1)
                .effect("Budget teleport utility ring").build());

        addItem(EquipmentItem.builder(6737, "Berserker ring (i)", EquipmentSlot.RING, CombatStyle.MELEE)
                .price(2_500_000).tier(6)
                .str(8, 0, 0)
                .effect("Imbued: Massive +8 Melee Strength; single highest DPS melee ring upgrade").build());

        addItem(EquipmentItem.builder(6733, "Archers ring (i)", EquipmentSlot.RING, CombatStyle.RANGED)
                .price(2_000_000).tier(6)
                .atk(0, 0, 0, 0, 8).def(0, 0, 0, 0, 8)
                .effect("Imbued: Grants +8 Ranged attack and defence").build());

        addItem(EquipmentItem.builder(6731, "Seers ring (i)", EquipmentSlot.RING, CombatStyle.MAGIC)
                .price(1_500_000).tier(5)
                .atk(0, 0, 0, 8, 0).def(0, 0, 0, 8, 0)
                .effect("Imbued: Grants +8 Magic attack and defence").build());

        addItem(EquipmentItem.builder(22975, "Brimstone ring", EquipmentSlot.RING, CombatStyle.MELEE)
                .price(3_000_000).tier(7)
                .str(4, 0, 0).atk(6, 6, 6, 6, 6).def(4, 4, 4, 4, 4)
                .effect("Hybrid ring providing +6 in all combat styles and lowers opponent magic defense").build());

        addItem(EquipmentItem.builder(19710, "Ring of suffering (i)", EquipmentSlot.RING, CombatStyle.MELEE)
                .req(Skill.HITPOINTS, 75).price(12_000_000).tier(8)
                .def(20, 20, 20, 20, 20).prayer(4)
                .effect("Stores recoil charges indefinitely with +20 all defence (Zulrah staple)").build());

        addItem(EquipmentItem.builder(25975, "Lightbearer", EquipmentSlot.RING, CombatStyle.MELEE)
                .price(1_500_000).tier(7)
                .effect("Doubles special attack regeneration rate from 10% per 30s to 10% per 15s (ToA & Bossing spec staple)").build());

        addItem(EquipmentItem.builder(28307, "Ultor ring", EquipmentSlot.RING, CombatStyle.MELEE)
                .req(Skill.MAGIC, 80).req(Skill.DEFENCE, 80).price(140_000_000).tier(10)
                .str(12, 0, 0).def(0, 0, 0, 0, 0)
                .effect("Best-in-slot Melee ring: +12 Melee Strength").build());

        addItem(EquipmentItem.builder(28310, "Bellator ring", EquipmentSlot.RING, CombatStyle.MELEE)
                .req(Skill.MAGIC, 80).req(Skill.DEFENCE, 80).questReq("Desert Treasure II").price(65_000_000).tier(10)
                .str(6, 0, 0).atk(0, 20, 0, 0, 0)
                .effect("Best-in-slot Slash ring: +20 Slash attack and +6 Strength (Vardorvis & Duke BiS)").build());

        addItem(EquipmentItem.builder(28313, "Venator ring", EquipmentSlot.RING, CombatStyle.RANGED)
                .req(Skill.MAGIC, 80).req(Skill.DEFENCE, 80).price(45_000_000).tier(9)
                .str(0, 2, 0).atk(0, 0, 0, 0, 10)
                .effect("Best-in-slot Ranged ring: +2 Ranged Strength and +10 Ranged attack").build());

        addItem(EquipmentItem.builder(28316, "Magus ring", EquipmentSlot.RING, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 80).req(Skill.DEFENCE, 80).questReq("Desert Treasure II").price(55_000_000).tier(10)
                .str(0, 0, 2).atk(0, 0, 0, 15, 0)
                .effect("Best-in-slot Magic ring: +2% Magic damage bonus and +15 Magic attack (Whisperer & Shadow BiS)").build());

        // ==========================================
        // 11. CAPES
        // ==========================================
        addItem(EquipmentItem.builder(6568, "Obsidian cape", EquipmentSlot.CAPE, CombatStyle.MELEE)
                .price(400_000).tier(2)
                .def(9, 9, 9, 9, 9)
                .effect("Budget defensive cape with +9 in all defensive stats").build());

        addItem(EquipmentItem.builder(6570, "Fire cape", EquipmentSlot.CAPE, CombatStyle.MELEE)
                .untradeable().price(0).tier(6)
                .str(4, 0, 0).atk(1, 1, 1, 1, 1).def(11, 11, 11, 11, 11).prayer(2)
                .effect("Best value melee cape: +4 Melee Strength and +11 defence for 0 GP").build());

        addItem(EquipmentItem.builder(21295, "Infernal cape", EquipmentSlot.CAPE, CombatStyle.MELEE)
                .untradeable().price(0).tier(10)
                .str(8, 0, 0).atk(4, 4, 4, 1, 1).def(12, 12, 12, 12, 12).prayer(2)
                .effect("Pinnacle melee cape: +8 Melee Strength and +4 attack").build());

        addItem(EquipmentItem.builder(10499, "Ava's accumulator", EquipmentSlot.CAPE, CombatStyle.RANGED)
                .req(Skill.RANGED, 50).questReq("Animal Magnetism").untradeable().price(0).tier(4)
                .atk(0, 0, 0, 0, 4)
                .effect("Retrieves 72% of fired ammunition and gives +4 Ranged attack").build());

        addItem(EquipmentItem.builder(22109, "Ava's assembler", EquipmentSlot.CAPE, CombatStyle.RANGED)
                .req(Skill.RANGED, 70).questReq("Dragon Slayer II").untradeable().price(0).tier(8)
                .str(0, 2, 0).atk(0, 0, 0, 0, 8)
                .effect("Retrieves 80% of ammunition and grants +2 Ranged Strength (Vorkath drop)").build());

        addItem(EquipmentItem.builder(21795, "Imbued saradomin cape", EquipmentSlot.CAPE, CombatStyle.MAGIC)
                .req(Skill.MAGIC, 75).untradeable().price(0).tier(7)
                .str(0, 0, 2).atk(0, 0, 0, 15, 0).def(0, 0, 0, 3, 0)
                .effect("+2% Magic damage bonus earned from Mage Arena II").build());

        // ==========================================
        // 12. AMMUNITION (Bolts, Arrows, Blessings)
        // ==========================================
        addItem(EquipmentItem.builder(11875, "Broad bolts", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 55).req(Skill.SLAYER, 55).price(60).tier(3).ammoType(AmmoType.BOLT)
                .str(0, 100, 0)
                .effect("Crossbow Ammo: Reliable high-strength budget bolts (Kurask & Turoth capable)").build());

        addItem(EquipmentItem.builder(9242, "Ruby bolts (e)", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 61).price(250).tier(4).ammoType(AmmoType.BOLT)
                .str(0, 103, 0)
                .effect("Crossbow Ammo: Blood Forfeit proc deals 20% of boss current HP (up to 100 damage!)").build());

        addItem(EquipmentItem.builder(9244, "Diamond bolts (e)", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 61).price(300).tier(4).ammoType(AmmoType.BOLT)
                .str(0, 105, 0)
                .effect("Crossbow Ammo: Armour Piercing proc ignores 100% of opponent defense").build());

        addItem(EquipmentItem.builder(21944, "Ruby dragon bolts (e)", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 64).price(1_200).tier(8).ammoType(AmmoType.BOLT)
                .str(0, 122, 0)
                .effect("Dragon Crossbow Ammo: +122 Ranged Strength; Blood Forfeit hits up to 100 damage (Vorkath & Nex staple)").build());

        addItem(EquipmentItem.builder(21946, "Diamond dragon bolts (e)", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 64).price(1_200).tier(8).ammoType(AmmoType.BOLT)
                .str(0, 122, 0)
                .effect("Dragon Crossbow Ammo: +122 Ranged Strength; Armour Piercing ignores defense").build());

        addItem(EquipmentItem.builder(21938, "Sapphire dragon bolts (e)", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 64).price(1_200).tier(8).ammoType(AmmoType.BOLT)
                .str(0, 122, 0)
                .effect("Dragon Crossbow Ammo: Clear Mind proc drains prayer and depletes Muspah prayer shield instantly").build());

        addItem(EquipmentItem.builder(892, "Rune arrow", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 40).price(80).tier(3).ammoType(AmmoType.ARROW)
                .str(0, 49, 0)
                .effect("Bow Ammo: Standard ammunition for Magic Shortbow and bows").build());

        addItem(EquipmentItem.builder(21326, "Amethyst arrow", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 50).price(250).tier(6).ammoType(AmmoType.ARROW)
                .str(0, 55, 0)
                .effect("Bow Ammo: High-tier +55 Ranged Strength arrows for bows and Twisted Bow").build());

        addItem(EquipmentItem.builder(11212, "Dragon arrow", EquipmentSlot.AMMO, CombatStyle.RANGED)
                .req(Skill.RANGED, 60).price(700).tier(8).ammoType(AmmoType.ARROW)
                .str(0, 60, 0)
                .effect("Bow Ammo: Best-in-slot +60 Ranged Strength arrows for Twisted Bow").build());

        addItem(EquipmentItem.builder(20220, "Holy blessing", EquipmentSlot.AMMO, CombatStyle.MELEE)
                .price(15_000).tier(3).ammoType(AmmoType.BLESSING)
                .prayer(1)
                .effect("Equips in ammo slot for non-ammo weapons to provide +1 Prayer bonus").build());

        addItem(EquipmentItem.builder(20223, "War blessing", EquipmentSlot.AMMO, CombatStyle.MELEE)
                .price(15_000).tier(3).ammoType(AmmoType.BLESSING)
                .prayer(1)
                .effect("Equips in ammo slot for non-ammo weapons to provide +1 Prayer bonus").build());

        // ==========================================
        // 13. SPECIAL ATTACK WEAPONS
        // ==========================================
        addItem(EquipmentItem.builder(5698, "Dragon dagger (p++)", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).questReq("Lost City").price(25_000).tier(4).attackType(AttackType.STAB)
                .str(40, 0, 0).atk(40, 25, -4, 1, 0)
                .effect("25% spec cost; fires 2 rapid strikes with +15% accuracy and +15% damage").build());

        addItem(EquipmentItem.builder(11804, "Bandos godsword", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(15_000_000).tier(8).twoHanded().attackType(AttackType.SLASH)
                .str(132, 0, 0).atk(-4, 0, 132, 0, 0).prayer(8)
                .effect("50% spec cost; reduces opponent Defence by the exact damage dealt (boss defense breaker)").build());

        addItem(EquipmentItem.builder(13576, "Dragon warhammer", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).req(Skill.STRENGTH, 60).price(25_000_000).tier(8).attackType(AttackType.CRUSH)
                .str(85, 0, 0).atk(-4, -4, 95, 0, 0)
                .effect("50% spec cost; reduces opponent Defence by a massive flat 30% on hit").build());

        addItem(EquipmentItem.builder(27690, "Voidwaker", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(80_000_000).tier(9).attackType(AttackType.SLASH)
                .str(80, 0, 0).atk(80, 80, 0, 0, 0)
                .effect("50% spec cost; 100% guaranteed hit dealing 50-150% max hit as unblockable magic damage").build());

        addItem(EquipmentItem.builder(13652, "Dragon claws", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 60).price(90_000_000).tier(9).attackType(AttackType.SLASH)
                .str(56, 0, 0).atk(0, 57, 0, 0, 0)
                .effect("50% spec cost; unleashes 4 rapid devastating slash strikes for massive burst DPS").build());

        addItem(EquipmentItem.builder(26233, "Ancient godsword", EquipmentSlot.SPEC_WEAPON, CombatStyle.MELEE)
                .req(Skill.ATTACK, 75).price(30_000_000).tier(8).twoHanded().attackType(AttackType.SLASH)
                .str(132, 0, 0).atk(0, 132, 80, 0, 0).prayer(8)
                .effect("50% spec cost; deals 25 unblockable damage after 8 ticks and heals the player 25 HP").build());
    }

    public List<EquipmentItem> getItemsForSlot(EquipmentSlot slot) {
        return itemsBySlot.getOrDefault(slot, Collections.emptyList());
    }

    public EquipmentItem getItem(String name) {
        if (name == null) return null;
        EquipmentItem item = itemsByName.get(name.toLowerCase());
        if (item != null) return item;
        for (EquipmentItem i : allItems) {
            if (i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }

    public EquipmentItem getItemByName(String name) {
        return getItem(name);
    }

    public EquipmentItem getItem(int itemId) {
        return itemsById.get(itemId);
    }

    public List<EquipmentItem> getAllItems() {
        return Collections.unmodifiableList(allItems);
    }
}
