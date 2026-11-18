package ui.admin;

import model.PasswordResetToken;
import model.User;
import service.AdminManagementService;
import service.PasswordResetService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import ui.components.MetricCard;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Super Admin Panel: Authentication & Password Recovery Center.
 * Enables administrators to inspect password recovery requests (token hashes only),
 * revoke pending tokens, initiate verified manual resets for users, and observe rate limits.
 */
public class AuthRecoveryPanel extends JPanel {
    private final User currentAdmin;
    private final PasswordResetService resetService;
    private final AdminManagementService adminService;

    private DefaultTableModel tableModel;
    private JTable tokenTable;
    private JLabel lblTotalRequests;
    private JLabel lblActiveTokens;
    private JLabel lblRevokedTokens;
    private List<PasswordResetToken> tokenList;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuthRecoveryPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.resetService = PasswordResetService.getInstance();
        this.adminService = new AdminManagementService();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        // 1. TOP HEADER & METRICS
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        // Header Title
        JPanel headerTitle = new JPanel(new BorderLayout());
        headerTitle.setOpaque(false);
        JLabel titleLbl = new JLabel("Authentication & Recovery Management");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Inspect single-use recovery token lifecycle, monitor abuse, and issue admin-assisted reset codes.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subLbl);
        headerTitle.add(titles, BorderLayout.WEST);

        // Action Buttons
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        CustomButton btnRefresh = new CustomButton("🔄 Refresh", CustomButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> refreshData());

        CustomButton btnManualReset = new CustomButton("🔑 Generate User Reset Code", CustomButton.Style.PRIMARY);
        btnManualReset.setPreferredSize(new Dimension(210, 32));
        btnManualReset.addActionListener(e -> handleManualReset());

        actionBtns.add(btnRefresh);
        actionBtns.add(btnManualReset);
        headerTitle.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerTitle, BorderLayout.NORTH);

        // Metric Summary Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsPanel.setOpaque(false);

        lblTotalRequests = new JLabel("0", SwingConstants.CENTER);
        lblActiveTokens = new JLabel("0", SwingConstants.CENTER);
        lblRevokedTokens = new JLabel("0", SwingConstants.CENTER);

        metricsPanel.add(new MetricCard("Total Token Requests", 0, MetricCard.Type.NEUTRAL, "Lifetime requests logged"));
        metricsPanel.add(new MetricCard("Active Outstanding Tokens", 0, MetricCard.Type.INCOME, "Eligible for verification"));
        metricsPanel.add(new MetricCard("Revoked / Expired", 0, MetricCard.Type.EXPENSE, "Invalidated or timed out"));

        topContainer.add(metricsPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // 2. TABLE OF RECOVERY TOKENS
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        JLabel tblHeader = new JLabel("Recovery Token Audit Trail (Cryptographic Hashes Stored)");
        tblHeader.setFont(FontManager.CARD_TITLE);
        tblHeader.setForeground(ThemeColors.TEXT_MAIN);
        centerPanel.add(tblHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "User Account", "Full Name", "Token Hash (SHA-256)", "Status", "Issued At", "Expires At", "Client IP"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tokenTable = new JTable(tableModel);
        tokenTable.setRowHeight(32);
        tokenTable.setFont(FontManager.BODY);
        tokenTable.getTableHeader().setFont(FontManager.FORM_LABEL);
        tokenTable.getTableHeader().setBackground(ThemeColors.SURFACE);
        tokenTable.getTableHeader().setForeground(ThemeColors.TEXT_MAIN);
        tokenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align ID and Status
        DefaultTableCellRenderer centerRend = new DefaultTableCellRenderer();
        centerRend.setHorizontalAlignment(SwingConstants.CENTER);
        tokenTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        tokenTable.getColumnModel().getColumn(0).setCellRenderer(centerRend);
        tokenTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        tokenTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        tokenTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String st = String.valueOf(val);
                if (PasswordResetToken.STATUS_PENDING.equalsIgnoreCase(st)) {
                    setForeground(ThemeColors.INCOME);
                } else if (PasswordResetToken.STATUS_USED.equalsIgnoreCase(st)) {
                    setForeground(ThemeColors.ACCENT);
                } else {
                    setForeground(ThemeColors.EXPENSE);
                }
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(tokenTable);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.CARD_BORDER));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Action Toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottomBar.setOpaque(false);

        CustomButton btnRevoke = new CustomButton("🚫 Revoke Selected Token", CustomButton.Style.EXPENSE);
        btnRevoke.setPreferredSize(new Dimension(190, 32));
        btnRevoke.addActionListener(e -> handleRevokeToken());
        bottomBar.add(btnRevoke);

        centerPanel.add(bottomBar, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        tokenList = resetService.getAllTokens();
        tableModel.setRowCount(0);

        int active = 0;
        int invalid = 0;

        for (PasswordResetToken t : tokenList) {
            if (t.isPending()) active++;
            else invalid++;

            tableModel.addRow(new Object[]{
                    t.getTokenId(),
                    t.getUserEmail(),
                    t.getUserFullName(),
                    t.getMaskedTokenHash(),
                    t.getStatus(),
                    t.getCreatedAt() != null ? t.getCreatedAt().format(FMT) : "-",
                    t.getExpiresAt() != null ? t.getExpiresAt().format(FMT) : "-",
                    t.getClientIp()
            });
        }
    }

    private void handleRevokeToken() {
        int sel = tokenTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Please select a token from the table to revoke.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PasswordResetToken token = tokenList.get(sel);
        if (!token.isPending()) {
            JOptionPane.showMessageDialog(this, "The selected token is already " + token.getStatus() + ".", "Cannot Revoke", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        boolean conf = ConfirmationDialog.show(this, "Revoke Token",
                "Are you sure you want to revoke recovery token #" + token.getTokenId() + " for " + token.getUserEmail() + "?");
        if (conf) {
            boolean ok = resetService.adminRevokeToken(currentAdmin, token.getTokenId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Recovery token successfully revoked.", "Token Revoked", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to revoke token.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleManualReset() {
        List<User> users;
        try {
            users = adminService.getAllUsers(currentAdmin);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> userCombo = new JComboBox<>();
        for (User u : users) {
            userCombo.addItem(u.getUserId() + ": " + u.getEmail() + " (" + u.getFullName() + ")");
        }

        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.add(new JLabel("Select user account to issue emergency one-time reset code:"));
        p.add(userCombo);

        int res = JOptionPane.showConfirmDialog(this, p, "Admin Assisted Password Reset", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            int selIdx = userCombo.getSelectedIndex();
            if (selIdx >= 0) {
                User target = users.get(selIdx);
                try {
                    String rawCode = resetService.adminInitiateReset(currentAdmin, target.getUserId());
                    refreshData();

                    JPanel resultPanel = new JPanel(new GridLayout(3, 1, 0, 6));
                    resultPanel.add(new JLabel("A single-use recovery code was generated for " + target.getEmail() + ":"));
                    JTextField txtCode = new JTextField(rawCode);
                    txtCode.setFont(new Font("Monospaced", Font.BOLD, 18));
                    txtCode.setEditable(false);
                    txtCode.setHorizontalAlignment(JTextField.CENTER);
                    resultPanel.add(txtCode);
                    resultPanel.add(new JLabel("Provide this code securely to the user. Valid for 30 minutes."));

                    JOptionPane.showMessageDialog(this, resultPanel, "Manual Reset Code Issued", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
