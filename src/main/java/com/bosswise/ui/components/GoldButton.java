package com.bosswise.ui.components;

import com.bosswise.ui.BossWiseDesign;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

public class GoldButton extends JButton {
    private boolean hover = false;
    private boolean pressed = false;
    private boolean active = false;

    public GoldButton(String text) {
        super(text);
        setFont(BossWiseDesign.getBoldFont());
        setForeground(BossWiseDesign.GOLD);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setMargin(new java.awt.Insets(1, 2, 1, 2));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(90, 28));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            setForeground(BossWiseDesign.TEXT_PRIMARY);
        } else {
            setForeground(BossWiseDesign.GOLD);
        }
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (active) {
            g2.setColor(BossWiseDesign.GOLD_BG);
        } else if (pressed) {
            g2.setColor(BossWiseDesign.CARD_BG);
        } else if (hover) {
            g2.setColor(BossWiseDesign.CARD_HOVER);
        } else {
            g2.setColor(BossWiseDesign.PANEL_BG);
        }
        g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);

        // Border
        if (active) {
            g2.setColor(BossWiseDesign.GOLD);
            g2.setStroke(new BasicStroke(1.5f));
        } else if (hover) {
            g2.setColor(BossWiseDesign.GOLD_HOVER);
            g2.setStroke(new BasicStroke(1.2f));
        } else {
            g2.setColor(BossWiseDesign.BORDER_COLOR);
            g2.setStroke(new BasicStroke(1.0f));
        }
        g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

        g2.dispose();
        super.paintComponent(g);
    }
}
