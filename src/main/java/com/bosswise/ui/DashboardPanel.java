package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.BossEligibility;
import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.model.GearUpgradeRecommendation;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.ProgressionGoal;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.LootTrackerService;
import com.bosswise.service.ProgressionService;
import com.bosswise.ui.components.BossCard;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.StatProgressBar;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import net.runelite.api.Skill;

public class DashboardPanel extends JPanel {
    private final AccountScannerService accountScanner;
    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final BossRecommendationEngine recommendationEngine;
    private final GearOptimizerService optimizerService;
    private final GearUpgradeAdvisor upgradeAdvisor;
    private final ProgressionService progressionService;
    private final LootTrackerService lootTrackerService;
    private final CurrentGearScannerService currentGearScanner;

    private final Consumer<String> onNavigate;
    private final Consumer<BossData> onSelectBossForGear;
    private final Consumer<BossData> onSelectBossForGuide;

    private final ScrollablePanel contentPanel;
    private final JTextField globalSearchField = new JTextField();
    private String currentSearchQuery = "";

    public DashboardPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            ProgressionService progressionService,
            Consumer<String> onNavigate,
            Consumer<BossData> onSelectBossForGear,
            Consumer<BossData> onSelectBossForGuide) {

        this(accountScanner, bossDatabase, new MonsterDatabase(), recommendationEngine, optimizerService,
                upgradeAdvisor, progressionService,
                new LootTrackerService(new com.bosswise.service.GePriceService(null), bossDatabase, new MonsterDatabase(), null),
                new CurrentGearScannerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)),
                onNavigate, onSelectBossForGear, onSelectBossForGuide);
    }

    public DashboardPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            BossRecommendationEngine recommendationEngine,
            GearOptimizerService optimizerService,
            GearUpgradeAdvisor upgradeAdvisor,
            ProgressionService progressionService,
            LootTrackerService lootTrackerService,
            CurrentGearScannerService currentGearScanner,
            Consumer<String> onNavigate,
            Consumer<BossData> onSelectBossForGear,
            Consumer<BossData> onSelectBossForGuide) {

        this.accountScanner = accountScanner;
        this.bossDatabase = bossDatabase;
        this.monsterDatabase = monsterDatabase;
        this.recommendationEngine = recommendationEngine;
        this.optimizerService = optimizerService;
        this.upgradeAdvisor = upgradeAdvisor;
        this.progressionService = progressionService;
        this.lootTrackerService = lootTrackerService;
        this.currentGearScanner = currentGearScanner;
        this.onNavigate = onNavigate;
        this.onSelectBossForGear = onSelectBossForGear;
        this.onSelectBossForGuide = onSelectBossForGuide;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        contentPanel = new ScrollablePanel();
        contentPanel.setLeftAlignChildren(true);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BossWiseDesign.BG_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        contentPanel.removeAll();

        PlayerAccount account = accountScanner.getCurrentAccount();
        if (account == null) {
            account = PlayerAccount.createDefault();
        }

        // 0. Global Search Bar (Feature 21)
        contentPanel.add(buildGlobalSearchBar());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        if (currentSearchQuery != null && !currentSearchQuery.trim().isEmpty()) {
            contentPanel.add(buildSearchResultsCard(currentSearchQuery.trim()));
            contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // 1. Account Summary Card
        contentPanel.add(buildAccountCard(account));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 1b. Live Loot & Session Overview (Feature 1 & 20)
        if (lootTrackerService != null) {
            contentPanel.add(buildLiveSessionCard());
            contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // 2. Boss Readiness Statistics Cards
        contentPanel.add(buildMetricsCard(account));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 3. Recommended Next Boss Card
        contentPanel.add(buildNextBossCard(account));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 4. Recommended Gear Upgrade Card
        contentPanel.add(buildNextUpgradeCard(account));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 5. Progression Roadmap Preview
        contentPanel.add(buildRoadmapPreview(account));

        revalidate();
        repaint();
    }

    private JPanel buildGlobalSearchBar() {
        JPanel bar = new JPanel(new BorderLayout(4, 0));
        bar.setBackground(BossWiseDesign.CARD_BG);
        bar.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

        JLabel icon = new JLabel("\uD83D\uDD0D");
        icon.setFont(BossWiseDesign.getBoldFont());
        icon.setForeground(BossWiseDesign.GOLD);
        bar.add(icon, BorderLayout.WEST);

        globalSearchField.setFont(BossWiseDesign.getSmallFont());
        globalSearchField.setBackground(BossWiseDesign.CARD_BG);
        globalSearchField.setForeground(BossWiseDesign.TEXT_PRIMARY);
        globalSearchField.setCaretColor(BossWiseDesign.GOLD);
        globalSearchField.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        globalSearchField.setToolTipText("Global search monsters, gear, bosses, and slayer tasks...");
        globalSearchField.addActionListener(e -> {
            this.currentSearchQuery = globalSearchField.getText();
            refresh();
        });
        bar.add(globalSearchField, BorderLayout.CENTER);

        GoldButton goBtn = new GoldButton("Find");
        goBtn.setFont(BossWiseDesign.getBadgeFont());
        goBtn.setPreferredSize(new Dimension(42, 20));
        goBtn.addActionListener(e -> {
            this.currentSearchQuery = globalSearchField.getText();
            refresh();
        });
        bar.add(goBtn, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildSearchResultsCard(String query) {
        VerticalPanel card = new VerticalPanel();
        card.setBackground(BossWiseDesign.CARD_BG);
        card.setBorder(BossWiseDesign.BORDER_GOLD);

        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setOpaque(false);
        JLabel title = new JLabel("SEARCH RESULTS: '" + query + "'");
        title.setFont(BossWiseDesign.getBadgeFont());
        title.setForeground(BossWiseDesign.GOLD);
        top.add(title, BorderLayout.WEST);

        GoldButton clear = new GoldButton("Clear");
        clear.setFont(BossWiseDesign.getBadgeFont());
        clear.setPreferredSize(new Dimension(45, 18));
        clear.addActionListener(e -> {
            this.currentSearchQuery = "";
            globalSearchField.setText("");
            refresh();
        });
        top.add(clear, BorderLayout.EAST);
        card.add(top);
        card.add(Box.createVerticalStrut(4));

        String q = query.toLowerCase();
        int matches = 0;

        // Search bosses
        for (BossData b : bossDatabase.getAllBosses().values()) {
            if (b.getName().toLowerCase().contains(q)) {
                matches++;
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(false);
                JLabel name = new JLabel("Boss: " + b.getName());
                name.setFont(BossWiseDesign.getSmallFont());
                name.setForeground(BossWiseDesign.TEXT_PRIMARY);
                row.add(name, BorderLayout.WEST);

                GoldButton view = new GoldButton("Gear");
                view.setPreferredSize(new Dimension(45, 18));
                view.setFont(BossWiseDesign.getBadgeFont());
                view.addActionListener(e -> onSelectBossForGear.accept(b));
                row.add(view, BorderLayout.EAST);
                card.add(row);
                card.add(Box.createVerticalStrut(2));
                if (matches >= 4) break;
            }
        }

        if (matches == 0) {
            JLabel empty = new JLabel("No direct matches found.");
            empty.setFont(BossWiseDesign.getSmallFont());
            empty.setForeground(BossWiseDesign.TEXT_MUTED);
            card.add(empty);
        }

        return card;
    }

    private JPanel buildLiveSessionCard() {
        VerticalPanel card = new VerticalPanel();
        card.setBackground(BossWiseDesign.CARD_BG);
        card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

        com.bosswise.model.LootSession session = lootTrackerService.getActiveSession();

        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setOpaque(false);
        JLabel sTitle = new JLabel("ACTIVE SESSION: " + session.getMonsterName().toUpperCase());
        sTitle.setFont(BossWiseDesign.getBadgeFont());
        sTitle.setForeground(BossWiseDesign.GOLD);
        top.add(sTitle, BorderLayout.WEST);

        GoldButton viewLoot = new GoldButton("Loot Tab");
        viewLoot.setFont(BossWiseDesign.getBadgeFont());
        viewLoot.setPreferredSize(new Dimension(65, 18));
        viewLoot.addActionListener(e -> onNavigate.accept("LOOT_TRACKER"));
        top.add(viewLoot, BorderLayout.EAST);
        card.add(top);
        card.add(Box.createVerticalStrut(3));

        long netProfit = session.getNetProfit();
        WrapText profit = new WrapText(String.format("Net Profit: %s GP  |  %d Kills (%s)",
                formatGp(netProfit), session.getKills(), session.getFormattedDuration()), BossWiseDesign.getBoldFont(),
                netProfit >= 0 ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);
        card.add(profit);

        WrapText rates = new WrapText(String.format("Profit Rate: %s/hr  |  Supplies: -%s",
                formatGp(session.getProfitPerHour()), formatGp(session.getEstimatedSupplyCosts())), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        card.add(rates);

        return card;
    }

    private String formatGp(long amount) {
        if (amount >= 1_000_000) return String.format("%.2fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%dK", amount / 1_000);
        return String.valueOf(amount);
    }

    private JPanel buildAccountCard(PlayerAccount account) {
        VerticalPanel card = new VerticalPanel();
        card.setBackground(BossWiseDesign.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BossWiseDesign.BORDER_CARD);

        // Header: Name & Combat Level
        JPanel header = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        String displayName = account.isLoggedIn() ? account.getUsername() : "Waiting for Login...";
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(BossWiseDesign.getTitleFont());
        nameLabel.setForeground(account.isLoggedIn() ? BossWiseDesign.TEXT_PRIMARY : BossWiseDesign.TEXT_MUTED);
        header.add(nameLabel, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        headerActions.setOpaque(false);

        JLabel cbLabel = new JLabel(account.isLoggedIn() ? "Combat " + account.getCombatLevel() : "Log In");
        cbLabel.setFont(BossWiseDesign.getBoldFont());
        cbLabel.setForeground(account.isLoggedIn() ? BossWiseDesign.GOLD : BossWiseDesign.STATUS_LOCKED);
        headerActions.add(cbLabel);

        GoldButton syncBtn = new GoldButton("\u21bb");
        syncBtn.setFont(BossWiseDesign.getBadgeFont());
        syncBtn.setPreferredSize(new Dimension(22, 20));
        syncBtn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        syncBtn.setToolTipText("Sync player stats from active game session");
        syncBtn.addActionListener(e -> {
            accountScanner.scanAccount();
            refresh();
        });
        headerActions.add(syncBtn);

        header.add(headerActions, BorderLayout.EAST);
        card.add(header);

        String subText = account.isLoggedIn()
                ? "Total Level: " + account.getTotalLevel()
                : "Log into Old School RuneScape to load character.";
        WrapText totalLabel = new WrapText(subText, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        card.add(totalLabel);
        card.add(Box.createVerticalStrut(6));

        // Combat Stats Grid: 4 rows x 2 cols (clean, spacious, zero horizontal overflow)
        JPanel statsGrid = new JPanel(new GridLayout(4, 2, 4, 4)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        statsGrid.setOpaque(false);
        statsGrid.setAlignmentX(LEFT_ALIGNMENT);

        addStatCell(statsGrid, "ATK", Skill.ATTACK, account);
        addStatCell(statsGrid, "STR", Skill.STRENGTH, account);
        addStatCell(statsGrid, "DEF", Skill.DEFENCE, account);
        addStatCell(statsGrid, "HP",  Skill.HITPOINTS, account);
        addStatCell(statsGrid, "RNG", Skill.RANGED, account);
        addStatCell(statsGrid, "MAG", Skill.MAGIC, account);
        addStatCell(statsGrid, "PRY", Skill.PRAYER, account);
        addStatCell(statsGrid, "SLY", Skill.SLAYER, account);

        card.add(statsGrid);
        return card;
    }

    private void addStatCell(JPanel parent, String label, Skill skill, PlayerAccount account) {
        JPanel cell = new JPanel(new BorderLayout(4, 0));
        cell.setBackground(BossWiseDesign.PANEL_BG);
        cell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)
        ));

        JLabel title = new JLabel(label);
        title.setFont(BossWiseDesign.getBadgeFont());
        title.setForeground(BossWiseDesign.TEXT_MUTED);
        cell.add(title, BorderLayout.WEST);

        int real = account.getRealLevel(skill);
        int boosted = account.getBoostedLevel(skill);
        String valStr = String.valueOf(real);
        if (boosted != real) {
            valStr = boosted + "/" + real;
        }

        JLabel val = new JLabel(valStr);
        val.setFont(BossWiseDesign.getBoldFont());
        val.setForeground((boosted > real) ? BossWiseDesign.STATUS_READY : (boosted < real ? BossWiseDesign.STATUS_LOCKED : BossWiseDesign.TEXT_PRIMARY));
        cell.add(val, BorderLayout.EAST);

        parent.add(cell);
    }

    private JPanel buildMetricsCard(PlayerAccount account) {
        int readyCount = 0;
        int almostCount = 0;
        int lockedCount = 0;

        for (BossData boss : bossDatabase.getAllBosses().values()) {
            BossEligibility el = recommendationEngine.evaluateEligibility(boss, account);
            if (el.isReady()) readyCount++;
            else if (el.isAlmostReady()) almostCount++;
            else lockedCount++;
        }

        JPanel card = new JPanel(new GridLayout(1, 3, 4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(LEFT_ALIGNMENT);

        card.add(createPill("READY", readyCount, BossWiseDesign.STATUS_READY));
        card.add(createPill("ALMOST", almostCount, BossWiseDesign.STATUS_ALMOST));
        card.add(createPill("LOCKED", lockedCount, BossWiseDesign.STATUS_LOCKED));

        return card;
    }

    private JPanel createPill(String title, int count, java.awt.Color color) {
        JPanel pill = new JPanel();
        pill.setLayout(new BoxLayout(pill, BoxLayout.Y_AXIS));
        pill.setBackground(BossWiseDesign.CARD_BG);
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 2, 4, 2)
        ));

        JLabel countLabel = new JLabel(String.valueOf(count), JLabel.CENTER);
        countLabel.setFont(BossWiseDesign.getTitleFont());
        countLabel.setForeground(color);
        countLabel.setAlignmentX(CENTER_ALIGNMENT);
        pill.add(countLabel);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(BossWiseDesign.getBadgeFont());
        titleLabel.setForeground(BossWiseDesign.TEXT_SECONDARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        pill.add(titleLabel);

        return pill;
    }

    private JPanel buildNextBossCard(PlayerAccount account) {
        VerticalPanel section = new VerticalPanel();
        section.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("RECOMMENDED NEXT BOSS");
        title.setFont(BossWiseDesign.getHeaderFont());
        title.setForeground(BossWiseDesign.GOLD);
        header.add(title, BorderLayout.WEST);

        section.add(header);
        section.add(Box.createVerticalStrut(6));

        List<BossData> bosses = recommendationEngine.getFilteredBosses(
                account, BossEligibilityStatus.READY, null, null, null, BossRecommendationEngine.SortOrder.RECOMMENDED
        );

        if (!bosses.isEmpty()) {
            BossData topBoss = bosses.get(0);
            BossEligibility el = recommendationEngine.evaluateEligibility(topBoss, account);
            section.add(new BossCard(topBoss, el, onSelectBossForGear, onSelectBossForGuide));
        } else {
            WrapText empty = new WrapText("Complete prerequisite quests to unlock recommended bosses.", BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_SECONDARY);
            section.add(empty);
        }

        return section;
    }

    private JPanel buildNextUpgradeCard(PlayerAccount account) {
        VerticalPanel card = new VerticalPanel();
        card.setBackground(BossWiseDesign.CARD_BG);
        card.setOpaque(true);
        card.setBorder(BossWiseDesign.BORDER_CARD);

        JPanel header = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("SUGGESTED NEXT UPGRADE");
        title.setFont(BossWiseDesign.getHeaderFont());
        title.setForeground(BossWiseDesign.GOLD);
        header.add(title, BorderLayout.WEST);
        card.add(header);
        card.add(Box.createVerticalStrut(6));

        // Generate baseline gear setup for best value
        BossData topBoss = bossDatabase.getBoss("vorkath");
        if (topBoss == null) topBoss = bossDatabase.getAllBosses().values().iterator().next();

        OptimizedGearSetup setup = optimizerService.generateSetup(
                "Best Value", topBoss, topBoss.getPrimaryStyle(), 5_000_000L, account, GearOptimizerService.PriorityMode.BALANCED
        );

        List<GearUpgradeRecommendation> upgrades = upgradeAdvisor.findUpgrades(setup, topBoss, 5_000_000L, account);

        if (!upgrades.isEmpty()) {
            GearUpgradeRecommendation top = upgrades.get(0);

            // Row 1: Slot (West) + Cost difference (East)
            JPanel topRow = new JPanel(new BorderLayout(4, 0)) {
                @Override
                public Dimension getMaximumSize() {
                    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
                }
            };
            topRow.setOpaque(false);
            topRow.setAlignmentX(LEFT_ALIGNMENT);

            JLabel slotLabel = new JLabel(top.getSlot().getDisplayName().toUpperCase());
            slotLabel.setFont(BossWiseDesign.getBadgeFont());
            slotLabel.setForeground(BossWiseDesign.GOLD);
            topRow.add(slotLabel, BorderLayout.WEST);

            JLabel costLabel = new JLabel("+" + GearOptimizerService.formatGp(top.getPriceDifference()) + " GP");
            costLabel.setFont(BossWiseDesign.getBadgeFont());
            costLabel.setForeground(top.isFitsBudget() ? BossWiseDesign.STATUS_READY : BossWiseDesign.GOLD);
            topRow.add(costLabel, BorderLayout.EAST);
            card.add(topRow);
            card.add(Box.createVerticalStrut(2));

            // Row 2: Item A -> Item B (snug, wrapping, never clipped)
            WrapText nameText = new WrapText(top.getCurrentItem().getName() + " \u2192 " + top.getUpgradeItem().getName(), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
            card.add(nameText);
            card.add(Box.createVerticalStrut(2));

            // Row 3: Stat comparison & why it matters (snug, wrapping, never clipped)
            WrapText statText = new WrapText(top.getStatComparison() + " \u2022 " + top.getWhyItMatters(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            card.add(statText);
        } else {
            WrapText empty = new WrapText("Your gear is optimized for your current budget!", BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_SECONDARY);
            card.add(empty);
        }

        return card;
    }

    private JPanel buildRoadmapPreview(PlayerAccount account) {
        VerticalPanel section = new VerticalPanel();
        section.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        JLabel title = new JLabel("IMMEDIATE GOALS");
        title.setFont(BossWiseDesign.getHeaderFont());
        title.setForeground(BossWiseDesign.GOLD);
        header.add(title, BorderLayout.WEST);

        GoldButton viewAll = new GoldButton("View All");
        viewAll.setFont(BossWiseDesign.getBadgeFont());
        viewAll.setPreferredSize(new Dimension(64, 22));
        viewAll.setMargin(new java.awt.Insets(1, 4, 1, 4));
        viewAll.addActionListener(e -> {
            if (onNavigate != null) onNavigate.accept("Progression");
        });
        header.add(viewAll, BorderLayout.EAST);

        section.add(header);
        section.add(Box.createVerticalStrut(6));

        List<ProgressionGoal> goals = progressionService.generateRoadmap(account);
        int count = 0;
        for (ProgressionGoal goal : goals) {
            if (count++ >= 3) break;

            VerticalPanel goalCard = new VerticalPanel();
            goalCard.setBackground(BossWiseDesign.CARD_BG);
            goalCard.setOpaque(true);
            goalCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_SUBTLE),
                    BorderFactory.createEmptyBorder(6, 7, 6, 7)
            ));

            WrapText gTitle = new WrapText(goal.getTitle(), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
            goalCard.add(gTitle);
            goalCard.add(Box.createVerticalStrut(2));

            WrapText gDesc = new WrapText(goal.getDescription(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            goalCard.add(gDesc);
            goalCard.add(Box.createVerticalStrut(4));

            StatProgressBar bar = new StatProgressBar(goal.getCurrentProgress(), goal.getTargetProgress());
            goalCard.add(bar);

            section.add(goalCard);
            section.add(Box.createVerticalStrut(6));
        }

        return section;
    }
}
