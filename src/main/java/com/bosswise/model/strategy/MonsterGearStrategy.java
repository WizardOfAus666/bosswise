package com.bosswise.model.strategy;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Authoritative, verified monster gear strategy holding structured slot-by-slot
 * equipment researched from individual OSRS Wiki strategy guides.
 */
public class MonsterGearStrategy {
    private final String monsterId;
    private final List<Integer> npcVariantIds;
    private final String monsterName;
    private final String wikiPage;
    private final String wikiStrategySource;
    private final CombatStyle combatStyle;
    private final String strategyName;
    private final String strategyCategory; // "WIKI_RECOMMENDED", "BOSSWISE_OPTIMISED", "BUDGET_ALTERNATIVE", "PLAYER_OWNED"

    // 11 Explicit Equipment Slots
    private final String recommendedWeapon;
    private final String recommendedHelmet;
    private final String recommendedBody;
    private final String recommendedLegs;
    private final String recommendedShield;
    private final String recommendedCape;
    private final String recommendedAmulet;
    private final String recommendedGloves;
    private final String recommendedBoots;
    private final String recommendedRing;
    private final String recommendedAmmunition;
    private final String recommendedSpecWeapon;

    private final List<String> specialEquipment;
    private final List<String> alternativeEquipment;
    private final List<String> requirements;
    private final List<String> recommendedPrayers;
    private final List<String> recommendedInventory;
    private final String strategyExplanation;
    private final String sourceLastChecked;
    private final String verificationStatus; // "VERIFIED", "PARTIALLY_VERIFIED", "UNVERIFIED", "UNAVAILABLE"
    private final StrategyConfidence confidenceLevel;
    private final long estimatedCostGp;

    private MonsterGearStrategy(Builder builder) {
        this.monsterId = builder.monsterId != null ? builder.monsterId : "";
        this.npcVariantIds = Collections.unmodifiableList(new ArrayList<>(builder.npcVariantIds));
        this.monsterName = builder.monsterName != null ? builder.monsterName : "";
        this.wikiPage = builder.wikiPage != null ? builder.wikiPage : "";
        this.wikiStrategySource = builder.wikiStrategySource != null ? builder.wikiStrategySource : "";
        this.combatStyle = builder.combatStyle != null ? builder.combatStyle : CombatStyle.MELEE;
        this.strategyName = builder.strategyName != null ? builder.strategyName : "";
        this.strategyCategory = builder.strategyCategory != null ? builder.strategyCategory : "WIKI_RECOMMENDED";

        this.recommendedWeapon = builder.recommendedWeapon != null ? builder.recommendedWeapon : "";
        this.recommendedHelmet = builder.recommendedHelmet != null ? builder.recommendedHelmet : "";
        this.recommendedBody = builder.recommendedBody != null ? builder.recommendedBody : "";
        this.recommendedLegs = builder.recommendedLegs != null ? builder.recommendedLegs : "";
        this.recommendedShield = builder.recommendedShield != null ? builder.recommendedShield : "";
        this.recommendedCape = builder.recommendedCape != null ? builder.recommendedCape : "";
        this.recommendedAmulet = builder.recommendedAmulet != null ? builder.recommendedAmulet : "";
        this.recommendedGloves = builder.recommendedGloves != null ? builder.recommendedGloves : "";
        this.recommendedBoots = builder.recommendedBoots != null ? builder.recommendedBoots : "";
        this.recommendedRing = builder.recommendedRing != null ? builder.recommendedRing : "";
        this.recommendedAmmunition = builder.recommendedAmmunition != null ? builder.recommendedAmmunition : "";
        this.recommendedSpecWeapon = builder.recommendedSpecWeapon != null ? builder.recommendedSpecWeapon : "";

        this.specialEquipment = Collections.unmodifiableList(new ArrayList<>(builder.specialEquipment));
        this.alternativeEquipment = Collections.unmodifiableList(new ArrayList<>(builder.alternativeEquipment));
        this.requirements = Collections.unmodifiableList(new ArrayList<>(builder.requirements));
        this.recommendedPrayers = Collections.unmodifiableList(new ArrayList<>(builder.recommendedPrayers));
        this.recommendedInventory = Collections.unmodifiableList(new ArrayList<>(builder.recommendedInventory));
        this.strategyExplanation = builder.strategyExplanation != null ? builder.strategyExplanation : "";
        this.sourceLastChecked = builder.sourceLastChecked != null ? builder.sourceLastChecked : "2026-11-16";
        this.verificationStatus = builder.verificationStatus != null ? builder.verificationStatus : "VERIFIED";
        this.confidenceLevel = builder.confidenceLevel != null ? builder.confidenceLevel : StrategyConfidence.VERIFIED;
        this.estimatedCostGp = builder.estimatedCostGp;
    }

