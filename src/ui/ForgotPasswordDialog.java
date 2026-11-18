package ui;

import service.PasswordResetService;
import ui.components.CustomButton;
import util.FontManager;
import util.InputValidator;
import util.ThemeColors;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * Multi-step modal recovery dialog for user password reset.
 * Step 1: Identify Account (Email address)
 * Step 2: Verify Single-Use Token with honest delivery reporting
 * Step 3: Set New Password with live dynamic strength bar and session revocation
 */
public class ForgotPasswordDialog extends JDialog {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    // Step 1 Controls
    private JTextField txtEmail;
    private JLabel lblStep1Error;

    // Step 2 Controls
    private JTextField txtToken;
    private JLabel lblStep2Status;
    private JLabel lblStep2Error;
    private JLabel lblTokenHint;

    // Step 3 Controls
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JCheckBox chkShowPasswords;
    private JLabel lblStep3Error;
    private PasswordStrengthBar strengthBar;

    // State
    private String validatedEmail = "";
    private String currentToken = "";
    private boolean resetSuccessful = false;

    public ForgotPasswordDialog(Frame parent) {
        super(parent, "Account Recovery & Password Reset", true);
        setSize(520, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BG_MAIN);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeColors.SURFACE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(18, 24, 16, 24)
        ));

        JLabel titleLbl = new JLabel("Password Recovery");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Recover account access using secure single-use token verification");
        subtitleLbl.setFont(FontManager.SMALL);
        subtitleLbl.setForeground(ThemeColors.TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subtitleLbl);
        headerPanel.add(titles, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Build Steps
        cardsPanel.setOpaque(false);
        cardsPanel.add(createStep1Panel(), "STEP1");
        cardsPanel.add(createStep2Panel(), "STEP2");
        cardsPanel.add(createStep3Panel(), "STEP3");

        add(cardsPanel, BorderLayout.CENTER);
        cardLayout.show(cardsPanel, "STEP1");
    }

    public boolean isResetSuccessful() {
        return resetSuccessful;
    }

    // =========================================================================
    // STEP 1: IDENTIFY ACCOUNT
    // =========================================================================
    private JPanel createStep1Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(6, 0, 6, 0);

        gbc.gridy = 0;
        JLabel lblInstr = new JLabel("<html><b>Step 1 of 3: Identify Account</b><br>Enter your registered account email to request a secure verification code.</html>");
        lblInstr.setFont(FontManager.BODY);
        lblInstr.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblInstr, gbc);

        gbc.gridy = 1;
        lblStep1Error = new JLabel(" ");
        lblStep1Error.setFont(FontManager.SMALL);
        lblStep1Error.setForeground(ThemeColors.EXPENSE);
        panel.add(lblStep1Error, gbc);

        gbc.gridy = 2;
        JLabel lblEmail = new JLabel("Registered Email Address");
        lblEmail.setFont(FontManager.FORM_LABEL);
        lblEmail.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblEmail, gbc);

        gbc.gridy = 3;
        txtEmail = new JTextField(24);
        txtEmail.setFont(FontManager.FORM_INPUT);
        panel.add(txtEmail, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(18, 0, 8, 0);
        CustomButton btnNext = new CustomButton("Send Recovery Token", CustomButton.Style.PRIMARY);
        btnNext.setPreferredSize(new Dimension(300, 36));
        btnNext.addActionListener(e -> handleStep1Submit());
        panel.add(btnNext, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 6, 0);
        CustomButton btnCancel = new CustomButton("Back to Login", CustomButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(300, 32));
        btnCancel.addActionListener(e -> dispose());
        panel.add(btnCancel, gbc);

        return panel;
    }

    private void handleStep1Submit() {
        String email = txtEmail.getText().trim();
        if (!InputValidator.isValidEmail(email)) {
            lblStep1Error.setText("Please enter a valid email address.");
            return;
        }
        lblStep1Error.setText(" ");

        PasswordResetService.ResetRequestResult res = PasswordResetService.getInstance().requestPasswordReset(email, "DesktopUI");
        if (!res.isSuccess()) {
            lblStep1Error.setText(res.getMessage());
            return;
        }

        validatedEmail = email;
        lblStep2Status.setText("<html>Token sent for: <b>" + validatedEmail + "</b><br>" + res.getMessage() + "</html>");
        if (res.getPreviewToken() != null) {
            lblTokenHint.setText("<html><b>Evaluator/Demo Token:</b> <font color='#2563EB'>" + res.getPreviewToken() + "</font></html>");
            lblTokenHint.setVisible(true);
            txtToken.setText(res.getPreviewToken());
        } else {
            lblTokenHint.setVisible(false);
            txtToken.setText("");
        }

        cardLayout.show(cardsPanel, "STEP2");
    }

    // =========================================================================
    // STEP 2: VERIFY OWNERSHIP TOKEN
    // =========================================================================
    private JPanel createStep2Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 32, 20, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);

        gbc.gridy = 0;
        JLabel lblInstr = new JLabel("<html><b>Step 2 of 3: Verify Token</b><br>Enter the single-use 6-digit code issued for your account.</html>");
        lblInstr.setFont(FontManager.BODY);
        lblInstr.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblInstr, gbc);

        gbc.gridy = 1;
        lblStep2Status = new JLabel("Token sent to email.");
        lblStep2Status.setFont(FontManager.SMALL);
        lblStep2Status.setForeground(ThemeColors.TEXT_MUTED);
        panel.add(lblStep2Status, gbc);

        gbc.gridy = 2;
        lblTokenHint = new JLabel(" ");
        lblTokenHint.setFont(FontManager.BODY);
        lblTokenHint.setForeground(ThemeColors.ACCENT);
        panel.add(lblTokenHint, gbc);

        gbc.gridy = 3;
        lblStep2Error = new JLabel(" ");
        lblStep2Error.setFont(FontManager.SMALL);
        lblStep2Error.setForeground(ThemeColors.EXPENSE);
        panel.add(lblStep2Error, gbc);

        gbc.gridy = 4;
        JLabel lblCode = new JLabel("6-Digit Verification Code");
        lblCode.setFont(FontManager.FORM_LABEL);
        lblCode.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblCode, gbc);

        gbc.gridy = 5;
        txtToken = new JTextField(12);
        txtToken.setFont(new Font("Monospaced", Font.BOLD, 18));
        txtToken.setHorizontalAlignment(JTextField.CENTER);
        panel.add(txtToken, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(14, 0, 6, 0);
        CustomButton btnVerify = new CustomButton("Verify Token & Continue", CustomButton.Style.PRIMARY);
        btnVerify.setPreferredSize(new Dimension(300, 36));
        btnVerify.addActionListener(e -> handleStep2Submit());
        panel.add(btnVerify, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 4, 0);
        CustomButton btnBack = new CustomButton("← Back to Email Entry", CustomButton.Style.OUTLINE);
        btnBack.setPreferredSize(new Dimension(300, 32));
        btnBack.addActionListener(e -> cardLayout.show(cardsPanel, "STEP1"));
        panel.add(btnBack, gbc);

        return panel;
    }

    private void handleStep2Submit() {
        String token = txtToken.getText().trim();
        if (token.isEmpty()) {
            lblStep2Error.setText("Please enter the verification code.");
            return;
        }
        lblStep2Error.setText(" ");
        currentToken = token;
        cardLayout.show(cardsPanel, "STEP3");
    }

    // =========================================================================
    // STEP 3: SET NEW PASSWORD WITH STRENGTH BAR
    // =========================================================================
    private JPanel createStep3Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 32, 16, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.insets = new Insets(3, 0, 3, 0);

        gbc.gridy = 0;
        JLabel lblInstr = new JLabel("<html><b>Step 3 of 3: Set New Password</b><br>Create a secure password. Existing active sessions will be terminated.</html>");
        lblInstr.setFont(FontManager.BODY);
        lblInstr.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblInstr, gbc);

        gbc.gridy = 1;
        lblStep3Error = new JLabel(" ");
        lblStep3Error.setFont(FontManager.SMALL);
        lblStep3Error.setForeground(ThemeColors.EXPENSE);
        panel.add(lblStep3Error, gbc);

        gbc.gridy = 2;
        JLabel lblNew = new JLabel("New Password");
        lblNew.setFont(FontManager.FORM_LABEL);
        lblNew.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblNew, gbc);

        gbc.gridy = 3;
        txtNewPassword = new JPasswordField(20);
        txtNewPassword.setFont(FontManager.FORM_INPUT);
        panel.add(txtNewPassword, gbc);

        gbc.gridy = 4;
        strengthBar = new PasswordStrengthBar();
        panel.add(strengthBar, gbc);

        gbc.gridy = 5;
        JLabel lblConfirm = new JLabel("Confirm New Password");
        lblConfirm.setFont(FontManager.FORM_LABEL);
        lblConfirm.setForeground(ThemeColors.TEXT_MAIN);
        panel.add(lblConfirm, gbc);

        gbc.gridy = 6;
        txtConfirmPassword = new JPasswordField(20);
        txtConfirmPassword.setFont(FontManager.FORM_INPUT);
        panel.add(txtConfirmPassword, gbc);

        gbc.gridy = 7;
        chkShowPasswords = new JCheckBox(" Show password text");
        chkShowPasswords.setFont(FontManager.SMALL);
        chkShowPasswords.setOpaque(false);
        chkShowPasswords.addItemListener((ItemEvent e) -> {
            char echo = chkShowPasswords.isSelected() ? (char) 0 : '•';
            txtNewPassword.setEchoChar(echo);
            txtConfirmPassword.setEchoChar(echo);
        });
        panel.add(chkShowPasswords, gbc);

        // Dynamic Document Listener for Password Strength
        txtNewPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void removeUpdate(DocumentEvent e) { updateStrength(); }
            @Override public void changedUpdate(DocumentEvent e) { updateStrength(); }
            private void updateStrength() {
                strengthBar.evaluate(new String(txtNewPassword.getPassword()));
            }
        });

        gbc.gridy = 8;
        gbc.insets = new Insets(12, 0, 6, 0);
        CustomButton btnSubmit = new CustomButton("Update Password & Revoke Sessions", CustomButton.Style.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(300, 36));
        btnSubmit.addActionListener(e -> handleStep3Submit());
        panel.add(btnSubmit, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(2, 0, 4, 0);
        CustomButton btnCancel = new CustomButton("Cancel", CustomButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(300, 30));
        btnCancel.addActionListener(e -> dispose());
        panel.add(btnCancel, gbc);

        return panel;
    }

    private void handleStep3Submit() {
        String p1 = new String(txtNewPassword.getPassword());
        String p2 = new String(txtConfirmPassword.getPassword());

        if (p1.length() < 6) {
            lblStep3Error.setText("Password must be at least 6 characters long.");
            return;
        }
        if (!p1.equals(p2)) {
            lblStep3Error.setText("Passwords do not match.");
            return;
        }

        try {
            boolean ok = PasswordResetService.getInstance().verifyAndResetPassword(
                    validatedEmail, currentToken, p1, p2
            );

            if (ok) {
                resetSuccessful = true;
                JOptionPane.showMessageDialog(this,
                        "Password successfully updated!\n\nAll existing sessions for " + validatedEmail +
                                " have been terminated.\nPlease log in with your new password.",
                        "Password Reset Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            lblStep3Error.setText(ex.getMessage());
            // If token invalid, allow user to jump back to Step 2
            if (ex.getMessage().toLowerCase().contains("token") || ex.getMessage().toLowerCase().contains("code")) {
                cardLayout.show(cardsPanel, "STEP2");
                lblStep2Error.setText(ex.getMessage());
            }
        } catch (Exception ex) {
            lblStep3Error.setText("Database error: " + ex.getMessage());
        }
    }

    /**
     * Custom lightweight password strength meter component.
     */
    private static class PasswordStrengthBar extends JPanel {
        private int score = 0; // 0 to 4
        private String label = "Strength: None";
        private Color barColor = ThemeColors.TEXT_MUTED;

        public PasswordStrengthBar() {
            setPreferredSize(new Dimension(280, 24));
            setOpaque(false);
        }

        public void evaluate(String pass) {
            if (pass == null || pass.isEmpty()) {
                score = 0;
                label = "Strength: None";
                barColor = ThemeColors.TEXT_MUTED;
            } else {
                int calc = 0;
                if (pass.length() >= 6) calc++;
                if (pass.length() >= 10) calc++;
                if (pass.matches(".*[A-Z].*") && pass.matches(".*[a-z].*")) calc++;
                if (pass.matches(".*\\d.*") || pass.matches(".*[^A-Za-z0-9].*")) calc++;

                score = calc;
                if (score <= 1) {
                    label = "Strength: Weak";
                    barColor = ThemeColors.EXPENSE;
                } else if (score == 2 || score == 3) {
                    label = "Strength: Moderate";
                    barColor = ThemeColors.WARNING;
                } else {
                    label = "Strength: Strong";
                    barColor = ThemeColors.INCOME;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int barWidth = w - 120;
            int barHeight = 6;
            int y = 9;

            // Background track
            g2.setColor(ThemeColors.CARD_BORDER);
            g2.fillRoundRect(0, y, barWidth, barHeight, 4, 4);

            // Filled segments
            if (score > 0) {
                int fillW = (int) (barWidth * (score / 4.0));
                g2.setColor(barColor);
                g2.fillRoundRect(0, y, fillW, barHeight, 4, 4);
            }

            // Text Label
            g2.setFont(FontManager.SMALL);
            g2.setColor(barColor);
            g2.drawString(label, barWidth + 12, y + 6);

            g2.dispose();
        }
    }
}
