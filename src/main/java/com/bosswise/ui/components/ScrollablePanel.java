package com.bosswise.ui.components;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * A JPanel that implements {@link Scrollable} and forces its width to track
 * the viewport width of its enclosing {@link javax.swing.JScrollPane}.
 * This prevents horizontal scrolling and ensures child components never clip horizontally.
 */
public class ScrollablePanel extends JPanel implements Scrollable {

    public ScrollablePanel() {
        super();
    }

    public ScrollablePanel(LayoutManager layout) {
        super(layout);
    }

    private boolean leftAlignChildren = false;

    /**
     * When enabled, every child added is forced to LEFT alignment so BoxLayout never offsets
     * cards to the right because of mixed alignmentX values.
     */
    public void setLeftAlignChildren(boolean leftAlignChildren) {
        this.leftAlignChildren = leftAlignChildren;
    }

    @Override
    protected void addImpl(java.awt.Component comp, Object constraints, int index) {
        if (leftAlignChildren && comp instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) comp).setAlignmentX(LEFT_ALIGNMENT);
        }
        super.addImpl(comp, constraints, index);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 32;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // Crucial: forces panel width to strictly follow the scrollpane viewport width
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
