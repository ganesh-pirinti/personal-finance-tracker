package model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Model encapsulating the complete application theme and styling configuration.
 * Supports presets, hex serialization, live draft modifications, and global publishing.
 */
public class ThemeConfiguration implements Cloneable {
    public static final String PRESET_LIGHT_DEFAULT = "Light Default";
    public static final String PRESET_DARK_MODERN = "Dark Modern";
    public static final String PRESET_SLATE_EXECUTIVE = "Slate Executive";
    public static final String PRESET_EMERALD_FOREST = "Emerald Forest";

    private String name = PRESET_LIGHT_DEFAULT;
    private boolean isDark = false;

    // Palette Colors
    private Color primary = new Color(37, 99, 235);        // #2563EB
    private Color primaryHover = new Color(29, 78, 216);   // #1D4ED8
    private Color bgMain = new Color(245, 247, 250);       // #F5F7FA
    private Color surface = new Color(255, 255, 255);      // #FFFFFF
    private Color surfaceHover = new Color(241, 245, 249); // #F1F5F9
    private Color sidebarBg = new Color(255, 255, 255);    // #FFFFFF
    private Color headerBg = new Color(255, 255, 255);     // #FFFFFF

    private Color textMain = new Color(30, 41, 59);        // #1E293B
    private Color textMuted = new Color(100, 116, 139);    // #64748B
    private Color border = new Color(226, 232, 240);       // #E2E8F0

    private Color income = new Color(16, 185, 129);        // #10B981
    private Color expense = new Color(239, 68, 68);        // #EF4444
    private Color balance = new Color(14, 165, 233);       // #0EA5E9
    private Color warning = new Color(245, 158, 11);       // #F59E0B

    // Typography & Layout
    private String fontFamily = "Segoe UI";
    private int fontScalePercent = 100;
    private int cardCornerRadius = 16;
    private String layoutDensity = "COMFORTABLE"; // "COMFORTABLE" or "COMPACT"

    public ThemeConfiguration() {}

    public ThemeConfiguration(String name) {
        this.name = name;
    }

    public static ThemeConfiguration createDefaultLight() {
        ThemeConfiguration cfg = new ThemeConfiguration(PRESET_LIGHT_DEFAULT);
        cfg.isDark = false;
        cfg.primary = new Color(37, 99, 235);
        cfg.primaryHover = new Color(29, 78, 216);
        cfg.bgMain = new Color(245, 247, 250);
        cfg.surface = new Color(255, 255, 255);
        cfg.surfaceHover = new Color(241, 245, 249);
        cfg.sidebarBg = new Color(255, 255, 255);
        cfg.headerBg = new Color(255, 255, 255);
        cfg.textMain = new Color(30, 41, 59);
        cfg.textMuted = new Color(100, 116, 139);
        cfg.border = new Color(226, 232, 240);
        cfg.income = new Color(16, 185, 129);
        cfg.expense = new Color(239, 68, 68);
        cfg.balance = new Color(14, 165, 233);
        cfg.warning = new Color(245, 158, 11);
        cfg.fontFamily = "Segoe UI";
        cfg.cardCornerRadius = 16;
        return cfg;
    }

    public static ThemeConfiguration createDarkModern() {
        ThemeConfiguration cfg = new ThemeConfiguration(PRESET_DARK_MODERN);
        cfg.isDark = true;
        cfg.primary = new Color(59, 130, 246);        // #3B82F6
        cfg.primaryHover = new Color(96, 165, 250);   // #60A5FA
        cfg.bgMain = new Color(15, 23, 42);           // #0F172A
        cfg.surface = new Color(30, 41, 59);          // #1E293B
        cfg.surfaceHover = new Color(51, 65, 85);     // #334155
        cfg.sidebarBg = new Color(24, 32, 47);        // #18202F
        cfg.headerBg = new Color(30, 41, 59);         // #1E293B
        cfg.textMain = new Color(248, 250, 252);      // #F8FAFC
        cfg.textMuted = new Color(148, 163, 184);     // #94A3B8
        cfg.border = new Color(51, 65, 85);           // #334155
        cfg.income = new Color(52, 211, 153);         // #34D399
        cfg.expense = new Color(248, 113, 113);       // #F87171
        cfg.balance = new Color(56, 189, 248);        // #38BDF8
        cfg.warning = new Color(251, 191, 36);        // #FBBF24
        cfg.fontFamily = "Segoe UI";
        cfg.cardCornerRadius = 16;
        return cfg;
    }

