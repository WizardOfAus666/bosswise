package com.bosswise.service;

import com.bosswise.model.CollectionLogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
public class CollectionLogService {
    private final List<CollectionLogEntry> entries = new ArrayList<>();
    private final Set<String> obtainedKeys = new HashSet<>();

    @com.google.inject.Inject(optional = true)
    private ConfigManager configManager;

    @Inject
    public CollectionLogService() {
        initEntries();
        loadSavedProgress();
    }

    public CollectionLogService(ConfigManager configManager) {
        this.configManager = configManager;
        initEntries();
        loadSavedProgress();
    }

    private void initEntries() {
        // Vorkath
        add("Bosses", "Vorkath", "Vorkath's Head", 22111, "1/50", false);
        add("Bosses", "Vorkath", "Draconic Visage", 11286, "1/5000", false);
        add("Bosses", "Vorkath", "Skeletal Visage", 22006, "1/5000", false);
        add("Bosses", "Vorkath", "Jar of Decay", 22106, "1/3000", false);
        add("Bosses", "Vorkath", "Vorki", 21992, "1/3000", false);

        // Zulrah
        add("Bosses", "Zulrah", "Tanzanite Fang", 12922, "1/512 (x2 rolls)", false);
        add("Bosses", "Zulrah", "Magic Fang", 12932, "1/512 (x2 rolls)", false);
        add("Bosses", "Zulrah", "Serpentine Visage", 12927, "1/512 (x2 rolls)", false);
        add("Bosses", "Zulrah", "Tanzanite Mutagen", 13248, "1/6553", false);
        add("Bosses", "Zulrah", "Magma Mutagen", 13247, "1/6553", false);
        add("Bosses", "Zulrah", "Pet Snakeling", 12921, "1/4000", false);

        // General Graardor
        add("Bosses", "General Graardor", "Bandos Chestplate", 11832, "1/384", false);
        add("Bosses", "General Graardor", "Bandos Tassets", 11834, "1/384", false);
        add("Bosses", "General Graardor", "Bandos Boots", 11836, "1/384", false);
        add("Bosses", "General Graardor", "Bandos Hilt", 11812, "1/508", false);
        add("Bosses", "General Graardor", "Pet General Graardor", 12650, "1/5000", false);

        // K'ril Tsutsaroth
        add("Bosses", "K'ril Tsutsaroth", "Zamorakian Spear", 11824, "1/508", false);
        add("Bosses", "K'ril Tsutsaroth", "Staff of the Dead", 11791, "1/508", false);
        add("Bosses", "K'ril Tsutsaroth", "Zamorak Hilt", 11816, "1/508", false);
        add("Bosses", "K'ril Tsutsaroth", "Pet K'ril Tsutsaroth", 12652, "1/5000", false);

        // Corporeal Beast
        add("Bosses", "Corporeal Beast", "Spectral Sigil", 12823, "1/1365", false);
        add("Bosses", "Corporeal Beast", "Arcane Sigil", 12827, "1/2730", false);
        add("Bosses", "Corporeal Beast", "Elysian Sigil", 12819, "1/4095", false);
        add("Bosses", "Corporeal Beast", "Holy Elixir", 12833, "1/170", false);
        add("Bosses", "Corporeal Beast", "Pet Dark Core", 12816, "1/5000", false);

        // Phantom Muspah
        add("Bosses", "Phantom Muspah", "Venator Shard", 27614, "1/100", false);
        add("Bosses", "Phantom Muspah", "Ancient Icon", 27622, "1/50", false);
        add("Bosses", "Phantom Muspah", "Muphin", 27623, "1/2500", false);

        // Cerberus
        add("Bosses", "Cerberus", "Primordial Crystal", 13231, "1/512", false);
        add("Bosses", "Cerberus", "Pegasian Crystal", 13229, "1/512", false);
        add("Bosses", "Cerberus", "Eternal Crystal", 13227, "1/512", false);
        add("Bosses", "Cerberus", "Hellpuppy", 13247, "1/3000", false);

        // Tombs of Amascut
        add("Raids", "Tombs of Amascut", "Osmumten's Fang", 26219, "1/3.4 on unique", false);
        add("Raids", "Tombs of Amascut", "Lightbearer", 25975, "1/3.4 on unique", false);
        add("Raids", "Tombs of Amascut", "Elidinis' Ward", 25985, "1/8 on unique", false);
        add("Raids", "Tombs of Amascut", "Masori Mask", 27226, "1/12 on unique", false);
        add("Raids", "Tombs of Amascut", "Masori Body", 27229, "1/12 on unique", false);
        add("Raids", "Tombs of Amascut", "Masori Chaps", 27232, "1/12 on unique", false);
        add("Raids", "Tombs of Amascut", "Tumeken's Shadow", 27275, "1/24 on unique", false);

        // Chambers of Xeric
        add("Raids", "Chambers of Xeric", "Dexterous Prayer Scroll", 21034, "1/3.45 on unique", false);
        add("Raids", "Chambers of Xeric", "Arcane Prayer Scroll", 21079, "1/3.45 on unique", false);
        add("Raids", "Chambers of Xeric", "Dragon Hunter Crossbow", 21012, "1/23 on unique", false);
        add("Raids", "Chambers of Xeric", "Twisted Bow", 20997, "1/34.5 on unique", false);

        // Slayer
        add("Slayer", "Abyssal Demon", "Abyssal Whip", 4151, "1/512", false);
        add("Slayer", "Cave Kraken", "Trident of the Seas", 11907, "1/512", false);
        add("Slayer", "Cave Kraken", "Kraken Tentacle", 12004, "1/400", false);
        add("Slayer", "Smoke Devil", "Occult Necklace", 12002, "1/512", false);
        add("Slayer", "Superiors", "Imbued Heart", 20724, "1/250 - 1/700", false);
        add("Slayer", "Superiors", "Eternal Gem", 21270, "1/250 - 1/700", false);
    }

