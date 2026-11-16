package com.bosswise.config;

import com.bosswise.model.CombatStyle;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("bosswise")
public interface BossWiseConfig extends Config {
    @ConfigItem(
            keyName = "defaultBudget",
            name = "Default Gear Budget (GP)",
            description = "Default available GP budget for equipment recommendations",
            position = 1
    )
    default int defaultBudget() {
        return 5_000_000;
    }

    @ConfigItem(
            keyName = "preferredCombatStyle",
            name = "Preferred Combat Style",
            description = "Default combat style selected in the Gear Optimizer",
            position = 2
    )
    default CombatStyle preferredCombatStyle() {
        return CombatStyle.MELEE;
    }

    @ConfigItem(
            keyName = "equipmentPriority",
            name = "Equipment Priority",
            description = "Whether the optimizer prioritizes maximum DPS, high defence, or balanced setups",
            position = 3
    )
    default PriorityMode equipmentPriority() {
        return PriorityMode.BALANCED;
    }

    @ConfigItem(
            keyName = "showLockedBosses",
            name = "Show Locked Bosses",
            description = "Display bosses that are locked due to missing quests or skill requirements",
            position = 4
    )
    default boolean showLockedBosses() {
        return true;
    }

    @ConfigItem(
            keyName = "showGroupBosses",
            name = "Show Group Bosses",
            description = "Display group and raid encounters in the boss finder",
            position = 5
    )
    default boolean showGroupBosses() {
        return true;
    }

    @ConfigItem(
            keyName = "compactLayout",
            name = "Compact Layout",
            description = "Use compact spacing to display more information on smaller sidebar widths",
            position = 6
    )
    default boolean compactLayout() {
        return false;
    }

    @ConfigItem(
            keyName = "autoRefreshOnStatChange",
            name = "Auto-Refresh on Stat Change",
            description = "Automatically update boss recommendations and progression when player levels up",
            position = 7
    )
    default boolean autoRefreshOnStatChange() {
        return true;
    }
}