    public static ThemeConfiguration createSlateExecutive() {
        ThemeConfiguration cfg = new ThemeConfiguration(PRESET_SLATE_EXECUTIVE);
        cfg.isDark = false;
        cfg.primary = new Color(71, 85, 105);         // Slate 600
        cfg.primaryHover = new Color(51, 65, 85);     // Slate 700
        cfg.bgMain = new Color(241, 245, 249);
        cfg.surface = new Color(255, 255, 255);
        cfg.surfaceHover = new Color(230, 235, 242);
        cfg.sidebarBg = new Color(248, 250, 252);
        cfg.headerBg = new Color(255, 255, 255);
        cfg.textMain = new Color(15, 23, 42);
        cfg.textMuted = new Color(100, 116, 139);
        cfg.border = new Color(203, 213, 225);
        cfg.income = new Color(22, 163, 74);
        cfg.expense = new Color(220, 38, 38);
        cfg.balance = new Color(2, 132, 199);
        cfg.warning = new Color(217, 119, 6);
        cfg.fontFamily = "SansSerif";
        cfg.cardCornerRadius = 12;
        return cfg;
    }

    public static ThemeConfiguration createEmeraldForest() {
        ThemeConfiguration cfg = new ThemeConfiguration(PRESET_EMERALD_FOREST);
        cfg.isDark = false;
        cfg.primary = new Color(5, 150, 105);         // Emerald 600
        cfg.primaryHover = new Color(4, 120, 87);     // Emerald 700
        cfg.bgMain = new Color(240, 253, 244);        // Mint tint
        cfg.surface = new Color(255, 255, 255);
        cfg.surfaceHover = new Color(220, 252, 231);
        cfg.sidebarBg = new Color(255, 255, 255);
        cfg.headerBg = new Color(255, 255, 255);
        cfg.textMain = new Color(6, 78, 59);          // Forest dark green
        cfg.textMuted = new Color(82, 110, 99);
        cfg.border = new Color(187, 247, 208);
        cfg.income = new Color(16, 185, 129);
        cfg.expense = new Color(239, 68, 68);
        cfg.balance = new Color(13, 148, 136);
        cfg.warning = new Color(217, 119, 6);
        cfg.fontFamily = "Segoe UI";
        cfg.cardCornerRadius = 18;
        return cfg;
    }

    // Helper conversion utilities
    public static String toHex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static Color fromHex(String hex, Color fallback) {
        if (hex == null || hex.trim().isEmpty()) return fallback;
        String clean = hex.trim().replace("#", "");
        if (clean.length() == 6) {
            try {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            } catch (NumberFormatException ignored) {}
        }
        return fallback;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new HashMap<>();
        m.put("theme_name", name);
        m.put("is_dark", String.valueOf(isDark));
        m.put("color_primary", toHex(primary));
        m.put("color_primary_hover", toHex(primaryHover));
        m.put("color_bg_main", toHex(bgMain));
        m.put("color_surface", toHex(surface));
        m.put("color_surface_hover", toHex(surfaceHover));
        m.put("color_sidebar_bg", toHex(sidebarBg));
        m.put("color_header_bg", toHex(headerBg));
        m.put("color_text_main", toHex(textMain));
        m.put("color_text_muted", toHex(textMuted));
        m.put("color_border", toHex(border));
        m.put("color_income", toHex(income));
        m.put("color_expense", toHex(expense));
        m.put("color_balance", toHex(balance));
        m.put("color_warning", toHex(warning));
        m.put("font_family", fontFamily);
        m.put("font_scale_percent", String.valueOf(fontScalePercent));
        m.put("card_corner_radius", String.valueOf(cardCornerRadius));
        m.put("layout_density", layoutDensity);
        return m;
    }

