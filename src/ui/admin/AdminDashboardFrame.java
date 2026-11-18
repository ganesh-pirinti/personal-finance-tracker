package ui.admin;

import model.User;
import ui.components.StatusIndicator;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;

/**
 * Screen: Super Admin Portal Main Frame
 * Manages the 16-item administrative suite, top status bar, and card-managed views
 * for comprehensive application oversight, security, and JDBC architecture inspection.
 */
public class AdminDashboardFrame extends JFrame implements AdminNavPanel.AdminNavListener {
    public interface AdminPortalCallback {
        void onSwitchToUserDashboard(User adminUser);
        void onLogout();
    }

    private final User currentAdmin;
    private final AdminPortalCallback callback;

    private AdminNavPanel navPanel;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;

    // Admin Panels
    private AdminOverviewPanel overviewPanel;
    private UserManagementPanel usersPanel;
    private AuthRecoveryPanel authRecoveryPanel;
    private SessionManagementPanel sessionManagementPanel;
    private JdbcControlCenterPanel jdbcControlPanel;
    private JdbcArchitectureVisualizerPanel jdbcArchPanel;
    private DatabaseHealthSchemaPanel dbSchemaPanel;
    private SecurityMonitoringPanel securityMonitoringPanel;
    private AppearanceStudioPanel uiCustomizationPanel;
    private ContentFieldEditorPanel contentFieldPanel;
    private FinancialAnalyticsPanel analyticsReportsPanel;
    private AuditLogsPanel auditLogsPanel;
    private ApplicationSettingsPanel appSettingsPanel;
    private AdminProfilePanel adminProfilePanel;

    private StatusIndicator statusIndicator;

    public AdminDashboardFrame(User currentAdmin, AdminPortalCallback callback) {
        super("Personal Finance Tracker — Super Administrator Portal");
        this.currentAdmin = currentAdmin;
        this.callback = callback;

        initUI();
    }

    private void initUI() {
        setSize(1280, 840);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeColors.BG_MAIN);
        setLayout(new BorderLayout());

        // 1. LEFT SIDEBAR
        navPanel = new AdminNavPanel(currentAdmin, this);
        add(navPanel, BorderLayout.WEST);

