package ui;

import model.User;
import service.AuthenticationService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import util.FontManager;
import util.InputValidator;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

/**
 * Screen 2: User Account Registration built with Swing JFrame and AWT Layouts.
 * Collects required personal and authentication details with input validation and password strength analysis.
 */
public class RegisterFrame extends JFrame {
    public interface RegisterCallback {
        void onBackToLogin();
        void onRegisterSuccess(User user);
    }

    private final AuthenticationService authService;
    private final RegisterCallback callback;

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtEmail;
    private JTextField txtMobile;
    private JTextField txtDob;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    private JLabel lblError;
    private JPanel strengthPanel;
    private InputValidator.PasswordStrength currentStrength = InputValidator.PasswordStrength.WEAK;

    public RegisterFrame(RegisterCallback callback) {
        super("Personal Finance Tracker — Create Account");
        this.callback = callback;
        this.authService = new AuthenticationService();

        initUI();
    }

    private void initUI() {
        setSize(580, 680);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(ThemeColors.BG_MAIN);
        setLayout(new BorderLayout());

        // Header Title Banner
        JPanel headerPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(580, 80); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRect(0, 0, w, h);

                g2.setFont(FontManager.APP_TITLE);
                g2.setColor(Color.WHITE);
                g2.drawString("Create Your Account", 28, 38);

                g2.setFont(FontManager.BODY);
                g2.setColor(new Color(224, 231, 255));
                g2.drawString("Join Personal Finance Tracker for smart money management", 28, 62);
                g2.dispose();
            }
        };
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields Container (Scrollable)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(ThemeColors.SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 20, 6, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Error message banner
        lblError = new JLabel("");
        lblError.setForeground(ThemeColors.EXPENSE);
        lblError.setFont(FontManager.SMALL_BUTTON);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblError, gbc);

        // First Name
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.35;
        JLabel lblFirst = new JLabel("First Name *");
        lblFirst.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblFirst, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtFirstName = new JTextField(20);
        txtFirstName.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtFirstName, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel lblLast = new JLabel("Last Name *");
        lblLast.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblLast, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtLastName = new JTextField(20);
        txtLastName.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtLastName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        JLabel lblEmail = new JLabel("Email Address *");
        lblEmail.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtEmail = new JTextField(20);
        txtEmail.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtEmail, gbc);

        // Mobile
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        JLabel lblMob = new JLabel("Mobile Number (Optional)");
        lblMob.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblMob, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtMobile = new JTextField(20);
        txtMobile.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtMobile, gbc);

        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        JLabel lblDob = new JLabel("Date of Birth (YYYY-MM-DD)");
        lblDob.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblDob, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtDob = new JTextField(20);
        txtDob.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtDob, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.35;
        JLabel lblPass = new JLabel("Password *");
        lblPass.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(FontManager.FORM_INPUT);
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                currentStrength = InputValidator.evaluatePasswordStrength(new String(txtPassword.getPassword()));
                strengthPanel.repaint();
            }
        });
        formPanel.add(txtPassword, gbc);

        // Password Strength Indicator Panel
        gbc.gridx = 1; gbc.gridy = 7;
        strengthPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(240, 20); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 4, 140, 6, 4, 4);

                int fillW = currentStrength == InputValidator.PasswordStrength.STRONG ? 140 :
                           (currentStrength == InputValidator.PasswordStrength.MEDIUM ? 90 : 40);
                g2.setColor(currentStrength.getColor());
                g2.fillRoundRect(0, 4, fillW, 6, 4, 4);

                g2.setFont(FontManager.SMALL);
                g2.drawString("Strength: " + currentStrength.getLabel(), 152, 11);
                g2.dispose();
            }
        };
        strengthPanel.setOpaque(false);
        formPanel.add(strengthPanel, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0.35;
        JLabel lblConf = new JLabel("Confirm Password *");
        lblConf.setFont(FontManager.FORM_LABEL);
        formPanel.add(lblConf, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(FontManager.FORM_INPUT);
        formPanel.add(txtConfirmPassword, gbc);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(ThemeColors.SURFACE);

        CustomButton btnBack = new CustomButton("← Back to Login", CustomButton.Style.SECONDARY);
        btnBack.addActionListener(e -> {
            setVisible(false);
            if (callback != null) callback.onBackToLogin();
        });

        CustomButton btnSubmit = new CustomButton("Create Account", CustomButton.Style.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(140, 36));
        btnSubmit.addActionListener(e -> handleRegister());

        buttonPanel.add(btnBack);
        buttonPanel.add(btnSubmit);
        add(buttonPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                if (callback != null) callback.onBackToLogin();
            }
        });
    }

    private void handleRegister() {
        lblError.setText("");

        String first = txtFirstName.getText().trim();
        String last = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();
        String mob = txtMobile.getText().trim();
        String dobStr = txtDob.getText().trim();
        String pass = new String(txtPassword.getPassword());
        String conf = new String(txtConfirmPassword.getPassword());

        LocalDate dob = null;
        if (InputValidator.isNotEmpty(dobStr)) {
            dob = InputValidator.parseDate(dobStr);
            if (dob == null) {
                lblError.setText("Invalid Date of Birth format. Please use YYYY-MM-DD.");
                return;
            }
        }

        final LocalDate finalDob = dob;

        new Thread(() -> {
            try {
                User newUser = authService.register(first, last, email, mob, finalDob, pass, conf);
                SwingUtilities.invokeLater(() -> {
                    if (newUser != null) {
                        JOptionPane.showMessageDialog(this,
                            "Account registered successfully for " + newUser.getEmail() + "!\nProceeding to login.",
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                        setVisible(false);
                        if (callback != null) {
                            callback.onRegisterSuccess(newUser);
                        }
                    }
                });
            } catch (IllegalArgumentException ex) {
                SwingUtilities.invokeLater(() -> lblError.setText(ex.getMessage()));
            } catch (java.sql.SQLException ex) {
                SwingUtilities.invokeLater(() -> {
                    lblError.setText("Database Connection Error. Account was not registered.");
                    JOptionPane.showMessageDialog(this,
                        "Database Error: Could not connect to MySQL to save your account.\n\n" +
                        "• Error: " + ex.getMessage() + "\n" +
                        "• Please ensure MySQL Server service is running on port 3306.\n" +
                        "• Or return to Login and click 'Preview Offline Demo (Evaluator Mode)' for an offline sandbox.",
                        "Database Connection Failure", JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> lblError.setText("Registration Error: " + ex.getMessage()));
            }
        }).start();
    }
}
