package com.bosswise.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import net.runelite.client.ui.FontManager;

public class BossWiseDesign {
    // Calibrated RuneLite Sidebar Dimensions (Wider Layout for High Readability)
    public static final int PANEL_WIDTH = 280;
    public static final int USABLE_WIDTH = 256;
    public static final int BODY_TEXT_WIDTH = 236;

    // Authentic OSRS Light Brown / Warm Parchment Palette
    public static final Color BG_DARK = new Color(0xD2, 0xC1, 0xA5);       // Warm Light Tan Brown (Main Panel BG)
    public static final Color PANEL_BG = new Color(0xC5, 0xB2, 0x94);      // Toasted Sandstone (Header & Controls)
    public static final Color CARD_BG = new Color(0xDF, 0xD2, 0xBC);       // Light Warm Parchment Card
    public static final Color CARD_HOVER = new Color(0xE9, 0xDF, 0xC9);    // Luminous Parchment Hover
    public static final Color BORDER_COLOR = new Color(0x99, 0x80, 0x60);  // Golden Wood Border
    public static final Color BORDER_SUBTLE = new Color(0xB5, 0x9E, 0x7C); // Soft Sandstone Border

    // Polished Burnished Gold Accents (High Contrast on Light Parchment)
    public static final Color GOLD = new Color(0x8F, 0x52, 0x00);          // Burnished Gold / Amber
    public static final Color GOLD_HOVER = new Color(0x73, 0x41, 0x00);    // Deep Amber Hover
    public static final Color GOLD_DARK = new Color(0x59, 0x32, 0x00);
    public static final Color GOLD_BG = new Color(0xDF, 0xC6, 0x96);       // Warm Golden Parchment

    // Crystal Clear Typography (Deep Dark Brown on Light Surfaces for AAA Contrast)
    public static final Color TEXT_PRIMARY = new Color(0x24, 0x18, 0x0E);     // Dark Umber / Espresso (Sharpest Contrast)
    public static final Color TEXT_SECONDARY = new Color(0x4B, 0x36, 0x23);   // Roasted Chestnut
    public static final Color TEXT_MUTED = new Color(0x73, 0x57, 0x3E);       // Warm Earth Brown

    // Status Colors (Calibrated for High Contrast on Light Backgrounds)
    public static final Color STATUS_READY = new Color(0x18, 0x6E, 0x34);      // Deep Emerald Green
    public static final Color STATUS_ALMOST = new Color(0xB2, 0x59, 0x00);     // Warm Amber-Orange
    public static final Color STATUS_LOCKED = new Color(0xA3, 0x20, 0x20);     // Crimson Wine Red
    public static final Color STATUS_INFO = new Color(0x12, 0x64, 0x8F);       // Deep Slate Blue

    // Compact Card Borders (Optimized for 280px panel width)
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
    );

    public static final Border BORDER_CARD_SLIM = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)
    );

    public static final Border BORDER_GOLD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 1),
            BorderFactory.createEmptyBorder(6, 7, 6, 7)
    );

    // Clean Anti-Aliased System Typography (Properly Scaled for Expanded Wide Sidebar)
    public static Font getTitleFont() {
        try {
            return FontManager.getDefaultBoldFont().deriveFont(15.5f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.BOLD, 15);
        }
    }

    public static Font getHeaderFont() {
        try {
            return FontManager.getDefaultBoldFont().deriveFont(12.5f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.BOLD, 12);
        }
    }

    public static Font getSubheaderFont() {
        try {
            return FontManager.getDefaultBoldFont().deriveFont(12.0f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.BOLD, 12);
        }
    }

    public static Font getBoldFont() {
        try {
            return FontManager.getDefaultBoldFont().deriveFont(11.5f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.BOLD, 11);
        }
    }

    public static Font getRegularFont() {
        try {
            return FontManager.getDefaultFont().deriveFont(11.5f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.PLAIN, 11);
        }
    }

    public static Font getSmallFont() {
        try {
            return FontManager.getDefaultFont().deriveFont(10.5f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.PLAIN, 10);
        }
    }

    public static Font getBadgeFont() {
        try {
            return FontManager.getDefaultBoldFont().deriveFont(10.0f);
        } catch (Exception e) {
            return new Font("Segoe UI", Font.BOLD, 10);
        }
    }
}
