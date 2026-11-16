package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.db.MonsterStrategyDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterData;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.strategy.MonsterStrategy;
import com.bosswise.model.strategy.PersonalizedStrategyScore;
import com.bosswise.model.strategy.StrategyCombatMechanic;
import com.bosswise.model.strategy.StrategyMethod;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.MonsterStrategyService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.util.LinkBrowser;

/**
 * Comprehensive Universal OSRS Monster Strategy Engine & Guide Panel.
 * Researches and presents monster-specific combat mechanics, prayers, phases,
 * avoidance tips, equipment advantages, and personalized player recommendations.
 */
public class BossGuidePanel extends JPanel {
    private static final int CARD_GAP = 6;

    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final MonsterStrategyDatabase strategyDatabase;
    private final MonsterStrategyService strategyService;
    private final AccountScannerService accountScanner;
    private final Consumer<BossData> openGearOptimizerCallback;
    private final Consumer<BossData> openInventoryCallback;

    // Filter controls
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> categoryFilter = new JComboBox<>(new String[]{"All Categories", "Bosses", "Slayer", "Wilderness", "Raids", "Regular"});
    private final JComboBox<String> bossSelector = new JComboBox<>();
    private final ScrollablePanel contentContainer = new ScrollablePanel();

    private int selectedMethodIndex = 0;

    public BossGuidePanel(BossDatabase bossDatabase) {
        this(bossDatabase, new MonsterDatabase(), new MonsterStrategyDatabase(), null, null, null, null);
    }

    public BossGuidePanel(BossDatabase bossDatabase, MonsterDatabase monsterDatabase) {
        this(bossDatabase, monsterDatabase, new MonsterStrategyDatabase(), null, null, null, null);
    }

