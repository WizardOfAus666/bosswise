package com.bosswise;

import com.bosswise.service.GePriceService;
import org.junit.Assert;
import org.junit.Test;

public class GePriceServiceTest {

    @Test
    public void testPriceServiceFallbackAndCaching() {
        GePriceService service = new GePriceService(null);

        // When offline or no ItemManager, returns baseline default price
        long price = service.getPrice(4151, 1_600_000L);
        Assert.assertEquals(1_600_000L, price);

        // Zero or invalid item returns 0
        Assert.assertEquals(0L, service.getPrice(0, 500L));
        Assert.assertEquals(0L, service.getPrice(-1, 500L));

        // Test cache clearing
        service.clearCache();
        Assert.assertTrue(service.isPriceStale(4151));
    }
}
