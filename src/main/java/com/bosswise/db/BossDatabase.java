package com.bosswise.db;

import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.BossDifficulty;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.QuestRequirement;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BossDatabase {
    private static final Logger log = LoggerFactory.getLogger(BossDatabase.class);
    private final Map<String, BossData> bosses = new LinkedHashMap<>();

    public BossDatabase() {
        loadBossesFromJson();
    }

    private void loadBossesFromJson() {
        try (InputStream is = getClass().getResourceAsStream("/data/bosses.json")) {
            if (is == null) {
                log.warn("[BossWise] /data/bosses.json not found on classpath");
                return;
            }
            JsonElement parsed = new JsonParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonArray array = (parsed != null && parsed.isJsonArray()) ? parsed.getAsJsonArray() : null;
            if (array == null) return;

            for (JsonElement elem : array) {
                if (!elem.isJsonObject()) continue;
                JsonObject o = elem.getAsJsonObject();
                BossData.Builder b = BossData.builder(
                        o.get("id").getAsString(),
                        o.get("name").getAsString(),
                        BossCategory.valueOf(o.get("category").getAsString()),
                        BossDifficulty.valueOf(o.get("difficulty").getAsString()),
                        CombatStyle.valueOf(o.get("primaryStyle").getAsString())
                );
                if (o.has("recommendedCombatLevel")) b.combatLevel(o.get("recommendedCombatLevel").getAsInt());
                if (o.has("suggestedGearBudget")) b.budget(o.get("suggestedGearBudget").getAsLong());
                if (o.has("solo")) b.solo(o.get("solo").getAsBoolean());
                if (o.has("group")) b.group(o.get("group").getAsBoolean());
                if (o.has("estimatedGpPerKill") && o.has("estimatedGpHour")) {
                    b.gp(o.get("estimatedGpPerKill").getAsLong(), o.get("estimatedGpHour").getAsLong());
                }
                if (o.has("weakness")) b.weakness(o.get("weakness").getAsString());
                if (o.has("overheadPrayer") && o.has("offensivePrayer")) {
                    b.prayers(o.get("overheadPrayer").getAsString(), o.get("offensivePrayer").getAsString());
                }
                if (o.has("howToGetThere")) b.travel(o.get("howToGetThere").getAsString());
                if (o.has("fightStrategy")) b.strategy(o.get("fightStrategy").getAsString());
                if (o.has("mechanicsSummary")) b.mechanics(o.get("mechanicsSummary").getAsString());
                if (o.has("phaseChanges")) b.phases(o.get("phaseChanges").getAsString());
                if (o.has("importantWarnings")) b.warnings(o.get("importantWarnings").getAsString());
                if (o.has("beginnerTips") && o.has("advancedTips")) {
                    b.tips(o.get("beginnerTips").getAsString(), o.get("advancedTips").getAsString());
                }
                if (o.has("minSkills") && o.get("minSkills").isJsonArray()) {
                    for (JsonElement se : o.getAsJsonArray("minSkills")) {
                        JsonObject s = se.getAsJsonObject();
                        Skill skill = Skill.valueOf(s.get("skill").getAsString());
                        int lvl = s.get("level").getAsInt();
                        boolean boostable = s.has("boostable") && s.get("boostable").getAsBoolean();
                        String note = s.has("note") ? s.get("note").getAsString() : null;
                        b.minSkill(skill, lvl, boostable, note);
                    }
                }
                if (o.has("recSkills") && o.get("recSkills").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> e : o.getAsJsonObject("recSkills").entrySet()) {
                        b.recSkill(Skill.valueOf(e.getKey()), e.getValue().getAsInt());
                    }
                }
                if (o.has("quests") && o.get("quests").isJsonArray()) {
                    for (JsonElement qe : o.getAsJsonArray("quests")) {
                        JsonObject q = qe.getAsJsonObject();
                        Quest quest = q.has("questEnum") ? findQuestSafe(q.get("questEnum").getAsString()) : null;
                        String qName = q.get("questName").getAsString();
                        boolean mandatory = !q.has("mandatory") || q.get("mandatory").getAsBoolean();
                        String notes = q.has("notes") ? q.get("notes").getAsString() : null;
                        b.quest(new QuestRequirement(quest, qName, mandatory, notes));
                    }
                }
                if (o.has("prerequisites") && o.get("prerequisites").isJsonArray()) {
                    for (JsonElement pe : o.getAsJsonArray("prerequisites")) {
                        b.prerequisite(pe.getAsString());
                    }
                }
                if (o.has("checklist") && o.get("checklist").isJsonArray()) {
                    for (JsonElement ce : o.getAsJsonArray("checklist")) {
                        b.addChecklist(ce.getAsString());
                    }
                }
                if (o.has("drops") && o.get("drops").isJsonArray()) {
                    for (JsonElement de : o.getAsJsonArray("drops")) {
                        b.addDrop(de.getAsString());
                    }
                }
                if (o.has("inventoryPresets") && o.get("inventoryPresets").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> pe : o.getAsJsonObject("inventoryPresets").entrySet()) {
                        List<InventoryItem> items = new ArrayList<>();
                        for (JsonElement ie : pe.getValue().getAsJsonArray()) {
                            JsonObject io = ie.getAsJsonObject();
                            items.add(new InventoryItem(
                                    io.get("itemId").getAsInt(),
                                    io.get("name").getAsString(),
                                    io.get("quantity").getAsInt(),
                                    io.get("category").getAsString(),
                                    io.get("unitPrice").getAsInt(),
                                    io.get("mandatory").getAsBoolean(),
                                    io.has("note") ? io.get("note").getAsString() : ""
                            ));
                        }
                        b.addInventoryPreset(pe.getKey(), new InventorySetup(pe.getKey(), items));
                    }
                }
                bosses.put(o.get("id").getAsString(), b.build());
            }
            log.info("[BossWise] Loaded {} bosses from JSON", bosses.size());
        } catch (Exception e) {
            log.error("[BossWise] Failed to load bosses from JSON", e);
        }
    }

    private static Quest findQuestSafe(String enumName) {
        try {
            return Quest.valueOf(enumName);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, BossData> getAllBosses() {
        return Collections.unmodifiableMap(bosses);
    }

    public BossData getBoss(String id) {
        return bosses.get(id);
    }
}
