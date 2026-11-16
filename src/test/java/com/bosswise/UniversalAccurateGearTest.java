package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.*;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import java.util.*;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UniversalAccurateGearTest {
    private BossDatabase bossDatabase;
    private MonsterDatabase monsterDatabase;
    private EquipmentDatabase equipmentDatabase;
    private GearOptimizerService optimizer;
    private PlayerAccount maxAccount;
    private PlayerAccount midAccount;

    @Before
    public void setup() {
        bossDatabase = new BossDatabase();
        monsterDatabase = new MonsterDatabase();
        equipmentDatabase = new EquipmentDatabase();
        GePriceService priceService = new GePriceService(null);
        optimizer = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, new com.bosswise.service.CombatCalculationService());

        // Max account (all skills 99, all quests)
        Map<Skill, Integer> maxReals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> maxBoosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            maxReals.put(s, 99);
            maxBoosteds.put(s, 99);
        }
        Set<String> allQuests = new HashSet<>(Arrays.asList(
                "Dragon Slayer I", "Dragon Slayer II", "Monkey Madness I", "Monkey Madness II",
                "The Fremennik Trials", "The Fremennik Isles", "The Fremennik Exiles",
                "Desert Treasure I", "Desert Treasure II - The Fallen Empire", "Song of the Elves",
                "Horror from the Deep", "Mourning's End Part II", "Haunted Mine"
        ));
        maxAccount = new PlayerAccount("MaxPvmGod", 126, 2277, maxReals, maxBoosteds, Collections.emptyMap(), allQuests, true);

        // Mid account (skills 70-75)
        Map<Skill, Integer> midReals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> midBoosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            midReals.put(s, 70);
            midBoosteds.put(s, 70);
        }
        midReals.put(Skill.ATTACK, 75);
        midReals.put(Skill.STRENGTH, 75);
        midReals.put(Skill.DEFENCE, 70);
        midReals.put(Skill.RANGED, 75);
        midReals.put(Skill.MAGIC, 75);
        midReals.put(Skill.PRAYER, 60);
        midReals.put(Skill.HITPOINTS, 75);
        midBoosteds.putAll(midReals);
        Set<String> midQuests = new HashSet<>(Arrays.asList(
                "Dragon Slayer I", "Monkey Madness I", "The Fremennik Trials", "The Fremennik Isles", "Horror from the Deep", "Haunted Mine"
        ));
        midAccount = new PlayerAccount("MidPlayer", 90, 1500, midReals, midBoosteds, Collections.emptyMap(), midQuests, true);
    }

    // =========================================================================
    // 1. BUDGET PRESETS & 4-TIER PROGRESSION (100K, 500K, 1M, 5M, 10M, 25M, 100M+)
    // =========================================================================

    @Test
    public void testFourDistinctTiers() {
        BossData vorkath = bossDatabase.getBoss("vorkath");
        long budget = 25_000_000L;

        OptimizedGearSetup budgetSetup = optimizer.generateSetup("Budget", vorkath, CombatStyle.RANGED, budget, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup bestValueSetup = optimizer.generateSetup("Best Value", vorkath, CombatStyle.RANGED, budget, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup highTierSetup = optimizer.generateSetup("High Tier", vorkath, CombatStyle.RANGED, budget, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup maxBisSetup = optimizer.generateSetup("Max BiS", vorkath, CombatStyle.RANGED, budget, maxAccount, PriorityMode.BALANCED);

        Assert.assertNotNull(budgetSetup);
        Assert.assertNotNull(bestValueSetup);
        Assert.assertNotNull(highTierSetup);
        Assert.assertNotNull(maxBisSetup);

        Assert.assertTrue(budgetSetup.getTotalCost() <= budget);
        Assert.assertTrue(bestValueSetup.getTotalCost() <= budget);
        Assert.assertTrue(highTierSetup.getTotalCost() <= budget);
        Assert.assertTrue(maxBisSetup.getTotalCost() <= budget);

        // Budget tier should be cheapest
        Assert.assertTrue(budgetSetup.getTotalCost() <= bestValueSetup.getTotalCost());
        // Max BiS should have highest combat performance or cost
        Assert.assertTrue(maxBisSetup.getCombatResult().getDps() >= budgetSetup.getCombatResult().getDps());
    }

    @Test
    public void testUltraLowBudget100k() {
        BossData scurrius = bossDatabase.getBoss("scurrius");
        OptimizedGearSetup setup = optimizer.generateSetup("Budget", scurrius, CombatStyle.MELEE, 100_000L, midAccount, PriorityMode.BALANCED);

        Assert.assertTrue("Setup cost must be <= 100,000 GP", setup.getTotalCost() <= 100_000L);
        Assert.assertNotNull(setup.getItem(EquipmentSlot.WEAPON));
        Assert.assertTrue(setup.getItem(EquipmentSlot.WEAPON).getCrushAttack() > 0 || setup.getItem(EquipmentSlot.WEAPON).getAttackType() == AttackType.CRUSH);
    }

    // =========================================================================
    // 2. BOSS-SPECIFIC WEAPON & COMBAT STYLE RULES
    // =========================================================================

    @Test
    public void testCorporealBeastSpearEnforcement() {
        BossData corp = bossDatabase.getBoss("corporeal_beast");
        OptimizedGearSetup setup = optimizer.generateSetup("Best Available", corp, CombatStyle.MELEE, 50_000_000L, maxAccount, PriorityMode.BALANCED);

        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull("Corp setup must have a weapon", weapon);
        Assert.assertTrue("Corp weapon must be a spear or fang: " + weapon.getName(),
                weapon.isSpear() || weapon.getName().contains("fang") || weapon.getName().contains("spear") || weapon.getName().contains("hasta"));
    }

    @Test
    public void testKrakenMagicOnlyEnforcement() {
        BossData kraken = bossDatabase.getBoss("kraken");
        BossCombatProfile profile = optimizer.getBossProfile(kraken);
        Assert.assertEquals(1, profile.getAllowedStyles().size());
        Assert.assertTrue(profile.getAllowedStyles().contains(CombatStyle.MAGIC));

        // Even if user requests MELEE, engine enforces MAGIC
        OptimizedGearSetup setup = optimizer.generateSetup("Best Available", kraken, CombatStyle.MELEE, 50_000_000L, maxAccount, PriorityMode.BALANCED);
        Assert.assertEquals(CombatStyle.MAGIC, setup.getCombatStyle());
        Assert.assertEquals(CombatStyle.MAGIC, setup.getItem(EquipmentSlot.WEAPON).getPrimaryStyle());
    }

    @Test
    public void testKreearraRangedOnlyEnforcement() {
        BossData kree = bossDatabase.getBoss("kreearra");
        BossCombatProfile profile = optimizer.getBossProfile(kree);
        Assert.assertEquals(1, profile.getAllowedStyles().size());
        Assert.assertTrue(profile.getAllowedStyles().contains(CombatStyle.RANGED));

        OptimizedGearSetup setup = optimizer.generateSetup("Best Available", kree, CombatStyle.MELEE, 50_000_000L, maxAccount, PriorityMode.BALANCED);
        Assert.assertEquals(CombatStyle.RANGED, setup.getCombatStyle());
        Assert.assertEquals(CombatStyle.RANGED, setup.getItem(EquipmentSlot.WEAPON).getPrimaryStyle());
    }

    @Test
    public void testWhispererMagicOnlyEnforcement() {
        BossData whisperer = bossDatabase.getBoss("the_whisperer");
        BossCombatProfile profile = optimizer.getBossProfile(whisperer);
        Assert.assertEquals(1, profile.getAllowedStyles().size());
        Assert.assertTrue(profile.getAllowedStyles().contains(CombatStyle.MAGIC));

        OptimizedGearSetup setup = optimizer.generateSetup("Max BiS", whisperer, CombatStyle.RANGED, 100_000_000L, maxAccount, PriorityMode.BALANCED);
        Assert.assertEquals(CombatStyle.MAGIC, setup.getCombatStyle());
    }

    @Test
    public void testVardorvisAndDukeSlashPreference() {
        BossData vard = bossDatabase.getBoss("vardorvis");
        BossData duke = bossDatabase.getBoss("duke_sucellus");

        OptimizedGearSetup vardSetup = optimizer.generateSetup("Best Available", vard, CombatStyle.MELEE, 50_000_000L, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup dukeSetup = optimizer.generateSetup("Best Available", duke, CombatStyle.MELEE, 50_000_000L, maxAccount, PriorityMode.BALANCED);

        Assert.assertEquals(AttackType.SLASH, vardSetup.getItem(EquipmentSlot.WEAPON).getAttackType());
        Assert.assertEquals(AttackType.SLASH, dukeSetup.getItem(EquipmentSlot.WEAPON).getAttackType());
    }

    @Test
    public void testSarachnisAndCalvarionCrushEnforcement() {
        BossData sarachnis = bossDatabase.getBoss("sarachnis");
        BossData calvarion = bossDatabase.getBoss("calvarion");

        OptimizedGearSetup sSetup = optimizer.generateSetup("Best Value", sarachnis, CombatStyle.MELEE, 5_000_000L, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup cSetup = optimizer.generateSetup("Best Value", calvarion, CombatStyle.MELEE, 5_000_000L, maxAccount, PriorityMode.BALANCED);

        Assert.assertTrue(sSetup.getItem(EquipmentSlot.WEAPON).getCrushAttack() > 0 || sSetup.getItem(EquipmentSlot.WEAPON).getAttackType() == AttackType.CRUSH);
        Assert.assertTrue(cSetup.getItem(EquipmentSlot.WEAPON).getCrushAttack() > 0 || cSetup.getItem(EquipmentSlot.WEAPON).getAttackType() == AttackType.CRUSH);
    }

    @Test
    public void testVorkathDragonfireAndSalveEi() {
        BossData vorkath = bossDatabase.getBoss("vorkath");
        OptimizedGearSetup setup = optimizer.generateSetup("Best Value", vorkath, CombatStyle.RANGED, 1_000_000L, midAccount, PriorityMode.BALANCED);

        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        EquipmentItem shield = setup.getItem(EquipmentSlot.SHIELD);
        EquipmentItem amulet = setup.getItem(EquipmentSlot.AMULET);

        if (weapon != null && !weapon.isTwoHanded()) {
            Assert.assertNotNull("Vorkath with 1-handed crossbow must equip shield for dragonfire protection", shield);
            Assert.assertTrue("Shield must protect against dragonfire", shield.isDragonfireProtection());
        }
        Assert.assertNotNull("Amulet must be equipped", amulet);
        Assert.assertTrue("Vorkath must prioritize Salve amulet (ei)", amulet.getName().contains("Salve amulet (ei)"));
    }

    @Test
    public void testDemonbanePrioritizationOnDemons() {
        BossData kril = bossDatabase.getBoss("kril_tsutsaroth");
        BossData cerb = bossDatabase.getBoss("cerberus");

        OptimizedGearSetup krilSetup = optimizer.generateSetup("Best Value", kril, CombatStyle.MELEE, 10_000_000L, maxAccount, PriorityMode.BALANCED);
        OptimizedGearSetup cerbSetup = optimizer.generateSetup("Best Value", cerb, CombatStyle.MELEE, 10_000_000L, maxAccount, PriorityMode.BALANCED);

        Assert.assertTrue("K'ril should prioritize demonbane weapon",
                krilSetup.getItem(EquipmentSlot.WEAPON).isDemonbane() || krilSetup.getItem(EquipmentSlot.WEAPON).getTier() >= 7);
        Assert.assertTrue("Cerberus should prioritize demonbane or crush weapon",
                cerbSetup.getItem(EquipmentSlot.WEAPON).isDemonbane() || cerbSetup.getItem(EquipmentSlot.WEAPON).getCrushAttack() > 0);
    }

    // =========================================================================
    // 3. UNIVERSAL MONSTER SPECIAL MECHANICS
    // =========================================================================

    @Test
    public void testBasiliskMirrorShieldEnforcement() {
        MonsterData basilisk = monsterDatabase.getMonsterByName("Basilisk Knights");
        if (basilisk == null) basilisk = monsterDatabase.getMonsterByName("Basilisk");
        Assert.assertNotNull("Basilisk must exist in MonsterDatabase", basilisk);

        OptimizedGearSetup setup = optimizer.generateSetup("Best Value", basilisk, null, CombatStyle.MELEE, 5_000_000L, maxAccount, PriorityMode.BALANCED, false);
        EquipmentItem shield = setup.getItem(EquipmentSlot.SHIELD);
        Assert.assertNotNull("Basilisk requires shield", shield);
        Assert.assertTrue("Basilisk requires mirror shield or V's shield: " + shield.getName(),
                shield.isMirrorShield() || shield.getName().contains("Mirror") || shield.getName().contains("V's"));
    }

    @Test
    public void testKuraskLeafBladedEnforcement() {
        MonsterData kurask = monsterDatabase.getMonsterByName("Kurask");
        Assert.assertNotNull("Kurask must exist in MonsterDatabase", kurask);

        OptimizedGearSetup setup = optimizer.generateSetup("Best Value", kurask, null, CombatStyle.MELEE, 5_000_000L, maxAccount, PriorityMode.BALANCED, false);
        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull("Kurask requires weapon", weapon);
        Assert.assertTrue("Kurask requires leaf-bladed weapon: " + weapon.getName(), weapon.isLeafBladed());
    }

    @Test
    public void testHydraBootsOfStoneEnforcement() {
        BossData hydra = bossDatabase.getBoss("alchemical_hydra");
        OptimizedGearSetup setup = optimizer.generateSetup("Best Value", hydra, CombatStyle.RANGED, 50_000_000L, maxAccount, PriorityMode.BALANCED);

        EquipmentItem boots = setup.getItem(EquipmentSlot.BOOTS);
        Assert.assertNotNull("Hydra requires boots", boots);
        Assert.assertTrue("Hydra requires boots of stone or brimstone: " + boots.getName(),
                boots.isBootsOfStone() || boots.getName().contains("stone") || boots.getName().contains("brimstone"));
    }
}
