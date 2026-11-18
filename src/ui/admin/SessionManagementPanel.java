package ui.admin;

import model.User;
import model.UserSession;
import service.SessionService;
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
import java.util.Map;

/**
 * Super Admin Panel: Session Management Center.
 * Tracks active desktop user sessions, displays client workstations, monitors heartbeats,
 * and allows administrators to revoke individual sessions, all sessions for a user, or other admins.
 */
public class SessionManagementPanel extends JPanel {
    private final User currentAdmin;
    private final SessionService sessionService;

    private DefaultTableModel tableModel;
    private JTable sessionTable;
    private JComboBox<String> filterCombo;
    private JTextField txtSearch;
    private List<UserSession> currentSessions;

    private MetricCard cardActive;
    private MetricCard cardTotal;
    private MetricCard cardRevoked;
    private MetricCard cardAdmin;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SessionManagementPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.sessionService = SessionService.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP SECTION: Header and Summary Cards
        JPanel topContainer = new JPanel(new BorderLayout(0, 14));
        topContainer.setOpaque(false);

        // Header Title
        JPanel headerTitle = new JPanel(new BorderLayout());
        headerTitle.setOpaque(false);

        JLabel titleLbl = new JLabel("Desktop Session Management");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Audit active user authentication tokens, enforce session lifecycles, and terminate unauthorized connections.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subLbl);
        headerTitle.add(titles, BorderLayout.WEST);

        // Top Action Buttons
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        CustomButton btnRefresh = new CustomButton("🔄 Refresh", CustomButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> refreshData());

        CustomButton btnRevokeAdmins = new CustomButton("🛡️ Revoke Other Admins", CustomButton.Style.OUTLINE);
        btnRevokeAdmins.setPreferredSize(new Dimension(175, 32));
        btnRevokeAdmins.addActionListener(e -> handleRevokeOtherAdmins());

        actionBtns.add(btnRefresh);
        actionBtns.add(btnRevokeAdmins);
        headerTitle.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerTitle, BorderLayout.NORTH);

        // Summary Metric Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsPanel.setOpaque(false);

        cardActive = new MetricCard("Active Sessions", 0, MetricCard.Type.INCOME, "Currently authenticated");
        cardTotal = new MetricCard("Total Registered", 0, MetricCard.Type.NEUTRAL, "Lifetime desktop sessions");
        cardRevoked = new MetricCard("Revoked / Expired", 0, MetricCard.Type.EXPENSE, "Terminated sessions");
        cardAdmin = new MetricCard("Active Admins", 0, MetricCard.Type.NEUTRAL, "Privileged active sessions");

        metricsPanel.add(cardActive);
        metricsPanel.add(cardTotal);
        metricsPanel.add(cardRevoked);
        metricsPanel.add(cardAdmin);

        topContainer.add(metricsPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // CENTER SECTION: Search, Filter, and Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Search & Filter Bar
        JPanel filterBar = new JPanel(new BorderLayout());
        filterBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(16);
        txtSearch.setFont(FontManager.FORM_INPUT);
        searchPanel.add(txtSearch);

        searchPanel.add(new JLabel("Status:"));
        filterCombo = new JComboBox<>(new String[]{"All Sessions", "ACTIVE", "EXPIRED", "REVOKED"});
        filterCombo.setFont(FontManager.BODY);
        filterCombo.addActionListener(e -> filterSessions());
        searchPanel.add(filterCombo);

        CustomButton btnFilter = new CustomButton("Filter", CustomButton.Style.SECONDARY);
        btnFilter.setPreferredSize(new Dimension(75, 28));
        btnFilter.addActionListener(e -> filterSessions());
        searchPanel.add(btnFilter);

        filterBar.add(searchPanel, BorderLayout.WEST);
        centerPanel.add(filterBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Session ID", "User Account", "Full Name", "Role", "Client / OS", "Created", "Last Active", "Expires", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        sessionTable = new JTable(tableModel);
        sessionTable.setRowHeight(32);
        sessionTable.setFont(FontManager.BODY);
        sessionTable.getTableHeader().setFont(FontManager.FORM_LABEL);
        sessionTable.getTableHeader().setBackground(ThemeColors.SURFACE);
        sessionTable.getTableHeader().setForeground(ThemeColors.TEXT_MAIN);
        sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column Renderers
        sessionTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        sessionTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        sessionTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        sessionTable.getColumnModel().getColumn(8).setPreferredWidth(85);

        sessionTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String st = String.valueOf(val);
                if (st.contains("ACTIVE")) {
                    setForeground(ThemeColors.INCOME);
                } else if (st.contains("EXPIRED")) {
                    setForeground(ThemeColors.WARNING);
                } else {
                    setForeground(ThemeColors.EXPENSE);
                }
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(sessionTable);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.CARD_BORDER));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Action Toolbar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottomBar.setOpaque(false);

        CustomButton btnRevokeUser = new CustomButton("Revoke All for User", CustomButton.Style.OUTLINE);
        btnRevokeUser.setPreferredSize(new Dimension(170, 32));
        btnRevokeUser.addActionListener(e -> handleRevokeAllForUser());

        CustomButton btnRevokeSingle = new CustomButton("🚫 Revoke Selected Session", CustomButton.Style.EXPENSE);
        btnRevokeSingle.setPreferredSize(new Dimension(195, 32));
        btnRevokeSingle.addActionListener(e -> handleRevokeSingle());

        bottomBar.add(btnRevokeUser);
        bottomBar.add(btnRevokeSingle);
        centerPanel.add(bottomBar, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        currentSessions = sessionService.getAllSessions();
        Map<String, Integer> metrics = sessionService.getSessionMetrics();

        cardActive.setValue(metrics.getOrDefault("active", 0));
        cardTotal.setValue(metrics.getOrDefault("total", 0));
        cardRevoked.setValue(metrics.getOrDefault("revoked", 0) + metrics.getOrDefault("expired", 0));
        cardAdmin.setValue(metrics.getOrDefault("adminSessions", 0));

        filterSessions();
    }

    private void filterSessions() {
        if (currentSessions == null) return;

        String selectedFilter = (String) filterCombo.getSelectedItem();
        String query = txtSearch.getText().trim().toLowerCase();

        tableModel.setRowCount(0);
        String mySessionId = SessionService.getCurrentSessionId();

        for (UserSession s : currentSessions) {
            boolean matchesStatus = "All Sessions".equals(selectedFilter) || s.getStatus().equalsIgnoreCase(selectedFilter);
            boolean matchesSearch = query.isEmpty() ||
                    s.getUserEmail().toLowerCase().contains(query) ||
                    s.getUserFullName().toLowerCase().contains(query) ||
                    s.getSessionId().toLowerCase().contains(query);

            if (matchesStatus && matchesSearch) {
                String sidDisplay = s.getMaskedSessionId();
                if (mySessionId != null && mySessionId.equals(s.getSessionId())) {
                    sidDisplay += " (YOU)";
                }

                tableModel.addRow(new Object[]{
                        sidDisplay,
                        s.getUserEmail(),
                        s.getUserFullName(),
                        s.getRole(),
                        s.getClientInfo() != null ? s.getClientInfo() : "Desktop",
                        s.getCreatedAt() != null ? s.getCreatedAt().format(FMT) : "-",
                        s.getLastActivity() != null ? s.getLastActivity().format(FMT) : "-",
                        s.getExpiresAt() != null ? s.getExpiresAt().format(FMT) : "-",
                        s.getStatus()
                });
            }
        }
    }

    private void handleRevokeSingle() {
        int sel = sessionTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session from the table to revoke.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserSession target = currentSessions.get(sel);
        if (!target.isActive()) {
            JOptionPane.showMessageDialog(this, "The selected session is already " + target.getStatus() + ".", "Cannot Revoke", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String mySessionId = SessionService.getCurrentSessionId();
        boolean isSelf = mySessionId != null && mySessionId.equals(target.getSessionId());

        String promptMsg = isSelf ?
                "WARNING: You are about to revoke your CURRENT admin session. You will be logged out immediately. Proceed?" :
                "Are you sure you want to revoke session " + target.getMaskedSessionId() + " for " + target.getUserEmail() + "?";

        boolean conf = ConfirmationDialog.show(this, "Confirm Session Revocation", promptMsg);
        if (conf) {
            boolean ok = sessionService.revokeSession(target.getSessionId(), currentAdmin);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Session terminated successfully.", "Revoked", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
                if (isSelf) {
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w != null) w.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to revoke session.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRevokeAllForUser() {
        int sel = sessionTable.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session to identify the user.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserSession target = currentSessions.get(sel);
        boolean conf = ConfirmationDialog.show(this, "Revoke All User Sessions",
                "Are you sure you want to revoke all active sessions for " + target.getUserEmail() + "?");
        if (conf) {
            int count = sessionService.revokeAllForUser(target.getUserId(), currentAdmin);
            JOptionPane.showMessageDialog(this, "Terminated " + count + " active session(s) for " + target.getUserEmail() + ".",
                    "Sessions Revoked", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        }
    }

    private void handleRevokeOtherAdmins() {
        boolean conf = ConfirmationDialog.show(this, "Revoke Other Administrator Sessions",
                "This will immediately invalidate all other active administrator sessions while keeping your current session active.\nDo you want to proceed?");
        if (conf) {
            int count = sessionService.revokeOtherAdmins(SessionService.getCurrentSessionId(), currentAdmin);
            JOptionPane.showMessageDialog(this, "Successfully revoked " + count + " other administrator session(s).",
                    "Admin Sessions Invalidation", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        }
    }
}
