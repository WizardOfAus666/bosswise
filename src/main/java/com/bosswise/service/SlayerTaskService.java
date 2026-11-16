package com.bosswise.service;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.SlayerTaskInfo;
import com.bosswise.model.SlayerTaskInfo.TaskLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;

@Singleton
public class SlayerTaskService {
    private final Map<String, SlayerTaskInfo> tasks = new HashMap<>();
    private final Client client;

    @com.google.inject.Inject(optional = true)
    private ConfigManager configManager;

    @Inject
    public SlayerTaskService(Client client) {
        this.client = client;
        initTasks();
    }

    public SlayerTaskService(Client client, ConfigManager configManager) {
        this.client = client;
        this.configManager = configManager;
        initTasks();
    }

    private void initTasks() {
        addTask(new SlayerTaskInfo(
                "Gargoyles", 75, CombatStyle.MELEE, "Abyssal whip / Osmumten's fang", 42000, 600000, 140,
                Arrays.asList(
                        new TaskLocation("Slayer Tower (Top Floor)", false, false, false, true, false, "+10% Slayer XP with Morytania Diary. Rock hammer mandatory.")
                ),
                "Marble Gargoyle (Level 215)", "Granite maul, Granite dust, Imbued heart (1/640)",
                "Smash with Rock Hammer at 9 HP or below. High alch drops (rune full helm, plates). Auto-smash perk recommended."
        ));

        addTask(new SlayerTaskInfo(
                "Abyssal Demons", 85, CombatStyle.MELEE, "Arclight / Emberlight / Scythe", 55000, 520000, 130,
                Arrays.asList(
                        new TaskLocation("Catacombs of Kourend", false, true, true, false, false, "Bursting/barraging allowed. Ancient shards & Dark totem pieces."),
                        new TaskLocation("Slayer Tower (Basement)", false, false, false, true, false, "Extra XP with Morytania Hard diary."),
                        new TaskLocation("Abyssal Area (Fairy CIP)", false, false, false, false, false, "Quiet spot, no crowding.")
                ),
                "Greater Abyssal Demon (Level 342)", "Abyssal whip, Abyssal head, Imbued heart (1/320)",
                "Demons teleport around player. Demonbane weapons deal +70% accuracy and +30% damage. Can alternative at Abyssal Sire boss."
        ));

        addTask(new SlayerTaskInfo(
                "Nechryaels", 80, CombatStyle.MAGIC, "Ice Barrage / Burst", 110000, 450000, 280,
                Arrays.asList(
                        new TaskLocation("Catacombs of Kourend", false, true, true, false, false, "Multi-combat bursting area. Spawn minion death spawns for high XP."),
                        new TaskLocation("Slayer Tower", false, false, false, true, false, "Single-way melee with Guthan's or prayer.")
                ),
                "Nechryarch (Level 300)", "Rune boots, Mystic robes, Imbued heart (1/390)",
                "Group them using darts and cast Ice Burst/Barrage. Bonecrusher restores prayer on minion deaths."
        ));

        addTask(new SlayerTaskInfo(
                "Dust Devils", 65, CombatStyle.MAGIC, "Ice Burst / Barrage", 125000, 250000, 380,
                Arrays.asList(
                        new TaskLocation("Catacombs of Kourend", false, true, true, false, false, "Premier bursting location. Stack with darts/cornering."),
                        new TaskLocation("Smoke Dungeon", false, false, false, false, false, "Single target melee, Face mask mandatory.")
                ),
                "Choke Devil (Level 175)", "Dust battlestaff, Imbued heart (1/520)",
                "Face mask or Slayer Helmet mandatory to prevent stat drain. Tremendous Magic and Slayer XP per hour."
        ));

        addTask(new SlayerTaskInfo(
                "Bloodvelds", 50, CombatStyle.MELEE, "Arclight / Blowpipe", 65000, 280000, 160,
                Arrays.asList(
                        new TaskLocation("Meiyerditch Laboratories", true, true, false, false, false, "Cannonable multi-combat room with high spawns."),
                        new TaskLocation("Catacombs of Kourend (Mutated)", false, false, true, false, false, "Mutated bloodvelds drop Dark totem pieces.")
                ),
                "Insatiable Bloodveld (Level 202)", "Blood runes, Meat pizza, Imbued heart (1/680)",
                "High Magic-based Melee attack; wear high Magic defence dragonhide or Karil's armor while praying protect or piety."
        ));

        addTask(new SlayerTaskInfo(
                "Cave Kraken", 87, CombatStyle.MAGIC, "Trident of the swamp", 40000, 1450000, 50,
                Arrays.asList(
                        new TaskLocation("Kraken Cove", false, false, false, false, false, "Private instance available for 25k GP.")
                ),
                "None", "Trident of the seas (1/512), Kraken tentacle (1/400), Pet (1/3000)",
                "Throw fishing bait to disturb whirlwind. 100% Magic combat. Very relaxing AFK boss."
        ));

        addTask(new SlayerTaskInfo(
                "Smoke Devils", 93, CombatStyle.MAGIC, "Ice Barrage (Catacombs) / Fang", 140000, 950000, 220,
                Arrays.asList(
                        new TaskLocation("Smoke Devil Dungeon", true, true, false, false, false, "Cannonable room for massive lure-bursting.")
                ),
                "Nuclear Smoke Devil (Level 280)", "Occult necklace (1/512), Smoke battlestaff, Imbued heart (1/270)",
                "Slayer helmet mandatory. High magic damage. Can alternatively kill Thermonuclear Smoke Devil boss."
        ));

        addTask(new SlayerTaskInfo(
                "Hydras", 95, CombatStyle.RANGED, "Twisted bow / Bowfa / Blowpipe", 75000, 3130000, 24,
                Arrays.asList(
                        new TaskLocation("Karuulm Slayer Dungeon (Boss)", false, false, false, false, false, "Boots of stone / brimstone mandatory.")
                ),
                "Colossal Hydra (Level 350)", "Hydra claw (1/1000), Hydra leather (1/512), Hydra tail",
                "Alchemical Hydra boss drops the coveted Hydra Claw. Count 3 attacks per phase and lure across chemical vents."
        ));

        addTask(new SlayerTaskInfo(
                "Basilisks", 40, CombatStyle.MELEE, "Fang / Inquisitor mace", 45000, 1200000, 45,
                Arrays.asList(
                        new TaskLocation("Jormungand's Prison (Knights)", false, false, false, false, false, "Requires 60 Slayer & The Fremennik Exiles.")
                ),
                "Basilisk Sentinel (Level 240)", "Basilisk jaw (1/1000 on task, 1/5000 off task)",
                "Mirror shield or V's shield MANDATORY. Basilisk jaw is attached to Helm of Neitiznot to craft the Neitiznot Faceguard."
        ));

        addTask(new SlayerTaskInfo(
                "Black Dragons", 1, CombatStyle.RANGED, "Dragon hunter crossbow / Lance", 50000, 3330000, 28,
                Arrays.asList(
                        new TaskLocation("Vorkath (Unofficial Boss Task)", false, false, false, false, false, "Counts as Black Dragon task with Slayer helm bonus!"),
                        new TaskLocation("King Black Dragon Lair", false, false, false, false, false, "KBD counts for Black Dragon task."),
                        new TaskLocation("Catacombs (Brutal Black Dragons)", false, false, true, false, false, "Relaxing AFK prayer camping.")
                ),
                "None", "Draconic visage, Dragon bones, Vorkath head",
                "Always opt to kill Vorkath or KBD if capable. Slayer helmet (i) applies to Vorkath only if NOT wearing Salve amulet (ei)."
        ));

        addTask(new SlayerTaskInfo(
                "Dagannoths", 1, CombatStyle.RANGED, "Cannon + Blowpipe / Trident", 90000, 1800000, 200,
                Arrays.asList(
                        new TaskLocation("Lighthouse Dungeon", true, true, false, false, false, "Fastest Slayer XP with cannon in Lighthouse."),
                        new TaskLocation("Waterbirth Island (DKs)", false, false, false, false, false, "Dagannoth Kings boss trio (Berserker / Archers / Seers rings)."),
                        new TaskLocation("Catacombs of Kourend", false, false, true, false, false, "Totem pieces & ancient shards.")
                ),
                "None", "Berserker ring (1/128 Rex), Archers ring (1/128 Prime), Seers ring (1/128 Supreme)",
                "One of the fastest tasks in game with cannon at Lighthouse, or high profit at Dagannoth Kings."
        ));
    }

    private void addTask(SlayerTaskInfo task) {
        tasks.put(task.getTaskName().toLowerCase(), task);
    }

    public List<String> getAllTaskNames() {
        List<String> names = new ArrayList<>();
        for (SlayerTaskInfo t : tasks.values()) {
            names.add(t.getTaskName());
        }
        Collections.sort(names);
        return names;
    }

    public SlayerTaskInfo getTask(String name) {
        if (name == null) return null;
        return tasks.get(name.toLowerCase());
    }

    public String detectCurrentSlayerTask() {
        if (configManager != null) {
            String task = configManager.getConfiguration("slayer", "taskName");
            if (task != null && !task.trim().isEmpty()) {
                return task;
            }
        }
        return "Gargoyles";
    }

    public int detectRemainingKills() {
        if (configManager != null) {
            String amount = configManager.getConfiguration("slayer", "amount");
            if (amount != null) {
                try {
                    return Integer.parseInt(amount);
                } catch (NumberFormatException ignored) {}
            }
        }
        return 140;
    }
}
