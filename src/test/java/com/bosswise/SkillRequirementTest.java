package com.bosswise;

import com.bosswise.model.PlayerAccount;
import com.bosswise.model.SkillRequirement;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Test;

public class SkillRequirementTest {

    @Test
    public void testPermanentSkillRequirement() {
        SkillRequirement req = new SkillRequirement(Skill.SLAYER, 87, false, null);
        Assert.assertEquals(Skill.SLAYER, req.getSkill());
        Assert.assertEquals(87, req.getLevel());
        Assert.assertFalse(req.isBoostable());
        Assert.assertEquals("Slayer 87", req.formatRequirement());

        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        reals.put(Skill.SLAYER, 86);
        boosteds.put(Skill.SLAYER, 91); // Wild pie boosted level

        PlayerAccount account = new PlayerAccount("TestPlayer", 100, 1500, reals, boosteds, new HashMap<>(), new HashSet<>(), true);

        // Non-boostable requirement must check real level and fail
        Assert.assertFalse(account.meetsSkill(req));

        // When real level is 87, it should pass
        reals.put(Skill.SLAYER, 87);
        PlayerAccount accountPass = new PlayerAccount("TestPlayer", 100, 1500, reals, boosteds, new HashMap<>(), new HashSet<>(), true);
        Assert.assertTrue(accountPass.meetsSkill(req));
    }

    @Test
    public void testBoostableSkillRequirement() {
        SkillRequirement req = new SkillRequirement(Skill.AGILITY, 70, true, "Summer pie (+5)");
        Assert.assertTrue(req.isBoostable());
        Assert.assertEquals("Agility 70 (Boostable: Summer pie (+5))", req.formatRequirement());

        Map<Skill, Integer> reals = new EnumMap<>(Skill.class);
        Map<Skill, Integer> boosteds = new EnumMap<>(Skill.class);
        reals.put(Skill.AGILITY, 66);
        boosteds.put(Skill.AGILITY, 71); // Boosted with pie

        PlayerAccount account = new PlayerAccount("TestPlayer", 100, 1500, reals, boosteds, new HashMap<>(), new HashSet<>(), true);

        // Boostable requirement accepts boosted level
        Assert.assertTrue(account.meetsSkill(req));
    }
}
