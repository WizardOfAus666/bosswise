package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BankGearCheckResult;
import com.bosswise.model.BossCategory;
import com.bosswise.model.BossCombatProfile;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.CurrentGearComparison;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.GearUpgradeRecommendation;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.StyleEvaluation;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.BankCheckerService;
import com.bosswise.service.CombatCalculationService;
import com.bosswise.service.CombatStyleEvaluator;
import com.bosswise.service.ExportService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearOptimizerService.PriorityMode;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.ui.components.EquipmentSlotRow;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

/**
 * Universal Gear Optimizer Panel supporting all OSRS attackable monsters,
 * slayer tasks, budget thresholds, and multi-style combat calculations.
 */
public class GearOptimizerPanel extends JPanel {
    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final AccountScannerService accountScanner;
    private final GearOptimizerService optimizerService;
    private final GearUpgradeAdvisor upgradeAdvisor;
    private final CombatStyleEvaluator styleEvaluator;

    private final JComboBox<String> categorySelector = new JComboBox<>(new String[]{
            "Bosses", "All Categories", "Slayer", "Superior Slayer", "Wilderness", "Raids", "Demi-Bosses", "Regular", "Quests", "Minigames"
    });
    private final JComboBox<String> monsterSelector = new JComboBox<>();
    private final JComboBox<CombatStyle> styleSelector = new JComboBox<>(new CombatStyle[]{
            CombatStyle.MELEE, CombatStyle.RANGED, CombatStyle.MAGIC
    });
    private final JCheckBox slayerTaskCheck = new JCheckBox("Slayer Task");
    private final JTextField budgetField = new JTextField("5000000");

    private String activeSetupTier = "Best Value";
    private final List<GoldButton> tierButtons = new ArrayList<>();
    private boolean updatingSelectors = false;

    private final VerticalPanel styleRankingContainer = new VerticalPanel();
    private final VerticalPanel summaryContainer = new VerticalPanel();
    private final VerticalPanel highlightsContainer = new VerticalPanel();
    private final VerticalPanel equipmentContainer = new VerticalPanel();
    private final VerticalPanel upgradesContainer = new VerticalPanel();

    private final CurrentGearScannerService currentGearScanner;
    private final BankCheckerService bankChecker;
    private final ExportService exportService;
    private final com.bosswise.service.GearComparisonService comparisonService;
    private OptimizedGearSetup lastSetup;

    private boolean comparisonMode = false;
    private final GoldButton compareModeBtn = new GoldButton("Compare Setups (5)");
    private final JComboBox<String> viableSetupsSelector = new JComboBox<>();
    private List<OptimizedGearSetup> currentViableSetups = new ArrayList<>();

    public GearOptimizerPanel(
            BossDatabase bossDatabase,
            AccountScannerService accountScanner,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor) {

        this(bossDatabase, new MonsterDatabase(), accountScanner, optimizerService, upgradeAdvisor,
                new CombatStyleEvaluator(optimizerService, new CombatCalculationService()));
    }

