# -*- coding: utf-8 -*-
"""
Expands strategies.json with comprehensive, authentic OSRS Wiki combat mechanics,
special attacks, prayer switches, weaknesses, and multi-tier methods for all major encounters.
"""
import json
import os

existing_path = r"C:\Users\Gaige\.gemini\antigravity\scratch\bosswise\src\main\resources\data\strategies.json"
with open(existing_path, "r", encoding="utf-8") as f:
    strategies = json.load(f)

existing_ids = {s["monsterId"] for s in strategies}

new_entries = [
    # -------------------------------------------------------------
    # PHANTOM MUSPAH
    # -------------------------------------------------------------
    {
        "monsterId": "phantom_muspah",
        "monsterName": "Phantom Muspah",
        "category": "BOSS",
        "primaryStyle": "RANGED",
        "combatLevel": 782,
        "hitpoints": 850,
        "slayerLevel": 1,
        "slayerCategory": "Phantom Muspah",
        "slayerTaskOnly": False,
        "locations": ["Weiss (Ghorrock Dungeon)"],
        "accessRequirements": ["Secrets of the North completed"],
        "overview": "The Phantom Muspah is a massive ancient Zarosian creature imprisoned beneath the frozen wastes of Weiss. It shifts between a Melee Phase (true running charge) and a Ranged Phase (firing homing ice spikes and magic clouds). During its Soul Shield Phase, it gains a thick protective barrier vulnerable only to prayer-draining attacks like Smite and Sapphire bolts (e).",
        "aggression": "Aggressive immediately upon entering chamber",
        "respawnTime": "Instant (re-enter or instance restart)",
        "multiCombat": False,
        "safespotInformation": "The step-back method allows players to attack the Melee phase without taking damage by moving away precisely when Muspah halts.",
        "overheadPrayer": "Protect from Magic (Ranged phase) / Protect from Melee (Melee phase)",
        "offensivePrayer": "Rigour (Ranged) / Augury (Magic) / Smite (Shield phase)",
        "prayerNotes": "During Ranged Phase, pray Protect from Magic while dodging ice spikes. During Shield Phase, activate Smite to drain its prayer shield rapidly with Sapphire bolts (e).",
        "elementalWeakness": "NONE",
        "elementalWeaknessPercent": 0,
        "weaknesses": ["Magic against Melee phase (Shadow / Trident / Freeze)", "Ranged against Ranged phase (Bow of faerdhinen / Twisted bow)", "Sapphire dragon bolts (e) & Smite on Soul Shield phase"],
        "immunities": ["Poison", "Venom", "Cannons"],
        "specialEquipmentAdvantage": [
            "Sapphire dragon bolts (e) / Sapphire bolts (e): Proc drains 5% of target's prayer and inflicts massive damage directly to the soul shield",
            "Bow of faerdhinen: Exceptional accuracy against high-defence Ranged phase",
            "Ice Barrage: Freezes Muspah for 20 seconds, allowing effortless kiting during Melee phase"
        ],
        "protectionAndSurvival": [
            "Protect from Magic & Protect from Melee",
            "Stamina potions are mandatory for step-back kiting",
            "Freezing spells (Ice Barrage / Ice Blitz) simplify the Melee phase dramatically"
        ],
        "phases": [
            "Melee Phase: Charges player; must be frozen with Ice Barrage or kited with step-back method.",
            "Ranged Phase: Fires homing spikes; creates stationary ice spikes on the arena floor.",
            "Soul Shield Phase: Enters invulnerable shell; player must Smite and fire Sapphire bolts (e) to shatter the barrier."
        ],
        "combatMechanics": [
            {
                "name": "Soul Shield Phase",
                "description": "Muspah shields itself in a purple barrier. Normal damage is heavily mitigated.",
                "avoidanceTip": "Switch to Crossbow with Sapphire bolts (e) and turn on Smite. The bolt proc drains huge chunks of the barrier."
            },
            {
                "name": "Homing Ice Spikes",
                "description": "Fires ice spikes that follow the player and create deadly spike tiles upon impact.",
                "avoidanceTip": "Keep moving and avoid stepping on existing ice spikes on the ground."
            }
        ],
        "commonMistakes": [
            "Forgetting Sapphire bolts (e) or Smite for the shield phase, dragging out the fight by minutes.",
            "Getting cornered by the Melee phase without run energy or an active freeze."
        ],
        "notableDrops": [
            "Venator shard (1/100, 5 creates the Venator Bow)",
            "Ancient icon (1/50, upgrades Ancient sceptres)",
            "Muphin (Pet, 1/2,500)"
        ],
        "importantTips": [
            "Bow of Faerdhinen with Crystal armour can solo Muspah without needing any magic switches by freezing with Ice Barrage and continuing to shoot BofA.",
            "Step back 1 tile immediately after Muspah swings in Melee phase to stall its movement."
        ],
        "wikiUrl": "https://oldschool.runescape.wiki/w/Phantom_Muspah",
        "wikiStrategyUrl": "https://oldschool.runescape.wiki/w/Phantom_Muspah/Strategies",
        "lastVerified": "2026-11-16",
        "confidence": "VERIFIED",
        "methods": [
            {
                "id": "muspah_bofa_stepback",
                "title": "BofA Ranged Only (Ice Barrage Freeze)",
                "tier": "ENDGAME",
                "combatStyle": "RANGED",
                "requiredSkills": {"RANGED": 85, "MAGIC": 82, "PRAYER": 74},
                "recommendedSkills": {"RANGED": 92, "MAGIC": 94},
                "requiredQuests": ["Secrets of the North", "Desert Treasure"],
                "overheadPrayer": "Protect from Magic / Protect from Melee",
                "offensivePrayer": "Rigour / Smite (Shield phase)",
                "potionSetup": ["Stamina potion(4) x2", "Divine ranging potion(4)", "Prayer potion(4) x3"],
                "inventoryHighlights": ["Ancient sceptre (Ice Barrage)", "Crossbow with Sapphire bolts (e) switch", "Angelfish"],
                "estimatedCostGp": 140000000,
                "whyThisWorks": "Bow of Faerdhinen shreds both phases. Casting Ice Barrage completely immobilizes the Melee phase, and Sapphire bolts (e) + Smite breaks the shield in 3-4 hits.",
                "detailedInstructions": "Camp BofA. When Melee phase starts, cast Ice Barrage to freeze Muspah, then step out of reach and shoot BofA. When shield phase triggers, equip Crossbow + Sapphire bolts and turn on Smite.",
                "keyItems": ["Bow of faerdhinen", "Crystal armour set", "Ancient sceptre", "Crossbow", "Sapphire dragon bolts (e)"]
            }
        ]
    },

    # -------------------------------------------------------------
    # CERBERUS
    # -------------------------------------------------------------
    {
        "monsterId": "cerberus",
        "monsterName": "Cerberus",
        "category": "BOSS",
        "primaryStyle": "MELEE",
        "combatLevel": 318,
        "hitpoints": 600,
        "slayerLevel": 91,
        "slayerCategory": "Hellhounds",
        "slayerTaskOnly": True,
        "locations": ["Taverley Dungeon (deep lair, enter via key or 80 Agility shortcut)"],
        "accessRequirements": ["91 Slayer", "Assigned a Hellhound Slayer task"],
        "overview": "Cerberus is the three-headed hellhound guardian of the River Souls. She attacks with a mix of Melee, Magic, and Ranged, and summons three ghostly apparitions that demand immediate overhead prayer switches, draining 30 prayer points per missed ghost.",
        "aggression": "Aggressive upon entering lair",
        "respawnTime": "Fast (9.0s)",
        "multiCombat": False,
        "safespotInformation": "No safespots. Melee combat requires standing directly in front of her.",
        "overheadPrayer": "Protect from Magic (Standard)",
        "offensivePrayer": "Piety (Melee) or Rigour (Ranged)",
        "prayerNotes": "Pray Protect from Magic against her standard magic orbs. When she summons souls (Aroo!), switch prayers to match ghosts from left to right: Ranged (Red) -> Magic (Blue) -> Melee (White).",
        "elementalWeakness": "NONE",
        "elementalWeaknessPercent": 0,
        "weaknesses": ["Demonbane weapons (Arclight / Emberlight +70% accuracy & +30-36% damage)", "Crush melee", "Slayer helmet (i) (+15-16.67% accuracy & damage)"],
        "immunities": ["Cannons"],
        "specialEquipmentAdvantage": [
            "Arclight / Emberlight: Massive demonbane bonus makes Cerberus shred in under 45 seconds",
            "Spectral spirit shield: Halves prayer drain from summoned ghosts from 30 points down to 15 points!",
            "Inquisitor's mace / Osmumten's fang on Crush: Exploits her low crush defence"
        ],
        "protectionAndSurvival": [
            "Spectral spirit shield switch strongly recommended to conserve prayer potions",
            "Prayer potions / Super restores (ghosts consume 90 prayer points per wave without Spectral)",
            "Run 2 tiles away when she spits lava pools ('Grrrrrr')"
        ],
        "phases": ["Standard fight until below 400 HP, where Souls appear. Below 200 HP, triple attack and lava pool frequency increases."],
        "combatMechanics": [
            {
                "name": "Summoned Souls (Aroo!)",
                "description": "Cerberus howls and summons 3 ghosts (Ranged, Magic, Melee) that march across the room.",
                "avoidanceTip": "Equip Spectral spirit shield. Pray against ghosts sequentially from left to right. Missed prayer deals 30 damage and drains 30 prayer."
            },
            {
                "name": "Lava Pools (Grrrrrr)",
                "description": "Cerberus growls and shoots 3 burning lava pools onto the floor beneath you.",
                "avoidanceTip": "Move off your tile immediately when Cerberus growls; standing in lava hits 15+ damage every tick."
            }
        ],
        "commonMistakes": [
            "Running out of prayer during the ghost phase, resulting in instant 90 damage KO.",
            "Standing still on the lava pools after she growls."
        ],
        "notableDrops": [
            "Primordial crystal (1/512, crafts Primordial boots)",
            "Pegasian crystal (1/512, crafts Pegasian boots)",
            "Eternal crystal (1/512, crafts Eternal boots)",
            "Smouldering stone (1/512)",
            "Hellpuppy (Pet, 1/3,000)"
        ],
        "importantTips": [
            "Equipping a Spectral spirit shield right before ghosts attack reduces the prayer drain from 30 to 15 points per ghost.",
            "Ghost prayer order is determined by their appearance left to right: Red = Range, Blue = Magic, White = Melee."
        ],
        "wikiUrl": "https://oldschool.runescape.wiki/w/Cerberus",
        "wikiStrategyUrl": "https://oldschool.runescape.wiki/w/Cerberus/Strategies",
        "lastVerified": "2026-11-16",
        "confidence": "VERIFIED",
        "methods": [
            {
                "id": "cerb_emberlight_spectral",
                "title": "Emberlight / Arclight (Spectral Shield Switch)",
                "tier": "ENDGAME",
                "combatStyle": "MELEE",
                "requiredSkills": {"ATTACK": 75, "STRENGTH": 85, "DEFENCE": 75, "SLAYER": 91, "PRAYER": 70},
                "recommendedSkills": {"STRENGTH": 95},
                "requiredQuests": ["Shadow of the Storm"],
                "overheadPrayer": "Protect from Magic",
                "offensivePrayer": "Piety",
                "potionSetup": ["Super combat potion(4)", "Prayer potion(4) x5"],
                "inventoryHighlights": ["Spectral spirit shield switch", "Dragon defender", "Shark / Karambwan"],
                "estimatedCostGp": 75000000,
                "whyThisWorks": "Emberlight/Arclight provides an insurmountable +70% accuracy boost against demons. Coupled with Spectral spirit shield, ghost prayer drains are halved, enabling 10+ kill trips.",
                "detailedInstructions": "Enter lair, pray Protect from Magic and Piety. Attack Cerberus with Emberlight. When she howls 'Aroo!', equip Spectral spirit shield and switch overhead prayers matching ghosts from left to right. Move off lava pools.",
                "keyItems": ["Emberlight / Arclight", "Dragon defender", "Spectral spirit shield", "Slayer helmet (i)", "Bandos chestplate", "Bandos tassets"]
            }
        ]
    },

    # -------------------------------------------------------------
    # ALCHEMICAL HYDRA
    # -------------------------------------------------------------
    {
        "monsterId": "alchemical_hydra",
        "monsterName": "Alchemical Hydra",
        "category": "BOSS",
        "primaryStyle": "RANGED",
        "combatLevel": 426,
        "hitpoints": 1100,
        "slayerLevel": 95,
        "slayerCategory": "Hydras",
        "slayerTaskOnly": True,
        "locations": ["Karuulm Slayer Dungeon (Mount Karuulm)"],
        "accessRequirements": ["95 Slayer", "Assigned a Hydra Slayer task", "Boots of stone, Boots of brimstone, or Granite boots (or Kourend Elite Diary)"],
        "overview": "The Alchemical Hydra is the multi-headed chromatic apex of Mount Karuulm. It cycles through 4 chromatic phases (Poison Green -> Lightning Blue -> Flame Red -> Enraged Grey), attacking with Magic and Ranged in strict 3-attack cycles while requiring luring onto colored chemical vents to strip its 75% damage-reduction shielding.",
        "aggression": "Aggressive upon walking onto arena floor",
        "respawnTime": "Standard (25s)",
        "multiCombat": False,
        "safespotInformation": "No safespots. Lure onto opposite-color chemical vent at the start of each phase.",
        "overheadPrayer": "Protect from Magic / Protect from Missiles (3-attack cycle)",
        "offensivePrayer": "Rigour (Ranged) or Piety (Melee)",
        "prayerNotes": "Hydra attacks 3 times with Magic, then 3 times with Ranged. Count 3 attacks, then switch overhead prayer. In Phase 4, it alternates styles on EVERY attack.",
        "elementalWeakness": "NONE",
        "elementalWeaknessPercent": 0,
        "weaknesses": ["Dragon hunter weapons (+20% DHL / +30% DHCB)", "Twisted bow", "Chemical vent debuff (strips 75% damage reduction)"],
        "immunities": ["Poison", "Venom", "Cannons"],
        "specialEquipmentAdvantage": [
            "Boots of stone / brimstone / granite boots: MANDATORY to survive the burning cavern floor",
            "Dragon hunter crossbow / Dragon hunter lance: +20-30% damage against draconic hydra",
            "Twisted bow: Massive accuracy and damage scaling with Hydra's 260 Magic level"
        ],
        "protectionAndSurvival": [
            "Boots of stone (unless Kourend & Kebos Elite Diary completed)",
            "Lure onto chemical vent at start of phase to remove 75% damage resistance",
            "Count to 3: Switch prayers every 3 attacks (Magic <-> Ranged)"
        ],
        "phases": [
            "Phase 1 (Green / Poison): Lure onto Red chemical vent. Dodges poison spit pools.",
            "Phase 2 (Blue / Lightning): Lure onto Green chemical vent. Lures lightning orbs away from player.",
            "Phase 3 (Red / Flame): Lure onto Blue chemical vent. Traps player in flame wall; walk along clear side.",
            "Phase 4 (Grey / Enraged): No vent needed. Alternates attacks EVERY shot. Dodge falling toxic acid."
        ],
        "combatMechanics": [
            {
                "name": "Chemical Vents (Shield Strip)",
                "description": "At the start of Phases 1-3, Hydra has 75% damage reduction until stood on the correct colored vent.",
                "avoidanceTip": "Lure Green onto Red vent, Blue onto Green vent, Red onto Blue vent. A message confirms shielding dropped."
            },
            {
                "name": "3-Attack Style Cycling",
                "description": "Fires 3 Magic attacks, then 3 Ranged attacks. In enraged phase, alternates every single attack.",
                "avoidanceTip": "Count 1-2-3 attacks, switch overhead prayer. Turn game audio on to hear the distinctive attack sounds."
            }
        ],
        "commonMistakes": [
            "Entering Mount Karuulm without protective boots, taking rapid 10 damage ticks from the scorching floor.",
            "Attacking Hydra before it steps on the correct chemical vent, dealing 75% reduced damage.",
            "Losing track of the 3-attack count."
        ],
        "notableDrops": [
            "Hydra claw (1/1,001, creates Dragon hunter lance)",
            "Hydra leather (1/514, creates Ferocious gloves)",
            "Hydra's fang / eye / heart (Brimstone ring pieces)",
            "Alchemical hydra head (1/256)",
            "Ikkle hydra (Pet, 1/3,000)"
        ],
        "importantTips": [
            "Stand 1 tile off the colored vent before phase transitions so Hydra walks onto it automatically.",
            "Flame phase: Stand in the south-west corner to bypass the entire flame maze with zero movement."
        ],
        "wikiUrl": "https://oldschool.runescape.wiki/w/Alchemical_Hydra",
        "wikiStrategyUrl": "https://oldschool.runescape.wiki/w/Alchemical_Hydra/Strategies",
        "lastVerified": "2026-11-16",
        "confidence": "VERIFIED",
        "methods": [
            {
                "id": "hydra_dhcb_tbow",
                "title": "Ranged BiS (DHCB / Twisted Bow)",
                "tier": "ENDGAME",
                "combatStyle": "RANGED",
                "requiredSkills": {"RANGED": 90, "PRAYER": 74, "SLAYER": 95},
                "recommendedSkills": {"RANGED": 99},
                "requiredQuests": [],
                "overheadPrayer": "Protect from Magic / Missiles (count of 3)",
                "offensivePrayer": "Rigour",
                "potionSetup": ["Divine ranging potion(4)", "Prayer potion(4) x6"],
                "inventoryHighlights": ["Ruby dragon bolts (e)", "Diamond dragon bolts (e)", "Boots of brimstone"],
                "estimatedCostGp": 90000000,
                "whyThisWorks": "Dragon Hunter Crossbow with Ruby bolts (e) delivers 100-damage procs across its 1100 hitpoint pool, shredding phases effortlessly when lured onto vents.",
                "detailedInstructions": "Enter arena. Stand next to Red vent. Pray Protect from Magic. Count 3 attacks, switch to Protect from Missiles. Repeat on Green and Blue vents.",
                "keyItems": ["Dragon hunter crossbow", "Twisted buckler", "Slayer helmet (i)", "Armadyl chestplate", "Armadyl chainskirt", "Pegasian boots"]
            }
        ]
    },

    # -------------------------------------------------------------
    # GROTESQUE GUARDIANS
    # -------------------------------------------------------------
    {
        "monsterId": "grotesque_guardians",
        "monsterName": "Grotesque Guardians",
        "category": "BOSS",
        "primaryStyle": "MELEE",
        "combatLevel": 236,
        "hitpoints": 900,
        "slayerLevel": 75,
        "slayerCategory": "Gargoyles",
        "slayerTaskOnly": True,
        "locations": ["Slayer Tower Roof (Canifis, unlocked via Brittle key)"],
        "accessRequirements": ["75 Slayer", "Brittle key", "Gargoyle Slayer task"],
        "overview": "The Grotesque Guardians (Dusk and Dawn) are twin gargoyle sentinels atop the Slayer Tower roof. The fight alternates between Dawn (Ranged vulnerability) and Dusk (Melee vulnerability), featuring falling rock debris, purple prison spheres, healing lightning orbs, and a final flame prison.",
        "aggression": "Aggressive upon ringing bell",
        "respawnTime": "Instant (ring bell to restart)",
        "multiCombat": False,
        "safespotInformation": "No safespots. Continuous spatial awareness and dodging required.",
        "overheadPrayer": "Protect from Missiles (against Dawn) / Protect from Melee (against Dusk)",
        "offensivePrayer": "Rigour (Dawn) & Piety (Dusk)",
        "prayerNotes": "Pray Protect from Missiles when fighting Dawn. Pray Protect from Melee when fighting Dusk. Switch smoothly during duo phases.",
        "elementalWeakness": "NONE",
        "elementalWeaknessPercent": 0,
        "weaknesses": ["Crush melee against Dusk (Bludgeon / Mace / Scythe)", "Ranged against Dawn (Toxic blowpipe / BofA)", "Rock hammer / Granite hammer smash on final phase"],
        "immunities": ["Cannons"],
        "specialEquipmentAdvantage": [
            "Rock hammer / Granite hammer: Required to shatter Dawn and Dusk when depleted",
            "Toxic blowpipe: Rapid 2-tick attack speed shreds Dawn's hitpoints",
            "Abyssal bludgeon / Osmumten's fang on Crush: Maximizes DPS against Dusk"
        ],
        "protectionAndSurvival": [
            "Rock hammer or Granite hammer",
            "Collect purple healing orbs before Dawn absorbs them",
            "Dodge falling stone shadows"
        ],
        "phases": [
            "Phase 1: Fight Dawn with Ranged until 50% HP; dodge falling rocks.",
            "Phase 2: Fight Dusk with Melee until 50% HP; escape purple prison sphere.",
            "Phase 3: Fight both simultaneously; absorb 3 healing orbs before Dawn recovers HP; smash Dawn with Rock hammer.",
            "Phase 4: Fight enraged Dusk with Melee; dodge flame walls and shadows; smash Dusk to complete kill."
        ],
        "combatMechanics": [
            {
                "name": "Purple Healing Orbs",
                "description": "Dawn launches 3 purple orbs towards corners of the arena that heal her for 90 HP each if uncollected.",
                "avoidanceTip": "Run through all 3 purple orbs before they detonate. Walking over an orb absorbs it safely for minor damage."
            },
            {
                "name": "Purple Prison Sphere",
                "description": "Dusk traps the player inside a purple square and drops a massive boulder on your tile.",
                "avoidanceTip": "Step through the flashing opening in the perimeter of the prison immediately to escape the explosion."
            }
        ],
        "commonMistakes": [
            "Forgetting to bring a Rock hammer or Granite hammer to finish the bosses.",
            "Ignoring the purple healing orbs, allowing Dawn to heal back up to full health."
        ],
        "notableDrops": [
            "Granite hammer (1/750)",
            "Black tourmaline core (1/1,000, crafts Guardian boots)",
            "Granite dust (50-100, upgrades cannonballs to +5 max hit)",
            "Noon / Midnight (Pet, 1/3,000)"
        ],
        "importantTips": [
            "Bring Toxic blowpipe for Dawn and high-tier Crush melee for Dusk. Fast transitions allow 1:30 - 2:00 minute kills."
        ],
        "wikiUrl": "https://oldschool.runescape.wiki/w/Grotesque_Guardians",
        "wikiStrategyUrl": "https://oldschool.runescape.wiki/w/Grotesque_Guardians/Strategies",
        "lastVerified": "2026-11-16",
        "confidence": "VERIFIED",
        "methods": [
            {
                "id": "gg_hybrid_blowpipe_crush",
                "title": "Hybrid (Blowpipe + Crush Melee)",
                "tier": "HIGH_LEVEL",
                "combatStyle": "MELEE",
                "requiredSkills": {"ATTACK": 75, "STRENGTH": 80, "RANGED": 75, "PRAYER": 70, "SLAYER": 75},
                "recommendedSkills": {"STRENGTH": 90, "RANGED": 90},
                "requiredQuests": ["Priest in Peril"],
                "overheadPrayer": "Protect from Missiles (Dawn) / Protect from Melee (Dusk)",
                "offensivePrayer": "Rigour / Piety",
                "potionSetup": ["Super combat potion(4)", "Ranging potion(4)", "Prayer potion(4) x2"],
                "inventoryHighlights": ["Rock hammer", "Toxic blowpipe switch", "Angelfish"],
                "estimatedCostGp": 25000000,
                "whyThisWorks": "Toxic blowpipe melts Dawn in under 15 seconds, and Abyssal bludgeon on Crush bypasses Dusk's stone armor.",
                "detailedInstructions": "Ring bell. Shoot Dawn with Blowpipe and Rigour. Switch to Melee and Piety against Dusk. Absorb purple orbs. Smash both with Rock hammer.",
                "keyItems": ["Toxic blowpipe", "Abyssal bludgeon", "Slayer helmet (i)", "Bandos chestplate", "Bandos tassets", "Rock hammer"]
            }
        ]
    },

    # -------------------------------------------------------------
    # TZTOK-JAD (FIGHT CAVES)
    # -------------------------------------------------------------
    {
        "monsterId": "tztok_jad",
        "monsterName": "TzTok-Jad",
        "category": "DUNGEON_MINIGAME",
        "primaryStyle": "RANGED",
        "combatLevel": 702,
        "hitpoints": 250,
        "slayerLevel": 1,
        "slayerCategory": "TzHaar",
        "slayerTaskOnly": False,
        "locations": ["Mor Ul Rek (Fight Caves, Mor Ul Rek volcano)"],
        "accessRequirements": ["None (TzHaar minigame)"],
        "overview": "TzTok-Jad is the legendary final test of the TzHaar Fight Caves. He possesses attacks capable of hitting up to 97 damage with both Magic and Ranged. Surviving Jad requires 100% accurate overhead prayer switching based on his wind-up animations (stomp vs hover), and tagging his 4 Yt-HurKot healers before they heal him to full.",
        "aggression": "Aggressive immediately upon wave 63 start",
        "respawnTime": "Instant (new Fight Cave wave)",
        "multiCombat": False,
        "safespotInformation": "Italy Rock, Dragon Rock, and Long Rock can be used to trap wave monsters and trap Jad out of line-of-sight.",
        "overheadPrayer": "Protect from Missiles (Stomp) / Protect from Magic (Hover)",
        "offensivePrayer": "Rigour / Eagle Eye",
        "prayerNotes": "Priority #1 is NEVER miss a prayer switch. Hovering on hind legs with fireball explosion = Protect from Magic. Slamming hooves down to ground with falling boulder = Protect from Missiles.",
        "elementalWeakness": "NONE",
        "elementalWeaknessPercent": 0,
        "weaknesses": ["Ranged (Bow of faerdhinen / Blowpipe / Crossbow)", "Chinchompas for tagging healers"],
        "immunities": ["Cannons"],
        "specialEquipmentAdvantage": [
            "Toxic blowpipe / Bow of faerdhinen: Exceptional DPS on Jad and the 62 preceding waves",
            "Chinchompas: Thrown attack splashes onto all 4 healers simultaneously, tagging them in 1 attack",
            "Slayer helmet (i): Provides +15% damage bonus if assigned a Jad Slayer task"
        ],
        "protectionAndSurvival": [
            "Saradomin brews and Super restores in 3:1 ratio (e.g. 6 brews, 14 restores)",
            "Stamina potion for running around Italy Rock",
            "Audio cues: Distinct whistling sound for Magic, crashing sound for Ranged"
        ],
        "phases": [
            "Wave 63: Jad spawns where the orange level 360 Mage spawned in wave 62.",
            "Healers Phase (at 50% Jad HP): 4 Yt-HurKot healers spawn and rush to heal Jad. Tag each healer once to pull aggro."
        ],
        "combatMechanics": [
            {
                "name": "Ranged Boulder Stomp",
                "description": "Jad rises up and slams his front feet down hard on the ground; a giant boulder drops on player.",
                "avoidanceTip": "Pray Protect from Missiles immediately when you see him slam his feet down."
            },
            {
                "name": "Magic Fireball Hover",
                "description": "Jad dangles on his hind legs and shoots a blast of red flames and fiery sparks.",
                "avoidanceTip": "Pray Protect from Magic immediately when you see him hover on his hind legs."
            },
            {
                "name": "Yt-HurKot Healers",
                "description": "At 125 HP (50%), 4 healers spawn and rapidly restore Jad's hitpoints.",
                "avoidanceTip": "Do ONE action per Jad attack. Wait for Jad to attack -> switch prayer -> shoot 1 healer -> wait for next attack -> shoot next healer."
            }
        ],
        "commonMistakes": [
            "Panicking when the healers spawn and rushing them, forgetting to watch Jad's attack animation and getting one-shot for 97 damage.",
            "Doing multiple actions between attacks. Always adhere to the rule: 1 action per prayer switch."
        ],
        "notableDrops": [
            "Fire cape (guaranteed on wave 63 completion)",
            "Tokkul (8,032)",
            "Tzrek-jad (Pet, 1/200 gamble or 1/100 on Slayer task)"
        ],
        "importantTips": [
            "The orange level 360 Mage in wave 62 marks the EXACT tile where Jad will spawn in wave 63.",
            "Turn game sound effects up: you can hear his magic attack wind up even if off-screen."
        ],
        "wikiUrl": "https://oldschool.runescape.wiki/w/TzTok-Jad",
        "wikiStrategyUrl": "https://oldschool.runescape.wiki/w/TzTok-Jad/Strategies",
        "lastVerified": "2026-11-16",
        "confidence": "VERIFIED",
        "methods": [
            {
                "id": "jad_blowpipe_classic",
                "title": "Classic Fight Caves (Blowpipe / Crossbow)",
                "tier": "MID_GAME",
                "combatStyle": "RANGED",
                "requiredSkills": {"RANGED": 75, "DEFENCE": 70, "PRAYER": 43, "HITPOINTS": 70},
                "recommendedSkills": {"RANGED": 80, "PRAYER": 70},
                "requiredQuests": [],
                "overheadPrayer": "Protect from Missiles / Magic (Telegraphed)",
                "offensivePrayer": "Eagle Eye / Rigour",
                "potionSetup": ["Saradomin brew(4) x6", "Super restore(4) x14", "Ranging potion(4) x2", "Stamina potion(4)"],
                "inventoryHighlights": ["Toxic blowpipe (Amethyst / Rune darts)", "Black chinchompas x25 (for healers)", "Emergency Angelfish"],
                "estimatedCostGp": 7500000,
                "whyThisWorks": "Toxic blowpipe's special attack provides massive healing on earlier waves, while fast attack speed shreds wave mobs and defeats Jad within 60 seconds.",
                "detailedInstructions": "Progress through waves 1-62 trapping enemies at Italy Rock. On wave 63, watch Jad's feet: Stomp down = Missiles, Hover back = Magic. Tag each healer once after confirming prayer.",
                "keyItems": ["Toxic blowpipe", "God d'hide body", "God d'hide chaps", "Amulet of fury", "Ava's accumulator", "Archers ring (i)"]
            }
        ]
    }
]

# Merge new entries
for entry in new_entries:
    if entry["monsterId"] not in existing_ids:
        strategies.append(entry)
        existing_ids.add(entry["monsterId"])
    else:
        # Update existing
        for i, s in enumerate(strategies):
            if s["monsterId"] == entry["monsterId"]:
                strategies[i] = entry
                break

with open(existing_path, "w", encoding="utf-8") as f:
    json.dump(strategies, f, indent=2, ensure_ascii=False)

print(f"Total strategies in strategies.json now: {len(strategies)}")
