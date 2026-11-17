package com.bosswise;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.service.SpecialEffectService;
import com.bosswise.service.SpecialEffectService.CombatMultipliers;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpecialEffectServiceTest {
    private SpecialEffectService effectService;
    private EquipmentDatabase equipmentDb;

    @Before
    public void setUp() {
        effectService = new SpecialEffectService();
        equipmentDb = new EquipmentDatabase();
    }

    @Test
    public void testDragonbaneBonus() {
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        EquipmentItem dhl = equipmentDb.getItemByName("Dragon hunter lance");
        gear.put(EquipmentSlot.WEAPON, dhl);

        MonsterCombatProfile dragon = MonsterCombatProfile.builder("vorkath", "Vorkath", CombatStyle.RANGED)
                .dragon()
                .build();

        CombatMultipliers mult = effectService.getMeleeMultipliers(gear, dragon, false);
        assertEquals(1.20, mult.getAccuracyMultiplier(), 0.001);
        assertEquals(1.20, mult.getDamageMultiplier(), 0.001);
    }

    @Test
    public void testDemonbaneEmberlightBonus() {
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        EquipmentItem emberlight = equipmentDb.getItemByName("Emberlight");
        gear.put(EquipmentSlot.WEAPON, emberlight);

        MonsterCombatProfile demon = MonsterCombatProfile.builder("kril", "K'ril Tsutsaroth", CombatStyle.MELEE)
                .demon()
                .build();

        CombatMultipliers mult = effectService.getMeleeMultipliers(gear, demon, false);
        assertEquals(1.70, mult.getAccuracyMultiplier(), 0.001);
        assertEquals(1.70, mult.getDamageMultiplier(), 0.001);
    }

    @Test
    public void testSalveDoesNotStackWithSlayerHelm() {
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        EquipmentItem salveEi = equipmentDb.getItemByName("Salve amulet (ei)");
        EquipmentItem slayerHelm = equipmentDb.getItemByName("Slayer helmet (i)");
        gear.put(EquipmentSlot.AMULET, salveEi);
        gear.put(EquipmentSlot.HEAD, slayerHelm);

        MonsterCombatProfile undead = MonsterCombatProfile.builder("vorkath", "Vorkath", CombatStyle.RANGED)
                .undead()
                .build();

        // On slayer task AND against undead with Salve (ei): Salve takes priority (+20%), does NOT stack with Slayer helm
        CombatMultipliers mult = effectService.getMeleeMultipliers(gear, undead, true);
        assertEquals(1.20, mult.getAccuracyMultiplier(), 0.001);
        assertEquals(1.20, mult.getDamageMultiplier(), 0.001);
        assertTrue(mult.getEffectSummary().contains("Salve"));
    }

    @Test
    public void testVoidKnightMeleeStacksWithSalve() {
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.HEAD, equipmentDb.getItemByName("Void melee helm"));
        gear.put(EquipmentSlot.BODY, equipmentDb.getItemByName("Void knight top"));
        gear.put(EquipmentSlot.LEGS, equipmentDb.getItemByName("Void knight robe"));
        gear.put(EquipmentSlot.GLOVES, equipmentDb.getItemByName("Void knight gloves"));
        gear.put(EquipmentSlot.AMULET, equipmentDb.getItemByName("Salve amulet (ei)"));

        MonsterCombatProfile undead = MonsterCombatProfile.builder("ankou", "Ankou", CombatStyle.MELEE)
                .undead()
                .build();

        // Void (10%) stacks multiplicatively with Salve (ei) (20%)
        CombatMultipliers mult = effectService.getMeleeMultipliers(gear, undead, false);
        assertEquals(1.20 * 1.10, mult.getAccuracyMultiplier(), 0.001);
        assertEquals(1.20 * 1.10, mult.getDamageMultiplier(), 0.001);
        assertTrue(mult.getEffectSummary().contains("Void Knight Melee"));
        assertTrue(mult.getEffectSummary().contains("Salve"));
    }

    @Test
    public void testRatbaneFlatBonus() {
        Map<EquipmentSlot, EquipmentItem> gear = new EnumMap<>(EquipmentSlot.class);
        gear.put(EquipmentSlot.WEAPON, equipmentDb.getItemByName("Bone mace"));

        MonsterCombatProfile rat = MonsterCombatProfile.builder("scurrius", "Scurrius", CombatStyle.MELEE)
                .rat()
                .build();

        CombatMultipliers mult = effectService.getMeleeMultipliers(gear, rat, false);
        assertEquals(10, mult.getFlatMaxHitBonus());
        assertTrue(mult.getEffectSummary().contains("Bone mace active"));
    }
}
