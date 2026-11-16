package com.bosswise;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.CombatCalculationService;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CombatCalculationServiceTest {
    private CombatCalculationService combatService;
    private MonsterDatabase monsterDatabase;
    private EquipmentDatabase equipmentDatabase;
    private PlayerAccount account;

    @Before
    public void setUp() {
        combatService = new CombatCalculationService();
        monsterDatabase = new MonsterDatabase();
        equipmentDatabase = new EquipmentDatabase();

        Map<Skill, Integer> stats = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) stats.put(s, 85);
        stats.put(Skill.ATTACK, 90);
        stats.put(Skill.STRENGTH, 90);
        stats.put(Skill.DEFENCE, 85);
        stats.put(Skill.RANGED, 90);
        stats.put(Skill.MAGIC, 90);
        stats.put(Skill.PRAYER, 77);
        stats.put(Skill.HITPOINTS, 90);
        stats.put(Skill.SLAYER, 85);

        account = new PlayerAccount("TestWarrior", 115, 1850, stats, stats, java.util.Collections.emptyMap(), java.util.Collections.emptySet(), true);
    }

    @Test
    public void testMeleeLowVsHighDefence() {
        MonsterData sandCrab = monsterDatabase.getMonster("sand_crab");
        MonsterData graardor = monsterDatabase.getMonster("general_graardor");

        Assert.assertNotNull(sandCrab);
        Assert.assertNotNull(graardor);

        Map<EquipmentSlot, EquipmentItem> meleeGear = new EnumMap<>(EquipmentSlot.class);
        meleeGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Abyssal whip"));
        meleeGear.put(EquipmentSlot.SHIELD, equipmentDatabase.getItem("Dragon defender"));
        meleeGear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Amulet of fury"));
        meleeGear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Fighter torso"));
        meleeGear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Dragon platelegs"));
        meleeGear.put(EquipmentSlot.BOOTS, equipmentDatabase.getItem("Dragon boots"));
        meleeGear.put(EquipmentSlot.GLOVES, equipmentDatabase.getItem("Barrows gloves"));
        meleeGear.put(EquipmentSlot.CAPE, equipmentDatabase.getItem("Fire cape"));

        CombatResult crabResult = combatService.calculateMelee(
                meleeGear, sandCrab.getPrimaryVariant(), sandCrab.getCombatProfile(), account, false
        );

        CombatResult graardorResult = combatService.calculateMelee(
                meleeGear, graardor.getPrimaryVariant(), graardor.getCombatProfile(), account, false
        );

        Assert.assertTrue("Crab hit chance should be > 95%", crabResult.getHitChance() > 0.95);
        Assert.assertTrue("Graardor hit chance should be lower than crab", graardorResult.getHitChance() < crabResult.getHitChance());
        Assert.assertTrue("Crab DPS must exceed Graardor DPS", crabResult.getDps() > graardorResult.getDps());
    }

    @Test
    public void testSlayerHelmetOnTaskBonus() {
        MonsterData gargoyle = monsterDatabase.getMonster("gargoyle");
        Assert.assertNotNull(gargoyle);

        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.HEAD, equipmentDatabase.getItem("Slayer helmet (i)"));
        gear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Abyssal bludgeon"));
        gear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Amulet of fury"));
        gear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Fighter torso"));
        gear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Torag's platelegs"));
        gear.put(EquipmentSlot.BOOTS, equipmentDatabase.getItem("Dragon boots"));
        gear.put(EquipmentSlot.GLOVES, equipmentDatabase.getItem("Barrows gloves"));
        gear.put(EquipmentSlot.CAPE, equipmentDatabase.getItem("Fire cape"));

        CombatResult offTask = combatService.calculateMelee(
                gear, gargoyle.getPrimaryVariant(), gargoyle.getCombatProfile(), account, false
        );

        CombatResult onTask = combatService.calculateMelee(
                gear, gargoyle.getPrimaryVariant(), gargoyle.getCombatProfile(), account, true
        );

        Assert.assertTrue("On-task max hit must exceed off-task max hit", onTask.getMaxHit() > offTask.getMaxHit());
        Assert.assertTrue("On-task DPS must be at least 15% higher than off-task DPS", onTask.getDps() >= offTask.getDps() * 1.15);
    }

    @Test
    public void testCorporealBeastSpearMechanic() {
        MonsterData corp = monsterDatabase.getMonster("corporeal_beast");
        Assert.assertNotNull(corp);

        Map<EquipmentSlot, EquipmentItem> spearGear = new EnumMap<>(EquipmentSlot.class);
        spearGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Zamorakian spear"));
        spearGear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Amulet of fury"));
        spearGear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Karil's leathertop"));
        spearGear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Karil's leatherskirt"));

        Map<EquipmentSlot, EquipmentItem> swordGear = new EnumMap<>(EquipmentSlot.class);
        swordGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Abyssal whip"));
        swordGear.put(EquipmentSlot.SHIELD, equipmentDatabase.getItem("Dragon defender"));
        swordGear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Amulet of fury"));
        swordGear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Karil's leathertop"));
        swordGear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Karil's leatherskirt"));

        CombatResult spearResult = combatService.calculateMelee(
                spearGear, corp.getPrimaryVariant(), corp.getCombatProfile(), account, false
        );

        CombatResult swordResult = combatService.calculateMelee(
                swordGear, corp.getPrimaryVariant(), corp.getCombatProfile(), account, false
        );

        // Corp takes 50% reduced damage from non-spears, so Zamorakian spear should have double expected efficiency
        Assert.assertTrue("Spear max hit should significantly exceed non-spear due to 50% reduction",
                spearResult.getMaxHit() > swordResult.getMaxHit());
    }

    @Test
    public void testDragonhunterCrossbowAgainstDragon() {
        MonsterData vorkath = monsterDatabase.getMonster("vorkath");
        Assert.assertNotNull(vorkath);

        Map<EquipmentSlot, EquipmentItem> dhcbGear = new EnumMap<>(EquipmentSlot.class);
        dhcbGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Dragon hunter crossbow"));
        dhcbGear.put(EquipmentSlot.SHIELD, equipmentDatabase.getItem("Dragonfire ward"));
        dhcbGear.put(EquipmentSlot.AMMO, equipmentDatabase.getItem("Ruby dragon bolts (e)"));
        dhcbGear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Necklace of anguish"));
        dhcbGear.put(EquipmentSlot.HEAD, equipmentDatabase.getItem("Blessed d'hide coif"));
        dhcbGear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Blessed d'hide body"));
        dhcbGear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Blessed d'hide chaps"));
        dhcbGear.put(EquipmentSlot.GLOVES, equipmentDatabase.getItem("Barrows gloves"));
        dhcbGear.put(EquipmentSlot.BOOTS, equipmentDatabase.getItem("Blessed d'hide boots"));
        dhcbGear.put(EquipmentSlot.CAPE, equipmentDatabase.getItem("Ava's assembler"));

        Map<EquipmentSlot, EquipmentItem> rcbGear = new EnumMap<>(EquipmentSlot.class);
        rcbGear.putAll(dhcbGear);
        rcbGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Rune crossbow"));

        CombatResult dhcbResult = combatService.calculateRanged(
                dhcbGear, vorkath.getPrimaryVariant(), vorkath.getCombatProfile(), account, false
        );

        CombatResult rcbResult = combatService.calculateRanged(
                rcbGear, vorkath.getPrimaryVariant(), vorkath.getCombatProfile(), account, false
        );

        Assert.assertTrue("DHCB accuracy roll must be higher than RCB due to +30% boost",
                dhcbResult.getAttackRoll() > rcbResult.getAttackRoll());
        Assert.assertTrue("DHCB DPS must be at least 30% higher than RCB",
                dhcbResult.getDps() > rcbResult.getDps() * 1.30);
    }

    @Test
    public void testElementalWeaknessMagicScaling() {
        MonsterData fireGiant = monsterDatabase.getMonster("fire_giant");
        Assert.assertNotNull(fireGiant);

        Map<EquipmentSlot, EquipmentItem> waterMageGear = new EnumMap<>(EquipmentSlot.class);
        waterMageGear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Staff of water"));
        waterMageGear.put(EquipmentSlot.AMULET, equipmentDatabase.getItem("Occult necklace"));
        waterMageGear.put(EquipmentSlot.BODY, equipmentDatabase.getItem("Mystic robe top"));
        waterMageGear.put(EquipmentSlot.LEGS, equipmentDatabase.getItem("Mystic robe bottom"));
        waterMageGear.put(EquipmentSlot.CAPE, equipmentDatabase.getItem("God cape"));

        CombatResult result = combatService.calculateMagic(
                waterMageGear, fireGiant.getPrimaryVariant(), fireGiant.getCombatProfile(), account, false
        );

        Assert.assertTrue("Water spell against Fire Giant should yield high hit chance (> 80%)",
                result.getHitChance() > 0.80);
        Assert.assertTrue("Water spell should deal substantial DPS (> 4.0)", result.getDps() > 4.0);
    }
}