    public BossGuidePanel(
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            MonsterStrategyDatabase strategyDatabase,
            MonsterStrategyService strategyService,
            AccountScannerService accountScanner,
            Consumer<BossData> openGearOptimizerCallback,
            Consumer<BossData> openInventoryCallback) {

        this.bossDatabase = bossDatabase != null ? bossDatabase : new BossDatabase();
        this.monsterDatabase = monsterDatabase != null ? monsterDatabase : new MonsterDatabase();
        this.strategyDatabase = strategyDatabase != null ? strategyDatabase : new MonsterStrategyDatabase(this.monsterDatabase, this.bossDatabase);
        this.strategyService = strategyService;
        this.accountScanner = accountScanner;
        this.openGearOptimizerCallback = openGearOptimizerCallback;
        this.openInventoryCallback = openInventoryCallback;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        // Header with Search and Selectors
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BossWiseDesign.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        // Row 1: Search Field
        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.setOpaque(false);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(BossWiseDesign.getSmallFont());
        searchIcon.setForeground(BossWiseDesign.GOLD);
        searchRow.add(searchIcon, BorderLayout.WEST);

        searchField.setFont(BossWiseDesign.getSmallFont());
        searchField.setBackground(BossWiseDesign.CARD_BG);
        searchField.setForeground(BossWiseDesign.TEXT_PRIMARY);
        searchField.setCaretColor(BossWiseDesign.GOLD);
        searchField.setToolTipText("Filter monsters by name or weakness");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshMonsterList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshMonsterList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshMonsterList(); }
        });
        searchRow.add(searchField, BorderLayout.CENTER);
        header.add(searchRow);
        header.add(Box.createVerticalStrut(4));

        // Row 2: Category Filter + Target Dropdown
        JPanel selectRow = new JPanel(new GridLayout(1, 2, 4, 0));
        selectRow.setOpaque(false);

        categoryFilter.setFont(BossWiseDesign.getSmallFont());
        categoryFilter.setBackground(BossWiseDesign.CARD_BG);
        categoryFilter.setForeground(BossWiseDesign.TEXT_PRIMARY);
        categoryFilter.addActionListener(e -> refreshMonsterList());
        selectRow.add(categoryFilter);

        bossSelector.setFont(BossWiseDesign.getBoldFont());
        bossSelector.setBackground(BossWiseDesign.CARD_BG);
        bossSelector.setForeground(BossWiseDesign.GOLD);
        bossSelector.addActionListener(e -> {
            selectedMethodIndex = 0;
            renderGuide();
        });
        selectRow.add(bossSelector);
        header.add(selectRow);

        add(header, BorderLayout.NORTH);

        // Center Content Container
        contentContainer.setLeftAlignChildren(true);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBackground(BossWiseDesign.BG_DARK);
        contentContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(contentContainer);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        refreshMonsterList();
    }

    public void selectBoss(BossData boss) {
        if (boss != null) {
            bossSelector.setSelectedItem(boss.getName());
            selectedMethodIndex = 0;
            renderGuide();
        }
    }

    public void selectMonsterByName(String name) {
        if (name != null) {
            bossSelector.setSelectedItem(name);
            selectedMethodIndex = 0;
            renderGuide();
        }
    }

    private void refreshMonsterList() {
        String filterQuery = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        String catSelection = (String) categoryFilter.getSelectedItem();

        List<String> matchingNames = new ArrayList<>();

        for (MonsterStrategy strat : strategyDatabase.getAllStrategies().values()) {
            // Category check
            if (catSelection != null && !catSelection.equals("All Categories")) {
                if (catSelection.equals("Bosses") && strat.getCategory() != MonsterCategory.BOSS) continue;
                if (catSelection.equals("Slayer") && strat.getCategory() != MonsterCategory.SLAYER && strat.getCategory() != MonsterCategory.SUPERIOR) continue;
                if (catSelection.equals("Wilderness") && strat.getCategory() != MonsterCategory.WILDERNESS) continue;
                if (catSelection.equals("Raids") && strat.getCategory() != MonsterCategory.RAID) continue;
                if (catSelection.equals("Regular") && strat.getCategory() != MonsterCategory.REGULAR) continue;
            }

            // Query check
            if (!filterQuery.isEmpty()) {
                boolean matchName = strat.getMonsterName().toLowerCase().contains(filterQuery);
                boolean matchWeak = strat.getWeaknesses().stream().anyMatch(w -> w.toLowerCase().contains(filterQuery));
                if (!matchName && !matchWeak) continue;
            }

            matchingNames.add(strat.getMonsterName());
        }

        Collections.sort(matchingNames);

        String prevSelected = (String) bossSelector.getSelectedItem();
        bossSelector.removeAllItems();

        for (String name : matchingNames) {
            bossSelector.addItem(name);
        }

        if (prevSelected != null && matchingNames.contains(prevSelected)) {
            bossSelector.setSelectedItem(prevSelected);
        } else if (!matchingNames.isEmpty()) {
            bossSelector.setSelectedIndex(0);
        }

        renderGuide();
    }

    private void renderGuide() {
        contentContainer.removeAll();

        String selectedName = (String) bossSelector.getSelectedItem();
        if (selectedName == null || selectedName.isEmpty()) {
            contentContainer.add(new WrapText("No monsters match your current filter.", BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_MUTED));
            contentContainer.revalidate();
            contentContainer.repaint();
            return;
        }

        MonsterStrategy strategy = strategyDatabase.getStrategyByName(selectedName);
        if (strategy == null) {
            strategy = strategyDatabase.getAllStrategies().values().iterator().next();
        }

        PlayerAccount account = (accountScanner != null) ? accountScanner.getCurrentAccount() : PlayerAccount.createDefault();

        // 1. MONSTER HEADER CARD
        renderHeaderCard(strategy);

        // 2. PERSONALIZED ACCOUNT RECOMMENDATION
        renderPersonalizedCard(strategy, account);

        // 3. STRATEGY METHODS (TIER SELECTOR & METHOD CARD)
        renderMethodsCard(strategy, account);

        // 4. COMBAT MECHANICS & PHASES
        renderMechanicsCard(strategy);

        // 5. PROTECTION & SURVIVAL
        renderProtectionCard(strategy);

        // 6. SPECIAL EQUIPMENT & ADVANTAGES
        renderSpecialEquipmentCard(strategy);

        // 7. COMMON MISTAKES
        renderCommonMistakesCard(strategy);

        // 8. NOTABLE DROPS
        renderDropsCard(strategy);

        // 9. OSRS WIKI ATTRIBUTION & SOURCE LINK
        renderAttributionCard(strategy);

        contentContainer.revalidate();
        contentContainer.repaint();
    }

    private void renderHeaderCard(MonsterStrategy strat) {
        VerticalPanel card = newCard(BossWiseDesign.BORDER_CARD);

        card.add(new WrapText(strat.getMonsterName(), BossWiseDesign.getTitleFont(), BossWiseDesign.GOLD));
        card.add(Box.createVerticalStrut(2));

        card.add(text("● " + strat.getConfidence().getLabel(), BossWiseDesign.getBadgeFont(), Color.decode(strat.getConfidence().getColorHex())));
        card.add(Box.createVerticalStrut(3));

        // Subtitle Info Row
        StringBuilder info = new StringBuilder();
        info.append(strat.getCategory().getDisplayName());
        info.append(" \u2022 Cb ").append(strat.getCombatLevel());
        info.append(" \u2022 HP ").append(strat.getHitpoints());
        if (strat.getSlayerLevel() > 1) {
            info.append(" \u2022 Slayer ").append(strat.getSlayerLevel());
        }
        card.add(new WrapText(info.toString(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(4));

        // Overview
        if (strat.getOverview() != null && !strat.getOverview().isEmpty()) {
            card.add(new WrapText(strat.getOverview(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
            card.add(Box.createVerticalStrut(4));
        }

        // Weaknesses
        if (!strat.getWeaknesses().isEmpty()) {
            card.add(text("WEAKNESSES:", BossWiseDesign.getBadgeFont(), BossWiseDesign.STATUS_READY));
            for (String w : strat.getWeaknesses()) {
                card.add(new WrapText("• " + w, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
            }
            card.add(Box.createVerticalStrut(3));
        }

        // Location & Travel
        if (!strat.getLocations().isEmpty()) {
            card.add(text("LOCATION:", BossWiseDesign.getBadgeFont(), BossWiseDesign.TEXT_MUTED));
            for (String loc : strat.getLocations()) {
                card.add(new WrapText(loc, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
            }
        }

        addCard(card);
    }

    private void renderPersonalizedCard(MonsterStrategy strat, PlayerAccount account) {
        if (strategyService == null) return;

        List<PersonalizedStrategyScore> scored = strategyService.personalize(strat, account, 10_000_000L);
        if (scored.isEmpty()) return;

        VerticalPanel card = newSectionCard("ACCOUNT RECOMMENDATION");

        PersonalizedStrategyScore top = scored.get(0);
        for (PersonalizedStrategyScore s : scored) {
            if (s.isBestOverall()) {
                top = s;
                break;
            }
        }

        card.add(text("BEST METHOD FOR YOU:", BossWiseDesign.getBadgeFont(), BossWiseDesign.GOLD));
        card.add(Box.createVerticalStrut(2));
        card.add(new WrapText(top.getMethod().getTitle(), BossWiseDesign.getBoldFont(), BossWiseDesign.STATUS_READY));
        card.add(Box.createVerticalStrut(3));

        card.add(new WrapText(top.getRecommendationReason(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));

        if (!top.isMeetsAllRequirements() && !top.getMissingRequirements().isEmpty()) {
            card.add(Box.createVerticalStrut(3));
            card.add(text("MISSING REQUIREMENTS:", BossWiseDesign.getBadgeFont(), BossWiseDesign.STATUS_LOCKED));
            for (String req : top.getMissingRequirements()) {
                card.add(new WrapText("⚠ " + req, BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_LOCKED));
            }
        }

        addCard(card);
    }

    private void renderMethodsCard(MonsterStrategy strat, PlayerAccount account) {
        if (strat.getMethods().isEmpty()) return;

        VerticalPanel card = newSectionCard("FIGHTING STRATEGY & METHODS (" + strat.getMethods().size() + ")");

        // Method Selector Buttons (wrap layout so buttons wrap cleanly)
        JPanel buttonRow = new JPanel(new com.bosswise.ui.components.WrapLayout(FlowLayout.LEFT, 2, 2));
        buttonRow.setOpaque(false);

        for (int i = 0; i < strat.getMethods().size(); i++) {
            final int idx = i;
            StrategyMethod m = strat.getMethods().get(i);
            GoldButton btn = new GoldButton(m.getTier().getDisplayName());
            btn.setFont(BossWiseDesign.getBadgeFont());
            btn.setActive(idx == selectedMethodIndex);
            btn.addActionListener(e -> {
                selectedMethodIndex = idx;
                renderGuide();
            });
            buttonRow.add(btn);
        }
        card.add(buttonRow);
        card.add(Box.createVerticalStrut(4));

        if (selectedMethodIndex >= strat.getMethods().size()) {
            selectedMethodIndex = 0;
        }

        StrategyMethod currentMethod = strat.getMethods().get(selectedMethodIndex);

        // Method Details
        card.add(new WrapText(currentMethod.getTitle(), BossWiseDesign.getBoldFont(), BossWiseDesign.GOLD));
        card.add(text("Estimated Loadout Cost: ~" + GearOptimizerService.formatGp(currentMethod.getEstimatedCostGp()), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
        card.add(Box.createVerticalStrut(3));

        // Prayers
        addLabelledText(card, "Overhead Prayer", currentMethod.getOverheadPrayer(), BossWiseDesign.GOLD);
        addLabelledText(card, "Offensive Prayer", currentMethod.getOffensivePrayer(), BossWiseDesign.TEXT_PRIMARY);

        // Potions
        if (!currentMethod.getPotionSetup().isEmpty()) {
            addLabelledText(card, "Potions", String.join(", ", currentMethod.getPotionSetup()), BossWiseDesign.TEXT_SECONDARY);
        }

        // Why This Works
        if (currentMethod.getWhyThisWorks() != null && !currentMethod.getWhyThisWorks().isEmpty()) {
            card.add(Box.createVerticalStrut(3));
            card.add(text("WHY THIS WORKS:", BossWiseDesign.getBadgeFont(), BossWiseDesign.STATUS_READY));
            card.add(new WrapText(currentMethod.getWhyThisWorks(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
        }

        // Step by Step Instructions
        if (currentMethod.getDetailedInstructions() != null && !currentMethod.getDetailedInstructions().isEmpty()) {
            card.add(Box.createVerticalStrut(3));
            card.add(text("COMBAT INSTRUCTIONS:", BossWiseDesign.getBadgeFont(), BossWiseDesign.TEXT_MUTED));
            card.add(new WrapText(currentMethod.getDetailedInstructions(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
        }

        // Action Buttons: Load in Gear Optimizer / View Supplies (stacked vertically for full sidebar width)
        card.add(Box.createVerticalStrut(6));

        GoldButton loadGearBtn = new GoldButton("⚡ Load in Gear Optimizer");
        loadGearBtn.setFont(BossWiseDesign.getSmallFont());
        loadGearBtn.addActionListener(e -> {
            BossData boss = bossDatabase.getBoss(strat.getMonsterId());
            if (boss == null && monsterDatabase != null) {
                MonsterData m = monsterDatabase.getMonster(strat.getMonsterId());
                if (m != null) boss = m.toBossData();
            }
            if (boss != null && openGearOptimizerCallback != null) {
                openGearOptimizerCallback.accept(boss);
            }
        });
        card.add(loadGearBtn);
        card.add(Box.createVerticalStrut(4));

        GoldButton suppliesBtn = new GoldButton("🎒 View Supplies");
        suppliesBtn.setFont(BossWiseDesign.getSmallFont());
        suppliesBtn.addActionListener(e -> {
            BossData boss = bossDatabase.getBoss(strat.getMonsterId());
            if (boss == null && monsterDatabase != null) {
                MonsterData m = monsterDatabase.getMonster(strat.getMonsterId());
                if (m != null) boss = m.toBossData();
            }
            if (boss != null && openInventoryCallback != null) {
                openInventoryCallback.accept(boss);
            }
        });
        card.add(suppliesBtn);

        addCard(card);
    }

    private void renderMechanicsCard(MonsterStrategy strat) {
        if (strat.getCombatMechanics().isEmpty() && strat.getPhases().isEmpty()) return;

        VerticalPanel card = newSectionCard("KEY COMBAT MECHANICS & PHASES");

        for (StrategyCombatMechanic mech : strat.getCombatMechanics()) {
            card.add(text(mech.getName(), BossWiseDesign.getBoldFont(), BossWiseDesign.GOLD));
            card.add(new WrapText(mech.getDescription(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
            if (mech.getAvoidanceTip() != null && !mech.getAvoidanceTip().isEmpty()) {
                card.add(new WrapText("💡 Tip: " + mech.getAvoidanceTip(), BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_READY));
            }
            card.add(Box.createVerticalStrut(4));
        }

        if (!strat.getPhases().isEmpty()) {
            card.add(text("PHASES:", BossWiseDesign.getBadgeFont(), BossWiseDesign.TEXT_MUTED));
            for (String phase : strat.getPhases()) {
                card.add(new WrapText("• " + phase, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
            }
        }

        addCard(card);
    }

    private void renderProtectionCard(MonsterStrategy strat) {
        if (strat.getProtectionAndSurvival().isEmpty()) return;

        VerticalPanel card = newSectionCard("PROTECTION & SURVIVAL");
        for (String prot : strat.getProtectionAndSurvival()) {
            card.add(new WrapText("🛡 " + prot, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
            card.add(Box.createVerticalStrut(2));
        }

        addCard(card);
    }

    private void renderSpecialEquipmentCard(MonsterStrategy strat) {
        if (strat.getSpecialEquipmentAdvantage().isEmpty()) return;

        VerticalPanel card = newSectionCard("SPECIAL EQUIPMENT ADVANTAGES");
        for (String eq : strat.getSpecialEquipmentAdvantage()) {
            card.add(new WrapText("⚔ " + eq, BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_READY));
            card.add(Box.createVerticalStrut(2));
        }

        addCard(card);
    }

    private void renderCommonMistakesCard(MonsterStrategy strat) {
        if (strat.getCommonMistakes().isEmpty()) return;

        VerticalPanel card = newCard(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.STATUS_LOCKED, 1),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        card.add(text("COMMON MISTAKES TO AVOID", BossWiseDesign.getBoldFont(), BossWiseDesign.STATUS_LOCKED));
        card.add(Box.createVerticalStrut(4));

        for (String mistake : strat.getCommonMistakes()) {
            card.add(new WrapText("⚠ " + mistake, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_PRIMARY));
            card.add(Box.createVerticalStrut(2));
        }

        addCard(card);
    }

    private void renderDropsCard(MonsterStrategy strat) {
        if (strat.getNotableDrops().isEmpty()) return;

        VerticalPanel card = newSectionCard("NOTABLE DROPS & UNIQUES");
        for (String drop : strat.getNotableDrops()) {
            card.add(new WrapText("💰 " + drop, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY));
            card.add(Box.createVerticalStrut(2));
        }

        addCard(card);
    }

    private void renderAttributionCard(MonsterStrategy strat) {
        VerticalPanel card = newCard(BossWiseDesign.BORDER_CARD);

        card.add(text("OSRS WIKI REFERENCE", BossWiseDesign.getBadgeFont(), BossWiseDesign.GOLD));
        card.add(text("Verified: " + strat.getLastVerified(), BossWiseDesign.getBadgeFont(), BossWiseDesign.TEXT_MUTED));
        card.add(Box.createVerticalStrut(3));

        card.add(new WrapText("Content curated and verified against the Old School RuneScape Wiki (CC BY-SA 3.0).", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED));
        card.add(Box.createVerticalStrut(4));

        GoldButton wikiBtn = new GoldButton("🌐 Open Official OSRS Wiki Page ↗");
        wikiBtn.setFont(BossWiseDesign.getSmallFont());
        wikiBtn.addActionListener(e -> {
            String url = (strat.getWikiStrategyUrl() != null && !strat.getWikiStrategyUrl().isEmpty())
                    ? strat.getWikiStrategyUrl()
                    : strat.getWikiUrl();
            if (url != null && !url.isEmpty()) {
                LinkBrowser.browse(url);
            }
        });
        card.add(wikiBtn);

        addCard(card);
    }

    private void addCard(VerticalPanel card) {
        contentContainer.add(card);
        contentContainer.add(Box.createVerticalStrut(CARD_GAP));
    }

    private VerticalPanel newCard(Border border) {
        VerticalPanel p = new VerticalPanel();
        p.setBackground(BossWiseDesign.CARD_BG);
        p.setBorder(border);
        return p;
    }

    private VerticalPanel newSectionCard(String title) {
        VerticalPanel p = newCard(BossWiseDesign.BORDER_CARD);
        p.add(new WrapText(title, BossWiseDesign.getBadgeFont(), BossWiseDesign.GOLD));
        p.add(Box.createVerticalStrut(4));
        return p;
    }

    private JPanel newRow() {
        JPanel r = new JPanel(new BorderLayout(4, 0));
        r.setOpaque(false);
        return r;
    }

    private void addLabelledText(VerticalPanel card, String label, String value, Color color) {
        if (value == null || value.trim().isEmpty() || "None".equalsIgnoreCase(value.trim())) return;
        card.add(new WrapText(label + ": " + value, BossWiseDesign.getSmallFont(), color));
        card.add(Box.createVerticalStrut(2));
    }

    private WrapText text(String content, Font font, Color color) {
        return new WrapText(content, font, color);
    }
}
