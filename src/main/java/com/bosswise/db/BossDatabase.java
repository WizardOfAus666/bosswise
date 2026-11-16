package com.bosswise.db;

import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.BossDifficulty;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.QuestRequirement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

@Singleton
public class BossDatabase {
    private final Map<String, BossData> bosses = new LinkedHashMap<>();

    public BossDatabase() {
        initBosses();
    }

    private void initBosses() {
        // 1. SCURRIUS - THE RAT KING
        addBoss(BossData.builder("scurrius", "Scurrius", BossCategory.BEGINNER, BossDifficulty.BEGINNER, CombatStyle.MELEE)
                .combatLevel(60)
                .budget(150_000L)
                .minSkill(Skill.ATTACK, 40)
                .minSkill(Skill.STRENGTH, 40)
                .minSkill(Skill.DEFENCE, 40)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 60)
                .recSkill(Skill.STRENGTH, 65)
                .recSkill(Skill.PRAYER, 43)
                .solo(true).group(true)
                .gp(15_000L, 450_000L)
                .weakness("Rat bone weapons (+10 max hit), Crush melee")
                .prayers("Protect from Missiles / Magic / Melee (Telegraphed overheads)", "Piety / Ultimate Strength")
                .travel("Varrock Sewers via manhole near the palace or south of the east bank.")
                .strategy("React to Scurrius's telegraphed attacks. When he throws debris from the ceiling, dodge the falling shadows immediately. Switch overhead prayer when he rears back for ranged or magic. Kill giant rat adds quickly or ignore if using bone mace/bow.")
                .mechanics("1. Ceiling Debris: Shadows appear on ground - move 1-2 tiles away.\n2. Overhead Prayer Switches: Orange projectile = Protect from Missiles, Blue wave = Protect from Magic, Swipe = Protect from Melee.\n3. Food Pile Healing: When below 80% and 30% HP, eats from food pile; deal damage to disrupt.")
                .phases("Phase 1: Normal attacks and ceiling collapse.\nPhase 2 (sub 80% & sub 30%): Rushes to food pile to heal and summons giant rats.")
                .warnings("Do not stand still when ceiling debris shadows appear; falling rocks hit up to 15+ multiple times.")
                .tips("Craft a Bone Mace, Bone Bow, or Bone Staff as soon as you get a Scurrius spine drop for enormous DPS.", "Turn on game sounds to hear attack telegraphs.")
                .addChecklist("Bring Prayer potions and food (Sharks/Karambwans)")
                .addChecklist("Watch for overhead projectile colors: Orange = Ranged, Blue = Magic")
                .addChecklist("Move immediately off ceiling shadow tiles")
                .addDrop("Scurrius' spine (1/33 solo)")
                .addDrop("Scurry (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(385, "Shark", 14, "Food", 900, true, "Primary food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, false, "Combo food"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Maintain overheads"),
                        new InventoryItem(2436, "Super attack(4)", 1, "Combat Potion", 1200, false, "Attack boost"),
                        new InventoryItem(2440, "Super strength(4)", 1, "Combat Potion", 2800, false, "Strength boost"),
                        new InventoryItem(8007, "Varrock teleport", 1, "Teleport", 500, true, "Emergency exit")
                ))
                .build());

        // 2. GIANT MOLE
        addBoss(BossData.builder("giant_mole", "Giant Mole", BossCategory.BEGINNER, BossDifficulty.BEGINNER, CombatStyle.MELEE)
                .combatLevel(70)
                .budget(250_000L)
                .minSkill(Skill.ATTACK, 60)
                .minSkill(Skill.STRENGTH, 60)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 70)
                .recSkill(Skill.STRENGTH, 75)
                .recSkill(Skill.PRAYER, 70)
                .solo(true).group(false)
                .gp(25_000L, 800_000L)
                .weakness("Dharok's Greataxe (low HP), Fang, Bowfa")
                .prayers("Protect from Melee (Mole only uses melee)", "Piety")
                .travel("Spade dig on the mole hills inside Falador Park. Require light source (Bullseye lantern or Kandarin headgear).")
                .strategy("Turn on Protect from Melee continuously. Hit the mole until it burrows. When it digs away, look at your minimap (Falador Shield 3/4 points directly to it) and chase it down. If using Dharok's, keep HP low with locator orb or rock cake.")
                .mechanics("1. Burrowing: At random damage intervals below 50% HP, digs and reappears across the cavern.\n2. Darkness: Without a light source, cavern goes completely black and insects extinguish lanterns.")
                .phases("Single phase. Digs more frequently as HP decreases.")
                .warnings("DO NOT forget a Bullseye Lantern or light source! Open flame lanterns can explode if swamp gas ignites!")
                .tips("Falador Hard Diary grants Falador Shield 3, which tracks the mole's burrow location with an arrow on the minimap.", "Stamina potions are essential because of constant running.")
                .addChecklist("Bring light source (Bullseye lantern or Kandarin headgear)")
                .addChecklist("Bring a Spade to enter the lair")
                .addChecklist("Equip Falador Shield 3/4 if unlocked")
                .addChecklist("Keep Protect from Melee active at all times")
                .addDrop("Baby mole (pet 1/3,000)")
                .addDrop("Mole skin & Mole claw (traded for bird nests)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(954, "Spade", 1, "Special Item", 10, true, "Required to dig into lair"),
                        new InventoryItem(4550, "Bullseye lantern", 1, "Special Item", 200, true, "Required light source"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(12625, "Stamina potion(4)", 2, "Utility", 8000, true, "Run energy for chasing"),
                        new InventoryItem(139, "Prayer potion(4)", 4, "Prayer Restore", 9500, true, "Melee protection"),
                        new InventoryItem(385, "Shark", 8, "Food", 900, false, "Emergency healing"),
                        new InventoryItem(8007, "Falador teleport", 1, "Teleport", 500, true, "Bank return")
                ))
                .build());

        // 3. BARROWS BROTHERS
        addBoss(BossData.builder("barrows", "Barrows Brothers", BossCategory.BEGINNER, BossDifficulty.BEGINNER, CombatStyle.MAGIC)
                .combatLevel(70)
                .budget(500_000L)
                .minSkill(Skill.MAGIC, 50)
                .minSkill(Skill.PRAYER, 43)
                .minSkill(Skill.DEFENCE, 40)
                .recSkill(Skill.MAGIC, 75)
                .recSkill(Skill.RANGED, 70)
                .recSkill(Skill.PRAYER, 50)
                .quest(new QuestRequirement(Quest.PRIEST_IN_PERIL, "Priest in Peril", true, "Access to Morytania"))
                .solo(true).group(false)
                .gp(60_000L, 1_200_000L)
                .weakness("Magic (Iban's / Trident) for 5 brothers; Ranged for Ahrim; Melee/Magic for Karil")
                .prayers("Protect from Melee (Dharok, Verac, Torag, Guthan), Protect from Missiles (Karil), Protect from Magic (Ahrim)", "Mystic Might / Augury")
                .travel("Barrows teleport tablet directly to the mounds, or Fairy ring BKR / Mort'ton minigame teleport.")
                .strategy("Kill Dharok first while prayer is high! Dharok hits over 50+ at low HP. Then kill Karil (use Magic or Ranged) and Ahrim (use Ranged with blowpipe/dhide). The remaining brothers (Guthan, Torag, Verac) have very low magic defence and melt to Iban Blast or Trident. Enter the crypt tunnel on the remaining brother and loot the chest.")
                .mechanics("1. Dharok's Set Effect: Hits higher the lower his hitpoints are. ALWAYS pray melee!\n2. Crypt Prayer Drain: Every 15-18 seconds inside the crypt, your prayer is drained by 8 + (brothers alive).\n3. Optimal Reward Potential: Kill 6 brothers + 2 Skeletons + 1 Bloodworm (880 reward points / 86.2% max runes and zero bolt racks).")
                .phases("6 crypts, each housing one brother. 1 crypt leads to the underground tunnel maze.")
                .warnings("NEVER fight Dharok without Protect from Melee active! He can one-shot players with 55+ damage.")
                .tips("Iban's staff (u) or Trident of the Seas ignores the melee brothers' heavy plate armour.", "Use Barrows teleport tablets for fast runs.")
                .addChecklist("Bring a Spade to dig into mounds")
                .addChecklist("Prioritize Dharok -> Karil -> Ahrim while Prayer points are full")
                .addChecklist("Kill 2 Skeletons + 1 Bloodworm in tunnel for 86% reward potential (max runes)")
                .addDrop("Barrows equipment pieces (1/14.5 per chest with 6 brothers killed)")
                .addDrop("Blood runes, Death runes, Chaos runes")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(954, "Spade", 1, "Special Item", 10, true, "Dig into mounds"),
                        new InventoryItem(19629, "Barrows teleport", 3, "Teleport", 3000, true, "Quick travel"),
                        new InventoryItem(139, "Prayer potion(4)", 2, "Prayer Restore", 9500, true, "Maintain overheads"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Sustenance"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, false, "Speed up Ahrim"),
                        new InventoryItem(11978, "Graceful outfit / Tank top", 1, "Armour", 0, false, "Weight reduction")
                ))
                .build());

        // 4. KING BLACK DRAGON (KBD)
        addBoss(BossData.builder("kbd", "King Black Dragon", BossCategory.BEGINNER, BossDifficulty.INTERMEDIATE, CombatStyle.RANGED)
                .combatLevel(80)
                .budget(1_000_000L)
                .minSkill(Skill.RANGED, 70)
                .minSkill(Skill.PRAYER, 43)
                .minSkill(Skill.HITPOINTS, 65)
                .recSkill(Skill.RANGED, 80)
                .recSkill(Skill.PRAYER, 70)
                .quest(new QuestRequirement(Quest.DRAGON_SLAYER_I, "Dragon Slayer I", true, "Anti-dragon shield"))
                .prerequisite("Anti-dragon shield or Dragonfire shield + Antifire potion")
                .solo(true).group(true)
                .gp(22_000L, 750_000L)
                .weakness("Dragonbane weapons (Dragon Hunter Crossbow / Lance), Stab, Ranged (Diamond bolts e)")
                .prayers("Protect from Melee (if in melee range) or Protect from Missiles", "Rigour / Eagle Eye")
                .travel("Ghorrock teleport (Ancient spellbook) or Burning Amulet to Lava Maze (level 41 Wilderness). Pull lever to enter KBD lair (safe instance).")
                .strategy("Drink Antifire potion and equip Anti-Dragon shield before entering! Stand in melee distance while praying Protect from Melee; this prevents his unblockable dragonfire breaths from triggering continuously and forces him to hit your melee protection.")
                .mechanics("1. Dragonfire Breaths: Fire (standard), Ice (freezes), Poison (poisons), Shock (drains all combat stats by 2).\n2. Wilderness Passage: The entrance lever is in 41 Wilderness; carry only 3-4 valuable items while travelling.")
                .phases("Single phase.")
                .warnings("Do not run through Wilderness with your cash stack! Drink extended antifire BEFORE pulling the lever.")
                .tips("Melee distance with Protect from Melee active significantly reduces total damage taken.", "Diamond bolts (e) punch through his high Defence.")
                .addChecklist("Drink Extended Antifire potion before entering")
                .addChecklist("Equip Anti-dragon shield / Dragonfire shield")
                .addChecklist("Bring Antidote++ / Anti-venom for poison breath")
                .addDrop("Prince black dragon (pet 1/3,000)")
                .addDrop("Draconic visage (1/5,000)")
                .addDrop("Dragon pickaxe (1/1,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(11951, "Extended antifire(4)", 1, "Protection", 7500, true, "Mandatory fire protection"),
                        new InventoryItem(5952, "Antidote++(4)", 1, "Protection", 1500, true, "Cures poison breath"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Ranged boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Overhead prayer"),
                        new InventoryItem(385, "Shark", 14, "Food", 900, true, "Food"),
                        new InventoryItem(11972, "Burning amulet(5)", 1, "Teleport", 3000, true, "Wilderness travel")
                ))
                .build());

        // 5. SARACHNIS
        addBoss(BossData.builder("sarachnis", "Sarachnis", BossCategory.INTERMEDIATE, BossDifficulty.INTERMEDIATE, CombatStyle.MELEE)
                .combatLevel(80)
                .budget(1_500_000L)
                .minSkill(Skill.ATTACK, 65)
                .minSkill(Skill.STRENGTH, 70)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 75)
                .recSkill(Skill.STRENGTH, 80)
                .recSkill(Skill.PRAYER, 70)
                .solo(true).group(true)
                .gp(35_000L, 1_100_000L)
                .weakness("Crush melee weapons (Sarachnis Cudgel, Inquisitor's Mace, Abyssal Bludgeon, Zombie Axe)")
                .prayers("Protect from Melee (when adjacent) / Protect from Missiles (when webbed)", "Piety")
                .travel("Kourend Woodland fairy ring CIP, or Xeric's Honour to Mount Quidamortem and run south to Forthos Dungeon.")
                .strategy("Attack with a Crush weapon. Every 4 attacks, Sarachnis wraps you in webbing and runs to a corner to shoot Ranged attacks. Immediately swap to Protect from Missiles when webbed. Once free, run back and swap to Protect from Melee. Kill or ignore the spawned spiderlings.")
                .mechanics("1. Web Trap: Entangles you for 3 seconds every 4th attack and retreats.\n2. Spider Spawns: At 66% and 33% HP, summons Spawn of Sarachnis (Mage spider) and minor melee spiders.\n3. Healing: If Sarachnis hits an unprotected player with Melee, she heals 5-10 HP.")
                .phases("Phase 1 (100%-66%): Basic attack rotation.\nPhase 2 (66%-33%): Spawns first wave of spiderlings.\nPhase 3 (33%-0%): Spawns second wave; attack speed increases.")
                .warnings("Never let Sarachnis melee you without Protect from Melee active - she heals off your unblocked damage!")
                .tips("Zombie Axe or Sarachnis Cudgel on Crush mode gives immense accuracy.", "Step under or away from mage spider to minimize magic chip damage.")
                .addChecklist("Use a weapon set to CRUSH style")
                .addChecklist("Pray Melee when adjacent; Pray Missiles immediately when webbed")
                .addChecklist("Kill the mage spiderling quickly during spawns")
                .addDrop("Sraracha (pet 1/3,000)")
                .addDrop("Sarachnis cudgel (1/384)")
                .addDrop("Giant egg sac (full)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 14, "Food", 900, true, "Primary healing"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, false, "Combo healing"),
                        new InventoryItem(13393, "Xeric's talisman", 1, "Teleport", 2000, true, "Forthos travel")
                ))
                .build());

        // 6. DAGANNOTH KINGS (DKS)
        addBoss(BossData.builder("dagannoth_kings", "Dagannoth Kings", BossCategory.INTERMEDIATE, BossDifficulty.INTERMEDIATE, CombatStyle.TRIBRID)
                .combatLevel(85)
                .budget(2_000_000L)
                .minSkill(Skill.MAGIC, 60)
                .minSkill(Skill.RANGED, 70)
                .minSkill(Skill.ATTACK, 70)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.MAGIC, 75)
                .recSkill(Skill.RANGED, 80)
                .recSkill(Skill.ATTACK, 80)
                .recSkill(Skill.PRAYER, 70)
                .quest(new QuestRequirement(Quest.HORROR_FROM_THE_DEEP, "Horror from the Deep", false, "God books & Waterbirth lore"))
                .prerequisite("Pet Rock & Rune/Throwing axe to enter Waterbirth Dungeon solo")
                .solo(true).group(true)
                .gp(45_000L, 1_800_000L)
                .weakness("Dagannoth Rex: Magic (Iban/Trident). Dagannoth Prime: Ranged (Blowpipe/Crossbow). Dagannoth Supreme: Melee (Whip/Fang/Tentacle).")
                .prayers("Protect from Melee (Rex), Protect from Magic (Prime), Protect from Missiles (Supreme)", "Piety / Rigour / Augury")
                .travel("Ferry to Waterbirth Island from Rellekka, descend deep into Waterbirth Dungeon to the lowest chamber.")
                .strategy("Beginners should start with Solo Rex: lure Rex to the south-east safe spot without aggroing Prime or Supreme, and cast Magic across the water safely. For full tribrid: maintain the cycle Rex -> Prime -> Supreme, ensuring you only fight one King at a time.")
                .mechanics("1. Combat Triangle: Rex is immune to Ranged/Melee (only weak to Magic). Prime has massive Magic/Melee def (only weak to Ranged). Supreme has high Magic/Ranged def (only weak to Melee).\n2. Spinolyps: Spitter bugs around the outer water pool that deal constant chip damage and poison.")
                .phases("Independent respawn cycles (90 seconds each). Kill order in rotation: Rex -> Prime -> Supreme.")
                .warnings("Do not run into the centre of the room when entering! All 3 kings will stack you simultaneously for lethal damage.")
                .tips("Bring Blood Barrage or a Guthan's set to heal to full HP off the Spinolyps between King kills.", "Fremennik Elite Diary drops all noted bones.")
                .addChecklist("Bring Pet Rock and Throwing axe (or a partner) for door entry")
                .addChecklist("Lure Rex south without getting line-of-sight from Prime")
                .addChecklist("Bring Antidote++ for Spinolyp poison")
                .addDrop("Berserker ring (1/128 from Rex)")
                .addDrop("Archers ring (1/128 from Supreme)")
                .addDrop("Seers ring (1/128 from Prime)")
                .addDrop("Dragon axe (1/128)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(5952, "Antidote++(4)", 1, "Protection", 1500, true, "Cures poison"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Range boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(560, "Death rune", 100, "Rune", 200, true, "Iban / Magic spells"),
                        new InventoryItem(8010, "Waterbirth teleport", 1, "Teleport", 1200, true, "Travel")
                ))
                .build());

        // 7. VORKATH
        addBoss(BossData.builder("vorkath", "Vorkath", BossCategory.ADVANCED, BossDifficulty.ADVANCED, CombatStyle.RANGED)
                .combatLevel(90)
                .budget(5_000_000L)
                .minSkill(Skill.RANGED, 75)
                .minSkill(Skill.PRAYER, 44)
                .minSkill(Skill.HITPOINTS, 75)
                .recSkill(Skill.RANGED, 85)
                .recSkill(Skill.PRAYER, 74)
                .recSkill(Skill.DEFENCE, 75)
                .quest(new QuestRequirement(Quest.DRAGON_SLAYER_II, "Dragon Slayer II", true, "Access to Ungael & Vorkath"))
                .prerequisite("Salve amulet (ei) (+20% damage/accuracy vs undead), Dragonfire protection, Anti-venom+")
                .solo(true).group(false)
                .gp(135_000L, 3_400_000L)
                .weakness("Dragonbane weapons (Dragon Hunter Crossbow, Dragon Hunter Lance), Salve Amulet (ei), Diamond / Ruby Dragon bolts (e)")
                .prayers("Protect from Magic (if wearing anti-dragon shield) or Protect from Missiles (if using 2H crossbow with super antifire)", "Rigour / Eagle Eye")
                .travel("Rellekka dock boat to Ungael, jump the ice barriers to Vorkath's arena.")
                .strategy("Every 6 basic attacks, Vorkath performs 1 of 2 special attacks:\n1. Zombified Spawn: Freezes you in place. Cast Crumble Undead immediately on the spawn to one-hit kill it before it explodes on you.\n2. Acid Pool / Rapid Fire: Spits acid pools across the floor and fires rapid fireballs. Turn off run and walk back and forth in a straight line ('woox walk' or standard walk) continuously until fire stops.")
                .mechanics("1. White Dragonfire (Freeze): Followed by Zombified spawn; must use Crumble Undead spell.\n2. Pink Fireball: Disables all active overhead prayers. Immediately turn Rigour and Protect back on.\n3. Vertical Fireball: Giant slow fireball launched into the air. If you stand on that tile, it deals 115+ damage (instant death). Move 2 tiles away.\n4. Acid Phase: Never stand still on green acid or fire tiles.")
                .phases("Cycles every 6 attacks between Acid Phase and Spawn Phase continuously until defeated.")
                .warnings("NEVER forget Crumble Undead runes (Chaos, Earth, Air). If the zombified spawn touches you, it explodes for up to 60+ damage!")
                .tips("Equip Salve amulet (ei) - its 20% accuracy and damage bonus stacks with Void Knight armour.", "Turn run OFF during the acid phase so you never accidentally sprint onto an acid pool.")
                .addChecklist("Equip Salve amulet (ei) (Mandatory for optimal DPS)")
                .addChecklist("Bring runes for Crumble Undead (Air, Earth, Chaos in Rune Pouch)")
                .addChecklist("Drink Extended Super Antifire + Anti-venom+ before waking Vorkath")
                .addChecklist("Move 2 tiles away when giant orange fireball is launched into air")
                .addDrop("Vorkath's head (Guaranteed at 50 kc for Ava's Assembler)")
                .addDrop("Draconic visage (1/5,000)")
                .addDrop("Skeletal visage (1/5,000)")
                .addDrop("Dragonbone necklace (1/1,000)")
                .addDrop("Vorki (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(22209, "Extended super antifire(4)", 1, "Protection", 14000, true, "Dragonfire immunity"),
                        new InventoryItem(12913, "Anti-venom+(4)", 1, "Protection", 11000, true, "Venom immunity"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 1, "Combat Potion", 12000, true, "Max range boost"),
                        new InventoryItem(139, "Prayer potion(4)", 4, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(12791, "Rune pouch (Chaos, Earth, Air)", 1, "Rune", 0, true, "Crumble Undead"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(19625, "Fremennik boots 4 / Rellekka teleport", 1, "Teleport", 1000, true, "Travel")
                ))
                .build());

        // 8. ZULRAH - THE SOLO SNAKE
        addBoss(BossData.builder("zulrah", "Zulrah", BossCategory.ADVANCED, BossDifficulty.ADVANCED, CombatStyle.HYBRID)
                .combatLevel(85)
                .budget(3_000_000L)
                .minSkill(Skill.MAGIC, 75)
                .minSkill(Skill.RANGED, 75)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.MAGIC, 85)
                .recSkill(Skill.RANGED, 85)
                .recSkill(Skill.PRAYER, 74)
                .quest(new QuestRequirement(Quest.REGICIDE, "Regicide", true, "Access to Tirannwn / Zul-Andra"))
                .prerequisite("Anti-venom+ or Serpentine helm, Rings of Recoil / Ring of Suffering")
                .solo(true).group(false)
                .gp(110_000L, 2_600_000L)
                .weakness("Green (Ranged) Phase: Weak to Magic. Red (Melee) Phase: Weak to Magic. Blue (Tanzanite) Phase: Weak to Ranged.")
                .prayers("Protect from Missiles (Green Phase), Protect from Magic (Blue Phase), Step back from tail swipe (Red Phase)", "Mystic Might / Eagle Eye / Rigour / Augury")
                .travel("Zul-Andra teleport scroll directly to the dock, or Charter ship to Port Tyras and run south.")
                .strategy("Zulrah follows 4 set rotation patterns (Rotation 1, 2, 3, 4). Memorize or use a rotation side guide:\n1. Green Phase (Ranged): Cast Magic, pray Protect from Missiles.\n2. Blue Phase (Magic): Shoot Ranged, pray Protect from Magic (watch out: can randomly fire ranged attacks).\n3. Red Phase (Melee): Stand safe, cast Magic; step 2 tiles aside when she rears back to strike with her tail.\n4. Jad Phase: Alternates every attack between Ranged and Magic. Switch overhead prayer every time Zulrah dips her head.")
                .mechanics("1. Venom Clouds: Toxic clouds cover parts of the shrine. Never stand in the venom clouds.\n2. Snakelings: Small adds that deal melee/magic damage and inflict venom. Ring of Recoil / Suffering reflects damage to kill them.\n3. Jad Phase: Predictable rapid alternation between Magic and Ranged attacks.")
                .phases("4 distinct rotations: Alpha (Green north), Beta (Green east), Gamma (Red north), Delta (Green north with melee).")
                .warnings("Always wear a Ring of Recoil or Ring of Suffering (i)! Without recoil damage, snakelings will overwhelm you.")
                .tips("Trident of the Swamp + Toxic Blowpipe is the classic effective budget hybrid setup.", "During the Jad phase, switch your prayer as soon as the attack animation begins, not when the projectile hits.")
                .addChecklist("Ensure Ring of Recoil has full charges or equip Ring of Suffering")
                .addChecklist("Drink Anti-venom+ and Extended Antifire before boarding the boat")
                .addChecklist("During Blue (Tanzanite) phase, eat up to full HP in case of random ranged attacks")
                .addChecklist("Move to the opposite side of the island when venom clouds engulf your quadrant")
                .addDrop("Tanzanite fang (Toxic blowpipe 1/512)")
                .addDrop("Magic fang (Trident of the swamp 1/512)")
                .addDrop("Serpentine visage (1/512)")
                .addDrop("Uncut onyx (1/512)")
                .addDrop("Pet snakeling (1/4,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12913, "Anti-venom+(4)", 1, "Protection", 11000, true, "Prevents envenomation"),
                        new InventoryItem(11951, "Extended antifire(4)", 1, "Protection", 7500, true, "Fire protection"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Range boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(2550, "Ring of recoil", 2, "Special Item", 1000, true, "Kill snakelings"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(12938, "Zul-andra teleport", 1, "Teleport", 8000, true, "Direct travel")
                ))
                .build());

        // 9. PHANTOM MUSPAH
        addBoss(BossData.builder("phantom_muspah", "Phantom Muspah", BossCategory.ADVANCED, BossDifficulty.ADVANCED, CombatStyle.HYBRID)
                .combatLevel(95)
                .budget(5_000_000L)
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.MAGIC, 80)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.RANGED, 88)
                .recSkill(Skill.MAGIC, 85)
                .quest(new QuestRequirement(findQuestSafe("SECRETS_OF_THE_NORTH"), "Secrets of the North", true, "Access to Weiss caverns"))
                .prerequisite("Sapphire bolts (e) / Crossbow for Prayer Shield phase, Freeze spells (Ice Barrage/Blitz)")
                .solo(true).group(false)
                .gp(120_000L, 3_000_000L)
                .weakness("Melee form: Freeze with Ice Barrage and hit with Magic (Shadow/Trident). Ranged form: Hit with Ranged (Bowfa/Crossbow). Shield phase: Sapphire bolts (e) or Smite.")
                .prayers("Protect from Magic (Ranged phase), Protect from Melee (if in melee reach)", "Rigour / Augury")
                .travel("Icy basalt to Weiss, climb down salt mine, enter ancient cavern.")
                .strategy("Step back and cast Ice Barrage whenever Muspah enters Melee Form to root it in place. Kite around while casting Magic. When in Ranged Form, switch to Ranged gear and pray Magic. During the final Shield Phase, switch on Smite and fire Sapphire Dragon Bolts (e) to rip through the prayer shield.")
                .mechanics("1. Form Swapping: Alternates between Melee Form (green spiky) and Ranged Form (grey winged).\n2. Spikes: Muspah erupts dark spikes from the ground. Hide behind charged spikes to block the homing blast.\n3. Prayer Shield Phase: Final phase grants a purple shield that requires Prayer draining (Smite + Sapphire bolt procs).")
                .phases("Form cycle throughout HP pool, culminating in Prayer Shield final stand at 15% HP.")
                .warnings("Do not let the Melee Form hit you while unfrozen! Hits over 60+ through Protect from Melee if close.")
                .tips("Sapphire bolts (e) proc drains the shield directly for huge chunks of damage.", "Greater Corruption (Arceuus) speeds up the shield phase dramatically.")
                .addChecklist("Equip crossbow with Sapphire bolts (e) for the shield phase")
                .addChecklist("Bring Ice Barrage runes or freezes for the Melee form")
                .addChecklist("Activate Smite prayer when the purple shield appears")
                .addDrop("Venator shard (1/100, 5 make Venator bow)")
                .addDrop("Muphin (pet 1/2,500)")
                .addDrop("Ancient icon (1/50)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(9240, "Sapphire dragon bolts (e)", 50, "Ammo", 600, true, "Mandatory for shield"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, false, "Melee if used"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 1, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(139, "Prayer potion(4)", 5, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(12791, "Rune pouch (Blood, Death, Water)", 1, "Rune", 0, true, "Ice Barrage"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 10. THE GAUNTLET & CORRUPTED GAUNTLET
        addBoss(BossData.builder("corrupted_gauntlet", "Corrupted Gauntlet (CG)", BossCategory.ENDGAME, BossDifficulty.GRANDMASTER, CombatStyle.TRIBRID)
                .combatLevel(90)
                .budget(0L) // Gear is entirely gathered inside the Gauntlet!
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.MAGIC, 80)
                .minSkill(Skill.DEFENCE, 75)
                .minSkill(Skill.PRAYER, 74)
                .recSkill(Skill.RANGED, 90)
                .recSkill(Skill.MAGIC, 90)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(Quest.SONG_OF_THE_ELVES, "Song of the Elves", true, "Access to Prifddinas"))
                .prerequisite("No outside items allowed; all weapons and armour are crafted inside.")
                .solo(true).group(false)
                .gp(200_000L, 4_500_000L)
                .weakness("Hunllef switches overhead prayers every 6 attacks you deal. Craft two Tier 3 perfected weapons (Bow, Staff, or Halberd).")
                .prayers("Protect from Missiles / Protect from Magic (Hunllef alternates every 4 attacks). Rigour / Augury / Piety.", "Offensive prayers")
                .travel("Prifddinas, enter the Gauntlet portal in the north-east district.")
                .strategy("You have 7:30 to explore, gather resources, craft Tier 1 or Tier 2 armor, craft two Tier 3 weapons (Bow + Staff recommended), make potions, and catch 16-24 paddlefish. During the Hunllef fight: count Hunllef's attacks (every 4 attacks it switches between Ranged and Magic). Count your own attacks (every 6 attacks it switches its overhead prayer). Never step on orange floor tiles. Keep moving when chasing tornadoes.")
                .mechanics("1. Hunllef Attack Count: Fires 4 ranged attacks, then 4 magic attacks. 4th attack has a sound cue.\n2. Prayer Disabling Orb: A purple magic attack disables your overhead prayer. Turn it back on immediately!\n3. Chasing Tornadoes: Summons glowing tornadoes that chase you. If they touch you, they stack heavy damage.\n4. Shifting Floor: Floor tiles light up orange. Standing on orange tiles deals rapid 15-20 damage.")
                .phases("Phase 1 (100%-66%): 1 tornado, slow floor.\nPhase 2 (66%-33%): 2 tornadoes, faster floor.\nPhase 3 (33%-0%): 4 tornadoes, fast shifting floor patterns.")
                .warnings("Do not run corners into tornadoes; diagonally path through safe tiles while eating.")
                .tips("Staff max hit does not scale with Magic level; staff hits maximum damage even at level 80 Magic.", "T2 armour prep is vastly safer for learners than T1 armour.")
                .addChecklist("Craft Tier 3 Bow + Tier 3 Staff")
                .addChecklist("Craft Tier 1 or Tier 2 Corrupted Armour")
                .addChecklist("Cook 16-24 Paddlefish and brew 2-3 Egniol potions")
                .addChecklist("Count Hunllef attacks (swap prayer every 4 attacks)")
                .addChecklist("Run continuously when tornadoes spawn")
                .addDrop("Enhanced crystal weapon seed (Bow of Faerdhinen / Blade of Saeldor 1/400)")
                .addDrop("Crystal armour seed (1/50)")
                .addDrop("Youngllef (pet 1/800)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(23834, "Corrupted paddlefish", 20, "Food", 0, true, "Crafted food inside"),
                        new InventoryItem(23842, "Egniol potion(4)", 3, "Combat Potion", 0, true, "Prayer + Stamina inside")
                ))
                .build());

        // 11. GENERAL GRAARDOR (BANDOS)
        addBoss(BossData.builder("general_graardor", "General Graardor", BossCategory.ENDGAME, BossDifficulty.ADVANCED, CombatStyle.MELEE)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.DEFENCE, 80)
                .minSkill(Skill.PRAYER, 70)
                .minSkill(Skill.HITPOINTS, 85)
                .recSkill(Skill.ATTACK, 90)
                .recSkill(Skill.STRENGTH, 95)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("70 Strength to enter Bandos Stronghold, Hammer to bang door, 40 Bandos killcount")
                .solo(true).group(true)
                .gp(120_000L, 2_800_000L)
                .weakness("Slash melee (Scythe, Blade of Saeldor, Tentacle), Stab (Fang), Ranged kite (Bowfa / Shadow 6:0 or 7:0)")
                .prayers("Protect from Melee (for melee tank) or Protect from Missiles (when 6:0 kiting)", "Piety / Rigour")
                .travel("God Wars Dungeon (Trollheim teleport or Stony basalt). Enter the south Bandos stronghold.")
                .strategy("In groups: Tank stands on Graardor with Protect from Melee and high defence gear (Justiciar / Torags) while attackers use Piety and high DPS weapons. In solo: use the 6:0 altar-door or red-click method with Bowfa / Shadow to kite Graardor around the room taking zero melee damage while praying Protect from Missiles against the ranged minion.")
                .mechanics("1. Heavy Melee: Graardor slams for up to 60+ melee damage if unprotected.\n2. Ranged Slam: Earth-shattering punch that hits everyone in the room for ranged damage.\n3. Minions: Sergeant Strongstack (Melee), Sergeant Steelwill (Magic), Sergeant Grimspike (Ranged).")
                .phases("Boss + 3 bodyguard minions.")
                .warnings("Graardor's melee attack can easily combo with minions for 80+ damage in a single tick.")
                .tips("Equip at least one Bandos item (e.g. Bandos blessing or boots) and one Zamorak item to avoid aggro in GWD lobby.", "Solo Bowfa 6:0 altar-door method allows 25+ kill trips without taking melee hits.")
                .addChecklist("Get 40 Bandos Killcount (kill Goblins in stronghold)")
                .addChecklist("Bring a Hammer to bang the entry door")
                .addChecklist("Equip Bandos and Zamorak god items in GWD entrance")
                .addDrop("Bandos tassets (1/381)")
                .addDrop("Bandos chestplate (1/381)")
                .addDrop("Bandos hilt (1/508)")
                .addDrop("Pet general graardor (1/5,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(2347, "Hammer", 1, "Special Item", 100, true, "Mandatory for Bandos door"),
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(3024, "Super restore(4)", 2, "Prayer Restore", 11000, true, "Stat restoration"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(8011, "Trollheim teleport", 1, "Teleport", 1500, true, "GWD travel")
                ))
                .build());

        // 12. COMMANDER ZILYANA (SARADOMIN)
        addBoss(BossData.builder("commander_zilyana", "Commander Zilyana", BossCategory.ENDGAME, BossDifficulty.ADVANCED, CombatStyle.RANGED)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.AGILITY, 70, true, "Summer pie (+5)")
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.RANGED, 90)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("70 Agility to enter Saradomin Encampment (boostable with Summer pie), Rope, 40 Saradomin killcount")
                .solo(true).group(true)
                .gp(115_000L, 2_500_000L)
                .weakness("Ranged (Bowfa, Twisted Bow, Armadyl Crossbow), Magic (Shadow). High melee speed.")
                .prayers("Protect from Magic (Zilyana fires powerful magic attacks; minion Bree also hits ranged)", "Rigour")
                .travel("God Wars Dungeon, traverse the icy waters to the south-east rock climb (70 Agility).")
                .strategy("Never let Zilyana touch you! She runs at double movement speed. Run clockwise or counter-clockwise around the perimeter of the room, firing your crossbow or bow at corners and straightaways. Keep Protect from Magic active to block her lightning attacks. Drink Stamina potions constantly.")
                .mechanics("1. Extreme Sprint Speed: Runs faster than normal monsters; must keep running without stopping.\n2. Melee Strike: If she catches up to you, she slashes for up to 31 melee damage.\n3. Lightning Magic: Shouts 'All praise Saradomin!' and calls down magic lightning.\n4. Bodyguards: Starlight (Melee), Bree (Ranged), Growler (Magic).")
                .phases("Continuous run-and-shoot rotation.")
                .warnings("Do not run out of run energy! Without Stamina active, Zilyana will immediately catch you and kill you.")
                .tips("Equip high range defence armor or crystal armor with Bowfa for massive DPS while sprinting.", "Starlight can be frozen with ice spells to keep him out of the path.")
                .addChecklist("Bring 70 Agility or Summer Pie for the rock climb")
                .addChecklist("Bring 2 Ropes for the GWD entrance and Saradomin hole")
                .addChecklist("Bring 4-5 Stamina Potions (Mandatory for running)")
                .addChecklist("Keep Protect from Magic active while running perimeter")
                .addDrop("Armadyl crossbow (1/508)")
                .addDrop("Saradomin hilt (1/508)")
                .addDrop("Saradomin sword (1/127)")
                .addDrop("Pet zilyana (1/5,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12625, "Stamina potion(4)", 4, "Utility", 8000, true, "Mandatory for running"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 2, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(954, "Rope", 1, "Special Item", 50, true, "Saradomin hole entrance"),
                        new InventoryItem(8011, "Trollheim teleport", 1, "Teleport", 1500, true, "Travel")
                ))
                .build());

        // 13. K'RIL TSUTSAROTH (ZAMORAK)
        addBoss(BossData.builder("kril_tsutsaroth", "K'ril Tsutsaroth", BossCategory.ENDGAME, BossDifficulty.ADVANCED, CombatStyle.MELEE)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.HITPOINTS, 70)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 90)
                .recSkill(Skill.STRENGTH, 95)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("70 Hitpoints to cross icy bridge, 40 Zamorak killcount")
                .solo(true).group(true)
                .gp(90_000L, 2_200_000L)
                .weakness("Demonbane weapons (Arclight, Emberlight), Slash (Fang, Scythe), Bowfa 8:0 / 5:0 method")
                .prayers("Protect from Melee (when tanking) / Protect from Magic", "Piety / Rigour")
                .travel("God Wars Dungeon, cross the frozen northern river (70 Hitpoints).")
                .strategy("In Melee: Use Arclight or Emberlight with Defender on Slash. Stand toe-to-toe and pray Protect from Melee. If K'ril shouts 'YARRRRR!', he performs his special slam which pierces Protect from Melee and drains half your prayer points. Drink Antidote++ for severe poison. In Ranged: Bowfa 8:0 door-altar prevents slam completely.")
                .mechanics("1. Yarrr Slam: Pierces prayer, dealing up to 49 damage and draining 50% current prayer.\n2. Poison Breath: Inflicts severe poison starting at 16 damage per tick.\n3. Bodyguards: Balfrug Kreeyath (Magic), Tstanon Karlak (Melee), Zakl'n Grit (Ranged).")
                .phases("Boss + 3 bodyguard demons.")
                .warnings("Do not enter without Antidote++ or Sanfew Serum! K'ril's poison hits 16 and rapidly drains health.")
                .tips("Arclight or Emberlight deals +70% accuracy and +25-35% damage against K'ril (demon).", "Keep hitpoints above 65 at all times to survive the Yarr slam.")
                .addChecklist("Ensure 70 Hitpoints to cross the northern ice bridge")
                .addChecklist("Bring Antidote++ or Anti-venom+ (Mandatory for 16-tick poison)")
                .addChecklist("Use Arclight or Emberlight on Slash")
                .addDrop("Zamorakian spear (1/127)")
                .addDrop("Staff of the dead (1/508)")
                .addDrop("Zamorak hilt (1/508)")
                .addDrop("Pet k'ril tsutsaroth (1/5,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(5952, "Antidote++(4)", 2, "Protection", 1500, true, "Cures 16 poison"),
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(3024, "Super restore(4)", 2, "Prayer Restore", 11000, true, "Restores prayer after Yarr slam"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(8011, "Trollheim teleport", 1, "Teleport", 1500, true, "Travel")
                ))
                .build());

        // 14. KREE'ARRA (ARMADYL)
        addBoss(BossData.builder("kreearra", "Kree'arra", BossCategory.ENDGAME, BossDifficulty.ADVANCED, CombatStyle.RANGED)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.PRAYER, 70)
                .minSkill(Skill.DEFENCE, 75)
                .recSkill(Skill.RANGED, 90)
                .recSkill(Skill.DEFENCE, 85)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("70 Ranged to cross the southern chasm, 40 Armadyl killcount")
                .solo(true).group(true)
                .gp(130_000L, 2_900_000L)
                .weakness("Ranged (Chinchompas targeted at minion, Bowfa, Twisted Bow, Crossbow with Diamond bolts)")
                .prayers("Protect from Missiles", "Rigour / Eagle Eye")
                .travel("God Wars Dungeon, grapple across the southern gap (70 Ranged).")
                .strategy("Kree'arra cannot be attacked with Melee. If you are not actively attacking Kree'arra with Ranged/Magic, she performs a devastating melee whirlwind attack that knocks you back across the room for 26+ damage. Always keep attacking. The Chinning method: throw Black or Red Chinchompas at the Melee minion (Flight Kilisa) while Kree'arra is stacked next to it to splash massive damage onto Kree'arra without her high ranged defence resisting.")
                .mechanics("1. Knockback Whirlwind: Hits for heavy melee if player is in melee distance and not attacking.\n2. Multi-Target Tornado: Magic and Ranged attacks hit all players in the room simultaneously.\n3. Bodyguards: Wingman Skree (Magic), Flockleader Geerin (Ranged), Flight Kilisa (Melee).")
                .phases("Boss + 3 avian bodyguards.")
                .warnings("Wear high magic/range defence armour (Karils or Armadyl). Never step into melee range unprovoked.")
                .tips("Black chinchompas on Longrange or Medium fuse thrown at Kilisa provide the highest DPS in the game at Kree'arra.", "Bring blood spells to heal off minions after Kree dies.")
                .addChecklist("Ensure 70 Ranged to cross the southern chasm")
                .addChecklist("Keep Protect from Missiles active throughout the fight")
                .addChecklist("Throw Chinchompas at the melee minion while standing in corner")
                .addDrop("Armadyl hilt (1/508)")
                .addDrop("Armadyl chestplate (1/381)")
                .addDrop("Armadyl chainskirt (1/381)")
                .addDrop("Armadyl helmet (1/381)")
                .addDrop("Pet kree'arra (1/5,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(11959, "Black chinchompa", 150, "Ammo", 2500, true, "AoE DPS weapon"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 2, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(3024, "Super restore(4)", 6, "Prayer Restore", 11000, true, "Prayer sustain"),
                        new InventoryItem(6685, "Saradomin brew(4)", 4, "Food", 6000, true, "High defence healing"),
                        new InventoryItem(8011, "Trollheim teleport", 1, "Teleport", 1500, true, "Travel")
                ))
                .build());

        // 15. CERBERUS
        addBoss(BossData.builder("cerberus", "Cerberus", BossCategory.SLAYER, BossDifficulty.ADVANCED, CombatStyle.MELEE)
                .combatLevel(95)
                .budget(4_000_000L)
                .minSkill(Skill.SLAYER, 91)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 88)
                .recSkill(Skill.STRENGTH, 92)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("Level 91 Slayer, on a Hellhound or Cerberus Slayer task")
                .solo(true).group(false)
                .gp(85_000L, 2_400_000L)
                .weakness("Crush (Inquisitor's Mace, Bludgeon, Sarachnis Cudgel), Arclight / Emberlight (demon), Fang (Stab)")
                .prayers("Protect from Magic / Melee", "Piety")
                .travel("Taverley Dungeon, descend to Key Master's cavern using Key master teleport scroll or 80 Agility shortcut.")
                .strategy("Attack with high crush or demonbane weapon. At under 400 HP (66%), Cerberus howls 'Aaarrrooooo!' and summons 3 Summoned Souls (Ghosts). Check the order of ghosts:\n- Blue Ghost: Magic\n- Red Ghost: Melee\n- Green Ghost: Ranged\nPray against each ghost in exact sequential order from left to right as their projectiles fly at you. Each blocked ghost drains 30 Prayer points (unblocked hits 30 damage!). At under 200 HP, she spits 'Grrrrr' triple lava pools - immediately run away.")
                .mechanics("1. Summoned Souls (Ghosts): 3 spirits appear on north ridge. Block with Magic/Melee/Ranged in sequence.\n2. Lava Pools: Shouts 'Grrrrrr' and spawns 3 lava puddles beneath your feet. Move away immediately.\n3. Triple Attack: Spits Magic, Ranged, then Melee in rapid succession.")
                .phases("Phase 1 (100%-66%): Standard attacks.\nPhase 2 (66%-33%): Summoned souls every 45 seconds.\nPhase 3 (33%-0%): Souls + Lava pools.")
                .warnings("Never let your Prayer drop below 30 during the Ghost phase! Unblocked ghost hits 30 pure damage.")
                .tips("Spectral Spirit Shield reduces prayer drain from ghost hits by 50% (from 30 to 15 per ghost).", "Drink a dose of Prayer potion right before the ghost attack hits so you don't get smited.")
                .addChecklist("Must be on a Hellhound Slayer task (Level 91 Slayer)")
                .addChecklist("Spectral Spirit Shield equipped for Ghost phase if owned")
                .addChecklist("Pray Ghost order: Blue=Magic, Red=Melee, Green=Ranged")
                .addChecklist("Step away when 'Grrrrrr' appears in chat (Lava pools)")
                .addDrop("Primordial crystal (1/512)")
                .addDrop("Pegasian crystal (1/512)")
                .addDrop("Eternal crystal (1/512)")
                .addDrop("Smouldering stone (1/512)")
                .addDrop("Hellpuppy (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "High prayer restore for ghosts"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(13249, "Key master teleport", 1, "Teleport", 5000, true, "Taverley travel")
                ))
                .build());

        // 16. ALCHEMICAL HYDRA
        addBoss(BossData.builder("alchemical_hydra", "Alchemical Hydra", BossCategory.SLAYER, BossDifficulty.MASTER, CombatStyle.RANGED)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.SLAYER, 95)
                .minSkill(Skill.RANGED, 85)
                .minSkill(Skill.PRAYER, 74)
                .recSkill(Skill.RANGED, 92)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("Level 95 Slayer, on a Hydra Slayer task, Boots of Stone/Brimstone/Granite (or Kourend Elite Diary)")
                .solo(true).group(false)
                .gp(150_000L, 3_800_000L)
                .weakness("Dragonbane weapons (Dragon Hunter Crossbow, Dragon Hunter Lance), Twisted Bow, Bowfa")
                .prayers("Protect from Magic / Protect from Missiles (Counts in 3s: 3 Magic, then 3 Ranged)", "Rigour")
                .travel("Karuulm Slayer Dungeon, lower elevator (Boots of stone / brimstone required unless Kourend Elite complete).")
                .strategy("Hydra's attacks follow strict counts of 3: 3 Magic projectiles, then 3 Ranged projectiles. Switch overhead prayers on every 3rd attack.\nLure Hydra onto chemical vent grates to strip its shield:\n- Green Phase (Poison): Lure onto Red Vent.\n- Blue Phase (Lightning): Lure onto Green Vent.\n- Red Phase (Fire): Lure onto Blue Vent.\n- Enraged Phase (Jad): Switches prayer every single attack!")
                .mechanics("1. Chemical Vents: Hydra has 75% damage reduction until lured onto the correct colored vent.\n2. Phase 1 Poison: Spits 4 pools of acid. Stand clear.\n3. Phase 2 Lightning: Summons 4 lightning balls. Walk north along wall to avoid.\n4. Phase 3 Fire: Traps you in a wall of fire. Path around the flame ring.\n5. Phase 4 Enraged: Spits acid, then switches between Ranged and Magic every single attack.")
                .phases("Poison (100%-75%) -> Lightning (75%-50%) -> Fire (50%-25%) -> Enraged (25%-0%).")
                .warnings("Do not forget Boots of Stone / Brimstone or you will take 20+ heat damage every tick inside Karuulm dungeon!")
                .tips("Counting out loud '1, 2, 3 - SWITCH' makes Hydra completely mechanical and effortless.", "On phase transitions, Hydra loses 1 attack from its count, skipping straight to the other style.")
                .addChecklist("Level 95 Slayer + Hydra Task required")
                .addChecklist("Equip Boots of Stone/Brimstone or complete Kourend Elite Diary")
                .addChecklist("Count in 3s: 3 Magic -> 3 Ranged -> 3 Magic")
                .addChecklist("Lure Hydra onto the matching colored chemical vent at the start of each phase")
                .addDrop("Hydra's claw (Ferocious gloves 1/1,001)")
                .addDrop("Hydra leather (1/514)")
                .addDrop("Hydra tail (1/514)")
                .addDrop("Ikkle hydra (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(23041, "Boots of stone", 1, "Protection", 1000, true, "Protects from dungeon heat"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 2, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(139, "Prayer potion(4)", 6, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(22949, "Rada's blessing 4 / Teleport", 1, "Teleport", 0, true, "Karuulm travel")
                ))
                .build());

        // 17. KRAKEN
        addBoss(BossData.builder("kraken", "Kraken", BossCategory.SLAYER, BossDifficulty.BEGINNER, CombatStyle.MAGIC)
                .combatLevel(80)
                .budget(1_000_000L)
                .minSkill(Skill.SLAYER, 87)
                .minSkill(Skill.MAGIC, 75)
                .minSkill(Skill.DEFENCE, 70)
                .recSkill(Skill.MAGIC, 85)
                .recSkill(Skill.DEFENCE, 80)
                .prerequisite("Level 87 Slayer, on a Cave Kraken Slayer task, Fishing explosive to wake up")
                .solo(true).group(false)
                .gp(30_000L, 950_000L)
                .weakness("Magic (Trident of the Seas / Swamp, Sanguinesti Staff, Shadow)")
                .prayers("Protect from Magic / Mystic Might / Augury", "Augury")
                .travel("Kraken Cove south of Piscatoris Fishing Colony (Fairy ring AKQ).")
                .strategy("Throw a Fishing Explosive into the large whirlpool to wake the main Kraken and tentacles simultaneously. Stand back and cast Trident on the Kraken. Wear high Magic Defence gear (Karil's armour / Blessed d'hide) rather than Magic Attack gear, because Kraken has 0 Magic Defence and you need defence against its magic projectiles.")
                .mechanics("1. Whirlpools: Must wake 4 small whirlpools and 1 large whirlpool (fishing explosive does all at once).\n2. Magic Attack: Kraken shoots magic projectiles that roll against player magic defence.")
                .phases("Single phase.")
                .warnings("Do not bring expensive magic robes (Ahrim/Ancestral); bring Karils or D'hide for Magic Defence!")
                .tips("Equip Sanguinesti staff or Blood Barrage for infinite trips without needing food.", "Drop fishing explosives in your inventory to wake all tentacles in 1 tick.")
                .addChecklist("Level 87 Slayer + Cave Kraken Task required")
                .addChecklist("Bring Fishing Explosives (bought from Slayer Masters)")
                .addChecklist("Wear high Magic Defence armor (Karil's or Black D'hide)")
                .addDrop("Kraken tentacle (1/512)")
                .addDrop("Trident of the seas (full 1/512)")
                .addDrop("Pet kraken (1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(6664, "Fishing explosive", 30, "Special Item", 60, true, "Wakes whirlpools"),
                        new InventoryItem(139, "Prayer potion(4)", 2, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 16, "Food", 900, true, "Healing"),
                        new InventoryItem(8009, "Teleport to house", 2, "Teleport", 800, true, "Travel")
                ))
                .build());

        // 18. GROTESQUE GUARDIANS
        addBoss(BossData.builder("grotesque_guardians", "Grotesque Guardians", BossCategory.SLAYER, BossDifficulty.INTERMEDIATE, CombatStyle.HYBRID)
                .combatLevel(85)
                .budget(2_000_000L)
                .minSkill(Skill.SLAYER, 75)
                .minSkill(Skill.ATTACK, 75)
                .minSkill(Skill.RANGED, 75)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 80)
                .recSkill(Skill.RANGED, 85)
                .recSkill(Skill.PRAYER, 70)
                .prerequisite("Level 75 Slayer, on a Gargoyle Slayer task, Brittle key to unlock roof, Rock hammer / Granitic hammer")
                .solo(true).group(false)
                .gp(45_000L, 1_300_000L)
                .weakness("Dusk: Crush / Slash melee. Dawn: Ranged (Blowpipe / Bowfa).")
                .prayers("Protect from Missiles (against Dawn) / Protect from Melee (against Dusk)", "Piety / Rigour")
                .travel("Slayer Tower in Canifis, climb to the rooftop.")
                .strategy("Phase 1: Attack Dawn with Ranged until 50% HP while dodging rockfall shadows.\nPhase 2: Attack Dusk with Melee until 50% HP, dodging his lightning attack.\nPhase 3: Both fly away; Dawn summons purple healing orbs. Walk over all 3 orbs to absorb them so Dawn cannot heal.\nPhase 4: Finish Dawn with Ranged, then Dusk enrages with a flame prison. Path out of the prison square and kill Dusk with Melee.")
                .mechanics("1. Rockfall: Shadows on ground indicate falling rubble; step aside.\n2. Purple Energy Orbs: Absorb them before Dawn eats them to prevent 90+ healing.\n3. Dusk Flame Prison: Traps you in a 3x3 fire ring; step through the gap before it explodes.")
                .phases("Dawn Ranged (P1) -> Dusk Melee (P2) -> Orb Absorption (P3) -> Enraged Dusk (P4).")
                .warnings("Do not forget your Rock Hammer or Granitic Hammer to smash them when they reach 0 HP!")
                .tips("Auto-smash perk from Slayer rewards works automatically without clicking hammer.", "Walk through the gap in Dusk's flame box immediately to avoid massive fire damage.")
                .addChecklist("Ensure Gargoyle Slayer task active (Level 75 Slayer)")
                .addChecklist("Bring Rock Hammer / Granitic Hammer")
                .addChecklist("Absorb the 3 purple energy orbs during Phase 3")
                .addDrop("Black tourmaline core (Guardian boots 1/1,000)")
                .addDrop("Granite hammer (1/750)")
                .addDrop("Noon (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(4162, "Rock hammer", 1, "Special Item", 500, true, "Smashes gargoyles"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Range boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 14, "Food", 900, true, "Food"),
                        new InventoryItem(11978, "Slayer ring(8)", 1, "Teleport", 500, true, "Slayer tower travel")
                ))
                .build());

        // 19. DUKE SUCELLUS (DT2)
        addBoss(BossData.builder("duke_sucellus", "Duke Sucellus", BossCategory.ENDGAME, BossDifficulty.MASTER, CombatStyle.MELEE)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.DEFENCE, 80)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 90)
                .recSkill(Skill.STRENGTH, 95)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(findQuestSafe("DESERT_TREASURE_II"), "Desert Treasure II", true, "Access to Duke's prison in Weiss"))
                .prerequisite("Slash melee weapon (Soulreaper axe, Scythe of vitur, Bellator ring, Arclight/Fang)")
                .solo(true).group(false)
                .gp(140_000L, 3_500_000L)
                .weakness("Slash melee. Susceptible to demonbane bonus (Arclight / Emberlight).")
                .prayers("Protect from Melee", "Piety")
                .travel("Ancient prison beneath Weiss (Desert Treasure II).")
                .strategy("Awakening Phase: Collect mushroom mushrooms and arder powder, crush and ferment two sleep potions, and feed Duke to awaken him.\nCombat Phase: Duke wakes with 600 HP. Stand 1 tile in front of Duke. Attack with Slash weapon on a 5-tick rhythm. When he charges his gaze eye attack, duck behind the large pillar to break line-of-sight. When black gas/acid erupts from vents, step aside. At 150 HP (25%), Duke goes berserk - his attack speed doubles. Stay in rhythm and execute him.")
                .mechanics("1. Gaze Attack: Duke's central eye glows white. Hide behind a pillar or take 80+ damage.\n2. Vent Gas: Dark shadows appear on floor tiles; step onto safe tiles.\n3. Enrage (sub 25%): Attack speed doubles from 5 ticks to 2.5 ticks; stay calm and maintain pillar hides.")
                .phases("Awakening (potions) -> Main combat -> Enrage (sub 25%).")
                .warnings("Do not be in Duke's line of sight when his gaze explodes! Hide behind the pillar.")
                .tips("Arclight / Emberlight deals massive bonus damage because Duke is classified as a Demon.", "Pre-craft potions quickly to reduce fight downtime.")
                .addChecklist("Bring Slash weapon (Arclight / Emberlight / Soulreaper / Fang)")
                .addChecklist("Hide behind pillar when white eye gaze charges")
                .addChecklist("Dodge floor vents when black miasma erupts")
                .addDrop("Magus vestige (1/720 roll)")
                .addDrop("Eye of the duke (Soulreaper axe piece)")
                .addDrop("Virtus robe pieces")
                .addDrop("Baron (pet 1/2,500)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(139, "Prayer potion(4)", 5, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 20. VARDORVIS (DT2)
        addBoss(BossData.builder("vardorvis", "Vardorvis", BossCategory.ENDGAME, BossDifficulty.MASTER, CombatStyle.MELEE)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.DEFENCE, 80)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 90)
                .recSkill(Skill.STRENGTH, 95)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(findQuestSafe("DESERT_TREASURE_II"), "Desert Treasure II", true, "Access to Stranglewood"))
                .prerequisite("Slash melee weapon (Soulreaper Axe, Blade of Saeldor, Abyssal Tentacle, Fang)")
                .solo(true).group(false)
                .gp(150_000L, 3_800_000L)
                .weakness("Slash melee. High susceptibility to Blood fury healing.")
                .prayers("Protect from Melee (Primary), Protect from Missiles (when green head fires)", "Piety")
                .travel("The Stranglewood (Kourend south-east jungle).")
                .strategy("Fast-paced reaction fight! Stand in the centre of the arena with Protect from Melee. Four whirring flying axes spawn from corners and fly inward in straight lines; dodge the axes by stepping onto open tiles. When a green severed head appears in a corner, switch prayer to Protect from Missiles for 1 tick to block the dart, then switch back to Melee. When tentacle spikes sprout from the ground, move off them immediately.")
                .mechanics("1. Flying Axes: 4 spinning axes spawn on the arena perimeter and cross the arena; getting clipped deals 30+ damage and inflicts bleeding.\n2. Strangle Spikes: Ground entangles you; click the tentacles to free yourself.\n3. Severed Head Projectile: Green projectile fired from corner; requires instant Protect from Missiles switch.\n4. Berserk Speed: Attack speed and axe speed ramp up as HP decreases.")
                .phases("Single continuous encounter; speed increases proportionally to missing HP.")
                .warnings("Do not panic eat! Eating during axe passes slows movement and causes you to get clipped by flying axes.")
                .tips("Amulet of Blood Fury heals you continuously throughout the relentless chip damage.", "Stay near the center column so you have maximum view of incoming axes.")
                .addChecklist("Equip high Slash weapon (Blade of Saeldor / Tentacle / Soulreaper)")
                .addChecklist("Amulet of Blood Fury highly recommended")
                .addChecklist("Dodge spinning axes across perimeter")
                .addChecklist("Flick Protect from Missiles when green head projectile flies")
                .addDrop("Ultor vestige (1/720 roll)")
                .addDrop("Executioner's axe head (Soulreaper piece)")
                .addDrop("Virtus robe pieces")
                .addDrop("Butch (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(139, "Prayer potion(4)", 5, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 21. THE WHISPERER (DT2)
        addBoss(BossData.builder("the_whisperer", "The Whisperer", BossCategory.ENDGAME, BossDifficulty.MASTER, CombatStyle.MAGIC)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.MAGIC, 85)
                .minSkill(Skill.DEFENCE, 75)
                .minSkill(Skill.PRAYER, 74)
                .recSkill(Skill.MAGIC, 92)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(findQuestSafe("DESERT_TREASURE_II"), "Desert Treasure II", true, "Access to Lassar Undercity"))
                .prerequisite("Powered magic staff (Shadow, Sang staff, Trident of the swamp)")
                .solo(true).group(false)
                .gp(160_000L, 4_000_000L)
                .weakness("Magic damage (Shadow / Sanguinesti / Trident). Shadow spells freeze her.")
                .prayers("Protect from Missiles / Magic (Counts in 3s like Hydra, alternating styles)", "Augury")
                .travel("Lassar Undercity / Camdoalza temple (Desert Treasure II).")
                .strategy("Rhythm based encounter. Whisperer fires attacks in sets of 3 (3 Ranged, then 3 Magic, or vice versa). Switch prayer after every 3rd projectile. Special attacks trigger between phases:\n1. Corrupted Seeds: Step onto safe glowing pillars.\n2. Shadow Realm: Jump into the shadow realm to hit soul pillars or bind the lost souls.\n3. Sanity Management: Sanity drains when making mistakes; maintain sanity to prevent insanity hallucination damage. Enrage phase at 20%: Rapid 1-tick attack rhythm while dodging tentacles.")
                .mechanics("1. 3-Attack Rhythm: 3 Ranged spikes -> 3 Magic tendrils. Switch overhead prayer every 3 hits.\n2. Sanity Meter: Taking hits drains sanity; below 0% triggers hallucinations.\n3. Shadow Realm: Dark rift portals to solve puzzle minigames.\n4. Enrage Phase: Rushes into melee distance and fires 1-tick attacks while tentacles sweep the floor.")
                .phases("Phase 1 -> Special 1 -> Phase 2 -> Special 2 -> Phase 3 -> Enrage (sub 20%).")
                .warnings("Do not lose your attack count rhythm during tentacle waves; stay in cadence.")
                .tips("Cast Shadow Barrage to freeze Whisperer in place during the enrage phase to safely cast spells from distance.", "Practice counting: '1, 2, 3 - SWITCH'.")
                .addChecklist("Bring powered staff (Shadow / Sang / Trident)")
                .addChecklist("Count in 3s: 3 Ranged -> 3 Magic")
                .addChecklist("Enter Shadow Realm when special attack prompts")
                .addDrop("Bellator vestige (1/720 roll)")
                .addDrop("Sirenic soul (Soulreaper piece)")
                .addDrop("Virtus robe pieces")
                .addDrop("Wisp (pet 1/2,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(139, "Prayer potion(4)", 5, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(3024, "Super restore(4)", 2, "Prayer Restore", 11000, true, "Restore sanity drain"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 22. THE LEVIATHAN (DT2)
        addBoss(BossData.builder("the_leviathan", "The Leviathan", BossCategory.ENDGAME, BossDifficulty.MASTER, CombatStyle.RANGED)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.RANGED, 85)
                .minSkill(Skill.PRAYER, 74)
                .minSkill(Skill.DEFENCE, 75)
                .recSkill(Skill.RANGED, 92)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(findQuestSafe("DESERT_TREASURE_II"), "Desert Treasure II", true, "Access to Scar"))
                .prerequisite("Ranged weapon (Bowfa, Zaryte crossbow, Twisted bow, Blowpipe), Shadow spells to stun")
                .solo(true).group(false)
                .gp(150_000L, 3_900_000L)
                .weakness("Ranged attacks. Shadow spells (Shadow Burst / Blitz / Barrage) stun its mouth.")
                .prayers("Protect from Missiles / Magic / Melee (Reactions to incoming projectiles)", "Rigour")
                .travel("The Scar (Beneath Temple of the Eye).")
                .strategy("The Leviathan fires Ranged, Magic, and Melee projectiles at an accelerating speed. Look at the projectile shape:\n- Blue Orb: Pray Magic\n- Green Spike: Pray Missiles\n- Orange Boulder: Pray Melee\nAs attack speed accelerates to blistering speed, cast Shadow Barrage / Blitz into its open mouth to stun it. When stunned, run behind it and attack the glowing white weak point on its back for guaranteed massive damage. Enrage at 20%: Leviathan enters feeding frenzy; follow the moving abyssal orb in a circle around the arena while attacking.")
                .mechanics("1. Accelerating Fire: Starts at 5-tick speed and speeds up to 1-tick machine-gun speed until stunned.\n2. Shadow Stun: Casting a Shadow spell stuns Leviathan and exposes the back weak point.\n3. Debris & Lightning: Rocks fall from ceiling, electricity sweeps floor tiles.\n4. Enrage Phase: A blue eye orb circles the room; stand inside the orb aura to deal damage without taking fatal chip damage.")
                .phases("Speed-up cycles until stunned -> Enrage at 20% HP.")
                .warnings("Do not miss the stun timing! If Leviathan reaches maximum speed without a stun, you will be overwhelmed.")
                .tips("Smoke / Shadow spells from Ancient Magicks are mandatory to stun the boss.", "Web weaver bow or Zaryte crossbow special attack deals enormous damage to the back weak spot.")
                .addChecklist("Bring Shadow spells to stun Leviathan")
                .addChecklist("React to projectile colors: Blue=Mage, Green=Range, Orange=Melee")
                .addChecklist("Run behind Leviathan to strike weak spot when stunned")
                .addDrop("Venator vestige (1/720 roll)")
                .addDrop("Leviathan's lure (Soulreaper piece)")
                .addDrop("Virtus robe pieces")
                .addDrop("Lil'viathan (pet 1/2,500)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(2444, "Divine ranging potion(4)", 2, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(139, "Prayer potion(4)", 5, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(12791, "Rune pouch (Shadow spell runes)", 1, "Rune", 0, true, "Mandatory stun"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 23. TOMBS OF AMASCUT (TOA - RAID 3)
        addBoss(BossData.builder("tombs_of_amascut", "Tombs of Amascut (ToA)", BossCategory.RAID, BossDifficulty.ADVANCED, CombatStyle.TRIBRID)
                .combatLevel(90)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 75)
                .minSkill(Skill.STRENGTH, 80)
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.MAGIC, 80)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 85)
                .recSkill(Skill.STRENGTH, 90)
                .recSkill(Skill.RANGED, 90)
                .recSkill(Skill.MAGIC, 90)
                .recSkill(Skill.PRAYER, 77)
                .quest(new QuestRequirement(findQuestSafe("BENEATH_CURSED_SANDS"), "Beneath Cursed Sands", true, "Access to Necropolis & ToA"))
                .prerequisite("Keris partisan (huge damage vs Kephri), Stab weapon (Fang/Hasta), Blowpipe, Trident/Shadow")
                .solo(true).group(true)
                .gp(250_000L, 5_500_000L)
                .weakness("Kephri: Keris Partisan (Kalphite). Zebak: Ranged (Bowfa/Crossbow). Ba-Ba: Melee (Fang). Akkha: Tribrid rotation. Wardens: Tribrid.")
                .prayers("Flexible overhead prayers per room. Protect from Magic / Missiles / Melee.", "Piety / Rigour / Augury")
                .travel("Necropolis in Kharidian Desert (Fairy ring AKP or Pharaoh's sceptre).")
                .strategy("ToA is the most accessible raid in OSRS thanks to its Invocation system (Entry Mode 0-145, Normal Mode 150-295, Expert Mode 300+). Conquer 4 demi-boss paths:\n1. Scabaras (Kephri): Kalphite queen; use Keris Partisan, avoid dung and flies.\n2. Crondis (Zebak): Giant crocodile; use Ranged, solve water puzzle, hide behind rocks during roar.\n3. Apep (Ba-Ba): Baboon boss; pray Melee, dodge rolling boulders with Blowpipe.\n4. Het (Akkha): Switches overheads and attacks every 80 damage. Fight with combat triangle counter. Final Wardens encounter: Obelisk -> P2 robot -> P3 final stand.")
                .mechanics("1. Invocations: Customize difficulty, monster health, and mechanics to set raid level.\n2. Supply Chest: Helpful spirit gives Ambrosia (instant 100% HP & Prayer), Nectar, and Tears between rooms.\n3. Wardens Phase 3: Skulls appear on floor; attack skulls with melee/range before countdown ends.")
                .phases("4 Demi-boss paths in chosen order -> Wardens P1, P2, P3.")
                .warnings("Do not fail the Wardens skull phase! Missing a single skull causes an instant raid wipe.")
                .tips("Keris Partisan is completely free from Beneath Cursed Sands and out-damages expensive weapons at Kephri.", "Start at 50 invocation entry mode to learn room layouts safely.")
                .addChecklist("Complete Beneath Cursed Sands for entry")
                .addChecklist("Bring Keris Partisan for Kephri")
                .addChecklist("Bring Melee (Fang/Hasta), Ranged (Blowpipe/Bowfa), and Magic (Trident) gear")
                .addDrop("Tumeken's shadow (1/24 in 300 inv)")
                .addDrop("Osmumten's fang (1/3.4 purple)")
                .addDrop("Elidinis' ward")
                .addDrop("Masori armour pieces")
                .addDrop("Elidinis' guardian (pet 1/1,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(25985, "Keris partisan", 1, "Weapon", 0, true, "Mandatory for Kephri"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 1, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11000, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 8, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 24. CHAMBERS OF XERIC (COX - RAID 1)
        addBoss(BossData.builder("chambers_of_xeric", "Chambers of Xeric (CoX)", BossCategory.RAID, BossDifficulty.MASTER, CombatStyle.TRIBRID)
                .combatLevel(100)
                .budget(10_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.RANGED, 85)
                .minSkill(Skill.MAGIC, 85)
                .minSkill(Skill.HERBLORE, 78, true, "Botanist (+4) / Overload (+12)")
                .recSkill(Skill.HERBLORE, 90)
                .recSkill(Skill.RANGED, 92)
                .recSkill(Skill.MAGIC, 90)
                .prerequisite("78+ Herblore to brew internal potions (Overload / Revitalisation), Lockpick, Salve (ei)")
                .solo(true).group(true)
                .gp(300_000L, 4_800_000L)
                .weakness("Tekton: Crush. Vasa: Stab / Ranged. Muttadiles: Slash / Ranged. Olm: Magic (Right hand), Melee (Left hand), Ranged (Head).")
                .prayers("Overhead prayers per monster room. Olm: Protect from Magic (Mage hand), Missiles (Range attacks).", "Piety / Rigour / Augury")
                .travel("Mount Quidamortem in Kourend (Xeric's Talisman teleport to Xeric's Honour).")
                .strategy("Raid through procedural puzzle and boss rooms (Tekton, Shamans, Vasa, Guardians, Mystics, Muttadile). Harvest herbs and mushrooms to craft Overloads and Xeric's Aid. Face the Great Olm:\n- Mage Hand (Right Hand): Cast Magic while running back and forth along the wall ('running the head').\n- Melee Hand (Left Hand): Switch to Melee and execute 4:1 or 3:1 rotation.\n- Head Phase: Olm shoots lightning, falling crystals, and acid pools. Burn down the head with Ranged.")
                .mechanics("1. Olm Head Turning: Olm attacks the sector he is facing; running across turns his head and cancels attacks.\n2. Crystal Bombs: Small crystal shards tick down to a devastating 50+ explosion; step far away.\n3. Lightning Wall: Sparks travel in straight lines across the floor; do not step into the electricity.\n4. Teleport Pairs: Portals appear beneath you; stand on matching paired portal to negate 40+ damage.")
                .phases("Scouting rooms -> Olm Phase 1, 2, 3 -> Final Head Stand.")
                .warnings("Do not stand near glowing crystal bombs! Explodes for 60+ damage.")
                .tips("Level 78 Herblore allows you to brew Overload (-) inside the raid; 90 Herblore allows Overload (+).", "Dragon Warhammer or Bandos Godsword special attacks are vital to strip Olm's Defence.")
                .addChecklist("Ensure 78+ Herblore (or pie boost) to brew raid overloads")
                .addChecklist("Bring Defence-draining spec weapon (Dragon Warhammer or BGS)")
                .addChecklist("Bring Tribrid gear: Melee (Fang/Lance), Ranged (Blowpipe/Bowfa), Magic (Trident/Shadow)")
                .addDrop("Twisted bow (1/34.5 purples)")
                .addDrop("Dexterous prayer scroll (Rigour 1/3.4)")
                .addDrop("Arcane prayer scroll (Augury 1/3.4)")
                .addDrop("Ancestral robe pieces")
                .addDrop("Olmlet (pet 1/53 raids)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(11802, "Armadyl godsword / BGS", 1, "Special Attack", 15000000, true, "Defence reduction"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 1, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11000, true, "Stat restoration"),
                        new InventoryItem(385, "Shark", 6, "Food", 900, true, "Raid start food"),
                        new InventoryItem(13393, "Xeric's talisman", 1, "Teleport", 2000, true, "Travel")
                ))
                .build());

        // 25. THEATRE OF BLOOD (TOB - RAID 2)
        addBoss(BossData.builder("theatre_of_blood", "Theatre of Blood (ToB)", BossCategory.RAID, BossDifficulty.GRANDMASTER, CombatStyle.TRIBRID)
                .combatLevel(110)
                .budget(15_000_000L)
                .minSkill(Skill.ATTACK, 85)
                .minSkill(Skill.STRENGTH, 90)
                .minSkill(Skill.DEFENCE, 80)
                .minSkill(Skill.RANGED, 85)
                .minSkill(Skill.MAGIC, 85)
                .minSkill(Skill.PRAYER, 77)
                .recSkill(Skill.ATTACK, 95)
                .recSkill(Skill.STRENGTH, 99)
                .recSkill(Skill.RANGED, 95)
                .recSkill(Skill.MAGIC, 95)
                .quest(new QuestRequirement(Quest.PRIEST_IN_PERIL, "Priest in Peril", true, "Access to Morytania"))
                .prerequisite("Scythe of vitur / Blade / Tentacle on Slash, Chinchompas, Freeze spells for Maiden, Poison weapon for Nylo")
                .solo(false).group(true)
                .gp(400_000L, 6_000_000L)
                .weakness("Maiden: Slash / Ice Barrage blood spawns. Bloat: Slash melee. Nylocas: Tribrid. Sotetseg: Slash. Xarpus: Ranged / Melee stepback. Verzik: Slash.")
                .prayers("High level prayer switches: Piety, Rigour, Augury. Overheads per room.", "Piety / Rigour")
                .travel("Ver Sinhaza in Darkmeyer (Drakan's medallion directly to ToB).")
                .strategy("6 brutal boss rooms:\n1. The Maiden of Suginti: Freeze red blood spawns before they reach Maiden; kite blood trails.\n2. Pestilent Bloat: Walk behind Bloat while he patrols; attack during down phase; hide behind pillars during flies.\n3. Nylocas: Aggressive wave defense matching combat triangle colors (White=Melee, Green=Range, Blue=Mage).\n4. Sotetseg: Maze phase requires running on illuminated path tiles without falling.\n5. Xarpus: Screech phase; step back 1 tile on attack rhythm to avoid poison reflections.\n6. Lady Verzik Vitur: P1 shield pillar hide -> P2 bouncing cabbage walk -> P3 green ball tank, web running, and tornado kiting.")
                .mechanics("1. Wipe Penalty: Total team wipe ends the raid and forfeits 100k GP fee.\n2. Verzik P2 Bounce: Stepping adjacent to Verzik off-tick causes her to stomp players into the wall for 60+.\n3. Sotetseg Maze: 1 player sees the safe path in the shadow realm and must guide the team across.")
                .phases("6 distinct rooms: Maiden -> Bloat -> Nylocas -> Sotetseg -> Xarpus -> Verzik.")
                .warnings("ToB is strictly cooperative high-mechanic PvM. One mistake during Verzik P2 or Bloat can instantly wipe the party.")
                .tips("Entry Mode is available to practice all 6 boss mechanics with infinite lives.", "Blade of Saeldor, Tentacle whip, or Scythe of Vitur on Slash is the primary damage source.")
                .addChecklist("Bring Drakan's Medallion for instant Ver Sinhaza teleport")
                .addChecklist("Bring Ice Barrage runes for Maiden blood spawns")
                .addChecklist("Bring Chinchompas for Nylocas waves")
                .addDrop("Scythe of vitur (1/19 purples)")
                .addDrop("Ghrazi rapier")
                .addDrop("Sanguinesti staff")
                .addDrop("Justiciar armour pieces")
                .addDrop("Lil' zik (pet 1/650)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 1, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11000, true, "Prayer sustain"),
                        new InventoryItem(6685, "Saradomin brew(4)", 6, "Food", 6000, true, "High healing for mistakes"),
                        new InventoryItem(22400, "Drakan's medallion", 1, "Teleport", 0, true, "Travel")
                ))
                .build());

        // 26. NEX
        addBoss(BossData.builder("nex", "Nex", BossCategory.ENDGAME, BossDifficulty.GRANDMASTER, CombatStyle.RANGED)
                .combatLevel(110)
                .budget(10_000_000L)
                .minSkill(Skill.RANGED, 85)
                .minSkill(Skill.DEFENCE, 80)
                .minSkill(Skill.PRAYER, 74)
                .recSkill(Skill.RANGED, 95)
                .recSkill(Skill.DEFENCE, 90)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("Frozen key (4 key pieces from GWD bosses), Ecumenical key or 40 Ancient killcount")
                .solo(false).group(true)
                .gp(280_000L, 5_200_000L)
                .weakness("Ranged attacks (Zaryte Crossbow with Ruby bolts, Twisted Bow, Fang on stab for minions).")
                .prayers("Protect from Magic / Protect from Missiles (Depends on phase)", "Rigour")
                .travel("Ancient Prison below God Wars Dungeon (use Frozen key on the south-east door).")
                .strategy("Nex moves through 5 distinct elemental phases, summoning 1 minion after each phase:\n1. Smoke Phase: Shouts 'Fumus, don't fail me!'. Spreads cough disease. Kill Fumus.\n2. Shadow Phase: Shouts 'Umbra, don't fail me!'. Shadow traps appear on floor; move 2 tiles away. Kill Umbra.\n3. Blood Phase: Shouts 'Cruor, don't fail me!'. Nex Siphon heals her if you attack during her red wings stance. Kill Blood Reavers. Kill Cruor.\n4. Ice Phase: Shouts 'Glacies, don't fail me!'. Traps player in ice prison (team must break ice). Kill Glacies.\n5. Zaros Phase: Deflects damage, activates wrath on death.")
                .mechanics("1. Blood Siphon: When Nex shields herself in red wings, DO NOT ATTACK! Every hit heals her.\n2. Ice Prison: Traps target in ice spikes dealing 75 damage unless teammates attack the ice stalagmites.\n3. Shadow Traps: Black shadows appear beneath player feet; step 1 tile away to avoid 50+ damage.\n4. Blood Sacrifices: Targeted player's cape turns red; run away from Nex until pink aura fades.")
                .phases("Smoke -> Shadow -> Blood -> Ice -> Zaros.")
                .warnings("NEVER hit Nex while she is siphoning (wings wrapped in blood)! You will reset minutes of team progress.")
                .tips("Ruby dragon bolts (e) are overwhelmingly the best ammunition due to Nex's gigantic 3,400 HP pool.", "Keep your distance from teammates during Smoke Phase to avoid spreading cough.")
                .addChecklist("Assemble the Frozen Key to open Ancient Prison")
                .addChecklist("Bring Ruby Dragon Bolts (e) and Diamond Dragon Bolts (e)")
                .addChecklist("Cease attacking during Blood Siphon")
                .addChecklist("Free imprisoned teammates during Ice Phase")
                .addDrop("Torva full helm (1/258)")
                .addDrop("Torva platebody (1/258)")
                .addDrop("Torva platelegs (1/258)")
                .addDrop("Zaryte crossbow (1/258)")
                .addDrop("Zaryte vambraces (1/172)")
                .addDrop("Nexling (pet 1/500)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(21905, "Ruby dragon bolts (e)", 150, "Ammo", 1800, true, "Max damage vs high HP"),
                        new InventoryItem(21932, "Diamond dragon bolts (e)", 100, "Ammo", 2200, true, "Zaros phase finish"),
                        new InventoryItem(2444, "Divine ranging potion(4)", 2, "Combat Potion", 12000, true, "Ranged boost"),
                        new InventoryItem(6685, "Saradomin brew(4)", 8, "Food", 6000, true, "Sustained high healing"),
                        new InventoryItem(3024, "Super restore(4)", 4, "Prayer Restore", 11000, true, "Restore brew stats"),
                        new InventoryItem(8011, "Trollheim teleport", 1, "Teleport", 1500, true, "Travel")
                ))
                .build());

        // 27. CORPOREAL BEAST
        addBoss(BossData.builder("corporeal_beast", "Corporeal Beast (Corp)", BossCategory.ENDGAME, BossDifficulty.MASTER, CombatStyle.MELEE)
                .combatLevel(100)
                .budget(5_000_000L)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 90)
                .recSkill(Skill.STRENGTH, 95)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("Zamorakian spear or Osmumten's Fang (spears/stabs deal 100% damage; all other weapons deal 50% reduced damage!)")
                .solo(true).group(true)
                .gp(80_000L, 2_400_000L)
                .weakness("Spears and Halberds (Zamorakian spear), Osmumten's Fang. Special attacks (DWH, Arclight, BGS) to drain stats.")
                .prayers("Protect from Magic", "Piety")
                .travel("Games necklace to Corporeal Beast lair.")
                .strategy("Damage Reduction Trait: Non-spear weapons suffer a 50% damage penalty! Always use a Zamorakian Spear or Fang on Stab.\nSpec Drain Solo Strategy (POH teleport method):\n1. 3 successful Dragon Warhammer specs (reduce defence to zero).\n2. 20 successful Arclight specs (reduce attack, strength, and defence to zero).\n3. 200+ Bandos Godsword spec damage (reduce magic level to zero).\nOnce fully drained, Corp becomes a giant punching bag that cannot hit through your armour!")
                .mechanics("1. Dark Energy Core: A shadowy ball spawns and bounces onto players, draining HP and healing Corp. Step 1 tile when it bounces to dodge it.\n2. Magic Splash: Fires magic bombs that splash into smaller clusters.\n3. Stomp: Deals heavy crush damage if player stands directly underneath.")
                .phases("Single phase.")
                .warnings("Do not bring bladed swords, bows, or whips - non-spear weapons have their damage cut in HALF!")
                .tips("POH with Ornate Rejuvenation Pool + Desert Amulet 4 / Games necklace makes solo spec-draining effortless.", "Step 1 tile whenever the dark energy core jumps to avoid getting leached.")
                .addChecklist("Equip Zamorakian Spear or Fang on Stab (Mandatory)")
                .addChecklist("Bring DWH, Arclight, and BGS if performing spec drains")
                .addChecklist("Keep Protect from Magic active")
                .addDrop("Elysian sigil (1/4,095)")
                .addDrop("Arcane sigil (1/1,365)")
                .addDrop("Spectral sigil (1/1,365)")
                .addDrop("Pet dark core (1/5,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(11889, "Zamorakian spear", 1, "Weapon", 3500000, true, "Mandatory spear weapon"),
                        new InventoryItem(12695, "Super combat potion(4)", 2, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(139, "Prayer potion(4)", 4, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal"),
                        new InventoryItem(3853, "Games necklace(8)", 1, "Teleport", 1000, true, "Direct travel")
                ))
                .build());

        // 28. WILDERNESS BOSS: CALVAR'ION / VET'ION
        addBoss(BossData.builder("calvarion", "Calvar'ion", BossCategory.WILDERNESS, BossDifficulty.INTERMEDIATE, CombatStyle.MELEE)
                .combatLevel(75)
                .budget(300_000L)
                .minSkill(Skill.ATTACK, 60)
                .minSkill(Skill.STRENGTH, 65)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 75)
                .recSkill(Skill.STRENGTH, 80)
                .recSkill(Skill.PRAYER, 70)
                .prerequisite("Crush melee weapon (Ursine chainmace, Dragon mace, Zombie axe, Viggora's chainmace), Salve amulet (e/ei)")
                .solo(true).group(false)
                .gp(45_000L, 1_900_000L)
                .weakness("Crush melee (+100% damage from Wilderness weapons; +20% damage from Salve amulet vs undead)")
                .prayers("Protect from Missiles / Melee", "Piety")
                .travel("Level 21 Wilderness graveyard cave (Cemetery teleport or Annakarl). Sub-30 Wilderness allows instant teleport!")
                .strategy("Calvar'ion is the solo variant of Vet'ion in sub-30 Wilderness. Stand and attack with a Crush weapon and Salve amulet. When Calvar'ion slams the ground, orange shockwaves spread in a grid - dodge the orange tiles. When lightning strikes, avoid the glowing ground rings. At half HP in both phases, he summons 2 undead skeletal hellhounds; kill the hounds before resuming damage.")
                .mechanics("1. Shield Phase: Cannot be damaged while skeletal hellhounds are alive.\n2. Shockwave Grid: Orange explosion crosses the room in a cross or diagonal; step 1 tile aside.\n3. Wilderness Risk: Pkers can enter the cave. Keep Player Indicators on and one-click teleport ready.")
                .phases("Phase 1 (Skeletal) -> Hounds -> Phase 2 (Enraged orange) -> Hounds -> Dead.")
                .warnings("Do not risk items you aren't willing to lose! Keep Protect Item on if carrying a +1.")
                .tips("Dragon mace or Zombie axe with Salve amulet (e) shreds Calvar'ion for almost zero risk.", "Royal Seed Pod or Amulet of Glory allows instant escape below 30 Wilderness.")
                .addChecklist("Bring Salve Amulet (e) or (ei) (+20% damage vs undead)")
                .addChecklist("Use a weapon with CRUSH style")
                .addChecklist("Bring one-click teleport for level 21 Wilderness")
                .addDrop("Voidwaker blade (1/912)")
                .addDrop("Skull of vet'ion (1/618)")
                .addDrop("Ring of the gods (1/716)")
                .addDrop("Vet'ion jr. (pet 1/2,800)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(10581, "Salve amulet (e)", 1, "Armour", 0, true, "Mandatory damage boost"),
                        new InventoryItem(1434, "Dragon mace", 1, "Weapon", 35000, true, "Low risk crush weapon"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(19564, "Royal seed pod", 1, "Teleport", 0, true, "One-click sub-30 escape")
                ))
                .build());

        // 29. WILDERNESS BOSS: SPINDEL / VENENATIS
        addBoss(BossData.builder("spindel", "Spindel", BossCategory.WILDERNESS, BossDifficulty.INTERMEDIATE, CombatStyle.MELEE)
                .combatLevel(75)
                .budget(300_000L)
                .minSkill(Skill.ATTACK, 65)
                .minSkill(Skill.STRENGTH, 70)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 75)
                .recSkill(Skill.STRENGTH, 80)
                .recSkill(Skill.PRAYER, 70)
                .prerequisite("Crush melee weapon (Dragon mace, Zombie axe, Ursine chainmace), Anti-venom or Antidote++")
                .solo(true).group(false)
                .gp(45_000L, 1_850_000L)
                .weakness("Crush melee. Heavy damage from Wilderness weapons.")
                .prayers("Protect from Magic", "Piety")
                .travel("Level 28 Wilderness cave near Spider Hill. Sub-30 Wilderness allows instant teleport!")
                .strategy("Enter the cave and attack with a Crush weapon while praying Protect from Magic. When Spindel spits white webs across the floor, never step into the webs (they drain run energy and deal continuous damage). Kill or ignore the spawned spiderlings. At 50% HP, Spindel summons a large spider add that shoots draining attacks - kill it quickly.")
                .mechanics("1. Web Spitting: Creates lines of sticky webs; stepping on them deals damage and drains run.\n2. Venom Spit: Inflicts severe venom; require Anti-venom+.\n3. Teleport Escape: Below 30 Wilderness; keep your one-click teleport handy.")
                .phases("Phase 1 -> Spiderling spawn at 50% -> Phase 2.")
                .warnings("Keep Protect from Magic active to block her high-damage projectile.")
                .tips("Ursine chainmace / Dragon mace on Crush mode rips through her Defence.", "Keep Antidote++ or Anti-venom active to prevent venom damage.")
                .addChecklist("Bring Antidote++ or Anti-venom+")
                .addChecklist("Use a weapon set to CRUSH style")
                .addChecklist("Keep Protect from Magic active")
                .addDrop("Voidwaker gem (1/912)")
                .addDrop("Fangs of venenatis (1/618)")
                .addDrop("Treasonous ring (1/716)")
                .addDrop("Venenatis spiderling (pet 1/2,800)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(5952, "Antidote++(4)", 1, "Protection", 1500, true, "Cures venom"),
                        new InventoryItem(1434, "Dragon mace", 1, "Weapon", 35000, true, "Crush weapon"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(19564, "Royal seed pod", 1, "Teleport", 0, true, "Escape")
                ))
                .build());

        // 30. WILDERNESS BOSS: ARTIO / CALLISTO
        addBoss(BossData.builder("artio", "Artio", BossCategory.WILDERNESS, BossDifficulty.INTERMEDIATE, CombatStyle.MAGIC)
                .combatLevel(75)
                .budget(300_000L)
                .minSkill(Skill.MAGIC, 70)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.MAGIC, 80)
                .recSkill(Skill.PRAYER, 70)
                .prerequisite("Freeze spells (Ice Barrage / Ice Blitz / Entangle), Magic weapon (Accursed sceptre / Trident / Ancient staff)")
                .solo(true).group(false)
                .gp(45_000L, 1_900_000L)
                .weakness("Magic damage (Very low Magic Defence; highly susceptible to Ice spells and freezes)")
                .prayers("Protect from Missiles", "Mystic Might / Augury")
                .travel("Level 21 Wilderness cave near Bandit Camp. Sub-30 Wilderness allows instant teleport!")
                .strategy("Artio deals devastating melee knockback damage if he reaches you! Immediately cast Ice Barrage / Blitz or Entangle upon entering to freeze him in place. Stand at maximum distance and cast Magic spells while praying Protect from Missiles to block his roar projectiles. Re-freeze him every time he thaws before he can walk over to you. Dodge bear traps spawned on the ground.")
                .mechanics("1. Freezing: Artio MUST be kept frozen; melee knockback hits 50+ through prayer.\n2. Roar Attack: White shockwave projectile; block with Protect from Missiles.\n3. Bear Traps: Ground traps that snap onto players, dealing 20 damage and immobilizing them.")
                .phases("Continuous freeze-kite encounter.")
                .warnings("NEVER let Artio reach melee distance! His paw swipe hits 55+ and throws you across the room.")
                .tips("Accursed Sceptre or Blood bark armour with Ice Barrage gives free, effortless kills without taking damage.", "Step 2 tiles away from snapping bear traps.")
                .addChecklist("Bring Ice Barrage / Blitz runes (Air, Water, Blood, Death)")
                .addChecklist("Keep Artio permanently frozen")
                .addChecklist("Pray Protect from Missiles for his ranged roar")
                .addDrop("Voidwaker hilt (1/912)")
                .addDrop("Claws of callisto (1/618)")
                .addDrop("Tyrannical ring (1/716)")
                .addDrop("Callisto cub (pet 1/2,800)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12791, "Rune pouch (Ice Barrage runes)", 1, "Rune", 0, true, "Mandatory freezes"),
                        new InventoryItem(139, "Prayer potion(4)", 3, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(19564, "Royal seed pod", 1, "Teleport", 0, true, "Sub-30 escape")
                ))
                .build());

        // 31. KALPHITE QUEEN (KQ)
        addBoss(BossData.builder("kalphite_queen", "Kalphite Queen", BossCategory.INTERMEDIATE, BossDifficulty.ADVANCED, CombatStyle.HYBRID)
                .combatLevel(90)
                .budget(2_500_000L)
                .minSkill(Skill.ATTACK, 75)
                .minSkill(Skill.STRENGTH, 80)
                .minSkill(Skill.RANGED, 80)
                .minSkill(Skill.PRAYER, 43)
                .recSkill(Skill.ATTACK, 85)
                .recSkill(Skill.STRENGTH, 90)
                .recSkill(Skill.RANGED, 88)
                .recSkill(Skill.PRAYER, 70)
                .prerequisite("2 Ropes to enter lair (unless Desert Elite diary completed), Keris partisan on Crush, Verac's or Fang, Blowpipe")
                .solo(true).group(true)
                .gp(45_000L, 1_400_000L)
                .weakness("Phase 1: Protects from Magic and Ranged (Use Crush Melee like Keris Partisan/Fang). Phase 2 (Wasp): Protects from Melee (Use Ranged like Blowpipe/Bowfa).")
                .prayers("Protect from Missiles / Protect from Magic", "Piety / Rigour")
                .travel("Kalphite Lair in Kharidian Desert (Fairy ring BIQ or Shantay Pass magic carpet).")
                .strategy("KQ's 100% accurate attacks pierce normal player defence! Phase 1 (Crawler): KQ uses Protect from Magic & Ranged overheads; attack with Keris Partisan or Fang on Crush. When Phase 1 dies, the Queen metamorphoses into a flying wasp (Phase 2) which uses Protect from Melee overhead; switch to Ranged (Toxic Blowpipe or Bowfa) to shoot her down. Step under her when eating to disrupt her attack cycle.")
                .mechanics("1. 100% Accuracy: KQ rolls 100% accuracy on magic/ranged attacks; you WILL take chip damage every single tick.\n2. Phase Transformation: Transforms from Crawler (Melee weak) into Flying Wasp (Ranged weak).")
                .phases("Phase 1: Beetle Crawler (weak to Crush) -> Phase 2: Flying Wasp (weak to Ranged).")
                .warnings("Do not forget 2 Ropes! One for the desert entrance, one for the queen's pit hole.")
                .tips("Step under KQ between your attack ticks ('flinching / step-under') to cut incoming damage in half.", "Desert Elite Diary unlocks the shortcut tunnel and permanent ropes.")
                .addChecklist("Bring 2 Ropes for the lair entrance")
                .addChecklist("Phase 1: Keris Partisan or Fang on CRUSH")
                .addChecklist("Phase 2: Toxic Blowpipe or Bowfa on RANGED")
                .addDrop("Dragon chainbody (1/128)")
                .addDrop("Dragon 2h sword (1/256)")
                .addDrop("Kalphite princess (pet 1/3,000)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(954, "Rope", 2, "Special Item", 50, true, "Lair access"),
                        new InventoryItem(25985, "Keris partisan", 1, "Weapon", 0, true, "Phase 1 crush weapon"),
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Melee boost"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Range boost"),
                        new InventoryItem(139, "Prayer potion(4)", 4, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(385, "Shark", 12, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());

        // 32. ABYSSAL SIRE
        addBoss(BossData.builder("abyssal_sire", "Abyssal Sire", BossCategory.SLAYER, BossDifficulty.ADVANCED, CombatStyle.HYBRID)
                .combatLevel(90)
                .budget(3_000_000L)
                .minSkill(Skill.SLAYER, 85)
                .minSkill(Skill.ATTACK, 80)
                .minSkill(Skill.STRENGTH, 85)
                .minSkill(Skill.MAGIC, 75)
                .minSkill(Skill.PRAYER, 70)
                .recSkill(Skill.ATTACK, 88)
                .recSkill(Skill.STRENGTH, 90)
                .recSkill(Skill.PRAYER, 77)
                .prerequisite("Level 85 Slayer, on an Abyssal Demon Slayer task, Smoke spells (Shadow Barrage / Smoke Blitz) to stun")
                .solo(true).group(false)
                .gp(55_000L, 1_600_000L)
                .weakness("Respiratory vents: Ranged (Blowpipe/Bowfa). Sire body: Demonbane (Arclight/Emberlight on Slash/Crush), Fang.")
                .prayers("Protect from Missiles (during spawns) / Protect from Melee", "Piety")
                .travel("Abyssal Nexus via Fairy Ring DIP (Priest in Peril required).")
                .strategy("Phase 1: Cast Shadow Barrage or Smoke spell to disorient Sire. While disoriented, shoot all 4 Respiratory Vents around the room using Toxic Blowpipe or Bowfa. Re-stun Sire if he wakes.\nPhase 2: Sire drops down to fight. Stand in melee range and attack with Arclight / Emberlight. Dodge the miasma poison pools spawned beneath your feet.\nPhase 3: Sire teleports you next to him and starts a self-destruct countdown. Immediately run 4 tiles away before the explosion! Finish him off with Melee.")
                .mechanics("1. Respiratory Vents: Must destroy all 4 vents while Sire is stunned to begin the melee phase.\n2. Miasma Pools: Acid puddles that deal rapid poison damage; step away.\n3. Teleport & Explosion: Teleports you directly beneath him and opens his chest to explode for 70+ damage; RUN AWAY.")
                .phases("Vents (Phase 1) -> Melee engagement (Phase 2) -> Explosion & frenzy (Phase 3).")
                .warnings("When Sire teleports you in Phase 3, IMMEDIATELY run away! The chest explosion is lethal.")
                .tips("Arclight or Emberlight deals huge demonbane bonus damage against Sire.", "Blood barrage scions to heal back to full health between kills.")
                .addChecklist("Ensure Abyssal Demon Slayer task active (Level 85 Slayer)")
                .addChecklist("Bring Smoke or Shadow spells to disorient Sire")
                .addChecklist("Destroy all 4 respiratory vents with Ranged")
                .addChecklist("Run away immediately when teleported in Phase 3")
                .addDrop("Unsired (1/100, placed in Font of Consumption)")
                .addDrop("Abyssal bludgeon pieces")
                .addDrop("Abyssal dagger (1/100 from Unsired)")
                .addDrop("Abyssal orphan (pet 5/128 from Unsired)")
                .addInventoryPreset("Standard", createPreset(
                        new InventoryItem(12695, "Super combat potion(4)", 1, "Combat Potion", 12000, true, "Combat boost"),
                        new InventoryItem(2444, "Ranging potion(4)", 1, "Combat Potion", 2000, true, "Range boost for vents"),
                        new InventoryItem(139, "Prayer potion(4)", 4, "Prayer Restore", 9500, true, "Prayer sustain"),
                        new InventoryItem(12791, "Rune pouch (Shadow / Smoke runes)", 1, "Rune", 0, true, "Stun spells"),
                        new InventoryItem(385, "Shark", 10, "Food", 900, true, "Food"),
                        new InventoryItem(3144, "Cooked karambwan", 4, "Food", 600, true, "Combo heal")
                ))
                .build());
    }

    private static Quest findQuestSafe(String enumName) {
        try {
            return Quest.valueOf(enumName);
        } catch (Exception e) {
            return null;
        }
    }

    private void addBoss(BossData boss) {
        bosses.put(boss.getId(), boss);
    }

    private static InventorySetup createPreset(InventoryItem... items) {
        return new InventorySetup("Standard", Arrays.asList(items));
    }

    public Map<String, BossData> getAllBosses() {
        return Collections.unmodifiableMap(bosses);
    }

    public BossData getBoss(String id) {
        return bosses.get(id);
    }
}
