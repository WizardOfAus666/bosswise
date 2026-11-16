package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import com.bosswise.service.GePriceService;
import com.bosswise.service.InventoryBuilderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InventoryBuilderTest {
    private BossDatabase bossDatabase;
    private InventoryBuilderService service;

    @Before
    public void setup() {
        bossDatabase = new BossDatabase();
        GePriceService priceService = new GePriceService(null);
        service = new InventoryBuilderService(priceService);
    }

    @Test
    public void testInventorySetupCalculation() {
        BossData vorkath = bossDatabase.getBoss("vorkath");
        Assert.assertNotNull(vorkath);

        InventorySetup setup = service.getInventorySetup(vorkath, "Standard");
        Assert.assertNotNull(setup);
        Assert.assertFalse(setup.getItems().isEmpty());

        long totalCost = service.calculateTotalCost(setup.getItems());
        Assert.assertTrue(totalCost > 0L);

        // Check required anti-dragon / anti-venom items for Vorkath
        boolean hasAntifire = false;
        boolean hasAntiVenom = false;
        for (InventoryItem item : setup.getItems()) {
            if (item.getName().toLowerCase().contains("antifire")) hasAntifire = true;
            if (item.getName().toLowerCase().contains("anti-venom") || item.getName().toLowerCase().contains("antidote")) hasAntiVenom = true;
        }

        Assert.assertTrue("Vorkath inventory must include antifire", hasAntifire);
        Assert.assertTrue("Vorkath inventory must include anti-venom", hasAntiVenom);
    }
}
