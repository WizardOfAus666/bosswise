package com.bosswise;

import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.QuestInfo;
import com.bosswise.scanner.QuestScannerService;
import java.util.Collections;
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

public class QuestScannerTest {
    private QuestScannerService questScanner;

    @Before
    public void setup() {
        questScanner = new QuestScannerService();
    }

    @Test
    public void testQuestDatabaseLoaded() {
        Map<String, QuestInfo> quests = questScanner.getAllQuests();
        Assert.assertFalse(quests.isEmpty());
        Assert.assertTrue(quests.containsKey("Dragon Slayer II"));
        Assert.assertTrue(quests.containsKey("Song of the Elves"));
        Assert.assertTrue(quests.containsKey("Recipe for Disaster"));

        QuestInfo ds2 = quests.get("Dragon Slayer II");
        Assert.assertEquals(Quest.DRAGON_SLAYER_II, ds2.getQuest());
        Assert.assertTrue(ds2.getUnlockedBosses().contains("Vorkath"));
        Assert.assertEquals(75, (int) ds2.getSkillRequirements().get(Skill.MAGIC));
    }

    @Test
    public void testPlayerQuestCompletionDetection() {
        Map<Quest, QuestState> questStates = new HashMap<>();
        questStates.put(Quest.DRAGON_SLAYER_I, QuestState.FINISHED);
        questStates.put(Quest.DRAGON_SLAYER_II, QuestState.IN_PROGRESS);

        Set<String> manual = new HashSet<>();
        manual.add("Dragon Slayer I");

        PlayerAccount account = new PlayerAccount("Quester", 90, 1400,
                new EnumMap<>(Skill.class), new EnumMap<>(Skill.class),
                questStates, manual, true);

        Assert.assertTrue(account.isQuestCompleted(Quest.DRAGON_SLAYER_I, "Dragon Slayer I"));
        Assert.assertFalse(account.isQuestCompleted(Quest.DRAGON_SLAYER_II, "Dragon Slayer II"));
        Assert.assertEquals(QuestState.IN_PROGRESS, account.getQuestState(Quest.DRAGON_SLAYER_II));
        Assert.assertEquals(QuestState.NOT_STARTED, account.getQuestState(Quest.SONG_OF_THE_ELVES));
    }
}
