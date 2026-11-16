package com.bosswise.service;

import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.StrategyConfidence;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WikiStrategyRefreshService {
    private static final Logger log = LoggerFactory.getLogger(WikiStrategyRefreshService.class);
    private static final String WIKI_API_ENDPOINT = "https://oldschool.runescape.wiki/api.php";
    private static final String USER_AGENT = "BossWise-RuneLite-Plugin/1.0.0 (RuneLite Plugin Hub; https://github.com/bosswise)";

    private final OkHttpClient httpClient;
    private final WikiCacheService cacheService;
    private final MonsterStrategyDatabase strategyDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private long lastRequestTime = 0L;
    private static final long MIN_REQUEST_INTERVAL_MS = 500L;

    @Inject
    public WikiStrategyRefreshService(
            OkHttpClient httpClient,
            WikiCacheService cacheService,
            MonsterStrategyDatabase strategyDatabase) {
        this.httpClient = httpClient != null ? httpClient : new OkHttpClient();
        this.cacheService = cacheService != null ? cacheService : new WikiCacheService();
        this.strategyDatabase = strategyDatabase;
    }

    public WikiStrategyRefreshService(WikiCacheService cacheService, MonsterStrategyDatabase strategyDatabase) {
        this(new OkHttpClient(), cacheService, strategyDatabase);
    }

    /**
     * Refresh a monster strategy asynchronously from the OSRS Wiki.
     */
    public CompletableFuture<MonsterStrategy> refreshStrategy(String monsterName) {
        CompletableFuture<MonsterStrategy> future = new CompletableFuture<>();

        if (monsterName == null || monsterName.trim().isEmpty()) {
            future.complete(null);
            return future;
        }

        MonsterStrategy local = strategyDatabase.getStrategyByName(monsterName);
        String cacheKey = "strat_" + monsterName.toLowerCase().replace(" ", "_");

        // Check cache first
        String cached = cacheService.get(cacheKey);
        if (cached != null) {
            future.complete(local);
            return future;
        }

        executor.submit(() -> {
            try {
                throttle();
                String pageTitle = monsterName.replace(" ", "_") + "/Strategies";
                String url = WIKI_API_ENDPOINT + "?action=parse&prop=wikitext&page="
                        + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8)
                        + "&format=json";

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "application/json")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        JsonObject json = new JsonParser().parse(body).getAsJsonObject();

                        if (json.has("parse") && json.getAsJsonObject("parse").has("wikitext")) {
                            String wikitext = json.getAsJsonObject("parse")
                                    .getAsJsonObject("wikitext")
                                    .get("*").getAsString();

                            cacheService.put(
                                    cacheKey,
                                    wikitext,
                                    "live-wiki-refresh",
                                    "https://oldschool.runescape.wiki/w/" + pageTitle,
                                    1.0
                            );
                            log.info("[BossWise] Successfully refreshed strategy from Wiki for {}", monsterName);
                        }
                    }
                } catch (Exception ex) {
                    log.debug("[BossWise] Wiki strategy fetch failed for {}: {}", monsterName, ex.getMessage());
                }

                // Return local strategy (preserved last-known-good)
                future.complete(local);
            } catch (Exception e) {
                log.debug("[BossWise] Exception during wiki strategy refresh: {}", e.getMessage());
                future.complete(local);
            }
        });

        return future;
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
