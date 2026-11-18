package ui.admin;

import model.AdminAuditEvent;
import model.SystemMetrics;
import model.User;
import service.AdminAnalyticsService;
import service.AuditLogService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import ui.components.MetricCard;
import util.CurrencyFormatter;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen: Admin Overview
 * Displays system-wide aggregated telemetry, user counts, financial inflow/outflow,
 * recent audit events, and operational health indicators.
 */
public class AdminOverviewPanel extends JPanel {
    public interface OverviewNavigationListener {
        void navigateTo(String sectionName);
    }

    private final User currentAdmin;
    private final AdminAnalyticsService analyticsService;
    private final AuditLogService auditLogService;
    private final OverviewNavigationListener navListener;

    private MetricCard cardUsers;
    private MetricCard cardInflow;
    private MetricCard cardOutflow;
    private MetricCard cardHealth;

    private DefaultTableModel auditTableModel;
    private JLabel lblDbDetails;

    public AdminOverviewPanel(User currentAdmin, OverviewNavigationListener navListener) {
        this.currentAdmin = currentAdmin;
        this.navListener = navListener;
        this.analyticsService = new AdminAnalyticsService();
        this.auditLogService = new AuditLogService();

        initUI();
        refreshData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP HEADER & REFRESH BAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Telemetry & Platform Overview");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);

        JLabel lblSubtitle = new JLabel("Aggregated platform metrics, operational health, and real-time audit intelligence");
        lblSubtitle.setFont(FontManager.BODY);
        lblSubtitle.setForeground(ThemeColors.TEXT_MUTED);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        CustomButton btnRefresh = new CustomButton("⟳ Refresh Telemetry", CustomButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. CENTER SCROLLABLE CONTAINER
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new GridBagLayout());
        centerContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 16, 0);

        // A. 4 SUMMARY CARDS
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsGrid.setOpaque(false);

        cardUsers = new MetricCard("Registered Users", "0", "0 Active • 0 Suspended", MetricCard.CardType.BALANCE);
        cardInflow = new MetricCard("Platform Inflow", "₹ 0.00", "Across all user accounts", MetricCard.CardType.INCOME);
        cardOutflow = new MetricCard("Platform Outflow", "₹ 0.00", "Across all transactions", MetricCard.CardType.EXPENSE);
        cardHealth = new MetricCard("Database Status", "Probing...", "Latency: -- ms", MetricCard.CardType.NEUTRAL);

        cardsGrid.add(cardUsers);
        cardsGrid.add(cardInflow);
        cardsGrid.add(cardOutflow);
        cardsGrid.add(cardHealth);

        centerContent.add(cardsGrid, gbc);

        // B. QUICK ACTION SHORTCUTS
        gbc.gridy++;
        CardPanel quickActionsCard = new CardPanel();
        quickActionsCard.setLayout(new BorderLayout(0, 10));
        quickActionsCard.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel lblActionsTitle = new JLabel("Administrative Quick Actions");
        lblActionsTitle.setFont(FontManager.CARD_TITLE);
        lblActionsTitle.setForeground(ThemeColors.TEXT_MAIN);
        quickActionsCard.add(lblActionsTitle, BorderLayout.NORTH);

        JPanel actionsButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsButtonRow.setOpaque(false);

        CustomButton btnGoUsers = new CustomButton("👥 User Management", CustomButton.ButtonType.PRIMARY);
        btnGoUsers.addActionListener(e -> { if (navListener != null) navListener.navigateTo(AdminNavPanel.NAV_USERS); });

        CustomButton btnGoTheme = new CustomButton("🎨 Appearance Studio", CustomButton.ButtonType.SECONDARY);
        btnGoTheme.addActionListener(e -> { if (navListener != null) navListener.navigateTo(AdminNavPanel.NAV_APPEARANCE); });

        CustomButton btnGoContent = new CustomButton("📝 Content Editor", CustomButton.ButtonType.SECONDARY);
        btnGoContent.addActionListener(e -> { if (navListener != null) navListener.navigateTo(AdminNavPanel.NAV_CONTENT); });

        CustomButton btnGoAudit = new CustomButton("📋 View Audit Logs", CustomButton.ButtonType.SECONDARY);
        btnGoAudit.addActionListener(e -> { if (navListener != null) navListener.navigateTo(AdminNavPanel.NAV_AUDIT); });

