package com.bosswise;

import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.ElementalWeakness;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterClassification;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MonsterDatabaseTest {
    private MonsterDatabase db;

    @Before
    public void setUp() {
        db = new MonsterDatabase();
    }

    @Test
    public void testDatabaseLoadsComprehensiveMonsters() {
        Map<String, MonsterData> monsters = db.getAllMonsters();
        Assert.assertNotNull("Monster database should not be null", monsters);
        Assert.assertTrue("Should load at least 40 monsters, found: " + monsters.size(), monsters.size() >= 40);
    }

    @Test
    public void testVariantsIndexedCorrectly() {
        // Vorkath standard npcId 8061
        MonsterVariant vorkathVariant = db.getVariantByNpcId(8061);
        Assert.assertNotNull("Vorkath variant (8061) should be indexed", vorkathVariant);
        Assert.assertTrue(vorkathVariant.getVariantName().contains("Vorkath"));
        Assert.assertEquals(750, vorkathVariant.getHitpoints());
        Assert.assertEquals(732, vorkathVariant.getCombatLevel());

        // Sand crab npcId 7206
        MonsterVariant crabVariant = db.getVariantByNpcId(7206);
        Assert.assertNotNull("Sand Crab variant (7206) should be indexed", crabVariant);
        Assert.assertEquals(60, crabVariant.getHitpoints());
    }

    @Test
    public void testCategoryFiltering() {
        List<MonsterData> slayer = db.getSlayerMonsters();
        Assert.assertFalse("Slayer monsters should not be empty", slayer.isEmpty());

        List<MonsterData> wildy = db.getWildernessMonsters();
        Assert.assertFalse("Wilderness monsters should not be empty", wildy.isEmpty());

        List<MonsterData> raids = db.getMonstersByCategory(MonsterCategory.RAID);
        Assert.assertFalse("Raid monsters should not be empty", raids.isEmpty());

        List<MonsterData> regular = db.getMonstersByCategory(MonsterCategory.REGULAR);
        Assert.assertFalse("Regular monsters should not be empty", regular.isEmpty());
    }

    @Test
    public void testBossDataConversion() {
        MonsterData vorkath = db.getMonster("vorkath");
        Assert.assertNotNull("Vorkath monster data must exist", vorkath);

        BossData bossData = vorkath.toBossData();
        Assert.assertNotNull("Converted BossData should not be null", bossData);
        Assert.assertEquals("Vorkath", bossData.getName());
        Assert.assertEquals(CombatStyle.RANGED, bossData.getPrimaryStyle());
        Assert.assertTrue("Vorkath weakness should mention dragon", bossData.getWeakness().toLowerCase().contains("dragon"));
    }

    @Test
    public void testProjectRebalanceElementalWeaknesses() {
        MonsterData fireGiant = db.getMonster("fire_giant");
        Assert.assertNotNull("Fire Giant should exist", fireGiant);
        Assert.assertEquals(ElementalWeakness.WATER, fireGiant.getCombatProfile().getElementalWeakness());
        Assert.assertEquals(50, fireGiant.getCombatProfile().getElementalWeaknessPercent());

        MonsterData iceGiant = db.getMonster("ice_giant");
        Assert.assertNotNull("Ice Giant should exist", iceGiant);
        Assert.assertEquals(ElementalWeakness.FIRE, iceGiant.getCombatProfile().getElementalWeakness());
        Assert.assertEquals(50, iceGiant.getCombatProfile().getElementalWeaknessPercent());
    }

    @Test
    public void testSpecialMechanicsFlags() {
        MonsterData vorkath = db.getMonster("vorkath");
        MonsterCombatProfile vProfile = vorkath.getCombatProfile();
        Assert.assertEquals(MonsterClassification.DRAGON, vProfile.getClassification());
        Assert.assertTrue("Vorkath requires dragonfire", vProfile.isRequiresDragonfire());
        Assert.assertTrue("Vorkath favors venom protection", vProfile.isFavorsVenomProtection());

        MonsterData gargoyle = db.getMonster("gargoyle");
        MonsterCombatProfile gProfile = gargoyle.getCombatProfile();
        Assert.assertTrue("Gargoyle requires rock hammer", gProfile.isRequiresRockHammer());
        Assert.assertTrue("Gargoyle requires crush", gProfile.isRequiresCrush());

        MonsterData corp = db.getMonster("corporeal_beast");
        MonsterCombatProfile cProfile = corp.getCombatProfile();
        Assert.assertTrue("Corporeal Beast requires spear", cProfile.isRequiresSpear());

        MonsterData kreearra = db.getMonster("kreearra");
        MonsterCombatProfile kProfile = kreearra.getCombatProfile();
        Assert.assertFalse("Kree'arra cannot be meleed", kProfile.isStyleAllowed(CombatStyle.MELEE));
        Assert.assertTrue("Kree'arra allows ranged", kProfile.isStyleAllowed(CombatStyle.RANGED));
    }

    @Test
    public void testCoreBossesPresent() {
        MonsterData barrows = db.getMonster("barrows");
        Assert.assertNotNull("Barrows Brothers should be in monster database", barrows);
        Assert.assertEquals("Barrows Brothers", barrows.getName());
        Assert.assertEquals(MonsterCategory.BOSS, barrows.getCategory());
        Assert.assertFalse("Barrows variants should not be empty", barrows.getVariants().isEmpty());

        Assert.assertNotNull("Cerberus must exist", db.getMonster("cerberus"));
        Assert.assertNotNull("Alchemical Hydra must exist", db.getMonster("alchemical_hydra"));
        Assert.assertNotNull("Duke Sucellus must exist", db.getMonster("duke_sucellus"));
        Assert.assertNotNull("Vardorvis must exist", db.getMonster("vardorvis"));
        Assert.assertNotNull("The Whisperer must exist", db.getMonster("the_whisperer"));
        Assert.assertNotNull("The Leviathan must exist", db.getMonster("the_leviathan"));
        Assert.assertNotNull("Nex must exist", db.getMonster("nex"));
    }
}
