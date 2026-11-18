package ui;

import database.DatabaseConnection;
import model.User;
import service.AdminManagementService;
import service.AuthenticationService;
import ui.components.CustomButton;
import ui.components.StatusIndicator;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;

/**
 * Screen 1: Welcome & Login built with Java Swing JFrame and AWT Layouts.
 * Features a modern two-column desktop presentation with custom branding and credential validation.
 */
public class LoginFrame extends JFrame {
    public interface AuthSuccessCallback {
        void onLoginSuccess(User user);
        void onOpenRegister();
    }

    private final AuthenticationService authService;
    private final AdminManagementService adminService;
    private final AuthSuccessCallback callback;

    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JLabel lblError;
    private StatusIndicator statusIndicator;

    public JTextField getEmailField() {
        return txtEmail;
    }

    public JPasswordField getPasswordField() {
        return txtPassword;
    }

    public LoginFrame(AuthSuccessCallback callback) {
        super("Personal Finance Tracker — Login");
        this.callback = callback;
        this.authService = new AuthenticationService();
        this.adminService = new AdminManagementService();

        initUI();
    }

    private void initUI() {
        setSize(880, 580);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeColors.BG_MAIN);
        setLayout(new GridLayout(1, 2));

        // -------------------------------------------------------------
        // LEFT COLUMN: BRAND HERO PANEL
        // -------------------------------------------------------------
        JPanel brandHeroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Gradient background using primary blue
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRect(0, 0, w, h);

                // Subtle circular decorative vectors
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(-60, -60, 240, 240);
                g2.fillOval(w - 140, h - 140, 260, 260);

                // Central brand mark
                int logoX = 40;
                int logoY = 80;
                int logoSize = 56;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(logoX, logoY, logoSize, logoSize, 16, 16);

                g2.setFont(FontManager.APP_TITLE);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawString("₹", logoX + 19, logoY + 38);

                // Title
                g2.setFont(FontManager.APP_TITLE);
                g2.setColor(Color.WHITE);
                g2.drawString("Finance Tracker", logoX, logoY + 95);

                g2.setFont(FontManager.BODY);
                g2.setColor(new Color(224, 231, 255));
                g2.drawString("Smart Income, Expense & Budget Management", logoX, logoY + 122);

                // Feature Highlights
                int listY = logoY + 180;
                String[] points = {
                    "✓ Secure Multi-User Authentication (PBKDF2-HMAC-SHA-256)",
                    "✓ Enterprise Super Admin Portal & Role-Based Access Control",
                    "✓ Real-time Dashboard with Custom Graphics2D Charts",
                    "✓ Dynamic Appearance & Branding Studio with Live Sync",
                    "✓ Monthly Spending Budgets with Proactive Alerts",
                    "✓ Persistent Relational Storage via JDBC and MySQL"
                };

                g2.setFont(FontManager.BODY);
                g2.setColor(new Color(241, 245, 249));
                for (String pt : points) {
                    g2.drawString(pt, logoX, listY);
                    listY += 28;
                }

                // Footer version notice
                g2.setFont(FontManager.SMALL);
                g2.setColor(new Color(191, 219, 254));
                g2.drawString("Advanced Java Desktop Application • Java Swing + AWT", logoX, h - 35);

