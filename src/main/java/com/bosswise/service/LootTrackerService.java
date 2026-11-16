package com.bosswise.service;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.LootDropItem;
import com.bosswise.model.LootSession;
import com.bosswise.model.MonsterData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LootTrackerService {
    private static final Logger log = LoggerFactory.getLogger(LootTrackerService.class);

    private final GePriceService priceService;
    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final Client client;

    private volatile LootSession activeSession = new LootSession("General Session");
    private final List<LootSession> sessionHistory = new CopyOnWriteArrayList<>();
    private final List<Consumer<LootSession>> sessionListeners = new CopyOnWriteArrayList<>();

    // Lifetime metrics
    private int lifetimeKills = 0;
    private long lifetimeLootValue = 0L;
    private long lifetimeSupplyCosts = 0L;

    @Inject
    public LootTrackerService(
            GePriceService priceService,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            Client client) {
        this.priceService = priceService;
        this.bossDatabase = bossDatabase;
        this.monsterDatabase = monsterDatabase;
        this.client = client;
    }

    public void addSessionListener(Consumer<LootSession> listener) {
        if (listener != null) {
            sessionListeners.add(listener);
            listener.accept(activeSession);
        }
    }

    public void removeSessionListener(Consumer<LootSession> listener) {
        sessionListeners.remove(listener);
    }

    public LootSession getActiveSession() {
        return activeSession;
    }

    public List<LootSession> getSessionHistory() {
        return Collections.unmodifiableList(sessionHistory);
    }

    public synchronized void startNewSession(String monsterName) {
        if (activeSession.getKills() > 0 || !activeSession.getDrops().isEmpty()) {
            sessionHistory.add(activeSession);
        }
        activeSession = new LootSession(monsterName != null ? monsterName : "General Session");
        notifyListeners();
    }

    public synchronized void resetCurrentSession() {
        activeSession = new LootSession(activeSession.getMonsterName());
        notifyListeners();
    }

    public synchronized void clearAllHistory() {
        sessionHistory.clear();
        lifetimeKills = 0;
        lifetimeLootValue = 0L;
        lifetimeSupplyCosts = 0L;
        resetCurrentSession();
    }

    public synchronized void onNpcLoot(NPC npc, Collection<ItemStack> items) {
        if (npc == null || items == null || items.isEmpty()) {
            return;
        }

        String npcName = npc.getName();
        if (npcName == null) npcName = "Unknown NPC";

        // Estimate supply cost based on encounter tier
        long supplyCost = estimateSupplyCostForNpc(npcName);

        // Record the kill in the active session
        activeSession.recordKill(supplyCost);
        lifetimeKills++;
        lifetimeSupplyCosts += supplyCost;

        List<LootDropItem> drops = new ArrayList<>();
        for (ItemStack item : items) {
            int itemId = item.getId();
            int qty = item.getQuantity();

            long unitPrice = (priceService != null) ? priceService.getPrice(itemId, 0) : 0;
            String itemName = "Item #" + itemId;
            if (client != null && client.getItemDefinition(itemId) != null) {
                itemName = client.getItemDefinition(itemId).getName();
            }

            LootDropItem drop = new LootDropItem(itemId, itemName, qty, unitPrice, null);
            drops.add(drop);
            lifetimeLootValue += drop.getTotalValue();
        }

        activeSession.addDrops(drops);
        notifyListeners();
    }

    private long estimateSupplyCostForNpc(String npcName) {
        String lower = npcName.toLowerCase();
        if (lower.contains("vorkath") || lower.contains("zulrah") || lower.contains("muspah")) {
            return 8000L; // antifires, antivenom, super combat, dragon bolts
        } else if (lower.contains("corp") || lower.contains("nex") || lower.contains("chambers") || lower.contains("theatre")) {
            return 25000L; // brews, super restores, blood runes
        } else if (lower.contains("graardor") || lower.contains("kril") || lower.contains("kree") || lower.contains("zilyana")) {
            return 12000L;
        } else if (lower.contains("barrows")) {
            return 2500L; // prayer pots, spade, runes
        } else if (lower.contains("scurrius")) {
            return 1500L; // low food, prayer
        }
        return 800L; // standard slayer monster food
    }

    private void notifyListeners() {
        for (Consumer<LootSession> listener : sessionListeners) {
            try {
                listener.accept(activeSession);
            } catch (Exception ex) {
                log.error("Error notifying loot session listener", ex);
            }
        }
    }

    public int getLifetimeKills() { return lifetimeKills; }
    public long getLifetimeLootValue() { return lifetimeLootValue; }
    public long getLifetimeSupplyCosts() { return lifetimeSupplyCosts; }
    public long getLifetimeNetProfit() { return lifetimeLootValue - lifetimeSupplyCosts; }
}
