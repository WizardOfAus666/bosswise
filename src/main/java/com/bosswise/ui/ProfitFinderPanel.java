package com.bosswise.ui;

import com.bosswise.model.DeathRiskProfile;
import com.bosswise.model.MonsterProfitProfile;
import com.bosswise.service.MonsterProfitService;
import com.bosswise.service.MonsterProfitService.ProfitFilter;
import com.bosswise.service.MonsterProfitService.ProfitSort;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ProfitFinderPanel extends JPanel {
    private final MonsterProfitService profitService;

    private final JComboBox<ProfitFilter> filterCombo = new JComboBox<>(ProfitFilter.values());
    private final JComboBox<ProfitSort> sortCombo = new JComboBox<>(ProfitSort.values());
    private final JPanel contentPanel = new VerticalPanel();

    public ProfitFinderPanel(MonsterProfitService profitService) {
        this.profitService = profitService;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BossWiseDesign.PANEL_BG);
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        WrapText title = new WrapText("PROFIT FINDER & RISK", BossWiseDesign.getHeaderFont(), BossWiseDesign.GOLD);
        topContainer.add(title);

        WrapText sub = new WrapText("Net GP/hr, supplies & death risk calculator", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        topContainer.add(sub);
        topContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        // Filter & Sort row
        JPanel filterRow = new JPanel(new GridLayout(1, 2, 4, 0));
        filterRow.setOpaque(false);

        filterCombo.setFont(BossWiseDesign.getSmallFont());
        filterCombo.setBackground(BossWiseDesign.CARD_BG);
        filterCombo.setForeground(BossWiseDesign.TEXT_PRIMARY);
        filterCombo.addActionListener(e -> renderList());
        filterRow.add(filterCombo);

        sortCombo.setFont(BossWiseDesign.getSmallFont());
        sortCombo.setBackground(BossWiseDesign.CARD_BG);
        sortCombo.setForeground(BossWiseDesign.TEXT_PRIMARY);
        sortCombo.addActionListener(e -> renderList());
        filterRow.add(sortCombo);

        topContainer.add(filterRow);
        add(topContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        renderList();
    }

    public void renderList() {
        contentPanel.removeAll();

        ProfitFilter filter = (ProfitFilter) filterCombo.getSelectedItem();
        ProfitSort sort = (ProfitSort) sortCombo.getSelectedItem();

        List<MonsterProfitProfile> profiles = profitService.getFilteredProfiles(filter, sort);

        for (MonsterProfitProfile p : profiles) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(BossWiseDesign.CARD_BG);
            card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
            card.setAlignmentX(LEFT_ALIGNMENT);

            // Name + Level row
            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setOpaque(false);
            WrapText name = new WrapText(p.getName(), BossWiseDesign.getBoldFont(), BossWiseDesign.GOLD);
            top.add(name, BorderLayout.CENTER);

            JLabel lvl = new JLabel("Lvl " + p.getCombatLevel());
            lvl.setFont(BossWiseDesign.getSmallFont());
            lvl.setForeground(BossWiseDesign.TEXT_MUTED);
            top.add(lvl, BorderLayout.EAST);
            card.add(top);
            card.add(Box.createRigidArea(new Dimension(0, 3)));

            // Net Profit (Bold)
            WrapText net = new WrapText("Net Profit: " + p.getFormattedNetProfit(), BossWiseDesign.getHeaderFont(), BossWiseDesign.STATUS_READY);
            card.add(net);

            WrapText opt = new WrapText(String.format("Optimistic: %.2fM/hr (with uniques)", p.getOptimisticNetProfitPerHour() / 1_000_000.0), BossWiseDesign.getSmallFont(), BossWiseDesign.STATUS_INFO);
            card.add(opt);

            WrapText stats = new WrapText(String.format("%s/kill  |  %d Kills/hr\nSupplies: -%dK/hr",
                    p.getFormattedGpPerKill(), p.getKillsPerHour(), p.getTotalCostsPerHour() / 1000), BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_SECONDARY);
            card.add(stats);

            WrapText gear = new WrapText("Min Gear Req: " + p.getFormattedMinGear() + " (" + p.getPrimaryStyle().name() + ")", BossWiseDesign.getSmallFont(), BossWiseDesign.GOLD_HOVER);
            card.add(gear);

            // Death Risk Summary (Feature 16)
            DeathRiskProfile risk = p.getDeathRisk();
            Color riskCol = risk.isPvpDangerous() ? BossWiseDesign.STATUS_LOCKED : BossWiseDesign.STATUS_ALMOST;
            WrapText death = new WrapText(String.format("Death Risk: %s (~%s loss)", risk.getLocationType(), risk.getFormattedTotalCost()), BossWiseDesign.getSmallFont(), riskCol);
            card.add(death);

            // Assumptions
            if (!p.getAssumptions().isEmpty()) {
                JLabel assump = new JLabel("<html><body style='width: 170px; color: #8E9DB0; font-size: 9px;'>" + p.getAssumptions() + "</body></html>");
                card.add(assump);
            }

            contentPanel.add(card);
            contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
