package com.bosswise;

import com.bosswise.config.BossWiseConfig;
import com.bosswise.db.BossDatabase;
import com.bosswise.db.EquipmentDatabase;
import com.bosswise.model.CombatStyle;
import com.bosswise.model.PlayerAccount;
import com.bosswise.scanner.AccountScannerService;
import com.bosswise.scanner.QuestScannerService;
import com.bosswise.service.BossRecommendationEngine;
import com.bosswise.service.GePriceService;
import com.bosswise.service.GearOptimizerService;
import com.bosswise.service.GearUpgradeAdvisor;
import com.bosswise.service.InventoryBuilderService;
import com.bosswise.service.ProgressionService;
import com.bosswise.ui.BossWisePluginPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.junit.Assert;
import org.junit.Test;

public class HeadlessRenderTest {

    private static final String ARTIFACT_DIR = "C:/Users/Gaige/.gemini/antigravity/brain/b4976cdb-d323-4b3a-a440-01599d75173a";

    @Test
    public void renderAndVerifyAllTabs() throws Exception {
        List<String> layoutProblems = new ArrayList<>();

        SwingUtilities.invokeAndWait(() -> {
            QuestScannerService questScanner = new QuestScannerService();
            AccountScannerService accountScanner = new AccountScannerService(null, questScanner);
            accountScanner.setCustomAccount(PlayerAccount.createDefault());

            GePriceService priceService = new GePriceService(null);
            BossDatabase bossDatabase = new BossDatabase();
            EquipmentDatabase equipmentDatabase = new EquipmentDatabase();

            BossRecommendationEngine recommendationEngine = new BossRecommendationEngine(bossDatabase);
            GearOptimizerService gearOptimizer = new GearOptimizerService(equipmentDatabase, priceService);
            GearUpgradeAdvisor upgradeAdvisor = new GearUpgradeAdvisor(equipmentDatabase, gearOptimizer, priceService);
            InventoryBuilderService inventoryService = new InventoryBuilderService(priceService);
            ProgressionService progressionService = new ProgressionService(bossDatabase, recommendationEngine);

            BossWiseConfig config = (BossWiseConfig) Proxy.newProxyInstance(
                    BossWiseConfig.class.getClassLoader(),
                    new Class<?>[]{BossWiseConfig.class},
                    (proxy, method, mArgs) -> {
                        if ("defaultBudget".equals(method.getName())) return 5_000_000;
                        if ("preferredCombatStyle".equals(method.getName())) return CombatStyle.MELEE;
                        if ("equipmentPriority".equals(method.getName())) return GearOptimizerService.PriorityMode.BALANCED;
                        if ("showLockedBosses".equals(method.getName())) return true;
                        if ("showGroupBosses".equals(method.getName())) return true;
                        if ("compactLayout".equals(method.getName())) return false;
                        if ("autoRefreshOnStatChange".equals(method.getName())) return true;
                        return null;
                    }
            );

            BossWisePluginPanel panel = new BossWisePluginPanel(
                    accountScanner,
                    bossDatabase,
                    recommendationEngine,
                    gearOptimizer,
                    upgradeAdvisor,
                    inventoryService,
                    progressionService,
                    config
            );

            String[] tabs = {"DASHBOARD", "BOSS_FINDER", "GEAR_OPTIMIZER", "BOSS_GUIDES", "INVENTORY", "PROGRESSION", "SETTINGS"};
            int[] widths = {225, 242, 280};

            for (int w : widths) {
                int h = 750;
                panel.setSize(w, h);

                for (String tab : tabs) {
                    panel.setActiveTab(tab);
                    for (int pass = 0; pass < 4; pass++) {
                        GuideLayoutTest.layoutTree(panel);
                    }

                    // Render preview image
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2 = img.createGraphics();
                    panel.paint(g2);
                    g2.dispose();

                    try {
                        File out = new File(ARTIFACT_DIR, "preview_" + tab.toLowerCase() + "_" + w + ".png");
                        ImageIO.write(img, "png", out);
                    } catch (Exception ignored) {}

                    // Verify scroll pane and child bounds
                    JScrollPane sp = findVisibleScrollPane(panel);
                    if (sp != null && sp.getViewport() != null && sp.getViewport().getView() != null) {
                        Assert.assertEquals(
                                "Horizontal scrollbar must never be shown on " + tab,
                                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
                                sp.getHorizontalScrollBarPolicy()
                        );
                        Component view = sp.getViewport().getView();
                        int vw = sp.getViewport().getWidth();
                        if (vw > 0 && view.getWidth() > vw) {
                            layoutProblems.add(tab + " @" + w + "px: view width " + view.getWidth() + " > viewport " + vw);
                        }
                        if (view instanceof Container && vw > 0) {
                            verifyContainerBounds((Container) view, view, vw, tab + " @" + w + "px", layoutProblems);
                        }
                    }
                }
            }
        });

        if (!layoutProblems.isEmpty()) {
            Assert.fail(layoutProblems.size() + " layout overflow issues detected:\n" + String.join("\n", layoutProblems));
        }
    }

    private static void verifyContainerBounds(Container container, Component root, int maxWidth, String ctx, List<String> problems) {
        for (Component c : container.getComponents()) {
            if (!c.isVisible()) continue;
            Rectangle r = SwingUtilities.convertRectangle(container, c.getBounds(), root);
            if (r.x < 0 || (r.x + r.width) > maxWidth + 1) { // 1px rounding tolerance
                String label = c.getClass().getSimpleName();
                problems.add(ctx + ": " + label + " spans x=" + r.x + ".." + (r.x + r.width) + " > maxWidth " + maxWidth);
            }
            if (c instanceof Container) {
                verifyContainerBounds((Container) c, root, maxWidth, ctx, problems);
            }
        }
    }

    private static JScrollPane findVisibleScrollPane(Container container) {
        for (Component c : container.getComponents()) {
            if (c.isVisible() && c instanceof JScrollPane) {
                return (JScrollPane) c;
            }
            if (c.isVisible() && c instanceof Container) {
                JScrollPane sp = findVisibleScrollPane((Container) c);
                if (sp != null) return sp;
            }
        }
        return null;
    }
}
