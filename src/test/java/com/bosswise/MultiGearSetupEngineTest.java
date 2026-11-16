package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterData;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MultiGearSetupEngineTest {
    private GearOptimizerService optimizerService;
    private MonsterDatabase monsterDatabase;
    private BossDatabase bossDatabase;
    private EquipmentDatabase equipmentDatabase;

    @Before
    public void setUp() {
        equipmentDatabase = new EquipmentDatabase();
        monsterDatabase = new MonsterDatabase();
        bossDatabase = new BossDatabase();
        GePriceService priceService = new GePriceService(null);
        optimizerService = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, new CombatCalculationService());
    }

    @Test
    public void testGenerateAllViableSetupsForScurrius() {
        BossData scurrius = bossDatabase.getBoss("scurrius");
        assertNotNull("Scurrius should be indexed", scurrius);

        PlayerAccount account = PlayerAccount.createDefault();
        List<OptimizedGearSetup> setups = optimizerService.generateAllViableSetups(scurrius, null, account, 10_000_000L, false);

        assertNotNull("Setups list must not be null", setups);
        assertTrue("Scurrius should generate multiple viable setups (at least 5)", setups.size() >= 5);

        // Verify each setup has valid DPS, max hit, and equipment
        for (OptimizedGearSetup s : setups) {
            assertTrue("DPS should be positive", s.getCalculatedDps() > 0.0);
            assertTrue("Max hit should be positive", s.getMaxHit() > 0);
            assertNotNull("Weapon must be chosen", s.getItem(EquipmentSlot.WEAPON));
        }
    }

    @Test
    public void testKreearraFiltersOutMelee() {
        BossData kree = bossDatabase.getBoss("kreearra");
        assertNotNull("Kree'arra should be indexed", kree);

        PlayerAccount account = PlayerAccount.createDefault();
        List<OptimizedGearSetup> setups = optimizerService.generateAllViableSetups(kree, null, account, 50_000_000L, false);

        assertNotNull(setups);
        assertFalse("Setups should not be empty", setups.isEmpty());

        // Kree'arra does not allow melee!
        for (OptimizedGearSetup s : setups) {
            assertFalse("Melee must not be recommended against Kree'arra", s.getCombatStyle() == CombatStyle.MELEE);
        }
    }

    @Test
    public void testCorporealBeastSpearEnforcement() {
        BossData corp = bossDatabase.getBoss("corporeal_beast");
        assertNotNull("Corp should be indexed", corp);

        PlayerAccount account = PlayerAccount.createDefault();
        List<OptimizedGearSetup> setups = optimizerService.generateAllViableSetups(corp, null, account, 100_000_000L, false);

        assertNotNull(setups);
        assertFalse(setups.isEmpty());

        // At Corp, spear or Osmumten's fang is mandatory for melee
        for (OptimizedGearSetup s : setups) {
            if (s.getCombatStyle() == CombatStyle.MELEE) {
                EquipmentItem weapon = s.getItem(EquipmentSlot.WEAPON);
                assertNotNull("Weapon should be equipped", weapon);
                assertTrue("Corp weapon must be a spear or fang: " + weapon.getName(),
                        weapon.isSpear() || weapon.getName().toLowerCase().contains("fang") || weapon.getName().toLowerCase().contains("halberd"));
            }
        }
    }

    @Test
    public void testMissingRequirementsDetection() {
        PlayerAccount freshAccount = PlayerAccount.createDefault();
        // createDefault has 80 Attack, 80 Strength, 80 Defence, 82 Ranged, 80 Magic

        EquipmentItem torvaHelm = equipmentDatabase.getItemByName("Torva full helm");
        assertNotNull(torvaHelm);

        // Torva full helm requires 80 Defence
        PlayerAccount account80 = PlayerAccount.builder("account80")
                .level(Skill.DEFENCE, 80)
                .build();
        List<String> missing = optimizerService.getMissingRequirements(torvaHelm, account80);
        assertTrue("80 defence account can equip Torva full helm", missing.isEmpty());

        // Account with 60 defence
        PlayerAccount lowDef = PlayerAccount.builder("lowdef")
                .level(Skill.DEFENCE, 60)
                .build();

        List<String> missingLow = optimizerService.getMissingRequirements(torvaHelm, lowDef);
        assertFalse("Low defence account should show missing requirements", missingLow.isEmpty());
        assertTrue("Missing text should mention Defence", missingLow.get(0).contains("Defence"));
    }

    @Test
    public void testBoneWeaponsNeverRecommendedAtNonRatBosses() {
        PlayerAccount account = PlayerAccount.createDefault();

        // 1. Vorkath (Dragon & Undead)
        BossData vorkath = bossDatabase.getBoss("vorkath");
        List<OptimizedGearSetup> vorkathSetups = optimizerService.generateAllViableSetups(vorkath, null, account, 1_000_000L, false);
        for (OptimizedGearSetup s : vorkathSetups) {
            EquipmentItem weapon = s.getItem(EquipmentSlot.WEAPON);
            if (weapon != null) {
                assertFalse("Vorkath setup must not recommend bone weapons: " + weapon.getName(),
                        weapon.getName().toLowerCase().contains("bone ") || weapon.isRatbane());
            }
        }

        // 2. Barrows Brothers (Magic)
        BossData barrows = bossDatabase.getBoss("barrows");
        List<OptimizedGearSetup> barrowsSetups = optimizerService.generateAllViableSetups(barrows, null, account, 500_000L, false);
        for (OptimizedGearSetup s : barrowsSetups) {
            EquipmentItem weapon = s.getItem(EquipmentSlot.WEAPON);
            if (weapon != null) {
                assertFalse("Barrows setup must not recommend bone weapons: " + weapon.getName(),
                        weapon.getName().toLowerCase().contains("bone ") || weapon.isRatbane());
            }
        }

        // 3. Giant Mole (Melee)
        BossData mole = bossDatabase.getBoss("giant_mole");
        List<OptimizedGearSetup> moleSetups = optimizerService.generateAllViableSetups(mole, null, account, 500_000L, false);
        for (OptimizedGearSetup s : moleSetups) {
            EquipmentItem weapon = s.getItem(EquipmentSlot.WEAPON);
            if (weapon != null) {
                assertFalse("Mole setup must not recommend bone weapons: " + weapon.getName(),
                        weapon.getName().toLowerCase().contains("bone ") || weapon.isRatbane());
            }
        }

        // 4. Scurrius (Rat) - Bone weapons SHOULD be allowed here!
        BossData scurrius = bossDatabase.getBoss("scurrius");
        List<OptimizedGearSetup> scurriusSetups = optimizerService.generateAllViableSetups(scurrius, null, account, 1_000_000L, false);
        boolean hasRatbane = false;
        for (OptimizedGearSetup s : scurriusSetups) {
            EquipmentItem weapon = s.getItem(EquipmentSlot.WEAPON);
            if (weapon != null && (weapon.isRatbane() || weapon.getName().toLowerCase().contains("bone "))) {
                hasRatbane = true;
                break;
            }
        }
        assertTrue("Scurrius should utilize Ratbane bone weapons", hasRatbane);
    }
}
