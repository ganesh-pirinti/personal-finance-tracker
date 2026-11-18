package ui.components;

import util.FontManager;
import util.ThemeColors;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern Swing JButton with custom Graphics2D rounded rendering,
 * hover/pressed visual states, and design system color variants.
 */
public class CustomButton extends JButton {
    public enum Style {
        PRIMARY, SECONDARY, SUCCESS, DANGER, OUTLINE;
        public static final Style EXPENSE = DANGER;
    }

    public enum ButtonType {
        PRIMARY, SECONDARY, SUCCESS, DANGER, OUTLINE;
        public static final ButtonType EXPENSE = DANGER;
    }

    private Style buttonStyle = Style.PRIMARY;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 8;
    private Font customFont = FontManager.BUTTON_TEXT;

    public CustomButton(String text) {
        this(text, Style.PRIMARY);
    }

    public CustomButton(String text, Style style) {
        super(text);
        this.buttonStyle = style != null ? style : Style.PRIMARY;
        initButton();
    }

    public CustomButton(String text, ButtonType buttonType) {
        super(text);
        this.buttonStyle = convertType(buttonType);
        initButton();
    }

    private static Style convertType(ButtonType type) {
        if (type == null) return Style.PRIMARY;
        switch (type) {
            case SECONDARY: return Style.SECONDARY;
            case SUCCESS: return Style.SUCCESS;
            case DANGER: return Style.DANGER;
            case OUTLINE: return Style.OUTLINE;
            default: return Style.PRIMARY;
        }
    }

    private void initButton() {
        setFont(customFont);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = false;
                    repaint();
                }
            }
        });
    }

    public void setStyle(Style style) {
        this.buttonStyle = style != null ? style : Style.PRIMARY;
        repaint();
    }

    public void setButtonType(ButtonType type) {
        this.buttonStyle = convertType(type);
        repaint();
    }

    public void setCustomFont(Font font) {
        this.customFont = font;
        setFont(font);
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 24, 80), Math.max(d.height + 10, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg = getBackgroundColor();
        Color fg = getForegroundColor();
        Color border = getBorderColor();

        // Background fill
        if (buttonStyle != Style.OUTLINE || isHovered) {
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
        }

        // Border outline
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        }

        // Text label
        g2.setFont(customFont != null ? customFont : getFont());
        g2.setColor(isEnabled() ? fg : ThemeColors.TEXT_LIGHT);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    private Color getBackgroundColor() {
        if (!isEnabled()) {
            return ThemeColors.SURFACE_HOVER;
        }
        switch (buttonStyle) {
            case SECONDARY:
                return isPressed ? ThemeColors.BORDER : (isHovered ? ThemeColors.SURFACE_HOVER : ThemeColors.SURFACE);
            case SUCCESS:
                return isPressed ? ThemeColors.INCOME_HOVER : (isHovered ? ThemeColors.INCOME_HOVER : ThemeColors.INCOME);
            case DANGER:
                return isPressed ? ThemeColors.EXPENSE_HOVER : (isHovered ? ThemeColors.EXPENSE_HOVER : ThemeColors.EXPENSE);
            case OUTLINE:
                return isHovered ? ThemeColors.PRIMARY_LIGHT : ThemeColors.SURFACE;
            case PRIMARY:
            default:
                return isPressed ? ThemeColors.PRIMARY_HOVER : (isHovered ? ThemeColors.PRIMARY_HOVER : ThemeColors.PRIMARY);
        }
    }

    private Color getForegroundColor() {
        if (!isEnabled()) {
            return ThemeColors.TEXT_LIGHT;
        }
        switch (buttonStyle) {
            case SECONDARY:
                return ThemeColors.TEXT_MAIN;
            case SUCCESS:
            case DANGER:
            case PRIMARY:
                return ThemeColors.TEXT_WHITE;
            case OUTLINE:
                return ThemeColors.PRIMARY;
            default:
                return ThemeColors.TEXT_WHITE;
        }
    }

    private Color getBorderColor() {
        switch (buttonStyle) {
            case SECONDARY:
                return ThemeColors.BORDER;
            case OUTLINE:
                return ThemeColors.PRIMARY;
            default:
                return null;
        }
    }
}
