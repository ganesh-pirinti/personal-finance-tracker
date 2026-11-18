package dao;

import database.DatabaseConnection;
import model.ApplicationSetting;
import model.ThemeConfiguration;
import service.AuthenticationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for persisting Application Settings, Dynamic UI Text, and Theme Versions.
 */
public class ApplicationSettingsDAO {
    // In-memory fallback stores
    private static final Map<String, ApplicationSetting> memorySettings = new HashMap<>();
    private static ThemeConfiguration activeMemoryTheme = ThemeConfiguration.createDefaultLight();
    private static final List<ThemeConfiguration> memoryThemeHistory = new ArrayList<>();

    static {
        resetMemoryStore();
    }

    public static synchronized void resetMemoryStore() {
        memorySettings.clear();
        seedDefaultSetting("app.name", "Personal Finance Tracker", "BRANDING", "Global application name displayed in headers");
        seedDefaultSetting("app.subtitle", "Smart Income, Expense & Budget Management", "BRANDING", "Global application subtitle");
        seedDefaultSetting("login.hero_title", "Personal Wealth Suite", "AUTH", "Heading on login screen hero banner");
        seedDefaultSetting("login.hero_subtitle", "Secure Financial Intelligence & Budget Control", "AUTH", "Subtitle on login screen hero banner");
        seedDefaultSetting("dashboard.welcome_prefix", "Welcome back,", "DASHBOARD", "Greeting prefix on user dashboard");
        seedDefaultSetting("currency.symbol", "₹", "BRANDING", "Currency symbol for monetary display");
        seedDefaultSetting("field.mobile_required", "false", "FORMS", "Whether mobile number is strictly required at registration");
        seedDefaultSetting("field.dob_required", "false", "FORMS", "Whether date of birth is required at registration");
        seedDefaultSetting("field.default_category", "Food", "FORMS", "Default category preselected in transaction modal");
        seedDefaultSetting("field.default_tx_type", "EXPENSE", "FORMS", "Default transaction type (INCOME or EXPENSE)");

        activeMemoryTheme = ThemeConfiguration.createDefaultLight();
        memoryThemeHistory.clear();
        memoryThemeHistory.add(activeMemoryTheme.clone());
    }

    private static void seedDefaultSetting(String key, String val, String cat, String desc) {
        memorySettings.put(key, new ApplicationSetting(key, val, cat, desc, LocalDateTime.now(), 1));
    }

    // =========================================================================
    // UI TEXT & APP SETTINGS PERSISTENCE
    // =========================================================================

