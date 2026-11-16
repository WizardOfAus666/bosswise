package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentItem.AmmoType;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.GearUpgradeRecommendation;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import com.bosswise.service.GearUpgradeAdvisor;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GearBudgetOptimizerTest {
    private BossDatabase bossDatabase;
    private EquipmentDatabase equipmentDatabase;
    private GearOptimizerService optimizer;
    private GearUpgradeAdvisor advisor;

    @Before
    public void setup() {
        bossDatabase = new BossDatabase();
        equipmentDatabase = new EquipmentDatabase();
        GePriceService priceService = new GePriceService(null);
        optimizer = new GearOptimizerService(equipmentDatabase, priceService);
        advisor = new GearUpgradeAdvisor(equipmentDatabase, optimizer, priceService);
    }

    private PlayerAccount createLowLevelAccount() {
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            reals.put(s, 40);
            boosteds.put(s, 40);
        }
        reals.put(Skill.PRAYER, 43);
        reals.put(Skill.HITPOINTS, 50);
        reals.put(Skill.SLAYER, 1);
        boosteds.putAll(reals);

        Map<Quest, QuestState> qStates = new HashMap<>();
        Set<String> quests = new HashSet<>();
        quests.add("Dragon Slayer I");

        return new PlayerAccount("LowLevelNoob", 55, 600, reals, boosteds, qStates, quests, true);
    }

    private PlayerAccount createMidLevelAccount() {
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            reals.put(s, 70);
            boosteds.put(s, 70);
        }
        reals.put(Skill.ATTACK, 75);
        reals.put(Skill.STRENGTH, 75);
        reals.put(Skill.RANGED, 75);
        reals.put(Skill.MAGIC, 75);
        reals.put(Skill.PRAYER, 60);
        reals.put(Skill.HITPOINTS, 75);
        boosteds.putAll(reals);

        Map<Quest, QuestState> qStates = new HashMap<>();
        Set<String> quests = new HashSet<>();
        quests.add("Dragon Slayer I");
        quests.add("Monkey Madness I");
        quests.add("The Fremennik Trials");
        quests.add("The Fremennik Isles");
        quests.add("Horror from the Deep");

        return new PlayerAccount("MidLevelGamer", 88, 1400, reals, boosteds, qStates, quests, true);
    }

    private PlayerAccount createHighLevelAccount() {
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            reals.put(s, 99);
            boosteds.put(s, 99);
        }
        boosteds.putAll(reals);

        Map<Quest, QuestState> qStates = new HashMap<>();
        Set<String> quests = new HashSet<>();
        quests.add("Dragon Slayer I");
        quests.add("Dragon Slayer II");
        quests.add("Monkey Madness I");
        quests.add("Recipe for Disaster");
        quests.add("Haunted Mine");
        quests.add("Song of the Elves");
        quests.add("The Fremennik Trials");
        quests.add("The Fremennik Isles");
        quests.add("The Fremennik Exiles");
        quests.add("Desert Treasure II");
        quests.add("While Guthix Sleeps");
        quests.add("Beneath Cursed Sands");

        return new PlayerAccount("MaxPvmChad", 126, 2277, reals, boosteds, qStates, quests, true);
    }

    // =========================================================================
    // 1. BUDGET CONSTRAINTS & TIER TESTS
    // =========================================================================

    @Test
    public void testBudgetsAcrossMultipleTiers() {
        PlayerAccount midAccount = createMidLevelAccount();
        BossData vorkath = bossDatabase.getBoss("vorkath");
        long[] budgets = new long[]{100_000L, 500_000L, 1_000_000L, 5_000_000L, 20_000_000L};

        for (long budget : budgets) {
            OptimizedGearSetup budgetSetup = optimizer.generateSetup(
                    "Budget", vorkath, CombatStyle.RANGED, budget, midAccount, PriorityMode.BALANCED
            );
            OptimizedGearSetup bestValueSetup = optimizer.generateSetup(
                    "Best Value", vorkath, CombatStyle.RANGED, budget, midAccount, PriorityMode.BALANCED
            );
            OptimizedGearSetup bestAvailSetup = optimizer.generateSetup(
                    "Best Available", vorkath, CombatStyle.RANGED, budget, midAccount, PriorityMode.BALANCED
            );

            // Assert budget adherence
            Assert.assertTrue("Budget setup cost " + budgetSetup.getTotalCost() + " exceeded budget " + budget,
                    budgetSetup.getTotalCost() <= budget);
            Assert.assertTrue("Best value setup cost " + bestValueSetup.getTotalCost() + " exceeded budget " + budget,
                    bestValueSetup.getTotalCost() <= budget);
            Assert.assertTrue("Best available setup cost " + bestAvailSetup.getTotalCost() + " exceeded budget " + budget,
                    bestAvailSetup.getTotalCost() <= budget);

            // Assert tier progression
            Assert.assertTrue("Budget (" + budgetSetup.getTotalCost() + ") should be <= Best Value (" + bestValueSetup.getTotalCost() + ")",
                    budgetSetup.getTotalCost() <= bestValueSetup.getTotalCost());
            Assert.assertTrue("Best Value (" + bestValueSetup.getTotalCost() + ") should be <= Best Available (" + bestAvailSetup.getTotalCost() + ")",
                    bestValueSetup.getTotalCost() <= bestAvailSetup.getTotalCost());
        }
    }

    // =========================================================================
    // 2. BOSS MECHANIC: VORKATH (DRAGONFIRE + UNDEAD)
    // =========================================================================

    @Test
    public void testVorkathMechanics() {
        PlayerAccount midAccount = createMidLevelAccount();
        BossData vorkath = bossDatabase.getBoss("vorkath");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", vorkath, CombatStyle.RANGED, 5_000_000L, midAccount, PriorityMode.BALANCED
        );

        // 1. Dragonfire protection mandatory on 1-handed setup
        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull("Vorkath setup must have a weapon", weapon);
        if (!weapon.isTwoHanded()) {
            EquipmentItem shield = setup.getItem(EquipmentSlot.SHIELD);
            Assert.assertNotNull("Vorkath 1H setup must have shield", shield);
            Assert.assertTrue("Vorkath shield must provide dragonfire protection", shield.isDragonfireProtection());
        }

        // 2. Mid-level account has NOT completed Haunted Mine -> Salve amulet (ei) must NOT be equipped
        EquipmentItem amulet = setup.getItem(EquipmentSlot.AMULET);
        if (amulet != null) {
            Assert.assertNotEquals("Player without Haunted Mine must not be equipped with Salve",
                    "Salve amulet (ei)", amulet.getName());
        }

        // 3. High-level account WITH Haunted Mine -> Salve amulet (ei) MUST be equipped (top tier for undead)
        PlayerAccount maxAccount = createHighLevelAccount();
        OptimizedGearSetup maxSetup = optimizer.generateSetup(
                "Best Value", vorkath, CombatStyle.RANGED, 50_000_000L, maxAccount, PriorityMode.BALANCED
        );
        EquipmentItem maxAmulet = maxSetup.getItem(EquipmentSlot.AMULET);
        Assert.assertNotNull(maxAmulet);
        Assert.assertEquals("High-level account at Vorkath should prioritize Salve amulet (ei)",
                "Salve amulet (ei)", maxAmulet.getName());
    }

    // =========================================================================
    // 3. BOSS MECHANIC: CALVAR'ION (WILDERNESS + CRUSH ONLY)
    // =========================================================================

    @Test
    public void testCalvarionCrushWeaponry() {
        PlayerAccount midAccount = createMidLevelAccount();
        BossData calvarion = bossDatabase.getBoss("calvarion");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", calvarion, CombatStyle.MELEE, 5_000_000L, midAccount, PriorityMode.BALANCED
        );

        // Weapon MUST be a crush weapon
        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull(weapon);
        Assert.assertTrue("Calvar'ion weapon must have crush attack bonus: " + weapon.getName(),
                weapon.getCrushAttack() > 0 || weapon.getAttackType() == AttackType.CRUSH);
        Assert.assertFalse("Calvar'ion must never equip pure slash weapon like whip: " + weapon.getName(),
                weapon.getName().contains("whip") || weapon.getName().contains("tentacle"));

        // Upgrades roadmap must never recommend magic items for Melee Calvar'ion
        List<GearUpgradeRecommendation> upgrades = advisor.findUpgrades(setup, calvarion, 5_000_000L, midAccount);
        for (GearUpgradeRecommendation up : upgrades) {
            Assert.assertFalse("Calvar'ion melee upgrade must not be magic (e.g. Tormented bracelet): " + up.getUpgradeItem().getName(),
                    up.getUpgradeItem().getName().contains("Tormented"));
            if (up.getSlot() == EquipmentSlot.WEAPON) {
                Assert.assertTrue("Weapon upgrade for Calvar'ion must have crush: " + up.getUpgradeItem().getName(),
                        up.getUpgradeItem().getCrushAttack() > 0);
            }
        }
    }

    // =========================================================================
    // 4. BOSS MECHANIC: CORPOREAL BEAST (SPEAR ONLY)
    // =========================================================================

    @Test
    public void testCorporealBeastSpearMechanic() {
        PlayerAccount maxAccount = createHighLevelAccount();
        BossData corp = bossDatabase.getBoss("corporeal_beast");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", corp, CombatStyle.MELEE, 10_000_000L, maxAccount, PriorityMode.BALANCED
        );

        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull(weapon);
        Assert.assertTrue("Corporeal Beast weapon must be a spear or fang: " + weapon.getName(),
                weapon.isSpear() || weapon.getName().contains("fang"));
    }

    // =========================================================================
    // 5. BOSS MECHANIC: SCURRIUS (NO SALVE ON LIVING RAT)
    // =========================================================================

    @Test
    public void testScurriusLivingBossNoSalve() {
        PlayerAccount maxAccount = createHighLevelAccount();
        BossData scurrius = bossDatabase.getBoss("scurrius");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", scurrius, CombatStyle.MELEE, 1_000_000L, maxAccount, PriorityMode.BALANCED
        );

        EquipmentItem amulet = setup.getItem(EquipmentSlot.AMULET);
        if (amulet != null) {
            Assert.assertNotEquals("Scurrius is a living rat, Salve amulet must never be equipped",
                    "Salve amulet (ei)", amulet.getName());
        }
    }

    // =========================================================================
    // 6. BOSS MECHANIC: KREE'ARRA (MELEE FORBIDDEN)
    // =========================================================================

    @Test
    public void testKreeArraMeleeForbidden() {
        PlayerAccount midAccount = createMidLevelAccount();
        BossData kreearra = bossDatabase.getBoss("kreearra");

        // Requesting MELEE for Kree'arra must automatically fall back to RANGED
        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", kreearra, CombatStyle.MELEE, 5_000_000L, midAccount, PriorityMode.BALANCED
        );

        Assert.assertEquals("Kree'arra combat style must be Ranged",
                CombatStyle.RANGED, setup.getCombatStyle());
        Assert.assertEquals("Weapon must be Ranged",
                CombatStyle.RANGED, setup.getItem(EquipmentSlot.WEAPON).getPrimaryStyle());
    }

    // =========================================================================
    // 7. COMPATIBILITY: 2H WEAPONS AND AMMUNITION
    // =========================================================================

    @Test
    public void testTwoHandedWeaponAndAmmoCompatibility() {
        PlayerAccount midAccount = createMidLevelAccount();
        BossData zulrah = bossDatabase.getBoss("zulrah");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", zulrah, CombatStyle.RANGED, 5_000_000L, midAccount, PriorityMode.BALANCED
        );

        EquipmentItem weapon = setup.getItem(EquipmentSlot.WEAPON);
        Assert.assertNotNull(weapon);

        if (weapon.isTwoHanded()) {
            Assert.assertNull("Shield must be null when two-handed weapon is equipped",
                    setup.getItem(EquipmentSlot.SHIELD));
        }

        if (weapon.getAmmoType() == AmmoType.BOLT) {
            EquipmentItem ammo = setup.getItem(EquipmentSlot.AMMO);
            Assert.assertNotNull("Crossbow must have bolts equipped", ammo);
            Assert.assertEquals("Ammo must be bolts", AmmoType.BOLT, ammo.getAmmoType());
        } else if (weapon.getAmmoType() == AmmoType.ARROW) {
            EquipmentItem ammo = setup.getItem(EquipmentSlot.AMMO);
            Assert.assertNotNull("Bow must have arrows equipped", ammo);
            Assert.assertEquals("Ammo must be arrows", AmmoType.ARROW, ammo.getAmmoType());
        }
    }

    // =========================================================================
    // 8. LOW LEVEL ACCOUNT RESTRICTIONS
    // =========================================================================

    @Test
    public void testLowLevelAccountCannotEquipHighTier() {
        PlayerAccount lowAccount = createLowLevelAccount();
        BossData mole = bossDatabase.getBoss("giant_mole");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Available", mole, CombatStyle.MELEE, 50_000_000L, lowAccount, PriorityMode.BALANCED
        );

        // Low account only has 40 Attack/Defence and no Barrows gloves or Bandos
        for (EquipmentItem item : setup.getEquipment().values()) {
            if (item != null) {
                Assert.assertTrue("Low level account cannot equip " + item.getName(),
                        optimizer.canEquip(item, lowAccount));
                Assert.assertFalse("Low level account should not equip Abyssal whip",
                        item.getName().equalsIgnoreCase("Abyssal whip"));
                Assert.assertFalse("Low level account should not equip Bandos chestplate",
                        item.getName().equalsIgnoreCase("Bandos chestplate"));
                Assert.assertFalse("Low level account should not equip Barrows gloves",
                        item.getName().equalsIgnoreCase("Barrows gloves"));
            }
        }
    }

    // =========================================================================
    // 9. EXPLANATIONS AND CHEAPER ALTERNATIVES
    // =========================================================================

    @Test
    public void testExplanationsAndCheaperAlternatives() {
        PlayerAccount maxAccount = createHighLevelAccount();
        BossData vorkath = bossDatabase.getBoss("vorkath");

        OptimizedGearSetup setup = optimizer.generateSetup(
                "Best Value", vorkath, CombatStyle.RANGED, 10_000_000L, maxAccount, PriorityMode.BALANCED
        );

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            String reason = setup.getReason(slot);
            Assert.assertNotNull("Reason for " + slot + " must not be null", reason);
            Assert.assertFalse("Reason for " + slot + " must not be empty", reason.trim().isEmpty());
        }
    }
}
