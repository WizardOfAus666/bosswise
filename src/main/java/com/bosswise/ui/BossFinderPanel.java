package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossCategory;
import com.bosswise.model.BossData;
import com.bosswise.model.BossEligibility;
import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.model.MonsterCategory;
import com.bosswise.model.MonsterData;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.BossRecommendationEngine.SortOrder;
import com.bosswise.ui.components.BossCard;
import com.bosswise.ui.components.ScrollablePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Universal Monster & Boss Finder Panel supporting all OSRS attackable monsters,
 * slayer creatures, superiors, wilderness NPCs, raids, and quest bosses.
 */
public class BossFinderPanel extends JPanel {
    private final AccountScannerService accountScanner;
    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final BossRecommendationEngine recommendationEngine;
    private final Consumer<BossData> onSelectBossForGear;
    private final Consumer<BossData> onSelectBossForGuide;

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All Status", "Ready", "Almost", "Locked"});
    private final JComboBox<String> categoryFilter = new JComboBox<>(new String[]{
            "All Types", "Bosses", "Slayer", "Superior", "Wilderness", "Raids", "Demi-Bosses", "Regular", "Quests"
    });
    private final JComboBox<SortOrder> sortSelector = new JComboBox<>(SortOrder.values());

    private final ScrollablePanel listContainer;

