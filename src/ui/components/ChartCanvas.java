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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swing JPanel rendering dual-bar monthly Income vs. Expense trends using Graphics2D.
 */
public class ChartCanvas extends JPanel {
    private Map<String, Double> incomeTrend = new LinkedHashMap<>();
    private Map<String, Double> expenseTrend = new LinkedHashMap<>();
    private String title = "Income & Expenses Trend";

    public ChartCanvas() {
        setOpaque(false);
        setPreferredSize(new Dimension(380, 240));
    }

    public void setData(Map<String, Double> incomeTrend, Map<String, Double> expenseTrend) {
        this.incomeTrend = incomeTrend != null ? incomeTrend : new LinkedHashMap<>();
        this.expenseTrend = expenseTrend != null ? expenseTrend : new LinkedHashMap<>();
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

        // Header: Title & Legend
        g2.setFont(FontManager.CARD_TITLE);
        g2.setColor(ThemeColors.TEXT_MAIN);
        g2.drawString(title, 20, 28);

        // Legend
        int legendX = w - 190;
        int legendY = 20;
        // Income legend
        g2.setColor(ThemeColors.INCOME);
        g2.fillRoundRect(legendX, legendY - 8, 10, 10, 3, 3);
        g2.setFont(FontManager.SMALL);
        g2.setColor(ThemeColors.TEXT_MUTED);
        g2.drawString("Income", legendX + 14, legendY);

        // Expense legend
        g2.setColor(ThemeColors.EXPENSE);
        g2.fillRoundRect(legendX + 75, legendY - 8, 10, 10, 3, 3);
        g2.setColor(ThemeColors.TEXT_MUTED);
        g2.drawString("Expenses", legendX + 89, legendY);

        // Check if there is data
        List<String> months = new ArrayList<>(incomeTrend.keySet());
        boolean hasData = false;
        double maxVal = 0.0;
        for (String m : months) {
            double inc = incomeTrend.getOrDefault(m, 0.0);
            double exp = expenseTrend.getOrDefault(m, 0.0);
            if (inc > 0 || exp > 0) hasData = true;
            if (inc > maxVal) maxVal = inc;
            if (exp > maxVal) maxVal = exp;
        }

        if (!hasData || months.isEmpty()) {
            drawEmptyState(g2, w, h);
            g2.dispose();
            return;
        }

        // Add 20% headroom to maxVal
        maxVal = Math.max(1000.0, maxVal * 1.2);

        // Chart Area Boundaries
        int chartLeft = 55;
        int chartRight = w - 24;
        int chartTop = 50;
        int chartBottom = h - 35;
        int chartWidth = chartRight - chartLeft;
        int chartHeight = chartBottom - chartTop;

        // Draw horizontal grid lines & Y labels (4 divisions)
        g2.setFont(FontManager.SMALL);
        for (int i = 0; i <= 4; i++) {
            int lineY = chartBottom - (i * chartHeight / 4);
            g2.setColor(ThemeColors.DIVIDER);
            g2.drawLine(chartLeft, lineY, chartRight, lineY);

            double labelVal = (maxVal / 4.0) * i;
            String labelStr = formatK(labelVal);
            g2.setColor(ThemeColors.TEXT_LIGHT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(labelStr, chartLeft - fm.stringWidth(labelStr) - 8, lineY + 4);
        }

        // Draw Bars for each month
        int numMonths = months.size();
        int slotWidth = chartWidth / numMonths;
        int barWidth = Math.max(8, (slotWidth - 28) / 2);

        for (int i = 0; i < numMonths; i++) {
            String mKey = months.get(i);
            double inc = incomeTrend.getOrDefault(mKey, 0.0);
            double exp = expenseTrend.getOrDefault(mKey, 0.0);

            int slotX = chartLeft + (i * slotWidth);
            int centerX = slotX + (slotWidth / 2);

            int incBarH = (int) ((inc / maxVal) * chartHeight);
            int expBarH = (int) ((exp / maxVal) * chartHeight);

            int incBarX = centerX - barWidth - 2;
            int expBarX = centerX + 2;

            // Income bar
            if (incBarH > 0) {
                g2.setColor(ThemeColors.INCOME);
                g2.fillRoundRect(incBarX, chartBottom - incBarH, barWidth, incBarH, 4, 4);
            }

            // Expense bar
            if (expBarH > 0) {
                g2.setColor(ThemeColors.EXPENSE);
                g2.fillRoundRect(expBarX, chartBottom - expBarH, barWidth, expBarH, 4, 4);
            }

            // Month Label on X-axis (e.g. "Jun", "Nov")
            String displayMonth = formatMonthLabel(mKey);
            g2.setColor(ThemeColors.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int monthLabelX = centerX - (fm.stringWidth(displayMonth) / 2);
            g2.drawString(displayMonth, monthLabelX, chartBottom + 18);
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int w, int h) {
        g2.setFont(FontManager.BODY);
        g2.setColor(ThemeColors.TEXT_LIGHT);
        String msg = "No trend data recorded yet. Add transactions to see monthly comparison.";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(msg)) / 2;
        int ty = (h / 2) + 10;
        g2.drawString(msg, tx, ty);
    }

    private String formatK(double val) {
        if (val >= 100000) {
            return String.format("%.0fL", val / 100000);
        } else if (val >= 1000) {
            return String.format("%.0fk", val / 1000);
        } else {
            return String.format("%.0f", val);
        }
    }

    private String formatMonthLabel(String monthKey) {
        if (monthKey == null || monthKey.length() < 7) return monthKey;
        String m = monthKey.substring(5, 7);
        switch (m) {
            case "01": return "Jan";
            case "02": return "Feb";
            case "03": return "Mar";
            case "04": return "Apr";
            case "05": return "May";
            case "06": return "Jun";
            case "07": return "Jul";
            case "08": return "Aug";
            case "09": return "Sep";
            case "10": return "Oct";
            case "11": return "Nov";
            case "12": return "Dec";
            default: return m;
        }
    }
}
