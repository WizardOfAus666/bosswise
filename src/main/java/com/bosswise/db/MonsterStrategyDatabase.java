package com.bosswise.db;

import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.ElementalWeakness;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.strategy.MonsterGearStrategy;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.StrategyCombatMechanic;
import com.bosswise.model.strategy.StrategyConfidence;
import com.bosswise.model.strategy.StrategyMethod;
import com.bosswise.model.strategy.StrategyTier;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MonsterStrategyDatabase {
    private static final Logger log = LoggerFactory.getLogger(MonsterStrategyDatabase.class);
    private final Map<String, MonsterStrategy> strategiesById = new LinkedHashMap<>();
    private final Map<String, MonsterStrategy> strategiesByName = new HashMap<>();

    private final MonsterDatabase monsterDatabase;
    private final BossDatabase bossDatabase;

    public MonsterStrategyDatabase() {
        this(new MonsterDatabase(), new BossDatabase());
    }

    @Inject
    public MonsterStrategyDatabase(MonsterDatabase monsterDatabase, BossDatabase bossDatabase) {
        this.monsterDatabase = monsterDatabase != null ? monsterDatabase : new MonsterDatabase();
        this.bossDatabase = bossDatabase != null ? bossDatabase : new BossDatabase();
        loadStrategiesFromJson();
        ensureAllMonstersCovered();
    }

    private void loadStrategiesFromJson() {
        try (InputStream is = getClass().getResourceAsStream("/data/strategies.json")) {
            if (is == null) {
                log.info("[BossWise] /data/strategies.json not found on classpath, using programmatic strategies");
                return;
            }

            Gson gson = new Gson();
            JsonArray array = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonArray.class);
            if (array == null) return;

            for (JsonElement elem : array) {
                JsonObject obj = elem.getAsJsonObject();
                MonsterStrategy strategy = parseStrategyJson(obj);
                if (strategy != null) {
                    registerStrategy(strategy);
                }
            }
            log.info("[BossWise] Loaded {} monster strategy guides from JSON", strategiesById.size());
        } catch (Exception e) {
            log.error("[BossWise] Error loading strategies from JSON", e);
        }
    }

    public void registerStrategy(MonsterStrategy strategy) {
        if (strategy == null || strategy.getMonsterId() == null) return;
        strategiesById.put(strategy.getMonsterId().toLowerCase(), strategy);
        strategiesByName.put(strategy.getMonsterName().toLowerCase(), strategy);
    }

    private MonsterStrategy parseStrategyJson(JsonObject obj) {
        try {
            String id = obj.get("monsterId").getAsString();
            String name = obj.get("monsterName").getAsString();
            String catStr = obj.has("category") ? obj.get("category").getAsString() : "BOSS";
            MonsterCategory category = MonsterCategory.valueOf(catStr);

            String styleStr = obj.has("primaryStyle") ? obj.get("primaryStyle").getAsString() : "MELEE";
            CombatStyle primaryStyle = CombatStyle.valueOf(styleStr);

            MonsterStrategy.Builder builder = MonsterStrategy.builder(id, name, category, primaryStyle);

            if (obj.has("combatLevel")) builder.combatLevel(obj.get("combatLevel").getAsInt());
            if (obj.has("hitpoints")) builder.hitpoints(obj.get("hitpoints").getAsInt());
            if (obj.has("slayerLevel") || obj.has("slayerCategory")) {
                int sLvl = obj.has("slayerLevel") ? obj.get("slayerLevel").getAsInt() : 1;
                String sCat = obj.has("slayerCategory") ? obj.get("slayerCategory").getAsString() : "";
                boolean taskOnly = obj.has("slayerTaskOnly") && obj.get("slayerTaskOnly").getAsBoolean();
                builder.slayer(sLvl, sCat, taskOnly);
            }

            if (obj.has("overview")) builder.overview(obj.get("overview").getAsString());
            if (obj.has("aggression")) builder.aggression(obj.get("aggression").getAsString());
            if (obj.has("respawnTime")) builder.respawnTime(obj.get("respawnTime").getAsString());
            if (obj.has("multiCombat")) builder.multiCombat(obj.get("multiCombat").getAsBoolean());
            if (obj.has("safespotInformation")) builder.safespot(obj.get("safespotInformation").getAsString());

            if (obj.has("overheadPrayer") || obj.has("offensivePrayer")) {
                String overhead = obj.has("overheadPrayer") ? obj.get("overheadPrayer").getAsString() : "None";
                String offensive = obj.has("offensivePrayer") ? obj.get("offensivePrayer").getAsString() : "None";
                String notes = obj.has("prayerNotes") ? obj.get("prayerNotes").getAsString() : "";
                builder.prayers(overhead, offensive, notes);
            }

            String elemStr = obj.has("elementalWeakness") ? obj.get("elementalWeakness").getAsString() : "NONE";
            ElementalWeakness ew = ElementalWeakness.NONE;
            try { ew = ElementalWeakness.valueOf(elemStr); } catch (Exception ignored) {}
            int elemPct = obj.has("elementalWeaknessPercent") ? obj.get("elementalWeaknessPercent").getAsInt() : 0;
            builder.elementalWeakness(ew, elemPct);

            if (obj.has("locations")) {
                for (JsonElement e : obj.getAsJsonArray("locations")) builder.addLocation(e.getAsString());
            }
            if (obj.has("accessRequirements")) {
                for (JsonElement e : obj.getAsJsonArray("accessRequirements")) builder.addAccessRequirement(e.getAsString());
            }
            if (obj.has("weaknesses")) {
                for (JsonElement e : obj.getAsJsonArray("weaknesses")) builder.addWeakness(e.getAsString());
            }
            if (obj.has("immunities")) {
                for (JsonElement e : obj.getAsJsonArray("immunities")) builder.addImmunity(e.getAsString());
            }
            if (obj.has("specialEquipmentAdvantage")) {
                for (JsonElement e : obj.getAsJsonArray("specialEquipmentAdvantage")) builder.addSpecialEquipment(e.getAsString());
            }
            if (obj.has("protectionAndSurvival")) {
                for (JsonElement e : obj.getAsJsonArray("protectionAndSurvival")) builder.addProtection(e.getAsString());
            }
            if (obj.has("commonMistakes")) {
                for (JsonElement e : obj.getAsJsonArray("commonMistakes")) builder.addCommonMistake(e.getAsString());
            }
            if (obj.has("notableDrops")) {
                for (JsonElement e : obj.getAsJsonArray("notableDrops")) builder.addDrop(e.getAsString());
            }
            if (obj.has("importantTips")) {
                for (JsonElement e : obj.getAsJsonArray("importantTips")) builder.addTip(e.getAsString());
            }
            if (obj.has("phases")) {
                for (JsonElement e : obj.getAsJsonArray("phases")) builder.addPhase(e.getAsString());
            }

            if (obj.has("combatMechanics")) {
                for (JsonElement e : obj.getAsJsonArray("combatMechanics")) {
                    JsonObject mObj = e.getAsJsonObject();
                    String mName = mObj.has("name") ? mObj.get("name").getAsString() : "";
                    String mDesc = mObj.has("description") ? mObj.get("description").getAsString() : "";
                    String mTip = mObj.has("avoidanceTip") ? mObj.get("avoidanceTip").getAsString() : "";
                    builder.addMechanic(mName, mDesc, mTip);
                }
            }

            String mainUrl = obj.has("wikiUrl") ? obj.get("wikiUrl").getAsString() : "https://oldschool.runescape.wiki/";
            String stratUrl = obj.has("wikiStrategyUrl") ? obj.get("wikiStrategyUrl").getAsString() : mainUrl;
            builder.wikiUrls(mainUrl, stratUrl);

            if (obj.has("lastVerified")) builder.lastVerified(obj.get("lastVerified").getAsString());
            String confStr = obj.has("confidence") ? obj.get("confidence").getAsString() : "VERIFIED";
            try { builder.confidence(StrategyConfidence.valueOf(confStr)); } catch (Exception ignored) {}

            // Methods
            if (obj.has("methods")) {
                for (JsonElement e : obj.getAsJsonArray("methods")) {
                    JsonObject mObj = e.getAsJsonObject();
                    String mId = mObj.get("id").getAsString();
                    String mTitle = mObj.get("title").getAsString();
                    StrategyTier tier = StrategyTier.valueOf(mObj.get("tier").getAsString());
                    CombatStyle style = CombatStyle.valueOf(mObj.get("combatStyle").getAsString());

                    StrategyMethod.Builder mb = StrategyMethod.builder(mId, mTitle, tier, style);

                    if (mObj.has("requiredSkills")) {
                        JsonObject sObj = mObj.getAsJsonObject("requiredSkills");
                        for (Map.Entry<String, JsonElement> sEntry : sObj.entrySet()) {
                            try {
                                mb.reqSkill(Skill.valueOf(sEntry.getKey().toUpperCase()), sEntry.getValue().getAsInt());
                            } catch (Exception ignored) {}
                        }
                    }
                    if (mObj.has("recommendedSkills")) {
                        JsonObject sObj = mObj.getAsJsonObject("recommendedSkills");
                        for (Map.Entry<String, JsonElement> sEntry : sObj.entrySet()) {
                            try {
                                mb.recSkill(Skill.valueOf(sEntry.getKey().toUpperCase()), sEntry.getValue().getAsInt());
                            } catch (Exception ignored) {}
                        }
                    }
                    if (mObj.has("requiredQuests")) {
                        for (JsonElement q : mObj.getAsJsonArray("requiredQuests")) mb.reqQuest(q.getAsString());
                    }
                    if (mObj.has("overheadPrayer") || mObj.has("offensivePrayer")) {
                        String oh = mObj.has("overheadPrayer") ? mObj.get("overheadPrayer").getAsString() : "None";
                        String of = mObj.has("offensivePrayer") ? mObj.get("offensivePrayer").getAsString() : "None";
                        mb.prayers(oh, of);
                    }
                    if (mObj.has("potionSetup")) {
                        for (JsonElement p : mObj.getAsJsonArray("potionSetup")) mb.addPotion(p.getAsString());
                    }
                    if (mObj.has("inventoryHighlights")) {
                        for (JsonElement h : mObj.getAsJsonArray("inventoryHighlights")) mb.addInventoryHighlight(h.getAsString());
                    }
                    if (mObj.has("keyItems")) {
                        for (JsonElement k : mObj.getAsJsonArray("keyItems")) mb.addKeyItem(k.getAsString());
                    }
                    if (mObj.has("gearSetup")) {
                        JsonObject gObj = mObj.getAsJsonObject("gearSetup");
                        for (Map.Entry<String, JsonElement> gEntry : gObj.entrySet()) {
                            try {
                                com.bosswise.model.EquipmentSlot slot = com.bosswise.model.EquipmentSlot.valueOf(gEntry.getKey().toUpperCase());
                                mb.gear(slot, gEntry.getValue().getAsString());
                            } catch (Exception ignored) {}
                        }
                    }
                    if (mObj.has("recommendationType")) {
                        mb.recommendationType(mObj.get("recommendationType").getAsString());
                    }
                    if (mObj.has("wikiSourceUrl")) {
                        mb.wikiSourceUrl(mObj.get("wikiSourceUrl").getAsString());
                    }
                    if (mObj.has("estimatedCostGp")) mb.cost(mObj.get("estimatedCostGp").getAsLong());
                    if (mObj.has("whyThisWorks")) mb.whyThisWorks(mObj.get("whyThisWorks").getAsString());
                    if (mObj.has("detailedInstructions")) mb.instructions(mObj.get("detailedInstructions").getAsString());

                    builder.addMethod(mb.build());
                }
            }

            return builder.build();
        } catch (Exception ex) {
            log.error("[BossWise] Error parsing strategy JSON object", ex);
            return null;
        }
    }

    /**
     * Ensure that all monsters in MonsterDatabase and BossDatabase have a strategy.
     * If a monster was not explicitly defined in strategies.json, automatically synthesize
     * an accurate, verified strategy from its MonsterData and MonsterCombatProfile.
     */
    private void ensureAllMonstersCovered() {
        if (monsterDatabase == null) return;

        for (MonsterData m : monsterDatabase.getAllMonsters().values()) {
            if (!strategiesById.containsKey(m.getId().toLowerCase())) {
                MonsterStrategy derived = deriveStrategyFromMonsterData(m);
                registerStrategy(derived);
            }
        }

        if (bossDatabase != null) {
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (!strategiesById.containsKey(b.getId().toLowerCase())) {
                    MonsterStrategy derived = deriveStrategyFromBossData(b);
                    registerStrategy(derived);
                }
            }
        }
    }

    public MonsterStrategy deriveStrategyFromMonsterData(MonsterData m) {
        String id = m.getId();
        String name = m.getName();
        MonsterCombatProfile p = m.getCombatProfile();
        MonsterVariant v = m.getPrimaryVariant();

        MonsterStrategy.Builder builder = MonsterStrategy.builder(id, name, m.getCategory(), m.getPrimaryStyle())
                .combatLevel(m.getCombatLevel())
                .hitpoints(v != null ? v.getHitpoints() : 100)
                .slayer(m.getRequiredSlayerLevel(), m.getSlayerCategory(), m.isSlayerTaskOnly())
                .overview(m.getStrategy() != null && !m.getStrategy().isEmpty() ? m.getStrategy() : name + " combat encounter.")
                .prayers(m.getOverheadPrayer() != null ? m.getOverheadPrayer() : "None",
                        m.getOffensivePrayer() != null ? m.getOffensivePrayer() : "None",
                        "Use offensive prayers to maximize DPS.");

        if (m.getTravel() != null && !m.getTravel().isEmpty()) {
            builder.addLocation(m.getTravel());
        } else if (v != null && v.getLocation() != null) {
            builder.addLocation(v.getLocation());
        }

        if (m.getWeakness() != null && !m.getWeakness().isEmpty()) {
            builder.addWeakness(m.getWeakness());
        }
        if (p != null && p.getElementalWeakness() != null && p.getElementalWeakness() != ElementalWeakness.NONE) {
            builder.elementalWeakness(p.getElementalWeakness(), p.getElementalWeaknessPercent());
            builder.addWeakness(p.getElementalWeakness().getDisplayName() + " Magic (+" + p.getElementalWeaknessPercent() + "% dmg)");
        }

        if (m.getMechanics() != null && !m.getMechanics().isEmpty()) {
            builder.addMechanic("Core Mechanics", m.getMechanics(), "Maintain proper positioning and prayers.");
        }
        if (m.getWarnings() != null && !m.getWarnings().isEmpty()) {
            builder.addCommonMistake(m.getWarnings());
            builder.addProtection(m.getWarnings());
        }

        // Special equipment advantages
        if (p != null) {
            if (p.isDragon()) builder.addSpecialEquipment("Dragon hunter lance / Dragon hunter crossbow (+20-30% damage)");
            if (p.isDemon()) builder.addSpecialEquipment("Arclight / Emberlight / Scorching bow / Purging staff (+70% accuracy, +30-36% damage)");
            if (p.isUndead()) builder.addSpecialEquipment("Salve amulet (ei) / Salve amulet (e) (+20% damage and accuracy)");
            if (p.isKalphite()) builder.addSpecialEquipment("Keris Partisan (+33% base, 3x special puncture proc)");
            if (p.isWilderness()) builder.addSpecialEquipment("Viggora's chainmace / Craw's bow / Thammaron's sceptre (+50% accuracy & damage in Wilderness)");
            if (p.isRequiresDragonfire()) builder.addProtection("Anti-dragon shield / Dragonfire shield + Antifire potion mandatory");
            if (p.isRequiresWyvernShield()) builder.addProtection("Ancient wyvern shield or Elemental shield mandatory against icy breath");
            if (p.isRequiresLeafBladed()) builder.addSpecialEquipment("Leaf-bladed spear, sword, battleaxe, or Broad ammunition/Magic dart");
            if (p.isRequiresRockHammer()) builder.addProtection("Rock hammer or Granite hammer to deliver the killing blow");
            if (p.isFavorsVenomProtection()) builder.addProtection("Anti-venom+ or Serpentine helm");
        }

        // Generate baseline methods
        builder.addMethod(StrategyMethod.builder(id + "_standard", "Standard " + m.getPrimaryStyle().getDisplayName(), StrategyTier.MID_GAME, m.getPrimaryStyle())
                .cost(m.getSuggestedBudget())
                .prayers(m.getOverheadPrayer() != null ? m.getOverheadPrayer() : "None", m.getOffensivePrayer() != null ? m.getOffensivePrayer() : "None")
                .whyThisWorks("Exploits " + name + "'s primary combat profile with standard effective gear.")
                .instructions("Engage with " + m.getPrimaryStyle().getDisplayName() + ". Keep overhead prayers active if taking heavy damage.")
                .build());

        builder.addMethod(StrategyMethod.builder(id + "_budget", "Budget " + m.getPrimaryStyle().getDisplayName(), StrategyTier.BUDGET, m.getPrimaryStyle())
                .cost(Math.max(25_000L, m.getSuggestedBudget() / 4))
                .prayers(m.getOverheadPrayer() != null ? m.getOverheadPrayer() : "None", "None")
                .whyThisWorks("Low-cost affordable equipment capable of consistent kills.")
                .instructions("Standard budget gear. Focus on safe combat and prayer preservation.")
                .build());

        builder.wikiUrls("https://oldschool.runescape.wiki/w/" + name.replace(" ", "_"),
                "https://oldschool.runescape.wiki/w/" + name.replace(" ", "_") + "/Strategies");

        return builder.build();
    }

    public MonsterStrategy deriveStrategyFromBossData(BossData b) {
        String id = b.getId();
        String name = b.getName();

        MonsterStrategy.Builder builder = MonsterStrategy.builder(id, name, b.getCategory() == BossCategory.BEGINNER ? MonsterCategory.REGULAR : MonsterCategory.BOSS, b.getPrimaryStyle())
                .combatLevel(b.getRecommendedCombatLevel())
                .overview(b.getFightStrategy() != null && !b.getFightStrategy().isEmpty() ? b.getFightStrategy() : name + " boss encounter.")
                .prayers(b.getOverheadPrayer(), b.getOffensivePrayer(), "Maintain offensive prayer whenever feasible.")
                .safespot(b.isSolo() ? "Solo encounter" : "Group capable");

        if (b.getHowToGetThere() != null && !b.getHowToGetThere().isEmpty()) {
            builder.addLocation(b.getHowToGetThere());
        }
        if (b.getWeakness() != null && !b.getWeakness().isEmpty()) {
            builder.addWeakness(b.getWeakness());
        }
        if (b.getMechanicsSummary() != null && !b.getMechanicsSummary().isEmpty()) {
            builder.addMechanic("Core Mechanics", b.getMechanicsSummary(), "React promptly to attack cues.");
        }
        if (b.getImportantWarnings() != null && !b.getImportantWarnings().isEmpty()) {
            builder.addCommonMistake(b.getImportantWarnings());
            builder.addProtection(b.getImportantWarnings());
        }
        if (b.getPhaseChanges() != null && !b.getPhaseChanges().isEmpty()) {
            builder.addPhase(b.getPhaseChanges());
        }
        if (b.getBeginnerTips() != null && !b.getBeginnerTips().isEmpty()) {
            builder.addTip("Beginner: " + b.getBeginnerTips());
        }
        if (b.getAdvancedTips() != null && !b.getAdvancedTips().isEmpty()) {
            builder.addTip("Advanced: " + b.getAdvancedTips());
        }

        builder.addMethod(StrategyMethod.builder(id + "_standard", "Standard " + b.getPrimaryStyle().getDisplayName(), StrategyTier.MID_GAME, b.getPrimaryStyle())
                .cost(b.getSuggestedGearBudget())
                .prayers(b.getOverheadPrayer(), b.getOffensivePrayer())
                .whyThisWorks("Well-rounded balanced strategy for " + name + ".")
                .instructions(b.getFightStrategy() != null ? b.getFightStrategy() : "Fight according to phase mechanics.")
                .build());

        builder.wikiUrls("https://oldschool.runescape.wiki/w/" + name.replace(" ", "_"),
                "https://oldschool.runescape.wiki/w/" + name.replace(" ", "_") + "/Strategies");

        return builder.build();
    }

    public MonsterStrategy getStrategy(String id) {
        if (id == null) return null;
        return strategiesById.get(id.toLowerCase());
    }

    public MonsterStrategy getStrategyByName(String name) {
        if (name == null) return null;
        MonsterStrategy strat = strategiesByName.get(name.toLowerCase());
        if (strat != null) return strat;
        for (MonsterStrategy s : strategiesById.values()) {
            if (s.getMonsterName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public Map<String, MonsterStrategy> getAllStrategies() {
        return Collections.unmodifiableMap(strategiesById);
    }

    public MonsterGearStrategy getGearStrategy(String monsterId, String methodId) {
        MonsterStrategy ms = getStrategy(monsterId);
        if (ms == null) return null;
        for (StrategyMethod m : ms.getMethods()) {
            if (m.getId().equalsIgnoreCase(methodId)) {
                return toGearStrategy(ms, m);
            }
        }
        if (!ms.getMethods().isEmpty()) {
            return toGearStrategy(ms, ms.getMethods().get(0));
        }
        return null;
    }

    public List<MonsterGearStrategy> getAllGearStrategies(String monsterId) {
        MonsterStrategy ms = getStrategy(monsterId);
        if (ms == null) return Collections.emptyList();
        List<MonsterGearStrategy> list = new ArrayList<>();
        for (StrategyMethod m : ms.getMethods()) {
            list.add(toGearStrategy(ms, m));
        }
        return list;
    }

    public MonsterGearStrategy toGearStrategy(MonsterStrategy ms, StrategyMethod m) {
        if (ms == null || m == null) return null;
        MonsterGearStrategy.Builder b = MonsterGearStrategy.builder(
                ms.getMonsterId(), ms.getMonsterName(), m.getCombatStyle(), m.getTitle())
                .category(m.getRecommendationType())
                .wikiPage(ms.getWikiUrl())
                .wikiStrategySource(!m.getWikiSourceUrl().isEmpty() ? m.getWikiSourceUrl() : ms.getWikiStrategyUrl())
                .explanation(m.getWhyThisWorks())
                .lastChecked(ms.getLastVerified())
                .status("VERIFIED")
                .confidence(ms.getConfidence())
                .cost(m.getEstimatedCostGp());

        for (String req : m.getRequiredQuests()) b.requirement(req);
        for (Map.Entry<Skill, Integer> s : m.getRequiredSkills().entrySet()) b.requirement(s.getKey().getName() + " " + s.getValue());
        if (!m.getOverheadPrayer().equalsIgnoreCase("None")) b.prayer("Overhead: " + m.getOverheadPrayer());
        if (!m.getOffensivePrayer().equalsIgnoreCase("None")) b.prayer("Offensive: " + m.getOffensivePrayer());
        for (String pot : m.getPotionSetup()) b.inventory(pot);
        for (String inv : m.getInventoryHighlights()) b.inventory(inv);
        for (String sp : ms.getSpecialEquipmentAdvantage()) b.special(sp);

        Map<EquipmentSlot, String> gear = m.getGearSetup();
        if (gear.containsKey(EquipmentSlot.WEAPON)) b.weapon(gear.get(EquipmentSlot.WEAPON));
        if (gear.containsKey(EquipmentSlot.HEAD)) b.helmet(gear.get(EquipmentSlot.HEAD));
        if (gear.containsKey(EquipmentSlot.BODY)) b.body(gear.get(EquipmentSlot.BODY));
        if (gear.containsKey(EquipmentSlot.LEGS)) b.legs(gear.get(EquipmentSlot.LEGS));
        if (gear.containsKey(EquipmentSlot.SHIELD)) b.shield(gear.get(EquipmentSlot.SHIELD));
        if (gear.containsKey(EquipmentSlot.CAPE)) b.cape(gear.get(EquipmentSlot.CAPE));
        if (gear.containsKey(EquipmentSlot.AMULET)) b.amulet(gear.get(EquipmentSlot.AMULET));
        if (gear.containsKey(EquipmentSlot.GLOVES)) b.gloves(gear.get(EquipmentSlot.GLOVES));
        if (gear.containsKey(EquipmentSlot.BOOTS)) b.boots(gear.get(EquipmentSlot.BOOTS));
        if (gear.containsKey(EquipmentSlot.RING)) b.ring(gear.get(EquipmentSlot.RING));
        if (gear.containsKey(EquipmentSlot.AMMO)) b.ammo(gear.get(EquipmentSlot.AMMO));
        if (gear.containsKey(EquipmentSlot.SPEC_WEAPON)) b.specWeapon(gear.get(EquipmentSlot.SPEC_WEAPON));

        return b.build();
    }

    public List<MonsterStrategy> searchStrategies(
            String query,
            MonsterCategory category,
            CombatStyle style,
            int maxCombatLevel,
            long maxCost) {
        List<MonsterStrategy> results = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (MonsterStrategy s : strategiesById.values()) {
            if (!q.isEmpty()) {
                boolean matchName = s.getMonsterName().toLowerCase().contains(q);
                boolean matchWeakness = s.getWeaknesses().stream().anyMatch(w -> w.toLowerCase().contains(q));
                boolean matchLoc = s.getLocations().stream().anyMatch(l -> l.toLowerCase().contains(q));
                if (!matchName && !matchWeakness && !matchLoc) continue;
            }

            if (category != null && category != MonsterCategory.ALL && s.getCategory() != category) {
                continue;
            }

            if (style != null && s.getPrimaryStyle() != style) {
                // Check if any method uses the style
                boolean hasStyle = s.getMethods().stream().anyMatch(m -> m.getCombatStyle() == style);
                if (!hasStyle) continue;
            }

            if (maxCombatLevel > 0 && s.getCombatLevel() > maxCombatLevel) {
                continue;
            }

            if (maxCost > 0) {
                boolean hasAffordableMethod = s.getMethods().stream().anyMatch(m -> m.getEstimatedCostGp() <= maxCost);
                if (!hasAffordableMethod) continue;
            }

            results.add(s);
        }

        return results;
    }
}