    private void add(String cat, String boss, String item, int id, String rate, boolean obt) {
        entries.add(new CollectionLogEntry(cat, boss, item, id, rate, obt));
    }

    public List<CollectionLogEntry> getAllEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<CollectionLogEntry> getEntriesForCategory(String category) {
        List<CollectionLogEntry> filtered = new ArrayList<>();
        for (CollectionLogEntry e : entries) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public void setItemObtained(String itemName, boolean obtained) {
        for (CollectionLogEntry e : entries) {
            if (e.getItemName().equalsIgnoreCase(itemName)) {
                e.setObtained(obtained);
                String key = e.getEncounterName() + "_" + e.getItemName();
                if (obtained) {
                    obtainedKeys.add(key);
                } else {
                    obtainedKeys.remove(key);
                }
                saveProgress();
                break;
            }
        }
    }

    public double getCategoryCompletionPercent(String category) {
        List<CollectionLogEntry> list = getEntriesForCategory(category);
        if (list.isEmpty()) return 0.0;
        int obtained = 0;
        for (CollectionLogEntry e : list) {
            if (e.isObtained()) obtained++;
        }
        return ((double) obtained / (double) list.size()) * 100.0;
    }

    public double getTotalCompletionPercent() {
        if (entries.isEmpty()) return 0.0;
        int obtained = 0;
        for (CollectionLogEntry e : entries) {
            if (e.isObtained()) obtained++;
        }
        return ((double) obtained / (double) entries.size()) * 100.0;
    }

    public int getTotalObtainedCount() {
        int count = 0;
        for (CollectionLogEntry e : entries) {
            if (e.isObtained()) count++;
        }
        return count;
    }

    private void saveProgress() {
        if (configManager != null) {
            configManager.setConfiguration("bosswise", "clog_keys", String.join(",", obtainedKeys));
        }
    }

    private void loadSavedProgress() {
        if (configManager != null) {
            String saved = configManager.getConfiguration("bosswise", "clog_keys");
            if (saved != null && !saved.trim().isEmpty()) {
                String[] parts = saved.split(",");
                Collections.addAll(obtainedKeys, parts);
                for (CollectionLogEntry e : entries) {
                    String key = e.getEncounterName() + "_" + e.getItemName();
                    if (obtainedKeys.contains(key)) {
                        e.setObtained(true);
                    }
                }
            }
        }
    }
}
