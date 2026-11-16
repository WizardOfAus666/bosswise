package com.bosswise.ui;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class SettingsPanel extends JPanel {
    private final BossWiseConfig config;
    private final AccountScannerService accountScanner;
    private final Runnable onSettingsChanged;

    public SettingsPanel(BossWiseConfig config, AccountScannerService accountScanner, Runnable onSettingsChanged) {
        this.config = config;
        this.accountScanner = accountScanner;
        this.onSettingsChanged = onSettingsChanged;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        ScrollablePanel content = new ScrollablePanel();
        content.setLeftAlignChildren(true);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BossWiseDesign.BG_DARK);
        content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Header
        JLabel title = new JLabel("BOSSWISE SETTINGS");
        title.setFont(BossWiseDesign.getHeaderFont());
        title.setForeground(BossWiseDesign.GOLD);
        content.add(title);
        content.add(Box.createRigidArea(new Dimension(0, 6)));

        // Action: Scan Account Now
        VerticalPanel scanCard = new VerticalPanel();
        scanCard.setBackground(BossWiseDesign.CARD_BG);
        scanCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel scanTitle = new JLabel("ACCOUNT SCAN");
        scanTitle.setFont(BossWiseDesign.getHeaderFont());
        scanTitle.setForeground(BossWiseDesign.GOLD);
        scanCard.add(scanTitle);
        scanCard.add(Box.createVerticalStrut(3));

        WrapText scanDesc = new WrapText("Instantly re-scan your combat stats and quest states from the active client session.", BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_SECONDARY);
        scanCard.add(scanDesc);
        scanCard.add(Box.createVerticalStrut(6));

        GoldButton scanBtn = new GoldButton("Scan Now");
        scanBtn.setPreferredSize(new Dimension(85, 26));
        scanBtn.setFont(BossWiseDesign.getBoldFont());
        scanBtn.addActionListener(e -> {
            accountScanner.scanAccount();
            if (onSettingsChanged != null) onSettingsChanged.run();
        });
        scanCard.add(scanBtn);
        content.add(scanCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // Preferences Card
        VerticalPanel prefCard = new VerticalPanel();
        prefCard.setBackground(BossWiseDesign.CARD_BG);
        prefCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel prefTitle = new JLabel("DISPLAY PREFERENCES");
        prefTitle.setFont(BossWiseDesign.getHeaderFont());
        prefTitle.setForeground(BossWiseDesign.GOLD);
        prefCard.add(prefTitle);
        prefCard.add(Box.createVerticalStrut(6));

        JCheckBox showLocked = new JCheckBox("Show Locked Bosses", config.showLockedBosses());
        styleCheck(showLocked);
        prefCard.add(showLocked);
        prefCard.add(Box.createVerticalStrut(4));

        JCheckBox showGroup = new JCheckBox("Show Group / Raid Encounters", config.showGroupBosses());
        styleCheck(showGroup);
        prefCard.add(showGroup);
        prefCard.add(Box.createVerticalStrut(4));

        JCheckBox autoRefresh = new JCheckBox("Auto-Refresh On Stat Changes", config.autoRefreshOnStatChange());
        styleCheck(autoRefresh);
        prefCard.add(autoRefresh);

        content.add(prefCard);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        // Plugin Hub Compliance Information
        VerticalPanel infoCard = new VerticalPanel();
        infoCard.setBackground(BossWiseDesign.CARD_BG);
        infoCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel infoTitle = new JLabel("HUB COMPLIANCE");
        infoTitle.setFont(BossWiseDesign.getHeaderFont());
        infoTitle.setForeground(BossWiseDesign.STATUS_READY);
        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(4));

        WrapText compTxt = new WrapText("BossWise is an informational assistant. It does not automate gameplay, perform automated clicks, or alter the game client. All data is retrieved through official RuneLite client APIs.", BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_SECONDARY);
        infoCard.add(compTxt);

        content.add(infoCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BossWiseDesign.BG_DARK);
        scroll.getViewport().setBackground(BossWiseDesign.BG_DARK);

        add(scroll, BorderLayout.CENTER);
    }

    private void styleCheck(JCheckBox cb) {
        cb.setFont(BossWiseDesign.getRegularFont());
        cb.setForeground(BossWiseDesign.TEXT_PRIMARY);
        cb.setOpaque(false);
        cb.setFocusPainted(false);
    }
}
