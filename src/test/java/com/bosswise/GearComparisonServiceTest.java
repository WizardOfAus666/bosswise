package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MultiSetupComparison;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearComparisonService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.InventoryBuilderService;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GearComparisonServiceTest {
    private GearComparisonService comparisonService;
    private GearOptimizerService optimizerService;
    private BossDatabase bossDatabase;

    @Before
    public void setUp() {
        EquipmentDatabase eqDb = new EquipmentDatabase();
        MonsterDatabase mDb = new MonsterDatabase();
        bossDatabase = new BossDatabase();
        GePriceService priceService = new GePriceService(null);
        optimizerService = new GearOptimizerService(eqDb, priceService, mDb, new CombatCalculationService());
        InventoryBuilderService invService = new InventoryBuilderService(priceService);
        comparisonService = new GearComparisonService(invService);
    }

    @Test
    public void testSideBySideComparisonOfUpToFiveSetups() {
        BossData vorkath = bossDatabase.getBoss("vorkath");
        assertNotNull(vorkath);

        PlayerAccount account = PlayerAccount.createDefault();
        List<OptimizedGearSetup> setups = optimizerService.generateAllViableSetups(vorkath, null, account, 50_000_000L, false);
        assertNotNull(setups);
        assertTrue(setups.size() >= 3);

        MultiSetupComparison comp = comparisonService.compareSetups(setups);
        assertNotNull(comp);
        assertTrue("Comparison must include at most 5 setups", comp.getSetups().size() <= 5);
        assertFalse("Metrics must not be empty", comp.getMetrics().isEmpty());

        // Check slot matrix
        assertNotNull(comp.getSlotMatrix());
        assertTrue(comp.getSlotMatrix().containsKey(EquipmentSlot.WEAPON));
        assertTrue(comp.getSlotMatrix().containsKey(EquipmentSlot.BODY));

        // Check insights
        assertFalse("Insights should be generated", comp.getInsights().isEmpty());
        assertTrue("Insights should highlight Top DPS", comp.getInsights().get(0).contains("Top DPS"));

        // Check matched inventories
        assertNotNull(comp.getMatchedInventories());
        assertFalse(comp.getMatchedInventories().isEmpty());
    }
}
