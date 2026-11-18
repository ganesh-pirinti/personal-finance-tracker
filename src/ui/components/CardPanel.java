package ui.components;

import util.ThemeColors;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Reusable Swing JPanel container that renders a modern rounded white card
 * with subtle borders and custom padding.
 */
public class CardPanel extends JPanel {
    private int cornerRadius = 12;
    private Color cardColor = ThemeColors.SURFACE;
    private Color borderColor = ThemeColors.BORDER;
    private Insets padding = new Insets(16, 16, 16, 16);

    public CardPanel() {
        super();
        setOpaque(false);
    }

    public CardPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public CardPanel(LayoutManager layout, Insets padding) {
        super(layout);
        this.padding = padding;
        setOpaque(false);
    }

    public void setCardColor(Color color) {
        this.cardColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setPadding(Insets insets) {
        this.padding = insets;
        revalidate();
        repaint();
    }

    @Override
    public Insets getInsets() {
        return padding;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Fill card background
        g2.setColor(cardColor);
        g2.fillRoundRect(2, 2, w - 4, h - 4, cornerRadius, cornerRadius);

        // Draw card border
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(2, 2, w - 5, h - 5, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
