package com.bosswise.service;

import com.bosswise.model.CombatTrainingTarget;
import com.bosswise.model.PlayerAccount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

@Singleton
public class CombatTrainingService {
    private final List<CombatTrainingTarget> targets = new ArrayList<>();

    @Inject
    public CombatTrainingService() {
        initTargets();
    }

    private void initTargets() {
        // Melee (Attack, Strength, Defence)
        for (Skill s : new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE}) {
            targets.add(new CombatTrainingTarget(
                    "Sand Crabs", s, 1, 65, 45000, 0, 9,
                    "Hosidius Beach / Crabclaw Isle", "Iron scimitar -> Rune scimitar / D Scim",
                    "Aggressive for 10 minutes. 100 HP, 1 Defence. Reset aggression by running north.", "None"
            ));

            targets.add(new CombatTrainingTarget(
                    "Ammonite Crabs", s, 60, 80, 72000, 0, 9,
                    "Fossil Island (North Beach)", "Dragon scimitar, Fighter torso, Berserker ring",
                    "100 HP, 1 Defence, 10 min aggression timer. Excellent 100% AFK XP.", "Bone Voyage quest"
            ));

            targets.add(new CombatTrainingTarget(
                    "Scurrius (The Rat King)", s, 60, 85, 110000, 40000, 4,
                    "Varrock Sewers", "Bone weapons (Bone mace: flat +10 damage per hit)",
                    "Prayer switches on rats/falling rocks. Extremely high combat XP multiplier.", "None"
            ));

            targets.add(new CombatTrainingTarget(
                    "Nightmare Zone (Custom Rumble)", s, 70, 99, 105000, 20000, 8,
                    "Yanille (Nightmare Zone)", "Dharok's armor (at 1 HP) or Obsidian + Berserker neck",
                    "Absorption potions + Overloads or Rock cake. 20-minute AFK intervals.", "5 quest boss completions"
            ));

            targets.add(new CombatTrainingTarget(
                    "Vyrewatch Sentinels", s, 80, 99, 80000, -350000, 8,
                    "Darkmeyer", "Blisterwood flail, Protect from Melee, Drakan's medallion",
                    "Altar right next to spawns for unlimited free prayer. Blood shard drops yield profit.", "Sins of the Father quest"
            ));
        }

        // Ranged
        targets.add(new CombatTrainingTarget(
                "Sand Crabs (Ranged)", Skill.RANGED, 1, 60, 42000, 8000, 9,
                "Hosidius Beach", "Oak shortbow -> Mithril -> Magic shortbow (i)",
                "Iron / Mithril knives or arrows. Reset aggression every 10 min.", "None"
        ));

        targets.add(new CombatTrainingTarget(
                "Ammonite Crabs (Ranged)", Skill.RANGED, 60, 75, 68000, 15000, 9,
                "Fossil Island", "Magic shortbow (i) + Rune arrows / Mithril darts",
                "High HP sand targets. Very cheap ammo consumption.", "Bone Voyage"
        ));

        targets.add(new CombatTrainingTarget(
                "Nightmare Zone (Ranged)", Skill.RANGED, 75, 99, 115000, 60000, 8,
                "Nightmare Zone", "Toxic blowpipe / Bow of Faerdhinen + Absorption potions",
                "Fastest non-chin ranged training with minimal click intensity.", "5 easy boss quests"
        ));

        targets.add(new CombatTrainingTarget(
                "Maniacal Monkeys (Chinchompas)", Skill.RANGED, 75, 99, 580000, 450000, 6,
                "Kruk's Dungeon (Ape Atoll)", "Red or Black Chinchompas, Void Knight Ranger set",
                "Massive multi-target AoE damage. Stack monkeys around bones. 600k+ XP/hr.", "Monkey Madness II"
        ));

        // Magic
        targets.add(new CombatTrainingTarget(
                "Splashing (Curse / Fire Strike)", Skill.MAGIC, 1, 55, 14000, 12000, 10,
                "Lumbridge / Clan Hall", "Staff of Air + Mind runes, negative magic attack (-65)",
                "Full AFK 20-minute combat auto-retaliate without killing target.", "None"
        ));

        targets.add(new CombatTrainingTarget(
                "High Level Alchemy", Skill.MAGIC, 55, 75, 78000, 50000, 6,
                "Grand Exchange / Bank", "Staff of Fire, Nature runes, Air battlestaves / D'hide bodies",
                "Zero HP combat experience. Trainable anywhere, pairs with Agility or bank standing.", "None"
        ));

        targets.add(new CombatTrainingTarget(
                "Dust Devils (Bursting)", Skill.MAGIC, 70, 90, 145000, -100000, 5,
                "Catacombs of Kourend", "Ice Burst, Ancient staff, Occult necklace, Bonecrusher",
                "Group targets with darts, cast Ice Burst. High drops pay for runes entirely.", "65 Slayer"
        ));

        targets.add(new CombatTrainingTarget(
                "Maniacal Monkeys (Barraging)", Skill.MAGIC, 80, 99, 320000, 350000, 6,
                "Kruk's Dungeon (Ape Atoll)", "Ice Barrage / Burst, Ahrim's / Ancestral / Virtus robes",
                "Insane Magic XP rate. Stack monkeys on bone drops with auto-retaliate.", "Monkey Madness II"
        ));
    }

    public List<CombatTrainingTarget> getRecommendations(PlayerAccount account, Skill skill, int targetLevel) {
        int curLvl = account != null ? account.getRealLevel(skill) : 70;
        List<CombatTrainingTarget> results = new ArrayList<>();

        for (CombatTrainingTarget t : targets) {
            if (t.getTrainedSkill() == skill) {
                // If within reasonable training range
                if (curLvl >= t.getMinRecommendedLevel() - 5 && curLvl <= t.getMaxOptimalLevel() + 5) {
                    results.add(t);
                }
            }
        }

        if (results.isEmpty()) {
            for (CombatTrainingTarget t : targets) {
                if (t.getTrainedSkill() == skill) {
                    results.add(t);
                }
            }
        }

        return results;
    }

    public int getXpForLevel(int level) {
        if (level <= 1) return 0;
        if (level > 99) level = 99;
        return Experience.getXpForLevel(level);
    }

    public int getXpRemaining(PlayerAccount account, Skill skill, int targetLevel) {
        int curLvl = account != null ? account.getRealLevel(skill) : 70;
        int curXp = getXpForLevel(curLvl);
        int targetXp = getXpForLevel(targetLevel);
        return Math.max(0, targetXp - curXp);
    }
}
