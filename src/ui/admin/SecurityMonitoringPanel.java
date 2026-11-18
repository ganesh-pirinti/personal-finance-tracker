package ui.admin;

import dao.AuditLogDAO;
import dao.UserDAO;
import model.AdminAuditEvent;
import model.User;
import service.SessionService;
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
 * Super Admin Panel: Security Monitoring Center.
 * Centralized dashboard monitoring authentication failures, suspended accounts,
 * session anomalies, recovery attempts, and privileged access events.
 */
public class SecurityMonitoringPanel extends JPanel {
    private final User currentAdmin;
    private final AuditLogDAO auditDAO;
    private final UserDAO userDAO;
    private final SessionService sessionService;

    private DefaultTableModel tableModel;
    private JTable securityTable;
    private MetricCard cardFailedLogins;
    private MetricCard cardSuspended;
    private MetricCard cardRevocations;
    private MetricCard cardPrivileged;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SecurityMonitoringPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.auditDAO = new AuditLogDAO();
        this.userDAO = new UserDAO();
        this.sessionService = SessionService.getInstance();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
        refreshData();
    }

    private void initUI() {
        // TOP: Header & Action
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JPanel headerTitle = new JPanel(new BorderLayout());
        headerTitle.setOpaque(false);

        JLabel titleLbl = new JLabel("Security & Threat Monitoring");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Real-time threat detection, authentication failures, credential brute-force telemetry, and privileged audit stream.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subLbl);
        headerTitle.add(titles, BorderLayout.WEST);

        CustomButton btnRefresh = new CustomButton("🔄 Refresh Telemetry", CustomButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(170, 32));
        btnRefresh.addActionListener(e -> refreshData());
        headerTitle.add(btnRefresh, BorderLayout.EAST);
        topContainer.add(headerTitle, BorderLayout.NORTH);

        // Security Metric Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 14, 0));
        metricsPanel.setOpaque(false);

        cardFailedLogins = new MetricCard("Failed Logins", 0, MetricCard.Type.EXPENSE, "Auth failure events");
        cardSuspended = new MetricCard("Suspended Users", 0, MetricCard.Type.WARNING, "Blocked user accounts");
        cardRevocations = new MetricCard("Session Kills", 0, MetricCard.Type.NEUTRAL, "Revoked active tokens");
        cardPrivileged = new MetricCard("Admin Actions", 0, MetricCard.Type.INCOME, "Elevated operations");

        metricsPanel.add(cardFailedLogins);
        metricsPanel.add(cardSuspended);
        metricsPanel.add(cardRevocations);
        metricsPanel.add(cardPrivileged);
        topContainer.add(metricsPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // CENTER: Security Event Log Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);

        JLabel tblHeader = new JLabel("Security Event Stream (Filter: AUTH, RECOVERY, ROLES, SESSIONS)");
        tblHeader.setFont(FontManager.CARD_TITLE);
        tblHeader.setForeground(ThemeColors.TEXT_MAIN);
        centerPanel.add(tblHeader, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Timestamp", "Principal", "Action", "Target", "Severity / Status", "Details"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        securityTable = new JTable(tableModel);
        securityTable.setRowHeight(30);
        securityTable.setFont(FontManager.BODY);
        securityTable.getTableHeader().setFont(FontManager.FORM_LABEL);
        securityTable.getTableHeader().setBackground(ThemeColors.SURFACE);
        securityTable.getTableHeader().setForeground(ThemeColors.TEXT_MAIN);

        securityTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        securityTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        securityTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        securityTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        securityTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String st = String.valueOf(val);
                if ("SUCCESS".equalsIgnoreCase(st)) {
                    setForeground(ThemeColors.INCOME);
                } else if ("FAILURE".equalsIgnoreCase(st) || "BLOCKED".equalsIgnoreCase(st)) {
                    setForeground(ThemeColors.EXPENSE);
                } else {
                    setForeground(ThemeColors.WARNING);
                }
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(securityTable);
        scroll.setBorder(BorderFactory.createLineBorder(ThemeColors.CARD_BORDER));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        List<AdminAuditEvent> allLogs;
        List<User> users;
        try {
            allLogs = auditDAO.findRecent(300, "ALL", "");
            users = userDAO.findAll();
        } catch (Exception e) {
            allLogs = new java.util.ArrayList<>();
            users = new java.util.ArrayList<>();
        }

        int failedLogins = 0;
        int privilegedCount = 0;
        int sessionKills = 0;

        tableModel.setRowCount(0);

        for (AdminAuditEvent log : allLogs) {
            String act = log.getActionType() != null ? log.getActionType().toUpperCase() : "";

            if (act.contains("LOGIN_FAILED") || act.contains("LOGIN_BLOCKED") || act.contains("RECOVERY_FAILED")) {
                failedLogins++;
            }
            if (act.contains("ADMIN") || act.contains("BOOTSTRAP") || act.contains("ROLE_CHANGE") || act.contains("USER_SUSPEND")) {
                privilegedCount++;
            }
            if (act.contains("REVOKE")) {
                sessionKills++;
            }

            // Filter security relevant actions
            if (act.contains("LOGIN") || act.contains("RECOVERY") || act.contains("PASSWORD") ||
                    act.contains("SESSION") || act.contains("ROLE") || act.contains("SUSPEND") ||
                    act.contains("BOOTSTRAP") || act.contains("SECURITY") || "FAILURE".equalsIgnoreCase(log.getOutcome())) {

                tableModel.addRow(new Object[]{
                        log.getEventId(),
                        log.getTimestamp() != null ? log.getTimestamp().format(FMT) : "-",
                        log.getActorEmail() != null ? log.getActorEmail() : "Anonymous",
                        log.getActionType(),
                        log.getTargetType() + " (" + log.getTargetId() + ")",
                        log.getOutcome(),
                        log.getDescription()
                });
            }
        }

        int suspendedUsers = 0;
        for (User u : users) {
            if (u.isSuspended()) suspendedUsers++;
        }

        cardFailedLogins.setValue(failedLogins);
        cardSuspended.setValue(suspendedUsers);
        cardRevocations.setValue(sessionKills);
        cardPrivileged.setValue(privilegedCount);
    }
}
