package ui.admin;

import model.User;
import service.AdminManagementService;
import ui.components.CardPanel;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen: User Management & Role Delegation
 * Allows administrators to search, filter, promote/demote roles, and suspend/reactivate user accounts.
 * Includes defensive protections preventing the demotion or suspension of the last active administrator.
 */
public class UserManagementPanel extends JPanel {
    private final User currentAdmin;
    private final AdminManagementService adminService;

    private JTextField txtSearch;
    private JComboBox<String> cmbRoleFilter;
    private JComboBox<String> cmbStatusFilter;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<User> currentDisplayedUsers = new ArrayList<>();

    private CustomButton btnToggleRole;
    private CustomButton btnToggleStatus;
    private JLabel lblCountBadge;

    public UserManagementPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.adminService = new AdminManagementService();

        initUI();
        refreshUsers();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP TITLE BAR
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("User Directory & Access Control");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);
        textPanel.add(lblTitle);

        lblCountBadge = new JLabel("0 Users");
        lblCountBadge.setFont(FontManager.SMALL_BUTTON);
        lblCountBadge.setForeground(ThemeColors.PRIMARY);
        textPanel.add(lblCountBadge);

        titlePanel.add(textPanel, BorderLayout.WEST);

        CustomButton btnRefresh = new CustomButton("⟳ Refresh Directory", CustomButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshUsers());
        titlePanel.add(btnRefresh, BorderLayout.EAST);

        add(titlePanel, BorderLayout.NORTH);

        // 2. SEARCH & FILTER TOOLBAR CARD
        CardPanel filterCard = new CardPanel();
        filterCard.setLayout(new BorderLayout(12, 10));
        filterCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search text
        gbc.gridx = 0;
        gbc.weightx = 0.5;
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.setFont(FontManager.FORM_INPUT);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name, email, or user ID...");
        txtSearch.addActionListener(e -> applyFilter());
        filterRow.add(txtSearch, gbc);

        // Role filter
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        cmbRoleFilter = new JComboBox<>(new String[]{"All Roles", "USER", "ADMIN"});
        cmbRoleFilter.setFont(FontManager.FORM_INPUT);
        cmbRoleFilter.setPreferredSize(new Dimension(120, 32));
        cmbRoleFilter.addActionListener(e -> applyFilter());
        filterRow.add(cmbRoleFilter, gbc);

        // Status filter
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "ACTIVE", "SUSPENDED"});
        cmbStatusFilter.setFont(FontManager.FORM_INPUT);
        cmbStatusFilter.setPreferredSize(new Dimension(130, 32));
        cmbStatusFilter.addActionListener(e -> applyFilter());
        filterRow.add(cmbStatusFilter, gbc);

        // Filter / Search Button
        gbc.gridx = 3;
        gbc.weightx = 0.1;
        CustomButton btnSearch = new CustomButton("Filter", CustomButton.ButtonType.PRIMARY);
        btnSearch.setPreferredSize(new Dimension(90, 32));
        btnSearch.addActionListener(e -> applyFilter());
        filterRow.add(btnSearch, gbc);

        // Clear button
        gbc.gridx = 4;
        gbc.weightx = 0.1;
        CustomButton btnClear = new CustomButton("Clear", CustomButton.ButtonType.SECONDARY);
        btnClear.setPreferredSize(new Dimension(80, 32));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            cmbRoleFilter.setSelectedIndex(0);
            cmbStatusFilter.setSelectedIndex(0);
            applyFilter();
        });
        filterRow.add(btnClear, gbc);

        filterCard.add(filterRow, BorderLayout.CENTER);

        // 3. MAIN TABLE & ACTION PANEL CONTAINER
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(filterCard, BorderLayout.NORTH);

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        String[] cols = {"ID", "Full Name", "Email Address", "Mobile", "Role", "Status", "Registered Date", "Last Login"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(32);
        userTable.setFont(FontManager.TABLE_CELL);
        userTable.getTableHeader().setFont(FontManager.TABLE_HEADER);
        userTable.getTableHeader().setBackground(ThemeColors.SURFACE_HOVER);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(7).setPreferredWidth(130);

        // Custom Status & Role Badges Renderer
        userTable.getColumnModel().getColumn(4).setCellRenderer(new RoleBadgeRenderer());
        userTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        userTable.getSelectionModel().addListSelectionListener(e -> updateActionButtons());

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Action Toolbar Below Table
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionToolbar.setOpaque(false);

        btnToggleRole = new CustomButton("Promote to Admin", CustomButton.ButtonType.PRIMARY);
        btnToggleRole.setEnabled(false);
        btnToggleRole.addActionListener(e -> handleRoleToggle());

        btnToggleStatus = new CustomButton("Suspend Account", CustomButton.ButtonType.DANGER);
        btnToggleStatus.setEnabled(false);
        btnToggleStatus.addActionListener(e -> handleStatusToggle());

        CustomButton btnUserDetails = new CustomButton("View Account Info", CustomButton.ButtonType.SECONDARY);
        btnUserDetails.addActionListener(e -> showSelectedUserDetails());

        CustomButton btnResetPassword = new CustomButton("🔑 Reset Password", CustomButton.ButtonType.SECONDARY);
        btnResetPassword.addActionListener(e -> handleAdminResetPassword());

        actionToolbar.add(btnUserDetails);
        actionToolbar.add(btnResetPassword);
        actionToolbar.add(btnToggleRole);
        actionToolbar.add(btnToggleStatus);

        tableCard.add(actionToolbar, BorderLayout.SOUTH);
        centerContainer.add(tableCard, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void updateActionButtons() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentDisplayedUsers.size()) {
            btnToggleRole.setEnabled(false);
            btnToggleStatus.setEnabled(false);
            return;
        }

        User selected = currentDisplayedUsers.get(selectedRow);
        btnToggleRole.setEnabled(true);
        btnToggleStatus.setEnabled(true);

        if (selected.isAdmin()) {
            btnToggleRole.setText("Demote to User");
            btnToggleRole.setButtonType(CustomButton.ButtonType.SECONDARY);
        } else {
            btnToggleRole.setText("Promote to Admin");
            btnToggleRole.setButtonType(CustomButton.ButtonType.PRIMARY);
        }

        if (selected.isSuspended()) {
            btnToggleStatus.setText("Reactivate Account");
            btnToggleStatus.setButtonType(CustomButton.ButtonType.SUCCESS);
        } else {
            btnToggleStatus.setText("Suspend Account");
            btnToggleStatus.setButtonType(CustomButton.ButtonType.DANGER);
        }
    }

    private void handleRoleToggle() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) return;
        User selected = currentDisplayedUsers.get(selectedRow);

        String newRole = selected.isAdmin() ? User.ROLE_USER : User.ROLE_ADMIN;
        String actionVerb = selected.isAdmin() ? "Demote" : "Promote";

        boolean confirmed = ConfirmationDialog.show(
            this,
            actionVerb + " User Role",
            "Are you sure you want to " + actionVerb.toLowerCase() + " " + selected.getFullName() +
            " (" + selected.getEmail() + ") to " + newRole + "?"
        );

        if (!confirmed) return;

        try {
            adminService.changeUserRole(currentAdmin, selected.getUserId(), newRole);
            JOptionPane.showMessageDialog(this,
                "User role successfully updated to " + newRole + ".",
                "Role Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshUsers();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this,
                ise.getMessage(),
                "Action Rejected", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error updating user role: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStatusToggle() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) return;
        User selected = currentDisplayedUsers.get(selectedRow);

        String newStatus = selected.isSuspended() ? User.STATUS_ACTIVE : User.STATUS_SUSPENDED;
        String actionVerb = selected.isSuspended() ? "Reactivate" : "Suspend";

        String reason = "";
        if (!selected.isSuspended()) {
            reason = JOptionPane.showInputDialog(this,
                "Enter reason for suspending account " + selected.getEmail() + ":",
                "Account Suspension", JOptionPane.QUESTION_MESSAGE);
            if (reason == null) return; // user cancelled
        } else {
            boolean confirmed = ConfirmationDialog.show(
                this,
                "Reactivate User Account",
                "Are you sure you want to reactivate the account for " + selected.getFullName() + "?"
            );
            if (!confirmed) return;
        }

        try {
            adminService.changeUserStatus(currentAdmin, selected.getUserId(), newStatus, reason);
            JOptionPane.showMessageDialog(this,
                "Account status successfully changed to " + newStatus + ".",
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshUsers();
        } catch (IllegalStateException ise) {
            JOptionPane.showMessageDialog(this,
                ise.getMessage(),
                "Action Rejected", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error updating account status: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedUserDetails() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the directory first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User selected = currentDisplayedUsers.get(selectedRow);

        String details = "User Profile & Security Details:\n\n" +
                "• User ID: #" + selected.getUserId() + "\n" +
                "• Full Name: " + selected.getFullName() + "\n" +
                "• Email Address: " + selected.getEmail() + "\n" +
                "• Phone: " + (selected.getMobileNumber() != null ? selected.getMobileNumber() : "None registered") + "\n" +
                "• Date of Birth: " + (selected.getDateOfBirth() != null ? selected.getDateOfBirth() : "Not specified") + "\n" +
                "• Security Role: " + selected.getRole() + "\n" +
                "• Account Lifecycle Status: " + selected.getAccountStatus() + "\n" +
                "• Registered Timestamp: " + selected.getCreatedAt() + "\n" +
                "• Last Authentication: " + (selected.getLastLogin() != null ? selected.getLastLogin() : "Never") + "\n\n" +
                "Note: Passwords and derived PBKDF2 keys are never exposed in administrative views.";

        JOptionPane.showMessageDialog(this, details, "User Account Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleAdminResetPassword() {
        int row = userTable.getSelectedRow();
        if (row < 0 || row >= currentDisplayedUsers.size()) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to reset password.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User selected = currentDisplayedUsers.get(row);
        boolean conf = ConfirmationDialog.show(this, "Issue Recovery Token",
                "Generate a single-use verification code for " + selected.getEmail() + " (" + selected.getFullName() + ")?");
        if (conf) {
            try {
                String token = service.PasswordResetService.getInstance().adminInitiateReset(currentAdmin, selected.getUserId());
                JPanel p = new JPanel(new GridLayout(3, 1, 0, 6));
                p.add(new JLabel("Single-use recovery code for " + selected.getEmail() + ":"));
                JTextField tf = new JTextField(token);
                tf.setFont(new Font("Monospaced", Font.BOLD, 18));
                tf.setEditable(false);
                tf.setHorizontalAlignment(JTextField.CENTER);
                p.add(tf);
                p.add(new JLabel("Provide this code securely to the user. Valid for 30 minutes."));
                JOptionPane.showMessageDialog(this, p, "Emergency Password Reset Issued", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error generating reset code: " + ex.getMessage(), "Reset Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshUsers() {
        applyFilter();
    }

    private void applyFilter() {
        String query = txtSearch.getText();
        String roleFilter = (String) cmbRoleFilter.getSelectedItem();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();

        if ("All Roles".equals(roleFilter)) roleFilter = "ALL";
        if ("All Statuses".equals(statusFilter)) statusFilter = "ALL";

        try {
            currentDisplayedUsers = adminService.searchAndFilterUsers(currentAdmin, query, roleFilter, statusFilter);
            tableModel.setRowCount(0);
            DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (User u : currentDisplayedUsers) {
                tableModel.addRow(new Object[]{
                    "#" + u.getUserId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getMobileNumber() != null ? u.getMobileNumber() : "--",
                    u.getRole(),
                    u.getAccountStatus(),
                    u.getCreatedAt() != null ? u.getCreatedAt().format(dFmt) : "--",
                    u.getLastLogin() != null ? u.getLastLogin().format(dtFmt) : "Never"
                });
            }

            lblCountBadge.setText(currentDisplayedUsers.size() + " Users Found");
            btnToggleRole.setEnabled(false);
            btnToggleStatus.setEnabled(false);

        } catch (Exception e) {
            System.err.println("Failed to fetch users: " + e.getMessage());
        }
    }

    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setFont(FontManager.SMALL_BUTTON);
            String role = String.valueOf(value);
            if ("ADMIN".equalsIgnoreCase(role)) {
                lbl.setForeground(ThemeColors.PRIMARY);
                lbl.setText("★ ADMIN");
            } else {
                lbl.setForeground(ThemeColors.TEXT_MUTED);
                lbl.setText("USER");
            }
            return lbl;
        }
    }

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setFont(FontManager.SMALL_BUTTON);
            String status = String.valueOf(value);
            if ("SUSPENDED".equalsIgnoreCase(status)) {
                lbl.setForeground(ThemeColors.EXPENSE);
                lbl.setText("● SUSPENDED");
            } else {
                lbl.setForeground(ThemeColors.INCOME);
                lbl.setText("● ACTIVE");
            }
            return lbl;
        }
    }
}
