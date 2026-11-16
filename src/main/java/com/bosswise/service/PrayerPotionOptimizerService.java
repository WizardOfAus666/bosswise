package com.bosswise.service;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.PrayerPlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;

@Singleton
public class PrayerPotionOptimizerService {
    private final GePriceService priceService;

    @Inject
    public PrayerPotionOptimizerService(GePriceService priceService) {
        this.priceService = priceService;
    }

    public List<PrayerPlan> evaluatePrayerPlans(
            MonsterCombatProfile profile,
            PlayerAccount account,
            CombatStyle style,
            int totalPrayerBonus) {

        int prayLvl = account != null ? account.getRealLevel(Skill.PRAYER) : 70;
        int restorePerDose = (int) Math.floor(7 + (prayLvl * 0.25));
        int restorePerPot = restorePerDose * 4;

        long prayPotPrice = priceService != null ? priceService.getPrice(139, 9500) : 9500;
        long superCombatPrice = priceService != null ? priceService.getPrice(12695, 12500) : 12500;
        long divineCombatPrice = priceService != null ? priceService.getPrice(23685, 18500) : 18500;

        String defOverhead = (profile != null && profile.getOverheadPrayer() != null && !profile.getOverheadPrayer().equalsIgnoreCase("None"))
                ? profile.getOverheadPrayer()
                : "Protect from Melee";

        List<PrayerPlan> plans = new ArrayList<>();

        // 1. Full Max Power (Piety / Rigour / Augury + Overhead + Divine Potions)
        String maxOffensive;
        double maxDmg;
        double maxAcc;
        double baseOffDrain;

        if (style == CombatStyle.RANGED) {
            if (prayLvl >= 74) {
                maxOffensive = "Rigour (+23% Str, +20% Acc)";
                maxDmg = 1.23; maxAcc = 1.20; baseOffDrain = 40.0;
            } else if (prayLvl >= 44) {
                maxOffensive = "Eagle Eye (+15% Str, +15% Acc)";
                maxDmg = 1.15; maxAcc = 1.15; baseOffDrain = 20.0;
            } else {
                maxOffensive = "Hawk Eye (+10% Str, +10% Acc)";
                maxDmg = 1.10; maxAcc = 1.10; baseOffDrain = 10.0;
            }
        } else if (style == CombatStyle.MAGIC) {
            if (prayLvl >= 77) {
                maxOffensive = "Augury (+25% Acc, +25% Def)";
                maxDmg = 1.04; maxAcc = 1.25; baseOffDrain = 40.0;
            } else if (prayLvl >= 45) {
                maxOffensive = "Mystic Might (+15% Acc)";
                maxDmg = 1.0; maxAcc = 1.15; baseOffDrain = 20.0;
            } else {
                maxOffensive = "Mystic Lore (+10% Acc)";
                maxDmg = 1.0; maxAcc = 1.10; baseOffDrain = 10.0;
            }
        } else {
            if (prayLvl >= 70) {
                maxOffensive = "Piety (+23% Str, +20% Acc, +25% Def)";
                maxDmg = 1.23; maxAcc = 1.20; baseOffDrain = 40.0;
            } else if (prayLvl >= 60) {
                maxOffensive = "Chivalry (+18% Str, +15% Acc, +20% Def)";
                maxDmg = 1.18; maxAcc = 1.15; baseOffDrain = 40.0;
            } else if (prayLvl >= 31) {
                maxOffensive = "Ultimate Strength (+15% Str)";
                maxDmg = 1.15; maxAcc = 1.0; baseOffDrain = 20.0;
            } else {
                maxOffensive = "Superhuman Strength (+10% Str)";
                maxDmg = 1.10; maxAcc = 1.0; baseOffDrain = 10.0;
            }
        }

        // Full Power Plan
        double totalBaseDrainFull = baseOffDrain + 20.0; // Offensive + Defensive overhead
        double drainPerMinFull = totalBaseDrainFull / (1.0 + (totalPrayerBonus / 30.0));
        double potsPerHourFull = (drainPerMinFull * 60.0) / (double) restorePerPot;
        long prayCostFull = (long) (potsPerHourFull * prayPotPrice);
        long potCostFull = (divineCombatPrice * 3); // ~3 pots per hour for divine (1 dose per 5 min = 12 doses = 3 pots)

        plans.add(new PrayerPlan(
                "Maximum Power & DPS",
                maxOffensive,
                defOverhead,
                maxDmg, maxAcc,
                drainPerMinFull,
                potsPerHourFull,
                prayCostFull,
                "Divine Potion (Maintained +15% stats)",
                potCostFull,
                Arrays.asList("Highest kill speed and accuracy.", "Drinks Divine potions every 5 minutes.", "High prayer consumption.")
        ));

        // 2. Balanced / Value Plan (15% Prayer + Overhead + Regular Super Combat)
        String balOffensive = (prayLvl >= 31) ? "15% Damage Prayer" : "10% Damage Prayer";
        double balBaseDrain = 20.0 + 20.0;
        double drainPerMinBal = balBaseDrain / (1.0 + (totalPrayerBonus / 30.0));
        double potsPerHourBal = (drainPerMinBal * 60.0) / (double) restorePerPot;
        long prayCostBal = (long) (potsPerHourBal * prayPotPrice);
        long potCostBal = superCombatPrice * 2;

        plans.add(new PrayerPlan(
                "Balanced & Value",
                balOffensive,
                defOverhead,
                1.15, 1.10,
                drainPerMinBal,
                potsPerHourBal,
                prayCostBal,
                "Regular Super Potion",
                potCostBal,
                Arrays.asList("30% lower prayer potion consumption than Piety/Rigour.", "Drinks standard potions every 7-10 minutes.")
        ));

        // 3. Ultra-Conservative / Camping Plan (Defensive Overhead Only)
        double drainPerMinCons = 20.0 / (1.0 + (totalPrayerBonus / 30.0));
        double potsPerHourCons = (drainPerMinCons * 60.0) / (double) restorePerPot;
        long prayCostCons = (long) (potsPerHourCons * prayPotPrice);

        plans.add(new PrayerPlan(
                "Sustain & Camping",
                "None (Conserve Prayer)",
                defOverhead,
                1.0, 1.0,
                drainPerMinCons,
                potsPerHourCons,
                prayCostCons,
                "Combat Potion",
                potCostBal,
                Arrays.asList("No offensive prayer drain. Minimizes trip banking.", "Triples trip length at the cost of ~15% DPS.")
        ));

        return plans;
    }
}
