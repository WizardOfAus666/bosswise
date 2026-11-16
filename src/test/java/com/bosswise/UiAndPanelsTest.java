package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.QuestScannerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.ProgressionService;
import com.bosswise.ui.BossWisePluginPanel;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.junit.Assert;
import org.junit.Test;

public class UiAndPanelsTest {

    @Test
    public void testUiInitializationAndNavigation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QuestScannerService questScanner = new QuestScannerService();
            AccountScannerService accountScanner = new AccountScannerService(null, questScanner);
            accountScanner.setCustomAccount(PlayerAccount.createDefault());

            GePriceService priceService = new GePriceService(null);
            BossDatabase bossDatabase = new BossDatabase();
            EquipmentDatabase equipmentDatabase = new EquipmentDatabase();

            BossRecommendationEngine recommendationEngine = new BossRecommendationEngine(bossDatabase);
            GearOptimizerService gearOptimizer = new GearOptimizerService(equipmentDatabase, priceService);
            GearUpgradeAdvisor upgradeAdvisor = new GearUpgradeAdvisor(equipmentDatabase, gearOptimizer, priceService);
            InventoryBuilderService inventoryService = new InventoryBuilderService(priceService);
            ProgressionService progressionService = new ProgressionService(bossDatabase, recommendationEngine);

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
                    accountScanner,
                    bossDatabase,
                    recommendationEngine,
                    gearOptimizer,
                    upgradeAdvisor,
                    inventoryService,
                    progressionService,
                    config
            );

            Assert.assertNotNull(panel);

            // Test navigation between all tabs
            panel.switchTab("DASHBOARD");
            panel.switchTab("BOSS_FINDER");
            panel.switchTab("GEAR_OPTIMIZER");
            panel.switchTab("BOSS_GUIDES");
            panel.switchTab("INVENTORY");
            panel.switchTab("PROGRESSION");
            panel.switchTab("SETTINGS");

            // Test cross-navigation with selected boss
            panel.openGearOptimizer(bossDatabase.getBoss("vorkath"));
            panel.openBossGuide(bossDatabase.getBoss("zulrah"));

            // Test refreshAll
            panel.refreshAll();

            // Verify all JScrollPanes in the panel hierarchy strictly have HORIZONTAL_SCROLLBAR_NEVER
            assertNoHorizontalScrollPanes(panel);
        });
    }

    @Test
    public void everyTabFitsSidebarWidth() throws Exception {
        List<String> problems = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            QuestScannerService questScanner = new QuestScannerService();
            AccountScannerService accountScanner = new AccountScannerService(null, questScanner);
            accountScanner.setCustomAccount(PlayerAccount.createDefault());

            GePriceService priceService = new GePriceService(null);
            BossDatabase bossDatabase = new BossDatabase();
            EquipmentDatabase equipmentDatabase = new EquipmentDatabase();

            BossRecommendationEngine recommendationEngine = new BossRecommendationEngine(bossDatabase);
            GearOptimizerService gearOptimizer = new GearOptimizerService(equipmentDatabase, priceService);
            GearUpgradeAdvisor upgradeAdvisor = new GearUpgradeAdvisor(equipmentDatabase, gearOptimizer, priceService);
            InventoryBuilderService inventoryService = new InventoryBuilderService(priceService);
            ProgressionService progressionService = new ProgressionService(bossDatabase, recommendationEngine);

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
                    accountScanner,
                    bossDatabase,
                    recommendationEngine,
                    gearOptimizer,
                    upgradeAdvisor,
                    inventoryService,
                    progressionService,
                    config
            );

            String[] tabs = {"DASHBOARD", "BOSS_FINDER", "GEAR_OPTIMIZER", "BOSS_GUIDES", "INVENTORY", "PROGRESSION", "SETTINGS"};
            int[] widths = {225, 242, 250};

            for (int w : widths) {
                panel.setSize(w, 800);
                for (String tab : tabs) {
                    panel.setActiveTab(tab);
                    for (int pass = 0; pass < 3; pass++) {
                        GuideLayoutTest.layoutTree(panel);
                    }
                    JScrollPane sp = findVisibleScrollPane(panel);
                    if (sp != null && sp.getViewport() != null && sp.getViewport().getView() != null) {
                        java.awt.Component view = sp.getViewport().getView();
                        int vw = sp.getViewport().getWidth();
                        if (vw > 0 && view.getWidth() > vw) {
                            problems.add("Tab " + tab + " @" + w + "px: view width " + view.getWidth() + " > viewport " + vw);
                        }
                    }
                }
            }
        });
        if (!problems.isEmpty()) {
            Assert.fail(problems.size() + " layout problem(s):\n" + String.join("\n", problems));
        }
    }

    private static JScrollPane findVisibleScrollPane(java.awt.Container container) {
        for (java.awt.Component c : container.getComponents()) {
            if (c.isVisible() && c instanceof JScrollPane) {
                return (JScrollPane) c;
            }
            if (c.isVisible() && c instanceof java.awt.Container) {
                JScrollPane sp = findVisibleScrollPane((java.awt.Container) c);
                if (sp != null) return sp;
            }
        }
        return null;
    }

    private static void assertNoHorizontalScrollPanes(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof javax.swing.JScrollPane) {
                javax.swing.JScrollPane sp = (javax.swing.JScrollPane) comp;
                Assert.assertEquals(
                        "All JScrollPanes must have HORIZONTAL_SCROLLBAR_NEVER to prevent horizontal scrolling",
                        javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                        sp.getHorizontalScrollBarPolicy()
                );
                java.awt.Component view = sp.getViewport().getView();
                if (view != null) {
                    Assert.assertTrue(
                            "Scroll content view " + view.getClass().getName() + " must implement Scrollable",
                            view instanceof javax.swing.Scrollable
                    );
                    javax.swing.Scrollable scrollable = (javax.swing.Scrollable) view;
                    Assert.assertTrue(
                            "Scroll content view " + view.getClass().getName() + " must return true for getScrollableTracksViewportWidth()",
                            scrollable.getScrollableTracksViewportWidth()
                    );
                }
            }
            if (comp instanceof java.awt.Container) {
                assertNoHorizontalScrollPanes((java.awt.Container) comp);
            }
        }
    }
}