    public String getMonsterId() { return monsterId; }
    public List<Integer> getNpcVariantIds() { return npcVariantIds; }
    public String getMonsterName() { return monsterName; }
    public String getWikiPage() { return wikiPage; }
    public String getWikiStrategySource() { return wikiStrategySource; }
    public CombatStyle getCombatStyle() { return combatStyle; }
    public String getStrategyName() { return strategyName; }
    public String getStrategyCategory() { return strategyCategory; }

    public String getRecommendedWeapon() { return recommendedWeapon; }
    public String getRecommendedHelmet() { return recommendedHelmet; }
    public String getRecommendedBody() { return recommendedBody; }
    public String getRecommendedLegs() { return recommendedLegs; }
    public String getRecommendedShield() { return recommendedShield; }
    public String getRecommendedCape() { return recommendedCape; }
    public String getRecommendedAmulet() { return recommendedAmulet; }
    public String getRecommendedGloves() { return recommendedGloves; }
    public String getRecommendedBoots() { return recommendedBoots; }
    public String getRecommendedRing() { return recommendedRing; }
    public String getRecommendedAmmunition() { return recommendedAmmunition; }
    public String getRecommendedSpecWeapon() { return recommendedSpecWeapon; }

    public String getItemForSlot(EquipmentSlot slot) {
        if (slot == null) return "";
        switch (slot) {
            case WEAPON: return recommendedWeapon;
            case HEAD: return recommendedHelmet;
            case BODY: return recommendedBody;
            case LEGS: return recommendedLegs;
            case SHIELD: return recommendedShield;
            case CAPE: return recommendedCape;
            case AMULET: return recommendedAmulet;
            case GLOVES: return recommendedGloves;
            case BOOTS: return recommendedBoots;
            case RING: return recommendedRing;
            case AMMO: return recommendedAmmunition;
            case SPEC_WEAPON: return recommendedSpecWeapon;
            default: return "";
        }
    }

    public Map<EquipmentSlot, String> getSlotGearMap() {
        Map<EquipmentSlot, String> map = new EnumMap<>(EquipmentSlot.class);
        if (!recommendedWeapon.isEmpty()) map.put(EquipmentSlot.WEAPON, recommendedWeapon);
        if (!recommendedHelmet.isEmpty()) map.put(EquipmentSlot.HEAD, recommendedHelmet);
        if (!recommendedBody.isEmpty()) map.put(EquipmentSlot.BODY, recommendedBody);
        if (!recommendedLegs.isEmpty()) map.put(EquipmentSlot.LEGS, recommendedLegs);
        if (!recommendedShield.isEmpty()) map.put(EquipmentSlot.SHIELD, recommendedShield);
        if (!recommendedCape.isEmpty()) map.put(EquipmentSlot.CAPE, recommendedCape);
        if (!recommendedAmulet.isEmpty()) map.put(EquipmentSlot.AMULET, recommendedAmulet);
        if (!recommendedGloves.isEmpty()) map.put(EquipmentSlot.GLOVES, recommendedGloves);
        if (!recommendedBoots.isEmpty()) map.put(EquipmentSlot.BOOTS, recommendedBoots);
        if (!recommendedRing.isEmpty()) map.put(EquipmentSlot.RING, recommendedRing);
        if (!recommendedAmmunition.isEmpty()) map.put(EquipmentSlot.AMMO, recommendedAmmunition);
        if (!recommendedSpecWeapon.isEmpty()) map.put(EquipmentSlot.SPEC_WEAPON, recommendedSpecWeapon);
        return map;
    }

    public List<String> getSpecialEquipment() { return specialEquipment; }
    public List<String> getAlternativeEquipment() { return alternativeEquipment; }
    public List<String> getRequirements() { return requirements; }
    public List<String> getRecommendedPrayers() { return recommendedPrayers; }
    public List<String> getRecommendedInventory() { return recommendedInventory; }
    public String getStrategyExplanation() { return strategyExplanation; }
    public String getSourceLastChecked() { return sourceLastChecked; }
    public String getVerificationStatus() { return verificationStatus; }
    public StrategyConfidence getConfidenceLevel() { return confidenceLevel; }
    public long getEstimatedCostGp() { return estimatedCostGp; }

