package ui.admin;

import model.User;
import service.ApplicationTextService;
import service.AuditLogService;
import service.EmailService;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;

/**
 * Super Admin Panel: Application & System Settings.
 * Configures global SMTP mail server parameters, live socket tests,
 * session timeout thresholds, password recovery expiration, and security constraints.
 */
public class ApplicationSettingsPanel extends JPanel {
    private final User currentAdmin;
    private final ApplicationTextService textService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // SMTP Controls
    private JTextField txtSmtpHost;
    private JTextField txtSmtpPort;
    private JTextField txtSmtpUser;
    private JPasswordField txtSmtpPass;
    private JTextField txtSmtpFrom;
    private JCheckBox chkSmtpTls;
    private JCheckBox chkSmtpEnabled;
    private JLabel lblSmtpTestStatus;

    // Security & Session Controls
    private JComboBox<String> cmbSessionTimeout;
    private JComboBox<String> cmbTokenExpiry;
    private JComboBox<String> cmbMinPasswordLen;

    public ApplicationSettingsPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.textService = ApplicationTextService.getInstance();
        this.emailService = EmailService.getInstance();
        this.auditLogService = new AuditLogService();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
        loadSettings();
    }

    private void initUI() {
        // TOP HEADER
        JPanel headerTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTitle.setOpaque(false);
        JLabel titleLbl = new JLabel("Application & Security Settings");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Configure external SMTP relay, session timeout limits, single-use token life, and system policies.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);
        headerTitle.add(titleLbl);
        headerTitle.add(subLbl);
        add(headerTitle, BorderLayout.NORTH);

        // CENTER: 2 Column Grid for SMTP & Security
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Column 1: SMTP Settings Card
        JPanel smtpCard = new JPanel(new BorderLayout(0, 12));
        smtpCard.setBackground(ThemeColors.SURFACE);
        smtpCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel smtpTitle = new JLabel("📧 SMTP Mail Delivery Gateway");
        smtpTitle.setFont(FontManager.CARD_TITLE);
        smtpTitle.setForeground(ThemeColors.TEXT_MAIN);
        smtpCard.add(smtpTitle, BorderLayout.NORTH);

        JPanel smtpForm = new JPanel(new GridBagLayout());
        smtpForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int row = 0;
        smtpForm.add(new JLabel("SMTP Hostname / IP:"), getGbc(row++));
        txtSmtpHost = new JTextField(20);
        smtpForm.add(txtSmtpHost, getGbc(row++));

        smtpForm.add(new JLabel("SMTP Port (25, 465, or 587):"), getGbc(row++));
        txtSmtpPort = new JTextField(20);
        smtpForm.add(txtSmtpPort, getGbc(row++));

        smtpForm.add(new JLabel("SMTP Username:"), getGbc(row++));
        txtSmtpUser = new JTextField(20);
        smtpForm.add(txtSmtpUser, getGbc(row++));

        smtpForm.add(new JLabel("SMTP Password:"), getGbc(row++));
        txtSmtpPass = new JPasswordField(20);
        smtpForm.add(txtSmtpPass, getGbc(row++));

        smtpForm.add(new JLabel("Sender From Address:"), getGbc(row++));
        txtSmtpFrom = new JTextField(20);
        smtpForm.add(txtSmtpFrom, getGbc(row++));

        chkSmtpTls = new JCheckBox(" Enable STARTTLS Encryption");
        chkSmtpTls.setOpaque(false);
        smtpForm.add(chkSmtpTls, getGbc(row++));

        chkSmtpEnabled = new JCheckBox(" Enable Live Email Dispatch (Uncheck for Offline/Demo mode)");
        chkSmtpEnabled.setOpaque(false);
        smtpForm.add(chkSmtpEnabled, getGbc(row++));

        lblSmtpTestStatus = new JLabel("Status: Untested");
        lblSmtpTestStatus.setFont(FontManager.SMALL);
        lblSmtpTestStatus.setForeground(ThemeColors.TEXT_MUTED);
        smtpForm.add(lblSmtpTestStatus, getGbc(row++));

        smtpCard.add(smtpForm, BorderLayout.CENTER);

        JPanel smtpActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        smtpActions.setOpaque(false);

        CustomButton btnTestSmtp = new CustomButton("⚡ Test Connection", CustomButton.Style.SECONDARY);
        btnTestSmtp.setPreferredSize(new Dimension(140, 32));
        btnTestSmtp.addActionListener(e -> handleTestSmtp());

        CustomButton btnSaveSmtp = new CustomButton("💾 Save SMTP Settings", CustomButton.Style.PRIMARY);
        btnSaveSmtp.setPreferredSize(new Dimension(160, 32));
        btnSaveSmtp.addActionListener(e -> handleSaveSmtp());

        smtpActions.add(btnTestSmtp);
        smtpActions.add(btnSaveSmtp);
        smtpCard.add(smtpActions, BorderLayout.SOUTH);

        centerGrid.add(smtpCard);

        // Column 2: Session & Security Policies Card
        JPanel secCard = new JPanel(new BorderLayout(0, 12));
        secCard.setBackground(ThemeColors.SURFACE);
        secCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel secTitle = new JLabel("⏱️ Session Lifecycles & Security Rules");
        secTitle.setFont(FontManager.CARD_TITLE);
        secTitle.setForeground(ThemeColors.TEXT_MAIN);
        secCard.add(secTitle, BorderLayout.NORTH);

        JPanel secForm = new JPanel(new GridBagLayout());
        secForm.setOpaque(false);

        int sRow = 0;
        secForm.add(new JLabel("Desktop Inactivity Session Timeout:"), getGbc(sRow++));
        cmbSessionTimeout = new JComboBox<>(new String[]{"15 Minutes", "30 Minutes", "60 Minutes", "120 Minutes", "480 Minutes"});
        cmbSessionTimeout.setFont(FontManager.FORM_INPUT);
        secForm.add(cmbSessionTimeout, getGbc(sRow++));

        secForm.add(new JLabel("Password Recovery Token Validity:"), getGbc(sRow++));
        cmbTokenExpiry = new JComboBox<>(new String[]{"5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "60 Minutes"});
        cmbTokenExpiry.setFont(FontManager.FORM_INPUT);
        secForm.add(cmbTokenExpiry, getGbc(sRow++));

        secForm.add(new JLabel("Minimum Password Length:"), getGbc(sRow++));
        cmbMinPasswordLen = new JComboBox<>(new String[]{"6 Characters", "8 Characters", "10 Characters", "12 Characters"});
        cmbMinPasswordLen.setFont(FontManager.FORM_INPUT);
        secForm.add(cmbMinPasswordLen, getGbc(sRow++));

        JLabel noteLbl = new JLabel("<html><b>Security Notice:</b><br>" +
                "• All password hashes use PBKDF2-HMAC-SHA-256 with cryptographically random salts.<br>" +
                "• All password reset tokens are stored strictly as SHA-256 digests.<br>" +
                "• Modifying session timeouts affects all newly established user sessions.</html>");
        noteLbl.setFont(FontManager.SMALL);
        noteLbl.setForeground(ThemeColors.TEXT_MUTED);
        secForm.add(noteLbl, getGbc(sRow++));

        secCard.add(secForm, BorderLayout.CENTER);

        JPanel secActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        secActions.setOpaque(false);

        CustomButton btnSaveSec = new CustomButton("💾 Save Security Policies", CustomButton.Style.PRIMARY);
        btnSaveSec.setPreferredSize(new Dimension(180, 32));
        btnSaveSec.addActionListener(e -> handleSaveSecurity());

        secActions.add(btnSaveSec);
        secCard.add(secActions, BorderLayout.SOUTH);

        centerGrid.add(secCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private GridBagConstraints getGbc(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(3, 0, 3, 0);
        return gbc;
    }

    private void loadSettings() {
        EmailService.SmtpConfig cfg = emailService.getConfig();
        txtSmtpHost.setText(cfg.host);
        txtSmtpPort.setText(String.valueOf(cfg.port));
        txtSmtpUser.setText(cfg.username);
        txtSmtpPass.setText(cfg.password.isEmpty() ? "" : "••••••••");
        txtSmtpFrom.setText(cfg.fromAddress);
        chkSmtpTls.setSelected(cfg.tlsEnabled);
        chkSmtpEnabled.setSelected(cfg.enabled);

        String sessTimeout = textService.getText("session.timeout_minutes", "60") + " Minutes";
        cmbSessionTimeout.setSelectedItem(sessTimeout);

        String tokenExp = textService.getText("password_reset.expiry_minutes", "15") + " Minutes";
        cmbTokenExpiry.setSelectedItem(tokenExp);

        String minLen = textService.getText("auth.min_password_length", "6") + " Characters";
        cmbMinPasswordLen.setSelectedItem(minLen);
    }

    private void handleTestSmtp() {
        String host = txtSmtpHost.getText().trim();
        int port = 587;
        try {
            port = Integer.parseInt(txtSmtpPort.getText().trim());
        } catch (Exception ignored) {}

        EmailService.SmtpConfig cfg = new EmailService.SmtpConfig(
                host, port, txtSmtpUser.getText().trim(),
                new String(txtSmtpPass.getPassword()),
                txtSmtpFrom.getText().trim(),
                chkSmtpTls.isSelected(),
                chkSmtpEnabled.isSelected()
        );

        lblSmtpTestStatus.setText("Connecting to " + host + ":" + port + "...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return emailService.testConnection(cfg);
            }
            @Override
            protected void done() {
                try {
                    String res = get();
                    lblSmtpTestStatus.setText("Result: " + res);
                    if (res.startsWith("Success")) {
                        lblSmtpTestStatus.setForeground(ThemeColors.INCOME);
                    } else {
                        lblSmtpTestStatus.setForeground(ThemeColors.EXPENSE);
                    }
                } catch (Exception e) {
                    lblSmtpTestStatus.setText("Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void handleSaveSmtp() {
        String host = txtSmtpHost.getText().trim();
        int port = 587;
        try {
            port = Integer.parseInt(txtSmtpPort.getText().trim());
        } catch (Exception ignored) {}

        String pass = new String(txtSmtpPass.getPassword());
        EmailService.SmtpConfig cfg = new EmailService.SmtpConfig(
                host, port, txtSmtpUser.getText().trim(),
                pass.startsWith("••••") ? null : pass,
                txtSmtpFrom.getText().trim(),
                chkSmtpTls.isSelected(),
                chkSmtpEnabled.isSelected()
        );

        emailService.saveConfig(cfg, currentAdmin.getUserId());
        auditLogService.log(currentAdmin.getUserId(), currentAdmin.getEmail(), "SMTP_SETTINGS_SAVED", "SETTINGS",
                host + ":" + port, "SUCCESS", "SMTP relay settings updated by admin.");

        JOptionPane.showMessageDialog(this, "SMTP configuration successfully saved.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSaveSecurity() {
        String sessStr = ((String) cmbSessionTimeout.getSelectedItem()).split(" ")[0];
        String tokenStr = ((String) cmbTokenExpiry.getSelectedItem()).split(" ")[0];
        String passStr = ((String) cmbMinPasswordLen.getSelectedItem()).split(" ")[0];

        textService.setText("session.timeout_minutes", sessStr, "SECURITY", "Desktop session timeout in minutes", currentAdmin.getUserId());
        textService.setText("password_reset.expiry_minutes", tokenStr, "SECURITY", "Password recovery code lifetime in minutes", currentAdmin.getUserId());
        textService.setText("auth.min_password_length", passStr, "SECURITY", "Minimum required password characters", currentAdmin.getUserId());

        auditLogService.log(currentAdmin.getUserId(), currentAdmin.getEmail(), "SECURITY_POLICIES_SAVED", "SETTINGS",
                "SECURITY", "SUCCESS", "Security thresholds updated: Session=" + sessStr + "m, Token=" + tokenStr + "m");

        JOptionPane.showMessageDialog(this, "Security policies saved successfully.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }
}
