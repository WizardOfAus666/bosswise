package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.ProgressionGoal;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.ProgressionService;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgressionRoadmapTest {
    private BossDatabase bossDatabase;
    private ProgressionService progressionService;

    @Before
    public void setup() {
        bossDatabase = new BossDatabase();
        BossRecommendationEngine engine = new BossRecommendationEngine(bossDatabase);
        progressionService = new ProgressionService(bossDatabase, engine);
    }

    @Test
    public void testRoadmapGeneration() {
        PlayerAccount account = PlayerAccount.createDefault();
        List<ProgressionGoal> roadmap = progressionService.generateRoadmap(account);

        Assert.assertNotNull(roadmap);
        Assert.assertFalse(roadmap.isEmpty());

        for (ProgressionGoal goal : roadmap) {
            Assert.assertNotNull(goal.getTitle());
            Assert.assertNotNull(goal.getDescription());
            Assert.assertTrue(goal.getPercentage() >= 0 && goal.getPercentage() <= 100);
        }
    }
}
