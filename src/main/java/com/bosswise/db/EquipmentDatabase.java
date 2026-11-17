package com.bosswise.db;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentItem.AmmoType;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.EquipmentSlot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EquipmentDatabase {
    private static final Logger log = LoggerFactory.getLogger(EquipmentDatabase.class);
    private final List<EquipmentItem> allItems = new ArrayList<>();
    private final Map<EquipmentSlot, List<EquipmentItem>> itemsBySlot = new EnumMap<>(EquipmentSlot.class);
    private final Map<Integer, EquipmentItem> itemsById = new HashMap<>();
    private final Map<String, EquipmentItem> itemsByName = new HashMap<>();

    public EquipmentDatabase() {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            itemsBySlot.put(slot, new ArrayList<>());
        }
        loadEquipmentFromJson("/data/equipment_defaults.json");
        loadEquipmentFromJson("/data/equipment.json");
    }

    private void addItem(EquipmentItem item) {
        if (item == null) return;
        EquipmentItem existing = itemsById.get(item.getItemId());
        if (existing != null) {
            allItems.remove(existing);
            itemsBySlot.get(existing.getSlot()).remove(existing);
        }
        allItems.add(item);
        itemsBySlot.get(item.getSlot()).add(item);
        itemsById.put(item.getItemId(), item);
        itemsByName.put(item.getName().toLowerCase(), item);
    }

    private void loadEquipmentFromJson(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                log.info("[BossWise] {} not found on classpath", resourcePath);
                return;
            }

            JsonElement parsed = new JsonParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonArray array = (parsed != null && parsed.isJsonArray()) ? parsed.getAsJsonArray() : null;
            if (array == null) return;

            int loaded = 0;
            for (JsonElement elem : array) {
                if (!elem.isJsonObject()) continue;
                try {
                    JsonObject obj = elem.getAsJsonObject();

                    int itemId = obj.get("itemId").getAsInt();
                    String name = obj.get("name").getAsString();
                    EquipmentSlot slot = EquipmentSlot.valueOf(obj.get("slot").getAsString());
                    CombatStyle style;
                    try {
                        style = CombatStyle.valueOf(obj.get("primaryStyle").getAsString());
                    } catch (Exception e) {
                        style = CombatStyle.HYBRID;
                    }

                    EquipmentItem.Builder builder = EquipmentItem.builder(itemId, name, slot, style);

                    if (obj.has("attackType")) {
                        try {
                            builder.attackType(AttackType.valueOf(obj.get("attackType").getAsString()));
                        } catch (Exception ignored) {}
                    }
                    if (obj.has("ammoType")) {
                        try {
                            builder.ammoType(AmmoType.valueOf(obj.get("ammoType").getAsString()));
                        } catch (Exception ignored) {}
                    }
                    if (obj.has("weaponCategory")) builder.weaponCategory(obj.get("weaponCategory").getAsString());
                    if (obj.has("attackSpeed")) builder.speed(obj.get("attackSpeed").getAsInt());
                    if (obj.has("twoHanded") && obj.get("twoHanded").getAsBoolean()) builder.twoHanded();
                    if (obj.has("defaultPrice")) builder.price(obj.get("defaultPrice").getAsInt());
                    if (obj.has("tier")) builder.tier(obj.get("tier").getAsInt());
                    if (obj.has("untradeable") && obj.get("untradeable").getAsBoolean()) builder.untradeable();
                    if (obj.has("questReq")) builder.questReq(obj.get("questReq").getAsString());

                    if (obj.has("reqSkills") && obj.get("reqSkills").isJsonObject()) {
                        JsonObject skills = obj.getAsJsonObject("reqSkills");
                        for (Map.Entry<String, JsonElement> entry : skills.entrySet()) {
                            try {
                                Skill skill = Skill.valueOf(entry.getKey().toUpperCase());
                                builder.req(skill, entry.getValue().getAsInt());
                            } catch (Exception ignored) {}
                        }
                    }

                    int stabAtk = obj.has("stabAttack") ? obj.get("stabAttack").getAsInt() : 0;
                    int slashAtk = obj.has("slashAttack") ? obj.get("slashAttack").getAsInt() : 0;
                    int crushAtk = obj.has("crushAttack") ? obj.get("crushAttack").getAsInt() : 0;
                    int magicAtk = obj.has("magicAttack") ? obj.get("magicAttack").getAsInt() : 0;
                    int rangedAtk = obj.has("rangedAttack") ? obj.get("rangedAttack").getAsInt() : 0;
                    builder.atk(stabAtk, slashAtk, crushAtk, magicAtk, rangedAtk);

                    int stabDef = obj.has("stabDefence") ? obj.get("stabDefence").getAsInt() : 0;
                    int slashDef = obj.has("slashDefence") ? obj.get("slashDefence").getAsInt() : 0;
                    int crushDef = obj.has("crushDefence") ? obj.get("crushDefence").getAsInt() : 0;
                    int magicDef = obj.has("magicDefence") ? obj.get("magicDefence").getAsInt() : 0;
                    int rangedDef = obj.has("rangedDefence") ? obj.get("rangedDefence").getAsInt() : 0;
                    builder.def(stabDef, slashDef, crushDef, magicDef, rangedDef);

                    int meleeStr = obj.has("meleeStrength") ? obj.get("meleeStrength").getAsInt() : 0;
                    int rangedStr = obj.has("rangedStrength") ? obj.get("rangedStrength").getAsInt() : 0;
                    int magicDmg = obj.has("magicDamagePercent") ? obj.get("magicDamagePercent").getAsInt() : 0;
                    builder.str(meleeStr, rangedStr, magicDmg);

                    if (obj.has("prayerBonus")) builder.prayer(obj.get("prayerBonus").getAsInt());

                    if (obj.has("dragonbane") && obj.get("dragonbane").getAsBoolean()) builder.dragonbane();
                    if (obj.has("demonbane") && obj.get("demonbane").getAsBoolean()) builder.demonbane();
                    if (obj.has("ratbane") && obj.get("ratbane").getAsBoolean()) builder.ratbane();
                    if (obj.has("undeadBonus") && obj.get("undeadBonus").getAsBoolean()) builder.undeadBonus();
                    if (obj.has("slayerTaskBonus") && obj.get("slayerTaskBonus").getAsBoolean()) builder.slayerTaskBonus();
                    if (obj.has("dragonfireProtection") && obj.get("dragonfireProtection").getAsBoolean()) builder.dragonfireProtection();
                    if (obj.has("venomProtection") && obj.get("venomProtection").getAsBoolean()) builder.venomProtection();
                    if (obj.has("wildernessBonus") && obj.get("wildernessBonus").getAsBoolean()) builder.wildernessBonus();
                    if (obj.has("spear") && obj.get("spear").getAsBoolean()) builder.spear();
                    if (obj.has("mirrorShield") && obj.get("mirrorShield").getAsBoolean()) builder.mirrorShield();
                    if (obj.has("leafBladed") && obj.get("leafBladed").getAsBoolean()) builder.leafBladed();
                    if (obj.has("bootsOfStone") && obj.get("bootsOfStone").getAsBoolean()) builder.bootsOfStone();
                    if (obj.has("voidKnight") && obj.get("voidKnight").getAsBoolean()) builder.voidKnight();
                    if (obj.has("kalphitebane") && obj.get("kalphitebane").getAsBoolean()) builder.kalphitebane();

                    if (obj.has("hasSpecialAttack") && obj.get("hasSpecialAttack").getAsBoolean()) {
                        int energy = obj.has("specEnergyCost") ? obj.get("specEnergyCost").getAsInt() : 50;
                        String specDesc = obj.has("specEffect") ? obj.get("specEffect").getAsString() : "";
                        builder.specialAttack(energy, specDesc);
                    }

                    if (obj.has("specialEffect")) builder.effect(obj.get("specialEffect").getAsString());
                    if (obj.has("source")) builder.source(obj.get("source").getAsString());
                    if (obj.has("verifiedDate")) builder.verifiedDate(obj.get("verifiedDate").getAsString());

                    addItem(builder.build());
                    loaded++;
                } catch (Exception itemEx) {
                    log.warn("[BossWise] Failed parsing equipment item from JSON: {}", itemEx.getMessage());
                }
            }
            log.info("[BossWise] Loaded {} equipment items from {} (Total indexed: {})", loaded, resourcePath, allItems.size());
        } catch (Exception e) {
            log.error("[BossWise] Error loading equipment from JSON", e);
        }
    }

    public List<EquipmentItem> getItemsForSlot(EquipmentSlot slot) {
        return itemsBySlot.getOrDefault(slot, Collections.emptyList());
    }

    public EquipmentItem getItem(String name) {
        if (name == null) return null;
        EquipmentItem item = itemsByName.get(name.toLowerCase());
        if (item != null) return item;
        for (EquipmentItem i : allItems) {
            if (i.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return null;
    }

    public EquipmentItem getItemByName(String name) {
        return getItem(name);
    }

    public EquipmentItem getItem(int itemId) {
        return itemsById.get(itemId);
    }

    public List<EquipmentItem> getAllItems() {
        return Collections.unmodifiableList(allItems);
    }
}
