package com.bosswise.service;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.DeathRiskProfile;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterProfitProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(MonsterProfitService.class);
    private final List<MonsterProfitProfile> profiles = new ArrayList<>();

    @Inject
    public MonsterProfitService() {
        loadProfilesFromJson();
    }

    private void loadProfilesFromJson() {
        try (InputStream is = getClass().getResourceAsStream("/data/profit_profiles.json")) {
            if (is == null) return;
            JsonElement parsed = new JsonParser().parse(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonArray arr = (parsed != null && parsed.isJsonArray()) ? parsed.getAsJsonArray() : null;
            if (arr == null) return;
            for (JsonElement elem : arr) {
                if (!elem.isJsonObject()) continue;
                JsonObject o = elem.getAsJsonObject();
                JsonObject dro = o.has("deathRisk") && o.get("deathRisk").isJsonObject()
                        ? o.getAsJsonObject("deathRisk") : null;
                DeathRiskProfile dr = dro != null ? new DeathRiskProfile(
                        dro.get("locationType").getAsString(),
                        dro.get("gravestoneFee").getAsLong(),
                        dro.get("retrievalServiceFee").getAsLong(),
                        dro.get("estimatedSupplyLoss").getAsLong(),
                        dro.get("pvpDangerous").getAsBoolean(),
                        dro.get("explanation").getAsString()
                ) : null;
                profiles.add(new MonsterProfitProfile(
                        o.get("monsterId").getAsString(),
                        o.get("name").getAsString(),
                        MonsterCategory.valueOf(o.get("category").getAsString()),
                        CombatStyle.valueOf(o.get("primaryStyle").getAsString()),
                        o.get("combatLevel").getAsInt(),
                        o.get("gpPerKill").getAsLong(),
                        o.get("killsPerHour").getAsInt(),
                        o.get("conservativeGpPerHour").getAsLong(),
                        o.get("optimisticGpPerHour").getAsLong(),
                        o.get("supplyCostPerHour").getAsLong(),
                        o.get("ammoCostPerHour").getAsLong(),
                        o.get("minGearInvestment").getAsLong(),
                        o.get("afkSuitable").getAsBoolean(),
                        o.get("killTimeSeconds").getAsInt(),
                        o.get("respawnSeconds").getAsInt(),
                        dr,
                        o.has("assumptions") ? o.get("assumptions").getAsString() : ""
                ));
            }
        } catch (Exception e) {
            log.error("[BossWise] Failed to load profit profiles from JSON", e);
        }
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
