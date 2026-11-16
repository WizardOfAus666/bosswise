package com.bosswise.ui.components;

import com.bosswise.ui.BossWiseDesign;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.View;

/**
 * Read-only multi-line text that always wraps to the width its parent gives it.
 *
 * <p>Unlike an HTML JLabel with a fixed CSS width, this never asks for more horizontal space
 * than is available: its preferred width is tiny (so parent layout managers can shrink it),
 * and its preferred height is accurately computed by accumulating all ancestor container insets
 * so words wrap cleanly and are never clipped.</p>
 */
public class WrapText extends JTextArea {
    private static final int MIN_PREF_WIDTH = 10;

    public WrapText(String text, Font font, Color color) {
        super(text == null ? "" : text);
        setFont(font);
        setForeground(color);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setHighlighter(null);
        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));
        setBorder(BorderFactory.createEmptyBorder());
        setMargin(new Insets(0, 0, 0, 0));
        setCursor(Cursor.getDefaultCursor());
        setAlignmentX(LEFT_ALIGNMENT);
    }

    /**
     * Width currently available for text, accurately accumulating all ancestor insets
     * until finding the enclosing viewport/window.
     */
    private int availableWidth() {
        int w = getWidth();
        if (w > 0) {
            Insets in = getInsets();
            return Math.max(1, w - (in != null ? in.left + in.right : 0));
        }

        int totalHorizontalInsets = 0;
        Insets in = getInsets();
        if (in != null) {
            totalHorizontalInsets += in.left + in.right;
        }

        Container current = getParent();
        while (current != null && current.getWidth() <= 0) {
            Insets ci = current.getInsets();
            if (ci != null) {
                totalHorizontalInsets += ci.left + ci.right;
            }
            current = current.getParent();
        }

        if (current != null && current.getWidth() > 0) {
            Insets ci = current.getInsets();
            if (ci != null) {
                totalHorizontalInsets += ci.left + ci.right;
            }
            w = current.getWidth() - totalHorizontalInsets;
            if (w > 20) {
                return w;
            }
        }
        return BossWiseDesign.BODY_TEXT_WIDTH;
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Insets in = getInsets();
        int insetsH = in != null ? in.left + in.right : 0;
        int insetsV = in != null ? in.top + in.bottom : 0;
        int textWidth = Math.max(1, availableWidth() - insetsH);
        
        try {
            View root = getUI() != null ? getUI().getRootView(this) : null;
            if (root != null) {
                root.setSize(textWidth, Integer.MAX_VALUE);
                int h = (int) Math.ceil(root.getPreferredSpan(View.Y_AXIS));
                return new Dimension(MIN_PREF_WIDTH, Math.max(14, h + insetsV));
            }
        } catch (Exception ignored) {}

        return super.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        int oldWidth = getWidth();
        super.setBounds(x, y, width, height);
        if (width != oldWidth && width > 0) {
            // Height depends on width: re-run layout once the new width is known.
            SwingUtilities.invokeLater(this::revalidate);
        }
    }
}