        CustomButton btnGoDb = new CustomButton("🗄️ Database Probe", CustomButton.ButtonType.SECONDARY);
        btnGoDb.addActionListener(e -> { if (navListener != null) navListener.navigateTo(AdminNavPanel.NAV_DATABASE); });

        actionsButtonRow.add(btnGoUsers);
        actionsButtonRow.add(btnGoTheme);
        actionsButtonRow.add(btnGoContent);
        actionsButtonRow.add(btnGoAudit);
        actionsButtonRow.add(btnGoDb);

        quickActionsCard.add(actionsButtonRow, BorderLayout.CENTER);
        centerContent.add(quickActionsCard, gbc);

        // C. RECENT AUDIT ACTIVITY TABLE
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        CardPanel auditCard = new CardPanel();
        auditCard.setLayout(new BorderLayout(0, 10));
        auditCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel auditHeader = new JPanel(new BorderLayout());
        auditHeader.setOpaque(false);
        JLabel lblRecentAudit = new JLabel("Recent Administrative & Security Operations");
        lblRecentAudit.setFont(FontManager.CARD_TITLE);
        lblRecentAudit.setForeground(ThemeColors.TEXT_MAIN);
        auditHeader.add(lblRecentAudit, BorderLayout.WEST);

        lblDbDetails = new JLabel("Database: Probing...");
        lblDbDetails.setFont(FontManager.SMALL);
        lblDbDetails.setForeground(ThemeColors.TEXT_MUTED);
        auditHeader.add(lblDbDetails, BorderLayout.EAST);

        auditCard.add(auditHeader, BorderLayout.NORTH);

        String[] cols = {"Time", "Actor", "Action Type", "Target", "Outcome", "Description"};
        auditTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable auditTable = new JTable(auditTableModel);
        auditTable.setRowHeight(28);
        auditTable.setFont(FontManager.TABLE_CELL);
        auditTable.getTableHeader().setFont(FontManager.TABLE_HEADER);
        auditTable.getTableHeader().setBackground(ThemeColors.SURFACE_HOVER);

        auditTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(320);

        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.setPreferredSize(new Dimension(800, 240));
        auditCard.add(scrollPane, BorderLayout.CENTER);

        centerContent.add(auditCard, gbc);

        JScrollPane mainScroll = new JScrollPane(centerContent);
        mainScroll.setBorder(null);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        add(mainScroll, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            SystemMetrics m = analyticsService.getSystemMetrics(currentAdmin);

            cardUsers.updateData(
                String.valueOf(m.getTotalUsers()),
                m.getActiveUsers() + " Active • " + m.getSuspendedUsers() + " Suspended • " + m.getAdminCount() + " Admins"
            );

            cardInflow.updateData(
                CurrencyFormatter.format(m.getAggregateIncome().doubleValue()),
                "Across " + m.getTotalTransactions() + " platform transactions"
            );

            cardOutflow.updateData(
                CurrencyFormatter.format(m.getAggregateExpense().doubleValue()),
                "Net Cash Flow: " + CurrencyFormatter.format(m.getAggregateNetCashFlow().doubleValue())
            );

            if (m.isDatabaseOnline()) {
                cardHealth.updateData("ONLINE (MySQL)", "Ping: " + m.getDatabasePingMs() + " ms • " + m.getTotalBudgets() + " Budgets");
                lblDbDetails.setText("MySQL Connected • Latency: " + m.getDatabasePingMs() + "ms");
            } else {
                cardHealth.updateData("OFFLINE (Demo)", "In-Memory Store Active");
                lblDbDetails.setText("Database Offline (Operating in Resilient Demo Mode)");
            }

            // Populate Recent Audit Logs
            auditTableModel.setRowCount(0);
            List<AdminAuditEvent> recentLogs = auditLogService.getRecentLogs(10, "ALL", null);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (AdminAuditEvent log : recentLogs) {
                auditTableModel.addRow(new Object[]{
                    log.getTimestamp() != null ? log.getTimestamp().format(fmt) : "--",
                    log.getActorEmail(),
                    log.getActionType(),
                    log.getTargetType() + (log.getTargetId() != null ? ":" + log.getTargetId() : ""),
                    log.getOutcome(),
                    log.getDescription()
                });
            }

        } catch (Exception e) {
            System.err.println("Error refreshing admin overview data: " + e.getMessage());
        }
    }
}