    public BossFinderPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            BossRecommendationEngine recommendationEngine,
            Consumer<BossData> onSelectBossForGear,
            Consumer<BossData> onSelectBossForGuide) {

        this(accountScanner, bossDatabase, new MonsterDatabase(), recommendationEngine, onSelectBossForGear, onSelectBossForGuide);
    }

    public BossFinderPanel(
            AccountScannerService accountScanner,
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            BossRecommendationEngine recommendationEngine,
            Consumer<BossData> onSelectBossForGear,
            Consumer<BossData> onSelectBossForGuide) {

        this.accountScanner = accountScanner;
        this.bossDatabase = bossDatabase;
        this.monsterDatabase = monsterDatabase;
        this.recommendationEngine = recommendationEngine;
        this.onSelectBossForGear = onSelectBossForGear;
        this.onSelectBossForGuide = onSelectBossForGuide;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        // Controls Panel at North
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(BossWiseDesign.PANEL_BG);
        controls.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Search Field
        searchField.setFont(BossWiseDesign.getRegularFont());
        searchField.setBackground(BossWiseDesign.CARD_BG);
        searchField.setForeground(BossWiseDesign.TEXT_PRIMARY);
        searchField.setCaretColor(BossWiseDesign.GOLD);
        searchField.setPreferredSize(new Dimension(100, 26));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        searchField.setToolTipText("Search monster name, drops, weakness, category...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshList(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshList(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshList(); }
        });

        JPanel searchWrap = new JPanel(new BorderLayout(6, 0));
        searchWrap.setOpaque(false);
        JLabel searchLbl = new JLabel("SEARCH:");
        searchLbl.setFont(BossWiseDesign.getBadgeFont());
        searchLbl.setForeground(BossWiseDesign.GOLD);
        searchWrap.add(searchLbl, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);
        controls.add(searchWrap);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Filter Rows
        JPanel filterRow = new JPanel(new GridLayout(1, 2, 4, 0));
        filterRow.setOpaque(false);
        styleCombo(statusFilter);
        styleCombo(categoryFilter);
        statusFilter.addActionListener(e -> refreshList());
        categoryFilter.addActionListener(e -> refreshList());
        filterRow.add(statusFilter);
        filterRow.add(categoryFilter);
        controls.add(filterRow);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Sort Row
        JPanel sortRow = new JPanel(new BorderLayout(6, 0));
        sortRow.setOpaque(false);
        JLabel sortLbl = new JLabel("SORT:");
        sortLbl.setFont(BossWiseDesign.getBadgeFont());
        sortLbl.setForeground(BossWiseDesign.GOLD);
        sortRow.add(sortLbl, BorderLayout.WEST);
        styleCombo(sortSelector);
        sortSelector.addActionListener(e -> refreshList());
        sortRow.add(sortSelector, BorderLayout.CENTER);
        controls.add(sortRow);

        add(controls, BorderLayout.NORTH);

        // List Container (ScrollablePanel ensures viewport width tracking)
        listContainer = new ScrollablePanel();
        listContainer.setLeftAlignChildren(true);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BossWiseDesign.BG_DARK);
        listContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        refreshList();
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(BossWiseDesign.getBoldFont());
        box.setBackground(BossWiseDesign.CARD_BG);
        box.setForeground(BossWiseDesign.TEXT_PRIMARY);
        box.setPreferredSize(new Dimension(80, 26));
        box.setBorder(BorderFactory.createLineBorder(BossWiseDesign.BORDER_COLOR, 1));
    }

    public void refreshList() {
        listContainer.removeAll();

        PlayerAccount rawAccount = accountScanner.getCurrentAccount();
        final PlayerAccount account = (rawAccount != null) ? rawAccount : PlayerAccount.createDefault();

        // Interpret status filter
        String statusStr = (String) statusFilter.getSelectedItem();
        BossEligibilityStatus filterStatus = null;
        if ("Ready".equalsIgnoreCase(statusStr)) filterStatus = BossEligibilityStatus.READY;
        else if ("Almost".equalsIgnoreCase(statusStr) || "Almost Ready".equalsIgnoreCase(statusStr)) filterStatus = BossEligibilityStatus.ALMOST_READY;
        else if ("Locked".equalsIgnoreCase(statusStr)) filterStatus = BossEligibilityStatus.LOCKED;

        String selectedCategory = (String) categoryFilter.getSelectedItem();
        SortOrder sortOrder = (SortOrder) sortSelector.getSelectedItem();
        String query = searchField.getText().trim().toLowerCase();

        // Gather all candidates (merging bossDatabase first for rich curated data, then monsterDatabase)
        Map<String, BossData> candidates = new LinkedHashMap<>();
        for (BossData b : bossDatabase.getAllBosses().values()) {
            candidates.put(b.getId(), b);
        }
        if (monsterDatabase != null) {
            for (Map.Entry<String, BossData> e : monsterDatabase.getAllBossesAsBossData().entrySet()) {
                candidates.putIfAbsent(e.getKey(), e.getValue());
            }
        }

        List<BossData> filtered = new ArrayList<>();
        for (BossData boss : candidates.values()) {
            // Category filter
            if (!matchesCategory(boss, selectedCategory)) {
                continue;
            }

            // Search query filter
            if (!query.isEmpty()) {
                boolean matchName = boss.getName().toLowerCase().contains(query);
                boolean matchDrop = boss.getNotableDrops().stream().anyMatch(d -> d.toLowerCase().contains(query));
                boolean matchWeak = boss.getWeakness().toLowerCase().contains(query);
                boolean matchCat = boss.getCategory().getDisplayName().toLowerCase().contains(query);
                if (!matchName && !matchDrop && !matchWeak && !matchCat) continue;
            }

            // Status filter
            BossEligibility el = recommendationEngine.evaluateEligibility(boss, account);
            if (filterStatus != null && el.getStatus() != filterStatus) {
                continue;
            }

            filtered.add(boss);
        }

        // Sort candidates
        if (sortOrder == SortOrder.EASIEST_FIRST) {
            filtered.sort(Comparator.comparing(BossData::getDifficulty));
        } else if (sortOrder == SortOrder.HIGHEST_GP) {
            filtered.sort((a, b) -> Long.compare(b.getEstimatedGpHour(), a.getEstimatedGpHour()));
        } else if (sortOrder == SortOrder.LOWEST_GEAR_COST) {
            filtered.sort((a, b) -> Long.compare(a.getSuggestedGearBudget(), b.getSuggestedGearBudget()));
        } else {
            // RECOMMENDED / BEST_SUITABILITY: Sort by eligibility recommendation score then profit
            filtered.sort((a, b) -> {
                BossEligibility elA = recommendationEngine.evaluateEligibility(a, account);
                BossEligibility elB = recommendationEngine.evaluateEligibility(b, account);
                int cmp = Integer.compare(elB.getRecommendationScore(), elA.getRecommendationScore());
                if (cmp != 0) return cmp;
                return Long.compare(b.getEstimatedGpHour(), a.getEstimatedGpHour());
            });
        }

        int count = 0;
        for (BossData boss : filtered) {
            BossEligibility el = recommendationEngine.evaluateEligibility(boss, account);
            BossCard card = new BossCard(boss, el, onSelectBossForGear, onSelectBossForGuide);
            listContainer.add(card);
            listContainer.add(Box.createRigidArea(new Dimension(0, 6)));
            count++;
        }

        if (count == 0) {
            JLabel empty = new JLabel("No monsters matched the selected criteria.", JLabel.CENTER);
            empty.setFont(BossWiseDesign.getSmallFont());
            empty.setForeground(BossWiseDesign.TEXT_MUTED);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listContainer.add(empty);
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private boolean matchesCategory(BossData boss, String category) {
        if (category == null || "All Types".equalsIgnoreCase(category)) {
            return true;
        }
        String catName = boss.getCategory().getDisplayName().toLowerCase();
        String catReq = category.toLowerCase();

        if ("bosses".equalsIgnoreCase(catReq)) {
            return boss.getCategory() == BossCategory.BEGINNER ||
                    boss.getCategory() == BossCategory.INTERMEDIATE ||
                    boss.getCategory() == BossCategory.ADVANCED ||
                    boss.getCategory() == BossCategory.ENDGAME ||
                    boss.getCategory() == BossCategory.GROUP;
        }
        if ("slayer".equalsIgnoreCase(catReq)) {
            return boss.getCategory() == BossCategory.SLAYER ||
                    boss.getName().toLowerCase().contains("slayer");
        }
        if ("superior".equalsIgnoreCase(catReq)) {
            return boss.getName().toLowerCase().contains("marble") ||
                    boss.getName().toLowerCase().contains("greater") ||
                    boss.getName().toLowerCase().contains("superior") ||
                    catName.contains("superior");
        }
        if ("wilderness".equalsIgnoreCase(catReq)) {
            return boss.getCategory() == BossCategory.WILDERNESS ||
                    (boss.getHowToGetThere() != null && boss.getHowToGetThere().toLowerCase().contains("wilderness"));
        }
        if ("raids".equalsIgnoreCase(catReq)) {
            return boss.getCategory() == BossCategory.RAID;
        }
        if ("demi-bosses".equalsIgnoreCase(catReq)) {
            return boss.getCategory() == BossCategory.DEMI_BOSS;
        }
        if ("regular".equalsIgnoreCase(catReq)) {
            return catName.contains("regular") || boss.getRecommendedCombatLevel() <= 60;
        }
        if ("quests".equalsIgnoreCase(catReq)) {
            return catName.contains("quest") || !boss.getQuestRequirements().isEmpty();
        }

        return catName.contains(catReq);
    }
}
