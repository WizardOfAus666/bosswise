package com.bosswise.service;

import com.bosswise.model.AfkMonsterProfile;
import com.bosswise.model.CombatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AfkMonsterFinderService {
    private final List<AfkMonsterProfile> profiles = new ArrayList<>();

    @Inject
    public AfkMonsterFinderService() {
        initProfiles();
    }

    private void initProfiles() {
        profiles.add(new AfkMonsterProfile(
                "Nightmare Zone (Custom Rumble)", "Yanille (NMZ)", 10, 20, CombatStyle.MELEE,
                true, true, "Absorption potions at 1 HP with Rock cake. Max hit capped at 1.",
                115000, -20000, "Maximum AFK in OSRS. 20 minutes before needing to interact or drink potions."
        ));

        profiles.add(new AfkMonsterProfile(
                "Ammonite Crabs", "Fossil Island (North Beach)", 9, 10, CombatStyle.MELEE,
                true, false, "Monsters hit max 1 with 1 Defence. 100% survivability without armor.",
                72000, 0, "10-minute continuous auto-retaliate aggression. Run north to reset timer."
        ));

        profiles.add(new AfkMonsterProfile(
                "Vyrewatch Sentinels", "Darkmeyer", 8, 8, CombatStyle.MELEE,
                true, false, "Protect from Melee. Free prayer restore altar 5 tiles away.",
                80000, 420000, "High combat XP while making steady profit from 1/1500 Blood Shard drops."
        ));

        profiles.add(new AfkMonsterProfile(
                "Rune Dragons", "Lithkren Vault (DS2)", 7, 5, CombatStyle.MELEE,
                true, false, "Justiciar armour + Dragonfire shield + Insulated boots. Tank setup.",
                65000, 1460000, "Lucrative semi-AFK combat. Justiciar tanks melee while praying Mage."
        ));

        profiles.add(new AfkMonsterProfile(
                "Brutal Black Dragons", "Catacombs of Kourend", 8, 6, CombatStyle.RANGED,
                true, false, "Protect from Magic + Extended Antifire = 0 incoming damage taken.",
                55000, 940000, "100% damage immunity with Protect from Magic and Extended Antifire."
        ));

        profiles.add(new AfkMonsterProfile(
                "Cave Kraken", "Kraken Cove", 8, 4, CombatStyle.MAGIC,
                true, false, "Very low damage taken with Karil's or high magic defence armor.",
                40000, 1450000, "Click boss with fishing bait, wait ~50 seconds per kill. Extremely chill."
        ));

        profiles.add(new AfkMonsterProfile(
                "Skeletal Wyverns", "Asgarnia Ice Dungeon", 8, 6, CombatStyle.RANGED,
                false, false, "100% safe spot behind icy stalagmites using crossbow.",
                48000, 820000, "Wyverns cannot retaliate from safespot. Great for low-attention ranged GP."
        ));

        profiles.add(new AfkMonsterProfile(
                "Gargoyles", "Slayer Tower", 7, 4, CombatStyle.MELEE,
                true, false, "Guthan's armor set for passive health drain, or Auto-smash hammer.",
                50000, 600000, "Steady alch drops (Rune full helms, 2h swords, adamant boots). Very relaxed."
        ));

        profiles.add(new AfkMonsterProfile(
                "Sand Crabs", "Hosidius Beach", 9, 10, CombatStyle.MELEE,
                true, false, "Passive low combat hits. Zero food required for 40+ Defence.",
                45000, 0, "The classic early-to-mid game AFK training spot. Accessible to all players."
        ));
    }

    public List<AfkMonsterProfile> getAllAfkProfiles() {
        List<AfkMonsterProfile> sorted = new ArrayList<>(profiles);
        sorted.sort(Comparator.comparingInt(AfkMonsterProfile::getAfkScore).reversed()
                .thenComparingInt(AfkMonsterProfile::getAttentionIntervalMinutes).reversed());
        return Collections.unmodifiableList(sorted);
    }
}
