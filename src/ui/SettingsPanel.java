package ui;

import database.DatabaseConnection;
import model.DatabaseConfig;
import model.User;
import service.AuthenticationService;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/**
 * Screen 11: Application & Database Settings built with Swing & AWT.
 * Allows updating database credentials and changing account passwords.
 */
public class SettingsPanel extends JPanel {
    private final Frame parentFrame;
    private final AuthenticationService authService;

    // Password change fields
    private JPasswordField txtOldPass;
    private JPasswordField txtNewPass;
    private JPasswordField txtConfirmPass;
    private JLabel lblPassMsg;

    // Database config fields
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDbName;
    private JTextField txtDbUser;
    private JPasswordField txtDbPass;
    private JLabel lblDbMsg;

    public SettingsPanel(Frame parentFrame, AuthenticationService authService) {
        this.parentFrame = parentFrame;
        this.authService = authService;

        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel content = new JPanel(new GridLayout(2, 1, 0, 16));
        content.setBackground(ThemeColors.BG_MAIN);

        // 1. Password Management Card
        JPanel passCard = createPasswordCard();
        content.add(passCard);

        // 2. Database Connection Card
        JPanel dbCard = createDatabaseConfigCard();
        content.add(dbCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createPasswordCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel title = new JLabel("Security & Password Management (PBKDF2-HMAC-SHA-256)");
        title.setFont(FontManager.CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ThemeColors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        lblPassMsg = new JLabel("");
        lblPassMsg.setFont(FontManager.SMALL);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblPassMsg, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lblOld = new JLabel("Current Password:");
        lblOld.setFont(FontManager.FORM_LABEL);
        form.add(lblOld, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtOldPass = new JPasswordField(20);
        txtOldPass.setFont(FontManager.FORM_INPUT);
        form.add(txtOldPass, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblNew = new JLabel("New Password:");
        lblNew.setFont(FontManager.FORM_LABEL);
        form.add(lblNew, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtNewPass = new JPasswordField(20);
        txtNewPass.setFont(FontManager.FORM_INPUT);
        form.add(txtNewPass, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblConfirm = new JLabel("Confirm New Password:");
        lblConfirm.setFont(FontManager.FORM_LABEL);
        form.add(lblConfirm, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtConfirmPass = new JPasswordField(20);
        txtConfirmPass.setFont(FontManager.FORM_INPUT);
        form.add(txtConfirmPass, gbc);

        card.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnPanel.setBackground(ThemeColors.SURFACE);
        CustomButton btnChangePass = new CustomButton("Update Password", CustomButton.Style.PRIMARY);
        btnChangePass.setCustomFont(FontManager.SMALL_BUTTON);
        btnChangePass.setPreferredSize(new Dimension(150, 30));
        btnChangePass.addActionListener(e -> handleChangePassword());
        btnPanel.add(btnChangePass);

        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createDatabaseConfigCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JLabel title = new JLabel("Database Connection Configuration (MySQL)");
        title.setFont(FontManager.CARD_TITLE);
        title.setForeground(ThemeColors.TEXT_MAIN);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ThemeColors.SURFACE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        lblDbMsg = new JLabel("");
        lblDbMsg.setFont(FontManager.SMALL);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        form.add(lblDbMsg, gbc);

        DatabaseConfig cfg = DatabaseConnection.getCurrentConfig();

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel lblHost = new JLabel("MySQL Host:");
        lblHost.setFont(FontManager.FORM_LABEL);
        form.add(lblHost, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtHost = new JTextField(cfg.getHost(), 20);
        txtHost.setFont(FontManager.FORM_INPUT);
        form.add(txtHost, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblPort = new JLabel("Port:");
        lblPort.setFont(FontManager.FORM_LABEL);
        form.add(lblPort, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPort = new JTextField(String.valueOf(cfg.getPort()), 20);
        txtPort.setFont(FontManager.FORM_INPUT);
        form.add(txtPort, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblName = new JLabel("Database Schema:");
        lblName.setFont(FontManager.FORM_LABEL);
        form.add(lblName, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDbName = new JTextField(cfg.getDatabaseName(), 20);
        txtDbName.setFont(FontManager.FORM_INPUT);
        form.add(txtDbName, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(FontManager.FORM_LABEL);
        form.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDbUser = new JTextField(cfg.getUsername(), 20);
        txtDbUser.setFont(FontManager.FORM_INPUT);
        form.add(txtDbUser, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(FontManager.FORM_LABEL);
        form.add(lblPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDbPass = new JPasswordField(cfg.getPassword(), 20);
        txtDbPass.setFont(FontManager.FORM_INPUT);
        form.add(txtDbPass, gbc);

        card.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnPanel.setBackground(ThemeColors.SURFACE);
        CustomButton btnSaveDb = new CustomButton("Save & Apply Settings", CustomButton.Style.PRIMARY);
        btnSaveDb.setCustomFont(FontManager.SMALL_BUTTON);
        btnSaveDb.setPreferredSize(new Dimension(170, 30));
        btnSaveDb.addActionListener(e -> handleSaveDbSettings());
        btnPanel.add(btnSaveDb);

        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private void handleChangePassword() {
        lblPassMsg.setText("");
        User user = AuthenticationService.getCurrentUser();
        if (user == null) {
            lblPassMsg.setForeground(ThemeColors.EXPENSE);
            lblPassMsg.setText("No active session found.");
            return;
        }

        String oldP = new String(txtOldPass.getPassword());
        String newP = new String(txtNewPass.getPassword());
        String confP = new String(txtConfirmPass.getPassword());

        new Thread(() -> {
            try {
                boolean ok = authService.changePassword(user.getUserId(), oldP, newP, confP);
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        lblPassMsg.setForeground(ThemeColors.INCOME);
                        lblPassMsg.setText("✓ Password updated successfully (PBKDF2 key derived).");
                        txtOldPass.setText("");
                        txtNewPass.setText("");
                        txtConfirmPass.setText("");
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblPassMsg.setForeground(ThemeColors.EXPENSE);
                    lblPassMsg.setText("Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void handleSaveDbSettings() {
        lblDbMsg.setText("");
        String host = txtHost.getText().trim();
        int port = 3306;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException ignored) {}
        String name = txtDbName.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = new String(txtDbPass.getPassword());

        DatabaseConnection.updateConfig(host, port, name, user, pass);
        lblDbMsg.setForeground(ThemeColors.INCOME);
        lblDbMsg.setText("✓ Database properties saved to resources/db.properties.");
    }
}
