package com.bosswise.service;

import com.bosswise.model.WatchlistItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class PriceWatcherService {
    private final GePriceService priceService;
    private final ConfigManager configManager;
    private final List<WatchlistItem> watchlist = new CopyOnWriteArrayList<>();

    @Inject
    public PriceWatcherService(GePriceService priceService, ConfigManager configManager) {
        this.priceService = priceService;
        this.configManager = configManager;
        initDefaultWatchlist();
    }

    private void initDefaultWatchlist() {
        // High impact bossing items
        addItem(26219, "Osmumten's fang", 12500000, 11000000);
        addItem(25865, "Bow of faerdhinen", 115000000, 105000000);
        addItem(21012, "Dragon hunter crossbow", 58000000, 52000000);
        addItem(11832, "Bandos chestplate", 28000000, 25000000);
        addItem(13576, "Dragon warhammer", 26000000, 22000000);
    }

    public List<WatchlistItem> getWatchlist() {
        return Collections.unmodifiableList(watchlist);
    }

    public synchronized void addItem(int itemId, String name, long defaultPrice, long targetPrice) {
        for (WatchlistItem item : watchlist) {
            if (item.getItemId() == itemId) {
                item.setTargetPrice(targetPrice);
                return;
            }
        }
        long livePrice = (priceService != null) ? priceService.getPrice(itemId, defaultPrice) : defaultPrice;
        watchlist.add(new WatchlistItem(itemId, name, livePrice, targetPrice));
    }

    public synchronized void removeItem(int itemId) {
        watchlist.removeIf(item -> item.getItemId() == itemId);
    }

    public synchronized void updateTargetPrice(int itemId, long newTarget) {
        for (WatchlistItem item : watchlist) {
            if (item.getItemId() == itemId) {
                item.setTargetPrice(newTarget);
                break;
            }
        }
    }

    public synchronized void refreshPrices() {
        if (priceService == null) return;
        for (WatchlistItem item : watchlist) {
            long live = priceService.getPrice(item.getItemId(), item.getCurrentPrice());
            if (live > 0) {
                item.setCurrentPrice(live);
            }
        }
    }
}
