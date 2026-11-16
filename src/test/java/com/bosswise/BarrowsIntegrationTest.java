package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterData;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.CombatStyleEvaluator;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.ui.GearOptimizerPanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComboBox;
import java.lang.reflect.Field;

public class BarrowsIntegrationTest {
    private BossDatabase bossDatabase;
    private MonsterDatabase monsterDatabase;
    private AccountScannerService accountScanner;
    private GearOptimizerService optimizerService;
    private GearUpgradeAdvisor upgradeAdvisor;
    private CombatStyleEvaluator styleEvaluator;

    @Before
    public void setUp() {
        bossDatabase = new BossDatabase();
        monsterDatabase = new MonsterDatabase();
        EquipmentDatabase equipmentDatabase = new EquipmentDatabase();
        GePriceService priceService = new GePriceService(null);
        optimizerService = new GearOptimizerService(equipmentDatabase, priceService);
        upgradeAdvisor = new GearUpgradeAdvisor(equipmentDatabase, optimizerService, priceService);
        styleEvaluator = new CombatStyleEvaluator(optimizerService, new CombatCalculationService());
        accountScanner = new AccountScannerService(null, null);
    }

    @Test
    public void testBarrowsInMonsterDatabase() {
        MonsterData barrows = monsterDatabase.getMonster("barrows");
        Assert.assertNotNull("Barrows must exist in MonsterDatabase", barrows);
        Assert.assertEquals("Barrows Brothers", barrows.getName());
        Assert.assertEquals(MonsterCategory.BOSS, barrows.getCategory());
        Assert.assertEquals(CombatStyle.MAGIC, barrows.getPrimaryStyle());
    }

    @Test
    public void testBarrowsInGearOptimizerDropdown() throws Exception {
        GearOptimizerPanel panel = new GearOptimizerPanel(
                bossDatabase, monsterDatabase, accountScanner, optimizerService, upgradeAdvisor, styleEvaluator
        );

        Field monsterSelField = GearOptimizerPanel.class.getDeclaredField("monsterSelector");
        monsterSelField.setAccessible(true);
        JComboBox<?> monsterSelector = (JComboBox<?>) monsterSelField.get(panel);

        boolean foundBarrows = false;
        for (int i = 0; i < monsterSelector.getItemCount(); i++) {
            if ("Barrows Brothers".equalsIgnoreCase(String.valueOf(monsterSelector.getItemAt(i)))) {
                foundBarrows = true;
                break;
            }
        }
        Assert.assertTrue("Barrows Brothers must appear in default Bosses dropdown in GearOptimizerPanel", foundBarrows);

        // Test selecting Barrows
        BossData barrowsBoss = bossDatabase.getBoss("barrows");
        panel.selectBoss(barrowsBoss);
        Assert.assertEquals("Barrows Brothers", monsterSelector.getSelectedItem());
    }

    @Test
    public void testBarrowsGearOptimization() {
        MonsterData barrows = monsterDatabase.getMonster("barrows");
        PlayerAccount account = PlayerAccount.createDefault();
        OptimizedGearSetup setup = optimizerService.generateSetup(
                "Best Value", barrows.toBossData(), CombatStyle.MAGIC, 5_000_000L, account, PriorityMode.BALANCED
        );
        Assert.assertNotNull("Setup must not be null", setup);
        Assert.assertTrue("Setup cost must be within budget", setup.getTotalCost() <= 5_000_000L);
        Assert.assertNotNull("Weapon must be recommended", setup.getItem(com.bosswise.model.EquipmentSlot.WEAPON));
    }
}
