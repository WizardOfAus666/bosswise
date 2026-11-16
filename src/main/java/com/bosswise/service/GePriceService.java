package com.bosswise.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.game.ItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GePriceService {
    private static final Logger log = LoggerFactory.getLogger(GePriceService.class);

    private final ItemManager itemManager;
    private final Map<Integer, Long> priceCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastFetchTimes = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final long CACHE_TTL_MS = 30 * 60 * 1000L; // 30 minutes

    @Inject
    public GePriceService(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    public long getPrice(int itemId, long defaultPrice) {
        if (itemId <= 0) return 0L;

        Long cached = priceCache.get(itemId);
        Long lastFetch = lastFetchTimes.get(itemId);
        long now = System.currentTimeMillis();

        if (cached != null && lastFetch != null && (now - lastFetch < CACHE_TTL_MS)) {
            return cached;
        }

        if (itemManager != null) {
            try {
                long price = itemManager.getItemPrice(itemId);
                if (price > 0) {
                    priceCache.put(itemId, price);
                    lastFetchTimes.put(itemId, now);
                    return price;
                }
            } catch (Throwable ex) {
                // Catches Exception as well as AssertionError if called when assertions enabled
                log.trace("ItemManager failed to fetch price for id {}: {}", itemId, ex.getMessage());
            }
        }

        // Return cached or default baseline price
        if (cached != null && cached > 0) {
            return cached;
        }
        return defaultPrice;
    }

    public void primePricesAsync(Iterable<Integer> itemIds) {
        if (itemManager == null || itemIds == null) return;

        executor.submit(() -> {
            long now = System.currentTimeMillis();
            for (int id : itemIds) {
                if (id <= 0) continue;
                try {
                    long price = itemManager.getItemPrice(id);
                    if (price > 0) {
                        priceCache.put(id, price);
                        lastFetchTimes.put(id, now);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public boolean isPriceStale(int itemId) {
        Long lastFetch = lastFetchTimes.get(itemId);
        if (lastFetch == null) return true;
        return (System.currentTimeMillis() - lastFetch) > CACHE_TTL_MS;
    }

    public void clearCache() {
        priceCache.clear();
        lastFetchTimes.clear();
    }
}
