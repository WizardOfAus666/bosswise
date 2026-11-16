package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.BankGearCheckResult;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.OptimizedGearSetup;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BankCheckerService {
    private static final Logger log = LoggerFactory.getLogger(BankCheckerService.class);

    private final Client client;
    private final EquipmentDatabase equipmentDatabase;
    private final GePriceService priceService;

    private final Set<Integer> ownedItemIds = new HashSet<>();
    private boolean bankHasBeenScanned = false;

    @Inject
    public BankCheckerService(Client client, EquipmentDatabase equipmentDatabase, GePriceService priceService) {
        this.client = client;
        this.equipmentDatabase = equipmentDatabase;
        this.priceService = priceService;
    }

    public synchronized void scanBank() {
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
        if (bankContainer != null) {
            Item[] items = bankContainer.getItems();
            for (Item item : items) {
                if (item != null && item.getId() > 0) {
                    ownedItemIds.add(item.getId());
                }
            }
            bankHasBeenScanned = true;
        }

        // Also add items from inventory and equipment
        ItemContainer invContainer = client.getItemContainer(InventoryID.INVENTORY);
        if (invContainer != null) {
            for (Item item : invContainer.getItems()) {
                if (item != null && item.getId() > 0) {
                    ownedItemIds.add(item.getId());
                }
            }
        }

        ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipContainer != null) {
            for (Item item : equipContainer.getItems()) {
                if (item != null && item.getId() > 0) {
                    ownedItemIds.add(item.getId());
                }
            }
        }
    }

    public void setCustomOwnedItemIds(Set<Integer> customIds) {
        this.ownedItemIds.clear();
        if (customIds != null) {
            this.ownedItemIds.addAll(customIds);
            this.bankHasBeenScanned = true;
        }
    }

    public boolean isItemOwned(int itemId) {
        return ownedItemIds.contains(itemId);
    }

    public boolean isBankScanned() {
        return bankHasBeenScanned;
    }

    public BankGearCheckResult checkSetupAgainstBank(OptimizedGearSetup setup) {
        if (setup == null) {
            return new BankGearCheckResult(Collections.emptyMap(), Collections.emptyMap(), 0L, 0, 0, bankHasBeenScanned);
        }

        Map<EquipmentSlot, EquipmentItem> owned = new EnumMap<>(EquipmentSlot.class);
        Map<EquipmentSlot, EquipmentItem> missing = new EnumMap<>(EquipmentSlot.class);
        long missingCost = 0L;
        int totalSlots = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.SPEC_WEAPON) continue;
            EquipmentItem item = setup.getItem(slot);
            if (item != null) {
                totalSlots++;
                if (isItemOwned(item.getItemId())) {
                    owned.put(slot, item);
                } else {
                    missing.put(slot, item);
                    long price = item.getPrice();
                    if (price <= 0 && priceService != null) {
                        price = priceService.getPrice(item.getItemId(), 0);
                    }
                    missingCost += Math.max(0, price);
                }
            }
        }

        return new BankGearCheckResult(
                owned,
                missing,
                missingCost,
                owned.size(),
                totalSlots,
                bankHasBeenScanned
        );
    }
}
