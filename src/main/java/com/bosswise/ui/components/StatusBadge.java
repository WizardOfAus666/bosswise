package com.bosswise.ui.components;

import com.bosswise.model.BossEligibilityStatus;
import com.bosswise.ui.BossWiseDesign;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class StatusBadge extends JLabel {
    private BossEligibilityStatus status;

    public StatusBadge(BossEligibilityStatus status) {
        super(status != null ? status.getLabel() : "Unknown", SwingConstants.CENTER);
        this.status = status;
        setFont(BossWiseDesign.getBadgeFont());
        setForeground(BossWiseDesign.TEXT_PRIMARY);
        setOpaque(false);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(46, d.width + 12), 20);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void setStatus(BossEligibilityStatus status) {
        this.status = status;
        setText(status != null ? status.getLabel() : "Unknown");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        java.awt.Color bg = (status != null) ? status.getPrimaryColor() : BossWiseDesign.BORDER_COLOR;
        g2.setColor(new java.awt.Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 160));
        g2.fillRoundRect(0, 0, width - 1, height - 1, 12, 12);

        // Border
        java.awt.Color border = (status != null) ? status.getAccentColor() : BossWiseDesign.BORDER_COLOR;
        g2.setColor(border);
        g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
