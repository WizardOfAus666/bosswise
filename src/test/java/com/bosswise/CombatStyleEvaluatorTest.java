package com.bosswise;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.MonsterData;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.StyleEvaluation;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.CombatStyleEvaluator;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Skill;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CombatStyleEvaluatorTest {
    private CombatStyleEvaluator evaluator;
    private MonsterDatabase monsterDatabase;
    private PlayerAccount account;

    @Before
    public void setUp() {
        EquipmentDatabase equipmentDatabase = new EquipmentDatabase();
        GePriceService priceService = new GePriceService(null);
        CombatCalculationService calcService = new CombatCalculationService();
        monsterDatabase = new MonsterDatabase();
        GearOptimizerService optimizerService = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, calcService);
        evaluator = new CombatStyleEvaluator(optimizerService, calcService);

        Map<Skill, Integer> stats = new EnumMap<>(Skill.class);
        for (Skill s : Skill.values()) stats.put(s, 85);
        stats.put(Skill.ATTACK, 85);
        stats.put(Skill.STRENGTH, 85);
        stats.put(Skill.DEFENCE, 80);
        stats.put(Skill.RANGED, 85);
        stats.put(Skill.MAGIC, 85);
        stats.put(Skill.PRAYER, 70);
        stats.put(Skill.HITPOINTS, 85);

        account = new PlayerAccount("TestAdventurer", 105, 1750, stats, stats, java.util.Collections.emptyMap(), java.util.Collections.emptySet(), true);
    }

    @Test
    public void testKreearraMeleeImmunityExcluded() {
        MonsterData kreearra = monsterDatabase.getMonster("kreearra");
        Assert.assertNotNull(kreearra);

        List<StyleEvaluation> styles = evaluator.evaluateStyles(
                kreearra, kreearra.getPrimaryVariant(), 10_000_000L, account, false
        );

        Assert.assertFalse("Evaluations should not be empty", styles.isEmpty());
        for (StyleEvaluation se : styles) {
            Assert.assertNotEquals("Melee must not be evaluated for Kree'arra (melee immune)",
                    CombatStyle.MELEE, se.getStyle());
        }
        Assert.assertEquals("Top recommended style for Kree'arra must be RANGED",
                CombatStyle.RANGED, styles.get(0).getStyle());
    }

    @Test
    public void testSarachnisFavorsMeleeCrush() {
        MonsterData sarachnis = monsterDatabase.getMonster("sarachnis");
        Assert.assertNotNull(sarachnis);

        List<StyleEvaluation> styles = evaluator.evaluateStyles(
                sarachnis, sarachnis.getPrimaryVariant(), 10_000_000L, account, false
        );

        Assert.assertFalse("Evaluations should not be empty", styles.isEmpty());
        Assert.assertEquals("Sarachnis should strongly favor MELEE",
                CombatStyle.MELEE, styles.get(0).getStyle());
    }
}
