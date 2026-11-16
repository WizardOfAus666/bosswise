package com.bosswise.service;

import com.bosswise.model.BossData;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InventoryBuilderService {
    public static final List<String> TIERS = Collections.unmodifiableList(Arrays.asList(
            "Budget", "Beginner", "Standard", "Advanced", "Maximum Efficiency"
    ));

    private final GePriceService priceService;

    @Inject
    public InventoryBuilderService(GePriceService priceService) {
        this.priceService = priceService;
    }

    public List<String> getAvailableTiers() {
        return TIERS;
    }

    public InventorySetup getInventorySetup(BossData boss, String tierName) {
        String tier = (tierName != null && !tierName.trim().isEmpty()) ? tierName.trim() : "Standard";

        List<InventoryItem> items;
        switch (tier.toLowerCase()) {
            case "budget":
                items = buildBudgetSetup(boss);
                break;
            case "beginner":
                items = buildBeginnerSetup(boss);
                break;
            case "advanced":
                items = buildAdvancedSetup(boss);
                break;
            case "maximum efficiency":
            case "max efficiency":
            case "max":
                items = buildMaxEfficiencySetup(boss);
                break;
            case "standard":
            default:
                items = buildStandardSetup(boss);
                break;
        }

        // Refresh with live GE prices
        List<InventoryItem> updatedItems = new ArrayList<>();
        for (InventoryItem item : items) {
            long livePrice = (priceService != null)
                    ? priceService.getPrice(item.getItemId(), item.getUnitPrice())
                    : item.getUnitPrice();
            updatedItems.add(new InventoryItem(
                    item.getItemId(),
                    item.getName(),
                    item.getQuantity(),
                    item.getCategory(),
                    (int) Math.min(Integer.MAX_VALUE, livePrice),
                    item.isMandatory(),
                    item.getNote()
            ));
        }

        return new InventorySetup(tier, updatedItems);
    }

    private List<InventoryItem> buildBudgetSetup(BossData boss) {
        List<InventoryItem> items = new ArrayList<>();
        String bId = boss != null ? boss.getId() : "";

        // Boss-specific mandatory utility
        addBossSpecificItems(items, bId);

        // Affordable food & potions
        items.add(new InventoryItem(379, "Lobster", 8, "Food", 250, false, "Budget healing"));
        items.add(new InventoryItem(385, "Shark", 10, "Food", 900, true, "Standard healing"));
        items.add(new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"));
        items.add(new InventoryItem(161, "Super attack(4)", 1, "Combat Potion", 1200, false, "Attack boost"));
        items.add(new InventoryItem(157, "Super strength(4)", 1, "Combat Potion", 3200, false, "Strength boost"));
        items.add(new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "Emergency escape"));

        return items;
    }

    private List<InventoryItem> buildBeginnerSetup(BossData boss) {
        List<InventoryItem> items = new ArrayList<>();
        String bId = boss != null ? boss.getId() : "";

        addBossSpecificItems(items, bId);

        // Safety-focused: combo eats, extra restores
        items.add(new InventoryItem(385, "Shark", 12, "Food", 900, true, "Primary burst healing"));
        items.add(new InventoryItem(3144, "Cooked karambwan", 5, "Food", 600, true, "Tick-eating combo"));
        items.add(new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11500, true, "Stat restore + prayer"));
        items.add(new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12500, true, "Combined boost"));
        items.add(new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "Safe escape"));

        return items;
    }

    private List<InventoryItem> buildStandardSetup(BossData boss) {
        List<InventoryItem> items = new ArrayList<>();
        String bId = boss != null ? boss.getId() : "";

        addBossSpecificItems(items, bId);

        items.add(new InventoryItem(385, "Shark", 10, "Food", 900, true, "Burst healing"));
        items.add(new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"));
        items.add(new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11500, true, "Prayer sustain"));
        items.add(new InventoryItem(23685, "Divine super combat potion(4)", 1, "Combat Potion", 18500, true, "Maintained 5-min boost"));
        items.add(new InventoryItem(12791, "Rune pouch", 1, "Rune", 0, false, "Dust, Law, Soul runes"));
        items.add(new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "Quick bank & pool reset"));

        return items;
    }

    private List<InventoryItem> buildAdvancedSetup(BossData boss) {
        List<InventoryItem> items = new ArrayList<>();
        String bId = boss != null ? boss.getId() : "";

        // Spec weapon
        items.add(new InventoryItem(11802, "Armadyl godsword", 1, "Special Attack", 12000000, false, "High burst spec"));

        addBossSpecificItems(items, bId);

        items.add(new InventoryItem(13441, "Anglerfish", 6, "Food", 1600, true, "Over-heal & emergency"));
        items.add(new InventoryItem(3144, "Cooked karambwan", 3, "Food", 600, true, "Combo heal"));
        items.add(new InventoryItem(6685, "Saradomin brew(4)", 2, "Food", 7500, false, "High capacity HP"));
        items.add(new InventoryItem(3024, "Super restore(4)", 5, "Prayer Restore", 11500, true, "Prayer & brew restore"));
        items.add(new InventoryItem(23685, "Divine super combat potion(4)", 1, "Combat Potion", 18500, true, "Combat boost"));
        items.add(new InventoryItem(12791, "Rune pouch", 1, "Rune", 0, false, "Teleports & thralls"));
        items.add(new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "House reset"));

        return items;
    }

    private List<InventoryItem> buildMaxEfficiencySetup(BossData boss) {
        List<InventoryItem> items = new ArrayList<>();
        String bId = boss != null ? boss.getId() : "";

        // Dual spec weapon & ring switch
        items.add(new InventoryItem(13576, "Dragon warhammer", 1, "Special Attack", 26000000, true, "Defence reduction spec"));
        items.add(new InventoryItem(27641, "Voidwaker", 1, "Special Attack", 115000000, false, "Guaranteed damage spec"));
        items.add(new InventoryItem(25975, "Lightbearer", 1, "Switch", 3500000, false, "2x Spec recharge"));

        addBossSpecificItems(items, bId);

        // Lean food, max prayer restores for continuous multi-kill trips
        items.add(new InventoryItem(13441, "Anglerfish", 3, "Food", 1600, false, "Emergency HP"));
        items.add(new InventoryItem(6685, "Saradomin brew(4)", 2, "Food", 7500, false, "Dense HP"));
        items.add(new InventoryItem(3024, "Super restore(4)", 7, "Prayer Restore", 11500, true, "Trip extension"));
        items.add(new InventoryItem(23685, "Divine super combat potion(4)", 2, "Combat Potion", 18500, true, "Extended boost"));
        items.add(new InventoryItem(12791, "Rune pouch", 1, "Rune", 0, true, "Arceuus resurrection thralls"));
        items.add(new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "Instant bank"));

        return items;
    }

    private void addBossSpecificItems(List<InventoryItem> items, String bossId) {
        if (bossId == null) return;
        switch (bossId.toLowerCase()) {
            case "vorkath":
                items.add(new InventoryItem(22209, "Extended super antifire(4)", 1, "Protection", 14000, true, "Mandatory dragonfire immunity"));
                items.add(new InventoryItem(12913, "Venom gland / Anti-venom+(4)", 1, "Protection", 11000, true, "Venom immunity"));
                items.add(new InventoryItem(12791, "Rune pouch (Crumble Undead)", 1, "Rune", 0, true, "Chaos, Death, Dust runes for Zombified Spawn"));
                break;
            case "zulrah":
                items.add(new InventoryItem(12913, "Anti-venom+(4)", 1, "Protection", 11000, true, "Cloud and minion venom protection"));
                items.add(new InventoryItem(2552, "Ring of dueling(8)", 1, "Teleport", 1200, false, "Ferox Enclave reset"));
                break;
            case "barrows":
                items.add(new InventoryItem(952, "Spade", 1, "Utility", 100, true, "Mandatory: Dig into crypts"));
                items.add(new InventoryItem(19629, "Barrows teleport", 3, "Teleport", 3500, false, "Fast crypt transit"));
                break;
            case "phantom_muspah":
                items.add(new InventoryItem(21948, "Sapphire dragon bolts (e)", 50, "Ammo", 1400, true, "Shield phase soul smite"));
                items.add(new InventoryItem(12791, "Rune pouch (Ice Barrage)", 1, "Rune", 0, true, "Blood, Death, Water runes to freeze"));
                break;
            case "gargoyles":
                items.add(new InventoryItem(4162, "Rock hammer", 1, "Utility", 500, true, "Mandatory: Smash brittle gargoyle"));
                break;
            case "alchemical_hydra":
                items.add(new InventoryItem(12913, "Anti-venom+(4)", 1, "Protection", 11000, true, "Poison phase protection"));
                break;
        }
    }

    public long calculateTotalCost(List<InventoryItem> items) {
        if (items == null) return 0L;
        long sum = 0L;
        for (InventoryItem item : items) {
            sum += item.getTotalCost();
        }
        return sum;
    }
}
