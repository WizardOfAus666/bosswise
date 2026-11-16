package com.bosswise.ui.components;

import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.ui.BossWiseDesign;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EquipmentSlotRow extends VerticalPanel {
    private final EquipmentSlot slot;
    private final EquipmentItem item;
    private final long price;
    private final String reason;

    public EquipmentSlotRow(EquipmentSlot slot, EquipmentItem item, long price, String reason, Consumer<EquipmentSlot> onUpgradeClick) {
        this(slot, item, price, reason, null, onUpgradeClick);
    }

    public EquipmentSlotRow(EquipmentSlot slot, EquipmentItem item, long price, String reason, List<String> missingRequirements, Consumer<EquipmentSlot> onUpgradeClick) {
        this.slot = slot;
        this.item = item;
        this.price = price;
        this.reason = reason;

        setBackground(BossWiseDesign.CARD_BG);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));

        // Line 1: Slot name (Left) and Price (Right)
        JPanel line1 = new JPanel(new BorderLayout(4, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        line1.setOpaque(false);
        line1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel slotLabel = new JLabel(slot.getDisplayName().toUpperCase());
        slotLabel.setFont(BossWiseDesign.getBadgeFont());
        slotLabel.setForeground(BossWiseDesign.GOLD);
        line1.add(slotLabel, BorderLayout.WEST);

        String priceStr;
        if (item == null) {
            priceStr = "-";
        } else if (item.isUntradeable()) {
            priceStr = "Free / Quest";
        } else {
            priceStr = GearOptimizerService.formatGp(price) + " GP";
        }

        JLabel priceLabel = new JLabel(priceStr);
        priceLabel.setFont(BossWiseDesign.getBadgeFont());
        priceLabel.setForeground(item != null && item.isUntradeable() ? BossWiseDesign.STATUS_READY : BossWiseDesign.GOLD);
        line1.add(priceLabel, BorderLayout.EAST);
        add(line1);
        add(Box.createVerticalStrut(2));

        // Line 2: Item Name (snug, wrapping, never clipped)
        String itemName = (item != null) ? item.getName() : "None / Empty";
        WrapText nameText = new WrapText(itemName, BossWiseDesign.getBoldFont(), (item != null) ? BossWiseDesign.TEXT_PRIMARY : BossWiseDesign.TEXT_MUTED);
        nameText.setToolTipText(itemName);
        add(nameText);

        // Missing Requirements (if any)
        if (missingRequirements != null && !missingRequirements.isEmpty()) {
            add(Box.createVerticalStrut(2));
            for (String req : missingRequirements) {
                WrapText reqText = new WrapText("⚠ " + req, BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_LOCKED);
                add(reqText);
            }
        }

        // Line 3: Reason / Stats if present (snug, wrapping, never clipped)
        if (reason != null && !reason.trim().isEmpty()) {
            add(Box.createVerticalStrut(2));
            WrapText reasonText = new WrapText(reason.trim(), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            add(reasonText);
        }
    }
}
