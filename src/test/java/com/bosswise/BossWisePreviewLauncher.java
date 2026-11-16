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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class BossWisePreviewLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}

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

            JFrame frame = new JFrame("BossWise – RuneLite Sidebar Preview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.setSize(new Dimension(360, 720));
            frame.setMinimumSize(new Dimension(340, 500));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
