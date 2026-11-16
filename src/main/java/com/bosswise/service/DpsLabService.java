package com.bosswise.service;

import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.DpsComparisonResult;
import com.bosswise.model.DpsComparisonResult.SetupEvaluation;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.PlayerAccount;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DpsLabService {
    private final CombatCalculationService combatCalculationService;
    private final GePriceService priceService;
    private final EquipmentDatabase equipmentDatabase;

    @Inject
    public DpsLabService(
            CombatCalculationService combatCalculationService,
            GePriceService priceService,
            EquipmentDatabase equipmentDatabase) {
        this.combatCalculationService = combatCalculationService;
        this.priceService = priceService;
        this.equipmentDatabase = equipmentDatabase;
    }

    public DpsComparisonResult compareSetups(
            String monsterName,
            MonsterVariant variant,
            MonsterCombatProfile profile,
            PlayerAccount account,
            CombatStyle combatStyle,
            Map<EquipmentSlot, EquipmentItem> setupAGear,
            String setupAName,
            Map<EquipmentSlot, EquipmentItem> setupBGear,
            String setupBName,
            String prayerOverride,
            String potionOverride,
            boolean onSlayerTask) {

        CombatResult resA = combatCalculationService.calculateCombat(
                setupAGear, variant, profile, account, combatStyle, onSlayerTask, prayerOverride, potionOverride);

        CombatResult resB = combatCalculationService.calculateCombat(
                setupBGear, variant, profile, account, combatStyle, onSlayerTask, prayerOverride, potionOverride);

        long costA = calculateTotalCost(setupAGear);
        long costB = calculateTotalCost(setupBGear);

        SetupEvaluation evalA = new SetupEvaluation(
                setupAName != null ? setupAName : "Setup A",
                setupAGear,
                resA,
                costA);

        SetupEvaluation evalB = new SetupEvaluation(
                setupBName != null ? setupBName : "Setup B",
                setupBGear,
                resB,
                costB);

        return new DpsComparisonResult(monsterName, combatStyle, evalA, evalB);
    }

    public long calculateTotalCost(Map<EquipmentSlot, EquipmentItem> equipment) {
        if (equipment == null) return 0L;
        long total = 0L;
        for (EquipmentItem item : equipment.values()) {
            if (item != null) {
                long price = item.getPrice();
                if (price == 0 && priceService != null) {
                    price = priceService.getPrice(item.getItemId(), 0);
                }
                total += Math.max(0, price);
            }
        }
        return total;
    }
}
