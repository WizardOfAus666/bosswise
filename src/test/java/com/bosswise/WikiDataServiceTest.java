package com.bosswise;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.WikiMetadata;
import com.bosswise.model.WikiMetadata.VerificationStatus;
import com.bosswise.service.WikiCacheService;
import com.bosswise.service.WikiDataService;
import com.google.gson.Gson;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import okhttp3.OkHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WikiDataServiceTest {
    private File tempCacheDir;
    private WikiCacheService cacheService;
    private WikiDataService wikiDataService;

    @Before
    public void setUp() {
        tempCacheDir = new File(System.getProperty("java.io.tmpdir"), "bosswise_test_wiki_cache_" + System.currentTimeMillis());
        tempCacheDir.mkdirs();
        cacheService = new WikiCacheService(tempCacheDir, new Gson());
        EquipmentDatabase eqDb = new EquipmentDatabase();
        MonsterDatabase mDb = new MonsterDatabase();
        wikiDataService = new WikiDataService(new OkHttpClient(), cacheService, eqDb, mDb);
    }

    @After
    public void tearDown() {
        if (tempCacheDir != null && tempCacheDir.exists()) {
            File[] files = tempCacheDir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            tempCacheDir.delete();
        }
    }

    @Test
    public void testCachePutAndGet() {
        cacheService.put("test_key", "sample wikitext content", "rev123", "https://wiki/test", 1.0);
        String retrieved = cacheService.get("test_key");
        assertEquals("sample wikitext content", retrieved);

        WikiMetadata meta = cacheService.getMetadata("test_key");
        assertNotNull(meta);
        assertEquals(VerificationStatus.VERIFIED, meta.getStatus());
    }

    @Test
    public void testParseMonsterWikitext() {
        String sampleWikitext = "{{Infobox Monster\n"
                + "|name = Test Dragon\n"
                + "|hitpoints = 750\n"
                + "|def = 214\n"
                + "|dstab = 65\n"
                + "|dslash = 65\n"
                + "|dcrush = 65\n"
                + "|dmagic = 150\n"
                + "|drange = 65\n"
                + "|aspeed = 5\n"
                + "|attribute = dragon\n"
                + "}}";

        MonsterCombatProfile profile = wikiDataService.parseMonsterWikitext("Test Dragon", sampleWikitext);
        assertNotNull(profile);
        assertTrue("Should detect dragon attribute", profile.isDragon());
    }

    @Test
    public void testOfflineFallback() {
        MonsterCombatProfile fallback = wikiDataService.getFallbackMonsterProfile("Vorkath");
        assertNotNull("Fallback profile should exist", fallback);
        assertTrue("Vorkath should have dragon attribute", fallback.isDragon());
    }
}
