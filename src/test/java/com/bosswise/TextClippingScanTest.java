package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.scanner.QuestScannerService;
import com.bosswise.service.*;
import com.bosswise.ui.BossWisePluginPanel;
import java.awt.*;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.junit.Test;

public class TextClippingScanTest {

    @Test
    public void scanAllTabsForTextClipping() throws Exception {
        List<String> clippedComponents = new ArrayList<>();

        SwingUtilities.invokeAndWait(() -> {
            QuestScannerService questScanner = new QuestScannerService();
            AccountScannerService accountScanner = new AccountScannerService(null, questScanner);
            accountScanner.setCustomAccount(PlayerAccount.createDefault());

            GePriceService priceService = new GePriceService(null);
            BossDatabase bossDatabase = new BossDatabase();
            MonsterDatabase monsterDatabase = new MonsterDatabase();
            EquipmentDatabase equipmentDatabase = new EquipmentDatabase();

            BossRecommendationEngine recommendationEngine = new BossRecommendationEngine(bossDatabase);
            CombatCalculationService combatCalc = new CombatCalculationService();
            GearOptimizerService gearOptimizer = new GearOptimizerService(equipmentDatabase, priceService, monsterDatabase, combatCalc);
            GearUpgradeAdvisor upgradeAdvisor = new GearUpgradeAdvisor(equipmentDatabase, gearOptimizer, priceService);
            InventoryBuilderService inventoryService = new InventoryBuilderService(priceService);
            ProgressionService progressionService = new ProgressionService(bossDatabase, recommendationEngine);
            CombatStyleEvaluator styleEvaluator = new CombatStyleEvaluator(gearOptimizer, combatCalc);
            CurrentGearScannerService gearScanner = new CurrentGearScannerService(null, equipmentDatabase, priceService);
            DpsLabService dpsLab = new DpsLabService(combatCalc, priceService, equipmentDatabase);
            SlayerTaskService slayerService = new SlayerTaskService(null, null);
            LootTrackerService lootTracker = new LootTrackerService(priceService, bossDatabase, monsterDatabase, null);
            MonsterProfitService profitService = new MonsterProfitService();
            DropRateCalculatorService dropCalc = new DropRateCalculatorService();
            GearRoadmapService roadmapService = new GearRoadmapService(equipmentDatabase, gearOptimizer, priceService);
            CombatAchievementService caService = new CombatAchievementService(null);
            CollectionLogService collLogService = new CollectionLogService(null);
            BankCheckerService bankChecker = new BankCheckerService(null, equipmentDatabase, priceService);
            ExportService exportService = new ExportService();

            BossWiseConfig config = (BossWiseConfig) Proxy.newProxyInstance(
                    BossWiseConfig.class.getClassLoader(),
                    new Class<?>[]{BossWiseConfig.class},
                    (proxy, method, mArgs) -> {
                        if ("defaultBudget".equals(method.getName())) return 5_000_000;
                        if ("preferredCombatStyle".equals(method.getName())) return CombatStyle.MELEE;
                        if ("equipmentPriority".equals(method.getName())) return GearOptimizerService.PriorityMode.BALANCED;
                        if ("showLockedBosses".equals(method.getName())) return true;
                        if ("showGroupBosses".equals(method.getName())) return true;
                        if ("compactLayout".equals(method.getName())) return false;
                        if ("autoRefreshOnStatChange".equals(method.getName())) return true;
                        return null;
                    }
            );

            BossWisePluginPanel panel = new BossWisePluginPanel(
                    accountScanner, bossDatabase, monsterDatabase, recommendationEngine,
                    gearOptimizer, upgradeAdvisor, inventoryService, progressionService,
                    combatCalc, styleEvaluator, gearScanner, dpsLab, slayerService,
                    lootTracker, profitService, dropCalc, roadmapService, caService,
                    collLogService, bankChecker, exportService, config
            );

            String[] tabs = {
                    "DASHBOARD", "BOSS_FINDER", "GEAR_OPTIMIZER", "DPS_LAB",
                    "SLAYER", "LOOT_TRACKER", "PROFIT_FINDER", "INVENTORY",
                    "PROGRESSION", "BOSS_GUIDES", "SETTINGS"
            };

            int[] widths = {225, 242, 280};

            for (int w : widths) {
                panel.setSize(w, 800);
                for (String tab : tabs) {
                    panel.setActiveTab(tab);
                    for (int pass = 0; pass < 4; pass++) {
                        GuideLayoutTest.layoutTree(panel);
                    }
                    scanContainer(panel, tab + " @" + w + "px", w, clippedComponents);
                }
            }
        });

        System.out.println("=== CLIPPING SCAN RESULTS ===");
        System.out.println("Total potential clipping items found: " + clippedComponents.size());
        for (String issue : clippedComponents) {
            System.out.println("[CLIPPED] " + issue);
        }
    }

    private void scanContainer(Container container, String context, int maxWidth, List<String> issues) {
        for (Component c : container.getComponents()) {
            if (!c.isVisible()) continue;

            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                String text = lbl.getText();
                if (text != null && !text.trim().isEmpty() && !text.startsWith("<html>")) {
                    FontMetrics fm = lbl.getFontMetrics(lbl.getFont());
                    int textWidth = fm.stringWidth(text);
                    int compWidth = lbl.getWidth();
                    // If component width is significantly less than text width (by more than 4px margin)
                    if (compWidth > 0 && compWidth < textWidth - 4) {
                        issues.add(context + " | JLabel: \"" + text + "\" textWidth=" + textWidth + " > compWidth=" + compWidth);
                    }
                }
            } else if (c instanceof JButton) {
                JButton btn = (JButton) c;
                String text = btn.getText();
                if (text != null && !text.trim().isEmpty() && !text.startsWith("<html>")) {
                    FontMetrics fm = btn.getFontMetrics(btn.getFont());
                    int textWidth = fm.stringWidth(text);
                    Insets in = btn.getInsets();
                    int insetsW = (in != null) ? (in.left + in.right) : 8;
                    int compWidth = btn.getWidth();
                    if (compWidth > 0 && compWidth < (textWidth + insetsW - 2)) {
                        issues.add(context + " | JButton: \"" + text + "\" requiredWidth=" + (textWidth + insetsW) + " > compWidth=" + compWidth);
                    }
                }
            }

            if (c instanceof Container) {
                scanContainer((Container) c, context, maxWidth, issues);
            }
        }
    }
}
