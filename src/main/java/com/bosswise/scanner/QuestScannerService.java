package com.bosswise.scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class QuestScannerService {
    private static final Logger log = LoggerFactory.getLogger(QuestScannerService.class);

    private final Map<String, QuestInfo> questDatabase = new HashMap<>();

    public QuestScannerService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        // Dragon Slayer I
        Map<Skill, Integer> ds1Skills = new HashMap<>();
        ds1Skills.put(Skill.CRAFTING, 8);
        questDatabase.put("Dragon Slayer I", new QuestInfo(
                Quest.DRAGON_SLAYER_I, "Dragon Slayer I",
                ds1Skills, Collections.emptyList(),
                Arrays.asList("King Black Dragon", "Green Dragons", "Rune Platebody"), 2));

        // Dragon Slayer II
        Map<Skill, Integer> ds2Skills = new HashMap<>();
        ds2Skills.put(Skill.MAGIC, 75);
        ds2Skills.put(Skill.SMITHING, 70);
        ds2Skills.put(Skill.MINING, 68);
        ds2Skills.put(Skill.CRAFTING, 62);
        ds2Skills.put(Skill.AGILITY, 60);
        ds2Skills.put(Skill.THIEVING, 60);
        ds2Skills.put(Skill.HITPOINTS, 50);
        questDatabase.put("Dragon Slayer II", new QuestInfo(
                Quest.DRAGON_SLAYER_II, "Dragon Slayer II",
                ds2Skills, Arrays.asList("Legends' Quest", "Dream Mentor", "A Tail of Two Cats", "Animal Magnetism", "Ghosts Ahoy", "Bone Voyage", "Client of Kourend"),
                Arrays.asList("Vorkath", "Tormented Demons", "Myths' Guild", "Ava's Assembler"), 5));

        // Song of the Elves
        Map<Skill, Integer> soteSkills = new HashMap<>();
        soteSkills.put(Skill.AGILITY, 70);
        soteSkills.put(Skill.CONSTRUCTION, 70);
        soteSkills.put(Skill.FARMING, 70);
        soteSkills.put(Skill.HERBLORE, 70);
        soteSkills.put(Skill.HUNTER, 70);
        soteSkills.put(Skill.MINING, 70);
        soteSkills.put(Skill.SMITHING, 70);
        soteSkills.put(Skill.WOODCUTTING, 70);
        questDatabase.put("Song of the Elves", new QuestInfo(
                Quest.SONG_OF_THE_ELVES, "Song of the Elves",
                soteSkills, Arrays.asList("Mourning's End Part II", "Making History"),
                Arrays.asList("The Gauntlet", "Corrupted Gauntlet", "Zalcano", "Prifddinas", "Bow of Faerdhinen"), 4));

        // Desert Treasure I
        Map<Skill, Integer> dt1Skills = new HashMap<>();
        dt1Skills.put(Skill.MAGIC, 50);
        dt1Skills.put(Skill.THIEVING, 53);
        dt1Skills.put(Skill.SLAYER, 10);
        dt1Skills.put(Skill.FIREMAKING, 50);
        questDatabase.put("Desert Treasure I", new QuestInfo(
                Quest.DESERT_TREASURE_I, "Desert Treasure I",
                dt1Skills, Arrays.asList("The Dig Site", "Temple of Ikov", "The Tourist Trap", "Priest in Peril", "Waterfall Quest", "Troll Stronghold"),
                Arrays.asList("Ancient Magicks", "Smoke Devil Dungeon"), 3));

        // Desert Treasure II
        Map<Skill, Integer> dt2Skills = new HashMap<>();
        dt2Skills.put(Skill.MAGIC, 75);
        dt2Skills.put(Skill.FIREMAKING, 75);
        dt2Skills.put(Skill.THIEVING, 70);
        dt2Skills.put(Skill.HERBLORE, 62);
        dt2Skills.put(Skill.RUNECRAFT, 60);
        dt2Skills.put(Skill.CONSTRUCTION, 60);
        questDatabase.put("Desert Treasure II", new QuestInfo(
                findQuestSafe("DESERT_TREASURE_II"), "Desert Treasure II",
                dt2Skills, Arrays.asList("Desert Treasure I", "Secrets of the North", "Enakhra's Lament", "Temple of the Eye", "The Garden of Death", "Below Ice Mountain", "His Faithful Servants"),
                Arrays.asList("Duke Sucellus", "Vardorvis", "The Whisperer", "The Leviathan", "Ancient Blood Sceptre", "Virtus Robes", "Soulreaper Axe"), 5));

        // Monkey Madness I
        questDatabase.put("Monkey Madness I", new QuestInfo(
                Quest.MONKEY_MADNESS_I, "Monkey Madness I",
                Collections.emptyMap(), Arrays.asList("The Grand Tree", "Tree Gnome Village"),
                Arrays.asList("Dragon Scimitar", "Ape Atoll"), 3));

        // Monkey Madness II
        Map<Skill, Integer> mm2Skills = new HashMap<>();
        mm2Skills.put(Skill.SLAYER, 69);
        mm2Skills.put(Skill.AGILITY, 55);
        mm2Skills.put(Skill.THIEVING, 55);
        mm2Skills.put(Skill.HUNTER, 60);
        questDatabase.put("Monkey Madness II", new QuestInfo(
                Quest.MONKEY_MADNESS_II, "Monkey Madness II",
                mm2Skills, Arrays.asList("Monkey Madness I", "Enlightened Journey", "The Eyes of Glouphrie", "Recipe for Disaster (King Awowogei)", "Troll Stronghold", "Watchtower"),
                Arrays.asList("Demonic Gorillas", "Heavy Ballista", "Zenyte Jewellery"), 4));

        // Recipe for Disaster
        Map<Skill, Integer> rfdSkills = new HashMap<>();
        rfdSkills.put(Skill.COOKING, 70);
        rfdSkills.put(Skill.AGILITY, 48);
        rfdSkills.put(Skill.MINING, 50);
        rfdSkills.put(Skill.FISHING, 53);
        rfdSkills.put(Skill.THIEVING, 53);
        questDatabase.put("Recipe for Disaster", new QuestInfo(
                Quest.RECIPE_FOR_DISASTER, "Recipe for Disaster",
                rfdSkills, Arrays.asList("Cook's Assistant", "Fishing Contest", "Goblin Diplomacy", "Big Chompy Bird Hunting", "Biohazard", "Demon Slayer", "Murder Mystery", "Nature Spirit", "Priest in Peril", "Restless Ghost", "Witch's House", "Lost City", "Legends' Quest", "Monkey Madness I", "Desert Treasure I", "Horror from the Deep"),
                Arrays.asList("Barrows Gloves", "Culinaromancer's Chest"), 10));

        // Priest in Peril
        questDatabase.put("Priest in Peril", new QuestInfo(
                Quest.PRIEST_IN_PERIL, "Priest in Peril",
                Collections.emptyMap(), Collections.emptyList(),
                Arrays.asList("Morytania Access", "Barrows", "Canifis", "Slayer Tower"), 1));

        // Beneath Cursed Sands
        Map<Skill, Integer> bcsSkills = new HashMap<>();
        bcsSkills.put(Skill.AGILITY, 62);
        bcsSkills.put(Skill.CRAFTING, 55);
        bcsSkills.put(Skill.FIREMAKING, 55);
        questDatabase.put("Beneath Cursed Sands", new QuestInfo(
                findQuestSafe("BENEATH_CURSED_SANDS"), "Beneath Cursed Sands",
                bcsSkills, Arrays.asList("Contact!", "Scorpion Catcher"),
                Arrays.asList("Tombs of Amascut (Raid 3)", "Keris Partisan", "Jal-Ak"), 2));

        // Sins of the Father
        Map<Skill, Integer> sotfSkills = new HashMap<>();
        sotfSkills.put(Skill.WOODCUTTING, 62);
        sotfSkills.put(Skill.FLETCHING, 60);
        sotfSkills.put(Skill.CRAFTING, 56);
        sotfSkills.put(Skill.AGILITY, 52);
        sotfSkills.put(Skill.ATTACK, 50);
        sotfSkills.put(Skill.SLAYER, 50);
        sotfSkills.put(Skill.MAGIC, 49);
        questDatabase.put("Sins of the Father", new QuestInfo(
                Quest.SINS_OF_THE_FATHER, "Sins of the Father",
                sotfSkills, Arrays.asList("Vampyre Slayer", "A Taste of Hope", "Darkness of Hallowvale"),
                Arrays.asList("Darkmeyer", "Blisterwood Flail", "Hallowed Sepulchre"), 2));

        // Regicide
        Map<Skill, Integer> regiSkills = new HashMap<>();
        regiSkills.put(Skill.AGILITY, 56);
        regiSkills.put(Skill.CRAFTING, 10);
        questDatabase.put("Regicide", new QuestInfo(
                Quest.REGICIDE, "Regicide",
                regiSkills, Arrays.asList("Underground Pass"),
                Arrays.asList("Zulrah Access (Tirannwn)", "Dragon Halberd"), 3));

        // Secrets of the North
        Map<Skill, Integer> sotnSkills = new HashMap<>();
        sotnSkills.put(Skill.HUNTER, 56);
        sotnSkills.put(Skill.THIEVING, 64);
        sotnSkills.put(Skill.AGILITY, 69);
        questDatabase.put("Secrets of the North", new QuestInfo(
                findQuestSafe("SECRETS_OF_THE_NORTH"), "Secrets of the North",
                sotnSkills, Arrays.asList("Desert Treasure I", "Devious Minds", "Hazeel Cult"),
                Arrays.asList("Phantom Muspah", "Ancient Icon", "Venator Bow"), 2));

        // Horror from the Deep
        questDatabase.put("Horror from the Deep", new QuestInfo(
                Quest.HORROR_FROM_THE_DEEP, "Horror from the Deep",
                Collections.singletonMap(Skill.AGILITY, 35), Arrays.asList("Barcrawl"),
                Arrays.asList("Dagannoth Kings", "God Books", "Lighthouse Dungeon"), 2));

        // The Fremennik Trials
        questDatabase.put("The Fremennik Trials", new QuestInfo(
                Quest.THE_FREMENNIK_TRIALS, "The Fremennik Trials",
                Collections.singletonMap(Skill.CRAFTING, 40), Collections.emptyList(),
                Arrays.asList("The Fremennik Isles", "Berserker Helm", "Warrior Helm"), 3));

        // The Fremennik Isles
        Map<Skill, Integer> islesSkills = new HashMap<>();
        islesSkills.put(Skill.AGILITY, 20);
        islesSkills.put(Skill.CRAFTING, 46);
        islesSkills.put(Skill.WOODCUTTING, 56);
        questDatabase.put("The Fremennik Isles", new QuestInfo(
                Quest.THE_FREMENNIK_ISLES, "The Fremennik Isles",
                islesSkills, Arrays.asList("The Fremennik Trials"),
                Arrays.asList("Helm of Neitiznot", "Jatizso and Neitiznot Access"), 1));

        // A Kingdom Divided
        Map<Skill, Integer> akdSkills = new HashMap<>();
        akdSkills.put(Skill.AGILITY, 54);
        akdSkills.put(Skill.THIEVING, 52);
        akdSkills.put(Skill.WOODCUTTING, 52);
        akdSkills.put(Skill.HERBLORE, 50);
        akdSkills.put(Skill.MINING, 42);
        akdSkills.put(Skill.CRAFTING, 38);
        questDatabase.put("A Kingdom Divided", new QuestInfo(
                Quest.A_KINGDOM_DIVIDED, "A Kingdom Divided",
                akdSkills, Arrays.asList("The Depths of Despair", "The Queen of Thieves", "The Ascent of Arceuus", "The Forsaken Tower", "Tale of the Righteous", "Client of Kourend"),
                Arrays.asList("Arceuus Spells (Thralls)", "Death Charge", "Sinister Offering"), 2));
    }

    private static Quest findQuestSafe(String enumName) {
        try {
            return Quest.valueOf(enumName);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<Quest, QuestState> scanQuests(Client client) {
        Map<Quest, QuestState> results = new HashMap<>();
        if (client == null) return results;

        for (Quest quest : Quest.values()) {
            try {
                QuestState state = quest.getState(client);
                results.put(quest, state != null ? state : QuestState.NOT_STARTED);
            } catch (Exception ex) {
                // If RuneLite API fails or state is unknown
                log.trace("Could not read state for quest: {}", quest);
            }
        }
        return results;
    }

    public Set<String> getKnownCompletedQuestNames(Map<Quest, QuestState> questStates) {
        Set<String> completed = new HashSet<>();
        if (questStates == null) return completed;

        for (Map.Entry<String, QuestInfo> entry : questDatabase.entrySet()) {
            Quest q = entry.getValue().getQuest();
            if (q != null) {
                QuestState state = questStates.get(q);
                if (state == QuestState.FINISHED) {
                    completed.add(entry.getKey());
                }
            }
        }
        return completed;
    }

    public QuestInfo getQuestInfo(String name) {
        return questDatabase.get(name);
    }

    public Map<String, QuestInfo> getAllQuests() {
        return Collections.unmodifiableMap(questDatabase);
    }
}
