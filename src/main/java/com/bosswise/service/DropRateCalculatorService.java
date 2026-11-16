package com.bosswise.service;

import com.bosswise.model.DropRateCalculation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DropRateCalculatorService {
    public static class PresetDrop {
        private final String bossName;
        private final String itemName;
        private final double probability;
        private final int guaranteedThreshold;
        private final String mechanics;

        public PresetDrop(String bossName, String itemName, double probability, int guaranteedThreshold, String mechanics) {
            this.bossName = bossName;
            this.itemName = itemName;
            this.probability = probability;
            this.guaranteedThreshold = guaranteedThreshold;
            this.mechanics = mechanics;
        }

        public String getBossName() { return bossName; }
        public String getItemName() { return itemName; }
        public double getProbability() { return probability; }
        public int getGuaranteedThreshold() { return guaranteedThreshold; }
        public String getMechanics() { return mechanics; }

        @Override
        public String toString() {
            return bossName + " - " + itemName;
        }
    }

    private final List<PresetDrop> presets = new ArrayList<>();

    @Inject
    public DropRateCalculatorService() {
        initPresets();
    }

    private void initPresets() {
        presets.add(new PresetDrop("Vorkath", "Vorkath's Head", 1.0 / 50.0, 50, "Guaranteed on exactly 50 KC if not received earlier (pity threshold)."));
        presets.add(new PresetDrop("Vorkath", "Draconic Visage", 1.0 / 5000.0, -1, "Standard independent rare drop table roll."));
        presets.add(new PresetDrop("Corrupted Gauntlet", "Enhanced Crystal Weapon Seed", 1.0 / 400.0, -1, "High value unique drop (Bowfa / Blade of Saeldor)."));
        presets.add(new PresetDrop("Zulrah", "Tanzanite Fang (Blowpipe)", 1.0 / 256.0, -1, "1/512 per roll with 2 independent item rolls per kill."));
        presets.add(new PresetDrop("Zulrah", "Magic Fang", 1.0 / 256.0, -1, "1/512 per roll with 2 independent item rolls per kill."));
        presets.add(new PresetDrop("Phantom Muspah", "Venator Shard (1 of 5)", 1.0 / 100.0, -1, "5 shards required to craft the full Venator Bow."));
        presets.add(new PresetDrop("General Graardor", "Bandos Chestplate", 1.0 / 384.0, -1, "1/384 from boss, 1/5376 from bodyguards."));
        presets.add(new PresetDrop("General Graardor", "Bandos Tassets", 1.0 / 384.0, -1, "1/384 from boss, 1/5376 from bodyguards."));
        presets.add(new PresetDrop("General Graardor", "Bandos Hilt", 1.0 / 508.0, -1, "1/508 from boss."));
        presets.add(new PresetDrop("Kalphite Queen", "Kq Head", 1.0 / 128.0, 256, "Guaranteed at 256 KC for Desert Elite diary."));
        presets.add(new PresetDrop("Vardorvis", "Ultor Vestige (DT2)", 1.0 / 1088.0, -1, "Requires hitting the unique roll 3 times before dropping."));
        presets.add(new PresetDrop("Duke Sucellus", "Magus Vestige (DT2)", 1.0 / 720.0, -1, "Requires hitting the unique roll 3 times before dropping."));
        presets.add(new PresetDrop("The Whisperer", "Bellator Vestige (DT2)", 1.0 / 512.0, -1, "Requires hitting the unique roll 3 times before dropping."));
        presets.add(new PresetDrop("The Leviathan", "Venator Vestige (DT2)", 1.0 / 768.0, -1, "Requires hitting the unique roll 3 times before dropping."));
        presets.add(new PresetDrop("Corporeal Beast", "Elysian Sigil", 1.0 / 4095.0, -1, "Pinnacle rare shield sigil."));
        presets.add(new PresetDrop("Abyssal Demon", "Abyssal Whip", 1.0 / 512.0, -1, "Classic slayer drop."));
        presets.add(new PresetDrop("Superior Monsters", "Imbued Heart", 1.0 / 350.0, -1, "Scales by superior slayer monster combat level."));
        presets.add(new PresetDrop("General Bosses", "Boss Pet (Standard)", 1.0 / 3000.0, -1, "Cosmetic prestige pet follower."));
    }

    public List<PresetDrop> getPresets() {
        return Collections.unmodifiableList(presets);
    }

    public DropRateCalculation calculatePreset(PresetDrop preset) {
        if (preset == null) {
            return calculateCustom("Custom Item", "Custom Boss", 500, -1);
        }
        return new DropRateCalculation(
                preset.getItemName(),
                preset.getBossName(),
                preset.getProbability(),
                preset.getGuaranteedThreshold(),
                preset.getMechanics()
        );
    }

    public DropRateCalculation calculateCustom(String itemName, String bossName, int oneInDenominator, int guaranteedThreshold) {
        int safeDenom = Math.max(1, oneInDenominator);
        double prob = 1.0 / (double) safeDenom;
        String desc = guaranteedThreshold > 0
                ? "Guaranteed drop at " + guaranteedThreshold + " KC."
                : "Standard independent Poisson / Binomial distribution.";
        return new DropRateCalculation(itemName, bossName, prob, guaranteedThreshold, desc);
    }
}
