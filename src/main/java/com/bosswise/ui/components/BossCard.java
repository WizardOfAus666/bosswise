package com.bosswise.ui.components;

import com.bosswise.model.BossData;
import com.bosswise.model.BossEligibility;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.ui.BossWiseDesign;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BossCard extends JPanel {
    private final BossData boss;
    private final BossEligibility eligibility;
    private boolean hovered = false;

    public BossCard(
            BossData boss,
            BossEligibility eligibility,
            Consumer<BossData> onGearClick,
            Consumer<BossData> onGuideClick) {
        this.boss = boss;
        this.eligibility = eligibility;

        setLayout(new BorderLayout(6, 6));
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(BossWiseDesign.CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });

        // Top Row: Title + Status Badge
        JPanel topRow = new JPanel(new BorderLayout(4, 0));
        topRow.setOpaque(false);

        WrapText titleLabel = new WrapText(boss.getName(), BossWiseDesign.getHeaderFont(), BossWiseDesign.TEXT_PRIMARY);
        titleLabel.setToolTipText(boss.getName());
        topRow.add(titleLabel, BorderLayout.CENTER);

        StatusBadge statusBadge = new StatusBadge(eligibility.getStatus());
        topRow.add(statusBadge, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // Center Content: Metadata & Score
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Tag row: Difficulty + Combat Style + Rec CB (wraps gracefully if needed)
        JPanel tagRow = new JPanel(new WrapLayout(WrapLayout.LEFT, 3, 2));
        tagRow.setOpaque(false);

        tagRow.add(createTag(boss.getDifficulty().getDisplayName(), boss.getDifficulty().getBadgeColor()));
        tagRow.add(createTag(boss.getPrimaryStyle().getDisplayName(), BossWiseDesign.TEXT_SECONDARY));
        tagRow.add(createTag("Cb " + boss.getRecommendedCombatLevel() + "+", BossWiseDesign.TEXT_MUTED));

        centerPanel.add(tagRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 3)));

        // Economics & Suitability Score Row
        JPanel econRow = new JPanel(new BorderLayout(4, 0));
        econRow.setOpaque(false);
        econRow.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

        String gpStr = "~" + GearOptimizerService.formatGp(boss.getEstimatedGpHour()) + "/hr";
        JLabel gpLabel = new JLabel(gpStr);
        gpLabel.setFont(BossWiseDesign.getBoldFont());
        gpLabel.setForeground(BossWiseDesign.GOLD);
        econRow.add(gpLabel, BorderLayout.WEST);

        JLabel scoreLabel = new JLabel("Match: " + eligibility.getRecommendationScore() + "%");
        scoreLabel.setFont(BossWiseDesign.getBoldFont());
        scoreLabel.setForeground(eligibility.isReady() ? BossWiseDesign.STATUS_READY : (eligibility.isAlmostReady() ? BossWiseDesign.STATUS_ALMOST : BossWiseDesign.STATUS_LOCKED));
        econRow.add(scoreLabel, BorderLayout.EAST);

        centerPanel.add(econRow);

        // Missing requirements warning if not READY
        if (!eligibility.isReady() && !eligibility.getMissingRequirements().isEmpty()) {
            VerticalPanel missingPanel = new VerticalPanel();
            missingPanel.setOpaque(false);
            missingPanel.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));

            WrapText missingHeader = new WrapText("Requirements needed:", BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_LOCKED);
            missingPanel.add(missingHeader);

            int shown = 0;
            for (String req : eligibility.getMissingRequirements()) {
                if (shown++ >= 2) {
                    JLabel moreLabel = new JLabel("+ " + (eligibility.getMissingRequirements().size() - 2) + " more...");
                    moreLabel.setFont(BossWiseDesign.getSmallFont());
                    moreLabel.setForeground(BossWiseDesign.TEXT_MUTED);
                    missingPanel.add(moreLabel);
                    break;
                }
                WrapText reqLabel = new WrapText("• " + req, BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
                missingPanel.add(reqLabel);
            }
            centerPanel.add(missingPanel);
        }

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions: "Gear" and "Guide"
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 4, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        GoldButton gearBtn = new GoldButton("Gear");
        gearBtn.setFont(BossWiseDesign.getBoldFont());
        gearBtn.setToolTipText("Optimize Gear Setup for " + boss.getName());
        gearBtn.addActionListener(e -> {
            if (onGearClick != null) onGearClick.accept(boss);
        });
        bottomRow.add(gearBtn);

        GoldButton guideBtn = new GoldButton("Guide");
        guideBtn.setFont(BossWiseDesign.getBoldFont());
        guideBtn.setToolTipText("Open Strategy Guide for " + boss.getName());
        guideBtn.addActionListener(e -> {
            if (onGuideClick != null) onGuideClick.accept(boss);
        });
        bottomRow.add(guideBtn);

        add(bottomRow, BorderLayout.SOUTH);
    }

    private JPanel createTag(String text, Color textColor) {
        JPanel tag = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        tag.setBackground(BossWiseDesign.PANEL_BG);
        tag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BossWiseDesign.BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(1, 4, 1, 4)
        ));
        JLabel lbl = new JLabel(text);
        lbl.setFont(BossWiseDesign.getSmallFont());
        lbl.setForeground(textColor);
        tag.add(lbl);
        return tag;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background card
        g2.setColor(hovered ? BossWiseDesign.CARD_HOVER : BossWiseDesign.CARD_BG);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

        // Border
        g2.setColor(hovered ? BossWiseDesign.GOLD : BossWiseDesign.BORDER_COLOR);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        g2.dispose();
        super.paintComponent(g);
    }
}