    public static ThemeConfiguration fromMap(Map<String, String> m) {
        ThemeConfiguration cfg = createDefaultLight();
        if (m == null || m.isEmpty()) return cfg;

        if (m.containsKey("theme_name")) cfg.setName(m.get("theme_name"));
        if (m.containsKey("is_dark")) cfg.setDark(Boolean.parseBoolean(m.get("is_dark")));
        if (m.containsKey("color_primary")) cfg.setPrimary(fromHex(m.get("color_primary"), cfg.primary));
        if (m.containsKey("color_primary_hover")) cfg.setPrimaryHover(fromHex(m.get("color_primary_hover"), cfg.primaryHover));
        if (m.containsKey("color_bg_main")) cfg.setBgMain(fromHex(m.get("color_bg_main"), cfg.bgMain));
        if (m.containsKey("color_surface")) cfg.setSurface(fromHex(m.get("color_surface"), cfg.surface));
        if (m.containsKey("color_surface_hover")) cfg.setSurfaceHover(fromHex(m.get("color_surface_hover"), cfg.surfaceHover));
        if (m.containsKey("color_sidebar_bg")) cfg.setSidebarBg(fromHex(m.get("color_sidebar_bg"), cfg.sidebarBg));
        if (m.containsKey("color_header_bg")) cfg.setHeaderBg(fromHex(m.get("color_header_bg"), cfg.headerBg));
        if (m.containsKey("color_text_main")) cfg.setTextMain(fromHex(m.get("color_text_main"), cfg.textMain));
        if (m.containsKey("color_text_muted")) cfg.setTextMuted(fromHex(m.get("color_text_muted"), cfg.textMuted));
        if (m.containsKey("color_border")) cfg.setBorder(fromHex(m.get("color_border"), cfg.border));
        if (m.containsKey("color_income")) cfg.setIncome(fromHex(m.get("color_income"), cfg.income));
        if (m.containsKey("color_expense")) cfg.setExpense(fromHex(m.get("color_expense"), cfg.expense));
        if (m.containsKey("color_balance")) cfg.setBalance(fromHex(m.get("color_balance"), cfg.balance));
        if (m.containsKey("color_warning")) cfg.setWarning(fromHex(m.get("color_warning"), cfg.warning));
        if (m.containsKey("font_family")) cfg.setFontFamily(m.get("font_family"));
        if (m.containsKey("layout_density")) cfg.setLayoutDensity(m.get("layout_density"));
        try {
            if (m.containsKey("font_scale_percent")) cfg.setFontScalePercent(Integer.parseInt(m.get("font_scale_percent")));
            if (m.containsKey("card_corner_radius")) cfg.setCardCornerRadius(Integer.parseInt(m.get("card_corner_radius")));
        } catch (NumberFormatException ignored) {}

        return cfg;
    }

    @Override
    public ThemeConfiguration clone() {
        try {
            return (ThemeConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            ThemeConfiguration c = new ThemeConfiguration(this.name);
            c.setDark(this.isDark);
            c.setPrimary(this.primary);
            c.setPrimaryHover(this.primaryHover);
            c.setBgMain(this.bgMain);
            c.setSurface(this.surface);
            c.setSurfaceHover(this.surfaceHover);
            c.setSidebarBg(this.sidebarBg);
            c.setHeaderBg(this.headerBg);
            c.setTextMain(this.textMain);
            c.setTextMuted(this.textMuted);
            c.setBorder(this.border);
            c.setIncome(this.income);
            c.setExpense(this.expense);
            c.setBalance(this.balance);
            c.setWarning(this.warning);
            c.setFontFamily(this.fontFamily);
            c.setFontScalePercent(this.fontScalePercent);
            c.setCardCornerRadius(this.cardCornerRadius);
            c.setLayoutDensity(this.layoutDensity);
            return c;
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isDark() { return isDark; }
    public void setDark(boolean dark) { isDark = dark; }

    public Color getPrimary() { return primary; }
    public void setPrimary(Color primary) { this.primary = primary; }

    public Color getPrimaryHover() { return primaryHover; }
    public void setPrimaryHover(Color primaryHover) { this.primaryHover = primaryHover; }

    public Color getBgMain() { return bgMain; }
    public void setBgMain(Color bgMain) { this.bgMain = bgMain; }

    public Color getSurface() { return surface; }
    public void setSurface(Color surface) { this.surface = surface; }

    public Color getSurfaceHover() { return surfaceHover; }
    public void setSurfaceHover(Color surfaceHover) { this.surfaceHover = surfaceHover; }

    public Color getSidebarBg() { return sidebarBg; }
    public void setSidebarBg(Color sidebarBg) { this.sidebarBg = sidebarBg; }

    public Color getHeaderBg() { return headerBg; }
    public void setHeaderBg(Color headerBg) { this.headerBg = headerBg; }

    public Color getTextMain() { return textMain; }
    public void setTextMain(Color textMain) { this.textMain = textMain; }

    public Color getTextMuted() { return textMuted; }
    public void setTextMuted(Color textMuted) { this.textMuted = textMuted; }

    public Color getBorder() { return border; }
    public void setBorder(Color border) { this.border = border; }

    public Color getIncome() { return income; }
    public void setIncome(Color income) { this.income = income; }

    public Color getExpense() { return expense; }
    public void setExpense(Color expense) { this.expense = expense; }

    public Color getBalance() { return balance; }
    public void setBalance(Color balance) { this.balance = balance; }

    public Color getWarning() { return warning; }
    public void setWarning(Color warning) { this.warning = warning; }

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

    public int getFontScalePercent() { return fontScalePercent; }
    public void setFontScalePercent(int fontScalePercent) { this.fontScalePercent = fontScalePercent; }

    public int getCardCornerRadius() { return cardCornerRadius; }
    public void setCardCornerRadius(int cardCornerRadius) { this.cardCornerRadius = cardCornerRadius; }

    public String getLayoutDensity() { return layoutDensity; }
    public void setLayoutDensity(String layoutDensity) { this.layoutDensity = layoutDensity; }
}
