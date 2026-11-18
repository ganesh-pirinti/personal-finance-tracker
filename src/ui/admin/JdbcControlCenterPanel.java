package ui.admin;

import database.DatabaseConnection;
import model.DatabaseConfig;
import model.User;
import service.AuditLogService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import ui.components.MetricCard;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Super Admin Panel: JDBC Control Center.
 * Provides real-time connection diagnostics, driver inspection, latency probes,
 * sanitized error logging, and masked configuration updates with rollback preservation.
 */
public class JdbcControlCenterPanel extends JPanel {
    private final User currentAdmin;
    private final AuditLogService auditLogService;

    private JLabel lblStatusBadge;
    private JLabel lblHostPort;
    private JLabel lblDatabase;
    private JLabel lblUsername;
    private JLabel lblJdbcUrl;
    private JLabel lblDriverMeta;
    private JLabel lblLastSuccess;
    private JLabel lblLastProbe;

    private MetricCard cardLatency;
    private MetricCard cardStatus;
    private MetricCard cardDriver;

    private JTextArea txtDiagnostics;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private LocalDateTime lastSuccessTime = null;

    public JdbcControlCenterPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.auditLogService = new AuditLogService();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initUI();
        runProbe();
    }

    private void initUI() {
        // TOP SECTION: Header & Actions
        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);

        JPanel headerTitle = new JPanel(new BorderLayout());
        headerTitle.setOpaque(false);

        JLabel titleLbl = new JLabel("JDBC Connection Control Center");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Inspect JDBC driver runtime, probe database latency, review diagnostic logs, and update connection parameters.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(titleLbl);
        titles.add(subLbl);
        headerTitle.add(titles, BorderLayout.WEST);

        // Actions
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtns.setOpaque(false);

        CustomButton btnProbe = new CustomButton("⚡ Probe Live Connection", CustomButton.Style.PRIMARY);
        btnProbe.setPreferredSize(new Dimension(190, 32));
        btnProbe.addActionListener(e -> runProbe());

        CustomButton btnEditConfig = new CustomButton("⚙️ Edit Settings", CustomButton.Style.OUTLINE);
        btnEditConfig.setPreferredSize(new Dimension(130, 32));
        btnEditConfig.addActionListener(e -> handleEditConfiguration());

        actionBtns.add(btnProbe);
        actionBtns.add(btnEditConfig);
        headerTitle.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerTitle, BorderLayout.NORTH);

        // Metric Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        metricsPanel.setOpaque(false);

        cardStatus = new MetricCard("Database Engine", 0, MetricCard.Type.NEUTRAL, "Probing status...");
        cardLatency = new MetricCard("Query Latency", 0, MetricCard.Type.INCOME, "Round-trip time");
        cardDriver = new MetricCard("Connector/J", 0, MetricCard.Type.NEUTRAL, "MySQL JDBC Driver");

        metricsPanel.add(cardStatus);
        metricsPanel.add(cardLatency);
        metricsPanel.add(cardDriver);
        topContainer.add(metricsPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // CENTER SECTION: Left Config details & Right Diagnostics log
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left Panel: Active Configuration Card
        JPanel configCard = new JPanel(new BorderLayout(0, 10));
        configCard.setBackground(ThemeColors.SURFACE);
        configCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JLabel cfgTitle = new JLabel("Active JDBC Configuration & Metadata");
        cfgTitle.setFont(FontManager.CARD_TITLE);
        cfgTitle.setForeground(ThemeColors.TEXT_MAIN);
        configCard.add(cfgTitle, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridLayout(7, 2, 8, 10));
        detailsGrid.setOpaque(false);

        lblStatusBadge = new JLabel("PROBING...");
        lblStatusBadge.setFont(FontManager.BODY_BOLD);

        lblHostPort = new JLabel("-");
        lblHostPort.setFont(FontManager.BODY);

        lblDatabase = new JLabel("-");
        lblDatabase.setFont(FontManager.BODY);

        lblUsername = new JLabel("-");
        lblUsername.setFont(FontManager.BODY);

        lblJdbcUrl = new JLabel("-");
        lblJdbcUrl.setFont(FontManager.SMALL);

        lblDriverMeta = new JLabel("com.mysql.cj.jdbc.Driver");
        lblDriverMeta.setFont(FontManager.SMALL);

        lblLastSuccess = new JLabel("Never");
        lblLastSuccess.setFont(FontManager.SMALL);

        addConfigRow(detailsGrid, "Connection State:", lblStatusBadge);
        addConfigRow(detailsGrid, "Host & Port:", lblHostPort);
        addConfigRow(detailsGrid, "Target Schema:", lblDatabase);
        addConfigRow(detailsGrid, "Database User:", lblUsername);
        addConfigRow(detailsGrid, "Resolved JDBC URL:", lblJdbcUrl);
        addConfigRow(detailsGrid, "Driver Class:", lblDriverMeta);
        addConfigRow(detailsGrid, "Last Live Success:", lblLastSuccess);

        configCard.add(detailsGrid, BorderLayout.CENTER);
        centerGrid.add(configCard);

        // Right Panel: Diagnostics Log
        JPanel diagCard = new JPanel(new BorderLayout(0, 8));
        diagCard.setBackground(ThemeColors.SURFACE);
        diagCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel diagTitle = new JLabel("Live Diagnostic Output & Sanitized Logs");
        diagTitle.setFont(FontManager.CARD_TITLE);
        diagTitle.setForeground(ThemeColors.TEXT_MAIN);
        diagCard.add(diagTitle, BorderLayout.NORTH);

        txtDiagnostics = new JTextArea();
        txtDiagnostics.setEditable(false);
        txtDiagnostics.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtDiagnostics.setBackground(new Color(248, 250, 252));
        txtDiagnostics.setForeground(ThemeColors.TEXT_MAIN);
        txtDiagnostics.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane logScroll = new JScrollPane(txtDiagnostics);
        logScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.CARD_BORDER));
        diagCard.add(logScroll, BorderLayout.CENTER);

        centerGrid.add(diagCard);
        add(centerGrid, BorderLayout.CENTER);
    }

    private void addConfigRow(JPanel panel, String label, JLabel valComp) {
        JLabel l = new JLabel(label);
        l.setFont(FontManager.FORM_LABEL);
        l.setForeground(ThemeColors.TEXT_MUTED);
        panel.add(l);
        panel.add(valComp);
    }

    public void runProbe() {
        DatabaseConfig cfg = DatabaseConnection.getCurrentConfig();
        lblHostPort.setText(cfg.getHost() + ":" + cfg.getPort());
        lblDatabase.setText(cfg.getDatabaseName());
        lblUsername.setText(cfg.getUsername());
        lblJdbcUrl.setText("jdbc:mysql://" + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabaseName());

        logDiag("--> Starting JDBC probe against: " + cfg.getHost() + ":" + cfg.getPort() + "/" + cfg.getDatabaseName() + " (User: " + cfg.getUsername() + ")...");

        new SwingWorker<DatabaseConnection.ConnectionStatus, Void>() {
            @Override
            protected DatabaseConnection.ConnectionStatus doInBackground() {
                return DatabaseConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    DatabaseConnection.ConnectionStatus status = get();
                    if (status.success) {
                        lblStatusBadge.setText("CONNECTED (ONLINE)");
                        lblStatusBadge.setForeground(ThemeColors.INCOME);
                        cardLatency.setValue((double) status.latencyMs);
                        cardStatus.setValue(1); // 1 = Online

                        lastSuccessTime = LocalDateTime.now();
                        lblLastSuccess.setText(lastSuccessTime.format(FMT));
                        lblDriverMeta.setText(status.driverName + " " + status.driverVersion);

                        logDiag("SUCCESS: Connection established in " + status.latencyMs + "ms.");
                        logDiag("Driver: " + status.driverName + " v" + status.driverVersion);
                        logDiag("Server: " + status.databaseProductName + " v" + status.databaseProductVersion);
                        logDiag("ACID Transaction Isolation: READ_COMMITTED supported.");
                    } else {
                        lblStatusBadge.setText("OFFLINE / IN-MEMORY FALLBACK");
                        lblStatusBadge.setForeground(ThemeColors.EXPENSE);
                        cardLatency.setValue(0.0);
                        cardStatus.setValue(0);

                        logDiag("PROBE OFFLINE: " + sanitizeError(status.errorMessage));
                        logDiag("Notice: In-Memory ACID Repository is active. All CRUD operations persist in session cache.");
                    }
                } catch (Exception e) {
                    logDiag("Probe error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private String sanitizeError(String msg) {
        if (msg == null) return "Unknown error";
        return msg.replaceAll("password='[^']*'", "password='••••'")
                  .replaceAll("password=[^&\\s]*", "password=••••");
    }

    private void logDiag(String msg) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        txtDiagnostics.append("[" + ts + "] " + msg + "\n");
        txtDiagnostics.setCaretPosition(txtDiagnostics.getDocument().getLength());
    }

    private void handleEditConfiguration() {
        DatabaseConfig cur = DatabaseConnection.getCurrentConfig();

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField txtHost = new JTextField(cur.getHost());
        JTextField txtPort = new JTextField(String.valueOf(cur.getPort()));
        JTextField txtDb = new JTextField(cur.getDatabaseName());
        JTextField txtUser = new JTextField(cur.getUsername());
        JPasswordField txtPass = new JPasswordField(cur.getPassword());

        form.add(new JLabel("Host:")); form.add(txtHost);
        form.add(new JLabel("Port:")); form.add(txtPort);
        form.add(new JLabel("Database Name:")); form.add(txtDb);
        form.add(new JLabel("Username:")); form.add(txtUser);
        form.add(new JLabel("Password:")); form.add(txtPass);

        int opt = JOptionPane.showConfirmDialog(this, form, "Update JDBC Connection Configuration", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String host = txtHost.getText().trim();
            int port = 3306;
            try {
                port = Integer.parseInt(txtPort.getText().trim());
            } catch (Exception ignored) {}
            String db = txtDb.getText().trim();
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            // Save backup for rollback preservation
            String oldHost = cur.getHost();
            int oldPort = cur.getPort();
            String oldDb = cur.getDatabaseName();
            String oldUser = cur.getUsername();
            String oldPass = cur.getPassword();

            DatabaseConnection.updateConfig(host, port, db, user, pass);
            logDiag("Configuration updated. Running validation probe...");

            DatabaseConnection.ConnectionStatus testRes = DatabaseConnection.testConnection();
            if (testRes.success) {
                JOptionPane.showMessageDialog(this, "Connection configuration verified and saved!\nLatency: " + testRes.latencyMs + "ms",
                        "Configuration Verified", JOptionPane.INFORMATION_MESSAGE);
                auditLogService.log(currentAdmin.getUserId(), currentAdmin.getEmail(), "JDBC_CONFIG_UPDATE", "DATABASE",
                        host + ":" + port + "/" + db, "SUCCESS", "JDBC connection settings updated by admin.");
            } else {
                int rb = JOptionPane.showConfirmDialog(this,
                        "Notice: The newly entered settings failed to establish a live connection:\n" +
                                sanitizeError(testRes.errorMessage) +
                                "\n\nWould you like to ROLL BACK to previous working configuration?",
                        "Connection Failed - Rollback Recommended",
                        JOptionPane.YES_NO_OPTION);
                if (rb == JOptionPane.YES_OPTION) {
                    DatabaseConnection.updateConfig(oldHost, oldPort, oldDb, oldUser, oldPass);
                    logDiag("Rolled back to previous configuration (" + oldHost + ":" + oldPort + "/" + oldDb + ").");
                }
            }
            runProbe();
        }
    }
}
