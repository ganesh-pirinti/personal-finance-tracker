package ui;

import database.DatabaseConnection;
import model.User;
import service.AuthenticationService;
import service.BudgetService;
import service.ReportService;
import service.TransactionService;
import util.ThemeColors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Screen 3: Main Desktop Application Window built with Java Swing JFrame and AWT Layouts.
 * Houses the left navigation sidebar, top toolbar, and card-managed central views.
 */
public class DashboardFrame extends JFrame implements NavigationPanel.NavigationListener {
    public interface LogoutCallback {
        void onLogout();
    }

    public interface AdminPortalLauncher {
        void onOpenAdminPortal(User user);
    }

    private final LogoutCallback logoutCallback;
    private final AdminPortalLauncher adminPortalLauncher;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final ReportService reportService;
    private final AuthenticationService authService;

    private NavigationPanel navPanel;
    private TopBarPanel topBarPanel;
    private JPanel contentCardPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private BudgetPanel budgetPanel;
    private ReportsPanel reportsPanel;
    private ProjectOverviewPanel overviewPanel;
    private JdbcArchitecturePanel jdbcPanel;
    private DatabaseTablesPanel tablesPanel;
    private SettingsPanel settingsPanel;

    public DashboardFrame(LogoutCallback logoutCallback) {
        this(logoutCallback, null);
    }

    public DashboardFrame(LogoutCallback logoutCallback, AdminPortalLauncher adminPortalLauncher) {
        super("Personal Finance Tracker — Smart Wealth Suite");
        this.logoutCallback = logoutCallback;
        this.adminPortalLauncher = adminPortalLauncher;

        this.transactionService = new TransactionService();
        this.budgetService = new BudgetService();
        this.reportService = new ReportService();
        this.authService = new AuthenticationService();

        initUI();
    }

    private void initUI() {
        setSize(1200, 780);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeColors.BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Left Navigation Sidebar
        navPanel = new NavigationPanel(this, this);
        add(navPanel, BorderLayout.WEST);

        // 2. Right Container (Top Bar + Card Content)
        JPanel mainRightContainer = new JPanel(new BorderLayout());
        mainRightContainer.setBackground(ThemeColors.BG_MAIN);

        topBarPanel = new TopBarPanel();
        mainRightContainer.add(topBarPanel, BorderLayout.NORTH);

        // AWT CardLayout for Central Screen Views
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(ThemeColors.BG_MAIN);

        // Initialize Screen Panels
        dashboardPanel = new DashboardPanel(this, transactionService, this);
        transactionPanel = new TransactionPanel(this, transactionService);
        budgetPanel = new BudgetPanel(this, budgetService);
        reportsPanel = new ReportsPanel(this, reportService);
        overviewPanel = new ProjectOverviewPanel();
        jdbcPanel = new JdbcArchitecturePanel();
        tablesPanel = new DatabaseTablesPanel();
        settingsPanel = new SettingsPanel(this, authService);

        contentCardPanel.add(dashboardPanel, NavigationPanel.NAV_DASHBOARD);
        contentCardPanel.add(transactionPanel, NavigationPanel.NAV_TRANSACTIONS);
        contentCardPanel.add(budgetPanel, NavigationPanel.NAV_BUDGETS);
        contentCardPanel.add(reportsPanel, NavigationPanel.NAV_REPORTS);
        contentCardPanel.add(overviewPanel, NavigationPanel.NAV_PROJECT_OVERVIEW);
        contentCardPanel.add(jdbcPanel, NavigationPanel.NAV_JDBC_ARCHITECTURE);
        contentCardPanel.add(tablesPanel, NavigationPanel.NAV_DATABASE_TABLES);
        contentCardPanel.add(settingsPanel, NavigationPanel.NAV_SETTINGS);

        mainRightContainer.add(contentCardPanel, BorderLayout.CENTER);
        add(mainRightContainer, BorderLayout.CENTER);

        // Window Closing Listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // Trigger initial data load and background DB check
        checkDatabaseConnectivity();
        refreshCurrentView();
    }

    @Override
    public void onNavigate(String target) {
        if (NavigationPanel.NAV_ADMIN_PORTAL.equals(target)) {
            if (adminPortalLauncher != null) {
                adminPortalLauncher.onOpenAdminPortal(AuthenticationService.getCurrentUser());
            }
            return;
        }

        if (NavigationPanel.NAV_ADD_INCOME.equals(target)) {
            TransactionFormDialog dlg = new TransactionFormDialog(this, "INCOME", null, transactionService, () -> {
                refreshCurrentView();
            });
            dlg.setVisible(true);
            return;
        }

        if (NavigationPanel.NAV_ADD_EXPENSE.equals(target)) {
            TransactionFormDialog dlg = new TransactionFormDialog(this, "EXPENSE", null, transactionService, () -> {
                refreshCurrentView();
            });
            dlg.setVisible(true);
            return;
        }

        topBarPanel.setPageTitle(target);
        cardLayout.show(contentCardPanel, target);
        navPanel.setActiveItem(target);

        // Trigger view-specific data refresh
        if (NavigationPanel.NAV_DASHBOARD.equals(target)) {
            dashboardPanel.refreshData();
        } else if (NavigationPanel.NAV_TRANSACTIONS.equals(target)) {
            transactionPanel.refreshData();
        } else if (NavigationPanel.NAV_BUDGETS.equals(target)) {
            budgetPanel.refreshData();
        } else if (NavigationPanel.NAV_REPORTS.equals(target)) {
            reportsPanel.refreshData();
        } else if (NavigationPanel.NAV_JDBC_ARCHITECTURE.equals(target)) {
            jdbcPanel.runConnectionTest();
        } else if (NavigationPanel.NAV_DATABASE_TABLES.equals(target)) {
            tablesPanel.repaint();
        }
    }

    @Override
    public void onLogout() {
        AuthenticationService.logout();
        setVisible(false);
        dispose();
        if (logoutCallback != null) {
            logoutCallback.onLogout();
        }
    }

    public void refreshCurrentView() {
        dashboardPanel.refreshData();
        transactionPanel.refreshData();
        budgetPanel.refreshData();
        reportsPanel.refreshData();
    }

    public void checkDatabaseConnectivity() {
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
            SwingUtilities.invokeLater(() -> {
                topBarPanel.updateDatabaseStatus(status);
            });
        }).start();
    }
}
