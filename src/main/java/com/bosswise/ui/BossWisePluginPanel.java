package com.bosswise.ui;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.BankCheckerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.CollectionLogService;
import com.bosswise.service.CombatAchievementService;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.CombatStyleEvaluator;
import com.bosswise.service.DpsLabService;
import com.bosswise.service.DropRateCalculatorService;
import com.bosswise.service.ExportService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearRoadmapService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.LootTrackerService;
import com.bosswise.service.MonsterProfitService;
import com.bosswise.service.ProgressionService;
import com.bosswise.service.SlayerTaskService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.PluginPanel;

public class BossWisePluginPanel extends PluginPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);

    private final DashboardPanel dashboardPanel;
    private final BossFinderPanel bossFinderPanel;
    private final GearOptimizerPanel gearOptimizerPanel;
    private final DpsLabPanel dpsLabPanel;
    private final SlayerPanel slayerPanel;
    private final LootTrackerPanel lootTrackerPanel;
    private final ProfitFinderPanel profitFinderPanel;
    private final InventoryBuilderPanel inventoryBuilderPanel;
    private final ProgressionPanel progressionPanel;
    private final BossGuidePanel bossGuidePanel;
    private final SettingsPanel settingsPanel;

    private final JLabel statusIndicator = new JLabel("● READY");
    private final GoldButton settingsButton = new GoldButton("⚙");
    private final Map<String, GoldButton> navButtons = new HashMap<>();

    public BossWisePluginPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            InventoryBuilderService inventoryService,
            ProgressionService progressionService,
            BossWiseConfig config) {

        this(accountScanner, bossDatabase, new MonsterDatabase(), recommendationEngine, optimizerService, upgradeAdvisor,
                inventoryService, progressionService, new CombatCalculationService(),
                new CombatStyleEvaluator(optimizerService, new CombatCalculationService()), config);
    }

    public BossWisePluginPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            InventoryBuilderService inventoryService,
            ProgressionService progressionService,
            CombatCalculationService combatCalculationService,
            CombatStyleEvaluator combatStyleEvaluator,
            BossWiseConfig config) {

        this(accountScanner, bossDatabase, monsterDatabase, recommendationEngine, optimizerService, upgradeAdvisor,
                inventoryService, progressionService, combatCalculationService, combatStyleEvaluator,
                new CurrentGearScannerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)),
                new DpsLabService(combatCalculationService, new com.bosswise.service.GePriceService(null), new com.bosswise.db.EquipmentDatabase()),
                new SlayerTaskService(null, null),
                new LootTrackerService(new com.bosswise.service.GePriceService(null), bossDatabase, monsterDatabase, null),
                new MonsterProfitService(),
                new DropRateCalculatorService(),
                new GearRoadmapService(new com.bosswise.db.EquipmentDatabase(), optimizerService, new com.bosswise.service.GePriceService(null)),
                new CombatAchievementService(null),
                new CollectionLogService(null),
                new BankCheckerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)),
                new ExportService(),
                config);
    }

    public BossWisePluginPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            InventoryBuilderService inventoryService,
            ProgressionService progressionService,
            CombatCalculationService combatCalculationService,
            CombatStyleEvaluator combatStyleEvaluator,
            CurrentGearScannerService currentGearScanner,
            DpsLabService dpsLabService,
            SlayerTaskService slayerTaskService,
            LootTrackerService lootTrackerService,
            MonsterProfitService monsterProfitService,
            DropRateCalculatorService dropRateCalculatorService,
            GearRoadmapService gearRoadmapService,
            CombatAchievementService combatAchievementService,
            CollectionLogService collectionLogService,
            BankCheckerService bankChecker,
            ExportService exportService,
            BossWiseConfig config) {
        this(accountScanner, bossDatabase, monsterDatabase, recommendationEngine, optimizerService, upgradeAdvisor,
                inventoryService, progressionService, combatCalculationService, combatStyleEvaluator,
                currentGearScanner, dpsLabService, slayerTaskService, lootTrackerService, monsterProfitService,
                dropRateCalculatorService, gearRoadmapService, combatAchievementService, collectionLogService,
                bankChecker, exportService, null, config);
    }

    public BossWisePluginPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            InventoryBuilderService inventoryService,
            ProgressionService progressionService,
            CombatCalculationService combatCalculationService,
            CombatStyleEvaluator combatStyleEvaluator,
            CurrentGearScannerService currentGearScanner,
            DpsLabService dpsLabService,
            SlayerTaskService slayerTaskService,
            LootTrackerService lootTrackerService,
            MonsterProfitService monsterProfitService,
            DropRateCalculatorService dropRateCalculatorService,
            GearRoadmapService gearRoadmapService,
            CombatAchievementService combatAchievementService,
            CollectionLogService collectionLogService,
            BankCheckerService bankChecker,
            ExportService exportService,
            com.bosswise.service.WikiSyncService wikiSyncService,
            BossWiseConfig config) {

        super(false);
        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);
        setPreferredSize(new Dimension(BossWiseDesign.PANEL_WIDTH, 0));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BossWiseDesign.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        // Title row: Brand + Settings + Status
        JPanel titleRow = new JPanel(new BorderLayout(4, 0));
        titleRow.setOpaque(false);

        JLabel brand = new JLabel("BOSSWISE");
        brand.setFont(BossWiseDesign.getTitleFont());
        brand.setForeground(BossWiseDesign.GOLD);
        titleRow.add(brand, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        topActions.setOpaque(false);

        GoldButton guidesNavBtn = new GoldButton("📖 Guides");
        guidesNavBtn.setFont(BossWiseDesign.getBadgeFont());
        guidesNavBtn.setPreferredSize(new Dimension(75, 24));
        guidesNavBtn.setToolTipText("Monster Strategy Guides & Combat Intelligence");
        guidesNavBtn.addActionListener(e -> setActiveTab("BOSS_GUIDES"));
        navButtons.put("BOSS_GUIDES", guidesNavBtn);
        topActions.add(guidesNavBtn);

        settingsButton.setFont(BossWiseDesign.getBoldFont());
        settingsButton.setPreferredSize(new Dimension(30, 24));
        settingsButton.setToolTipText("BossWise Settings & Watchlist");
        settingsButton.addActionListener(e -> setActiveTab("SETTINGS"));
        navButtons.put("SETTINGS", settingsButton);
        topActions.add(settingsButton);

        statusIndicator.setFont(BossWiseDesign.getBadgeFont());
        statusIndicator.setForeground(BossWiseDesign.STATUS_READY);
        topActions.add(statusIndicator);

        titleRow.add(topActions, BorderLayout.EAST);
        header.add(titleRow);

        WrapText subtitle = new WrapText("Combat & PvM Intelligence", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        header.add(subtitle);
        header.add(Box.createRigidArea(new Dimension(0, 6)));

        // Navigation Bar: 3 rows of 3 compact buttons (calibrated for 225px sidebar width)
        // Row 1: Core Combat
        JPanel navRow1 = new JPanel(new GridLayout(1, 3, 3, 0));
        navRow1.setOpaque(false);
        addNavTab(navRow1, "Overview", "DASHBOARD");
        addNavTab(navRow1, "Monsters", "BOSS_FINDER");
        addNavTab(navRow1, "Gear", "GEAR_OPTIMIZER");
        header.add(navRow1);
        header.add(Box.createRigidArea(new Dimension(0, 3)));

        // Row 2: Analysis & Combat
        JPanel navRow2 = new JPanel(new GridLayout(1, 3, 3, 0));
        navRow2.setOpaque(false);
        addNavTab(navRow2, "DPS Lab", "DPS_LAB");
        addNavTab(navRow2, "Slayer", "SLAYER");
        addNavTab(navRow2, "Loot", "LOOT_TRACKER");
        header.add(navRow2);
        header.add(Box.createRigidArea(new Dimension(0, 3)));

        // Row 3: Economics & Progression
        JPanel navRow3 = new JPanel(new GridLayout(1, 3, 3, 0));
        navRow3.setOpaque(false);
        addNavTab(navRow3, "Profit", "PROFIT_FINDER");
        addNavTab(navRow3, "Supplies", "INVENTORY");
        addNavTab(navRow3, "Roadmap", "PROGRESSION");
        header.add(navRow3);

        add(header, BorderLayout.NORTH);

        // Instantiate Child Panels
        this.dashboardPanel = new DashboardPanel(
                accountScanner, bossDatabase, monsterDatabase, recommendationEngine, optimizerService,
                upgradeAdvisor, progressionService, lootTrackerService, currentGearScanner,
                tabName -> switchTab(tabName),
                boss -> openGearOptimizer(boss),
                boss -> openBossGuide(boss)
        );

        this.bossFinderPanel = new BossFinderPanel(
                accountScanner, bossDatabase, monsterDatabase, recommendationEngine,
                boss -> openGearOptimizer(boss),
                boss -> openBossGuide(boss)
        );

        this.gearOptimizerPanel = new GearOptimizerPanel(
                bossDatabase, monsterDatabase, accountScanner, optimizerService, upgradeAdvisor,
                combatStyleEvaluator, currentGearScanner, bankChecker, exportService,
                new com.bosswise.service.GearComparisonService(inventoryService)
        );

        this.dpsLabPanel = new DpsLabPanel(
                bossDatabase, monsterDatabase, accountScanner, currentGearScanner,
                optimizerService, dpsLabService
        );

        this.slayerPanel = new SlayerPanel(slayerTaskService);
        this.lootTrackerPanel = new LootTrackerPanel(lootTrackerService, dropRateCalculatorService);
        this.profitFinderPanel = new ProfitFinderPanel(monsterProfitService);
        this.inventoryBuilderPanel = new InventoryBuilderPanel(bossDatabase, inventoryService, exportService);

        this.progressionPanel = new ProgressionPanel(
                accountScanner, progressionService, gearRoadmapService,
                combatAchievementService, collectionLogService, currentGearScanner
        );

        com.bosswise.db.MonsterStrategyDatabase strategyDb = new com.bosswise.db.MonsterStrategyDatabase(monsterDatabase, bossDatabase);
        com.bosswise.service.MonsterStrategyService strategySvc = new com.bosswise.service.MonsterStrategyService(
                strategyDb, optimizerService, inventoryService, monsterDatabase, bossDatabase
        );
        this.bossGuidePanel = new BossGuidePanel(
                bossDatabase, monsterDatabase, strategyDb, strategySvc, accountScanner,
                boss -> openGearOptimizer(boss),
                boss -> openInventory(boss)
        );
        this.settingsPanel = new SettingsPanel(config, accountScanner, wikiSyncService, () -> refreshAll());

        // Register panels in CardLayout
        mainContainer.add(dashboardPanel, "DASHBOARD");
        mainContainer.add(bossFinderPanel, "BOSS_FINDER");
        mainContainer.add(gearOptimizerPanel, "GEAR_OPTIMIZER");
        mainContainer.add(dpsLabPanel, "DPS_LAB");
        mainContainer.add(slayerPanel, "SLAYER");
        mainContainer.add(lootTrackerPanel, "LOOT_TRACKER");
        mainContainer.add(profitFinderPanel, "PROFIT_FINDER");
        mainContainer.add(inventoryBuilderPanel, "INVENTORY");
        mainContainer.add(progressionPanel, "PROGRESSION");
        mainContainer.add(bossGuidePanel, "BOSS_GUIDES");
        mainContainer.add(settingsPanel, "SETTINGS");

        add(mainContainer, BorderLayout.CENTER);

        setActiveTab("DASHBOARD");
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BossWiseDesign.PANEL_WIDTH, super.getPreferredSize().height);
    }

    private void addNavTab(JPanel parent, String label, String cardName) {
        GoldButton btn = new GoldButton(label);
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(78, 25));
        btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btn.addActionListener(e -> setActiveTab(cardName));
        navButtons.put(cardName, btn);
        parent.add(btn);
    }

    public void setActiveTab(String cardName) {
        cardLayout.show(mainContainer, cardName);

        for (Map.Entry<String, GoldButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equalsIgnoreCase(cardName);
            entry.getValue().setActive(active);
        }

        revalidate();
        repaint();
    }

    public void switchTab(String tabName) {
        if ("Bosses".equalsIgnoreCase(tabName) || "Monsters".equalsIgnoreCase(tabName) || "BOSS_FINDER".equalsIgnoreCase(tabName)) {
            setActiveTab("BOSS_FINDER");
        } else if ("Gear".equalsIgnoreCase(tabName) || "GEAR_OPTIMIZER".equalsIgnoreCase(tabName)) {
            setActiveTab("GEAR_OPTIMIZER");
        } else if ("DPS".equalsIgnoreCase(tabName) || "DPS_LAB".equalsIgnoreCase(tabName)) {
            setActiveTab("DPS_LAB");
        } else if ("Slayer".equalsIgnoreCase(tabName) || "SLAYER".equalsIgnoreCase(tabName)) {
            setActiveTab("SLAYER");
        } else if ("Loot".equalsIgnoreCase(tabName) || "LOOT_TRACKER".equalsIgnoreCase(tabName)) {
            setActiveTab("LOOT_TRACKER");
        } else if ("Profit".equalsIgnoreCase(tabName) || "PROFIT_FINDER".equalsIgnoreCase(tabName)) {
            setActiveTab("PROFIT_FINDER");
        } else if ("Guides".equalsIgnoreCase(tabName) || "BOSS_GUIDES".equalsIgnoreCase(tabName) || "Strategies".equalsIgnoreCase(tabName) || "STRATEGY".equalsIgnoreCase(tabName)) {
            setActiveTab("BOSS_GUIDES");
        } else if ("Inventory".equalsIgnoreCase(tabName) || "Supplies".equalsIgnoreCase(tabName) || "INVENTORY".equalsIgnoreCase(tabName)) {
            setActiveTab("INVENTORY");
        } else if ("Progression".equalsIgnoreCase(tabName) || "Roadmap".equalsIgnoreCase(tabName) || "PROGRESSION".equalsIgnoreCase(tabName)) {
            setActiveTab("PROGRESSION");
        } else if ("Settings".equalsIgnoreCase(tabName) || "SETTINGS".equalsIgnoreCase(tabName)) {
            setActiveTab("SETTINGS");
        } else {
            setActiveTab("DASHBOARD");
        }
    }

    public void openGearOptimizer(BossData boss) {
        if (gearOptimizerPanel != null) {
            gearOptimizerPanel.selectBoss(boss);
        }
        setActiveTab("GEAR_OPTIMIZER");
    }

    public void openBossGuide(BossData boss) {
        if (bossGuidePanel != null) {
            bossGuidePanel.selectBoss(boss);
        }
        setActiveTab("BOSS_GUIDES");
    }

    public void openInventory(BossData boss) {
        if (inventoryBuilderPanel != null && boss != null) {
            inventoryBuilderPanel.selectBoss(boss);
        }
        setActiveTab("INVENTORY");
    }

    public void refreshAll() {
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refresh();
            bossFinderPanel.refreshList();
            gearOptimizerPanel.calculateAndRender();
            progressionPanel.refreshRoadmap();
            if (lootTrackerPanel != null) lootTrackerPanel.renderAll();
            if (dpsLabPanel != null) dpsLabPanel.calculateAndRender();
            if (profitFinderPanel != null) profitFinderPanel.renderList();
        });
    }

    public void updateStatus(String statusText, java.awt.Color color) {
        SwingUtilities.invokeLater(() -> {
            statusIndicator.setText("● " + statusText);
            statusIndicator.setForeground(color);
        });
    }

    public DashboardPanel getDashboardPanel() { return dashboardPanel; }
    public BossFinderPanel getBossFinderPanel() { return bossFinderPanel; }
    public GearOptimizerPanel getGearOptimizerPanel() { return gearOptimizerPanel; }
    public DpsLabPanel getDpsLabPanel() { return dpsLabPanel; }
    public SlayerPanel getSlayerPanel() { return slayerPanel; }
    public LootTrackerPanel getLootTrackerPanel() { return lootTrackerPanel; }
    public ProfitFinderPanel getProfitFinderPanel() { return profitFinderPanel; }
    public BossGuidePanel getBossGuidePanel() { return bossGuidePanel; }
    public InventoryBuilderPanel getInventoryBuilderPanel() { return inventoryBuilderPanel; }
    public ProgressionPanel getProgressionPanel() { return progressionPanel; }
    public SettingsPanel getSettingsPanel() { return settingsPanel; }
}
