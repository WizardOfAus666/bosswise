package com.bosswise.model.strategy;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;

public class StrategyMethod {
    private final String id;
    private final String title;
    private final StrategyTier tier;
    private final CombatStyle combatStyle;
    private final Map<Skill, Integer> requiredSkills;
    private final Map<Skill, Integer> recommendedSkills;
    private final List<String> requiredQuests;
    private final String overheadPrayer;
    private final String offensivePrayer;
    private final List<String> potionSetup;
    private final List<String> inventoryHighlights;
    private final long estimatedCostGp;
    private final String whyThisWorks;
    private final String detailedInstructions;
    private final List<String> keyItems;

    // Structured 11-slot gear mapping & recommendation distinction
    private final Map<EquipmentSlot, String> gearSetup;
    private final String recommendationType; // "WIKI_RECOMMENDED", "BOSSWISE_OPTIMISED", "BUDGET_ALTERNATIVE", "PLAYER_OWNED"
    private final String wikiSourceUrl;

    public StrategyMethod(
            String id,
            String title,
            StrategyTier tier,
            CombatStyle combatStyle,
            Map<Skill, Integer> requiredSkills,
            Map<Skill, Integer> recommendedSkills,
            List<String> requiredQuests,
            String overheadPrayer,
            String offensivePrayer,
            List<String> potionSetup,
            List<String> inventoryHighlights,
            long estimatedCostGp,
            String whyThisWorks,
            String detailedInstructions,
            List<String> keyItems) {

        this(id, title, tier, combatStyle, requiredSkills, recommendedSkills, requiredQuests,
                overheadPrayer, offensivePrayer, potionSetup, inventoryHighlights, estimatedCostGp,
                whyThisWorks, detailedInstructions, keyItems, Collections.emptyMap(), "WIKI_RECOMMENDED", "");
    }

