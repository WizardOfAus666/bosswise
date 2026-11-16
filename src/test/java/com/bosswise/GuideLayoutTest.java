package com.bosswise;

import com.bosswise.db.BossDatabase;
import com.bosswise.model.BossData;
import com.bosswise.ui.BossGuidePanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * Lays out the Guides tab at real RuneLite sidebar widths for every boss and verifies that
 * no component extends past the visible viewport and no text is clipped or truncated.
 */
public class GuideLayoutTest {

    /** RuneLite PluginPanel.PANEL_WIDTH (225) is the minimum; 242 is the default outer width. */
    private static final int[] WIDTHS = {225, 242, 250};

    @Test
    public void everyGuideCardFitsSidebarWidth() throws Exception {
        List<String> problems = new ArrayList<>();
        SwingUtilities.invokeAndWait(() -> {
            BossDatabase db = new BossDatabase();
            BossGuidePanel panel = new BossGuidePanel(db);
            for (int width : WIDTHS) {
                for (BossData boss : db.getAllBosses().values()) {
                    panel.selectBoss(boss);
                    panel.setSize(width, 900);
                    for (int pass = 0; pass < 4; pass++) {
                        layoutTree(panel);
                    }
                    check(panel, boss.getName() + " @" + width + "px", problems);
                }
            }
        });
        if (!problems.isEmpty()) {
            int shown = Math.min(problems.size(), 40);
            Assert.fail(problems.size() + " layout problem(s):\n" + String.join("\n", problems.subList(0, shown)));
        }
    }

    /**
     * Headless equivalent of revalidate(): invalidate every container (clearing BoxLayout's
     * cached size requirements) and then lay out top-down. Container.validate() is a no-op
     * without a native peer, so this is done manually.
     */
    static void layoutTree(Component c) {
        invalidateTree(c);
        doLayoutTree(c);
    }

    private static void invalidateTree(Component c) {
        c.invalidate();
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                invalidateTree(child);
            }
        }
    }

    private static void doLayoutTree(Component c) {
        if (c instanceof Container) {
            Container ct = (Container) c;
            ct.doLayout();
            for (Component child : ct.getComponents()) {
                doLayoutTree(child);
            }
        }
    }

    private static void check(BossGuidePanel panel, String ctx, List<String> problems) {
        JScrollPane scroll = findScroll(panel);
        Assert.assertNotNull("Guides tab must contain a JScrollPane", scroll);
        Assert.assertEquals(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, scroll.getHorizontalScrollBarPolicy());

        Component view = scroll.getViewport().getView();
        int vw = scroll.getViewport().getWidth();
        if (view.getWidth() != vw) {
            problems.add(ctx + ": content width " + view.getWidth() + " != viewport width " + vw);
        }
        checkChildren((Container) view, view, vw, ctx, problems);
    }

    private static void checkChildren(Container parent, Component view, int vw, String ctx, List<String> problems) {
        for (Component c : parent.getComponents()) {
            if (!c.isVisible()) continue;
            Rectangle r = SwingUtilities.convertRectangle(parent, c.getBounds(), view);
            String name = describe(c);
            if (r.x < 0 || r.x + r.width > vw) {
                problems.add(ctx + ": " + name + " spans x=" + r.x + ".." + (r.x + r.width) + " outside viewport 0.." + vw);
            }
            if (c instanceof JTextArea) {
                int need = c.getPreferredSize().height;
                if (c.getHeight() < need) {
                    problems.add(ctx + ": " + name + " height " + c.getHeight() + " < needed " + need + " (lines cut off)");
                }
            } else if (c instanceof JLabel || c instanceof AbstractButton) {
                int need = c.getPreferredSize().width;
                if (c.getWidth() < need) {
                    problems.add(ctx + ": " + name + " width " + c.getWidth() + " < needed " + need + " (text truncated)");
                }
            }
            if (c instanceof Container) {
                checkChildren((Container) c, view, vw, ctx, problems);
            }
        }
    }

    private static String describe(Component c) {
        String text = null;
        if (c instanceof JLabel) text = ((JLabel) c).getText();
        else if (c instanceof JTextArea) text = ((JTextArea) c).getText();
        else if (c instanceof AbstractButton) text = ((AbstractButton) c).getText();
        String cls = c.getClass().getSimpleName().isEmpty() ? c.getClass().getName() : c.getClass().getSimpleName();
        if (text == null) return cls;
        text = text.replaceAll("<[^>]+>", "").replace('\n', ' ');
        return cls + "[\"" + (text.length() > 40 ? text.substring(0, 40) + "..." : text) + "\"]";
    }

    private static JScrollPane findScroll(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JScrollPane) return (JScrollPane) child;
            if (child instanceof Container) {
                JScrollPane s = findScroll((Container) child);
                if (s != null) return s;
            }
        }
        return null;
    }
}