        // 2. RIGHT CONTAINER (TOP BAR + CARD CONTENT)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(ThemeColors.BG_MAIN);

        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(1020, 56));
        topBar.setBackground(ThemeColors.HEADER_BG);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.CARD_BORDER));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        leftTop.setOpaque(false);
        JLabel lblPortalTitle = new JLabel("Enterprise System Control • " + (currentAdmin != null ? currentAdmin.getFullName() : "Administrator"));
        lblPortalTitle.setFont(FontManager.BODY_BOLD);
        lblPortalTitle.setForeground(ThemeColors.TEXT_MAIN);
        leftTop.add(lblPortalTitle);
        topBar.add(leftTop, BorderLayout.WEST);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 14));
        rightTop.setOpaque(false);

        JLabel lblBadge = new JLabel("SUPER ADMIN");
        lblBadge.setFont(FontManager.SMALL_BUTTON);
        lblBadge.setForeground(ThemeColors.PRIMARY);
        rightTop.add(lblBadge);

        statusIndicator = new StatusIndicator();
        rightTop.add(statusIndicator);

        topBar.add(rightTop, BorderLayout.EAST);
        rightContainer.add(topBar, BorderLayout.NORTH);

        // Card Content Area
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(ThemeColors.BG_MAIN);

        // Instantiate all 14 panels
        overviewPanel = new AdminOverviewPanel(currentAdmin, this::onNavSelected);
        usersPanel = new UserManagementPanel(currentAdmin);
        authRecoveryPanel = new AuthRecoveryPanel(currentAdmin);
        sessionManagementPanel = new SessionManagementPanel(currentAdmin);
        jdbcControlPanel = new JdbcControlCenterPanel(currentAdmin);
        jdbcArchPanel = new JdbcArchitectureVisualizerPanel(currentAdmin);
        dbSchemaPanel = new DatabaseHealthSchemaPanel(currentAdmin);
        securityMonitoringPanel = new SecurityMonitoringPanel(currentAdmin);
        uiCustomizationPanel = new AppearanceStudioPanel(currentAdmin);
        contentFieldPanel = new ContentFieldEditorPanel(currentAdmin);
        analyticsReportsPanel = new FinancialAnalyticsPanel(currentAdmin);
        auditLogsPanel = new AuditLogsPanel(currentAdmin);
        appSettingsPanel = new ApplicationSettingsPanel(currentAdmin);
        adminProfilePanel = new AdminProfilePanel(currentAdmin);

        contentCardPanel.add(overviewPanel, AdminNavPanel.NAV_OVERVIEW);
        contentCardPanel.add(usersPanel, AdminNavPanel.NAV_USERS);
        contentCardPanel.add(authRecoveryPanel, AdminNavPanel.NAV_AUTH_RECOVERY);
        contentCardPanel.add(sessionManagementPanel, AdminNavPanel.NAV_SESSIONS);
        contentCardPanel.add(jdbcControlPanel, AdminNavPanel.NAV_JDBC_CONTROL);
        contentCardPanel.add(jdbcArchPanel, AdminNavPanel.NAV_JDBC_ARCH);
        contentCardPanel.add(dbSchemaPanel, AdminNavPanel.NAV_DATABASE_SCHEMA);
        contentCardPanel.add(securityMonitoringPanel, AdminNavPanel.NAV_SECURITY_MONITOR);
        contentCardPanel.add(uiCustomizationPanel, AdminNavPanel.NAV_UI_CUSTOMIZATION);
        contentCardPanel.add(contentFieldPanel, AdminNavPanel.NAV_CONTENT_FIELDS);
        contentCardPanel.add(analyticsReportsPanel, AdminNavPanel.NAV_ANALYTICS);
        contentCardPanel.add(auditLogsPanel, AdminNavPanel.NAV_AUDIT);
        contentCardPanel.add(appSettingsPanel, AdminNavPanel.NAV_SETTINGS);
        contentCardPanel.add(adminProfilePanel, AdminNavPanel.NAV_PROFILE);

        rightContainer.add(contentCardPanel, BorderLayout.CENTER);
        add(rightContainer, BorderLayout.CENTER);

        // Asynchronously probe database connection to reflect real connectivity status
        new Thread(() -> {
            database.DatabaseConnection.ConnectionStatus status = database.DatabaseConnection.testConnection();
            SwingUtilities.invokeLater(() -> statusIndicator.setStatus(status));
        }).start();
    }

    @Override
    public void onNavSelected(String sectionName) {
        navPanel.setActiveNav(sectionName);
        cardLayout.show(contentCardPanel, sectionName);

        // Dynamically refresh panel state upon navigation
        if (AdminNavPanel.NAV_OVERVIEW.equals(sectionName)) {
            overviewPanel.refreshData();
        } else if (AdminNavPanel.NAV_USERS.equals(sectionName)) {
            usersPanel.refreshUsers();
        } else if (AdminNavPanel.NAV_AUTH_RECOVERY.equals(sectionName)) {
            authRecoveryPanel.refreshData();
        } else if (AdminNavPanel.NAV_SESSIONS.equals(sectionName)) {
            sessionManagementPanel.refreshData();
        } else if (AdminNavPanel.NAV_JDBC_CONTROL.equals(sectionName)) {
            jdbcControlPanel.runProbe();
        } else if (AdminNavPanel.NAV_DATABASE_SCHEMA.equals(sectionName)) {
            dbSchemaPanel.refreshCounts();
        } else if (AdminNavPanel.NAV_SECURITY_MONITOR.equals(sectionName)) {
            securityMonitoringPanel.refreshData();
        } else if (AdminNavPanel.NAV_ANALYTICS.equals(sectionName)) {
            analyticsReportsPanel.refreshAnalytics();
        } else if (AdminNavPanel.NAV_AUDIT.equals(sectionName)) {
            auditLogsPanel.refreshLogs();
        }
    }

    @Override
    public void onSwitchToUserApp() {
        if (callback != null) {
            callback.onSwitchToUserDashboard(currentAdmin);
        }
    }

    @Override
    public void onLogout() {
        if (callback != null) {
            callback.onLogout();
        }
    }
}