    public StrategyMethod(
            String id,
            String title,
            StrategyTier tier,
            CombatStyle combatStyle,
            Map<Skill, Integer> requiredSkills,
            Map<Skill, Integer> recommendedSkills,
            List<String> requiredQuests,
            String overheadPrayer,
            String offensivePrayer,
            List<String> potionSetup,
            List<String> inventoryHighlights,
            long estimatedCostGp,
            String whyThisWorks,
            String detailedInstructions,
            List<String> keyItems,
            Map<EquipmentSlot, String> gearSetup,
            String recommendationType,
            String wikiSourceUrl) {

        this.id = id != null ? id : "";
        this.title = title != null ? title : "";
        this.tier = tier != null ? tier : StrategyTier.MID_GAME;
        this.combatStyle = combatStyle != null ? combatStyle : CombatStyle.MELEE;
        this.requiredSkills = requiredSkills != null ? Collections.unmodifiableMap(new EnumMap<>(requiredSkills)) : Collections.emptyMap();
        this.recommendedSkills = recommendedSkills != null ? Collections.unmodifiableMap(new EnumMap<>(recommendedSkills)) : Collections.emptyMap();
        this.requiredQuests = requiredQuests != null ? Collections.unmodifiableList(new ArrayList<>(requiredQuests)) : Collections.emptyList();
        this.overheadPrayer = overheadPrayer != null ? overheadPrayer : "None";
        this.offensivePrayer = offensivePrayer != null ? offensivePrayer : "None";
        this.potionSetup = potionSetup != null ? Collections.unmodifiableList(new ArrayList<>(potionSetup)) : Collections.emptyList();
        this.inventoryHighlights = inventoryHighlights != null ? Collections.unmodifiableList(new ArrayList<>(inventoryHighlights)) : Collections.emptyList();
        this.estimatedCostGp = estimatedCostGp;
        this.whyThisWorks = whyThisWorks != null ? whyThisWorks : "";
        this.detailedInstructions = detailedInstructions != null ? detailedInstructions : "";
        this.keyItems = keyItems != null ? Collections.unmodifiableList(new ArrayList<>(keyItems)) : Collections.emptyList();

        this.gearSetup = (gearSetup != null && !gearSetup.isEmpty()) ?
                Collections.unmodifiableMap(new EnumMap<>(gearSetup)) : Collections.emptyMap();
        this.recommendationType = recommendationType != null ? recommendationType : "WIKI_RECOMMENDED";
        this.wikiSourceUrl = wikiSourceUrl != null ? wikiSourceUrl : "";
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public StrategyTier getTier() { return tier; }
    public CombatStyle getCombatStyle() { return combatStyle; }
    public Map<Skill, Integer> getRequiredSkills() { return requiredSkills; }
    public Map<Skill, Integer> getRecommendedSkills() { return recommendedSkills; }
    public List<String> getRequiredQuests() { return requiredQuests; }
    public String getOverheadPrayer() { return overheadPrayer; }
    public String getOffensivePrayer() { return offensivePrayer; }
    public List<String> getPotionSetup() { return potionSetup; }
    public List<String> getInventoryHighlights() { return inventoryHighlights; }
    public long getEstimatedCostGp() { return estimatedCostGp; }
    public String getWhyThisWorks() { return whyThisWorks; }
    public String getDetailedInstructions() { return detailedInstructions; }
    public List<String> getKeyItems() { return keyItems; }

    public Map<EquipmentSlot, String> getGearSetup() { return gearSetup; }
    public String getRecommendationType() { return recommendationType; }
    public String getWikiSourceUrl() { return wikiSourceUrl; }

    public String getItem(EquipmentSlot slot) {
        return gearSetup.get(slot);
    }

    public static Builder builder(String id, String title, StrategyTier tier, CombatStyle style) {
        return new Builder(id, title, tier, style);
    }

    public static class Builder {
        private final String id;
        private final String title;
        private final StrategyTier tier;
        private final CombatStyle combatStyle;
        private final Map<Skill, Integer> requiredSkills = new EnumMap<>(Skill.class);
        private final Map<Skill, Integer> recommendedSkills = new EnumMap<>(Skill.class);
        private final List<String> requiredQuests = new ArrayList<>();
        private String overheadPrayer = "None";
        private String offensivePrayer = "None";
        private final List<String> potionSetup = new ArrayList<>();
        private final List<String> inventoryHighlights = new ArrayList<>();
        private long estimatedCostGp = 0L;
        private String whyThisWorks = "";
        private String detailedInstructions = "";
        private final List<String> keyItems = new ArrayList<>();
        private final Map<EquipmentSlot, String> gearSetup = new EnumMap<>(EquipmentSlot.class);
        private String recommendationType = "WIKI_RECOMMENDED";
        private String wikiSourceUrl = "";

        public Builder(String id, String title, StrategyTier tier, CombatStyle style) {
            this.id = id;
            this.title = title;
            this.tier = tier;
            this.combatStyle = style;
        }

        public Builder reqSkill(Skill skill, int level) {
            this.requiredSkills.put(skill, level);
            return this;
        }

        public Builder recSkill(Skill skill, int level) {
            this.recommendedSkills.put(skill, level);
            return this;
        }

        public Builder reqQuest(String quest) {
            this.requiredQuests.add(quest);
            return this;
        }

        public Builder prayers(String overhead, String offensive) {
            this.overheadPrayer = overhead;
            this.offensivePrayer = offensive;
            return this;
        }

        public Builder addPotion(String potion) {
            this.potionSetup.add(potion);
            return this;
        }

        public Builder addInventoryHighlight(String item) {
            this.inventoryHighlights.add(item);
            return this;
        }

        public Builder cost(long costGp) {
            this.estimatedCostGp = costGp;
            return this;
        }

        public Builder whyThisWorks(String explanation) {
            this.whyThisWorks = explanation;
            return this;
        }

        public Builder instructions(String instructions) {
            this.detailedInstructions = instructions;
            return this;
        }

        public Builder addKeyItem(String item) {
            this.keyItems.add(item);
            return this;
        }

        public Builder recommendationType(String type) {
            this.recommendationType = type;
            return this;
        }

        public Builder wikiSourceUrl(String url) {
            this.wikiSourceUrl = url;
            return this;
        }

        public Builder gear(EquipmentSlot slot, String itemName) {
            if (slot != null && itemName != null && !itemName.trim().isEmpty()) {
                this.gearSetup.put(slot, itemName.trim());
            }
            return this;
        }

        public Builder weapon(String item) { return gear(EquipmentSlot.WEAPON, item); }
        public Builder shield(String item) { return gear(EquipmentSlot.SHIELD, item); }
        public Builder head(String item) { return gear(EquipmentSlot.HEAD, item); }
        public Builder body(String item) { return gear(EquipmentSlot.BODY, item); }
        public Builder legs(String item) { return gear(EquipmentSlot.LEGS, item); }
        public Builder cape(String item) { return gear(EquipmentSlot.CAPE, item); }
        public Builder amulet(String item) { return gear(EquipmentSlot.AMULET, item); }
        public Builder gloves(String item) { return gear(EquipmentSlot.GLOVES, item); }
        public Builder boots(String item) { return gear(EquipmentSlot.BOOTS, item); }
        public Builder ring(String item) { return gear(EquipmentSlot.RING, item); }
        public Builder ammo(String item) { return gear(EquipmentSlot.AMMO, item); }
        public Builder specWeapon(String item) { return gear(EquipmentSlot.SPEC_WEAPON, item); }

        public StrategyMethod build() {
            return new StrategyMethod(
                    id, title, tier, combatStyle,
                    requiredSkills, recommendedSkills, requiredQuests,
                    overheadPrayer, offensivePrayer, potionSetup,
                    inventoryHighlights, estimatedCostGp, whyThisWorks,
                    detailedInstructions, keyItems, gearSetup, recommendationType, wikiSourceUrl
            );
        }
    }
}
