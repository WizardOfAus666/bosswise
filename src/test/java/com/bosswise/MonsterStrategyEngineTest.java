package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.PersonalizedStrategyScore;
import com.bosswise.model.strategy.StrategyConfidence;
import com.bosswise.model.strategy.StrategyMethod;
import com.bosswise.model.strategy.StrategyTier;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GePriceService;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.MonsterStrategyService;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MonsterStrategyEngineTest {
    private MonsterDatabase monsterDatabase;
    private BossDatabase bossDatabase;
    private MonsterStrategyDatabase strategyDatabase;
    private MonsterStrategyService strategyService;
    private GearOptimizerService gearOptimizerService;
    private InventoryBuilderService inventoryBuilderService;

    @Before
    public void setUp() {
        monsterDatabase = new MonsterDatabase();
        bossDatabase = new BossDatabase();
        strategyDatabase = new MonsterStrategyDatabase(monsterDatabase, bossDatabase);

        EquipmentDatabase equipmentDatabase = new EquipmentDatabase();
        GePriceService priceService = new GePriceService(null);
        CombatCalculationService combatCalc = new CombatCalculationService();

        gearOptimizerService = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, combatCalc);
        inventoryBuilderService = new InventoryBuilderService(priceService);

        strategyService = new MonsterStrategyService(
                strategyDatabase,
                gearOptimizerService,
                inventoryBuilderService,
                monsterDatabase,
                bossDatabase
        );
    }

    @Test
    public void testStrategyDatabaseLoadsAndCoversAllMonsters() {
        Map<String, MonsterStrategy> all = strategyDatabase.getAllStrategies();
        assertNotNull("Strategies map must not be null", all);
        assertTrue("Must contain strategies for at least 75 monsters", all.size() >= 75);

        // Verify key premier bosses
        assertNotNull("Vorkath must have strategy", strategyDatabase.getStrategy("vorkath"));
        assertNotNull("Zulrah must have strategy", strategyDatabase.getStrategy("zulrah"));
        assertNotNull("KBD must have strategy", strategyDatabase.getStrategy("king_black_dragon"));
        assertNotNull("Barrows must have strategy", strategyDatabase.getStrategy("barrows"));
        assertNotNull("Scurrius must have strategy", strategyDatabase.getStrategy("scurrius"));
        assertNotNull("Jad must have strategy", strategyDatabase.getStrategy("tztok_jad"));

        // Verify key slayer monsters
        assertNotNull("Abyssal demon must have strategy", strategyDatabase.getStrategy("abyssal_demon"));
        assertNotNull("Gargoyle must have strategy", strategyDatabase.getStrategy("gargoyle"));
        assertNotNull("Skeletal wyvern must have strategy", strategyDatabase.getStrategy("skeletal_wyvern"));

        // Verify regular monsters
        assertNotNull("Hill giant must have strategy", strategyDatabase.getStrategy("hill_giant"));
        assertNotNull("Cow must have strategy", strategyDatabase.getStrategy("cow"));
    }

    @Test
    public void testVorkathStrategyDetailsAndAccuracy() {
        MonsterStrategy vorkath = strategyDatabase.getStrategy("vorkath");
        assertNotNull(vorkath);
        assertEquals("Vorkath", vorkath.getMonsterName());
        assertEquals(732, vorkath.getCombatLevel());
        assertEquals(StrategyConfidence.VERIFIED, vorkath.getConfidence());
        assertTrue(vorkath.getWikiUrl().contains("oldschool.runescape.wiki"));

        // Check mechanics
        assertTrue(vorkath.getCombatMechanics().size() >= 3);
        boolean hasAcid = vorkath.getCombatMechanics().stream().anyMatch(m -> m.getName().contains("Acid"));
        boolean hasSpawn = vorkath.getCombatMechanics().stream().anyMatch(m -> m.getName().contains("Spawn"));
        assertTrue("Must detail Acid Phase mechanics", hasAcid);
        assertTrue("Must detail Zombified Spawn mechanics", hasSpawn);

        // Check special equipment advantages
        boolean hasDhc = vorkath.getSpecialEquipmentAdvantage().stream().anyMatch(e -> e.contains("Dragon hunter"));
        boolean hasSalve = vorkath.getSpecialEquipmentAdvantage().stream().anyMatch(e -> e.contains("Salve"));
        assertTrue("Must mention Dragon hunter weapon advantage", hasDhc);
        assertTrue("Must mention Salve amulet advantage", hasSalve);

        // Check methods
        assertTrue("Must offer at least 2 methods", vorkath.getMethods().size() >= 2);
        StrategyMethod bis = vorkath.getMethodByTier(StrategyTier.ENDGAME);
        assertNotNull("Must offer Endgame method", bis);
        assertEquals(CombatStyle.RANGED, bis.getCombatStyle());
        assertTrue(bis.getWhyThisWorks().contains("Dragon Hunter"));
    }

    @Test
    public void testPersonalizationMatchesPlayerAccount() {
        MonsterStrategy vorkath = strategyDatabase.getStrategy("vorkath");
        assertNotNull(vorkath);

        // High level account with 99 Ranged and DS2 completed
        PlayerAccount highLevel = PlayerAccount.builder("HighLevelPro")
                .level(Skill.RANGED, 99)
                .level(Skill.PRAYER, 77)
                .level(Skill.DEFENCE, 85)
                .level(Skill.HITPOINTS, 99)
                .quest("Dragon Slayer II")
                .build();

        List<PersonalizedStrategyScore> highScores = strategyService.personalize(vorkath, highLevel, 100_000_000L);
        assertFalse("High level scores must not be empty", highScores.isEmpty());

        PersonalizedStrategyScore topHigh = highScores.get(0);
        assertTrue("High level should meet all requirements", topHigh.isMeetsAllRequirements());
        assertTrue("Top method should be Best Overall", topHigh.isBestOverall());

        // Low level account without DS2 or Ranged
        PlayerAccount beginner = PlayerAccount.builder("NewPlayer")
                .level(Skill.RANGED, 50)
                .level(Skill.PRAYER, 43)
                .level(Skill.DEFENCE, 40)
                .build();

        List<PersonalizedStrategyScore> beginnerScores = strategyService.personalize(vorkath, beginner, 500_000L);
        assertFalse("Beginner scores must not be empty", beginnerScores.isEmpty());

        // Verify that missing requirements are flagged
        boolean hasLocked = beginnerScores.stream().anyMatch(s -> !s.isMeetsAllRequirements() && !s.getMissingRequirements().isEmpty());
        assertTrue("Beginner must have locked methods with missing requirements listed", hasLocked);
    }

    @Test
    public void testSearchAndFilteringStrategies() {
        // Search by category BOSS
        List<MonsterStrategy> bosses = strategyDatabase.searchStrategies("", MonsterCategory.BOSS, null, 0, 0);
        assertFalse(bosses.isEmpty());
        assertTrue(bosses.stream().allMatch(b -> b.getCategory() == MonsterCategory.BOSS));

        // Search by name "demon"
        List<MonsterStrategy> demons = strategyDatabase.searchStrategies("demon", null, null, 0, 0);
        assertFalse(demons.isEmpty());
        assertTrue(demons.stream().allMatch(d -> d.getMonsterName().toLowerCase().contains("demon") ||
                d.getWeaknesses().stream().anyMatch(w -> w.toLowerCase().contains("demon"))));

        // Search with budget limit 500k
        List<MonsterStrategy> budgetOptions = strategyDatabase.searchStrategies("", null, null, 0, 500_000L);
        assertFalse(budgetOptions.isEmpty());
        for (MonsterStrategy s : budgetOptions) {
            boolean hasBudget = s.getMethods().stream().anyMatch(m -> m.getEstimatedCostGp() <= 500_000L);
            assertTrue("Must contain a method within budget", hasBudget);
        }
    }

    @Test
    public void testIntegrationWithGearOptimizerAndInventoryBuilder() {
        MonsterStrategy vorkath = strategyDatabase.getStrategy("vorkath");
        assertNotNull(vorkath);
        StrategyMethod method = vorkath.getMethods().get(0);

        PlayerAccount account = PlayerAccount.createDefault();
        com.bosswise.model.OptimizedGearSetup gear = strategyService.getCalculatedGearSetup(method, vorkath, account, 50_000_000L);
        assertNotNull("Gear optimizer must calculate an equipment setup", gear);
        assertTrue("Must calculate positive DPS", gear.getCalculatedDps() > 0);
        assertNotNull("Must provide weapon slot", gear.getItem(com.bosswise.model.EquipmentSlot.WEAPON));

        com.bosswise.model.InventorySetup inventory = strategyService.getCalculatedInventorySetup(method, vorkath, account);
        assertNotNull("Inventory builder must generate an inventory", inventory);
        assertTrue("Inventory must not exceed 28 slots", inventory.getUsedSlots() <= 28);
        assertTrue("Inventory must be valid", inventory.isValid());
    }
}
