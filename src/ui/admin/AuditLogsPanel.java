package ui.admin;

import model.AdminAuditEvent;
import model.User;
import service.AuditLogService;
import ui.components.CardPanel;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen: Activity & Security Audit Trail
 * Displays an immutable chronological record of administrative actions, role assignments,
 * configuration updates, and security events with CSV export capabilities.
 */
public class AuditLogsPanel extends JPanel {
    private final User currentAdmin;
    private final AuditLogService auditLogService;

    private JTextField txtSearch;
    private JComboBox<String> cmbActionFilter;
    private JTable logsTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalCount;

    private List<AdminAuditEvent> displayedLogs = new ArrayList<>();

    public AuditLogsPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.auditLogService = new AuditLogService();

        initUI();
        refreshLogs();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 1. TOP TITLE BAR
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Administrative Audit Trail & Activity Logs");
        lblTitle.setFont(FontManager.SECTION_TITLE);
        lblTitle.setForeground(ThemeColors.TEXT_MAIN);
        titlePanel.add(lblTitle);

        lblTotalCount = new JLabel("0 Events Recorded");
        lblTotalCount.setFont(FontManager.SMALL_BUTTON);
        lblTotalCount.setForeground(ThemeColors.PRIMARY);
        titlePanel.add(lblTotalCount);

