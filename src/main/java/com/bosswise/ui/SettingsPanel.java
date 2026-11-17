package com.bosswise.ui;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.service.WikiSyncService;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ScrollPaneConstants;

public class SettingsPanel extends JPanel {
    private final BossWiseConfig config;
    private final AccountScannerService accountScanner;
    private final WikiSyncService wikiSyncService;
    private final Runnable onSettingsChanged;

    public SettingsPanel(BossWiseConfig config, AccountScannerService accountScanner, Runnable onSettingsChanged) {
        this(config, accountScanner, new WikiSyncService(), onSettingsChanged);
    }

    public SettingsPanel(BossWiseConfig config, AccountScannerService accountScanner,
            WikiSyncService wikiSyncService, Runnable onSettingsChanged) {
        this.config = config;
        this.accountScanner = accountScanner;
        this.wikiSyncService = wikiSyncService != null ? wikiSyncService : new WikiSyncService();
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

        // WikiSync lookup: judge gear for an account this client is not logged into
        VerticalPanel syncCard = new VerticalPanel();
        syncCard.setBackground(BossWiseDesign.CARD_BG);
        syncCard.setBorder(BossWiseDesign.BORDER_CARD);

        JLabel syncTitle = new JLabel("WIKISYNC LOOKUP");
        syncTitle.setFont(BossWiseDesign.getHeaderFont());
        syncTitle.setForeground(BossWiseDesign.GOLD);
        syncCard.add(syncTitle);
        syncCard.add(Box.createVerticalStrut(3));

        WrapText syncDesc = new WrapText("Plan for an account this client is not logged into. Needs that player to run "
                + "the WikiSync plugin (most RuneLite setups do) and to have logged in with it once. Levels and "
                + "quests come from sync.runescape.wiki and drive every recommendation until you switch back.",
                BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_SECONDARY);
        syncCard.add(syncDesc);
        syncCard.add(Box.createVerticalStrut(6));

        JTextField nameField = new JTextField();
        nameField.setFont(BossWiseDesign.getRegularFont());
        nameField.setForeground(BossWiseDesign.TEXT_PRIMARY);
        nameField.setBackground(BossWiseDesign.CARD_HOVER);
        nameField.setCaretColor(BossWiseDesign.TEXT_PRIMARY);
        nameField.setBorder(BossWiseDesign.BORDER_CARD);
        nameField.setToolTipText("RuneScape name of the account to plan for");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        nameField.setPreferredSize(new Dimension(180, 24));
        syncCard.add(nameField);
        syncCard.add(Box.createVerticalStrut(5));

        WrapText syncStatus = new WrapText("", BossWiseDesign.getRegularFont(), BossWiseDesign.TEXT_MUTED);
        GoldButton loadBtn = new GoldButton("Load Profile");
        loadBtn.setPreferredSize(new Dimension(110, 26));
        loadBtn.setFont(BossWiseDesign.getBoldFont());
        GoldButton resetBtn = new GoldButton("Use This Client");
        resetBtn.setPreferredSize(new Dimension(120, 26));
        resetBtn.setFont(BossWiseDesign.getBoldFont());

        Runnable doLookup = () -> {
            String typed = nameField.getText();
            if (typed == null || typed.trim().isEmpty()) {
                syncStatus.setForeground(BossWiseDesign.STATUS_LOCKED);
                syncStatus.setText("Enter a RuneScape name first.");
                return;
            }
            loadBtn.setEnabled(false);
            syncStatus.setForeground(BossWiseDesign.TEXT_MUTED);
            syncStatus.setText("Looking up " + typed.trim() + "...");
            wikiSyncService.lookupAsync(typed, result -> SwingUtilities.invokeLater(() -> {
                loadBtn.setEnabled(true);
                syncStatus.setForeground(result.isSuccess()
                        ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);
                syncStatus.setText(result.isSuccess()
                        ? "Now using " + result.getMessage() : result.getMessage());
                if (result.isSuccess() && accountScanner != null) {
                    accountScanner.setCustomAccount(result.getAccount());
                    if (onSettingsChanged != null) onSettingsChanged.run();
                }
            }));
        };

        nameField.addActionListener(e -> doLookup.run());
        loadBtn.addActionListener(e -> doLookup.run());
        resetBtn.addActionListener(e -> {
            if (accountScanner != null) accountScanner.clearCustomAccount();
            nameField.setText("");
            syncStatus.setForeground(BossWiseDesign.TEXT_MUTED);
        syncStatus.setText("Back to this client's account.");
            if (onSettingsChanged != null) onSettingsChanged.run();
        });

        syncCard.add(loadBtn);
        syncCard.add(Box.createVerticalStrut(4));
        syncCard.add(resetBtn);
        syncCard.add(Box.createVerticalStrut(4));

        if (accountScanner != null && accountScanner.isUsingExternalAccount()) {
            syncStatus.setForeground(BossWiseDesign.STATUS_READY);
            syncStatus.setText("Now using " + WikiSyncService.describe(accountScanner.getCurrentAccount()));
        }
        syncCard.add(syncStatus);

        content.add(syncCard);
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
