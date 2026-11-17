package com.bosswise.service;

import com.bosswise.model.WikiMetadata;
import com.bosswise.model.WikiMetadata.VerificationStatus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WikiCacheService {
    private static final Logger log = LoggerFactory.getLogger(WikiCacheService.class);
    private static final long DEFAULT_TTL_MS = 7L * 24 * 60 * 60 * 1000L; // 7 days

    private final Map<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private final File diskCacheDir;
    private final Gson gson;

    private final AtomicInteger cacheHits = new AtomicInteger(0);
    private final AtomicInteger cacheMisses = new AtomicInteger(0);

    public static class CacheEntry {
        public String key;
        public String content;
        public long timestamp;
        public long ttlMs;
        public String revisionId;
        public String sourceUrl;
        public double confidence;

        public CacheEntry() {}

        public CacheEntry(String key, String content, long ttlMs, String revisionId, String sourceUrl, double confidence) {
            this.key = key;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
            this.ttlMs = ttlMs;
            this.revisionId = revisionId;
            this.sourceUrl = sourceUrl;
            this.confidence = confidence;
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > ttlMs;
        }
    }

    @Inject
    public WikiCacheService(Gson gson) {
        this.gson = gson != null ? gson.newBuilder().setPrettyPrinting().create() : null;
        File home = new File(System.getProperty("user.home", "."));
        File rl = new File(home, ".runelite");
        File bw = new File(rl, "bosswise");
        this.diskCacheDir = new File(bw, "wiki_cache");

        try {
            if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }
        } catch (Exception ex) {
            log.warn("[BossWise] Could not create wiki cache dir: {}", ex.getMessage());
        }
    }

    public WikiCacheService(File cacheDir, Gson gson) {
        this.gson = gson != null ? gson.newBuilder().setPrettyPrinting().create() : null;
        this.diskCacheDir = cacheDir;
        try {
            if (!diskCacheDir.exists()) {
                diskCacheDir.mkdirs();
            }
        } catch (Exception ex) {
            log.warn("[BossWise] Could not create custom wiki cache dir: {}", ex.getMessage());
        }
    }

    public String get(String rawKey) {
        if (rawKey == null) return null;
        String key = sanitizeKey(rawKey);

        // 1. Check memory cache
        CacheEntry entry = memoryCache.get(key);
        if (entry != null) {
            if (!entry.isExpired()) {
                cacheHits.incrementAndGet();
                return entry.content;
            } else {
                memoryCache.remove(key);
            }
        }

        // 2. Check disk cache
        if (gson != null && diskCacheDir != null && diskCacheDir.exists()) {
            File file = new File(diskCacheDir, key + ".json");
            if (file.exists() && file.isFile()) {
                try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                    CacheEntry diskEntry = gson.fromJson(reader, CacheEntry.class);
                    if (diskEntry != null && !diskEntry.isExpired()) {
                        memoryCache.put(key, diskEntry);
                        cacheHits.incrementAndGet();
                        return diskEntry.content;
                    } else if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception ex) {
                    log.debug("[BossWise] Error reading disk cache for {}: {}", key, ex.getMessage());
                }
            }
        }

        cacheMisses.incrementAndGet();
        return null;
    }

    public void put(String rawKey, String content, String revisionId, String sourceUrl, double confidence) {
        if (rawKey == null || content == null) return;
        String key = sanitizeKey(rawKey);
        CacheEntry entry = new CacheEntry(key, content, DEFAULT_TTL_MS, revisionId, sourceUrl, confidence);

        memoryCache.put(key, entry);

        if (gson != null && diskCacheDir != null && diskCacheDir.exists()) {
            try {
                File file = new File(diskCacheDir, key + ".json");
                try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                    gson.toJson(entry, writer);
                }
            } catch (Exception ex) {
                log.debug("[BossWise] Failed to persist wiki entry {}: {}", key, ex.getMessage());
            }
        }
    }

    public WikiMetadata getMetadata(String rawKey) {
        String key = sanitizeKey(rawKey);
        CacheEntry entry = memoryCache.get(key);
        if (entry != null) {
            return new WikiMetadata(
                    entry.sourceUrl != null ? entry.sourceUrl : "https://oldschool.runescape.wiki/w/" + rawKey,
                    entry.timestamp,
                    entry.isExpired() ? VerificationStatus.PARTIALLY_VERIFIED : VerificationStatus.VERIFIED,
                    entry.confidence,
                    entry.revisionId != null ? entry.revisionId : "cached",
                    "Loaded from BossWise local cache"
            );
        }
        return WikiMetadata.offlineFallback(rawKey);
    }

    public void clear() {
        memoryCache.clear();
        if (gson != null && diskCacheDir != null && diskCacheDir.exists()) {
            File[] files = diskCacheDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    public int getCacheHits() { return cacheHits.get(); }
    public int getCacheMisses() { return cacheMisses.get(); }
    public int getMemoryCacheSize() { return memoryCache.size(); }

    private String sanitizeKey(String key) {
        return key.toLowerCase().trim().replaceAll("[^a-z0-9_-]", "_");
    }
}
