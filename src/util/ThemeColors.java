package util;

import java.awt.Color;

/**
 * Design system color constants and dynamic runtime theme palette for the Personal Finance Tracker.
 * Supports live modifications from the Appearance & Branding Studio.
 */
public final class ThemeColors {
    private ThemeColors() {} // Prevent instantiation

    // Backgrounds & Surfaces (Mutable to support live Super Admin dynamic theming)
    public static Color BG_MAIN = new Color(245, 247, 250);        // Very light blue-grey
    public static Color SURFACE = new Color(255, 255, 255);        // Pure white card/panel background
    public static Color SURFACE_HOVER = new Color(241, 245, 249);  // Subtle hover surface
    public static Color HOVER = SURFACE_HOVER;
    public static Color SIDEBAR_BG = new Color(255, 255, 255);     // Clean sidebar
    public static Color HEADER_BG = new Color(255, 255, 255);      // Top bar background

    // Primary Accents
    public static Color PRIMARY = new Color(37, 99, 235);          // Professional Royal Blue
    public static Color PRIMARY_HOVER = new Color(29, 78, 216);    // Darker blue
    public static Color PRIMARY_LIGHT = new Color(239, 246, 255);  // Soft blue tint
    public static Color PRIMARY_TEXT = new Color(29, 78, 216);
    public static Color ACCENT = PRIMARY;

    // Financial Indicators
    public static Color INCOME = new Color(16, 185, 129);          // Emerald Green
    public static Color INCOME_LIGHT = new Color(236, 253, 245);   // Soft green background
    public static Color INCOME_HOVER = new Color(5, 150, 105);
    public static Color EXPENSE = new Color(239, 68, 68);          // Coral / Crimson Red
    public static Color EXPENSE_LIGHT = new Color(254, 242, 242);  // Soft red background
    public static Color EXPENSE_HOVER = new Color(220, 38, 38);
    public static Color BALANCE = new Color(14, 165, 233);         // Sky Blue
    public static Color BALANCE_LIGHT = new Color(240, 249, 255);

    // Warnings & Accents
    public static Color WARNING = new Color(245, 158, 11);         // Warm amber
    public static Color WARNING_LIGHT = new Color(254, 243, 199);
    public static Color INFO = new Color(99, 102, 241);            // Indigo

    // Typography
    public static Color TEXT_MAIN = new Color(30, 41, 59);         // Deep charcoal/slate
    public static Color TEXT_MUTED = new Color(100, 116, 139);     // Muted grey
    public static Color TEXT_LIGHT = new Color(148, 163, 184);     // Subtitle placeholder
    public static Color TEXT_WHITE = new Color(255, 255, 255);

    // Borders & Dividers
    public static Color BORDER = new Color(226, 232, 240);         // Subtle blue-grey border
    public static Color CARD_BORDER = BORDER;
    public static Color BORDER_FOCUS = new Color(147, 197, 253);   // Input focus border
    public static Color DIVIDER = new Color(241, 245, 249);

    // Chart Palette (distinct harmonious colors for expense categories)
    public static Color[] CHART_COLORS = {
        new Color(59, 130, 246),  // Food (Blue)
        new Color(16, 185, 129),  // Travel (Green)
        new Color(245, 158, 11),  // Education (Amber)
        new Color(236, 72, 153),  // Shopping (Pink)
        new Color(139, 92, 246),  // Bills (Purple)
        new Color(20, 184, 166),  // Entertainment (Teal)
        new Color(249, 115, 22),  // Healthcare (Orange)
        new Color(107, 114, 128)  // Other (Slate)
    };

    public static synchronized void updateColors(Color primary, Color primaryHover, Color bgMain,
                                                 Color surface, Color surfaceHover, Color sidebarBg,
                                                 Color headerBg, Color textMain, Color textMuted,
                                                 Color border, Color income, Color expense,
                                                 Color balance, Color warning) {
        if (primary != null) {
            PRIMARY = primary;
            PRIMARY_TEXT = primary;
            PRIMARY_LIGHT = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 30);
        }
        if (primaryHover != null) PRIMARY_HOVER = primaryHover;
        if (bgMain != null) BG_MAIN = bgMain;
        if (surface != null) SURFACE = surface;
        if (surfaceHover != null) SURFACE_HOVER = surfaceHover;
        if (sidebarBg != null) SIDEBAR_BG = sidebarBg;
        if (headerBg != null) HEADER_BG = headerBg;
        if (textMain != null) TEXT_MAIN = textMain;
        if (textMuted != null) TEXT_MUTED = textMuted;
        if (border != null) {
            BORDER = border;
            DIVIDER = border;
        }
        if (income != null) {
            INCOME = income;
            INCOME_LIGHT = new Color(income.getRed(), income.getGreen(), income.getBlue(), 30);
        }
        if (expense != null) {
            EXPENSE = expense;
            EXPENSE_LIGHT = new Color(expense.getRed(), expense.getGreen(), expense.getBlue(), 30);
        }
        if (balance != null) {
            BALANCE = balance;
            BALANCE_LIGHT = new Color(balance.getRed(), balance.getGreen(), balance.getBlue(), 30);
        }
        if (warning != null) {
            WARNING = warning;
            WARNING_LIGHT = new Color(warning.getRed(), warning.getGreen(), warning.getBlue(), 30);
        }
    }
}
