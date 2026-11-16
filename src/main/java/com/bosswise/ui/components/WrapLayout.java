package com.bosswise.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A FlowLayout that calculates preferred size by wrapping components to fit
 * within the target container's available width rather than expanding horizontally.
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout() {
        super();
    }

    public WrapLayout(int align) {
        super(align);
    }

    public WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth();
            if (targetWidth <= 0) {
                Container p = target.getParent();
                while (p != null && targetWidth <= 0) {
                    targetWidth = p.getWidth();
                    p = p.getParent();
                }
                if (targetWidth <= 0) {
                    targetWidth = com.bosswise.ui.BossWiseDesign.BODY_TEXT_WIDTH;
                }
            }

            Insets insets = target.getInsets();
            int maxRowWidth = targetWidth - (insets.left + insets.right + getHgap() * 2);
            if (maxRowWidth <= 0) {
                maxRowWidth = com.bosswise.ui.BossWiseDesign.BODY_TEXT_WIDTH;
            }

            int rowWidth = 0;
            int rowHeight = 0;
            int totalHeight = 0;
            int maxActualWidth = 0;
            boolean firstInRow = true;

            int n = target.getComponentCount();
            for (int i = 0; i < n; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (!firstInRow && rowWidth + d.width + getHgap() > maxRowWidth) {
                        totalHeight += rowHeight + getVgap();
                        maxActualWidth = Math.max(maxActualWidth, rowWidth);
                        rowWidth = d.width;
                        rowHeight = d.height;
                    } else {
                        if (!firstInRow) {
                            rowWidth += getHgap();
                        }
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                    firstInRow = false;
                }
            }
            totalHeight += rowHeight;
            maxActualWidth = Math.max(maxActualWidth, rowWidth);

            return new Dimension(
                    maxActualWidth + insets.left + insets.right + getHgap() * 2,
                    totalHeight + insets.top + insets.bottom + getVgap() * 2
            );
        }
    }
}
