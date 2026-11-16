package com.bosswise.ui;

import com.bosswise.model.CombatStyle;
import com.bosswise.model.SlayerTaskInfo;
import com.bosswise.model.SlayerTaskInfo.TaskLocation;
import com.bosswise.service.SlayerTaskService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.StatusBadge;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class SlayerPanel extends JPanel {
    private final SlayerTaskService slayerTaskService;

    private final JComboBox<String> taskCombo = new JComboBox<>();
    private final JTextField remainingField = new JTextField("140", 4);
    private final JPanel contentPanel = new VerticalPanel();

    public SlayerPanel(SlayerTaskService slayerTaskService) {
        this.slayerTaskService = slayerTaskService;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BossWiseDesign.PANEL_BG);
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        JLabel title = new JLabel("SLAYER ASSISTANT");
        title.setFont(BossWiseDesign.getHeaderFont());
        title.setForeground(BossWiseDesign.GOLD);
        topContainer.add(title);

        WrapText sub = new WrapText("Task locations, gear, XP rates & superiors", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        topContainer.add(sub);
        topContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        // Task selector row
        List<String> tasks = slayerTaskService.getAllTaskNames();
        for (String t : tasks) {
            taskCombo.addItem(t);
        }
        taskCombo.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 26));
        taskCombo.setFont(BossWiseDesign.getRegularFont());
        taskCombo.setBackground(BossWiseDesign.CARD_BG);
        taskCombo.setForeground(BossWiseDesign.TEXT_PRIMARY);
        taskCombo.addActionListener(e -> renderTaskDetails());
        topContainer.add(taskCombo);
        topContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        // Remaining & Detect row
        JPanel countRow = new JPanel(new BorderLayout(4, 0));
        countRow.setOpaque(false);

        JPanel leftCount = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftCount.setOpaque(false);
        JLabel remLabel = new JLabel("Remaining:");
        remLabel.setFont(BossWiseDesign.getSmallFont());
        remLabel.setForeground(BossWiseDesign.TEXT_SECONDARY);
        leftCount.add(remLabel);

        remainingField.setFont(BossWiseDesign.getSmallFont());
        remainingField.setBackground(BossWiseDesign.CARD_BG);
        remainingField.setForeground(BossWiseDesign.GOLD);
        remainingField.setCaretColor(BossWiseDesign.GOLD);
        remainingField.addActionListener(e -> renderTaskDetails());
        leftCount.add(remainingField);
        countRow.add(leftCount, BorderLayout.WEST);

        GoldButton detectBtn = new GoldButton("Detect Task");
        detectBtn.setFont(BossWiseDesign.getBadgeFont());
        detectBtn.setPreferredSize(new Dimension(85, 22));
        detectBtn.addActionListener(e -> {
            String current = slayerTaskService.detectCurrentSlayerTask();
            int amount = slayerTaskService.detectRemainingKills();
            taskCombo.setSelectedItem(current);
            remainingField.setText(String.valueOf(amount));
            renderTaskDetails();
        });
        countRow.add(detectBtn, BorderLayout.EAST);

        topContainer.add(countRow);
        add(topContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        renderTaskDetails();
    }

    private void renderTaskDetails() {
        contentPanel.removeAll();

        String selectedTask = (String) taskCombo.getSelectedItem();
        if (selectedTask == null) selectedTask = "Gargoyles";

        SlayerTaskInfo task = slayerTaskService.getTask(selectedTask);
        if (task == null) {
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        int remaining = 140;
        try {
            remaining = Integer.parseInt(remainingField.getText().trim());
        } catch (NumberFormatException ignored) {}

        int estMinutes = task.estimateTaskDurationMinutes(remaining);

        // 1. Task Summary Card
        JPanel summaryCard = new JPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBackground(BossWiseDesign.CARD_BG);
        summaryCard.setBorder(BossWiseDesign.BORDER_GOLD);
        summaryCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tName = new JLabel(task.getTaskName() + " (Level " + task.getSlayerLevelReq() + ")");
        tName.setFont(BossWiseDesign.getHeaderFont());
        tName.setForeground(BossWiseDesign.GOLD);
        summaryCard.add(tName);
        summaryCard.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel rates = new JLabel(String.format("XP: %dK/hr  |  Profit: %dK/hr",
                task.getEstimatedXpPerHour() / 1000, task.getEstimatedGpPerHour() / 1000));
        rates.setFont(BossWiseDesign.getBoldFont());
        rates.setForeground(BossWiseDesign.TEXT_PRIMARY);
        summaryCard.add(rates);

        WrapText duration = new WrapText(String.format("Estimated Time: %d mins (%d kills remaining)", estMinutes, remaining), BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_READY);
        summaryCard.add(duration);

        WrapText styleRec = new WrapText("Style: " + task.getRecommendedStyle().name() + "  |  Weapon: " + task.getRecommendedWeapon(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        summaryCard.add(styleRec);

        contentPanel.add(summaryCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 2. Locations Comparison Card
        JPanel locCard = new JPanel();
        locCard.setLayout(new BoxLayout(locCard, BoxLayout.Y_AXIS));
        locCard.setBackground(BossWiseDesign.CARD_BG);
        locCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
        locCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lTitle = new JLabel("LOCATION COMPARISON");
        lTitle.setFont(BossWiseDesign.getBoldFont());
        lTitle.setForeground(BossWiseDesign.GOLD);
        locCard.add(lTitle);
        locCard.add(Box.createRigidArea(new Dimension(0, 4)));

        for (TaskLocation loc : task.getLocations()) {
            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setOpaque(false);
            item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_SUBTLE));

            JLabel name = new JLabel(loc.getName());
            name.setFont(BossWiseDesign.getBoldFont());
            name.setForeground(BossWiseDesign.TEXT_PRIMARY);
            item.add(name);

            String badges = (loc.isCannonable() ? "[Cannon] " : "") +
                    (loc.isMultiCombat() ? "[Multi] " : "[Single] ") +
                    (loc.isPrayerRestoreBonus() ? "[Prayer Restore] " : "") +
                    (loc.isSlayerXpBonus() ? "[+10% XP] " : "");
            JLabel b = new JLabel(badges);
            b.setFont(BossWiseDesign.getSmallFont());
            b.setForeground(BossWiseDesign.STATUS_INFO);
            item.add(b);

            JLabel desc = new JLabel("<html><body style='width: 170px;'>" + loc.getDescription() + "</body></html>");
            desc.setFont(BossWiseDesign.getSmallFont());
            desc.setForeground(BossWiseDesign.TEXT_MUTED);
            item.add(desc);
            item.add(Box.createRigidArea(new Dimension(0, 4)));

            locCard.add(item);
        }
        contentPanel.add(locCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 3. Superior Slayer Monster Card
        JPanel supCard = new JPanel();
        supCard.setLayout(new BoxLayout(supCard, BoxLayout.Y_AXIS));
        supCard.setBackground(BossWiseDesign.CARD_BG);
        supCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
        supCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sTitle = new JLabel("SUPERIOR SLAYER VARIANT");
        sTitle.setFont(BossWiseDesign.getBoldFont());
        sTitle.setForeground(new Color(0x9C, 0x27, 0xB0)); // Purple
        supCard.add(sTitle);
        supCard.add(Box.createRigidArea(new Dimension(0, 4)));

        WrapText sName = new WrapText(task.getSuperiorVariantName(), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
        supCard.add(sName);

        JLabel drops = new JLabel("<html><body style='width: 170px;'>Drops: " + task.getSuperiorUniqueDrops() + "</body></html>");
        drops.setFont(BossWiseDesign.getSmallFont());
        drops.setForeground(BossWiseDesign.STATUS_READY);
        supCard.add(drops);

        contentPanel.add(supCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 4. Strategy & Warning Card
        JPanel stratCard = new JPanel();
        stratCard.setLayout(new BoxLayout(stratCard, BoxLayout.Y_AXIS));
        stratCard.setBackground(BossWiseDesign.CARD_BG);
        stratCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
        stratCard.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 100));

        JLabel stTitle = new JLabel("TASK STRATEGY");
        stTitle.setFont(BossWiseDesign.getBoldFont());
        stTitle.setForeground(BossWiseDesign.GOLD);
        stratCard.add(stTitle);
        stratCard.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel notes = new JLabel("<html><body style='width: 170px;'>" + task.getStrategyNotes() + "</body></html>");
        notes.setFont(BossWiseDesign.getSmallFont());
        notes.setForeground(BossWiseDesign.TEXT_SECONDARY);
        stratCard.add(notes);

        contentPanel.add(stratCard);

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
