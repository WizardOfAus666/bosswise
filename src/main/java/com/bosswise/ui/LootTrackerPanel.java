package com.bosswise.ui;

import com.bosswise.model.DropRateCalculation;
import com.bosswise.model.LootDropItem;
import com.bosswise.model.LootSession;
import com.bosswise.service.DropRateCalculatorService;
import com.bosswise.service.DropRateCalculatorService.PresetDrop;
import com.bosswise.service.LootTrackerService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class LootTrackerPanel extends JPanel {
    private final LootTrackerService lootTrackerService;
    private final DropRateCalculatorService dropRateCalculatorService;

    private final JPanel contentPanel = new VerticalPanel();
    private final JComboBox<PresetDrop> dropCombo = new JComboBox<>();

    public LootTrackerPanel(
            LootTrackerService lootTrackerService,
            DropRateCalculatorService dropRateCalculatorService) {
        this.lootTrackerService = lootTrackerService;
        this.dropRateCalculatorService = dropRateCalculatorService;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BossWiseDesign.PANEL_BG);
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        WrapText title = new WrapText("LOOT & PROFIT TRACKER", BossWiseDesign.getHeaderFont(), BossWiseDesign.GOLD);
        topContainer.add(title);

        WrapText sub = new WrapText("Live drops, supply deduction & drop rates", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        topContainer.add(sub);
        topContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        // Session Control Buttons
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 4, 0));
        actionRow.setOpaque(false);

        GoldButton resetBtn = new GoldButton("Reset Session");
        resetBtn.setFont(BossWiseDesign.getBadgeFont());
        resetBtn.setPreferredSize(new Dimension(98, 22));
        resetBtn.addActionListener(e -> {
            lootTrackerService.resetCurrentSession();
            renderAll();
        });
        actionRow.add(resetBtn);

        GoldButton newSessionBtn = new GoldButton("New Session");
        newSessionBtn.setFont(BossWiseDesign.getBadgeFont());
        newSessionBtn.setPreferredSize(new Dimension(98, 22));
        newSessionBtn.addActionListener(e -> {
            lootTrackerService.startNewSession("Boss Encounter");
            renderAll();
        });
        actionRow.add(newSessionBtn);

        topContainer.add(actionRow);
        add(topContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Reactive listener on new loot
        lootTrackerService.addSessionListener(session -> {
            SwingUtilities.invokeLater(this::renderAll);
        });

        renderAll();
    }

    public void renderAll() {
        contentPanel.removeAll();
        LootSession session = lootTrackerService.getActiveSession();

        // 1. Live Session Stats Card
        VerticalPanel statsCard = new VerticalPanel();
        statsCard.setBackground(BossWiseDesign.CARD_BG);
        statsCard.setBorder(BossWiseDesign.BORDER_GOLD);

        WrapText sTitle = new WrapText("ACTIVE SESSION: " + session.getMonsterName().toUpperCase(), BossWiseDesign.getBoldFont(), BossWiseDesign.GOLD);
        statsCard.add(sTitle);
        statsCard.add(Box.createRigidArea(new Dimension(0, 4)));

        long netProfit = session.getNetProfit();
        JLabel profitLabel = new JLabel("Net Profit: " + formatGp(netProfit) + " GP");
        profitLabel.setFont(BossWiseDesign.getTitleFont());
        profitLabel.setForeground(netProfit >= 0 ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);
        statsCard.add(profitLabel);

        WrapText killsTime = new WrapText(String.format("Kills: %d  |  Duration: %s",
                session.getKills(), session.getFormattedDuration()), BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_PRIMARY);
        statsCard.add(killsTime);

        WrapText rates = new WrapText(String.format("Rate: %.1f Kills/hr  |  Profit: %s/hr",
                session.getKillsPerHour(), formatGp(session.getProfitPerHour())), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        statsCard.add(rates);

        WrapText costs = new WrapText(String.format("Gross Loot: %s  |  Supplies: -%s",
                formatGp(session.getTotalLootValue()), formatGp(session.getEstimatedSupplyCosts())), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        statsCard.add(costs);

        contentPanel.add(statsCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 2. Drop Rarity Badges
        JPanel rarityRow = new JPanel(new GridLayout(1, 4, 2, 0));
        rarityRow.setOpaque(false);
        rarityRow.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 20));

        Map<LootDropItem.DropRarity, Integer> counts = session.getRarityCounts();
        addRarityBadge(rarityRow, "Unq: " + counts.get(LootDropItem.DropRarity.UNIQUE), new Color(0xFF, 0x98, 0x00));
        addRarityBadge(rarityRow, "Rare: " + counts.get(LootDropItem.DropRarity.RARE), new Color(0x9C, 0x27, 0xB0));
        addRarityBadge(rarityRow, "Val: " + counts.get(LootDropItem.DropRarity.VALUABLE), new Color(0x4C, 0xAF, 0x50));
        addRarityBadge(rarityRow, "Com: " + counts.get(LootDropItem.DropRarity.COMMON), new Color(0xB0, 0xBE, 0xC5));

        contentPanel.add(rarityRow);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 3. Recent Drops List
        VerticalPanel dropsCard = new VerticalPanel();
        dropsCard.setBackground(BossWiseDesign.CARD_BG);
        dropsCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

        JLabel dTitle = new JLabel("RECORDED DROPS (" + session.getDrops().size() + ")");
        dTitle.setFont(BossWiseDesign.getBoldFont());
        dTitle.setForeground(BossWiseDesign.GOLD);
        dropsCard.add(dTitle);
        dropsCard.add(Box.createRigidArea(new Dimension(0, 4)));

        List<LootDropItem> drops = session.getDrops();
        if (drops.isEmpty()) {
            WrapText empty = new WrapText("Kill monsters in-game to track drops in real time.", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
            dropsCard.add(empty);
        } else {
            int displayCount = Math.min(10, drops.size());
            for (int i = drops.size() - 1; i >= drops.size() - displayCount; i--) {
                LootDropItem item = drops.get(i);
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_SUBTLE));

                WrapText name = new WrapText(item.getQuantity() + "x " + item.getName(), BossWiseDesign.getSmallFont(), Color.decode(item.getRarity().getHexColor()));
                row.add(name, BorderLayout.CENTER);

                JLabel val = new JLabel(item.getFormattedTotalValue());
                val.setFont(BossWiseDesign.getSmallFont());
                val.setForeground(BossWiseDesign.TEXT_PRIMARY);
                row.add(val, BorderLayout.EAST);

                dropsCard.add(row);
            }
        }
        contentPanel.add(dropsCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 4. Drop Rate & Dry Streak Calculator (Feature 7)
        renderDropRateCalculator();

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addRarityBadge(JPanel parent, String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(BossWiseDesign.getBadgeFont());
        l.setForeground(color);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(color, 1));
        parent.add(l);
    }

    private void renderDropRateCalculator() {
        VerticalPanel calcCard = new VerticalPanel();
        calcCard.setBackground(BossWiseDesign.CARD_BG);
        calcCard.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

        WrapText cTitle = new WrapText("DROP RATE & DRY CALCULATOR", BossWiseDesign.getBoldFont(), BossWiseDesign.GOLD);
        calcCard.add(cTitle);
        calcCard.add(Box.createRigidArea(new Dimension(0, 4)));

        if (dropCombo.getItemCount() == 0) {
            for (PresetDrop p : dropRateCalculatorService.getPresets()) {
                dropCombo.addItem(p);
            }
            dropCombo.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 24));
            dropCombo.setFont(BossWiseDesign.getSmallFont());
            dropCombo.setBackground(BossWiseDesign.PANEL_BG);
            dropCombo.setForeground(BossWiseDesign.TEXT_PRIMARY);
            dropCombo.addActionListener(e -> renderAll());
        }
        calcCard.add(dropCombo);
        calcCard.add(Box.createRigidArea(new Dimension(0, 6)));

        PresetDrop selected = (PresetDrop) dropCombo.getSelectedItem();
        DropRateCalculation calc = dropRateCalculatorService.calculatePreset(selected);

        WrapText prob = new WrapText(String.format("Base Rate: %s  |  Expected: %d KC",
                calc.getFormattedFraction(), calc.getExpectedKills()), BossWiseDesign.getBoldFont(), BossWiseDesign.TEXT_PRIMARY);
        calcCard.add(prob);

        WrapText median = new WrapText(String.format("50%% Chance (Median): %d KC", calc.getMedianKills()), BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_READY);
        calcCard.add(median);

        WrapText ninety = new WrapText(String.format("90%% Chance: %d KC  |  99%%: %d KC",
                calc.getNinetyPercentKills(), calc.getNinetyNinePercentKills()), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
        calcCard.add(ninety);
        calcCard.add(Box.createRigidArea(new Dimension(0, 4)));

        // Killcount probability table
        WrapText table = new WrapText(String.format("10 KC: %.1f%%  |  50 KC: %.1f%%  |  100 KC: %.1f%%",
                calc.getChanceAt10() * 100.0, calc.getChanceAt50() * 100.0, calc.getChanceAt100() * 100.0), BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_INFO);
        calcCard.add(table);

        if (!calc.getSpecialMechanicDescription().isEmpty()) {
            WrapText mech = new WrapText(calc.getSpecialMechanicDescription(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
            calcCard.add(mech);
        }

        contentPanel.add(calcCard);
    }

    private String formatGp(long amount) {
        if (amount >= 1_000_000) return String.format("%.2fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%dK", amount / 1_000);
        return String.valueOf(amount);
    }
}
