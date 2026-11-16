package com.bosswise;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterData;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.StrategyConfidence;
import com.bosswise.model.strategy.StrategyMethod;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Comprehensive automated verification test auditing all 75 supported OSRS monsters
 * against authentic OSRS Wiki strategy mechanics, equipment restrictions, and combat rules.
 */
public class UniversalMonsterGearVerificationTest {

    private MonsterDatabase monsterDatabase;
    private MonsterStrategyDatabase strategyDatabase;
    private EquipmentDatabase equipmentDatabase;

    @Before
    public void setUp() {
        monsterDatabase = new MonsterDatabase();
        strategyDatabase = new MonsterStrategyDatabase();
        equipmentDatabase = new EquipmentDatabase();
    }

    @Test
    public void testAll75MonstersExistInStrategyDatabase() {
        var allMonsters = monsterDatabase.getAllMonsters().values();
        assertEquals("Monster database must contain 75 monsters", 75, allMonsters.size());

        for (MonsterData monster : allMonsters) {
            MonsterStrategy strat = strategyDatabase.getStrategy(monster.getId());
            assertNotNull("Monster '" + monster.getName() + "' (ID: " + monster.getId() + ") must have strategy", strat);
            assertEquals("Strategy monsterId must match", monster.getId(), strat.getMonsterId());
            assertEquals(StrategyConfidence.VERIFIED, strat.getConfidence());
            assertFalse("Strategy must have at least one method", strat.getMethods().isEmpty());
            assertTrue("Strategy must have a valid Wiki URL", strat.getWikiUrl().startsWith("https://oldschool.runescape.wiki/w/"));
        }
    }

    @Test
    public void testAllStrategyGearItemsExistInEquipmentDatabase() {
        for (MonsterStrategy strat : strategyDatabase.getAllStrategies().values()) {
            for (StrategyMethod method : strat.getMethods()) {
                Map<EquipmentSlot, String> gear = method.getGearSetup();
                for (Map.Entry<EquipmentSlot, String> entry : gear.entrySet()) {
                    EquipmentSlot slot = entry.getKey();
                    String itemName = entry.getValue();
                    if (itemName == null || itemName.trim().isEmpty() || "None".equalsIgnoreCase(itemName)) {
                        continue;
                    }
                    EquipmentItem item = equipmentDatabase.getItemByName(itemName);
                    assertNotNull("Item '" + itemName + "' in slot " + slot + " for monster '"
                            + strat.getMonsterName() + "' (Method: " + method.getTitle() + ") must exist in EquipmentDatabase", item);
                }
            }
        }
    }

    @Test
    public void testNoTwoHandedWeaponsWithShield() {
        for (MonsterStrategy strat : strategyDatabase.getAllStrategies().values()) {
            for (StrategyMethod method : strat.getMethods()) {
                Map<EquipmentSlot, String> gear = method.getGearSetup();
                String weaponName = gear.get(EquipmentSlot.WEAPON);
                if (weaponName != null) {
                    EquipmentItem weapon = equipmentDatabase.getItemByName(weaponName);
                    if (weapon != null && weapon.isTwoHanded()) {
                        String shieldName = gear.get(EquipmentSlot.SHIELD);
                        assertTrue("Two-handed weapon '" + weaponName + "' must not equip shield in method '"
                                + method.getTitle() + "' (had: " + shieldName + ")",
                                shieldName == null || shieldName.trim().isEmpty() || "None".equalsIgnoreCase(shieldName));
                    }
                }
            }
        }
    }

