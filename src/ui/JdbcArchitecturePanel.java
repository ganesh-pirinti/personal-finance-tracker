package ui;

import database.DatabaseConnection;
import model.DatabaseConfig;
import ui.components.ArchitectureDiagramCanvas;
import ui.components.CustomButton;
import ui.components.StatusIndicator;
import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Screen 9: Live JDBC Connection Demonstration & Interactive Architecture Visualizer
 * built with Swing & AWT.
 */
public class JdbcArchitecturePanel extends JPanel {
    private final StatusIndicator statusIndicator;
    private final JPanel infoPanel;
    private final JPanel troubleshootPanel;
    private final ArchitectureDiagramCanvas diagramCanvas;

    private DatabaseConnection.ConnectionStatus currentStatus = new DatabaseConnection.ConnectionStatus();

    public JdbcArchitecturePanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(ThemeColors.BG_MAIN);

        // Top Status Header Banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(ThemeColors.SURFACE);
        topBanner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JPanel titlePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(380, 44); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(FontManager.SECTION_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString("JDBC Connection Architecture", 10, 28);
                g2.dispose();
            }
        };
        titlePanel.setOpaque(false);
        topBanner.add(titlePanel, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 6));
        rightActions.setBackground(ThemeColors.SURFACE);

        statusIndicator = new StatusIndicator();
        CustomButton btnTestConn = new CustomButton("⚡ Test Connection Now", CustomButton.Style.PRIMARY);
        btnTestConn.setCustomFont(FontManager.SMALL_BUTTON);
        btnTestConn.setPreferredSize(new Dimension(170, 30));
        btnTestConn.addActionListener(e -> runConnectionTest());

        rightActions.add(statusIndicator);
        rightActions.add(btnTestConn);
        topBanner.add(rightActions, BorderLayout.EAST);

        content.add(topBanner, BorderLayout.NORTH);

        // Center Content: Info Grid + Diagram + Troubleshooting
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setBackground(ThemeColors.BG_MAIN);

        // 1. Connection Parameters Grid Card
        infoPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(700, 130); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SURFACE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);

                DatabaseConfig cfg = DatabaseConnection.getCurrentConfig();

                drawItem(g2, 20, 24, "JDBC Driver Class", "com.mysql.cj.jdbc.Driver");
                drawItem(g2, 250, 24, "Database Engine", currentStatus.databaseProductName + " " + currentStatus.databaseProductVersion);
                drawItem(g2, 500, 24, "Connection State", currentStatus.success ? "ACTIVE / ESTABLISHED" : "DISCONNECTED / UNREACHABLE");

                drawItem(g2, 20, 68, "Host & Port", cfg.getHost() + ":" + cfg.getPort());
                drawItem(g2, 250, 68, "Target Database Schema", cfg.getDatabaseName());
                drawItem(g2, 500, 68, "Round-trip Latency", currentStatus.success ? (currentStatus.latencyMs + " ms") : "N/A");

                // Note
                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_LIGHT);
                g2.drawString("Authentication: Credentials loaded from environment or resources/db.properties (Passwords hidden for security).", 20, 114);

                g2.dispose();
            }

            private void drawItem(Graphics2D g2, int x, int y, String label, String val) {
                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString(label, x, y);

                g2.setFont(FontManager.BODY_BOLD);
                g2.setColor(ThemeColors.TEXT_MAIN);
                if (val.length() > 28) val = val.substring(0, 27) + "…";
                g2.drawString(val, x, y + 18);
            }
        };
        infoPanel.setOpaque(false);
        centerPanel.add(infoPanel, BorderLayout.NORTH);

        // 2. Interactive JDBC Flow Diagram Panel
        JPanel diagramWrapper = new JPanel(new BorderLayout(0, 8));
        diagramWrapper.setBackground(ThemeColors.BG_MAIN);

        diagramCanvas = new ArchitectureDiagramCanvas(ArchitectureDiagramCanvas.Mode.JDBC_DATA_FLOW);
        diagramWrapper.add(diagramCanvas, BorderLayout.CENTER);

        // 3. Troubleshooting & Setup Instructions Card
        troubleshootPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(700, 190); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SURFACE);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);

                g2.setFont(FontManager.CARD_TITLE);
                g2.setColor(currentStatus.success ? ThemeColors.INCOME : ThemeColors.EXPENSE);
                g2.drawString(currentStatus.success ?
                        "✓ JDBC Status: Connection Test Succeeded" :
                        "⚠️ JDBC Diagnostic Status: Local MySQL Server Disconnected", 20, 28);

                g2.setFont(FontManager.BODY);
                g2.setColor(ThemeColors.TEXT_MAIN);

                if (currentStatus.success) {
                    g2.drawString("The application has verified live communication with the MySQL database engine on port 3306.", 20, 54);
                    g2.drawString("Parameterized PreparedStatements are active and ready to process real-time transaction records.", 20, 74);
                } else {
                    String err = currentStatus.errorMessage != null ? currentStatus.errorMessage : "Connection refused by host:3306";
                    g2.setColor(ThemeColors.EXPENSE);
                    g2.setFont(FontManager.SMALL);
                    g2.drawString("Error Details: " + err, 20, 52);

                    g2.setFont(FontManager.BODY_BOLD);
                    g2.setColor(ThemeColors.TEXT_MAIN);
                    g2.drawString("Quick MySQL Setup Guide for Local Machine:", 20, 80);

                    g2.setFont(FontManager.SMALL);
                    g2.setColor(ThemeColors.TEXT_MUTED);
                    String[] steps = {
                        "1. Ensure MySQL Server is started: Run 'net start MySQL' (or launch via XAMPP / MySQL Workbench).",
                        "2. Create the database: Execute 'mysql -u root -p < resources/schema.sql' or import in phpMyAdmin.",
                        "3. Update credentials in 'resources/db.properties' (or via the Settings tab) with your MySQL root password.",
                        "4. Click '⚡ Test Connection Now' above to verify live connectivity."
                    };
                    for (int i = 0; i < steps.length; i++) {
                        g2.drawString(steps[i], 24, 102 + (i * 20));
                    }
                }

                g2.dispose();
            }
        };
        troubleshootPanel.setOpaque(false);
        diagramWrapper.add(troubleshootPanel, BorderLayout.SOUTH);

        centerPanel.add(diagramWrapper, BorderLayout.CENTER);
        content.add(centerPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Run initial test
        runConnectionTest();
    }

    public void runConnectionTest() {
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
            SwingUtilities.invokeLater(() -> {
                currentStatus = status;
                statusIndicator.setStatus(currentStatus.success, currentStatus.latencyMs);
                infoPanel.repaint();
                troubleshootPanel.repaint();
            });
        }).start();
    }
}
