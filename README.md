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
- [User Interface Design](#user-interface-design)
- [Technical Architecture](#technical-architecture)
- [Building & Testing](#building--testing)
  - [Prerequisites](#prerequisites)
  - [Automated Verification](#automated-verification)
  - [Launching in RuneLite Dev Mode](#launching-in-runelite-dev-mode)
  - [Launching Standalone UI Preview](#launching-standalone-ui-preview)
- [Project Directory Structure](#project-directory-structure)
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

## User Interface Design

Crafted to match modern commercial RuneLite plugin design standards:
- **Matte Black Aesthetic**: `#121212` background with `#1E1E1E` dark charcoal card containers.
- **Subtle OSRS Gold Accents**: Clean `#D4AF37` highlights and borders.
- **Typography**: Native RuneLite fonts (`FontManager.getRunescapeBoldFont`, `FontManager.getRunescapeFont`).
- **Compact Layout**: Formatted for standard RuneLite sidebar widths (225px to 270px) with responsive card stacking.
- **Status Pills**: Vivid Green (`#43A047`), Amber (`#FB8C00`), and Red (`#E53935`) indicators.

---

## Technical Architecture

```
com.bosswise
├── BossWisePlugin.java          # Lifecycle, Guice injection, RuneLite event bus subscriptions
├── config
│   └── BossWiseConfig.java      # RuneLite config group ("bosswise") with settings & defaults
├── model
│   ├── BossCategory.java        # Beginner, Intermediate, Advanced, Endgame, Slayer, Raids...
│   ├── BossData.java            # Boss model, requirements, mechanics, drop tables, inventory presets
│   ├── BossDifficulty.java      # Beginner, Intermediate, Advanced, Master, Grandmaster
│   ├── BossEligibility.java     # Computed readiness status, score (0-100), and missing requirements
│   ├── BossEligibilityStatus.java # READY, ALMOST_READY, LOCKED
│   ├── CombatStyle.java         # Melee, Ranged, Magic, Hybrid, Tribrid
│   ├── EquipmentItem.java       # Stats, requirements, slot, special effects, pricing, tier
│   ├── EquipmentSlot.java       # Head, Cape, Neck, Weapon, Body, Shield, Legs, Gloves, Boots, Ring...
│   ├── GearUpgradeRecommendation.java # Slot upgrade delta, price gap, stat delta, budget fit
│   ├── InventoryItem.java       # Item ID, name, quantity, category, unit price, mandatory flag
│   ├── InventorySetup.java      # Preset loadout tier with total supply cost
│   ├── OptimizedGearSetup.java  # Output loadout, total cost, remaining GP, stat totals, slot reasons
│   ├── PlayerAccount.java       # Scanned stats (real & boosted), combat level, quest completion set
│   ├── ProgressionGoal.java     # Skill, Quest, Gear milestone goals with progress calculation
│   ├── QuestRequirement.java    # Quest enum, quest name, mandatory flag, notes
│   └── SkillRequirement.java    # Skill enum, required level, boostable flag, boost method
├── scanner
│   ├── AccountScannerService.java # Reads player stats, combat level, total level via Client APIs
│   ├── QuestInfo.java           # Quest metadata, skill requirements, prerequisites, unlocked bosses
│   └── QuestScannerService.java # Scans Quest.getState(client) and maintains quest unlocks database
├── db
│   ├── BossDatabase.java        # 30+ comprehensive, verified OSRS bosses
│   └── EquipmentDatabase.java   # Rich equipment database across Melee, Ranged, and Magic
├── service
│   ├── BossRecommendationEngine.java # Intelligent scoring & eligibility evaluation
│   ├── GePriceService.java      # RuneLite ItemManager integration + caching & offline fallbacks
│   ├── GearOptimizerService.java # Knapsack budget optimizer across 12 equipment slots
│   ├── GearUpgradeAdvisor.java  # Upgrade recommendations based on current gear
│   ├── InventoryBuilderService.java # Supply calculator with live GE pricing
│   └── ProgressionService.java  # Account progression roadmap generator
└── ui
    ├── BossWiseDesign.java      # Design system: palette, borders, fonts, spacing
    ├── BossWisePluginPanel.java # Master PluginPanel container with tab bar navigation
    ├── DashboardPanel.java      # Account stats, metrics, top boss, top upgrade, roadmap preview
    ├── BossFinderPanel.java     # Filterable, sortable, searchable boss card directory
    ├── GearOptimizerPanel.java  # Budget selector, 3 tier setups, slot table, upgrade advisor
    ├── BossGuidePanel.java      # Strategy guides, mechanics, checklists, pro tips, drops
    ├── InventoryBuilderPanel.java # Supplies table, quantities, cost calculation
    ├── ProgressionPanel.java    # Account milestones roadmap with progress bars
    ├── SettingsPanel.java       # In-panel settings & manual account scan trigger
    └── components
        ├── BossCard.java        # Interactive boss card with status pill, score, actions
        ├── EquipmentSlotRow.java # Slot row with item name, price tag, stats, and upgrade hint
        ├── GoldButton.java      # Styled OSRS button with hover & click animations
        ├── StatProgressBar.java # Custom progress bar with percentage indicator
        └── StatusBadge.java     # Rounded pill badge for READY, ALMOST READY, LOCKED
```

---

## Building & Testing

### Prerequisites
- JDK 11, JDK 17, or JDK 21 (Eclipse Adoptium Temurin recommended)
- RuneLite client dependencies (automatically resolved from Gradle cache)

### Automated Verification
Run the included verification build script to compile sources, execute unit tests, and package the distribution JAR:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 verify
```

Expected output:
```
[INFO] Compiled 43 source files to target\classes
[INFO] Compiled 11 test files to target\test-classes
[INFO] Executing tests: com.bosswise.BossEligibilityTest, com.bosswise.GearBudgetOptimizerTest, ...
OK (15 tests)
[INFO] Built jar: target\bosswise-1.0.0.jar
[INFO] BUILD SUCCESS
```

### Launching in RuneLite Dev Mode
To launch RuneLite with BossWise loaded as an active plugin:

```cmd
run-bosswise.bat
```

### Launching Standalone UI Preview
To inspect and test the UI layout and features in a standalone desktop preview window without starting the full RuneLite client:

```cmd
run-preview.bat
```

---

## License

This project is licensed under the **BSD 2-Clause License**. See the [LICENSE](LICENSE) file for details.
