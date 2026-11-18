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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Swing JPanel rendering an Expense Distribution Donut Chart with Category Legend using Graphics2D.
 */
public class DonutChartCanvas extends JPanel {
    private Map<String, Double> categoryExpenses = new LinkedHashMap<>();
    private String title = "Expenses by Category";

    public DonutChartCanvas() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 240));
    }

    public void setData(Map<String, Double> categoryExpenses) {
        this.categoryExpenses = categoryExpenses != null ? categoryExpenses : new LinkedHashMap<>();
        repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(220, 190);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Draw Card Surface
        g2.setColor(ThemeColors.SURFACE);
        g2.fillRoundRect(2, 2, w - 4, h - 4, 14, 14);
        g2.setColor(ThemeColors.BORDER);
        g2.drawRoundRect(2, 2, w - 5, h - 5, 14, 14);

        // Header Title
        g2.setFont(FontManager.CARD_TITLE);
        g2.setColor(ThemeColors.TEXT_MAIN);
        g2.drawString(title, 20, 28);

        // Calculate total
        double total = 0.0;
        for (Double val : categoryExpenses.values()) {
            if (val != null && val > 0) total += val;
        }

        if (total <= 0 || categoryExpenses.isEmpty()) {
            drawEmptyState(g2, w, h);
            g2.dispose();
            return;
        }

        // Donut Geometry
        int donutSize = Math.min(160, h - 70);
        int donutX = 24;
        int donutY = 50 + (h - 70 - donutSize) / 2;

        // Draw Arcs
        double startAngle = 90.0;
        int colorIdx = 0;
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            double val = entry.getValue() != null ? entry.getValue() : 0.0;
            if (val <= 0) continue;

            double arcAngle = (val / total) * 360.0;
            Color arcColor = ThemeColors.CHART_COLORS[colorIdx % ThemeColors.CHART_COLORS.length];
            g2.setColor(arcColor);
            g2.fillArc(donutX, donutY, donutSize, donutSize, (int) Math.round(startAngle), (int) Math.round(-arcAngle));

            startAngle -= arcAngle;
            colorIdx++;
        }

        // Draw Donut Center Cutout (White circle)
        int innerSize = (int) (donutSize * 0.58);
        int innerX = donutX + (donutSize - innerSize) / 2;
        int innerY = donutY + (donutSize - innerSize) / 2;
        g2.setColor(ThemeColors.SURFACE);
        g2.fillOval(innerX, innerY, innerSize, innerSize);

        // Center Text: "Total" & Amount
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MUTED);
        FontMetrics fmSmall = g2.getFontMetrics();
        String totalLabel = "Total Expense";
        g2.drawString(totalLabel, innerX + (innerSize - fmSmall.stringWidth(totalLabel)) / 2, innerY + (innerSize / 2) - 4);

        g2.setFont(FontManager.CARD_TITLE);
        g2.setColor(ThemeColors.TEXT_MAIN);
        FontMetrics fmTitle = g2.getFontMetrics();
        String totalAmountStr = CurrencyFormatter.format(total);
        g2.drawString(totalAmountStr, innerX + (innerSize - fmTitle.stringWidth(totalAmountStr)) / 2, innerY + (innerSize / 2) + 14);

        // Right-Side Legend
        int legendX = donutX + donutSize + 24;
        int legendY = 60;
        int maxLegendItems = 6;
        int itemIdx = 0;

        g2.setFont(FontManager.TABLE_CELL);

        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            if (itemIdx >= maxLegendItems) break;
            double val = entry.getValue() != null ? entry.getValue() : 0.0;
            if (val <= 0) continue;

            Color swatch = ThemeColors.CHART_COLORS[itemIdx % ThemeColors.CHART_COLORS.length];
            int currentY = legendY + (itemIdx * 24);

            // Color Swatch
            g2.setColor(swatch);
            g2.fillRoundRect(legendX, currentY - 9, 10, 10, 3, 3);

            // Category & Percentage
            g2.setColor(ThemeColors.TEXT_MAIN);
            String catName = entry.getKey();
            if (catName.length() > 10) catName = catName.substring(0, 9) + "…";
            double pct = (val / total) * 100.0;
            String label = String.format("%s (%.0f%%)", catName, pct);
            g2.drawString(label, legendX + 16, currentY);

            itemIdx++;
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int w, int h) {
        g2.setFont(FontManager.BODY);
        g2.setColor(ThemeColors.TEXT_LIGHT);
        String msg = "No expenses recorded for this period.";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(msg)) / 2;
        int ty = (h / 2) + 10;
        g2.drawString(msg, tx, ty);
    }
}
