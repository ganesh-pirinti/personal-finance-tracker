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
 * Modern financial & telemetry metric card built with Swing JPanel and Graphics2D.
 * Displays title, prominent value, subtitle, and custom vector icon.
 */
public class MetricCard extends JPanel {
    public enum Type {
        INCOME, EXPENSE, BALANCE, NEUTRAL, WARNING
    }

    public static final class CardType {
        public static final Type INCOME = Type.INCOME;
        public static final Type EXPENSE = Type.EXPENSE;
        public static final Type BALANCE = Type.BALANCE;
        public static final Type NEUTRAL = Type.NEUTRAL;
        public static final Type WARNING = Type.WARNING;
    }

    private String title;
    private double amount;
    private String displayValue = null;
    private String subtitle;
    private Type type;

    public MetricCard(String title, double amount, String subtitle, Type type) {
        this.title = title;
        this.amount = amount;
        this.displayValue = null;
        this.subtitle = subtitle;
        this.type = type != null ? type : Type.NEUTRAL;
        setOpaque(false);
        setPreferredSize(new Dimension(220, 105));
    }

    public MetricCard(String title, String displayValue, String subtitle, Type type) {
        this.title = title;
        this.amount = 0;
        this.displayValue = displayValue;
        this.subtitle = subtitle;
        this.type = type != null ? type : Type.NEUTRAL;
        setOpaque(false);
        setPreferredSize(new Dimension(220, 105));
    }

    public MetricCard(String title, double amount, Type type, String subtitle) {
        this(title, amount, subtitle, type);
    }

    public MetricCard(String title, int amount, Type type, String subtitle) {
        this(title, (double) amount, subtitle, type);
    }

    public MetricCard(String title, String displayValue, Type type, String subtitle) {
        this(title, displayValue, subtitle, type);
    }

    public void setValue(double amount) {
        updateData(amount, this.subtitle);
    }

    public void setValue(int amount) {
        updateData((double) amount, this.subtitle);
    }

    public void setValue(String displayValue) {
        updateData(displayValue, this.subtitle);
    }

    public void updateData(double amount, String subtitle) {
        this.amount = amount;
        this.displayValue = null;
        if (subtitle != null) this.subtitle = subtitle;
        repaint();
    }

    public void updateData(String displayValue, String subtitle) {
        this.displayValue = displayValue;
        if (subtitle != null) this.subtitle = subtitle;
        repaint();
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(160, 95);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Surface Card Background
        g2.setColor(ThemeColors.SURFACE);
        g2.fillRoundRect(2, 2, w - 4, h - 4, 16, 16);

        // 2. Subtle Card Border
        g2.setColor(ThemeColors.BORDER);
        g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);

        // 3. Card Title
        g2.setFont(FontManager.CARD_TITLE);
        g2.setColor(ThemeColors.TEXT_MUTED);
        g2.drawString(title, 20, 30);

        // 4. Formatted Value Amount with dynamic scaling to prevent clipping
        String valueStr = (displayValue != null) ? displayValue : CurrencyFormatter.format(amount);
        int maxTextWidth = Math.max(50, w - 74);
        java.awt.Font font = FontManager.METRIC_AMOUNT;
        FontMetrics fm = g2.getFontMetrics(font);
        if (fm.stringWidth(valueStr) > maxTextWidth) {
            float size = font.getSize2D();
            while (size > 14f && fm.stringWidth(valueStr) > maxTextWidth) {
                size -= 1.5f;
                font = font.deriveFont(size);
                fm = g2.getFontMetrics(font);
            }
        }
        g2.setFont(font);
        g2.setColor(getValueColor());
        g2.drawString(valueStr, 20, 64);

        // 5. Subtitle Context / Status
        g2.setFont(FontManager.SUB_METRIC);
        g2.setColor(ThemeColors.TEXT_LIGHT);
        if (subtitle != null) {
            g2.drawString(subtitle, 20, 88);
        }

        // 6. Vector Indicator Icon (Top Right)
        drawVectorIcon(g2, w - 48, 16);

        g2.dispose();
    }

    private Color getValueColor() {
        switch (type) {
            case INCOME: return ThemeColors.INCOME;
            case EXPENSE: return ThemeColors.EXPENSE;
            case BALANCE: return ThemeColors.PRIMARY;
            default: return ThemeColors.TEXT_MAIN;
        }
    }

    private void drawVectorIcon(Graphics2D g2, int x, int y) {
        int size = 32;
        Color badgeBg;
        Color iconColor;
        String symbol;

        switch (type) {
            case INCOME:
                badgeBg = ThemeColors.INCOME_LIGHT;
                iconColor = ThemeColors.INCOME;
                symbol = "↑";
                break;
            case EXPENSE:
                badgeBg = ThemeColors.EXPENSE_LIGHT;
                iconColor = ThemeColors.EXPENSE;
                symbol = "↓";
                break;
            case BALANCE:
                badgeBg = ThemeColors.PRIMARY_LIGHT;
                iconColor = ThemeColors.PRIMARY;
                symbol = "₹";
                break;
            default:
                badgeBg = ThemeColors.SURFACE_HOVER;
                iconColor = ThemeColors.TEXT_MUTED;
                symbol = "●";
                break;
        }

        // Circular background
        g2.setColor(badgeBg);
        g2.fillOval(x, y, size, size);

        // Centered vector glyph
        g2.setColor(iconColor);
        g2.setFont(FontManager.BUTTON_TEXT);
        FontMetrics fm = g2.getFontMetrics();
        int symX = x + (size - fm.stringWidth(symbol)) / 2;
        int symY = y + (size - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(symbol, symX, symY);
    }
}