    public GearOptimizerPanel(
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            AccountScannerService accountScanner,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            CombatStyleEvaluator styleEvaluator) {

        this(bossDatabase, monsterDatabase, accountScanner, optimizerService, upgradeAdvisor, styleEvaluator,
                new CurrentGearScannerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)),
                new BankCheckerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)),
                new ExportService());
    }

    public GearOptimizerPanel(
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            AccountScannerService accountScanner,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            CombatStyleEvaluator styleEvaluator,
            CurrentGearScannerService currentGearScanner,
            BankCheckerService bankChecker,
            ExportService exportService) {

        this(bossDatabase, monsterDatabase, accountScanner, optimizerService, upgradeAdvisor, styleEvaluator,
                currentGearScanner, bankChecker, exportService,
                new com.bosswise.service.GearComparisonService(new com.bosswise.service.InventoryBuilderService(null)));
    }

    public GearOptimizerPanel(
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            AccountScannerService accountScanner,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            CombatStyleEvaluator styleEvaluator,
            CurrentGearScannerService currentGearScanner,
            BankCheckerService bankChecker,
            ExportService exportService,
            com.bosswise.service.GearComparisonService comparisonService) {

        this.bossDatabase = bossDatabase;
        this.monsterDatabase = monsterDatabase;
        this.accountScanner = accountScanner;
        this.optimizerService = optimizerService;
        this.upgradeAdvisor = upgradeAdvisor;
        this.styleEvaluator = styleEvaluator;
        this.currentGearScanner = currentGearScanner;
        this.bankChecker = bankChecker;
        this.exportService = exportService;
        this.comparisonService = (comparisonService != null)
                ? comparisonService
                : new com.bosswise.service.GearComparisonService(new com.bosswise.service.InventoryBuilderService(null));

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        // Top Control Panel
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(BossWiseDesign.PANEL_BG);
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Row 1: Category Filter & Monster Selector
        JPanel row1 = new JPanel(new GridLayout(1, 2, 4, 0));
        row1.setOpaque(false);
        styleCombo(categorySelector);
        styleCombo(monsterSelector);
        categorySelector.addActionListener(e -> onCategoryChanged());
        monsterSelector.addActionListener(e -> {
            if (!updatingSelectors) onMonsterChanged();
        });
        row1.add(categorySelector);
        row1.add(monsterSelector);
        controls.add(row1);
        controls.add(Box.createRigidArea(new Dimension(0, 5)));

        // Row 2: Combat Style & Slayer Task Checkbox
        JPanel row2 = new JPanel(new BorderLayout(6, 0));
        row2.setOpaque(false);
        styleCombo(styleSelector);
        styleSelector.setPreferredSize(new Dimension(100, 26));
        styleSelector.addActionListener(e -> {
            if (!updatingSelectors) calculateAndRender();
        });
        row2.add(styleSelector, BorderLayout.WEST);

        slayerTaskCheck.setFont(BossWiseDesign.getBadgeFont());
        slayerTaskCheck.setForeground(BossWiseDesign.GOLD);
        slayerTaskCheck.setOpaque(false);
        slayerTaskCheck.setFocusPainted(false);
        slayerTaskCheck.setToolTipText("Enable to apply Slayer helmet (i) and Black mask bonuses");
        slayerTaskCheck.addActionListener(e -> calculateAndRender());
        row2.add(slayerTaskCheck, BorderLayout.EAST);
        controls.add(row2);
        controls.add(Box.createRigidArea(new Dimension(0, 5)));

        // Row 3: Budget Input Row
        JPanel budgetRow = new JPanel(new BorderLayout(6, 0));
        budgetRow.setOpaque(false);
        JLabel bLabel = new JLabel("BUDGET:");
        bLabel.setFont(BossWiseDesign.getBadgeFont());
        bLabel.setForeground(BossWiseDesign.GOLD);
        budgetRow.add(bLabel, BorderLayout.WEST);

        budgetField.setFont(BossWiseDesign.getRegularFont());
        budgetField.setBackground(BossWiseDesign.CARD_BG);
        budgetField.setForeground(BossWiseDesign.TEXT_PRIMARY);
        budgetField.setPreferredSize(new Dimension(75, 26));
        budgetField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        budgetField.addActionListener(e -> calculateAndRender());
        budgetRow.add(budgetField, BorderLayout.CENTER);

        GoldButton calcBtn = new GoldButton("Apply");
        calcBtn.setPreferredSize(new Dimension(50, 26));
        calcBtn.setFont(BossWiseDesign.getBoldFont());
        calcBtn.setToolTipText("Calculate optimized setup for budget");
        calcBtn.addActionListener(e -> calculateAndRender());
        budgetRow.add(calcBtn, BorderLayout.EAST);
        controls.add(budgetRow);
        controls.add(Box.createRigidArea(new Dimension(0, 5)));

        // Row 4a: Presets (Sugg, 100K, 500K, 1M)
        JPanel presetsRow1 = new JPanel(new GridLayout(1, 4, 3, 0));
        presetsRow1.setOpaque(false);
        addSuggestedButton(presetsRow1);
        addPresetButton(presetsRow1, "100K", 100_000L);
        addPresetButton(presetsRow1, "500K", 500_000L);
        addPresetButton(presetsRow1, "1M", 1_000_000L);
        controls.add(presetsRow1);
        controls.add(Box.createRigidArea(new Dimension(0, 3)));

        // Row 4b: Higher Presets (5M, 10M, 25M, 100M)
        JPanel presetsRow2 = new JPanel(new GridLayout(1, 4, 3, 0));
        presetsRow2.setOpaque(false);
        addPresetButton(presetsRow2, "5M", 5_000_000L);
        addPresetButton(presetsRow2, "10M", 10_000_000L);
        addPresetButton(presetsRow2, "25M", 25_000_000L);
        addPresetButton(presetsRow2, "100M", 100_000_000L);
        controls.add(presetsRow2);
        controls.add(Box.createRigidArea(new Dimension(0, 5)));

        // Row 5: Tier Tabs (Budget / Value / High / Max BiS)
        JPanel tabRow = new JPanel(new GridLayout(1, 4, 3, 0));
        tabRow.setOpaque(false);
        addTierButton(tabRow, "Budget", "Budget");
        addTierButton(tabRow, "Value", "Best Value");
        addTierButton(tabRow, "High", "High Tier");
        addTierButton(tabRow, "Max BiS", "Max BiS");
        controls.add(tabRow);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Row 6: Compare Mode Button
        compareModeBtn.setFont(BossWiseDesign.getBadgeFont());
        compareModeBtn.setPreferredSize(new Dimension(200, 24));
        compareModeBtn.setToolTipText("Compare up to 5 viable gear setups side-by-side with metrics");
        compareModeBtn.addActionListener(e -> {
            comparisonMode = !comparisonMode;
            compareModeBtn.setText(comparisonMode ? "← Single Setup View" : "Compare Setups (5)");
            compareModeBtn.setActive(comparisonMode);
            calculateAndRender();
        });
        controls.add(compareModeBtn);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Row 7: Viable Setups Selector Dropdown
        styleCombo(viableSetupsSelector);
        viableSetupsSelector.setToolTipText("Quickly switch between all viable gear setups");
        viableSetupsSelector.addActionListener(e -> {
            if (!updatingSelectors && !comparisonMode) {
                int idx = viableSetupsSelector.getSelectedIndex();
                if (idx >= 0 && idx < currentViableSetups.size()) {
                    renderSingleSetup(currentViableSetups.get(idx));
                }
            }
        });
        controls.add(viableSetupsSelector);

        add(controls, BorderLayout.NORTH);

        // Center Content Scroll (tracks viewport width to prevent horizontal overflow)
        ScrollablePanel centerPanel = new ScrollablePanel();
        centerPanel.setLeftAlignChildren(true);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BossWiseDesign.BG_DARK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        styleRankingContainer.setOpaque(false);
        centerPanel.add(styleRankingContainer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        summaryContainer.setOpaque(false);
        centerPanel.add(summaryContainer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        highlightsContainer.setOpaque(false);
        centerPanel.add(highlightsContainer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        equipmentContainer.setOpaque(false);
        centerPanel.add(equipmentContainer);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        upgradesContainer.setOpaque(false);
        centerPanel.add(upgradesContainer);

        JScrollPane scroll = new JScrollPane(centerPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        populateMonstersForCategory("Bosses");
        onMonsterChanged();
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(BossWiseDesign.getBoldFont());
        box.setBackground(BossWiseDesign.CARD_BG);
        box.setForeground(BossWiseDesign.TEXT_PRIMARY);
        box.setPreferredSize(new Dimension(80, 26));
        box.setBorder(BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1));
    }

    private void addSuggestedButton(JPanel parent) {
        GoldButton btn = new GoldButton("Sugg");
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(46, 22));
        btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btn.setToolTipText("Auto-fill recommended budget for selected monster");
        btn.addActionListener(e -> {
            long sugg = getSelectedSuggestedBudget();
            budgetField.setText(String.valueOf(sugg));
            calculateAndRender();
        });
        parent.add(btn);
    }

    private long getSelectedSuggestedBudget() {
        String monsterName = (String) monsterSelector.getSelectedItem();
        if (monsterName != null) {
            MonsterData m = monsterDatabase.getMonsterByName(monsterName);
            if (m != null && m.getSuggestedBudget() > 0) {
                return m.getSuggestedBudget();
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getName().equalsIgnoreCase(monsterName) && b.getSuggestedGearBudget() > 0) {
                    return b.getSuggestedGearBudget();
                }
            }
        }
        return 5_000_000L;
    }

    private void addPresetButton(JPanel parent, String label, long amount) {
        GoldButton btn = new GoldButton(label);
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(46, 22));
        btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btn.setToolTipText("Set budget to " + label + " GP");
        btn.addActionListener(e -> {
            budgetField.setText(String.valueOf(amount));
            calculateAndRender();
        });
        parent.add(btn);
    }

    private void addTierButton(JPanel parent, String label, String tierValue) {
        GoldButton btn = new GoldButton(label);
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(50, 24));
        btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btn.putClientProperty("tierValue", tierValue);
        btn.setActive(tierValue.equalsIgnoreCase(activeSetupTier));
        btn.setToolTipText("Switch to " + tierValue + " setup");
        btn.addActionListener(e -> {
            activeSetupTier = tierValue;
            updateTierButtonStyles();
            calculateAndRender();
        });
        tierButtons.add(btn);
        parent.add(btn);
    }

    private void updateTierButtonStyles() {
        for (GoldButton b : tierButtons) {
            String val = (String) b.getClientProperty("tierValue");
            b.setActive(val != null && val.equalsIgnoreCase(activeSetupTier));
        }
    }

    private void onCategoryChanged() {
        String cat = (String) categorySelector.getSelectedItem();
        populateMonstersForCategory(cat);
        onMonsterChanged();
    }

    private void populateMonstersForCategory(String category) {
        updatingSelectors = true;
        monsterSelector.removeAllItems();

        Set<String> names = new LinkedHashSet<>();

        if ("Bosses".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.BOSS)) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                names.add(b.getName());
            }
        } else if ("Slayer".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getSlayerMonsters()) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getCategory() == BossCategory.SLAYER) {
                    names.add(b.getName());
                }
            }
        } else if ("Superior Slayer".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.SUPERIOR)) {
                names.add(m.getName());
            }
        } else if ("Wilderness".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getWildernessMonsters()) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getCategory() == BossCategory.WILDERNESS) {
                    names.add(b.getName());
                }
            }
        } else if ("Raids".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.RAID)) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getCategory() == BossCategory.RAID) {
                    names.add(b.getName());
                }
            }
        } else if ("Demi-Bosses".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.DEMI_BOSS)) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getCategory() == BossCategory.DEMI_BOSS) {
                    names.add(b.getName());
                }
            }
        } else if ("Regular".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.REGULAR)) {
                names.add(m.getName());
            }
        } else if ("Quests".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.QUEST)) {
                names.add(m.getName());
            }
        } else if ("Minigames".equalsIgnoreCase(category) || "Dungeons & Minigames".equalsIgnoreCase(category)) {
            for (MonsterData m : monsterDatabase.getMonstersByCategory(MonsterCategory.DUNGEON_MINIGAME)) {
                names.add(m.getName());
            }
        } else {
            // All Categories: combine all monsters AND all bosses
            for (MonsterData m : monsterDatabase.getAllMonsters().values()) {
                names.add(m.getName());
            }
            for (BossData b : bossDatabase.getAllBosses().values()) {
                names.add(b.getName());
            }
        }

        List<String> sortedNames = new ArrayList<>(names);
        Collections.sort(sortedNames);
        for (String n : sortedNames) {
            monsterSelector.addItem(n);
        }

        updatingSelectors = false;
    }

    private void onMonsterChanged() {
        String monsterName = (String) monsterSelector.getSelectedItem();
        if (monsterName == null) return;

        MonsterData monster = monsterDatabase.getMonsterByName(monsterName);
        if (monster != null) {
            com.bosswise.model.MonsterCombatProfile profile = monster.getCombatProfile();
            if (profile != null) {
                styleSelector.setSelectedItem(profile.getRecommendedStyle());
                if (monster.getRequiredSlayerLevel() > 1 || monster.getCategory() == MonsterCategory.SLAYER) {
                    slayerTaskCheck.setSelected(true);
                }
            }
        } else {
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getName().equalsIgnoreCase(monsterName)) {
                    BossCombatProfile profile = optimizerService.getBossProfile(b);
                    styleSelector.setSelectedItem(profile.getRecommendedStyle());
                    break;
                }
            }
        }
        calculateAndRender();
    }

    public void selectBoss(BossData boss) {
        if (boss != null) {
            selectMonsterByName(boss.getName());
        }
    }

    public void selectMonster(MonsterData monster) {
        if (monster != null) {
            selectMonsterByName(monster.getName());
        }
    }

    private void selectMonsterByName(String name) {
        boolean foundInCurrent = false;
        for (int i = 0; i < monsterSelector.getItemCount(); i++) {
            if (name.equalsIgnoreCase(monsterSelector.getItemAt(i))) {
                foundInCurrent = true;
                break;
            }
        }
        if (!foundInCurrent) {
            categorySelector.setSelectedItem("Bosses");
            populateMonstersForCategory("Bosses");
            for (int i = 0; i < monsterSelector.getItemCount(); i++) {
                if (name.equalsIgnoreCase(monsterSelector.getItemAt(i))) {
                    foundInCurrent = true;
                    break;
                }
            }
            if (!foundInCurrent) {
                categorySelector.setSelectedItem("All Categories");
                populateMonstersForCategory("All Categories");
            }
        }
        monsterSelector.setSelectedItem(name);
        onMonsterChanged();
    }

    public void calculateAndRender() {
        updateTierButtonStyles();
        styleRankingContainer.removeAll();
        summaryContainer.removeAll();
        highlightsContainer.removeAll();
        equipmentContainer.removeAll();
        upgradesContainer.removeAll();

        String monsterName = (String) monsterSelector.getSelectedItem();
        MonsterData monster = (monsterName != null) ? monsterDatabase.getMonsterByName(monsterName) : null;
        BossData boss = null;

        if (monsterName != null) {
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getName().equalsIgnoreCase(monsterName) || (monster != null && b.getId().equalsIgnoreCase(monster.getId()))) {
                    boss = b;
                    break;
                }
            }
        }
        if (boss == null && monster != null) {
            boss = monster.toBossData();
        }
        if (boss == null) {
            boss = bossDatabase.getAllBosses().values().iterator().next();
        }

        CombatStyle selectedStyle = (CombatStyle) styleSelector.getSelectedItem();
        if (selectedStyle == null) selectedStyle = CombatStyle.MELEE;

        boolean onSlayerTask = slayerTaskCheck.isSelected();

        long budget;
        try {
            budget = Long.parseLong(budgetField.getText().trim().replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            budget = 5_000_000L;
            budgetField.setText("5000000");
        }

        PlayerAccount account = accountScanner.getCurrentAccount();
        if (account == null) account = PlayerAccount.createDefault();

        MonsterVariant variant = (monster != null) ? monster.getPrimaryVariant() : null;

        // 1. Intelligent Combat Style Ranking Evaluation
        if (monster != null && styleEvaluator != null) {
            List<StyleEvaluation> styleRanks = styleEvaluator.evaluateStyles(monster, variant, budget, account, onSlayerTask);
            if (!styleRanks.isEmpty()) {
                VerticalPanel rankCard = new VerticalPanel();
                rankCard.setBackground(BossWiseDesign.CARD_BG);
                rankCard.setBorder(BossWiseDesign.BORDER_CARD);

                JPanel rkHeader = new JPanel(new BorderLayout(4, 0)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                    }
                };
                rkHeader.setOpaque(false);
                rkHeader.setAlignmentX(LEFT_ALIGNMENT);

                JLabel rkTitle = new JLabel("STYLE EVALUATION");
                rkTitle.setFont(BossWiseDesign.getHeaderFont());
                rkTitle.setForeground(BossWiseDesign.GOLD);
                rkHeader.add(rkTitle, BorderLayout.WEST);

                StyleEvaluation topRank = styleRanks.get(0);
                JLabel topLbl = new JLabel("★ Top: " + topRank.getStyle().getDisplayName().toUpperCase());
                topLbl.setFont(BossWiseDesign.getBadgeFont());
                topLbl.setForeground(BossWiseDesign.STATUS_READY);
                rkHeader.add(topLbl, BorderLayout.EAST);
                rankCard.add(rkHeader);
                rankCard.add(Box.createVerticalStrut(3));

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < styleRanks.size(); i++) {
                    StyleEvaluation se = styleRanks.get(i);
                    if (i > 0) sb.append("  •  ");
                    sb.append(se.getStyle().getDisplayName()).append(": ").append(String.format("%.2f DPS", se.getDps()));
                }
                WrapText rankDetails = new WrapText(sb.toString(), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
                rankCard.add(rankDetails);

                if (topRank.getReason() != null && !topRank.getReason().isEmpty()) {
                    rankCard.add(Box.createVerticalStrut(2));
                    WrapText whyText = new WrapText(topRank.getReason(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
                    rankCard.add(whyText);
                }

                styleRankingContainer.add(rankCard);
            }
        }

        // 2. Generate All Viable Gear Setups
        List<OptimizedGearSetup> allSetups = optimizerService.generateAllViableSetups(
                boss, monster, account, budget, onSlayerTask
        );
        this.currentViableSetups = allSetups;

        updatingSelectors = true;
        viableSetupsSelector.removeAllItems();
        for (OptimizedGearSetup s : allSetups) {
            String badge = s.isWikiRecommended() ? "[WIKI] " : (s.isBudgetAlternative() ? "[BUDGET] " : "[OPT] ");
            viableSetupsSelector.addItem(badge + s.getSetupName() + " (" + String.format("%.2f DPS", s.getCalculatedDps()) + ")");
        }
        updatingSelectors = false;

        if (comparisonMode) {
            renderComparisonMode(allSetups, boss, monster, account);
        } else {
            OptimizedGearSetup setup = null;
            for (OptimizedGearSetup s : allSetups) {
                if (s.getSetupName().toLowerCase().contains(activeSetupTier.toLowerCase())) {
                    setup = s;
                    break;
                }
            }
            if (setup == null && !allSetups.isEmpty()) {
                setup = allSetups.get(0);
            }
            if (setup == null) {
                setup = optimizerService.generateSetup(
                        activeSetupTier, boss, selectedStyle, budget, account, PriorityMode.BALANCED
                );
            }
            renderSingleSetup(setup, boss, monster, account);
        }
    }

    private void renderSingleSetup(OptimizedGearSetup setup) {
        String monsterName = (String) monsterSelector.getSelectedItem();
        MonsterData monster = (monsterName != null) ? monsterDatabase.getMonsterByName(monsterName) : null;
        BossData boss = (monster != null) ? monster.toBossData() : null;
        if (boss == null && monsterName != null) {
            for (BossData b : bossDatabase.getAllBosses().values()) {
                if (b.getName().equalsIgnoreCase(monsterName)) { boss = b; break; }
            }
        }
        PlayerAccount account = accountScanner.getCurrentAccount();
        if (account == null) account = PlayerAccount.createDefault();
        renderSingleSetup(setup, boss, monster, account);
    }

    private void renderSingleSetup(OptimizedGearSetup setup, BossData boss, MonsterData monster, PlayerAccount account) {
        this.lastSetup = setup;
        CombatStyle selectedStyle = setup.getCombatStyle();

        // 3. Performance & Summary Card
        VerticalPanel sumCard = new VerticalPanel();
        sumCard.setBackground(BossWiseDesign.CARD_BG);
        sumCard.setBorder(BossWiseDesign.BORDER_CARD);

        JPanel sumHeader = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        sumHeader.setOpaque(false);
        sumHeader.setAlignmentX(LEFT_ALIGNMENT);
        JLabel tierLbl = new JLabel(setup.getSetupName().toUpperCase());
        tierLbl.setFont(BossWiseDesign.getHeaderFont());
        tierLbl.setForeground(BossWiseDesign.GOLD);
        sumHeader.add(tierLbl, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerRight.setOpaque(false);

        JLabel costLbl = new JLabel(GearOptimizerService.formatGp(setup.getTotalCost()) + " GP");
        costLbl.setFont(BossWiseDesign.getBoldFont());
        costLbl.setForeground(BossWiseDesign.TEXT_PRIMARY);
        headerRight.add(costLbl);

        GoldButton exportBtn = new GoldButton("Export");
        exportBtn.setFont(BossWiseDesign.getBadgeFont());
        exportBtn.setPreferredSize(new Dimension(50, 20));
        exportBtn.addActionListener(e -> {
            if (lastSetup != null && exportService != null) {
                exportService.copyToClipboard(exportService.exportGearSetupAsJson(lastSetup));
                exportBtn.setText("Copied!");
                new javax.swing.Timer(1500, evt -> exportBtn.setText("Export")).start();
            }
        });
        headerRight.add(exportBtn);

        sumHeader.add(headerRight, BorderLayout.EAST);
        sumCard.add(sumHeader);

        // Category Badge Row & Wiki Link
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(LEFT_ALIGNMENT);

        Color badgeColor = BossWiseDesign.STATUS_INFO;
        if (setup.isWikiRecommended()) badgeColor = BossWiseDesign.GOLD;
        else if (setup.isBudgetAlternative()) badgeColor = BossWiseDesign.STATUS_ALMOST;
        else if (setup.isPlayerOwned()) badgeColor = BossWiseDesign.STATUS_READY;

        JLabel badge = new JLabel("[" + setup.getCategoryBadgeLabel() + "]");
        badge.setFont(BossWiseDesign.getBadgeFont());
        badge.setForeground(badgeColor);
        badgeRow.add(badge);

        if (setup.getWikiSourceUrl() != null && !setup.getWikiSourceUrl().isEmpty()) {
            GoldButton wikiBtn = new GoldButton("Wiki Guide");
            wikiBtn.setFont(BossWiseDesign.getBadgeFont());
            wikiBtn.setPreferredSize(new Dimension(72, 18));
            wikiBtn.addActionListener(e -> {
                net.runelite.client.util.LinkBrowser.browse(setup.getWikiSourceUrl());
            });
            badgeRow.add(wikiBtn);
        }
        sumCard.add(badgeRow);
        sumCard.add(Box.createVerticalStrut(2));

        JLabel remLbl = new JLabel("Remaining: " + GearOptimizerService.formatGp(setup.getRemainingBudget()) + " GP");
        remLbl.setFont(BossWiseDesign.getSmallFont());
        remLbl.setForeground(BossWiseDesign.TEXT_SECONDARY);
        remLbl.setAlignmentX(LEFT_ALIGNMENT);
        sumCard.add(remLbl);
        sumCard.add(Box.createVerticalStrut(4));

        // Primary Combat Calculations Grid (2x2)
        JPanel combatGrid = new JPanel(new GridLayout(2, 2, 4, 2)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        combatGrid.setOpaque(false);
        combatGrid.setAlignmentX(LEFT_ALIGNMENT);

        CombatResult result = setup.getCombatResult();
        double dps = (result != null) ? result.getDps() : setup.getCalculatedDps();
        int maxHit = (result != null) ? result.getMaxHit() : setup.getMaxHit();
        String hitChanceStr = (result != null) ? result.getFormattedHitChance() : setup.getHitChance();
        if (hitChanceStr == null || hitChanceStr.isEmpty()) hitChanceStr = "0.0%";

        combatGrid.add(createMiniStat("DPS", String.format("%.2f", dps), BossWiseDesign.GOLD));
        combatGrid.add(createMiniStat("Max Hit", String.valueOf(maxHit), BossWiseDesign.STATUS_READY));
        combatGrid.add(createMiniStat("Accuracy", hitChanceStr, BossWiseDesign.STATUS_INFO));
        combatGrid.add(createMiniStat("Offence", "+" + setup.getTotalOffenceBonus(), BossWiseDesign.GOLD_HOVER));
        sumCard.add(combatGrid);
        sumCard.add(Box.createVerticalStrut(3));

        // Secondary Equipment Stats Grid (2x2)
        JPanel bonusGrid = new JPanel(new GridLayout(2, 2, 4, 2)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        bonusGrid.setOpaque(false);
        bonusGrid.setAlignmentX(LEFT_ALIGNMENT);

        if (selectedStyle == CombatStyle.RANGED) {
            bonusGrid.add(createMiniStat("Rng Str", "+" + setup.getTotalRangedStrength(), BossWiseDesign.TEXT_PRIMARY));
        } else if (selectedStyle == CombatStyle.MAGIC) {
            bonusGrid.add(createMiniStat("Mage Dmg", "+" + setup.getTotalMagicDamagePercent() + "%", BossWiseDesign.TEXT_PRIMARY));
        } else {
            bonusGrid.add(createMiniStat("Melee Str", "+" + setup.getTotalMeleeStrength(), BossWiseDesign.TEXT_PRIMARY));
        }
        bonusGrid.add(createMiniStat("Avg Def", "+" + setup.getTotalDefenceBonus(), BossWiseDesign.TEXT_SECONDARY));
        bonusGrid.add(createMiniStat("Prayer", "+" + setup.getTotalPrayerBonus(), BossWiseDesign.GOLD_HOVER));
        int speedTicks = (result != null && result.getAttackSpeedTicks() > 0) ? result.getAttackSpeedTicks() : 4;
        bonusGrid.add(createMiniStat("Speed", speedTicks + " Ticks (" + String.format("%.1fs", speedTicks * 0.6) + ")", BossWiseDesign.TEXT_MUTED));
        sumCard.add(bonusGrid);

        summaryContainer.add(sumCard);

        // Feature 3: Current Gear Analyser Comparison Card
        if (currentGearScanner != null) {
            MonsterCombatProfile profile = (monster != null)
                    ? monster.getCombatProfile()
                    : ((boss != null) ? optimizerService.getBossProfile(boss).toMonsterCombatProfile() : null);

            CurrentGearComparison comp = currentGearScanner.compareWithRecommended(
                    setup, profile, new CombatCalculationService(), account
            );

            if (comp != null) {
                VerticalPanel compCard = new VerticalPanel();
                compCard.setBackground(BossWiseDesign.CARD_BG);
                compCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

                WrapText cTitle = new WrapText("EQUIPPED VS RECOMMENDED", BossWiseDesign.getBadgeFont(), BossWiseDesign.GOLD);
                compCard.add(cTitle);
                compCard.add(Box.createVerticalStrut(3));

                double dpsDiff = comp.getDpsDifference();
                String dSign = dpsDiff >= 0 ? "+" : "";
                WrapText dpsDelta = new WrapText(String.format("Recommended: %s%.2f DPS (%s%.1f%%)",
                        dSign, dpsDiff, dSign, comp.getDpsPercentageImprovement()),
                        BossWiseDesign.getBoldFont(),
                        dpsDiff >= 0 ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);
                compCard.add(dpsDelta);

                WrapText maxDelta = new WrapText(String.format("Max Hit: %+d  |  Accuracy: %+.1f%%",
                        comp.getMaxHitDifference(), comp.getAccuracyDifference()),
                        BossWiseDesign.getSmallFont(),
                        BossWiseDesign.TEXT_SECONDARY);
                compCard.add(maxDelta);

                for (String protWarn : comp.getMissingProtections()) {
                    WrapText warn = new WrapText("! " + protWarn, BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_LOCKED);
                    compCard.add(warn);
                }

                summaryContainer.add(Box.createVerticalStrut(4));
                summaryContainer.add(compCard);
            }
        }

        // Feature 18: Bank-Aware Gear Status Card
        if (bankChecker != null) {
            BankGearCheckResult bankRes = bankChecker.checkSetupAgainstBank(setup);
            VerticalPanel bCard = new VerticalPanel();
            bCard.setBackground(BossWiseDesign.CARD_BG);
            bCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

            JLabel bTitle = new JLabel("BANK-AWARE STATUS");
            bTitle.setFont(BossWiseDesign.getBadgeFont());
            bTitle.setForeground(BossWiseDesign.STATUS_INFO);
            bCard.add(bTitle);
            bCard.add(Box.createVerticalStrut(2));

            String ownedText = bankRes.isBankScanned()
                    ? String.format("Owned: %d / %d Items (%.0f%%) in Bank",
                            bankRes.getOwnedSlotsCount(), bankRes.getTotalSlotsCount(), bankRes.getOwnedPercentage())
                    : "Bank not yet opened in-game (Open bank to scan owned gear)";
            WrapText bStatus = new WrapText(ownedText, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY);
            bCard.add(bStatus);

            if (bankRes.isBankScanned()) {
                WrapText bMissing = new WrapText("Missing Gear Cost: " + bankRes.getFormattedMissingCost() + " to complete setup",
                        BossWiseDesign.getSmallFont(),
                        BossWiseDesign.GOLD_HOVER);
                bCard.add(bMissing);
            }

            summaryContainer.add(Box.createVerticalStrut(4));
            summaryContainer.add(bCard);
        }

        // 4. Combat Highlights & Mechanics Card
        List<String> highlights = setup.getHighlights();
        if (highlights != null && !highlights.isEmpty()) {
            VerticalPanel hlCard = new VerticalPanel();
            hlCard.setBackground(BossWiseDesign.CARD_BG);
            hlCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 7, 5, 7)
            ));

            JLabel hlTitle = new JLabel("TACTICAL HIGHLIGHTS");
            hlTitle.setFont(BossWiseDesign.getBadgeFont());
            hlTitle.setForeground(BossWiseDesign.GOLD);
            hlCard.add(hlTitle);
            hlCard.add(Box.createVerticalStrut(2));

            for (String hl : highlights) {
                WrapText hlText = new WrapText("• " + hl, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
                hlCard.add(hlText);
                hlCard.add(Box.createVerticalStrut(2));
            }
            highlightsContainer.add(hlCard);
        }

        // 5. Equipment Slots List with Missing Requirements Check
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            EquipmentItem item = setup.getItem(slot);
            long price = optimizerService.getItemPrice(item);
            String reason = setup.getReason(slot);
            List<String> missing = optimizerService.getMissingRequirements(item, account);
            EquipmentSlotRow row = new EquipmentSlotRow(slot, item, price, reason, missing, s -> {});
            equipmentContainer.add(row);
        }

        // 6. Upgrades Adviser Section
        List<GearUpgradeRecommendation> upgrades = upgradeAdvisor.findUpgrades(setup, boss, setup.getRemainingBudget(), account);
        if (!upgrades.isEmpty()) {
            VerticalPanel upSection = new VerticalPanel();
            upSection.setOpaque(false);

            JLabel upTitle = new JLabel("UPGRADE ROADMAP");
            upTitle.setFont(BossWiseDesign.getHeaderFont());
            upTitle.setForeground(BossWiseDesign.GOLD);
            upSection.add(upTitle);
            upSection.add(Box.createVerticalStrut(4));

            int count = 0;
            for (GearUpgradeRecommendation up : upgrades) {
                if (count++ >= 4) break;

                VerticalPanel upCard = new VerticalPanel();
                upCard.setBackground(BossWiseDesign.CARD_BG);
                upCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_SUBTLE),
                        BorderFactory.createEmptyBorder(5, 7, 5, 7)
                ));

                JPanel upTop = new JPanel(new BorderLayout(4, 0)) {
                    @Override
                    public Dimension getMaximumSize() {
                        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                    }
                };
                upTop.setOpaque(false);
                upTop.setAlignmentX(LEFT_ALIGNMENT);

                JLabel slotLbl = new JLabel(up.getSlot().getDisplayName().toUpperCase());
                slotLbl.setFont(BossWiseDesign.getBadgeFont());
                slotLbl.setForeground(BossWiseDesign.GOLD);
                upTop.add(slotLbl, BorderLayout.WEST);

                JLabel diffLbl = new JLabel("+" + GearOptimizerService.formatGp(up.getPriceDifference()) + " GP");
                diffLbl.setFont(BossWiseDesign.getBadgeFont());
                diffLbl.setForeground(up.isFitsBudget() ? BossWiseDesign.STATUS_READY : BossWiseDesign.GOLD);
                upTop.add(diffLbl, BorderLayout.EAST);
                upCard.add(upTop);
                upCard.add(Box.createVerticalStrut(2));

                WrapText title = new WrapText(up.getCurrentItem().getName() + " \u2192 " + up.getUpgradeItem().getName(), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
                upCard.add(title);
                upCard.add(Box.createVerticalStrut(2));

                WrapText detail = new WrapText(up.getStatComparison() + " \u2022 " + up.getWhyItMatters(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
                upCard.add(detail);

                upSection.add(upCard);
                upSection.add(Box.createVerticalStrut(4));
            }

            upgradesContainer.add(upSection);
        }

        revalidateContainers();
    }

    private void renderComparisonMode(List<OptimizedGearSetup> setups, BossData boss, MonsterData monster, PlayerAccount account) {
        summaryContainer.removeAll();
        highlightsContainer.removeAll();
        equipmentContainer.removeAll();
        upgradesContainer.removeAll();

        com.bosswise.model.MultiSetupComparison comparison = comparisonService.compareSetups(setups);

        // 1. Overview & Insights Card
        VerticalPanel ovCard = new VerticalPanel();
        ovCard.setBackground(BossWiseDesign.CARD_BG);
        ovCard.setBorder(BossWiseDesign.BORDER_CARD);

        JPanel ovHeader = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        ovHeader.setOpaque(false);
        ovHeader.setAlignmentX(LEFT_ALIGNMENT);

        JLabel ovTitle = new JLabel("MULTI-SETUP COMPARISON (" + comparison.getSetups().size() + " SETUPS)");
        ovTitle.setFont(BossWiseDesign.getHeaderFont());
        ovTitle.setForeground(BossWiseDesign.GOLD);
        ovHeader.add(ovTitle, BorderLayout.WEST);
        ovCard.add(ovHeader);
        ovCard.add(Box.createVerticalStrut(4));

        for (String insight : comparison.getInsights()) {
            WrapText insText = new WrapText("★ " + insight, BossWiseDesign.getBoldFont(), BossWiseDesign.STATUS_READY);
            ovCard.add(insText);
            ovCard.add(Box.createVerticalStrut(2));
        }
        summaryContainer.add(ovCard);
        summaryContainer.add(Box.createVerticalStrut(6));

        // 2. Metrics Card
        VerticalPanel metCard = new VerticalPanel();
        metCard.setBackground(BossWiseDesign.CARD_BG);
        metCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel metTitle = new JLabel("PERFORMANCE COMPARISON");
        metTitle.setFont(BossWiseDesign.getBadgeFont());
        metTitle.setForeground(BossWiseDesign.GOLD);
        metCard.add(metTitle);
        metCard.add(Box.createVerticalStrut(4));

        for (com.bosswise.model.MultiSetupComparison.SetupComparisonMetric metric : comparison.getMetrics()) {
            JPanel mRow = new JPanel(new BorderLayout(4, 0)) {
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                }
            };
            mRow.setOpaque(false);
            mRow.setAlignmentX(LEFT_ALIGNMENT);

            JLabel nameLbl = new JLabel(metric.getMetricName() + ":");
            nameLbl.setFont(BossWiseDesign.getBoldFont());
            nameLbl.setForeground(BossWiseDesign.TEXT_SECONDARY);
            mRow.add(nameLbl, BorderLayout.WEST);
            metCard.add(mRow);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < metric.getValues().size(); i++) {
                if (i > 0) sb.append("  |  ");
                String val = metric.getValues().get(i);
                if (i == metric.getBestIndex()) {
                    sb.append("★ ").append(val);
                } else {
                    sb.append(val);
                }
            }
            WrapText valText = new WrapText(sb.toString(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY);
            metCard.add(valText);
            metCard.add(Box.createVerticalStrut(3));
        }
        summaryContainer.add(metCard);
        summaryContainer.add(Box.createVerticalStrut(6));

        // 3. Matched Inventories Card
        if (!comparison.getMatchedInventories().isEmpty()) {
            VerticalPanel invCard = new VerticalPanel();
            invCard.setBackground(BossWiseDesign.CARD_BG);
            invCard.setBorder(BossWiseDesign.BORDER_CARD);

            JLabel invTitle = new JLabel("MATCHED 28-SLOT INVENTORIES");
            invTitle.setFont(BossWiseDesign.getBadgeFont());
            invTitle.setForeground(BossWiseDesign.GOLD);
            invCard.add(invTitle);
            invCard.add(Box.createVerticalStrut(4));

            for (Map.Entry<Integer, com.bosswise.model.InventorySetup> entry : comparison.getMatchedInventories().entrySet()) {
                int setupIdx = entry.getKey();
                if (setupIdx < comparison.getSetups().size()) {
                    OptimizedGearSetup s = comparison.getSetups().get(setupIdx);
                    com.bosswise.model.InventorySetup inv = entry.getValue();
                    WrapText invRow = new WrapText(
                            String.format("• %s: %s (%s GP supplies)",
                                    s.getSetupName(), inv.getTierName(), GearOptimizerService.formatGp(inv.getTotalCost())),
                            BossWiseDesign.getSmallFont(),
                            BossWiseDesign.TEXT_PRIMARY
                    );
                    invCard.add(invRow);
                    invCard.add(Box.createVerticalStrut(2));
                }
            }
            summaryContainer.add(invCard);
            summaryContainer.add(Box.createVerticalStrut(6));
        }

        // 4. Equipment Slot Matrix Card
        VerticalPanel matCard = new VerticalPanel();
        matCard.setBackground(BossWiseDesign.CARD_BG);
        matCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel matTitle = new JLabel("SLOT-BY-SLOT GEAR BREAKDOWN");
        matTitle.setFont(BossWiseDesign.getBadgeFont());
        matTitle.setForeground(BossWiseDesign.GOLD);
        matCard.add(matTitle);
        matCard.add(Box.createVerticalStrut(4));

        for (Map.Entry<EquipmentSlot, List<EquipmentItem>> entry : comparison.getSlotMatrix().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            List<EquipmentItem> items = entry.getValue();

            boolean allSame = true;
            String firstName = items.isEmpty() || items.get(0) == null ? "None" : items.get(0).getName();
            for (EquipmentItem it : items) {
                String curName = (it == null) ? "None" : it.getName();
                if (!curName.equalsIgnoreCase(firstName)) {
                    allSame = false;
                    break;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(slot.getDisplayName().toUpperCase()).append(": ");
            if (allSame) {
                sb.append(firstName).append(" (All Setups)");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    if (i > 0) sb.append(" \u2192 ");
                    sb.append(items.get(i) != null ? items.get(i).getName() : "None");
                }
            }
            WrapText slotText = new WrapText(sb.toString(), BossWiseDesign.getSmallFont(), allSame ? BossWiseDesign.TEXT_SECONDARY : BossWiseDesign.GOLD_HOVER);
            matCard.add(slotText);
            matCard.add(Box.createVerticalStrut(2));
        }
        summaryContainer.add(matCard);

        revalidateContainers();
    }

    private void revalidateContainers() {

        styleRankingContainer.revalidate();
        styleRankingContainer.repaint();
        summaryContainer.revalidate();
        summaryContainer.repaint();
        highlightsContainer.revalidate();
        highlightsContainer.repaint();
        equipmentContainer.revalidate();
        equipmentContainer.repaint();
        upgradesContainer.revalidate();
        upgradesContainer.repaint();
    }

    private JPanel createMiniStat(String label, String value, Color valColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label + ":");
        l.setFont(BossWiseDesign.getSmallFont());
        l.setForeground(BossWiseDesign.TEXT_MUTED);
        p.add(l);
        JLabel v = new JLabel(value);
        v.setFont(BossWiseDesign.getSmallFont());
        v.setForeground(valColor);
        p.add(v);
        return p;
    }
}
