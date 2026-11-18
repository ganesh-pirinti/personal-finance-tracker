package service;

import dao.ApplicationSettingsDAO;
import model.ThemeConfiguration;
import util.FontManager;
import util.ThemeColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing live application styling, theme publishing, and live UI updates.
 */
public class ThemeConfigurationService {
    public interface ThemeChangeListener {
        void onThemeChanged(ThemeConfiguration newTheme);
    }

    private static final ThemeConfigurationService INSTANCE = new ThemeConfigurationService();
    private final ApplicationSettingsDAO settingsDAO;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    private ThemeConfiguration currentActiveTheme;

    public ThemeConfigurationService() {
        this.settingsDAO = new ApplicationSettingsDAO();
        this.currentActiveTheme = settingsDAO.getActiveTheme();
        applyThemeGlobally(this.currentActiveTheme);
    }

    public static ThemeConfigurationService getInstance() {
        return INSTANCE;
    }

    public synchronized ThemeConfiguration getActiveTheme() {
        if (currentActiveTheme == null) {
            currentActiveTheme = settingsDAO.getActiveTheme();
        }
        return currentActiveTheme.clone();
    }

    public synchronized void publishTheme(ThemeConfiguration theme, int actorUserId) {
        if (theme == null) return;
        try {
            settingsDAO.saveTheme(theme, true, actorUserId);
        } catch (Exception e) {
            System.err.println("Could not persist theme to database: " + e.getMessage());
        }
        this.currentActiveTheme = theme.clone();
        applyThemeGlobally(this.currentActiveTheme);
        notifyListeners(this.currentActiveTheme);
    }

    public synchronized void resetToDefault(int actorUserId) {
        ThemeConfiguration defaultLight = ThemeConfiguration.createDefaultLight();
        publishTheme(defaultLight, actorUserId);
    }

    public synchronized void addThemeChangeListener(ThemeChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(ThemeConfiguration theme) {
        for (ThemeChangeListener l : new ArrayList<>(listeners)) {
            try {
                l.onThemeChanged(theme);
            } catch (Exception e) {
                System.err.println("Error in theme change listener: " + e.getMessage());
            }
        }
    }

    private void applyThemeGlobally(ThemeConfiguration t) {
        ThemeColors.updateColors(
            t.getPrimary(), t.getPrimaryHover(), t.getBgMain(),
            t.getSurface(), t.getSurfaceHover(), t.getSidebarBg(),
            t.getHeaderBg(), t.getTextMain(), t.getTextMuted(),
            t.getBorder(), t.getIncome(), t.getExpense(),
            t.getBalance(), t.getWarning()
        );
        FontManager.updateFontFamily(t.getFontFamily());
    }
}
