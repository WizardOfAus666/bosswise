package com.bosswise.db;

import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.ElementalWeakness;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterClassification;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
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
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MonsterDatabase {
    private static final Logger log = LoggerFactory.getLogger(MonsterDatabase.class);
    private final Map<String, MonsterData> monsters = new LinkedHashMap<>();
    private final Map<Integer, MonsterVariant> variantsByNpcId = new HashMap<>();

    public MonsterDatabase() {
        loadMonstersFromJson();
    }

    private void loadMonstersFromJson() {
        try (InputStream is = getClass().getResourceAsStream("/data/monsters.json")) {
            if (is == null) {
                log.warn("[BossWise] /data/monsters.json not found on classpath, initializing programmatic defaults");
                return;
            }

            Gson gson = new Gson();
            JsonArray array = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonArray.class);

            for (JsonElement elem : array) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                String name = obj.get("name").getAsString();
                String catStr = obj.get("category").getAsString();
                MonsterCategory category = MonsterCategory.valueOf(catStr);

                String styleStr = obj.has("primaryStyle") ? obj.get("primaryStyle").getAsString() : "MELEE";
                CombatStyle primaryStyle = CombatStyle.valueOf(styleStr);

                int combatLevel = obj.has("combatLevel") ? obj.get("combatLevel").getAsInt() : 50;
                long budget = obj.has("suggestedBudget") ? obj.get("suggestedBudget").getAsLong() : 500_000L;

                MonsterData.Builder mBuilder = MonsterData.builder(id, name, category, primaryStyle)
                        .combatLevel(combatLevel)
                        .budget(budget);

                // Strategy & Guide fields
                if (obj.has("weakness")) mBuilder.weakness(obj.get("weakness").getAsString());
                if (obj.has("overheadPrayer") && obj.has("offensivePrayer")) {
                    mBuilder.prayers(obj.get("overheadPrayer").getAsString(), obj.get("offensivePrayer").getAsString());
                }
                if (obj.has("travel")) mBuilder.travel(obj.get("travel").getAsString());
                if (obj.has("strategy")) mBuilder.strategy(obj.get("strategy").getAsString());
                if (obj.has("mechanics")) mBuilder.mechanics(obj.get("mechanics").getAsString());
                if (obj.has("warnings")) mBuilder.warnings(obj.get("warnings").getAsString());

                // Slayer
                if (obj.has("requiredSlayerLevel") || obj.has("slayerCategory")) {
                    int slayerLvl = obj.has("requiredSlayerLevel") ? obj.get("requiredSlayerLevel").getAsInt() : 1;
                    String slayerCat = obj.has("slayerCategory") ? obj.get("slayerCategory").getAsString() : "";
                    boolean taskOnly = obj.has("slayerTaskOnly") && obj.get("slayerTaskOnly").getAsBoolean();
                    mBuilder.slayer(slayerLvl, slayerCat, taskOnly);
                }

                if (obj.has("superiorVariantName") && obj.has("superiorNpcId")) {
                    mBuilder.superior(obj.get("superiorVariantName").getAsString(), obj.get("superiorNpcId").getAsInt());
                }

                // Classification & Elements
                String classStr = obj.has("classification") ? obj.get("classification").getAsString() : "NONE";
                MonsterClassification classification;
                try {
                    classification = MonsterClassification.valueOf(classStr);
                } catch (Exception e) {
                    classification = MonsterClassification.NONE;
                }

                String elemStr = obj.has("elementalWeakness") ? obj.get("elementalWeakness").getAsString() : "NONE";
                ElementalWeakness elementalWeakness;
                try {
                    elementalWeakness = ElementalWeakness.valueOf(elemStr);
                } catch (Exception e) {
                    elementalWeakness = ElementalWeakness.NONE;
                }
                int elemPct = obj.has("elementalWeaknessPercent") ? obj.get("elementalWeaknessPercent").getAsInt() : 0;

                MonsterCombatProfile.Builder pBuilder = MonsterCombatProfile.builder(id, name, primaryStyle)
                        .classification(classification)
                        .elementalWeakness(elementalWeakness, elemPct);

                // Special mechanic flags
                String nameLower = name.toLowerCase();
                if (classification == MonsterClassification.DRAGON || nameLower.contains("dragon") || nameLower.contains("vorkath")) {
                    pBuilder.requiresDragonfire();
                }
                if (nameLower.contains("wyvern")) {
                    pBuilder.requiresWyvernShield();
                }
                if (nameLower.contains("basilisk")) {
                    pBuilder.requiresMirrorShield();
                }
                if (nameLower.contains("kurask") || nameLower.contains("turoth")) {
                    pBuilder.requiresLeafBladed();
                }
                if (nameLower.contains("hydra") || nameLower.contains("drake") || nameLower.contains("wyrm")) {
                    pBuilder.requiresBootsOfStone();
                }
                if (nameLower.contains("vardorvis") || nameLower.contains("duke sucellus") || nameLower.contains("duke_sucellus")) {
                    pBuilder.favoredAttackType(AttackType.SLASH);
                }
                if (nameLower.contains("gargoyle")) {
                    pBuilder.requiresRockHammer();
                    pBuilder.favoredAttackType(AttackType.CRUSH);
                    pBuilder.requiresCrush();
                }
                if (nameLower.contains("calvarion") || nameLower.contains("spindel") || nameLower.contains("sarachnis") || nameLower.contains("tekton")) {
                    pBuilder.favoredAttackType(AttackType.CRUSH);
                    pBuilder.requiresCrush();
                }
                if (nameLower.contains("corporeal")) {
                    pBuilder.favoredAttackType(AttackType.STAB);
                    pBuilder.requiresSpear();
                }
                if (nameLower.contains("zulrah") || nameLower.contains("vorkath")) {
                    pBuilder.favorsVenomProtection();
                }
                if (nameLower.contains("zulrah")) {
                    pBuilder.favorsRecoil();
                }
                if (category == MonsterCategory.WILDERNESS || id.contains("revenant") || nameLower.contains("calvarion") || nameLower.contains("spindel") || nameLower.contains("artio")) {
                    pBuilder.wilderness();
                }
                if (id.equals("kreearra") || nameLower.contains("kree'arra") || nameLower.contains("kreearra")) {
                    pBuilder.allowedStyles(CombatStyle.RANGED, CombatStyle.MAGIC);
                }

                mBuilder.combatProfile(pBuilder.build());

                // Parse Variants
                if (obj.has("variants")) {
                    JsonArray varArray = obj.getAsJsonArray("variants");
                    for (JsonElement vElem : varArray) {
                        JsonObject vObj = vElem.getAsJsonObject();
                        int npcId = vObj.has("npcId") ? vObj.get("npcId").getAsInt() : 0;
                        String vName = vObj.has("variantName") ? vObj.get("variantName").getAsString() : "Standard";
                        String loc = vObj.has("location") ? vObj.get("location").getAsString() : "Various";
                        int cLvl = vObj.has("combatLevel") ? vObj.get("combatLevel").getAsInt() : combatLevel;
                        int hp = vObj.has("hitpoints") ? vObj.get("hitpoints").getAsInt() : 100;
                        int atk = vObj.has("attackLevel") ? vObj.get("attackLevel").getAsInt() : 50;
                        int str = vObj.has("strengthLevel") ? vObj.get("strengthLevel").getAsInt() : 50;
                        int def = vObj.has("defenceLevel") ? vObj.get("defenceLevel").getAsInt() : 50;
                        int mag = vObj.has("magicLevel") ? vObj.get("magicLevel").getAsInt() : 50;
                        int rng = vObj.has("rangedLevel") ? vObj.get("rangedLevel").getAsInt() : 50;

                        int stabDef = vObj.has("stabDefence") ? vObj.get("stabDefence").getAsInt() : 0;
                        int slashDef = vObj.has("slashDefence") ? vObj.get("slashDefence").getAsInt() : 0;
                        int crushDef = vObj.has("crushDefence") ? vObj.get("crushDefence").getAsInt() : 0;
                        int magDef = vObj.has("magicDefence") ? vObj.get("magicDefence").getAsInt() : 0;
                        int rngDef = vObj.has("rangedDefence") ? vObj.get("rangedDefence").getAsInt() : 0;

                        int spd = vObj.has("attackSpeed") ? vObj.get("attackSpeed").getAsInt() : 4;
                        boolean wildy = vObj.has("wilderness") && vObj.get("wilderness").getAsBoolean();
                        boolean multi = vObj.has("multiCombat") && vObj.get("multiCombat").getAsBoolean();
                        boolean cannon = vObj.has("cannonable") && vObj.get("cannonable").getAsBoolean();

                        List<String> styles = new ArrayList<>();
                        if (vObj.has("attackStyles")) {
                            for (JsonElement s : vObj.getAsJsonArray("attackStyles")) {
                                styles.add(s.getAsString());
                            }
                        }

                        MonsterVariant variant = new MonsterVariant(
                                npcId, vName, loc, cLvl, hp, atk, str, def, mag, rng,
                                stabDef, slashDef, crushDef, magDef, rngDef, spd, styles,
                                wildy, multi, cannon
                        );
                        mBuilder.addVariant(variant);
                        if (npcId > 0) {
                            variantsByNpcId.put(npcId, variant);
                        }
                    }
                }

                MonsterData monster = mBuilder.build();
                monsters.put(monster.getId().toLowerCase(), monster);
            }
            log.info("[BossWise] Loaded {} universal OSRS monsters ({} variants indexed)", monsters.size(), variantsByNpcId.size());
        } catch (Exception e) {
            log.error("[BossWise] Failed to load monsters from JSON", e);
        }
    }

    public MonsterData getMonster(String id) {
        if (id == null) return null;
        return monsters.get(id.toLowerCase());
    }

    public MonsterData getMonsterByName(String name) {
        if (name == null) return null;
        for (MonsterData m : monsters.values()) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public MonsterVariant getVariantByNpcId(int npcId) {
        return variantsByNpcId.get(npcId);
    }

    public Map<String, MonsterData> getAllMonsters() {
        return Collections.unmodifiableMap(monsters);
    }

    public List<MonsterData> getMonstersByCategory(MonsterCategory category) {
        if (category == null || category == MonsterCategory.ALL) {
            return new ArrayList<>(monsters.values());
        }
        List<MonsterData> list = new ArrayList<>();
        for (MonsterData m : monsters.values()) {
            if (m.getCategory() == category) {
                list.add(m);
            }
        }
        return list;
    }

    public List<MonsterData> getSlayerMonsters() {
        List<MonsterData> list = new ArrayList<>();
        for (MonsterData m : monsters.values()) {
            if (m.getCategory() == MonsterCategory.SLAYER || m.getCategory() == MonsterCategory.SUPERIOR || m.getRequiredSlayerLevel() > 1) {
                list.add(m);
            }
        }
        return list;
    }

    public List<MonsterData> getWildernessMonsters() {
        List<MonsterData> list = new ArrayList<>();
        for (MonsterData m : monsters.values()) {
            if (m.getCategory() == MonsterCategory.WILDERNESS || m.getCombatProfile().isWilderness()) {
                list.add(m);
            }
        }
        return list;
    }

    public Map<String, BossData> getAllBossesAsBossData() {
        Map<String, BossData> map = new LinkedHashMap<>();
        for (MonsterData m : monsters.values()) {
            map.put(m.getId(), m.toBossData());
        }
        return map;
    }
}