        headerPanel.add(titlePanel, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        CustomButton btnExport = new CustomButton("Export Audit CSV", CustomButton.ButtonType.SECONDARY);
        btnExport.addActionListener(e -> exportToCsv());
        btnRow.add(btnExport);

        CustomButton btnRefresh = new CustomButton("⟳ Refresh Logs", CustomButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshLogs());
        btnRow.add(btnRefresh);

        headerPanel.add(btnRow, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. SEARCH & ACTION FILTER TOOLBAR
        CardPanel filterCard = new CardPanel();
        filterCard.setLayout(new GridBagLayout());
        filterCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        txtSearch = new JTextField();
        txtSearch.setFont(FontManager.FORM_INPUT);
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by actor email, target, or keywords...");
        txtSearch.addActionListener(e -> refreshLogs());
        filterCard.add(txtSearch, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        cmbActionFilter = new JComboBox<>(new String[]{
            "All Actions",
            "USER_ROLE_CHANGE",
            "USER_STATUS_CHANGE",
            "THEME_PUBLISH",
            "TEXT_UPDATE",
            "CONFIG_UPDATE",
            "BOOTSTRAP_ADMIN",
            "LOGIN_SUCCESS",
            "LOGIN_FAILED",
            "LOGIN_BLOCKED"
        });
        cmbActionFilter.setFont(FontManager.FORM_INPUT);
        cmbActionFilter.setPreferredSize(new Dimension(170, 32));
        cmbActionFilter.addActionListener(e -> refreshLogs());
        filterCard.add(cmbActionFilter, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        CustomButton btnFilter = new CustomButton("Filter", CustomButton.ButtonType.PRIMARY);
        btnFilter.setPreferredSize(new Dimension(80, 32));
        btnFilter.addActionListener(e -> refreshLogs());
        filterCard.add(btnFilter, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.1;
        CustomButton btnClear = new CustomButton("Clear", CustomButton.ButtonType.SECONDARY);
        btnClear.setPreferredSize(new Dimension(80, 32));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            cmbActionFilter.setSelectedIndex(0);
            refreshLogs();
        });
        filterCard.add(btnClear, gbc);

        // 3. TABLE CARD
        JPanel centerContainer = new JPanel(new BorderLayout(0, 12));
        centerContainer.setOpaque(false);
        centerContainer.add(filterCard, BorderLayout.NORTH);

        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] cols = {"Event ID", "Timestamp", "Actor Email", "Action Type", "Target", "Outcome", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        logsTable = new JTable(tableModel);
        logsTable.setRowHeight(30);
        logsTable.setFont(FontManager.TABLE_CELL);
        logsTable.getTableHeader().setFont(FontManager.TABLE_HEADER);
        logsTable.getTableHeader().setBackground(ThemeColors.SURFACE_HOVER);
        logsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        logsTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        logsTable.getColumnModel().getColumn(1).setPreferredWidth(135);
        logsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        logsTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        logsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        logsTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        logsTable.getColumnModel().getColumn(6).setPreferredWidth(320);

        logsTable.getColumnModel().getColumn(5).setCellRenderer(new OutcomeBadgeRenderer());

        JScrollPane scrollTable = new JScrollPane(logsTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // Details bar below table
        JPanel detailsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        detailsBar.setOpaque(false);

        CustomButton btnViewDetail = new CustomButton("View Full Audit Entry", CustomButton.ButtonType.SECONDARY);
        btnViewDetail.addActionListener(e -> viewSelectedLog());
        detailsBar.add(btnViewDetail);

        tableCard.add(detailsBar, BorderLayout.SOUTH);
        centerContainer.add(tableCard, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    public void refreshLogs() {
        String query = txtSearch.getText().trim();
        String filter = (String) cmbActionFilter.getSelectedItem();
        if ("All Actions".equals(filter)) filter = "ALL";

        displayedLogs = auditLogService.getRecentLogs(150, filter, query);
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AdminAuditEvent log : displayedLogs) {
            tableModel.addRow(new Object[]{
                "#" + log.getEventId(),
                log.getTimestamp() != null ? log.getTimestamp().format(fmt) : "--",
                log.getActorEmail(),
                log.getActionType(),
                log.getTargetType() + (log.getTargetId() != null ? ":" + log.getTargetId() : ""),
                log.getOutcome(),
                log.getDescription()
            });
        }

        lblTotalCount.setText(displayedLogs.size() + " Audit Entries Loaded");
    }

    private void viewSelectedLog() {
        int row = logsTable.getSelectedRow();
        if (row < 0 || row >= displayedLogs.size()) {
            JOptionPane.showMessageDialog(this, "Please select an audit log record from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AdminAuditEvent log = displayedLogs.get(row);
        String details = "Audit Log Record #" + log.getEventId() + "\n\n" +
                "• Timestamp: " + log.getTimestamp() + "\n" +
                "• Actor User ID: #" + log.getActorUserId() + "\n" +
                "• Actor Email: " + log.getActorEmail() + "\n" +
                "• Action Type: " + log.getActionType() + "\n" +
                "• Target Entity: " + log.getTargetType() + "\n" +
                "• Target ID: " + log.getTargetId() + "\n" +
                "• Operation Outcome: " + log.getOutcome() + "\n\n" +
                "Detailed Description:\n" + log.getDescription();

        JOptionPane.showMessageDialog(this, details, "Audit Log Record #" + log.getEventId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportToCsv() {
        if (displayedLogs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No audit log records available to export.", "Empty Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("audit_logs_export.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".csv")) {
                f = new File(f.getAbsolutePath() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("Event ID,Timestamp,Actor User ID,Actor Email,Action Type,Target Type,Target ID,Outcome,Description");
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (AdminAuditEvent log : displayedLogs) {
                    pw.println(String.format("%d,\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                        log.getEventId(),
                        log.getTimestamp() != null ? log.getTimestamp().format(fmt) : "",
                        log.getActorUserId(),
                        log.getActorEmail(),
                        log.getActionType(),
                        log.getTargetType(),
                        log.getTargetId() != null ? log.getTargetId() : "",
                        log.getOutcome(),
                        log.getDescription().replace("\"", "\"\"")
                    ));
                }

                JOptionPane.showMessageDialog(this,
                    "Successfully exported " + displayedLogs.size() + " audit records to:\n" + f.getAbsolutePath(),
                    "Audit Export Successful", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to export audit logs: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class OutcomeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setFont(FontManager.SMALL_BUTTON);
            String outcome = String.valueOf(value);
            if ("SUCCESS".equalsIgnoreCase(outcome)) {
                lbl.setForeground(ThemeColors.INCOME);
            } else if ("FAILURE".equalsIgnoreCase(outcome)) {
                lbl.setForeground(ThemeColors.EXPENSE);
            } else if ("BLOCKED".equalsIgnoreCase(outcome)) {
                lbl.setForeground(ThemeColors.WARNING);
            }
            return lbl;
        }
    }
}
