package com.bosswise.service;

import com.bosswise.model.GroupRolePlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GroupBossPlannerService {
    public static final List<String> SUPPORTED_BOSSES = Collections.unmodifiableList(Arrays.asList(
            "Nex", "General Graardor", "Corporeal Beast", "Tombs of Amascut",
            "Chambers of Xeric", "Theatre of Blood", "K'ril Tsutsaroth", "Kree'arra"
    ));

    private final Map<String, List<GroupRolePlan>> plansByBoss = new HashMap<>();

    @Inject
    public GroupBossPlannerService() {
        initPlans();
    }

    private void initPlans() {
        // Nex
        plansByBoss.put("Nex", Arrays.asList(
                new GroupRolePlan("Main Range DPS", "Equips ZCB / Tbow / Masori to deal maximum sustained damage during all 5 phases.",
                        "Masori / Armadyl, ZCB + Ruby dragon bolts (e), Venator ring", "Zaryte Crossbow (ZCB)",
                        "10 Saradomin brews, 6 Super restores, 1 Ranging pot", 25, 4500000,
                        Arrays.asList("DPS blood reavers during Blood phase.", "Spec ZCB immediately on phase transitions.", "Stand spread out during Shadow phase.")),
                new GroupRolePlan("Tank / Phase Caller", "Walks Nex under and calls cough / phase timing. Wears high magic/range defence.",
                        "Torva / Justiciar + Spectral / Elysian spirit shield", "Bandos Godsword (BGS)",
                        "14 Saradomin brews, 6 Super restores, 1 Divine pot", 25, 4500000,
                        Arrays.asList("Aggro Nex first to take melee aggression.", "Walk under Nex when ice prison triggers.", "Drink brews continuously while tanking."))
        ));

        // General Graardor
        plansByBoss.put("General Graardor", Arrays.asList(
                new GroupRolePlan("Tank", "Aggros Bandos first, wears full Justiciar/Torva + Dragonfire shield or Crystal shield.",
                        "Justiciar body & legs, Dinh's bulwark / DFS, Ring of suffering", "Dragon Warhammer (DWH)",
                        "12 Saradomin brews, 4 Super restores, 1 Super combat, Bones to Peaches", 40, 1800000,
                        Arrays.asList("Enter room first to receive Graardor's initial melee hits.", "Pray Protect from Melee continuously.", "Cast Bones to Peaches after bodyguards die.")),
                new GroupRolePlan("DPS Attacker", "Max strength gear (Bandos / Torva / Fang / Voidwaker) to eliminate Graardor in under 20 seconds.",
                        "Torva / Bandos, Osmumten's Fang / Scythe, Ultor / Berserker ring", "Voidwaker / Dragon Claws",
                        "6 Anglerfish, 4 Karambwans, 5 Super restores, 2 Divine super combat", 40, 1800000,
                        Arrays.asList("Enter immediately after tank.", "Dump claws or Voidwaker specs.", "Help kill the ranged minion (Grimspike) first post-kill."))
        ));

        // Corporeal Beast
        plansByBoss.put("Corporeal Beast", Arrays.asList(
                new GroupRolePlan("Spec Reducer", "Spears with DWH, BGS, and Arclight to drain Corp's stats to zero.",
                        "Dragon warhammer, Arclight, Bandos godsword, Teleport to house", "DWH -> Arclight -> BGS",
                        "House teleport tabs, Super combat, 4 Karambwans", 15, 1200000,
                        Arrays.asList("Land 3 DWH specs, 20 Arclight specs, and 200 BGS damage.", "Pool reset in POH between spec dumps.", "Swap to Zamorakian spear or Fang once drained.")),
                new GroupRolePlan("Main DPS", "Attacks with Zamorakian spear or Osmumten's fang once Corporeal Beast is fully drained.",
                        "Zamorakian spear / Fang, Bandos armor, Ferocious gloves", "Dragon Claws",
                        "Games necklaces, Sharks, Super combat, Prayer pots", 15, 1200000,
                        Arrays.asList("Spear / Hasta / Fang ONLY (other weapons suffer 50% damage penalty).", "Step away when dark core jumps towards you."))
        ));

        // Tombs of Amascut
        plansByBoss.put("Tombs of Amascut", Arrays.asList(
                new GroupRolePlan("Main DPS", "Primary raid damage dealer using Tumeken's Shadow and Fang.",
                        "Tumeken's shadow + Osmumten's fang, Masori & Ancestral switches", "Voidwaker / BGS",
                        "Raid supply pots (Nectar, Tears, Ambrosia, Adrenaline)", 2, 4200000,
                        Arrays.asList("Call skull phase on Wardens.", "Coordinate Adrenaline pot with BGS / ZCB specs.", "Pass salts at start of raid.")),
                new GroupRolePlan("Keris Healer", "Carries Keris Partisan of the Sun to heal teammates in emergencies using special attack.",
                        "Keris partisan of the sun, Lightbearer, Fang switch", "Keris Sun Spec",
                        "Extra prayer potions for sun spec conversion", 2, 4200000,
                        Arrays.asList("Save spec bar for Sun healing on teammates in P3/P4.", "DPS with standard weapons during path rooms."))
        ));
    }

    public List<String> getSupportedBosses() {
        return SUPPORTED_BOSSES;
    }

    public List<GroupRolePlan> getPlansForBoss(String bossName) {
        if (bossName == null) return Collections.emptyList();
        for (Map.Entry<String, List<GroupRolePlan>> entry : plansByBoss.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(bossName)) {
                return entry.getValue();
            }
        }
        // Fallback for any boss
        return Arrays.asList(
                new GroupRolePlan("Main DPS", "Primary damage dealer focusing on boss weaknesses.",
                        "Best available combat gear for boss style", "BGS / Dragon Claws",
                        "Standard boss supplies + House tabs", 20, 1500000,
                        Arrays.asList("Focus fire on boss.", "Coordinate spec reduction with teammates.")),
                new GroupRolePlan("Spec & Support", "Provides Defence reduction specs (DWH / BGS) and backup food.",
                        "Dragon warhammer / Bandos godsword + Tank gear", "DWH / BGS",
                        "Extra prayer potions and combo foods", 20, 1500000,
                        Arrays.asList("Land first spec on spawn.", "Support tanking if boss switches target."))
        );
    }
}
