package app;

import database.DatabaseInitializer;
import model.User;
import service.AuthenticationService;
import ui.DashboardFrame;
import ui.LoginFrame;
import ui.RegisterFrame;
import ui.admin.AdminDashboardFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Main application controller managing Swing window transitions, user session state,
 * role-based portal routing, and database bootstrapping on the Swing Event Dispatch Thread.
 */
public class Application {
    private LoginFrame loginFrame;
    private RegisterFrame registerFrame;
    private DashboardFrame dashboardFrame;
    private AdminDashboardFrame adminDashboardFrame;

    public void start() {
        // Asynchronously bootstrap database in background to avoid blocking Swing startup
        new Thread(() -> {
            boolean ok = DatabaseInitializer.initialize();
            if (ok) {
                System.out.println("✓ Database & schema initialization verified successfully.");
            } else {
                System.out.println("ℹ Database is not currently reachable. The application will operate with live diagnostic indicators.");
            }
        }).start();

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(this::showLogin);
    }

    public void showLogin() {
        closeAllFrames();

        loginFrame = new LoginFrame(new LoginFrame.AuthSuccessCallback() {
            @Override
            public void onLoginSuccess(User user) {
                AuthenticationService.setCurrentUser(user);

                if (user.isAdmin()) {
                    String[] options = {"★ Super Admin Portal", "Personal Finance Dashboard"};
                    int choice = JOptionPane.showOptionDialog(
                        loginFrame,
                        "Welcome back, " + user.getFullName() + "!\nYou have Administrator privileges. Which view would you like to enter?",
                        "Administrator Session Detected",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                    );

                    if (choice == 0) {
                        showAdminDashboard(user);
                        return;
                    }
                }
                showDashboard(user);
            }

            @Override
            public void onOpenRegister() {
                showRegister();
            }
        });
        loginFrame.setVisible(true);
    }

    public void showRegister() {
        closeAllFrames();

        registerFrame = new RegisterFrame(new RegisterFrame.RegisterCallback() {
            @Override
            public void onBackToLogin() {
                showLogin();
            }

            @Override
            public void onRegisterSuccess(User user) {
                showLogin();
            }
        });
        registerFrame.setVisible(true);
    }

    public void showDashboard(User user) {
        closeAllFrames();
        AuthenticationService.setCurrentUser(user);

        dashboardFrame = new DashboardFrame(
            this::showLogin,
            this::showAdminDashboard
        );
        dashboardFrame.setVisible(true);
    }

    public void showAdminDashboard(User adminUser) {
        if (!adminUser.isAdmin()) {
            JOptionPane.showMessageDialog(null,
                "Access Denied: You do not have administrator permissions.",
                "Security Alert", JOptionPane.ERROR_MESSAGE);
            showDashboard(adminUser);
            return;
        }

        closeAllFrames();
        AuthenticationService.setCurrentUser(adminUser);

        adminDashboardFrame = new AdminDashboardFrame(adminUser, new AdminDashboardFrame.AdminPortalCallback() {
            @Override
            public void onSwitchToUserDashboard(User user) {
                showDashboard(user);
            }

            @Override
            public void onLogout() {
                AuthenticationService.logout();
                showLogin();
            }
        });
        adminDashboardFrame.setVisible(true);
    }

    private void closeAllFrames() {
        if (loginFrame != null) {
            loginFrame.setVisible(false);
            loginFrame.dispose();
            loginFrame = null;
        }
        if (registerFrame != null) {
            registerFrame.setVisible(false);
            registerFrame.dispose();
            registerFrame = null;
        }
        if (dashboardFrame != null) {
            dashboardFrame.setVisible(false);
            dashboardFrame.dispose();
            dashboardFrame = null;
        }
        if (adminDashboardFrame != null) {
            adminDashboardFrame.setVisible(false);
            adminDashboardFrame.dispose();
            adminDashboardFrame = null;
        }
    }
}
