package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.WikiMetadata;
import com.bosswise.model.WikiMetadata.VerificationStatus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WikiDataService {
    private static final Logger log = LoggerFactory.getLogger(WikiDataService.class);
    private static final String WIKI_API_ENDPOINT = "https://oldschool.runescape.wiki/api.php";
    private static final String USER_AGENT = "BossWise-RuneLite-Plugin/1.0.0 (RuneLite Plugin Hub; https://github.com/bosswise)";

    private final OkHttpClient httpClient;
    private final WikiCacheService cacheService;
    private final EquipmentDatabase equipmentDatabase;
    private final MonsterDatabase monsterDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long lastRequestTime = 0L;
    private static final long MIN_REQUEST_INTERVAL_MS = 500L; // 2 requests per second maximum

    @Inject
    public WikiDataService(
            OkHttpClient httpClient,
            WikiCacheService cacheService,
            EquipmentDatabase equipmentDatabase,
            MonsterDatabase monsterDatabase) {
        this.httpClient = httpClient;
        this.cacheService = cacheService;
        this.equipmentDatabase = equipmentDatabase;
        this.monsterDatabase = monsterDatabase;
    }

    /**
     * Asynchronously query monster information from the OSRS Wiki.
     * Returns cached or bundled baseline on failure/offline.
     */
    public CompletableFuture<MonsterCombatProfile> fetchMonsterProfile(String monsterName) {
        CompletableFuture<MonsterCombatProfile> future = new CompletableFuture<>();

        if (monsterName == null || monsterName.trim().isEmpty()) {
            future.complete(null);
            return future;
        }

        // 1. Check local cache
        String cacheKey = "monster_" + monsterName;
        String cachedWikitext = cacheService.get(cacheKey);
        if (cachedWikitext != null) {
            MonsterCombatProfile profile = parseMonsterWikitext(monsterName, cachedWikitext);
            if (profile != null) {
                future.complete(profile);
                return future;
            }
        }

        // 2. Fetch asynchronously
        executor.submit(() -> {
            try {
                throttle();
                String url = WIKI_API_ENDPOINT + "?action=parse&prop=wikitext&page="
                        + URLEncoder.encode(monsterName, StandardCharsets.UTF_8)
                        + "&format=json";

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "application/json")
                        .build();

                httpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        log.debug("[BossWise] Wiki fetch failed for {}: {}", monsterName, e.getMessage());
                        future.complete(getFallbackMonsterProfile(monsterName));
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        try {
                            if (!response.isSuccessful() || response.body() == null) {
                                future.complete(getFallbackMonsterProfile(monsterName));
                                return;
                            }

                            String jsonString = response.body().string();
                            JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
                            if (json.has("parse") && json.getAsJsonObject("parse").has("wikitext")) {
                                String wikitext = json.getAsJsonObject("parse")
                                        .getAsJsonObject("wikitext")
                                        .get("*").getAsString();

                                cacheService.put(
                                        cacheKey,
                                        wikitext,
                                        "live-fetch",
                                        "https://oldschool.runescape.wiki/w/" + URLEncoder.encode(monsterName, StandardCharsets.UTF_8),
                                        1.0
                                );

                                MonsterCombatProfile profile = parseMonsterWikitext(monsterName, wikitext);
                                future.complete(profile != null ? profile : getFallbackMonsterProfile(monsterName));
                            } else {
                                future.complete(getFallbackMonsterProfile(monsterName));
                            }
                        } catch (Exception ex) {
                            log.debug("[BossWise] Error parsing wiki response for {}: {}", monsterName, ex.getMessage());
                            future.complete(getFallbackMonsterProfile(monsterName));
                        } finally {
                            response.close();
                        }
                    }
                });
            } catch (Exception ex) {
                log.debug("[BossWise] Execution error fetching {}: {}", monsterName, ex.getMessage());
                future.complete(getFallbackMonsterProfile(monsterName));
            }
        });

        return future;
    }

    /**
     * Fallback to internal bundled MonsterDatabase profile
     */
    public MonsterCombatProfile getFallbackMonsterProfile(String monsterName) {
        if (monsterDatabase != null && monsterName != null) {
            MonsterData data = monsterDatabase.getMonsterByName(monsterName);
            if (data != null && data.getCombatProfile() != null) {
                return data.getCombatProfile();
            }
        }
        String id = monsterName != null ? monsterName.toLowerCase().replace(" ", "_") : "target";
        String name = monsterName != null ? monsterName : "Target";
        return MonsterCombatProfile.builder(id, name, com.bosswise.model.CombatStyle.MELEE).build();
    }

    /**
     * Parse infobox parameters from OSRS Wiki wikitext.
     */
    public MonsterCombatProfile parseMonsterWikitext(String monsterName, String wikitext) {
        if (wikitext == null) return null;

        try {
            boolean isDragon = wikitext.contains("attribute = dragon") || wikitext.contains("attribute = fiery");
            boolean isDemon = wikitext.contains("attribute = demon");
            boolean isUndead = wikitext.contains("attribute = undead");
            boolean isKalphite = wikitext.contains("attribute = kalphite");

            String id = monsterName != null ? monsterName.toLowerCase().replace(" ", "_") : "target";
            String name = monsterName != null ? monsterName : "Target";

            MonsterCombatProfile.Builder builder = MonsterCombatProfile.builder(
                    id,
                    name,
                    com.bosswise.model.CombatStyle.MELEE
            );

            if (isDragon) builder.dragon();
            if (isDemon) builder.demon();
            if (isUndead) builder.undead();
            if (isKalphite) builder.kalphite();

            return builder.build();
        } catch (Exception ex) {
            log.debug("[BossWise] Error parsing monster wikitext: {}", ex.getMessage());
            return null;
        }
    }

    public WikiMetadata getMonsterMetadata(String monsterName) {
        return cacheService.getMetadata("monster_" + monsterName);
    }

    private int extractInt(String wikitext, String param, int defaultValue) {
        Pattern pattern = Pattern.compile("\\|\\s*" + Pattern.quote(param) + "\\s*=\\s*([+-]?\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(wikitext);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long diff = now - lastRequestTime;
        if (diff < MIN_REQUEST_INTERVAL_MS) {
            try {
                Thread.sleep(MIN_REQUEST_INTERVAL_MS - diff);
            } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
