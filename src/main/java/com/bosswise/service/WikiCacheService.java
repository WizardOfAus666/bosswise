package com.bosswise.service;

import com.bosswise.model.WikiMetadata;
import com.bosswise.model.WikiMetadata.VerificationStatus;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.util.Filepath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WikiCacheService {
    private static final Logger log = LoggerFactory.getLogger(WikiCacheService.class);
    private static final long DEFAULT_TTL_MS = 7L * 24 * 60 * 60 * 1000L; // 7 days

    private final Map<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private volatile Filepath diskCacheDir;
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
    }

    public WikiCacheService(Filepath cacheDir, Gson gson) {
        this.gson = gson != null ? gson.newBuilder().setPrettyPrinting().create() : null;
        setCacheDir(cacheDir);
    }

    public void setCacheDir(Filepath cacheDir) {
        this.diskCacheDir = cacheDir;
        if (cacheDir != null) {
            try {
                if (!cacheDir.exists()) {
                    cacheDir.createDirectories();
                }
            } catch (Exception ex) {
                log.warn("[BossWise] Could not create wiki cache dir: {}", ex.getMessage());
            }
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
        Filepath dir = diskCacheDir;
        if (gson != null && dir != null && dir.exists()) {
            Filepath file = dir.joinSegment(key + ".json");
            if (file.exists() && file.isFile()) {
                try (Reader reader = file.openReader()) {
                    CacheEntry diskEntry = gson.fromJson(reader, CacheEntry.class);
                    if (diskEntry != null && !diskEntry.isExpired()) {
                        memoryCache.put(key, diskEntry);
                        cacheHits.incrementAndGet();
                        return diskEntry.content;
                    } else {
                        file.deleteIfExists();
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

        Filepath dir = diskCacheDir;
        if (gson != null && dir != null && dir.exists()) {
            try {
                Filepath file = dir.joinSegment(key + ".json");
                try (Writer writer = file.openWriter()) {
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
        Filepath dir = diskCacheDir;
        if (dir != null && dir.exists()) {
            try (Stream<Filepath> entries = dir.walk(1)) {
                entries.filter(p -> p.isFile() && p.getFileName().endsWith(".json"))
                        .forEach(p -> {
                            try {
                                p.deleteIfExists();
                            } catch (IOException ignored) {}
                        });
            } catch (IOException ex) {
                log.debug("[BossWise] Error clearing disk cache: {}", ex.getMessage());
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
