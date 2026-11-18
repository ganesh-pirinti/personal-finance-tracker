package ui.components;

import database.DatabaseConnection;
import util.FontManager;
import util.ThemeColors;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Compact Swing JPanel indicator reflecting live MySQL database connectivity
 * with distinct, truthful diagnostic states, tooltips, and interactive probe on click.
 */
public class StatusIndicator extends JPanel {
    private DatabaseConnection.ConnectionState state = DatabaseConnection.ConnectionState.DISCONNECTED;
    private long latencyMs = -1;
    private String diagnosis = "Not connected to MySQL";
    private String testTimestamp = "";
    private boolean probing = false;

    public StatusIndicator() {
        this(false, -1);
    }

    public StatusIndicator(boolean connected, long latencyMs) {
        this.state = connected ? DatabaseConnection.ConnectionState.CONNECTED : DatabaseConnection.ConnectionState.DISCONNECTED;
        this.latencyMs = latencyMs;
        initComponent();
    }

    private void initComponent() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateTooltip();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                triggerRecheck();
            }
        });
    }

    public void triggerRecheck() {
        if (probing) return;
        probing = true;
        repaint();
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection(true);
            SwingUtilities.invokeLater(() -> {
                probing = false;
                setStatus(status);
            });
        }).start();
    }

    public void setStatus(DatabaseConnection.ConnectionStatus status) {
        if (status == null) {
            this.state = DatabaseConnection.ConnectionState.DISCONNECTED;
            this.latencyMs = -1;
            this.diagnosis = "Unknown status";
            this.testTimestamp = "";
        } else {
            this.state = status.state != null ? status.state : (status.success ? DatabaseConnection.ConnectionState.CONNECTED : DatabaseConnection.ConnectionState.DISCONNECTED);
            this.latencyMs = status.latencyMs;
            this.diagnosis = status.sanitizedDiagnosis != null ? status.sanitizedDiagnosis : "";
            this.testTimestamp = status.testTimestamp != null ? status.testTimestamp : "";
        }
        updateTooltip();
        repaint();
    }

    public void setStatus(boolean connected, long latencyMs) {
        this.state = connected ? DatabaseConnection.ConnectionState.CONNECTED : DatabaseConnection.ConnectionState.DISCONNECTED;
        this.latencyMs = latencyMs;
        this.diagnosis = connected ? "Connected to MySQL successfully" : "MySQL server unreachable";
        updateTooltip();
        repaint();
    }

    public boolean isConnected() {
        return state == DatabaseConnection.ConnectionState.CONNECTED;
    }

    public DatabaseConnection.ConnectionState getState() {
        return state;
    }

    private void updateTooltip() {
        String dbInfo = DatabaseConnection.getHost() + ":" + DatabaseConnection.getPort() + "/" + DatabaseConnection.getDatabaseName();
        String html = "<html><div style='padding:4px; font-family:sans-serif;'>"
                + "<b>MySQL Target:</b> " + dbInfo + "<br>"
                + "<b>Status:</b> " + state + "<br>"
                + (latencyMs >= 0 ? "<b>Latency:</b> " + latencyMs + " ms<br>" : "")
                + "<b>Diagnosis:</b> " + diagnosis + "<br>"
                + (testTimestamp.isEmpty() ? "" : "<b>Last Tested:</b> " + testTimestamp + "<br>")
                + "<span style='color:#6366f1; font-size:9px;'><i>Click to re-probe connection now</i></span>"
                + "</div></html>";
        setToolTipText(html);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(175, 28);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color pillBg;
        Color pillBorder;
        Color dotColor;
        Color textColor;
        String text;

        if (probing) {
            pillBg = new Color(243, 244, 246);
            pillBorder = new Color(209, 213, 219);
            dotColor = new Color(156, 163, 175);
            textColor = new Color(75, 85, 99);
            text = "Testing MySQL...";
        } else {
            switch (state) {
                case CONNECTED:
                    pillBg = ThemeColors.INCOME_LIGHT;
                    pillBorder = new Color(167, 243, 208);
                    dotColor = ThemeColors.INCOME;
                    textColor = new Color(6, 95, 70);
                    text = latencyMs >= 0 ? "MySQL Online (" + latencyMs + "ms)" : "MySQL Online";
                    break;
                case AUTH_FAILED:
                    pillBg = new Color(254, 226, 226);
                    pillBorder = new Color(248, 113, 113);
                    dotColor = new Color(220, 38, 38);
                    textColor = new Color(185, 28, 28);
                    text = "MySQL Auth Failed";
                    break;
                case DATABASE_UNAVAILABLE:
                    pillBg = new Color(254, 243, 199);
                    pillBorder = new Color(252, 211, 77);
                    dotColor = new Color(217, 119, 6);
                    textColor = new Color(146, 64, 14);
                    text = "DB Not Found";
                    break;
                case SCHEMA_MIGRATION_REQUIRED:
                    pillBg = new Color(255, 237, 213);
                    pillBorder = new Color(253, 186, 116);
                    dotColor = new Color(234, 88, 12);
                    textColor = new Color(154, 52, 18);
                    text = "Schema Incomplete";
                    break;
                case DEMONSTRATION_MODE:
                    pillBg = new Color(238, 242, 255);
                    pillBorder = new Color(199, 210, 254);
                    dotColor = new Color(99, 102, 241);
                    textColor = new Color(67, 56, 202);
                    text = "Offline Demo Mode";
                    break;
                case DISCONNECTED:
                default:
                    pillBg = ThemeColors.EXPENSE_LIGHT;
                    pillBorder = new Color(254, 202, 202);
                    dotColor = ThemeColors.EXPENSE;
                    textColor = new Color(153, 27, 27);
                    text = "MySQL Disconnected";
                    break;
            }
        }

        // Pill shape
        g2.setColor(pillBg);
        g2.fillRoundRect(1, 1, w - 2, h - 2, h, h);
        g2.setColor(pillBorder);
        g2.drawRoundRect(1, 1, w - 3, h - 3, h, h);

        // Status Dot
        int dotSize = 8;
        int dotX = 10;
        int dotY = (h - dotSize) / 2;
        g2.setColor(dotColor);
        g2.fillOval(dotX, dotY, dotSize, dotSize);

        // Status Text
        g2.setFont(FontManager.SMALL);
        g2.setColor(textColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, dotX + dotSize + 7, ((h - fm.getHeight()) / 2) + fm.getAscent() - 1);

        g2.dispose();
    }
}
