package ui.components;

import util.CurrencyFormatter;
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
 * Budget spending progress bar built with Swing JPanel and Graphics2D,
 * featuring dynamic threshold alerts and percentage tracking.
 */
public class ProgressBarComponent extends JPanel {
    private String category;
    private double spent;
    private double limit;

    public ProgressBarComponent(String category, double spent, double limit) {
        this.category = category;
        this.spent = spent;
        this.limit = limit;
        setOpaque(false);
        setPreferredSize(new Dimension(300, 68));
    }

    public void updateValues(double spent, double limit) {
        this.spent = spent;
        this.limit = limit;
        repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(220, 60);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        double pct = (limit > 0) ? (spent / limit) * 100.0 : 0.0;
        double remaining = Math.max(0.0, limit - spent);

        // Header Row: Category name & spent/limit
        g2.setFont(FontManager.BODY_BOLD);
        g2.setColor(ThemeColors.TEXT_MAIN);
        g2.drawString(category, 0, 16);

        // Right-aligned spent / limit text
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MUTED);
        String amountInfo = CurrencyFormatter.format(spent) + " / " + CurrencyFormatter.format(limit);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(amountInfo, w - fm.stringWidth(amountInfo), 16);

        // Bar Track
        int trackY = 26;
        int trackHeight = 10;
        g2.setColor(new Color(226, 232, 240));
        g2.fillRoundRect(0, trackY, w, trackHeight, trackHeight, trackHeight);

        // Bar Fill Color
        Color fillColor;
        if (pct >= 100.0) {
            fillColor = ThemeColors.EXPENSE;
        } else if (pct >= 80.0) {
            fillColor = ThemeColors.WARNING;
        } else {
            fillColor = ThemeColors.INCOME;
        }

        // Fill Progress
        int fillWidth = (int) Math.min(w, Math.round((Math.min(100.0, pct) / 100.0) * w));
        if (fillWidth > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, trackY, fillWidth, trackHeight, trackHeight, trackHeight);
        }

        // Bottom Row: Status / Warning text & Percentage
        int footerY = 52;
        g2.setFont(FontManager.SMALL);
        if (pct >= 100.0) {
            g2.setColor(ThemeColors.EXPENSE);
            g2.drawString("⚠️ Limit Exceeded!", 0, footerY);
        } else if (pct >= 80.0) {
            g2.setColor(ThemeColors.WARNING);
            g2.drawString("⚠️ Approaching limit (" + CurrencyFormatter.format(remaining) + " remaining)", 0, footerY);
        } else {
            g2.setColor(ThemeColors.TEXT_LIGHT);
            g2.drawString(CurrencyFormatter.format(remaining) + " remaining", 0, footerY);
        }

        // Percentage text right-aligned
        String pctStr = String.format("%.0f%%", pct);
        g2.setColor(fillColor);
        g2.drawString(pctStr, w - fm.stringWidth(pctStr), footerY);

        g2.dispose();
    }
}
