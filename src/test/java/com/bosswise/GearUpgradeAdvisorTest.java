package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.GearUpgradeRecommendation;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import com.bosswise.service.GearUpgradeAdvisor;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GearUpgradeAdvisorTest {
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

    @Test
    public void testUpgradeSuggestions() {
        PlayerAccount account = PlayerAccount.createDefault();
        BossData boss = bossDatabase.getBoss("giant_mole");

        // Start with cheapest setup
        OptimizedGearSetup current = optimizer.generateSetup(
                "Cheapest Effective", boss, CombatStyle.MELEE, 500_000L, account, PriorityMode.BALANCED
        );

        List<GearUpgradeRecommendation> upgrades = advisor.findUpgrades(current, 5_000_000L, account);
        Assert.assertFalse(upgrades.isEmpty());

        GearUpgradeRecommendation top = upgrades.get(0);
        Assert.assertTrue(top.getUpgradeItem().getTier() > top.getCurrentItem().getTier());
        Assert.assertNotNull(top.getStatComparison());
        Assert.assertNotNull(top.getWhyItMatters());
    }
}
