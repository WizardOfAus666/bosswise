package com.bosswise.service;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.DeathRiskProfile;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterProfitProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MonsterProfitService {
    public enum ProfitFilter {
        ALL("All Monsters"),
        BOSSES("Bosses & Demi-Bosses"),
        SLAYER("Slayer Monsters"),
        LOW_LEVEL("Low-Level & Early Game"),
        MELEE("Best for Melee"),
        RANGED("Best for Ranged"),
        MAGIC("Best for Magic"),
        AFK("Best AFK Options"),
        LOW_GEAR_COST("Budget-Friendly (<1M Gear)");

        private final String displayName;
        ProfitFilter(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum ProfitSort {
        NET_PROFIT_DESC("Highest Net Profit"),
        GP_PER_KILL_DESC("Highest GP Per Kill"),
        KILLS_PER_HR_DESC("Fastest Kills/Hour"),
        GEAR_COST_ASC("Lowest Gear Cost");

        private final String displayName;
        ProfitSort(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    private final List<MonsterProfitProfile> profiles = new ArrayList<>();

    @Inject
    public MonsterProfitService() {
        initProfiles();
    }

    private void initProfiles() {
        // High Tier Bosses & Raids
        add("tombs_of_amascut", "Tombs of Amascut (Expert 300)", MonsterCategory.RAID, CombatStyle.HYBRID, 120,
                3800000, 2, 5500000, 7500000, 450000, 150000, 50000000, false, 1500, 180,
                new DeathRiskProfile("Raid Wipe Chest", 0, 100000, 45000, false, "100k party wipe fee upon team death"),
                "Assumes solo Invocations 300, ~27 min clear time, includes expected value of Shadow, Fang, and Masori.");

        add("nex", "Nex (5-Man / Mini)", MonsterCategory.BOSS, CombatStyle.RANGED, 1001,
                850000, 14, 5300000, 8200000, 900000, 300000, 25000000, false, 180, 45,
                DeathRiskProfile.forMonster("Nex", false, 25000000),
                "Assumes 5-man team, Torva/ZCB/Nihil horn drop splits included in expected value.");

        add("vorkath", "Vorkath", MonsterCategory.BOSS, CombatStyle.RANGED, 732,
                135000, 28, 3330000, 3950000, 280000, 170000, 2500000, false, 95, 25,
                DeathRiskProfile.forMonster("Vorkath", false, 2500000),
                "Assumes Dragon Hunter Crossbow / Lance, Salve (ei), 28 kills/hr including banking via Moonclan/Fremennik boots.");

        add("alchemical_hydra", "Alchemical Hydra", MonsterCategory.BOSS, CombatStyle.RANGED, 426,
                145000, 24, 3130000, 3800000, 220000, 130000, 15000000, false, 115, 25,
                DeathRiskProfile.forMonster("Alchemical Hydra", false, 15000000),
                "Assumes 95 Slayer, Blowpipe/Bowfa/Tbow, includes Hydra Claw & Leather EV.");

        add("phantom_muspah", "Phantom Muspah", MonsterCategory.BOSS, CombatStyle.RANGED, 742,
                140000, 22, 2680000, 3300000, 260000, 140000, 5000000, false, 120, 25,
                DeathRiskProfile.forMonster("Phantom Muspah", false, 5000000),
                "Assumes Bowfa or Crossbow with Sapphire dragon bolts (e) for shield phase.");

        add("vardorvis", "Vardorvis", MonsterCategory.BOSS, CombatStyle.MELEE, 720,
                125000, 30, 3300000, 4200000, 350000, 100000, 10000000, false, 85, 20,
                DeathRiskProfile.forMonster("Vardorvis", false, 10000000),
                "Assumes Soulreaper axe / Tentacle whip / Fang on slash, includes Ultor vestige EV.");

        add("duke_sucellus", "Duke Sucellus", MonsterCategory.BOSS, CombatStyle.MELEE, 640,
                115000, 24, 2410000, 3100000, 250000, 100000, 8000000, false, 110, 25,
                DeathRiskProfile.forMonster("Duke Sucellus", false, 8000000),
                "Assumes Arclight/Emberlight/Scythe, Magus vestige EV included.");

        add("zulrah", "Zulrah", MonsterCategory.BOSS, CombatStyle.HYBRID, 725,
                110000, 24, 2240000, 2850000, 250000, 150000, 3000000, false, 110, 20,
                DeathRiskProfile.forMonster("Zulrah", false, 3000000),
                "Assumes Trident of the swamp + Blowpipe / Bowfa switch, Zul-andra teleports.");

        add("the_whisperer", "The Whisperer", MonsterCategory.BOSS, CombatStyle.MAGIC, 680,
                135000, 20, 2200000, 2900000, 400000, 100000, 12000000, false, 135, 25,
                DeathRiskProfile.forMonster("The Whisperer", false, 12000000),
                "Assumes Sanguinesti / Shadow magic, Bellator vestige EV included.");

        add("the_leviathan", "The Leviathan", MonsterCategory.BOSS, CombatStyle.RANGED, 710,
                130000, 22, 2410000, 3150000, 320000, 130000, 10000000, false, 120, 25,
                DeathRiskProfile.forMonster("The Leviathan", false, 10000000),
                "Assumes Webweaver / Bowfa / ZCB, Venator vestige EV included.");

        add("cerberus", "Cerberus", MonsterCategory.BOSS, CombatStyle.MELEE, 318,
                120000, 28, 2760000, 3600000, 520000, 80000, 6000000, false, 90, 20,
                DeathRiskProfile.forMonster("Cerberus", false, 6000000),
                "Assumes 91 Slayer, Arclight/Emberlight, Primordial crystal EV, high prayer drain from ghosts.");

        add("general_graardor", "General Graardor", MonsterCategory.BOSS, CombatStyle.MELEE, 624,
                95000, 25, 2020000, 2800000, 280000, 70000, 5000000, false, 100, 25,
                DeathRiskProfile.forMonster("General Graardor", false, 5000000),
                "Assumes Door-altar ranged or standard tank+DPS, Bandos chest/tassets EV included.");

        add("calvarion", "Calvar'ion", MonsterCategory.WILDERNESS, CombatStyle.MELEE, 274,
                85000, 35, 2820000, 3500000, 120000, 30000, 500000, false, 75, 15,
                DeathRiskProfile.forMonster("Calvar'ion", true, 500000),
                "Wilderness demi-boss, low risk (3 items protected), Dragon pickaxe and Voidwaker blade EV.");

        add("spindel", "Spindel", MonsterCategory.WILDERNESS, CombatStyle.MELEE, 278,
                90000, 30, 2520000, 3200000, 140000, 40000, 500000, false, 85, 15,
                DeathRiskProfile.forMonster("Spindel", true, 500000),
                "Wilderness demi-boss, Ursine chainmace/crush, Voidwaker gem EV.");

        add("artio", "Artio", MonsterCategory.WILDERNESS, CombatStyle.RANGED, 276,
                95000, 28, 2480000, 3100000, 140000, 40000, 500000, false, 90, 15,
                DeathRiskProfile.forMonster("Artio", true, 500000),
                "Wilderness demi-boss, Bowfa/Webweaver, Voidwaker hilt EV.");

        add("demonic_gorillas", "Demonic Gorillas", MonsterCategory.SLAYER, CombatStyle.HYBRID, 275,
                65000, 45, 2620000, 3400000, 240000, 60000, 2000000, false, 60, 15,
                DeathRiskProfile.forMonster("Demonic Gorillas", false, 2000000),
                "Requires MM2, 1/300 Zenyte shard EV dominates profit.");

        // Mid-Level & AFK Monsters
        add("kraken", "Kraken", MonsterCategory.BOSS, CombatStyle.MAGIC, 291,
                38000, 50, 1450000, 1850000, 380000, 70000, 1500000, true, 55, 12,
                DeathRiskProfile.forMonster("Kraken", false, 1500000),
                "87 Slayer required. Highly AFK. Trident of the swamp, fishing bait.");

        add("rune_dragons", "Rune Dragons", MonsterCategory.REGULAR, CombatStyle.MELEE, 380,
                38000, 45, 1460000, 1800000, 210000, 40000, 8000000, true, 65, 15,
                DeathRiskProfile.forMonster("Rune Dragons", false, 8000000),
                "Requires DS2. Dragon hunter lance + Justiciar for high AFK stability.");

        add("brutal_black_dragons", "Brutal Black Dragons", MonsterCategory.REGULAR, CombatStyle.RANGED, 315,
                32000, 35, 940000, 120000, 140000, 40000, 2000000, true, 75, 20,
                DeathRiskProfile.forMonster("Brutal Black Dragons", false, 2000000),
                "Requires 77 Slayer. 100% safe prayer protection. Dragon hunter crossbow or blowpipe.");

        add("skeletal_wyverns", "Skeletal Wyverns", MonsterCategory.SLAYER, CombatStyle.RANGED, 140,
                22000, 40, 820000, 1050000, 45000, 15000, 800000, true, 70, 15,
                DeathRiskProfile.forMonster("Skeletal Wyverns", false, 800000),
                "Requires 72 Slayer. Safe-spot ranged with broad bolts / ruby bolts.");

        add("barrows", "Barrows Brothers", MonsterCategory.BOSS, CombatStyle.MAGIC, 115,
                65000, 14, 820000, 1100000, 70000, 20000, 350000, false, 190, 20,
                DeathRiskProfile.forMonster("Barrows", false, 350000),
                "Iban staff / Trident + Spade. Morytania Hard diary doubles rune rewards.");

        add("gargoyles", "Gargoyles", MonsterCategory.SLAYER, CombatStyle.MELEE, 111,
                4500, 140, 600000, 750000, 25000, 5000, 300000, true, 20, 5,
                DeathRiskProfile.forMonster("Gargoyles", false, 300000),
                "Requires 75 Slayer. High alch drops, rock hammer, guthan or monkfish.");

        add("kurasks", "Kurasks", MonsterCategory.SLAYER, CombatStyle.MELEE, 106,
                4200, 120, 479000, 600000, 20000, 5000, 200000, true, 22, 5,
                DeathRiskProfile.forMonster("Kurasks", false, 200000),
                "Requires 70 Slayer. Leaf-bladed sword / battleaxe. High seed & coin drops.");

        add("scurrius", "Scurrius (The Rat King)", MonsterCategory.BOSS, CombatStyle.MELEE, 156,
                18000, 40, 640000, 780000, 65000, 15000, 100000, false, 65, 15,
                DeathRiskProfile.forMonster("Scurrius", false, 100000),
                "Beginner boss in Varrock Sewers. Bone mace flat +10 damage. Excellent early combat XP.");
    }

    private void add(String id, String name, MonsterCategory cat, CombatStyle style, int cb,
                     long gpKill, int kph, long consGp, long optGp, long supCost, long ammoCost,
                     long minGear, boolean afk, int killTime, int respawn, DeathRiskProfile death, String assumptions) {
        profiles.add(new MonsterProfitProfile(
                id, name, cat, style, cb, gpKill, kph, consGp, optGp, supCost, ammoCost,
                minGear, afk, killTime, respawn, death, assumptions
        ));
    }

    public List<MonsterProfitProfile> getAllProfiles() {
        return Collections.unmodifiableList(profiles);
    }

    public MonsterProfitProfile getProfile(String id) {
        if (id == null) return null;
        for (MonsterProfitProfile p : profiles) {
            if (p.getMonsterId().equalsIgnoreCase(id) || p.getMonsterName().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public List<MonsterProfitProfile> getFilteredProfiles(ProfitFilter filter, ProfitSort sort) {
        List<MonsterProfitProfile> result = new ArrayList<>();

        for (MonsterProfitProfile p : profiles) {
            boolean match = true;
            if (filter != null) {
                switch (filter) {
                    case BOSSES:
                        match = (p.getCategory() == MonsterCategory.BOSS || p.getCategory() == MonsterCategory.RAID || p.getCategory() == MonsterCategory.DEMI_BOSS);
                        break;
                    case SLAYER:
                        match = (p.getCategory() == MonsterCategory.SLAYER || p.getCategory() == MonsterCategory.SUPERIOR);
                        break;
                    case LOW_LEVEL:
                        match = (p.getCombatLevel() <= 160 || p.getMinGearInvestment() <= 500000);
                        break;
                    case MELEE:
                        match = (p.getPrimaryStyle() == CombatStyle.MELEE);
                        break;
                    case RANGED:
                        match = (p.getPrimaryStyle() == CombatStyle.RANGED);
                        break;
                    case MAGIC:
                        match = (p.getPrimaryStyle() == CombatStyle.MAGIC);
                        break;
                    case AFK:
                        match = p.isAfkSuitable();
                        break;
                    case LOW_GEAR_COST:
                        match = (p.getMinGearInvestment() <= 1000000);
                        break;
                    case ALL:
                    default:
                        match = true;
                        break;
                }
            }
            if (match) {
                result.add(p);
            }
        }

        if (sort != null) {
            switch (sort) {
                case GP_PER_KILL_DESC:
                    result.sort(Comparator.comparingLong(MonsterProfitProfile::getGpPerKill).reversed());
                    break;
                case KILLS_PER_HR_DESC:
                    result.sort(Comparator.comparingInt(MonsterProfitProfile::getKillsPerHour).reversed());
                    break;
                case GEAR_COST_ASC:
                    result.sort(Comparator.comparingLong(MonsterProfitProfile::getMinGearInvestment));
                    break;
                case NET_PROFIT_DESC:
                default:
                    result.sort(Comparator.comparingLong(MonsterProfitProfile::getNetProfitPerHour).reversed());
                    break;
            }
        }

        return result;
    }
}
