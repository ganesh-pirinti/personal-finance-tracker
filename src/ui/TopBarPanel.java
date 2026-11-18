package ui;

import ui.components.StatusIndicator;
import util.FontManager;
import util.ThemeColors;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Top application toolbar built with Swing JPanel, displaying page title,
 * database connectivity status, and user session details.
 */
public class TopBarPanel extends JPanel {
    private String pageTitle = "Dashboard";
    private final StatusIndicator statusIndicator;
    private final JPanel titlePanel;

    public TopBarPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColors.HEADER_BG);
        setPreferredSize(new Dimension(800, 60));

        // Title Panel on Left
        titlePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(350, 60);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.HEADER_BG);
                g2.fillRect(0, 0, w, h);

                g2.setFont(FontManager.SECTION_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString(pageTitle, 24, 32);

                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString("Personal Finance Management System (Swing & AWT)", 24, 48);

                // Bottom divider
                g2.setColor(ThemeColors.BORDER);
                g2.drawLine(0, h - 1, w, h - 1);

                g2.dispose();
            }
        };
        add(titlePanel, BorderLayout.WEST);

        // Right side: Database status indicator
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 16));
        rightPanel.setBackground(ThemeColors.HEADER_BG);

        statusIndicator = new StatusIndicator();
        rightPanel.add(statusIndicator);

        add(rightPanel, BorderLayout.EAST);
    }

    public void setPageTitle(String title) {
        this.pageTitle = title;
        titlePanel.repaint();
    }

    public void updateDatabaseStatus(boolean connected, long latencyMs) {
        statusIndicator.setStatus(connected, latencyMs);
    }

    public void updateDatabaseStatus(database.DatabaseConnection.ConnectionStatus status) {
        statusIndicator.setStatus(status);
    }

    public StatusIndicator getStatusIndicator() {
        return statusIndicator;
    }
}
