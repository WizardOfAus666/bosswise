# BossWise – The Ultimate OSRS Bossing Assistant

[![RuneLite Plugin](https://img.shields.io/badge/RuneLite-Plugin-orange.svg)](https://runelite.net)
[![License](https://img.shields.io/badge/License-BSD_2--Clause-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-brightgreen.svg)](https://adoptium.net)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)]()

**BossWise** is an intelligent, professional-quality Old School RuneScape (OSRS) RuneLite plugin designed to analyze your actual account progression, identify eligible boss encounters, optimize equipment loadouts for any custom GP budget, advise on high-impact gear upgrades, build customized supply inventories, and guide you through fight mechanics.

Engineered strictly for official **RuneLite Plugin Hub** submission, BossWise operates purely as an informational companion: it never automates gameplay, never clicks, never manipulates packets, and never interferes with the game client.

---

## Table of Contents

- [Core Features](#core-features)
  - [1. Real-Time Account Scanner](#1-real-time-account-scanner)
  - [2. Quest Progression Engine](#2-quest-progression-engine)
  - [3. Comprehensive Boss Database (30+ Encounters)](#3-comprehensive-boss-database-30-encounters)
  - [4. Intelligent Boss Recommendation Engine](#4-intelligent-boss-recommendation-engine)
  - [5. Gear Budget Optimizer](#5-gear-budget-optimizer)
  - [6. Gear Upgrade Advisor](#6-gear-upgrade-advisor)
  - [7. Customized Inventory & Supply Builder](#7-customized-inventory--supply-builder)
  - [8. In-Depth Boss Strategy Guides](#8-in-depth-boss-strategy-guides)
  - [9. Account Progression Roadmap](#9-account-progression-roadmap)
- [Plugin Hub Compliance & Fair Play](#plugin-hub-compliance--fair-play)
- [Building & Testing](#building--testing)
- [License](#license)

---

## Core Features

### 1. Real-Time Account Scanner
- Automatically reads the logged-in player's skills through official RuneLite `Client` APIs:
  - **Combat Stats**: Attack, Strength, Defence, Ranged, Magic, Prayer, Hitpoints
  - **Utility Skills**: Slayer, Agility, Herblore, Construction, Crafting, Hunter, Mining, etc.
- Accurately distinguishes **real base levels** from **boosted levels**. Permanent requirements enforce base levels while boostable requirements (e.g. Summer Pie for Agility, Botanist for Herblore) clearly display boost guidance.
- Calculates true combat level via `Experience.getCombatLevel` and reads overall total level.
- Reactive auto-refresh when leveling up in-game via `@Subscribe onStatChanged`.

### 2. Quest Progression Engine
- Leverages `net.runelite.api.Quest.getState(client)` to accurately verify completed and in-progress quests.
- Deep dependency database mapping prerequisite quests, minimum skill levels, and unlocked content:
  - *Dragon Slayer II* (Vorkath, Myths' Guild, Ava's Assembler)
  - *Song of the Elves* (The Gauntlet, Corrupted Gauntlet, Prifddinas, Bowfa)
  - *Desert Treasure II* (Duke Sucellus, Vardorvis, The Whisperer, The Leviathan, Virtus robes)
  - *Beneath Cursed Sands* (Tombs of Amascut, Keris Partisan)
  - *Recipe for Disaster* (Barrows Gloves)
  - *Secrets of the North* (Phantom Muspah, Venator Bow)
  - *Priest in Peril, Regicide, Monkey Madness I & II, A Kingdom Divided*, and more.
- Unidentified or inaccessible states gracefully default to `UNKNOWN` rather than making unsafe assumptions.

### 3. Comprehensive Boss Database (30+ Encounters)
Accurately represents the OSRS bossing landscape across all tiers:
- **Beginner**: Scurrius, Giant Mole, Barrows Brothers, Obor, Bryophyta, King Black Dragon
- **Intermediate**: Sarachnis, Dagannoth Kings (Rex, Prime, Supreme), Kalphite Queen
- **Advanced**: Zulrah, Vorkath, Phantom Muspah, Demonic Gorillas, Grotesque Guardians, Abyssal Sire, Kraken
- **Endgame**: Alchemical Hydra, Cerberus, Commander Zilyana, General Graardor, K'ril Tsutsaroth, Kree'arra, Corporeal Beast, Nex
- **Desert Treasure II**: Duke Sucellus, Vardorvis, The Whisperer, The Leviathan
- **Raids**: Chambers of Xeric (CoX), Theatre of Blood (ToB), Tombs of Amascut (ToA)
- **Wilderness**: Calvar'ion, Spindel, Artio, Chaos Elemental

Every boss entry includes verified weaknesses, overhead prayers, travel directions, phase changes, mechanic warnings, beginner tips, advanced tips, pre-fight checklists, and estimated GP/kill rates.

### 4. Intelligent Boss Recommendation Engine
Categorizes every boss dynamically based on your account state:
- **GREEN (READY)**: Account meets all mandatory quests, entry prerequisites, and hard skill levels.
- **YELLOW (ALMOST READY)**: Missing $\le 5$ skill levels or a single prerequisite quest. Shows exact missing requirements.
- **RED (LOCKED)**: Missing major quest lines or high Slayer milestones.
- **Scoring (0–100%)**: Calculates account suitability factoring player combat level, recommended combat stats ratio, GP potential, and difficulty.
- **Filters & Sorting**:
  - Filter by Status (Ready, Almost Ready, Locked), Category, Difficulty, or Solo/Group.
  - Sort by Recommended, Easiest First, Highest GP/hr, Lowest Gear Cost, or Closest to Unlocking.

### 5. Gear Budget Optimizer
- Accepts **any custom GP budget** alongside quick preset pills (500K, 1M, 2.5M, 5M, 25M).
- Generates 3 discrete loadouts:
  1. **Cheapest Effective Setup**: Minimum viable budget gear that safely clears the encounter.
  2. **Best Value Setup**: Sweet spot balancing DPS and defence per GP spent (caps at tier 7 to avoid diminishing returns).
  3. **Maximum Budget Setup**: Highest-performing gear loadout that strictly fits inside your budget without ever exceeding it.
- Slot-by-slot allocation: Weapon, Shield, Helmet, Body, Legs, Gloves, Boots, Amulet, Ring, Cape, Ammo, and Spec Weapon.
- Strict account validation: checks Attack, Strength, Defence, Ranged, Magic, and Quest unlocks so you are never recommended an item you cannot equip.
- Respects boss mechanics: enforces Dragonfire shields at Vorkath/KBD, Spear/Fang at Corporeal Beast, and Crush at Sarachnis/Calvar'ion.
- Accounts for untradeable staples (Fire Cape, Fighter Torso, Barrows Gloves, Void Knight, Ava's Assembler, Salve (ei)) at 0 GP cost.

### 6. Gear Upgrade Advisor
- Identifies the highest-impact upgrades for your current loadout slot by slot.
- Computes price difference against live GE market prices.
- Displays stat deltas (e.g. `+4 Strength`, `+12 Slash Attack`) and functional impact (e.g. *"Osmumten's Fang rerolls missed accuracy, massive DPS jump vs high-defence bosses"*).
- Flags whether the upgrade fits within your remaining budget.

### 7. Customized Inventory & Supply Builder
- Tailored inventory setups per boss across 4 tiers: **Beginner**, **Budget**, **Standard**, and **Advanced**.
- Itemizes food, combat potions, prayer restores, utility potions, runes, ammo, and boss-specific protection items (e.g. Anti-dragon shield, Rock hammer, Boots of stone).
- Calculates total supply cost per trip using live GE prices.

### 8. In-Depth Boss Strategy Guides
- Step-by-step strategy for every encounter.
- Dedicated sections: Location & Travel, Recommended Prayers, Critical Warnings, Fight Strategy, Phase Mechanics, Pro Tips, Interactive Pre-Fight Checklist, and Notable Drops.

### 9. Account Progression Roadmap
- Synthesizes your account gap into actionable milestones:
  - Skill training goals with current and target levels
  - Quest unlock requirements for high-priority bosses
  - Combat milestones (e.g. Level 70 Prayer for Piety, Level 75 Ranged for Toxic Blowpipe)
- Features visual progress bars and persistent interactive completion tracking.

---

## Plugin Hub Compliance & Fair Play

BossWise is built strictly in accordance with [RuneLite's Plugin Hub Rules](https://github.com/runelite/plugin-hub):
- **No Automated Gameplay**: Does not perform automated clicks, automated prayer flicking, automated gear switching, or automated movement.
- **No Packet Manipulation**: Interacts solely through official, read-only RuneLite API getters (`Client.getRealSkillLevel`, `Quest.getState`, `ItemManager.getItemPrice`).
- **No Unfair Overlays**: Contains zero prohibited screen overlays, tick counters, or predictive combat boxes.
- **Thread Safety**: All networking, caching, and database lookups run asynchronously off the client thread; all UI mutations run safely on `SwingUtilities.invokeLater`.
- **Zero Privacy Invasions**: No tracking, credential collection, or external telemetry.

---

## Building & Testing

Requires JDK 11 or 17:

```bash
./gradlew test   # Run unit tests
./gradlew run    # Launch RuneLite in developer mode with BossWise loaded
```

---

## License

This project is licensed under the **BSD 2-Clause License**. See the [LICENSE](LICENSE) file for details.
