package com.bosswise.ui;

import com.bosswise.db.BossDatabase;
import com.bosswise.db.MonsterDatabase;
import com.bosswise.model.BossCombatProfile;
import com.bosswise.model.BossData;
import com.bosswise.model.CombatResult;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.DpsComparisonResult;
import com.bosswise.model.EquipmentItem;
import com.bosswise.model.EquipmentSlot;
import com.bosswise.model.MonsterCombatProfile;
import com.bosswise.model.MonsterData;
import com.bosswise.model.MonsterVariant;
import com.bosswise.model.OptimizedGearSetup;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.CurrentGearScannerService;
import com.bosswise.service.DpsLabService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.ui.components.GoldButton;
import com.bosswise.ui.components.ScrollablePanel;
import com.bosswise.ui.components.StatusBadge;
import com.bosswise.ui.components.VerticalPanel;
import com.bosswise.ui.components.WrapText;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class DpsLabPanel extends JPanel {
    private final BossDatabase bossDatabase;
    private final MonsterDatabase monsterDatabase;
    private final AccountScannerService accountScanner;
    private final CurrentGearScannerService currentGearScanner;
    private final GearOptimizerService optimizerService;
    private final DpsLabService dpsLabService;

    private final JComboBox<String> monsterCombo = new JComboBox<>();
    private final JPanel contentPanel = new VerticalPanel();

    private CombatStyle selectedStyle = CombatStyle.MELEE;
    private String selectedPrayer = "Piety";
    private String selectedPotion = "Super";
    private boolean onSlayerTask = false;

    public DpsLabPanel(
            BossDatabase bossDatabase,
            MonsterDatabase monsterDatabase,
            AccountScannerService accountScanner,
            CurrentGearScannerService currentGearScanner,
            GearOptimizerService optimizerService,
            DpsLabService dpsLabService) {

        this.bossDatabase = bossDatabase;
        this.monsterDatabase = monsterDatabase;
        this.accountScanner = accountScanner;
        this.currentGearScanner = currentGearScanner;
        this.optimizerService = optimizerService;
        this.dpsLabService = dpsLabService;

        setLayout(new BorderLayout());
        setBackground(BossWiseDesign.BG_DARK);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BossWiseDesign.PANEL_BG);
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BossWiseDesign.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        WrapText title = new WrapText("DPS COMPARISON LAB", BossWiseDesign.getHeaderFont(), BossWiseDesign.GOLD);
        topContainer.add(title);

        WrapText sub = new WrapText("Setup A (Current) vs Setup B (Optimized)", BossWiseDesign.getSmallFont(), BossWiseDesign.TEXT_MUTED);
        topContainer.add(sub);
        topContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        // Monster selector dropdown
        initMonsterCombo();
        monsterCombo.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 26));
        monsterCombo.setFont(BossWiseDesign.getRegularFont());
        monsterCombo.setBackground(BossWiseDesign.CARD_BG);
        monsterCombo.setForeground(BossWiseDesign.TEXT_PRIMARY);
        monsterCombo.addActionListener(e -> calculateAndRender());
        topContainer.add(monsterCombo);
        topContainer.add(Box.createRigidArea(new Dimension(0, 6)));

        // Style selector buttons
        JPanel styleRow = new JPanel(new GridLayout(1, 3, 3, 0));
        styleRow.setOpaque(false);
        addStyleBtn(styleRow, "Melee", CombatStyle.MELEE);
        addStyleBtn(styleRow, "Ranged", CombatStyle.RANGED);
        addStyleBtn(styleRow, "Magic", CombatStyle.MAGIC);
        topContainer.add(styleRow);
        topContainer.add(Box.createRigidArea(new Dimension(0, 4)));

        // Buff toggles row
        JPanel buffRow = new JPanel(new GridLayout(1, 2, 4, 0));
        buffRow.setOpaque(false);

        GoldButton prayBtn = new GoldButton("Pray: " + selectedPrayer);
        prayBtn.setFont(BossWiseDesign.getBadgeFont());
        prayBtn.setPreferredSize(new Dimension(98, 22));
        prayBtn.addActionListener(e -> {
            cyclePrayer();
            prayBtn.setText("Pray: " + selectedPrayer);
            calculateAndRender();
        });
        buffRow.add(prayBtn);

        GoldButton potBtn = new GoldButton("Pot: " + selectedPotion);
        potBtn.setFont(BossWiseDesign.getBadgeFont());
        potBtn.setPreferredSize(new Dimension(98, 22));
        potBtn.addActionListener(e -> {
            cyclePotion();
            potBtn.setText("Pot: " + selectedPotion);
            calculateAndRender();
        });
        buffRow.add(potBtn);

        topContainer.add(buffRow);

        add(topContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        calculateAndRender();
    }

    private void initMonsterCombo() {
        monsterCombo.removeAllItems();
        monsterCombo.addItem("Vorkath");
        monsterCombo.addItem("Zulrah");
        monsterCombo.addItem("Scurrius");
        monsterCombo.addItem("General Graardor");
        monsterCombo.addItem("Phantom Muspah");
        monsterCombo.addItem("Alchemical Hydra");
        monsterCombo.addItem("Nex");
        monsterCombo.addItem("Vardorvis");
        monsterCombo.addItem("Duke Sucellus");
        monsterCombo.addItem("The Whisperer");
        monsterCombo.addItem("The Leviathan");
        monsterCombo.addItem("Kraken");
        monsterCombo.addItem("Cerberus");
        monsterCombo.addItem("Calvar'ion");
        monsterCombo.addItem("Barrows");
        monsterCombo.addItem("Corporeal Beast");
    }

    private void addStyleBtn(JPanel parent, String label, CombatStyle style) {
        GoldButton btn = new GoldButton(label);
        btn.setFont(BossWiseDesign.getBadgeFont());
        btn.setPreferredSize(new Dimension(66, 22));
        btn.setActive(this.selectedStyle == style);
        btn.addActionListener(e -> {
            this.selectedStyle = style;
            if (style == CombatStyle.RANGED) selectedPrayer = "Rigour";
            else if (style == CombatStyle.MAGIC) selectedPrayer = "Augury";
            else selectedPrayer = "Piety";
            calculateAndRender();
        });
        parent.add(btn);
    }

    private void cyclePrayer() {
        if ("Piety".equalsIgnoreCase(selectedPrayer) || "Rigour".equalsIgnoreCase(selectedPrayer) || "Augury".equalsIgnoreCase(selectedPrayer)) {
            selectedPrayer = "15%";
        } else if ("15%".equalsIgnoreCase(selectedPrayer)) {
            selectedPrayer = "None";
        } else {
            if (selectedStyle == CombatStyle.RANGED) selectedPrayer = "Rigour";
            else if (selectedStyle == CombatStyle.MAGIC) selectedPrayer = "Augury";
            else selectedPrayer = "Piety";
        }
    }

    private void cyclePotion() {
        if ("Super".equalsIgnoreCase(selectedPotion) || "Divine".equalsIgnoreCase(selectedPotion)) {
            selectedPotion = "Regular";
        } else if ("Regular".equalsIgnoreCase(selectedPotion)) {
            selectedPotion = "None";
        } else {
            selectedPotion = "Super";
        }
    }

    public void calculateAndRender() {
        contentPanel.removeAll();

        String selectedName = (String) monsterCombo.getSelectedItem();
        if (selectedName == null) selectedName = "Vorkath";

        BossData boss = bossDatabase.getBoss(selectedName.toLowerCase().replace(" ", "_").replace("'", ""));
        MonsterData monster = monsterDatabase.getMonster(selectedName.toLowerCase().replace(" ", "_").replace("'", ""));
        PlayerAccount account = (accountScanner != null) ? accountScanner.getCurrentAccount() : PlayerAccount.createDefault();

        MonsterVariant variant = (monster != null && !monster.getVariants().isEmpty())
                ? monster.getVariants().get(0)
                : ((boss != null) ? boss.toVariant() : null);

        MonsterCombatProfile profile = (monster != null)
                ? monster.getCombatProfile()
                : ((boss != null) ? optimizerService.getBossProfile(boss).toMonsterCombatProfile() : null);

        // Setup A: Current equipped gear (or fallback)
        Map<EquipmentSlot, EquipmentItem> setupAGear = (currentGearScanner != null)
                ? currentGearScanner.getCurrentEquipment()
                : java.util.Collections.emptyMap();

        // Setup B: Optimized Best Value / Available setup for this boss & budget
        long budget = 25_000_000L;
        OptimizedGearSetup optSetup = (boss != null)
                ? optimizerService.generateSetup("Best Available", boss, selectedStyle, budget, account, GearOptimizerService.PriorityMode.BALANCED)
                : null;
        Map<EquipmentSlot, EquipmentItem> setupBGear = (optSetup != null) ? optSetup.getEquipment() : setupAGear;

        DpsComparisonResult result = dpsLabService.compareSetups(
                selectedName, variant, profile, account, selectedStyle,
                setupAGear, "Current Gear",
                setupBGear, "Recommended Gear",
                selectedPrayer, selectedPotion, onSlayerTask
        );

        // 1. Verdict Card
        JPanel verdictCard = new JPanel();
        verdictCard.setLayout(new BoxLayout(verdictCard, BoxLayout.Y_AXIS));
        verdictCard.setBackground(BossWiseDesign.CARD_BG);
        verdictCard.setBorder(BossWiseDesign.BORDER_GOLD);
        verdictCard.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 120));

        JLabel vTitle = new JLabel("COMPARISON VERDICT");
        vTitle.setFont(BossWiseDesign.getBoldFont());
        vTitle.setForeground(BossWiseDesign.GOLD);
        verdictCard.add(vTitle);
        verdictCard.add(Box.createRigidArea(new Dimension(0, 4)));

        double dpsDiff = result.getDpsDifference();
        String dpsSign = (dpsDiff >= 0) ? "+" : "";
        JLabel dpsLabel = new JLabel(String.format("%s%.2f DPS (%s%.1f%%)", dpsSign, dpsDiff, dpsSign, result.getDpsPercentageImprovement()));
        dpsLabel.setFont(BossWiseDesign.getTitleFont());
        dpsLabel.setForeground(dpsDiff >= 0 ? BossWiseDesign.STATUS_READY : BossWiseDesign.STATUS_LOCKED);
        verdictCard.add(dpsLabel);

        JLabel maxAcc = new JLabel(String.format("Max Hit: %+d  |  Accuracy: %+.1f%%", result.getMaxHitDifference(), result.getAccuracyDifference()));
        maxAcc.setFont(BossWiseDesign.getRegularFont());
        maxAcc.setForeground(BossWiseDesign.TEXT_SECONDARY);
        verdictCard.add(maxAcc);

        long costDiff = result.getCostDifference();
        String costStr = (costDiff >= 0) ? "+" + formatGp(costDiff) : formatGp(costDiff);
        JLabel costLabel = new JLabel("Upgrade Cost: " + costStr + " GP");
        costLabel.setFont(BossWiseDesign.getSmallFont());
        costLabel.setForeground(BossWiseDesign.TEXT_MUTED);
        verdictCard.add(costLabel);

        contentPanel.add(verdictCard);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 2. Setup A Card
        addSetupCard("SETUP A: CURRENT GEAR", result.getSetupA(), BossWiseDesign.STATUS_INFO);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 3. Setup B Card
        addSetupCard("SETUP B: RECOMMENDED GEAR", result.getSetupB(), BossWiseDesign.GOLD);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void addSetupCard(String title, DpsComparisonResult.SetupEvaluation eval, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BossWiseDesign.CARD_BG);
        card.setBorder(BossWiseDesign.BORDER_CARD_SLIM);
        card.setMaximumSize(new Dimension(BossWiseDesign.USABLE_WIDTH, 140));

        JLabel t = new JLabel(title);
        t.setFont(BossWiseDesign.getBoldFont());
        t.setForeground(accent);
        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel dps = new JLabel(String.format("Expected DPS: %.2f", eval.getDps()));
        dps.setFont(BossWiseDesign.getHeaderFont());
        dps.setForeground(BossWiseDesign.TEXT_PRIMARY);
        card.add(dps);

        JLabel stats = new JLabel(String.format("Max Hit: %d  |  Hit: %.1f%%  |  Speed: %.2fs",
                eval.getMaxHit(), eval.getHitChance() * 100.0, 1.0 / eval.getAttacksPerSecond()));
        stats.setFont(BossWiseDesign.getSmallFont());
        stats.setForeground(BossWiseDesign.TEXT_SECONDARY);
        card.add(stats);

        JLabel rolls = new JLabel(String.format("Atk Roll: %s  |  Def Roll: %s",
                formatNumber(eval.getAttackRoll()), formatNumber(eval.getDefenceRoll())));
        rolls.setFont(BossWiseDesign.getSmallFont());
        rolls.setForeground(BossWiseDesign.TEXT_MUTED);
        card.add(rolls);

        JLabel cost = new JLabel("Total Gear Value: " + formatGp(eval.getTotalPrice()) + " GP");
        cost.setFont(BossWiseDesign.getSmallFont());
        cost.setForeground(BossWiseDesign.GOLD_HOVER);
        card.add(cost);

        contentPanel.add(card);
    }

    private String formatGp(long amount) {
        if (amount >= 1_000_000) return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000) return String.format("%dK", amount / 1_000);
        return String.valueOf(amount);
    }

    private String formatNumber(long n) {
        if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
        if (n >= 1_000) return String.format("%dK", n / 1_000);
        return String.valueOf(n);
    }
}
