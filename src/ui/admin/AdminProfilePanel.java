package ui.admin;

import model.User;
import service.AuthenticationService;
import service.SessionService;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Super Admin Panel: Administrator Profile & Account Credentials.
 * Displays active administrator identity, privileged role confirmation, active session details,
 * and allows secure self-service password updates.
 */
public class AdminProfilePanel extends JPanel {
    private final User currentAdmin;
    private final AuthenticationService authService;

    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblStatus;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminProfilePanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.authService = new AuthenticationService();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
    }

    private void initUI() {
        // TOP HEADER
        JPanel headerTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTitle.setOpaque(false);
        JLabel titleLbl = new JLabel("Administrator Profile & Account Credentials");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Review administrator identity credentials, active session tokens, and modify authentication passwords.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);
        headerTitle.add(titleLbl);
        headerTitle.add(subLbl);
        add(headerTitle, BorderLayout.NORTH);

        // CENTER: 2 Columns (Profile details & Password change)
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Column 1: Identity & Session Details
        JPanel profileCard = new JPanel(new BorderLayout(0, 12));
        profileCard.setBackground(ThemeColors.SURFACE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel profTitle = new JLabel("👤 Identity & Active Session Token");
        profTitle.setFont(FontManager.CARD_TITLE);
        profTitle.setForeground(ThemeColors.TEXT_MAIN);
        profileCard.add(profTitle, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridLayout(7, 2, 8, 12));
        infoGrid.setOpaque(false);

        addInfoRow(infoGrid, "Full Name:", currentAdmin.getFullName());
        addInfoRow(infoGrid, "Email Address:", currentAdmin.getEmail());
        addInfoRow(infoGrid, "System Role:", currentAdmin.getRole() + " (SUPER ADMINISTRATOR)");
        addInfoRow(infoGrid, "Account Status:", currentAdmin.getAccountStatus());
        addInfoRow(infoGrid, "Active Session ID:", SessionService.getCurrentSessionId() != null ?
                SessionService.getCurrentSessionId().substring(0, Math.min(16, SessionService.getCurrentSessionId().length())) + "..." : "Local Workstation");
        addInfoRow(infoGrid, "Client Workstation:", SessionService.getClientInfo());
        addInfoRow(infoGrid, "Last Login:", currentAdmin.getLastLogin() != null ? currentAdmin.getLastLogin().format(FMT) : "Active Now");

        profileCard.add(infoGrid, BorderLayout.CENTER);
        centerGrid.add(profileCard);

        // Column 2: Password Change Form
        JPanel passCard = new JPanel(new BorderLayout(0, 12));
        passCard.setBackground(ThemeColors.SURFACE);
        passCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel passTitle = new JLabel("🔒 Change Admin Password");
        passTitle.setFont(FontManager.CARD_TITLE);
        passTitle.setForeground(ThemeColors.TEXT_MAIN);
        passCard.add(passTitle, BorderLayout.NORTH);

        JPanel passForm = new JPanel(new GridBagLayout());
        passForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);

        int r = 0;
        passForm.add(new JLabel("Current Password:"), getGbc(r++, gbc));
        txtOldPassword = new JPasswordField(20);
        passForm.add(txtOldPassword, getGbc(r++, gbc));

        passForm.add(new JLabel("New Password (min 6 characters):"), getGbc(r++, gbc));
        txtNewPassword = new JPasswordField(20);
        passForm.add(txtNewPassword, getGbc(r++, gbc));

        passForm.add(new JLabel("Confirm New Password:"), getGbc(r++, gbc));
        txtConfirmPassword = new JPasswordField(20);
        passForm.add(txtConfirmPassword, getGbc(r++, gbc));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(FontManager.SMALL);
        lblStatus.setForeground(ThemeColors.EXPENSE);
        passForm.add(lblStatus, getGbc(r++, gbc));

        passCard.add(passForm, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnRow.setOpaque(false);

        CustomButton btnUpdate = new CustomButton("Update Password", CustomButton.Style.PRIMARY);
        btnUpdate.setPreferredSize(new Dimension(160, 34));
        btnUpdate.addActionListener(e -> handlePasswordChange());
        btnRow.add(btnUpdate);

        passCard.add(btnRow, BorderLayout.SOUTH);
        centerGrid.add(passCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private GridBagConstraints getGbc(int row, GridBagConstraints base) {
        base.gridy = row;
        return base;
    }

    private void addInfoRow(JPanel p, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(FontManager.FORM_LABEL);
        lbl.setForeground(ThemeColors.TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(FontManager.BODY_BOLD);
        val.setForeground(ThemeColors.TEXT_MAIN);

        p.add(lbl);
        p.add(val);
    }

    private void handlePasswordChange() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confPass = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            lblStatus.setText("All password fields are required.");
            return;
        }

        try {
            boolean ok = authService.changePassword(currentAdmin.getUserId(), oldPass, newPass, confPass);
            if (ok) {
                lblStatus.setForeground(ThemeColors.INCOME);
                lblStatus.setText("Password successfully updated!");
                txtOldPassword.setText("");
                txtNewPassword.setText("");
                txtConfirmPassword.setText("");
                JOptionPane.showMessageDialog(this, "Your password has been changed successfully.", "Password Updated", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            lblStatus.setForeground(ThemeColors.EXPENSE);
            lblStatus.setText(ex.getMessage());
        }
    }
}
