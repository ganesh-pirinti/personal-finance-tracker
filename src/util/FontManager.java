package util;

import java.awt.Font;

/**
 * Standardized font styles for the Personal Finance Tracker application.
 * Supports dynamic font family updates through the Appearance Studio.
 */
public final class FontManager {
    private FontManager() {}

    private static String fontFamily = "Segoe UI";

    public static Font APP_TITLE = new Font(fontFamily, Font.BOLD, 22);
    public static Font SECTION_TITLE = new Font(fontFamily, Font.BOLD, 18);
    public static Font CARD_TITLE = new Font(fontFamily, Font.BOLD, 14);
    public static Font METRIC_AMOUNT = new Font(fontFamily, Font.BOLD, 24);
    public static Font SUB_METRIC = new Font(fontFamily, Font.PLAIN, 12);

    public static Font NAV_ITEM = new Font(fontFamily, Font.PLAIN, 13);
    public static Font NAV_ITEM_ACTIVE = new Font(fontFamily, Font.BOLD, 13);

    public static Font FORM_LABEL = new Font(fontFamily, Font.BOLD, 12);
    public static Font FORM_INPUT = new Font(fontFamily, Font.PLAIN, 13);
    public static Font BUTTON_TEXT = new Font(fontFamily, Font.BOLD, 13);
    public static Font SMALL_BUTTON = new Font(fontFamily, Font.BOLD, 11);

    public static Font TABLE_HEADER = new Font(fontFamily, Font.BOLD, 12);
    public static Font TABLE_CELL = new Font(fontFamily, Font.PLAIN, 12);
    public static Font TABLE_AMOUNT = new Font(fontFamily, Font.BOLD, 12);

    public static Font BODY = new Font(fontFamily, Font.PLAIN, 13);
    public static Font BODY_BOLD = new Font(fontFamily, Font.BOLD, 13);
    public static Font SMALL = new Font(fontFamily, Font.PLAIN, 11);
    public static Font CODE = new Font("Consolas", Font.PLAIN, 12);

    public static synchronized void updateFontFamily(String family) {
        if (family == null || family.trim().isEmpty()) return;
        fontFamily = family;

        APP_TITLE = new Font(fontFamily, Font.BOLD, 22);
        SECTION_TITLE = new Font(fontFamily, Font.BOLD, 18);
        CARD_TITLE = new Font(fontFamily, Font.BOLD, 14);
        METRIC_AMOUNT = new Font(fontFamily, Font.BOLD, 24);
        SUB_METRIC = new Font(fontFamily, Font.PLAIN, 12);

        NAV_ITEM = new Font(fontFamily, Font.PLAIN, 13);
        NAV_ITEM_ACTIVE = new Font(fontFamily, Font.BOLD, 13);

        FORM_LABEL = new Font(fontFamily, Font.BOLD, 12);
        FORM_INPUT = new Font(fontFamily, Font.PLAIN, 13);
        BUTTON_TEXT = new Font(fontFamily, Font.BOLD, 13);
        SMALL_BUTTON = new Font(fontFamily, Font.BOLD, 11);

        TABLE_HEADER = new Font(fontFamily, Font.BOLD, 12);
        TABLE_CELL = new Font(fontFamily, Font.PLAIN, 12);
        TABLE_AMOUNT = new Font(fontFamily, Font.BOLD, 12);

        BODY = new Font(fontFamily, Font.PLAIN, 13);
        BODY_BOLD = new Font(fontFamily, Font.BOLD, 13);
        SMALL = new Font(fontFamily, Font.PLAIN, 11);
    }

    public static String getFontFamily() {
        return fontFamily;
    }
}
