package com.bosswise.scanner;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.CurrentGearComparison;
import com.bosswise.model.CurrentGearComparison.SlotUpgrade;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.GePriceService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CurrentGearScannerService {
    private static final Logger log = LoggerFactory.getLogger(CurrentGearScannerService.class);

    private final Client client;
    private final EquipmentDatabase equipmentDatabase;
    private final GePriceService priceService;
    private final List<Consumer<Map<EquipmentSlot, EquipmentItem>>> listeners = new CopyOnWriteArrayList<>();

    private volatile Map<EquipmentSlot, EquipmentItem> currentEquipment = new EnumMap<>(EquipmentSlot.class);

    @Inject
    public CurrentGearScannerService(Client client, EquipmentDatabase equipmentDatabase, GePriceService priceService) {
        this.client = client;
        this.equipmentDatabase = equipmentDatabase;
        this.priceService = priceService;
    }

    public void addGearListener(Consumer<Map<EquipmentSlot, EquipmentItem>> listener) {
        if (listener != null) {
            listeners.add(listener);
            listener.accept(getCurrentEquipment());
        }
    }

    public void removeGearListener(Consumer<Map<EquipmentSlot, EquipmentItem>> listener) {
        listeners.remove(listener);
    }

    public Map<EquipmentSlot, EquipmentItem> getCurrentEquipment() {
        if (currentEquipment.isEmpty() && (client == null || client.getGameState() != GameState.LOGGED_IN)) {
            return getFallbackEquipment();
        }
        return Collections.unmodifiableMap(currentEquipment);
    }

    public synchronized void scanEquippedGear() {
        if (client == null || client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipContainer == null) {
            return;
        }

        Map<EquipmentSlot, EquipmentItem> scanned = new EnumMap<>(EquipmentSlot.class);
        Item[] items = equipContainer.getItems();

        for (EquipmentInventorySlot slot : EquipmentInventorySlot.values()) {
            int slotIdx = slot.getSlotIdx();
            if (slotIdx >= 0 && slotIdx < items.length) {
                Item item = items[slotIdx];
                if (item != null && item.getId() > 0) {
                    EquipmentSlot bwSlot = mapRuneLiteSlot(slot);
                    if (bwSlot != null) {
                        EquipmentItem eqItem = resolveEquipmentItem(item.getId(), bwSlot);
                        if (eqItem != null) {
                            scanned.put(bwSlot, eqItem);
                        }
                    }
                }
            }
        }

        this.currentEquipment = scanned;
        notifyListeners();
    }

    public void setCustomEquipment(Map<EquipmentSlot, EquipmentItem> custom) {
        this.currentEquipment = custom != null ? new EnumMap<>(custom) : new EnumMap<>(EquipmentSlot.class);
        notifyListeners();
    }

    private void notifyListeners() {
        Map<EquipmentSlot, EquipmentItem> immutable = Collections.unmodifiableMap(currentEquipment);
        for (Consumer<Map<EquipmentSlot, EquipmentItem>> listener : listeners) {
            try {
                listener.accept(immutable);
            } catch (Exception ex) {
                log.error("Error notifying gear listener", ex);
            }
        }
    }

    private EquipmentItem resolveEquipmentItem(int itemId, EquipmentSlot slot) {
        EquipmentItem item = equipmentDatabase.getItem(itemId);
        if (item != null) {
            return item;
        }
        // If item is not explicitly indexed, build lightweight representation
        long price = (priceService != null) ? priceService.getPrice(itemId, 0) : 0;
        String name = "Item #" + itemId;
        if (client != null && client.getItemDefinition(itemId) != null) {
            name = client.getItemDefinition(itemId).getName();
        }
        return EquipmentItem.createSimple(itemId, name, slot, price, 5, CombatStyle.MELEE);
    }

    private EquipmentSlot mapRuneLiteSlot(EquipmentInventorySlot slot) {
        switch (slot) {
            case HEAD: return EquipmentSlot.HEAD;
            case CAPE: return EquipmentSlot.CAPE;
            case AMULET: return EquipmentSlot.AMULET;
            case WEAPON: return EquipmentSlot.WEAPON;
            case BODY: return EquipmentSlot.BODY;
            case SHIELD: return EquipmentSlot.SHIELD;
            case LEGS: return EquipmentSlot.LEGS;
            case GLOVES: return EquipmentSlot.GLOVES;
            case BOOTS: return EquipmentSlot.BOOTS;
            case RING: return EquipmentSlot.RING;
            case AMMO: return EquipmentSlot.AMMO;
            default: return null;
        }
    }

    public long calculateGearValue(Map<EquipmentSlot, EquipmentItem> gear) {
        if (gear == null) return 0L;
        long total = 0L;
        for (EquipmentItem item : gear.values()) {
            if (item != null && item.getPrice() > 0) {
                total += item.getPrice();
            }
        }
        return total;
    }

    public CurrentGearComparison compareWithRecommended(
            OptimizedGearSetup recommended,
            MonsterCombatProfile profile,
            CombatCalculationService combatCalc,
            PlayerAccount account) {

        Map<EquipmentSlot, EquipmentItem> current = getCurrentEquipment();
        CombatStyle style = (recommended != null) ? recommended.getCombatStyle() : CombatStyle.MELEE;

        CombatResult currentResult = (combatCalc != null && profile != null && account != null)
                ? combatCalc.calculateCombat(current, null, profile, account, style, false)
                : null;

        CombatResult recResult = (recommended != null) ? recommended.getCombatResult() : null;

        long currentVal = calculateGearValue(current);
        long recVal = (recommended != null) ? recommended.getTotalPrice() : 0L;

        List<String> missingProtections = new ArrayList<>();
        if (profile != null) {
            if (profile.isRequiresDragonfire()) {
                EquipmentItem shield = current.get(EquipmentSlot.SHIELD);
                EquipmentItem weapon = current.get(EquipmentSlot.WEAPON);
                boolean hasDfire = (shield != null && shield.isDragonfireProtection());
                if (!hasDfire && (weapon == null || !weapon.isTwoHanded())) {
                    missingProtections.add("Requires Dragonfire Protection (Anti-dragon shield / DFS)");
                }
            }
            if (profile.isRequiresMirrorShield()) {
                EquipmentItem shield = current.get(EquipmentSlot.SHIELD);
                if (shield == null || !shield.isMirrorShield()) {
                    missingProtections.add("Requires Mirror Shield or V's Shield (Basilisks)");
                }
            }
            if (profile.isRequiresLeafBladed()) {
                EquipmentItem weapon = current.get(EquipmentSlot.WEAPON);
                if (weapon == null || !weapon.isLeafBladed()) {
                    missingProtections.add("Requires Leaf-bladed weapon (Kurasks/Turoths)");
                }
            }
            if (profile.isRequiresSpear()) {
                EquipmentItem weapon = current.get(EquipmentSlot.WEAPON);
                if (weapon == null || (!weapon.isSpear() && !weapon.getName().toLowerCase().contains("fang"))) {
                    missingProtections.add("Requires Spear/Hasta/Fang (Corporeal Beast 50% damage penalty)");
                }
            }
            if (profile.isRequiresBootsOfStone()) {
                EquipmentItem boots = current.get(EquipmentSlot.BOOTS);
                if (boots == null || !boots.isBootsOfStone()) {
                    missingProtections.add("Requires Boots of Stone / Brimstone Boots (Karuulm Dungeon heat)");
                }
            }
        }

        List<SlotUpgrade> upgrades = new ArrayList<>();
        if (recommended != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot == EquipmentSlot.SPEC_WEAPON) continue;
                EquipmentItem cur = current.get(slot);
                EquipmentItem rec = recommended.getItem(slot);
                if (rec != null && (cur == null || cur.getItemId() != rec.getItemId())) {
                    long costDiff = Math.max(0, rec.getPrice() - (cur != null ? cur.getPrice() : 0));
                    String impact = (cur == null)
                            ? "Empty slot - adds " + rec.getName()
                            : "Upgrade from " + cur.getName() + " to " + rec.getName() + " (Tier " + rec.getTier() + ")";
                    upgrades.add(new SlotUpgrade(slot, cur, rec, costDiff, impact));
                }
            }
        }

        return new CurrentGearComparison(
                current,
                recommended,
                currentResult,
                recResult,
                currentVal,
                recVal,
                missingProtections,
                upgrades
        );
    }

    private Map<EquipmentSlot, EquipmentItem> getFallbackEquipment() {
        Map<EquipmentSlot, EquipmentItem> fallback = new EnumMap<>(EquipmentSlot.class);
        putFallback(fallback, EquipmentSlot.HEAD, 10828, "Helm of neitiznot", 50000, 5, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.CAPE, 6570, "Fire cape", 0, 7, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.AMULET, 1704, "Amulet of glory", 12000, 4, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.WEAPON, 4587, "Dragon scimitar", 60000, 5, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.BODY, 10551, "Fighter torso", 0, 6, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.SHIELD, 8850, "Rune defender", 0, 5, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.LEGS, 1079, "Rune platelegs", 38000, 4, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.GLOVES, 7462, "Barrows gloves", 100000, 7, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.BOOTS, 11840, "Dragon boots", 140000, 6, CombatStyle.MELEE);
        putFallback(fallback, EquipmentSlot.RING, 6737, "Berserker ring", 2500000, 6, CombatStyle.MELEE);
        return fallback;
    }

    private void putFallback(Map<EquipmentSlot, EquipmentItem> map, EquipmentSlot slot, int id, String name, long price, int tier, CombatStyle style) {
        EquipmentItem item = equipmentDatabase.getItem(id);
        if (item == null) {
            item = equipmentDatabase.getItem(name);
        }
        if (item != null) {
            map.put(slot, item);
        } else {
            map.put(slot, EquipmentItem.createSimple(id, name, slot, price, tier, style));
        }
    }
}
