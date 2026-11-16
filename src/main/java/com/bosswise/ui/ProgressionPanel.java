package com.bosswise.ui;

import com.bosswise.model.CollectionLogEntry;
import com.bosswise.model.CombatAchievementTask;
import com.bosswise.model.CombatAchievementTask.CaTier;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.GearRoadmapStep;
import com.bosswise.model.PlayerAccount;
import com.bosswise.model.ProgressionGoal;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.CollectionLogService;
import com.bosswise.service.CombatAchievementService;
import com.bosswise.service.GearRoadmapService;
import com.bosswise.service.ProgressionService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.StatProgressBar;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ProgressionPanel extends JPanel {
    private final AccountScannerService accountScanner;
    private final ProgressionService progressionService;
    private final GearRoadmapService gearRoadmapService;
    private final CombatAchievementService combatAchievementService;
    private final CollectionLogService collectionLogService;
    private final CurrentGearScannerService currentGearScanner;

    private final ScrollablePanel goalsContainer = new ScrollablePanel();
    private final Set<String> markedComplete = new HashSet<>();

    private String currentSubTab = "UNLOCKS"; // UNLOCKS, GEAR, ACHIEVEMENTS, LOG
    private final List<GoldButton> subTabButtons = new ArrayList<>();

    public ProgressionPanel(AccountScannerService accountScanner, ProgressionService progressionService) {
        this(accountScanner, progressionService,
                new GearRoadmapService(new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GearOptimizerService(new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)), new com.bosswise.service.GePriceService(null)),
                new CombatAchievementService(null),
                new CollectionLogService(null),
                new CurrentGearScannerService(null, new com.bosswise.db.EquipmentDatabase(), new com.bosswise.service.GePriceService(null)));
    }

    public ProgressionPanel(
            AccountScannerService accountScanner,
            ProgressionService progressionService,
            GearRoadmapService gearRoadmapService,
            CombatAchievementService combatAchievementService,
            CollectionLogService collectionLogService,
            CurrentGearScannerService currentGearScanner) {

        this.accountScanner = accountScanner;
        this.progressionService = progressionService;
        this.gearRoadmapService = gearRoadmapService;
        this.combatAchievementService = combatAchievementService;
        this.collectionLogService = collectionLogService;
        this.currentGearScanner = currentGearScanner;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BossWiseDesign.PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        WrapText title = new WrapText("PROGRESSION & GOALS", BossWiseDesign.getHeaderFont(), BossWiseDesign.GOLD);
        header.add(title);

        WrapText sub = new WrapText("Roadmaps, achievements, wishlist & log", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        header.add(sub);
        header.add(Box.createRigidArea(new Dimension(0, 6)));

        // Sub-tabs row
        JPanel tabRow = new JPanel(new GridLayout(1, 4, 2, 0));
        tabRow.setOpaque(false);
        addSubTabBtn(tabRow, "Unlocks", "UNLOCKS");
        addSubTabBtn(tabRow, "Gear", "GEAR");
        addSubTabBtn(tabRow, "CA Tiers", "ACHIEVEMENTS");
        addSubTabBtn(tabRow, "Coll Log", "LOG");
        header.add(tabRow);

        add(header, BorderLayout.NORTH);

        goalsContainer.setLeftAlignChildren(true);
        goalsContainer.setLayout(new BoxLayout(goalsContainer, BoxLayout.Y_AXIS));
        goalsContainer.setBackground(BossWiseDesign.BG_DARK);
        goalsContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(goalsContainer);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        refreshRoadmap();
    }

    private void addSubTabBtn(JPanel parent, String label, String tabKey) {
        GoldButton btn = new GoldButton(label);
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(50, 22));
        btn.setMargin(new java.awt.Insets(1, 1, 1, 1));
        btn.setActive(this.currentSubTab.equalsIgnoreCase(tabKey));
        btn.addActionListener(e -> {
            this.currentSubTab = tabKey;
            for (GoldButton b : subTabButtons) {
                b.setActive(b == btn);
            }
            refreshRoadmap();
        });
        subTabButtons.add(btn);
        parent.add(btn);
    }

    public void refreshRoadmap() {
        goalsContainer.removeAll();

        PlayerAccount account = (accountScanner != null) ? accountScanner.getCurrentAccount() : PlayerAccount.createDefault();
        if (account == null) account = PlayerAccount.createDefault();

        switch (currentSubTab.toUpperCase()) {
            case "GEAR":
                renderGearRoadmap(account);
                break;
            case "ACHIEVEMENTS":
                renderCombatAchievements();
                break;
            case "LOG":
                renderCollectionLog();
                break;
            case "UNLOCKS":
            default:
                renderBossUnlocks(account);
                break;
        }

        goalsContainer.revalidate();
        goalsContainer.repaint();
    }

    private void renderBossUnlocks(PlayerAccount account) {
        List<ProgressionGoal> goals = progressionService.generateRoadmap(account);
        if (goals.isEmpty()) {
            JLabel empty = new JLabel("All major boss prerequisites completed!", JLabel.CENTER);
            empty.setFont(BossWiseDesign.getHeaderFont());
            empty.setForeground(BossWiseDesign.STATUS_READY);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            goalsContainer.add(empty);
            return;
        }

        int index = 1;
        for (ProgressionGoal goal : goals) {
            VerticalPanel card = new VerticalPanel();
            card.setBackground(BossWiseDesign.CARD_BG);
            card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setOpaque(false);
            WrapText numLbl = new WrapText(index++ + ". " + goal.getTitle(), BossWiseDesign.getBoldFont(), markedComplete.contains(goal.getId()) ? BossWiseDesign.TEXT_MUTED : BossWiseDesign.TEXT_PRIMARY);
            top.add(numLbl, BorderLayout.CENTER);

            JCheckBox check = new JCheckBox();
            check.setOpaque(false);
            check.setSelected(markedComplete.contains(goal.getId()));
            check.addActionListener(e -> {
                if (check.isSelected()) markedComplete.add(goal.getId());
                else markedComplete.remove(goal.getId());
                refreshRoadmap();
            });
            top.add(check, BorderLayout.EAST);
            card.add(top);
            card.add(Box.createVerticalStrut(2));

            WrapText descLbl = new WrapText(goal.getDescription(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            card.add(descLbl);
            card.add(Box.createVerticalStrut(4));

            StatProgressBar bar = new StatProgressBar(goal.getCurrentProgress(), goal.getTargetProgress());
            if (markedComplete.contains(goal.getId())) {
                bar.setFillColor(BossWiseDesign.STATUS_READY);
            }
            card.add(bar);

            goalsContainer.add(card);
            goalsContainer.add(Box.createVerticalStrut(6));
        }
    }

    private void renderGearRoadmap(PlayerAccount account) {
        // Savings Goal Progress Card
        if (gearRoadmapService != null) {
            VerticalPanel goalCard = new VerticalPanel();
            goalCard.setBackground(BossWiseDesign.CARD_BG);
            goalCard.setBorder(BossWiseDesign.BORDER_GOLD);

            JLabel gTitle = new JLabel("SAVINGS GOAL: " + gearRoadmapService.getSavingsGoalTargetName().toUpperCase());
            gTitle.setFont(BossWiseDesign.getBoldFont());
            gTitle.setForeground(BossWiseDesign.GOLD);
            goalCard.add(gTitle);
            goalCard.add(Box.createVerticalStrut(2));

            long goalGp = gearRoadmapService.getSavingsGoalGp();
            long currentGp = 6_500_000L; // sample account balance or configurable
            double prog = gearRoadmapService.getSavingsProgress(currentGp);
            JLabel progLbl = new JLabel(String.format("Saved: %s / %s GP (%.0f%%)",
                    formatGp(currentGp), formatGp(goalGp), prog * 100.0));
            progLbl.setFont(BossWiseDesign.getSmallFont());
            progLbl.setForeground(BossWiseDesign.STATUS_READY);
            goalCard.add(progLbl);
            goalCard.add(Box.createVerticalStrut(4));

            StatProgressBar bar = new StatProgressBar((int) (currentGp / 1000), (int) (goalGp / 1000));
            bar.setFillColor(BossWiseDesign.GOLD);
            goalCard.add(bar);

            goalsContainer.add(goalCard);
            goalsContainer.add(Box.createVerticalStrut(6));

            // Ranked Gear Upgrade Steps (Feature 9)
            Map<com.bosswise.model.EquipmentSlot, EquipmentItem> curGear = (currentGearScanner != null)
                    ? currentGearScanner.getCurrentEquipment()
                    : Collections.emptyMap();

            List<GearRoadmapStep> steps = gearRoadmapService.generateRoadmap(account, curGear, CombatStyle.MELEE, null);
            for (GearRoadmapStep step : steps) {
                VerticalPanel stepCard = new VerticalPanel();
                stepCard.setBackground(BossWiseDesign.CARD_BG);
                stepCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

                JPanel top = new JPanel(new BorderLayout(4, 0));
                top.setOpaque(false);
                JLabel rankLbl = new JLabel("#" + step.getPriorityRank() + " " + step.getSlot().getDisplayName().toUpperCase());
                rankLbl.setFont(BossWiseDesign.getBadgeFont());
                rankLbl.setForeground(BossWiseDesign.GOLD);
                top.add(rankLbl, BorderLayout.WEST);

                JLabel cost = new JLabel("+" + step.getFormattedUpgradeCost());
                cost.setFont(BossWiseDesign.getBoldFont());
                cost.setForeground(step.isCanEquipNow() ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_ALMOST);
                top.add(cost, BorderLayout.EAST);
                stepCard.add(top);
                stepCard.add(Box.createVerticalStrut(2));

                WrapText title = new WrapText(step.getCurrentItem().getName() + " \u2192 " + step.getSuggestedItem().getName(),
                        BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
                stepCard.add(title);
                stepCard.add(Box.createVerticalStrut(2));

                WrapText reason = new WrapText(step.getReason(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
                stepCard.add(reason);

                if (!step.getRequirements().isEmpty()) {
                    WrapText reqs = new WrapText("Requires: " + String.join(", ", step.getRequirements()),
                            BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_LOCKED);
                    stepCard.add(reqs);
                }

                goalsContainer.add(stepCard);
                goalsContainer.add(Box.createVerticalStrut(6));
            }
        }
    }

    private void renderCombatAchievements() {
        if (combatAchievementService == null) return;

        // Tiers Progress Overview Card
        VerticalPanel progCard = new VerticalPanel();
        progCard.setBackground(BossWiseDesign.CARD_BG);
        progCard.setBorder(BossWiseDesign.BORDER_GOLD);

        JLabel caTitle = new JLabel("COMBAT ACHIEVEMENTS: " + String.format("%.0f%% COMPLETE", combatAchievementService.getTotalCompletionPercent()));
        caTitle.setFont(BossWiseDesign.getBoldFont());
        caTitle.setForeground(BossWiseDesign.GOLD);
        progCard.add(caTitle);
        progCard.add(Box.createVerticalStrut(4));

        for (CaTier tier : CaTier.values()) {
            double pct = combatAchievementService.getTierCompletionPercent(tier);
            JLabel tLbl = new JLabel(String.format("%s: %.0f%%", tier.getDisplayName(), pct));
            tLbl.setFont(BossWiseDesign.getSmallFont());
            tLbl.setForeground(Color.decode(tier.getHexColor()));
            progCard.add(tLbl);
        }
        goalsContainer.add(progCard);
        goalsContainer.add(Box.createVerticalStrut(6));

        // Task List
        for (CombatAchievementTask task : combatAchievementService.getAllTasks()) {
            VerticalPanel card = new VerticalPanel();
            card.setBackground(BossWiseDesign.CARD_BG);
            card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setOpaque(false);
            JLabel name = new JLabel(task.getBossName() + ": " + task.getTaskName());
            name.setFont(BossWiseDesign.getBoldFont());
            name.setForeground(Color.decode(task.getTier().getHexColor()));
            top.add(name, BorderLayout.CENTER);

            JCheckBox chk = new JCheckBox();
            chk.setOpaque(false);
            chk.setSelected(task.isCompleted());
            chk.addActionListener(e -> {
                combatAchievementService.setTaskCompleted(task.getTaskName(), chk.isSelected());
                refreshRoadmap();
            });
            top.add(chk, BorderLayout.EAST);
            card.add(top);
            card.add(Box.createVerticalStrut(2));

            WrapText desc = new WrapText(task.getDescription(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            card.add(desc);

            if (!task.getStrategyTip().isEmpty()) {
                WrapText tip = new WrapText("Tip: " + task.getStrategyTip(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
                card.add(tip);
            }

            goalsContainer.add(card);
            goalsContainer.add(Box.createVerticalStrut(4));
        }
    }

    private void renderCollectionLog() {
        if (collectionLogService == null) return;

        VerticalPanel logCard = new VerticalPanel();
        logCard.setBackground(BossWiseDesign.CARD_BG);
        logCard.setBorder(BossWiseDesign.BORDER_GOLD);

        JLabel lTitle = new JLabel("COLLECTION LOG: " + String.format("%.0f%% COMPLETED", collectionLogService.getTotalCompletionPercent()));
        lTitle.setFont(BossWiseDesign.getBoldFont());
        lTitle.setForeground(BossWiseDesign.GOLD);
        logCard.add(lTitle);
        logCard.add(Box.createVerticalStrut(2));

        JLabel count = new JLabel(collectionLogService.getTotalObtainedCount() + " / " + collectionLogService.getAllEntries().size() + " Uniques Unlocked");
        count.setFont(BossWiseDesign.getSmallFont());
        count.setForeground(BossWiseDesign.STATUS_READY);
        logCard.add(count);

        goalsContainer.add(logCard);
        goalsContainer.add(Box.createVerticalStrut(6));

        for (CollectionLogEntry entry : collectionLogService.getAllEntries()) {
            JPanel card = new JPanel(new BorderLayout(4, 0));
            card.setBackground(BossWiseDesign.CARD_BG);
            card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
            card.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 42));

            JPanel left = new JPanel();
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setOpaque(false);

            JLabel name = new JLabel(entry.getItemName());
            name.setFont(BossWiseDesign.getBoldFont());
            name.setForeground(entry.isObtained() ? BossWiseDesign.STATUS_READY : BossWiseDesign.TEXT_PRIMARY);
            left.add(name);

            JLabel sub = new JLabel(entry.getEncounterName() + " (" + entry.getDropRate() + ")");
            sub.setFont(BossWiseDesign.getSmallFont());
            sub.setForeground(BossWiseDesign.TEXT_MUTED);
            left.add(sub);
            card.add(left, BorderLayout.CENTER);

            JCheckBox chk = new JCheckBox();
            chk.setOpaque(false);
            chk.setSelected(entry.isObtained());
            chk.addActionListener(e -> {
                collectionLogService.setItemObtained(entry.getItemName(), chk.isSelected());
                refreshRoadmap();
            });
            card.add(chk, BorderLayout.EAST);

            goalsContainer.add(card);
            goalsContainer.add(Box.createVerticalStrut(4));
        }
    }

    private String formatGp(long amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%dK", amount / 1_000);
        return String.valueOf(amount);
    }
}
