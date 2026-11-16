package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.*;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.*;
import java.util.*;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UltimateExpansionTest {

    private BossDatabase bossDatabase;
    private MonsterDatabase monsterDatabase;
    private EquipmentDatabase equipmentDatabase;
    private GePriceService priceService;
    private CombatCalculationService combatCalc;
    private GearOptimizerService optimizer;
    private PlayerAccount account;

    @Before
    public void setUp() {
        bossDatabase = new BossDatabase();
        monsterDatabase = new MonsterDatabase();
        equipmentDatabase = new EquipmentDatabase();
        priceService = new GePriceService(null);
        combatCalc = new CombatCalculationService();
        optimizer = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, combatCalc);

        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) {
            reals.put(s, 85);
            boosteds.put(s, 85);
        }
        Set<String> quests = new HashSet<>(Arrays.asList("Dragon Slayer I", "Monkey Madness I"));
        account = new PlayerAccount("TestHero", 110, 1850, reals, boosteds, Collections.emptyMap(), quests, true);
    }

    @Test
    public void testDpsLabComparison() {
        DpsLabService lab = new DpsLabService(combatCalc, priceService, equipmentDatabase);
        MonsterData vorkath = monsterDatabase.getMonster("vorkath");
        Assert.assertNotNull("Vorkath monster must exist in database", vorkath);

        MonsterVariant variant = vorkath.getVariants().get(0);
        MonsterCombatProfile profile = vorkath.getCombatProfile();

        Map<EquipmentSlot, EquipmentItem> setupA = new EnumMap<>(EquipmentSlot.class);
        setupA.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Rune crossbow"));

        Map<EquipmentSlot, EquipmentItem> setupB = new EnumMap<>(EquipmentSlot.class);
        setupB.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Dragon hunter crossbow"));

        DpsComparisonResult result = lab.compareSetups(
                "Vorkath", variant, profile, account, CombatStyle.RANGED,
                setupA, "Budget Setup",
                setupB, "BiS Setup",
                "Rigour", "Ranging potion", false
        );

        Assert.assertNotNull(result);
        Assert.assertTrue("DHCB should have higher DPS than Rune crossbow at Vorkath", result.getDpsDifference() > 0);
        Assert.assertTrue("Hit chance should be positive", result.getSetupB().getHitChance() > 0);
        Assert.assertNotNull("Verdict should not be null", result.getVerdict());
    }

    @Test
    public void testInventoryBuilder28SlotsValidation() {
        InventoryBuilderService builder = new InventoryBuilderService(priceService);
        BossData vorkath = bossDatabase.getBoss("vorkath");

        InventorySetup budgetSetup = builder.getInventorySetup(vorkath, "Budget");
        Assert.assertNotNull(budgetSetup);
        Assert.assertTrue("Inventory slots used must be <= 28", budgetSetup.getUsedSlots() <= 28);
        Assert.assertTrue("Setup must be valid", budgetSetup.isValid());
        Assert.assertTrue("Total food healing must be positive", budgetSetup.getTotalHealAmount() > 0);
        Assert.assertTrue("Total prayer restore must be positive", budgetSetup.getTotalPrayerRestore() > 0);

        TripMetrics metrics = budgetSetup.calculateTripMetrics(750, 4.5, 30);
        Assert.assertNotNull(metrics);
        Assert.assertTrue("Estimated kills per trip should be >= 1", metrics.getEstimatedKills() >= 1);
        Assert.assertTrue("Valid inventory trip", metrics.isValid());
    }

    @Test
    public void testCurrentGearComparison() {
        CurrentGearScannerService scanner = new CurrentGearScannerService(null, equipmentDatabase, priceService);
        BossData vorkath = bossDatabase.getBoss("vorkath");
        Assert.assertNotNull(vorkath);

        OptimizedGearSetup rec = optimizer.generateSetup(
                "Standard", vorkath, CombatStyle.RANGED, 50_000_000L, account, GearOptimizerService.PriorityMode.BALANCED
        );
        Assert.assertNotNull(rec);

        CurrentGearComparison comp = scanner.compareWithRecommended(rec, optimizer.getBossProfile(vorkath).toMonsterCombatProfile(), combatCalc, account);
        Assert.assertNotNull(comp);
        Assert.assertNotNull(comp.getSlotUpgrades());
    }

    @Test
    public void testDropRateCalculatorMath() {
        DropRateCalculatorService calc = new DropRateCalculatorService();

        // 1/50 rate (e.g. Vorkath head), pity threshold at 50
        DropRateCalculation vorkathHead = calc.calculateCustom("Vorkath's Head", "Vorkath", 50, 50);
        Assert.assertEquals("Expected median KC for 1/50 is ~35", 35, vorkathHead.getMedianKills());
        Assert.assertEquals(1.0, vorkathHead.calculateChanceAtKc(50), 0.001); // 100% due to pity threshold

        // Normal 1/512 drop at KC 512
        DropRateCalculation dwh = calc.calculateCustom("Dragon warhammer", "Lizardman shaman", 3000, -1);
        Assert.assertTrue("At 10 KC chance should be lower than at 500 KC", dwh.calculateChanceAtKc(10) < dwh.calculateChanceAtKc(500));
    }

    @Test
    public void testMonsterProfitService() {
        MonsterProfitService service = new MonsterProfitService();
        List<MonsterProfitProfile> all = service.getAllProfiles();
        Assert.assertTrue("Must have registered profit profiles", all.size() >= 20);

        List<MonsterProfitProfile> rangedProfits = service.getFilteredProfiles(MonsterProfitService.ProfitFilter.RANGED, MonsterProfitService.ProfitSort.NET_PROFIT_DESC);
        Assert.assertFalse("Should find ranged profitable encounters", rangedProfits.isEmpty());

        List<MonsterProfitProfile> highProfit = service.getFilteredProfiles(MonsterProfitService.ProfitFilter.BOSSES, MonsterProfitService.ProfitSort.NET_PROFIT_DESC);
        Assert.assertFalse("Should filter encounters for bosses", highProfit.isEmpty());

        MonsterProfitProfile vorkathProf = service.getProfile("vorkath");
        Assert.assertNotNull(vorkathProf);
        Assert.assertTrue("Vorkath net profit per hour must be positive", vorkathProf.getNetProfitPerHour() > 1_000_000L);
    }

    @Test
    public void testLootTrackerSession() {
        LootTrackerService tracker = new LootTrackerService(priceService, bossDatabase, monsterDatabase, null);
        tracker.startNewSession("Zulrah");

        LootSession session = tracker.getActiveSession();
        Assert.assertNotNull(session);
        Assert.assertEquals("Zulrah", session.getMonsterName());
        Assert.assertEquals(0, session.getKills());

        session.recordKill(15_000L);
        session.addDrop(new LootDropItem(12922, "Tanzanite fang", 1, 4_500_000L, LootDropItem.DropRarity.UNIQUE));

        Assert.assertEquals(1, session.getKills());
        Assert.assertEquals(4_500_000L, session.getTotalLootValue());
        Assert.assertEquals(15_000L, session.getEstimatedSupplyCosts());
        Assert.assertEquals(4_485_000L, session.getNetProfit());
        Assert.assertEquals(Integer.valueOf(1), session.getRarityCounts().get(LootDropItem.DropRarity.UNIQUE));
    }

    @Test
    public void testPrayerDrainOptimization() {
        PrayerPotionOptimizerService prayService = new PrayerPotionOptimizerService(priceService);
        MonsterData hydra = monsterDatabase.getMonster("alchemical_hydra");

        List<PrayerPlan> plans = prayService.evaluatePrayerPlans(
                hydra.getCombatProfile(), account, CombatStyle.RANGED, 15
        );

        Assert.assertFalse("Prayer plans should not be empty", plans.isEmpty());
        for (PrayerPlan plan : plans) {
            Assert.assertTrue("Drain rate per min should be positive", plan.getDrainPerMinute() > 0);
            Assert.assertTrue("Potions per hour should be positive", plan.getPrayerPotionsPerHour() > 0);
        }
    }

    @Test
    public void testCombatTrainingTargets() {
        CombatTrainingService training = new CombatTrainingService();
        List<CombatTrainingTarget> targets = training.getRecommendations(account, Skill.ATTACK, 99);
        Assert.assertFalse("Training recommendations must not be empty", targets.isEmpty());

        int xpRemaining = training.getXpRemaining(account, Skill.ATTACK, 99);
        Assert.assertTrue("XP remaining from 85 to 99 should be ~9.7M", xpRemaining > 9_000_000);
    }

    @Test
    public void testAfkMonsterFinder() {
        AfkMonsterFinderService afk = new AfkMonsterFinderService();
        List<AfkMonsterProfile> profiles = afk.getAllAfkProfiles();
        Assert.assertFalse("AFK profiles should not be empty", profiles.isEmpty());

        for (AfkMonsterProfile p : profiles) {
            Assert.assertTrue("AFK score must be between 1 and 10", p.getAfkScore() >= 1 && p.getAfkScore() <= 10);
            Assert.assertTrue("Reset timer must be >= 4 mins", p.getAttentionIntervalMinutes() >= 4);
        }
    }

    @Test
    public void testSlayerTaskAssistant() {
        SlayerTaskService slayer = new SlayerTaskService(null, null);
        SlayerTaskInfo gargoyles = slayer.getTask("Gargoyles");
        Assert.assertNotNull(gargoyles);
        Assert.assertFalse("Locations should not be empty", gargoyles.getLocations().isEmpty());
        Assert.assertTrue("Estimated duration minutes should be positive", gargoyles.estimateTaskDurationMinutes(180) > 0);
    }

    @Test
    public void testBankCheckerService() {
        BankCheckerService bank = new BankCheckerService(null, equipmentDatabase, priceService);
        Set<Integer> owned = new HashSet<>(Arrays.asList(1333, 4587)); // Rune scim & Dragon scim
        bank.setCustomOwnedItemIds(owned);

        Assert.assertTrue(bank.isItemOwned(1333));
        Assert.assertTrue(bank.isItemOwned(4587));
        Assert.assertFalse(bank.isItemOwned(11802)); // Armadyl godsword not owned

        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Dragon scimitar"));
        gear.put(EquipmentSlot.HEAD, equipmentDatabase.getItem("Helm of neitiznot"));

        OptimizedGearSetup setup = new OptimizedGearSetup(
                "TestSetup", CombatStyle.MELEE, gear, 110_000L, 5_000_000L, Collections.emptyMap(), Collections.emptyList()
        );

        BankGearCheckResult result = bank.checkSetupAgainstBank(setup);
        Assert.assertNotNull(result);
        Assert.assertEquals("Should own 1 item (Dragon scimitar)", 1, result.getOwnedSlotsCount());
        Assert.assertEquals("Should be missing 1 item (Helm of neitiznot)", 1, result.getMissingItems().size());
        Assert.assertTrue("Missing cost should be > 0", result.getMissingGearCost() > 0);
    }

    @Test
    public void testExportServiceJsonAndCsv() {
        ExportService exporter = new ExportService();
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.WEAPON, equipmentDatabase.getItem("Abyssal whip"));

        OptimizedGearSetup setup = new OptimizedGearSetup(
                "Standard", CombatStyle.MELEE, gear, 1_600_000L, 10_000_000L, Collections.emptyMap(), Collections.emptyList()
        );

        String json = exporter.exportGearSetupAsJson(setup);
        Assert.assertTrue("JSON should contain weapon", json.contains("Abyssal whip"));
        Assert.assertTrue("JSON should contain WEAPON slot", json.contains("WEAPON"));

        String csv = exporter.exportGearSetupAsCsv(setup);
        Assert.assertTrue("CSV should contain header", csv.contains("Slot,Item ID,Item Name,Price,Tier"));
        Assert.assertTrue("CSV should contain Abyssal whip", csv.contains("Abyssal whip"));
    }
}
