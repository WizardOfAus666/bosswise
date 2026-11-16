package com.bosswise.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Vertical BoxLayout stack whose children are all left-aligned and stretched to full width.
 *
 * <p>BoxLayout misplaces children when their alignmentX values differ (JLabel defaults to
 * LEFT, JPanel/Box fillers to CENTER): left-aligned children start at the centre line and
 * run off the right edge. Forcing a single alignment removes that offset. Height is capped
 * at the preferred height so the stack never stretches vertically.</p>
 */
public class VerticalPanel extends ScrollablePanel {
    public VerticalPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLeftAlignChildren(true);
        setAlignmentX(LEFT_ALIGNMENT);
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (comp instanceof JComponent) {
            ((JComponent) comp).setAlignmentX(LEFT_ALIGNMENT);
        }
        super.addImpl(comp, constraints, index);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, super.getMinimumSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
