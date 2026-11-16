package com.bosswise.service;

import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem.AttackType;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.StyleEvaluation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CombatStyleEvaluator {
    private final GearOptimizerService optimizerService;
    private final CombatCalculationService calculationService;

    @Inject
    public CombatStyleEvaluator(GearOptimizerService optimizerService, CombatCalculationService calculationService) {
        this.optimizerService = optimizerService;
        this.calculationService = calculationService;
    }

    public List<StyleEvaluation> evaluateStyles(
            MonsterData monster,
            MonsterVariant variant,
            long budget,
            PlayerAccount account,
            boolean onSlayerTask) {

        if (monster == null) return Collections.emptyList();
        if (variant == null) variant = monster.getPrimaryVariant();

        MonsterCombatProfile profile = monster.getCombatProfile();
        List<StyleEvaluation> evaluations = new ArrayList<>();

        CombatStyle[] styles = new CombatStyle[]{CombatStyle.MELEE, CombatStyle.RANGED, CombatStyle.MAGIC};

        for (CombatStyle style : styles) {
            // Check if style is allowed by monster mechanics
            if (profile != null && !profile.isStyleAllowed(style)) {
                continue;
            }

            // Generate best value setup for this style
            OptimizedGearSetup setup = optimizerService.generateSetup(
                    "Best Value",
                    monster.toBossData(),
                    style,
                    budget,
                    account,
                    GearOptimizerService.PriorityMode.BALANCED
            );

            // Calculate true OSRS combat performance
            CombatResult result = calculationService.calculateCombat(
                    setup.getEquipment(),
                    variant,
                    profile,
                    account,
                    style,
                    onSlayerTask
            );

            evaluations.add(new StyleEvaluation(
                    style,
                    result.getAttackType(),
                    result,
                    setup,
                    result.getDps(),
                    "",
                    false
            ));
        }

        // Rank by calculated DPS descending
        evaluations.sort(Comparator.comparingDouble(StyleEvaluation::getDps).reversed());

        if (evaluations.isEmpty()) return Collections.emptyList();

        List<StyleEvaluation> finalRankings = new ArrayList<>();
        double topDps = evaluations.get(0).getDps();

        for (int i = 0; i < evaluations.size(); i++) {
            StyleEvaluation eval = evaluations.get(i);
            boolean isTop = (i == 0);

            String reason;
            if (isTop) {
                reason = String.format("Optimal: Calculated highest expected damage (%.2f DPS with %s).",
                        eval.getDps(), eval.getCombatResult().getFormattedHitChance());
                if (profile != null && profile.getElementalWeaknessPercent() > 0 && eval.getStyle() == CombatStyle.MAGIC) {
                    reason += String.format(" Leverages +%d%% %s elemental weakness.",
                            profile.getElementalWeaknessPercent(), profile.getElementalWeakness().getDisplayName());
                }
            } else {
                double diff = topDps - eval.getDps();
                reason = String.format("Alternative: Produces %.2f DPS (-%.2f DPS lower than %s).",
                        eval.getDps(), diff, evaluations.get(0).getStyle().getDisplayName());
            }

            finalRankings.add(new StyleEvaluation(
                    eval.getStyle(),
                    eval.getAttackType(),
                    eval.getCombatResult(),
                    eval.getSetup(),
                    eval.getDps(),
                    reason,
                    isTop
            ));
        }

        return finalRankings;
    }
}
