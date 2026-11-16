# BossWise – Collaborator & Developer Guide

Welcome to the **BossWise** development team! This guide explains how to set up, develop, test, and contribute to the BossWise RuneLite plugin.

---

## 1. Quick Start for Collaborators

### Prerequisites
- **Java Development Kit (JDK)**: Java 11 or 17 (Java 17 recommended; e.g. [Eclipse Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17)).
- **Git**: For version control and branch management.
- **IDE**: IntelliJ IDEA (recommended), Visual Studio Code, or Eclipse.

### Opening in Your IDE
- **IntelliJ IDEA**: 
  1. Click `File` -> `Open...`
  2. Select the `build.gradle` file in the project root.
  3. Choose **Open as Project**. IntelliJ will automatically import all Gradle dependencies (RuneLite 1.13.6, Guice, Lombok, FlatLaf, JUnit).
- **VS Code**:
  1. Open the project folder.
  2. Install the **Extension Pack for Java** extension.
  3. The Gradle daemon will configure the project automatically.

---

## 2. Common Development Commands

### Run RuneLite with BossWise Attached (Dev Mode)
To launch a live RuneLite client with BossWise pre-installed on the sidebar:
- **Windows Batch**: Double-click `run-plugin.bat` (or run `.\run-plugin.bat` in terminal).
- **Gradle Wrapper**:
  ```bash
  ./gradlew run --no-daemon
  ```
- **PowerShell**:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .\run-bosswise.ps1
  ```

### Run the Automated Test Suite (97 Unit Tests)
Before submitting code, ensure all 97 tests pass cleanly:
```bash
# Using Gradle
./gradlew test

# Or using the build script
powershell -ExecutionPolicy Bypass -File .\build.ps1 test
```

### Build Distribution JAR
```bash
./gradlew jar
# Built JAR is output to build/libs/bosswise-1.0.0.jar (or target/bosswise-1.0.0.jar)
```

---

## 3. Architecture Overview

BossWise uses **Google Guice** dependency injection (RuneLite standard) and is split into modular layers:

```
src/main/java/com/bosswise/
├── BossWisePlugin.java             # Main RuneLite plugin lifecycle, event subscribers (@Subscribe)
├── config/
│   └── BossWiseConfig.java         # User-facing configuration settings
├── db/
│   ├── EquipmentDatabase.java      # 220 verified OSRS equipment items (267 indexed variations)
│   ├── MonsterDatabase.java        # 75 universal OSRS monsters (86 variants)
│   ├── MonsterStrategyDatabase.java # 75 verified OSRS Wiki combat strategies with 11-slot gear
│   └── BossDatabase.java           # Classic boss profiles and eligibility requirements
├── model/
│   ├── EquipmentItem.java          # Full OSRS equipment stats, special effects, requirements
│   ├── OptimizedGearSetup.java     # Complete loadout with [WIKI], [OPT], [BUDGET] category tags
│   ├── PlayerAccount.java          # Logged-in player skill levels, quests, combat level
│   └── strategy/                   # Strategy models, mechanics, and combat methods
├── scanner/
│   ├── AccountScannerService.java  # Real-time player skills & quest verification
│   └── CurrentGearScannerService.java # Equipped items and bank scanner
├── service/
│   ├── GearOptimizerService.java   # Generates multi-tier setups respecting OSRS mechanics
│   ├── CombatCalculationService.java # Accurate OSRS hit chance, max hit, DPS engine
│   ├── LootTrackerService.java     # Live loot tracking, profit/hr, supply deduction
│   └── ProgressionService.java     # Account roadmap and quest suggestions
└── ui/
    ├── BossWisePluginPanel.java    # Top bar navigation, 280px sidebar container
    ├── BossWiseDesign.java         # Centralized Light Brown / Warm Parchment design system
    ├── components/                 # WrapText (zero overflow), GoldButton, StatProgressBar
    └── *Panel.java                 # Individual tab panels (Dashboard, Gear, Guides, Slayer, etc.)
```

---

## 4. UI Design System (Light Brown / Warm Parchment)

BossWise uses a 280px wide high-readability layout styled with an authentic OSRS parchment aesthetic:
- **Width**: `BossWiseDesign.PANEL_WIDTH = 280` (`USABLE_WIDTH = 256`, `BODY_TEXT_WIDTH = 236`).
- **Backgrounds**: `BG_DARK` (`#D2C1A5` Tan Parchment), `PANEL_BG` (`#C5B294` Toasted Sandstone), `CARD_BG` (`#DFD2BC` Light Parchment).
- **Typography**: `TEXT_PRIMARY` (`#24180E` Dark Espresso Umber) delivers an 11.5:1 AAA contrast ratio.
- **Zero Overflow Policy**: Use `WrapText` instead of standard `JLabel` for multi-word or dynamic text to prevent text clipping across all sidebar widths.

---

## 5. Gear Recommendation Guidelines

When adding or modifying gear recommendations:
1. **Always Verify with OSRS Wiki**: Every recommendation should reference the official strategy guide (e.g. `https://oldschool.runescape.wiki/w/<Monster_Name>/Strategies`).
2. **Assign Clear Category Badges**:
   - `[WIKI RECOMMENDED]` – Setups directly from the Wiki strategy guides.
   - `[BOSSWISE OPTIMISED]` – Calculated DPS alternatives.
   - `[BUDGET ALTERNATIVE]` – Viable setups under 1.5M GP.
   - `[PLAYER OWNED]` – Assembled from player bank / equipped items.
3. **Respect Hard Mechanics**:
   - Kurasks & Turoths **must** use Leaf-bladed weapons or broad ammo.
   - Basilisks **must** equip a Mirror shield or V's shield.
   - Skeletal Wyverns **must** equip a Wyvern shield, Dragonfire shield, or Elemental shield.
   - Corporeal Beast **must** equip a Spear or Fang (damage penalty otherwise).
   - Kree'arra **cannot** be attacked with Melee.
   - Kraken **cannot** be attacked with Melee or Ranged.

---

## 6. Git Workflow

```bash
# 1. Create a feature branch
git checkout -b feature/your-feature-name

# 2. Make your edits and run all tests
./gradlew test

# 3. Commit with a clear message
git commit -m "feat(gear): add new Colosseum combat strategy"

# 4. Push to remote and open a Pull Request
git push -u origin feature/your-feature-name
```

---

## 7. RuneLite Plugin Hub Compliance

BossWise is built for the official **RuneLite Plugin Hub**. All contributions must strictly follow Jagex and RuneLite third-party client rules:
- **No automation**: Never click, switch prayers, or automate gameplay.
- **Purely informational**: Present combat calculations, strategy guides, and advice clearly to the user.
- **No private server or deobfuscated client APIs**: Rely only on public `net.runelite.api` and `net.runelite.client` interfaces.