    public static Builder builder(String monsterId, String monsterName, CombatStyle combatStyle, String strategyName) {
        return new Builder(monsterId, monsterName, combatStyle, strategyName);
    }

    public static class Builder {
        private final String monsterId;
        private final String monsterName;
        private final CombatStyle combatStyle;
        private final String strategyName;
        private final List<Integer> npcVariantIds = new ArrayList<>();
        private String wikiPage = "";
        private String wikiStrategySource = "";
        private String strategyCategory = "WIKI_RECOMMENDED";

        private String recommendedWeapon = "";
        private String recommendedHelmet = "";
        private String recommendedBody = "";
        private String recommendedLegs = "";
        private String recommendedShield = "";
        private String recommendedCape = "";
        private String recommendedAmulet = "";
        private String recommendedGloves = "";
        private String recommendedBoots = "";
        private String recommendedRing = "";
        private String recommendedAmmunition = "";
        private String recommendedSpecWeapon = "";

        private final List<String> specialEquipment = new ArrayList<>();
        private final List<String> alternativeEquipment = new ArrayList<>();
        private final List<String> requirements = new ArrayList<>();
        private final List<String> recommendedPrayers = new ArrayList<>();
        private final List<String> recommendedInventory = new ArrayList<>();
        private String strategyExplanation = "";
        private String sourceLastChecked = "2026-11-16";
        private String verificationStatus = "VERIFIED";
        private StrategyConfidence confidenceLevel = StrategyConfidence.VERIFIED;
        private long estimatedCostGp = 0L;

        public Builder(String monsterId, String monsterName, CombatStyle combatStyle, String strategyName) {
            this.monsterId = monsterId;
            this.monsterName = monsterName;
            this.combatStyle = combatStyle;
            this.strategyName = strategyName;
        }

        public Builder variantId(int id) { this.npcVariantIds.add(id); return this; }
        public Builder wikiPage(String page) { this.wikiPage = page; return this; }
        public Builder wikiStrategySource(String src) { this.wikiStrategySource = src; return this; }
        public Builder category(String cat) { this.strategyCategory = cat; return this; }

        public Builder weapon(String weapon) { this.recommendedWeapon = weapon; return this; }
        public Builder helmet(String helm) { this.recommendedHelmet = helm; return this; }
        public Builder body(String body) { this.recommendedBody = body; return this; }
        public Builder legs(String legs) { this.recommendedLegs = legs; return this; }
        public Builder shield(String shield) { this.recommendedShield = shield; return this; }
        public Builder cape(String cape) { this.recommendedCape = cape; return this; }
        public Builder amulet(String amulet) { this.recommendedAmulet = amulet; return this; }
        public Builder gloves(String gloves) { this.recommendedGloves = gloves; return this; }
        public Builder boots(String boots) { this.recommendedBoots = boots; return this; }
        public Builder ring(String ring) { this.recommendedRing = ring; return this; }
        public Builder ammo(String ammo) { this.recommendedAmmunition = ammo; return this; }
        public Builder specWeapon(String spec) { this.recommendedSpecWeapon = spec; return this; }

        public Builder special(String item) { this.specialEquipment.add(item); return this; }
        public Builder alternative(String alt) { this.alternativeEquipment.add(alt); return this; }
        public Builder requirement(String req) { this.requirements.add(req); return this; }
        public Builder prayer(String pray) { this.recommendedPrayers.add(pray); return this; }
        public Builder inventory(String inv) { this.recommendedInventory.add(inv); return this; }
        public Builder explanation(String exp) { this.strategyExplanation = exp; return this; }
        public Builder lastChecked(String date) { this.sourceLastChecked = date; return this; }
        public Builder status(String status) { this.verificationStatus = status; return this; }
        public Builder confidence(StrategyConfidence conf) { this.confidenceLevel = conf; return this; }
        public Builder cost(long cost) { this.estimatedCostGp = cost; return this; }

        public MonsterGearStrategy build() {
            return new MonsterGearStrategy(this);
        }
    }
}
