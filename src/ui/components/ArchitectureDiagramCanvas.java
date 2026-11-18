package ui.components;

import util.FontManager;
import util.ThemeColors;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Swing JPanel visualizer illustrating the 4-tier application architecture
 * and 5-stage JDBC database connectivity flow using Graphics2D.
 */
public class ArchitectureDiagramCanvas extends JPanel {
    public enum Mode {
        SYSTEM_ARCHITECTURE, JDBC_DATA_FLOW
    }

    private Mode mode = Mode.SYSTEM_ARCHITECTURE;

    public ArchitectureDiagramCanvas(Mode mode) {
        this.mode = mode;
        setOpaque(false);
        setPreferredSize(new Dimension(680, 280));
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(500, 240);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(ThemeColors.SURFACE);
        g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
        g2.setColor(ThemeColors.BORDER);
        g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);

        if (mode == Mode.SYSTEM_ARCHITECTURE) {
            drawSystemArchitecture(g2, w, h);
        } else {
            drawJdbcDataFlow(g2, w, h);
        }

        g2.dispose();
    }

    private void drawSystemArchitecture(Graphics2D g2, int w, int h) {
        int margin = 20;
        int boxW = (w - (margin * 2) - 60) / 4;
        int boxH = h - 60;
        int startY = 30;

        String[] titles = {
            "Presentation Layer",
            "Business Logic",
            "Data Access Layer",
            "Database Layer"
        };

        String[][] details = {
            {"Java Swing & AWT", "JFrame & CardLayout", "Graphics2D Charts", "JTable & ActionListeners"},
            {"AuthService (PBKDF2)", "TransactionService", "BudgetService", "Input Validation"},
            {"UserDAO", "TransactionDAO", "BudgetDAO", "PreparedStatement"},
            {"MySQL 8.0+ Engine", "users Table", "transactions Table", "budgets Table"}
        };

        Color[] colors = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(139, 92, 246)
        };

        for (int i = 0; i < 4; i++) {
            int x = margin + i * (boxW + 20);

            // Card
            g2.setColor(new Color(248, 250, 252));
            g2.fillRoundRect(x, startY, boxW, boxH, 8, 8);
            g2.setColor(colors[i]);
            g2.drawRoundRect(x, startY, boxW, boxH, 8, 8);

            // Header stripe
            g2.setColor(colors[i]);
            g2.fillRoundRect(x, startY, boxW, 26, 8, 8);
            g2.fillRect(x, startY + 16, boxW, 10);

            // Header Title
            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (boxW - fm.stringWidth(titles[i])) / 2;
            g2.drawString(titles[i], tx, startY + 17);

            // Bullets
            g2.setFont(FontManager.SMALL);
            g2.setColor(ThemeColors.TEXT_MAIN);
            for (int j = 0; j < details[i].length; j++) {
                g2.drawString("• " + details[i][j], x + 8, startY + 52 + (j * 20));
            }

            // Arrow to next box
            if (i < 3) {
                int arrowX = x + boxW + 4;
                int arrowY = startY + (boxH / 2);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawLine(arrowX, arrowY, arrowX + 10, arrowY);
                int[] px = {arrowX + 10, arrowX + 6, arrowX + 6};
                int[] py = {arrowY, arrowY - 4, arrowY + 4};
                g2.fillPolygon(px, py, 3);
            }
        }

        // Subtitle bottom
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MUTED);
        g2.drawString("Four-tier architectural separation combining Java Swing UI with AWT Graphics & JDBC persistence.", 20, h - 10);
    }

    private void drawJdbcDataFlow(Graphics2D g2, int w, int h) {
        int margin = 20;
        int boxW = (w - (margin * 2) - 80) / 5;
        int boxH = 90;
        int startY = 40;

        String[] stages = {
            "Java Swing UI",
            "Service Logic",
            "DAO Repository",
            "JDBC Driver",
            "MySQL Server"
        };

        String[] subText = {
            "JFrame / JTable",
            "Validation & Flow",
            "PreparedStatement",
            "mysql-connector-j",
            "InnoDB Engine"
        };

        for (int i = 0; i < 5; i++) {
            int x = margin + i * (boxW + 20);

            // Box
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(x, startY, boxW, boxH, 8, 8);
            g2.setColor(ThemeColors.PRIMARY);
            g2.drawRoundRect(x, startY, boxW, boxH, 8, 8);

            // Title
            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(ThemeColors.PRIMARY);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (boxW - fm.stringWidth(stages[i])) / 2;
            g2.drawString(stages[i], tx, startY + 32);

            // Subtext
            g2.setFont(FontManager.SMALL);
            g2.setColor(ThemeColors.TEXT_MUTED);
            FontMetrics fm2 = g2.getFontMetrics();
            int sx = x + (boxW - fm2.stringWidth(subText[i])) / 2;
            g2.drawString(subText[i], sx, startY + 54);

            // Connector arrow
            if (i < 4) {
                int arrowX = x + boxW + 4;
                int arrowY = startY + (boxH / 2);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawLine(arrowX, arrowY, arrowX + 10, arrowY);
                int[] px = {arrowX + 10, arrowX + 6, arrowX + 6};
                int[] py = {arrowY, arrowY - 4, arrowY + 4};
                g2.fillPolygon(px, py, 3);
            }
        }

        // Bottom Flow explanation
        int bottomY = startY + boxH + 35;
        g2.setFont(FontManager.BODY_BOLD);
        g2.setColor(ThemeColors.INCOME);
        g2.drawString("Write Operation Flow:", margin, bottomY);
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MAIN);
        g2.drawString("Swing Form Action → Validate Input → Parameterized SQL → executeUpdate() → Commit → Refresh Swing Models", margin + 145, bottomY);

        bottomY += 28;
        g2.setFont(FontManager.BODY_BOLD);
        g2.setColor(ThemeColors.PRIMARY);
        g2.drawString("Read Operation Flow:", margin, bottomY);
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MAIN);
        g2.drawString("Dashboard Load → Call Service → executeQuery() → Scroll ResultSet → Map to POJOs → Repaint Swing JTable & Charts", margin + 145, bottomY);
    }
}
