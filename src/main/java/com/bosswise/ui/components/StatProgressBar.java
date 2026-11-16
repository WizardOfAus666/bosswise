package com.bosswise.ui.components;

import com.bosswise.ui.BossWiseDesign;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

public class StatProgressBar extends JComponent {
    private int current;
    private int target;
    private Color fillColor = BossWiseDesign.GOLD;

    public StatProgressBar(int current, int target) {
        this.current = current;
        this.target = Math.max(1, target);
        setPreferredSize(new Dimension(140, 20));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    public void setValues(int current, int target) {
        this.current = current;
        this.target = Math.max(1, target);
        repaint();
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Track background
        g2.setColor(BossWiseDesign.BORDER_COLOR);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Fill bar
        double pct = Math.min(1.0, Math.max(0.0, (double) current / target));
        int fillW = (int) Math.round((w - 1) * pct);

        if (fillW > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, fillW, h - 1, 10, 10);
        }

        // Outer border
        g2.setColor(BossWiseDesign.BORDER_SUBTLE);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Text centered
        g2.setFont(BossWiseDesign.getBadgeFont());
        g2.setColor(new Color(0xFF, 0xFD, 0xF5));
        String label = current + " / " + target + " (" + (int)(pct * 100) + "%)";
        int textW = g2.getFontMetrics().stringWidth(label);
        int textX = (w - textW) / 2;
        int textY = ((h - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent();
        g2.drawString(label, textX, textY);

        g2.dispose();
    }
}
