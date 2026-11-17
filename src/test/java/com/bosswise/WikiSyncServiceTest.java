package com.bosswise;

import com.bosswise.model.PlayerAccount;
import com.bosswise.service.WikiSyncService;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Parser tests for WikiSync profiles. The JSON below is the real shape returned by
 * https://sync.runescape.wiki/runelite/player/&lt;name&gt;/STANDARD, trimmed to the
 * parts BossWise reads: levels keyed by display name, and quests keyed by name with
 * 0 = not started, 1 = in progress, 2 = finished.
 */
public class WikiSyncServiceTest {

    private static final String MAXED_PROFILE = "{"
            + "\"username\":\"Test Account\","
            + "\"timestamp\":\"2026-11-17T09:59:22.463Z\","
            + "\"quests\":{\".\":0,"
            + "\"Dragon Slayer II\":2,"
            + "\"Monkey Madness II\":1,"
            + "\"Song of the Elves\":2,"
            + "\"Desert Treasure I\":2,"
            + "\"Recipe for Disaster\":0},"
            + "\"levels\":{\"Attack\":99,\"Strength\":99,\"Defence\":99,\"Hitpoints\":99,\"Prayer\":99,"
            + "\"Ranged\":99,\"Magic\":99,\"Slayer\":99,\"Herblore\":99,\"Agility\":99,\"Sailing\":42},"
            + "\"achievement_diaries\":{},"
            + "\"combat_achievements\":[28,32],"
            + "\"collection_log\":[]"
            + "}";

    private static final String LOW_PROFILE = "{"
            + "\"username\":\"Fresh Acc\","
            + "\"quests\":{\"Dragon Slayer II\":0},"
            + "\"levels\":{\"Attack\":40,\"Strength\":45,\"Defence\":30,\"Hitpoints\":42,\"Prayer\":20,"
            + "\"Ranged\":35,\"Magic\":37,\"Slayer\":5}"
            + "}";

    private WikiSyncService service;

    @Before
    public void setUp() {
        service = new WikiSyncService();
    }

    @Test
    public void parsesLevels() {
        PlayerAccount account = service.parseProfile(MAXED_PROFILE);
        Assert.assertNotNull(account);
        Assert.assertEquals("Test Account", account.getUsername());
        Assert.assertEquals(99, account.getRealLevel(Skill.ATTACK));
        Assert.assertEquals(99, account.getRealLevel(Skill.SLAYER));
        Assert.assertEquals(99, account.getRealLevel(Skill.MAGIC));
    }

    @Test
    public void boostedLevelsMatchRealLevelsForALookup() {
        PlayerAccount account = service.parseProfile(MAXED_PROFILE);
        Assert.assertEquals(account.getRealLevel(Skill.STRENGTH), account.getBoostedLevel(Skill.STRENGTH));
    }

    @Test
    public void unlistedSkillsFallBackInsteadOfBreaking() {
        // Only 11 skills are in the fixture; the rest must still be safe to read.
        PlayerAccount account = service.parseProfile(MAXED_PROFILE);
        Assert.assertEquals(1, account.getRealLevel(Skill.MINING));
        // "Sailing" is not in every RuneLite build's Skill enum; it must not throw,
        // and it still counts toward the total level.
        Assert.assertTrue(account.getTotalLevel() >= 1032);
    }

    @Test
    public void computesCombatLevel() {
        Assert.assertEquals(126, service.parseProfile(MAXED_PROFILE).getCombatLevel());
        PlayerAccount low = service.parseProfile(LOW_PROFILE);
        Assert.assertTrue(low.getCombatLevel() > 3 && low.getCombatLevel() < 126);
    }

    @Test
    public void finishedQuestsAreCompletedAndInProgressOnesAreNot() {
        PlayerAccount account = service.parseProfile(MAXED_PROFILE);
        Assert.assertTrue(account.hasCompletedQuest("Dragon Slayer II"));
        Assert.assertTrue(account.hasCompletedQuest("Song of the Elves"));
        Assert.assertFalse("in progress is not complete", account.hasCompletedQuest("Monkey Madness II"));
        Assert.assertFalse(account.hasCompletedQuest("Recipe for Disaster"));
        Assert.assertEquals(3, account.getCompletedQuestNames().size());
    }

    @Test
    public void mapsQuestNamesOntoTheClientQuestEnum() {
        PlayerAccount account = service.parseProfile(MAXED_PROFILE);
        Assert.assertEquals(QuestState.FINISHED, account.getQuestState(Quest.DRAGON_SLAYER_II));
        Assert.assertEquals(QuestState.IN_PROGRESS, account.getQuestState(Quest.MONKEY_MADNESS_II));
        Assert.assertEquals(QuestState.NOT_STARTED, account.getQuestState(Quest.RECIPE_FOR_DISASTER));
    }

    @Test
    public void badInputReturnsNullRatherThanThrowing() {
        Assert.assertNull(service.parseProfile(null));
        Assert.assertNull(service.parseProfile(""));
        Assert.assertNull(service.parseProfile("{\"username\":\"No levels here\"}"));
        Assert.assertNull("malformed JSON must not throw", service.parseProfile("not json at all"));
    }

    @Test
    public void emptyUsernameIsRejectedWithoutANetworkCall() {
        final String[] message = new String[1];
        service.lookupAsync("   ", result -> {
            Assert.assertFalse(result.isSuccess());
            Assert.assertNull(result.getAccount());
            message[0] = result.getMessage();
        });
        Assert.assertNotNull("callback must fire synchronously for invalid input", message[0]);
    }

    @Test
    public void overlongUsernameIsRejectedWithoutANetworkCall() {
        final boolean[] called = new boolean[1];
        service.lookupAsync("thisnameistoolongforosrs", result -> {
            called[0] = true;
            Assert.assertFalse(result.isSuccess());
        });
        Assert.assertTrue(called[0]);
    }

    @Test
    public void describeMentionsTheAccount() {
        String line = WikiSyncService.describe(service.parseProfile(MAXED_PROFILE));
        Assert.assertTrue(line.contains("Test Account"));
        Assert.assertTrue(line.contains("126"));
    }
}