                g2.dispose();
            }
        };
        add(brandHeroPanel);

        // -------------------------------------------------------------
        // RIGHT COLUMN: AUTHENTICATION FORM
        // -------------------------------------------------------------
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ThemeColors.SURFACE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(36, 44, 30, 44));

        // Form Header
        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 4));
        formHeader.setBackground(ThemeColors.SURFACE);

        JLabel lblWelcome = new JLabel("Welcome Back");
        lblWelcome.setFont(FontManager.SECTION_TITLE);
        lblWelcome.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSub = new JLabel("Log in with your email and password to access your tracker.");
        lblSub.setFont(FontManager.BODY);
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        formHeader.add(lblWelcome);
        formHeader.add(lblSub);
        rightPanel.add(formHeader, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel formFields = new JPanel(new GridBagLayout());
        formFields.setBackground(ThemeColors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        // Error message banner
        gbc.gridy = 0;
        lblError = new JLabel("");
        lblError.setFont(FontManager.SMALL);
        lblError.setForeground(ThemeColors.EXPENSE);
        lblError.setPreferredSize(new Dimension(300, 20));
        formFields.add(lblError, gbc);

        // Email Label & Input
        gbc.gridy = 1;
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(FontManager.FORM_LABEL);
        lblEmail.setForeground(ThemeColors.TEXT_MAIN);
        formFields.add(lblEmail, gbc);

        gbc.gridy = 2;
        txtEmail = new JTextField(25);
        txtEmail.setFont(FontManager.FORM_INPUT);
        formFields.add(txtEmail, gbc);

        // Password Label & Input
        gbc.gridy = 3;
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(FontManager.FORM_LABEL);
        lblPass.setForeground(ThemeColors.TEXT_MAIN);
        formFields.add(lblPass, gbc);

        gbc.gridy = 4;
        txtPassword = new JPasswordField(25);
        txtPassword.setFont(FontManager.FORM_INPUT);
        formFields.add(txtPassword, gbc);

        // Show/Hide Password Checkbox and Forgot Password action
        gbc.gridy = 5;
        chkShowPassword = new JCheckBox(" Show password");
        chkShowPassword.setFont(FontManager.SMALL);
        chkShowPassword.setOpaque(false);
        chkShowPassword.addItemListener((ItemEvent e) -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        JButton btnForgot = new JButton("Forgot Password?");
        btnForgot.setFont(FontManager.SMALL_BUTTON);
        btnForgot.setForeground(ThemeColors.ACCENT);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setBorderPainted(false);
        btnForgot.setFocusPainted(false);
        btnForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> openForgotPasswordDialog());

        JPanel passRow = new JPanel(new BorderLayout());
        passRow.setOpaque(false);
        passRow.add(chkShowPassword, BorderLayout.WEST);
        passRow.add(btnForgot, BorderLayout.EAST);
        formFields.add(passRow, gbc);

        // Login Button
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 4, 0);
        CustomButton btnLogin = new CustomButton("Log In to Account", CustomButton.Style.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(300, 36));
        btnLogin.addActionListener(e -> handleLogin());
        formFields.add(btnLogin, gbc);

        // Register Link Button
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 4, 0);
        CustomButton btnRegister = new CustomButton("Don't have an account? Register Here", CustomButton.Style.OUTLINE);
        btnRegister.setPreferredSize(new Dimension(300, 32));
        btnRegister.addActionListener(e -> {
            if (callback != null) {
                callback.onOpenRegister();
            }
        });
        formFields.add(btnRegister, gbc);

        // Evaluator Offline Preview Button
        gbc.gridy = 8;
        gbc.insets = new Insets(4, 0, 4, 0);
        CustomButton btnDemo = new CustomButton("🔍 Preview Offline Demo (Evaluator Mode)", CustomButton.Style.SECONDARY);
        btnDemo.setCustomFont(FontManager.SMALL_BUTTON);
        btnDemo.setPreferredSize(new Dimension(300, 30));
        btnDemo.addActionListener(e -> handleEvaluatorDemo());
        formFields.add(btnDemo, gbc);

        // Admin Setup & Bootstrap Secret Key Promotion
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 0, 6, 0);
        CustomButton btnBootstrap = new CustomButton("⚡ Admin Setup & Bootstrap Token", CustomButton.Style.OUTLINE);
        btnBootstrap.setCustomFont(FontManager.SMALL_BUTTON);
        btnBootstrap.setPreferredSize(new Dimension(300, 30));
        btnBootstrap.addActionListener(e -> handleBootstrapDialog());
        formFields.add(btnBootstrap, gbc);

        rightPanel.add(formFields, BorderLayout.CENTER);

        // Footer Database Status
        JPanel bottomStatus = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        bottomStatus.setBackground(ThemeColors.SURFACE);
        statusIndicator = new StatusIndicator();
        bottomStatus.add(statusIndicator);
        rightPanel.add(bottomStatus, BorderLayout.SOUTH);

        add(rightPanel);

        // Test DB connection in background
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
            SwingUtilities.invokeLater(() -> {
                statusIndicator.setStatus(status);
            });
        }).start();
    }

    private void handleLogin() {
        lblError.setText("");
        String email = txtEmail.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (email.isEmpty() || pass.isEmpty()) {
            lblError.setText("Please enter both email and password.");
            return;
        }

        AuthenticationService.setDemoMode(false);

        new Thread(() -> {
            try {
                User user = authService.login(email, pass);
                SwingUtilities.invokeLater(() -> {
                    if (user != null && callback != null) {
                        setVisible(false);
                        callback.onLoginSuccess(user);
                    }
                });
            } catch (IllegalStateException ex) {
                SwingUtilities.invokeLater(() -> lblError.setText("Account Suspended: " + ex.getMessage()));
            } catch (IllegalArgumentException ex) {
                SwingUtilities.invokeLater(() -> lblError.setText(ex.getMessage()));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
                    if (msg.contains("Communications link failure") || msg.contains("Connection refused") || msg.contains("SQLException") || msg.contains("Access denied")) {
                        lblError.setText("MySQL Connection Error: Verify MySQL is running on port 3306.");
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Could not connect to MySQL database:\n" + msg + "\n\n" +
                                "Troubleshooting steps:\n" +
                                "1. Ensure MySQL service is running (e.g. Start-Service MySQL2610).\n" +
                                "2. Verify database and credentials in resources/db.properties.\n" +
                                "3. Or click 'Preview Offline Demo' to test without MySQL.",
                                "Database Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        lblError.setText("Authentication Error: " + msg);
                    }
                });
            }
        }).start();
    }

    private void handleEvaluatorDemo() {
        int opt = JOptionPane.showConfirmDialog(this,
            "You are entering ISOLATED EVALUATOR DEMO MODE.\n\n" +
            "• Operates exclusively on isolated in-memory sample records.\n" +
            "• Financial records and modifications are NOT persisted to MySQL.\n" +
            "• Intended solely for offline evaluator assessment.\n\n" +
            "Do you want to proceed into Evaluator Demo Mode?",
            "Isolated Evaluator Demo Mode",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        AuthenticationService.setDemoMode(true);
        lblError.setText("");
        // Create explicit demo evaluator session with isolated sample data
        User demoUser = new User();
        demoUser.setUserId(999);
        demoUser.setFirstName("Evaluator");
        demoUser.setLastName("(Demo Mode)");
        demoUser.setEmail("evaluator.demo@local");
        demoUser.setMobileNumber("+91 9999999999");
        demoUser.setDateOfBirth(java.time.LocalDate.of(2000, 1, 1));
        demoUser.setRole(User.ROLE_ADMIN);
        demoUser.setAccountStatus(User.STATUS_ACTIVE);

        if (callback != null) {
            service.SessionService.getInstance().createSession(demoUser, "Isolated Evaluator Sandbox");
            setVisible(false);
            callback.onLoginSuccess(demoUser);
        }
    }

    private void openForgotPasswordDialog() {
        ForgotPasswordDialog dlg = new ForgotPasswordDialog(this);
        dlg.setVisible(true);
        if (dlg.isResetSuccessful()) {
            txtPassword.setText("");
            lblError.setText("");
        }
    }

    private void handleBootstrapDialog() {
        if (!adminService.isBootstrapAvailable()) {
            JOptionPane.showMessageDialog(this,
                "Administrator Bootstrap is currently DISABLED by security policy.\n\n" +
                "Requirements for initial setup:\n" +
                "1. The ADMIN_BOOTSTRAP_TOKEN environment variable must be configured.\n" +
                "2. Zero active administrators must exist in the database.\n\n" +
                "If an administrator already exists, further role promotions must\n" +
                "be performed by an authorized administrator in the Admin Portal.",
                "Bootstrap Unavailable",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 6));
        JTextField txtTargetEmail = new JTextField();
        JPasswordField txtSecret = new JPasswordField();

        panel.add(new JLabel("User Account Email to promote to ADMINISTRATOR:"));
        panel.add(txtTargetEmail);
        panel.add(new JLabel("Bootstrap Setup Secret Token (from ADMIN_BOOTSTRAP_TOKEN):"));
        panel.add(txtSecret);

        int res = JOptionPane.showConfirmDialog(this, panel, "One-Time Initial Administrator Setup", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String email = txtTargetEmail.getText().trim();
            String secret = new String(txtSecret.getPassword()).trim();

            try {
                User promoted = adminService.bootstrapFirstAdmin(email, secret);
                JOptionPane.showMessageDialog(this,
                    "Success! " + promoted.getFullName() + " (" + promoted.getEmail() + ") has been elevated to ADMINISTRATOR.\n" +
                    "The bootstrap setup wizard is now permanently disabled.",
                    "Bootstrap Succeeded", JOptionPane.INFORMATION_MESSAGE);
                txtEmail.setText(promoted.getEmail());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Bootstrap failed: " + ex.getMessage(),
                    "Bootstrap Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
