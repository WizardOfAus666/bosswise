package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.BossEligibility;
import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.BossRecommendationEngine;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BossEligibilityTest {
    private BossDatabase bossDatabase;
    private BossRecommendationEngine engine;

    @Before
    public void setup() {
        bossDatabase = new BossDatabase();
        engine = new BossRecommendationEngine(bossDatabase);
    }

    @Test
    public void testReadyBossEligibility() {
        // Create an account with high stats and DS2 completed
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) reals.put(s, 90);

        Map<Quest, QuestState> quests = new HashMap<>();
        quests.put(Quest.DRAGON_SLAYER_II, QuestState.FINISHED);

        Set<String> manual = new HashSet<>();
        manual.add("Dragon Slayer II");

        PlayerAccount account = new PlayerAccount("MaxedPvMer", 126, 2000, reals, reals, quests, manual, true);

        BossData vorkath = bossDatabase.getBoss("vorkath");
        Assert.assertNotNull(vorkath);

        BossEligibility el = engine.evaluateEligibility(vorkath, account);
        Assert.assertEquals(BossEligibilityStatus.READY, el.getStatus());
        Assert.assertTrue(el.isReady());
        Assert.assertTrue(el.getMissingRequirements().isEmpty());
        Assert.assertTrue(el.getRecommendationScore() >= 80);
    }

    @Test
    public void testLockedDueToMissingQuest() {
        // High stats but missing DS2
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) reals.put(s, 90);

        PlayerAccount account = new PlayerAccount("NoQuestPlayer", 126, 2000, reals, reals, new HashMap<>(), new HashSet<>(), true);

        BossData vorkath = bossDatabase.getBoss("vorkath");
        BossEligibility el = engine.evaluateEligibility(vorkath, account);

        // Cannot be READY when missing mandatory quest
        Assert.assertNotEquals(BossEligibilityStatus.READY, el.getStatus());
        Assert.assertFalse(el.getMissingQuests().isEmpty());
    }

    @Test
    public void testAlmostReadySkillGap() {
        // Account has Slayer 85, Cerberus requires 91 (gap of 6 -> locked)
        // Kraken requires 87 (gap of 2 -> almost ready!)
        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) reals.put(s, 80);
        reals.put(Skill.SLAYER, 85);
        reals.put(Skill.MAGIC, 85);

        PlayerAccount account = new PlayerAccount("CloseSlayer", 100, 1600, reals, reals, new HashMap<>(), new HashSet<>(), true);

        BossData kraken = bossDatabase.getBoss("kraken");
        BossEligibility krakenEl = engine.evaluateEligibility(kraken, account);
        Assert.assertEquals(BossEligibilityStatus.ALMOST_READY, krakenEl.getStatus());
        Assert.assertEquals(1, krakenEl.getMissingSkills().size());
    }
}