    public void saveSetting(ApplicationSetting setting) throws SQLException {
        if (setting == null || setting.getSettingKey() == null) return;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySettings) {
                setting.setUpdatedAt(LocalDateTime.now());
                memorySettings.put(setting.getSettingKey(), setting);
            }
            return;
        }

        try {
            String sql = "INSERT INTO ui_text_settings (text_key, text_value, category, description, updated_at, updated_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE text_value = VALUES(text_value), category = VALUES(category), " +
                         "description = VALUES(description), updated_at = VALUES(updated_at), updated_by = VALUES(updated_by)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, setting.getSettingKey());
                ps.setString(2, setting.getSettingValue());
                ps.setString(3, setting.getCategory());
                ps.setString(4, setting.getDescription());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(6, setting.getUpdatedBy());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            synchronized (memorySettings) {
                setting.setUpdatedAt(LocalDateTime.now());
                memorySettings.put(setting.getSettingKey(), setting);
            }
            return;
        }

        synchronized (memorySettings) {
            setting.setUpdatedAt(LocalDateTime.now());
            memorySettings.put(setting.getSettingKey(), setting);
        }
    }

    public List<ApplicationSetting> getAllSettings() {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySettings) {
                return new ArrayList<>(memorySettings.values());
            }
        }

        List<ApplicationSetting> list = new ArrayList<>();
        try {
            String sql = "SELECT text_key, text_value, category, description, updated_at, updated_by " +
                         "FROM ui_text_settings ORDER BY category, text_key";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    ApplicationSetting s = new ApplicationSetting();
                    s.setSettingKey(rs.getString("text_key"));
                    s.setSettingValue(rs.getString("text_value"));
                    s.setCategory(rs.getString("category"));
                    s.setDescription(rs.getString("description"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    if (ts != null) s.setUpdatedAt(ts.toLocalDateTime());
                    s.setUpdatedBy(rs.getInt("updated_by"));
                    list.add(s);
                }
                if (!list.isEmpty()) return list;
            }
        } catch (SQLException ignored) {}

        synchronized (memorySettings) {
            list.addAll(memorySettings.values());
        }
        return list;
    }

    public String getSettingValue(String key, String fallback) {
        if (key == null) return fallback;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySettings) {
                ApplicationSetting s = memorySettings.get(key);
                if (s != null && s.getSettingValue() != null) {
                    return s.getSettingValue();
                }
            }
            return fallback;
        }

        try {
            String sql = "SELECT text_value FROM ui_text_settings WHERE text_key = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String val = rs.getString(1);
                        if (val != null) return val;
                    }
                }
            }
        } catch (SQLException ignored) {}

        synchronized (memorySettings) {
            ApplicationSetting s = memorySettings.get(key);
            if (s != null && s.getSettingValue() != null) {
                return s.getSettingValue();
            }
        }
        return fallback;
    }

    // =========================================================================
    // THEME CONFIGURATION PERSISTENCE
    // =========================================================================

    public void saveTheme(ThemeConfiguration theme, boolean isActive, int actorUserId) throws SQLException {
        if (theme == null) return;

        if (AuthenticationService.isDemoMode()) {
            if (isActive) {
                activeMemoryTheme = theme.clone();
            }
            memoryThemeHistory.add(theme.clone());
            return;
        }

        try {
            if (isActive) {
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("UPDATE theme_versions SET is_active = FALSE");
                }
            }

            String sql = "INSERT INTO theme_versions (theme_name, is_dark, color_primary, color_primary_hover, " +
                         "color_bg_main, color_surface, color_surface_hover, color_sidebar_bg, color_header_bg, " +
                         "color_text_main, color_text_muted, color_border, color_income, color_expense, " +
                         "color_balance, color_warning, font_family, font_scale_percent, card_corner_radius, " +
                         "layout_density, is_active, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, theme.getName());
                ps.setBoolean(2, theme.isDark());
                ps.setString(3, ThemeConfiguration.toHex(theme.getPrimary()));
                ps.setString(4, ThemeConfiguration.toHex(theme.getPrimaryHover()));
                ps.setString(5, ThemeConfiguration.toHex(theme.getBgMain()));
                ps.setString(6, ThemeConfiguration.toHex(theme.getSurface()));
                ps.setString(7, ThemeConfiguration.toHex(theme.getSurfaceHover()));
                ps.setString(8, ThemeConfiguration.toHex(theme.getSidebarBg()));
                ps.setString(9, ThemeConfiguration.toHex(theme.getHeaderBg()));
                ps.setString(10, ThemeConfiguration.toHex(theme.getTextMain()));
                ps.setString(11, ThemeConfiguration.toHex(theme.getTextMuted()));
                ps.setString(12, ThemeConfiguration.toHex(theme.getBorder()));
                ps.setString(13, ThemeConfiguration.toHex(theme.getIncome()));
                ps.setString(14, ThemeConfiguration.toHex(theme.getExpense()));
                ps.setString(15, ThemeConfiguration.toHex(theme.getBalance()));
                ps.setString(16, ThemeConfiguration.toHex(theme.getWarning()));
                ps.setString(17, theme.getFontFamily());
                ps.setInt(18, theme.getFontScalePercent());
                ps.setInt(19, theme.getCardCornerRadius());
                ps.setString(20, theme.getLayoutDensity());
                ps.setBoolean(21, isActive);
                ps.setInt(22, actorUserId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (isActive) {
                activeMemoryTheme = theme.clone();
            }
            memoryThemeHistory.add(theme.clone());
            return;
        }

        if (isActive) {
            activeMemoryTheme = theme.clone();
        }
        memoryThemeHistory.add(theme.clone());
    }

    public ThemeConfiguration getActiveTheme() {
        if (AuthenticationService.isDemoMode()) {
            return activeMemoryTheme.clone();
        }

        try {
            String sql = "SELECT * FROM theme_versions WHERE is_active = TRUE ORDER BY created_at DESC LIMIT 1";
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    ThemeConfiguration cfg = new ThemeConfiguration();
                    cfg.setName(rs.getString("theme_name"));
                    cfg.setDark(rs.getBoolean("is_dark"));
                    cfg.setPrimary(ThemeConfiguration.fromHex(rs.getString("color_primary"), cfg.getPrimary()));
                    cfg.setPrimaryHover(ThemeConfiguration.fromHex(rs.getString("color_primary_hover"), cfg.getPrimaryHover()));
                    cfg.setBgMain(ThemeConfiguration.fromHex(rs.getString("color_bg_main"), cfg.getBgMain()));
                    cfg.setSurface(ThemeConfiguration.fromHex(rs.getString("color_surface"), cfg.getSurface()));
                    cfg.setSurfaceHover(ThemeConfiguration.fromHex(rs.getString("color_surface_hover"), cfg.getSurfaceHover()));
                    cfg.setSidebarBg(ThemeConfiguration.fromHex(rs.getString("color_sidebar_bg"), cfg.getSidebarBg()));
                    cfg.setHeaderBg(ThemeConfiguration.fromHex(rs.getString("color_header_bg"), cfg.getHeaderBg()));
                    cfg.setTextMain(ThemeConfiguration.fromHex(rs.getString("color_text_main"), cfg.getTextMain()));
                    cfg.setTextMuted(ThemeConfiguration.fromHex(rs.getString("color_text_muted"), cfg.getTextMuted()));
                    cfg.setBorder(ThemeConfiguration.fromHex(rs.getString("color_border"), cfg.getBorder()));
                    cfg.setIncome(ThemeConfiguration.fromHex(rs.getString("color_income"), cfg.getIncome()));
                    cfg.setExpense(ThemeConfiguration.fromHex(rs.getString("color_expense"), cfg.getExpense()));
                    cfg.setBalance(ThemeConfiguration.fromHex(rs.getString("color_balance"), cfg.getBalance()));
                    cfg.setWarning(ThemeConfiguration.fromHex(rs.getString("color_warning"), cfg.getWarning()));
                    cfg.setFontFamily(rs.getString("font_family"));
                    cfg.setFontScalePercent(rs.getInt("font_scale_percent"));
                    cfg.setCardCornerRadius(rs.getInt("card_corner_radius"));
                    cfg.setLayoutDensity(rs.getString("layout_density"));
                    activeMemoryTheme = cfg.clone();
                    return cfg;
                }
            }
        } catch (SQLException ignored) {}

        return activeMemoryTheme.clone();
    }
}
