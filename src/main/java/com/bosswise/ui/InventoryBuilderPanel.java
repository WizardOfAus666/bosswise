package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossData;
import com.bosswise.model.InventoryItem;
import com.bosswise.model.InventorySetup;
import com.bosswise.model.TripMetrics;
import com.bosswise.service.ExportService;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class InventoryBuilderPanel extends JPanel {
    private final BossDatabase bossDatabase;
    private final InventoryBuilderService inventoryService;
    private final ExportService exportService;

    private final JComboBox<String> bossSelector = new JComboBox<>();
    private final JComboBox<String> tierSelector = new JComboBox<>();
    private final ScrollablePanel itemsContainer = new ScrollablePanel();

    private final JLabel slotLabel = new JLabel("Slots: 28/28");
    private final JLabel tripStatsLabel = new JLabel("Trip: ~5 Kills | 15 Mins");
    private final JLabel totalCostLabel = new JLabel("0 GP");

    private InventorySetup currentSetup;

    public InventoryBuilderPanel(BossDatabase bossDatabase, InventoryBuilderService inventoryService) {
        this(bossDatabase, inventoryService, new ExportService());
    }

    public InventoryBuilderPanel(
            BossDatabase bossDatabase,
            InventoryBuilderService inventoryService,
            ExportService exportService) {

        this.bossDatabase = bossDatabase;
        this.inventoryService = inventoryService;
        this.exportService = exportService;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        for (BossData b : bossDatabase.getAllBosses().values()) {
            bossSelector.addItem(b.getName());
        }

        for (String tier : inventoryService.getAvailableTiers()) {
            tierSelector.addItem(tier);
        }
        tierSelector.setSelectedItem("Standard");

        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBackground(BossWiseDesign.PANEL_BG);
        controls.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel selRow = new JPanel(new GridLayout(1, 2, 4, 0));
        selRow.setOpaque(false);
        styleCombo(bossSelector);
        styleCombo(tierSelector);
        bossSelector.addActionListener(e -> renderInventory());
        tierSelector.addActionListener(e -> renderInventory());
        selRow.add(bossSelector);
        selRow.add(tierSelector);
        controls.add(selRow);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Slot status & Export button row
        JPanel slotRow = new JPanel(new BorderLayout(4, 0));
        slotRow.setOpaque(false);

        slotLabel.setFont(BossWiseDesign.getBoldFont());
        slotLabel.setForeground(BossWiseDesign.STATUS_READY);
        slotRow.add(slotLabel, BorderLayout.WEST);

        GoldButton exportBtn = new GoldButton("Export JSON");
        exportBtn.setFont(BossWiseDesign.getBadgeFont());
        exportBtn.setPreferredSize(new Dimension(80, 20));
        exportBtn.addActionListener(e -> {
            if (currentSetup != null && exportService != null) {
                exportService.copyToClipboard(exportService.exportInventoryAsJson(currentSetup));
                exportBtn.setText("Copied!");
                new javax.swing.Timer(1500, evt -> exportBtn.setText("Export JSON")).start();
            }
        });
        slotRow.add(exportBtn, BorderLayout.EAST);
        controls.add(slotRow);
        controls.add(Box.createRigidArea(new Dimension(0, 4)));

        // Trip Metrics Row
        JPanel tripRow = new JPanel(new BorderLayout(4, 0));
        tripRow.setOpaque(false);
        tripStatsLabel.setFont(BossWiseDesign.getSmallFont());
        tripStatsLabel.setForeground(BossWiseDesign.TEXT_SECONDARY);
        tripRow.add(tripStatsLabel, BorderLayout.WEST);

        totalCostLabel.setFont(BossWiseDesign.getBoldFont());
        totalCostLabel.setForeground(BossWiseDesign.GOLD);
        tripRow.add(totalCostLabel, BorderLayout.EAST);
        controls.add(tripRow);

        add(controls, BorderLayout.NORTH);

        itemsContainer.setLeftAlignChildren(true);
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(BossWiseDesign.BG_DARK);
        itemsContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane scroll = new JScrollPane(itemsContainer);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);

        renderInventory();
    }

    private void styleCombo(JComboBox<?> box) {
        box.setFont(BossWiseDesign.getSmallFont());
        box.setBackground(BossWiseDesign.CARD_BG);
        box.setForeground(BossWiseDesign.TEXT_PRIMARY);
    }

    public void selectBoss(BossData boss) {
        if (boss != null) {
            bossSelector.setSelectedItem(boss.getName());
            renderInventory();
        }
    }

    public void renderInventory() {
        itemsContainer.removeAll();

        String selectedBossName = (String) bossSelector.getSelectedItem();
        String selectedTier = (String) tierSelector.getSelectedItem();
        BossData boss = null;
        for (BossData b : bossDatabase.getAllBosses().values()) {
            if (b.getName().equalsIgnoreCase(selectedBossName)) {
                boss = b;
                break;
            }
        }

        currentSetup = inventoryService.getInventorySetup(boss, selectedTier);
        if (currentSetup == null) {
            itemsContainer.revalidate();
            itemsContainer.repaint();
            return;
        }

        // Metrics update
        int usedSlots = currentSetup.getUsedSlots();
        boolean valid = currentSetup.isValid();
        slotLabel.setText(String.format("Slots: %d / 28 %s", usedSlots, valid ? "[VALID]" : "[EXCEEDED]"));
        slotLabel.setForeground(valid ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);

        int bossHp = (boss != null) ? boss.getHitpoints() : 750;
        int bossMaxHit = (boss != null) ? boss.getMaxHit() : 30;
        TripMetrics metrics = currentSetup.calculateTripMetrics(bossHp, 6.0, bossMaxHit);

        tripStatsLabel.setText(String.format("~%d Kills/Trip | %d Mins | +%d HP",
                metrics.getEstimatedKills(), metrics.getTripDurationMinutes(), currentSetup.getTotalHealAmount()));
        totalCostLabel.setText(formatGp(currentSetup.getTotalCost()) + " GP");

        List<InventoryItem> items = currentSetup.getItems();
        for (InventoryItem item : items) {
            JPanel card = new JPanel(new BorderLayout(4, 0));
            card.setBackground(BossWiseDesign.CARD_BG);
            card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);

            VerticalPanel left = new VerticalPanel();
            left.setOpaque(false);

            WrapText name = new WrapText(item.getName(), BossWiseDesign.getBoldFont(),
                    item.isMandatory() ? BossWiseDesign.GOLD : BossWiseDesign.TEXT_PRIMARY);
            left.add(name);

            WrapText sub = new WrapText(item.getCategory() + " (" + formatGp(item.getTotalCost()) + " GP)",
                    BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
            left.add(sub);
            card.add(left, BorderLayout.CENTER);

            // Quantity adjust buttons (- / qty / +)
            JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            qtyPanel.setOpaque(false);

            GoldButton minus = new GoldButton("-");
            minus.setPreferredSize(new Dimension(20, 20));
            minus.setFont(BossWiseDesign.getBadgeFont());
            minus.addActionListener(e -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    renderInventory();
                }
            });
            qtyPanel.add(minus);

            JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
            qtyLabel.setFont(BossWiseDesign.getBoldFont());
            qtyLabel.setForeground(BossWiseDesign.TEXT_PRIMARY);
            qtyLabel.setPreferredSize(new Dimension(22, 20));
            qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            qtyPanel.add(qtyLabel);

            GoldButton plus = new GoldButton("+");
            plus.setPreferredSize(new Dimension(20, 20));
            plus.setFont(BossWiseDesign.getBadgeFont());
            plus.addActionListener(e -> {
                item.setQuantity(item.getQuantity() + 1);
                renderInventory();
            });
            qtyPanel.add(plus);

            card.add(qtyPanel, BorderLayout.EAST);

            itemsContainer.add(card);
            itemsContainer.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        itemsContainer.revalidate();
        itemsContainer.repaint();
    }

    private String formatGp(long amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%dK", amount / 1_000);
        return String.valueOf(amount);
    }
}
