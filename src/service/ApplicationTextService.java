package service;

import dao.ApplicationSettingsDAO;
import model.ApplicationSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized service managing dynamic UI text, branding strings, and form field settings.
 * Includes in-memory caching and safe fallback resolution.
 */
public class ApplicationTextService {
    private static final ApplicationTextService INSTANCE = new ApplicationTextService();
    private final ApplicationSettingsDAO settingsDAO;
    private final Map<String, String> cache = new HashMap<>();

    public ApplicationTextService() {
        this.settingsDAO = new ApplicationSettingsDAO();
        refreshCache();
    }

    public static ApplicationTextService getInstance() {
        return INSTANCE;
    }

    public synchronized void refreshCache() {
        cache.clear();
        List<ApplicationSetting> all = settingsDAO.getAllSettings();
        for (ApplicationSetting s : all) {
            cache.put(s.getSettingKey(), s.getSettingValue());
        }
    }

    /**
     * Retrieves dynamic text for the given key, or returns the fallback if unset.
     */
    public synchronized String getText(String key, String fallback) {
        if (key == null) return fallback;
        if (cache.containsKey(key)) {
            String val = cache.get(key);
            if (val != null && !val.trim().isEmpty()) return val;
        }
        return fallback;
    }

    public synchronized void setText(String key, String value, String category, String description, int actorUserId) {
        try {
            ApplicationSetting setting = new ApplicationSetting(key, value, category, description);
            setting.setUpdatedBy(actorUserId);
            settingsDAO.saveSetting(setting);
            cache.put(key, value);
        } catch (Exception e) {
            cache.put(key, value);
        }
    }

    public List<ApplicationSetting> getAllSettings() {
        return settingsDAO.getAllSettings();
    }

    public synchronized void resetToDefaults(int actorUserId) {
        setText("app.name", "Personal Finance Tracker", "BRANDING", "Global application name displayed in headers", actorUserId);
        setText("app.subtitle", "Smart Income, Expense & Budget Management", "BRANDING", "Global application subtitle", actorUserId);
        setText("login.hero_title", "Personal Wealth Suite", "AUTH", "Heading on login screen hero banner", actorUserId);
        setText("login.hero_subtitle", "Secure Financial Intelligence & Budget Control", "AUTH", "Subtitle on login screen hero banner", actorUserId);
        setText("dashboard.welcome_prefix", "Welcome back,", "DASHBOARD", "Greeting prefix on user dashboard", actorUserId);
        setText("currency.symbol", "₹", "BRANDING", "Currency symbol for monetary display", actorUserId);
        setText("field.mobile_required", "false", "FORMS", "Whether mobile number is strictly required at registration", actorUserId);
        setText("field.dob_required", "false", "FORMS", "Whether date of birth is required at registration", actorUserId);
        setText("field.default_category", "Food", "FORMS", "Default category preselected in transaction modal", actorUserId);
        setText("field.default_tx_type", "EXPENSE", "FORMS", "Default transaction type (INCOME or EXPENSE)", actorUserId);
        refreshCache();
    }
}