    @Test
    public void testKuraskAndTurothLeafBladedRequirement() {
        String[] leafBaneMonsters = {"kurask", "turoth"};
        for (String id : leafBaneMonsters) {
            MonsterStrategy strat = strategyDatabase.getStrategy(id);
            assertNotNull(strat);
            for (StrategyMethod method : strat.getMethods()) {
                String weaponName = method.getGearSetup().get(EquipmentSlot.WEAPON);
                assertNotNull("Weapon must be present for " + id, weaponName);
                EquipmentItem weapon = equipmentDatabase.getItemByName(weaponName);
                assertNotNull(weapon);

                if (method.getCombatStyle() == CombatStyle.MELEE) {
                    assertTrue("Melee weapon against " + id + " must be leaf-bladed: " + weaponName,
                            weapon.isLeafBladed() || weaponName.toLowerCase().contains("leaf-bladed"));
                } else if (method.getCombatStyle() == CombatStyle.RANGED) {
                    String ammoName = method.getGearSetup().get(EquipmentSlot.AMMO);
                    assertTrue("Ranged ammunition against " + id + " must be broad ammunition: " + ammoName,
                            ammoName != null && (ammoName.toLowerCase().contains("broad") || weapon.isLeafBladed()));
                }
            }
        }
    }

    @Test
    public void testBasiliskMirrorShieldRequirement() {
        String[] basiliskMonsters = {"basilisk", "basilisk_knight"};
        for (String id : basiliskMonsters) {
            MonsterStrategy strat = strategyDatabase.getStrategy(id);
            assertNotNull(strat);
            for (StrategyMethod method : strat.getMethods()) {
                String shieldName = method.getGearSetup().get(EquipmentSlot.SHIELD);
                assertNotNull("Shield must be equipped for " + id, shieldName);
                assertTrue("Shield against " + id + " must be Mirror shield or V's shield: " + shieldName,
                        shieldName.equalsIgnoreCase("Mirror shield") || shieldName.equalsIgnoreCase("V's shield"));
            }
        }
    }

    @Test
    public void testSkeletalWyvernDragonfireProtection() {
        MonsterStrategy wyvern = strategyDatabase.getStrategy("skeletal_wyvern");
        assertNotNull(wyvern);
        for (StrategyMethod method : wyvern.getMethods()) {
            String shieldName = method.getGearSetup().get(EquipmentSlot.SHIELD);
            assertNotNull("Shield must be equipped against Skeletal Wyvern", shieldName);
            EquipmentItem shield = equipmentDatabase.getItemByName(shieldName);
            assertNotNull(shield);
            assertTrue("Shield against Skeletal Wyvern must provide wyvern/dragonfire protection: " + shieldName,
                    shield.isDragonfireProtection() || shieldName.toLowerCase().contains("wyvern shield") || shieldName.toLowerCase().contains("dragonfire"));
        }
    }

    @Test
    public void testCorporealBeastSpearRequirement() {
        MonsterStrategy corp = strategyDatabase.getStrategy("corporeal_beast");
        assertNotNull(corp);
        for (StrategyMethod method : corp.getMethods()) {
            if (method.getCombatStyle() == CombatStyle.MELEE) {
                String weaponName = method.getGearSetup().get(EquipmentSlot.WEAPON);
                assertNotNull("Corporeal beast weapon must be set", weaponName);
                EquipmentItem weapon = equipmentDatabase.getItemByName(weaponName);
                assertNotNull(weapon);
                assertTrue("Corporeal beast weapon must be a spear or Osmumten's fang: " + weaponName,
                        weapon.isSpear() || weaponName.toLowerCase().contains("fang"));
            }
        }
    }

    @Test
    public void testKreearraAllowsOnlyRangedOrMagic() {
        MonsterStrategy kree = strategyDatabase.getStrategy("kreearra");
        assertNotNull("Kree'arra strategy must exist", kree);
        for (StrategyMethod method : kree.getMethods()) {
            assertFalse("Kree'arra cannot be attacked with melee", method.getCombatStyle() == CombatStyle.MELEE);
        }
    }

    @Test
    public void testKrakenAllowsOnlyMagic() {
        MonsterStrategy kraken = strategyDatabase.getStrategy("kraken");
        assertNotNull("Kraken strategy must exist", kraken);
        for (StrategyMethod method : kraken.getMethods()) {
            assertEquals("Kraken can only be damaged by Magic", CombatStyle.MAGIC, method.getCombatStyle());
        }
    }
}
