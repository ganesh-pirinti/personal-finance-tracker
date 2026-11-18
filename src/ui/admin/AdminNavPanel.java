package ui.admin;

import model.User;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Left sidebar navigation panel for the Super Admin Portal.
 * Supports the 16-item administration suite with scrollable navigation list,
 * real-time hover states, active indicators, and administrator identity badge.
 */
public class AdminNavPanel extends JPanel {
    public interface AdminNavListener {
        void onNavSelected(String sectionName);
        void onSwitchToUserApp();
        void onLogout();
    }

    public static final String NAV_OVERVIEW = "Admin Overview";
    public static final String NAV_USERS = "User Management";
    public static final String NAV_AUTH_RECOVERY = "Authentication & Recovery";
    public static final String NAV_SESSIONS = "Session Management";
    public static final String NAV_JDBC_CONTROL = "JDBC Control Center";
    public static final String NAV_JDBC_ARCH = "JDBC Architecture Visualizer";
    public static final String NAV_DATABASE_SCHEMA = "Database Health & Schema";
    public static final String NAV_SECURITY_MONITOR = "Security Monitoring";
    public static final String NAV_UI_CUSTOMIZATION = "UI/UX Customization";
    public static final String NAV_CONTENT_FIELDS = "Content & Field Editor";
    public static final String NAV_ANALYTICS = "Analytics & Reports";
    public static final String NAV_AUDIT = "Audit Logs";
    public static final String NAV_SETTINGS = "Application Settings";
    public static final String NAV_PROFILE = "Admin Profile";
    public static final String NAV_RETURN_USER = "Return to User Dashboard";
    public static final String NAV_LOGOUT = "Logout";

    // Legacy aliases
    public static final String NAV_APPEARANCE = NAV_UI_CUSTOMIZATION;
    public static final String NAV_CONTENT = NAV_CONTENT_FIELDS;
    public static final String NAV_DATABASE = NAV_DATABASE_SCHEMA;
    public static final String NAV_FORM_CONFIG = NAV_CONTENT_FIELDS;
    public static final String NAV_SECURITY = NAV_SECURITY_MONITOR;

    private final User currentAdmin;
    private final AdminNavListener listener;
    private final List<NavItemPanel> navItems = new ArrayList<>();
    private String activeNav = NAV_OVERVIEW;

    public AdminNavPanel(User currentAdmin, AdminNavListener listener) {
        this.currentAdmin = currentAdmin;
        this.listener = listener;

        initUI();
    }

    private void initUI() {
        setPreferredSize(new Dimension(260, 780));
        setBackground(ThemeColors.SIDEBAR_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeColors.CARD_BORDER));

        // 1. BRAND HEADER
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int iconX = 18;
                int iconY = 18;
                int iconSize = 38;

                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(iconX, iconY, iconSize, iconSize, 10, 10);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));
                g2.drawString("★", iconX + 11, iconY + 25);

                g2.setFont(FontManager.SECTION_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString("Super Admin", iconX + 48, iconY + 20);

                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawString("Finance Tracker Suite", iconX + 48, iconY + 34);

                g2.dispose();
            }
        };
        headerPanel.setPreferredSize(new Dimension(260, 72));
        headerPanel.setOpaque(false);
        add(headerPanel, BorderLayout.NORTH);

        // 2. SCROLLABLE NAV LINKS
        JPanel navListPanel = new JPanel(new GridBagLayout());
        navListPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 10, 2, 10);

        addNavItem(navListPanel, gbc, NAV_OVERVIEW, "System overview & stats");
        addNavItem(navListPanel, gbc, NAV_USERS, "Roles, suspensions & accounts");
        addNavItem(navListPanel, gbc, NAV_AUTH_RECOVERY, "Tokens & admin password reset");
        addNavItem(navListPanel, gbc, NAV_SESSIONS, "Session management & termination");
        addNavItem(navListPanel, gbc, NAV_JDBC_CONTROL, "Live JDBC probe & latency");
        addNavItem(navListPanel, gbc, NAV_JDBC_ARCH, "Interactive JDBC pipeline visualizer");
        addNavItem(navListPanel, gbc, NAV_DATABASE_SCHEMA, "ER diagrams & table schemas");
        addNavItem(navListPanel, gbc, NAV_SECURITY_MONITOR, "Security alerts & threat stream");
        addNavItem(navListPanel, gbc, NAV_UI_CUSTOMIZATION, "Branding, themes & appearance");
        addNavItem(navListPanel, gbc, NAV_CONTENT_FIELDS, "Content text & field rules");
        addNavItem(navListPanel, gbc, NAV_ANALYTICS, "Financial analytics & reports");
        addNavItem(navListPanel, gbc, NAV_AUDIT, "Immutable audit trail");
        addNavItem(navListPanel, gbc, NAV_SETTINGS, "SMTP mail & security policies");
        addNavItem(navListPanel, gbc, NAV_PROFILE, "Admin credentials & profile");
        addNavItem(navListPanel, gbc, NAV_RETURN_USER, "Return to User Dashboard");
        addNavItem(navListPanel, gbc, NAV_LOGOUT, "Terminate session and sign out");

        // Spacer at bottom of nav list
        gbc.gridy++;
        gbc.weighty = 1.0;
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        navListPanel.add(spacer, gbc);

        JScrollPane scrollPane = new JScrollPane(navListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // 3. FOOTER IDENTITY BADGE
        JPanel footerPanel = new JPanel(new BorderLayout(0, 6));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 14, 12, 14)
        ));

        JPanel adminPill = new JPanel(new GridLayout(2, 1, 0, 2));
        adminPill.setOpaque(false);

        JLabel lblAdminName = new JLabel((currentAdmin != null ? currentAdmin.getFullName() : "Administrator") + " (ADMIN)");
        lblAdminName.setFont(FontManager.SMALL_BUTTON);
        lblAdminName.setForeground(ThemeColors.PRIMARY);
        adminPill.add(lblAdminName);

        JLabel lblAdminEmail = new JLabel(currentAdmin != null ? currentAdmin.getEmail() : "admin@tracker.local");
        lblAdminEmail.setFont(FontManager.SMALL);
        lblAdminEmail.setForeground(ThemeColors.TEXT_MUTED);
        adminPill.add(lblAdminEmail);

        footerPanel.add(adminPill, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addNavItem(JPanel container, GridBagConstraints gbc, String section, String tooltip) {
        NavItemPanel item = new NavItemPanel(section, tooltip);
        navItems.add(item);
        container.add(item, gbc);
        gbc.gridy++;
    }

    public void setActiveNav(String section) {
        this.activeNav = section;
        for (NavItemPanel item : navItems) {
            item.setActive(item.sectionName.equals(section));
        }
    }

    private class NavItemPanel extends JPanel {
        private final String sectionName;
        private boolean isActive = false;
        private boolean isHovered = false;

        public NavItemPanel(String sectionName, String tooltip) {
            this.sectionName = sectionName;
            this.isActive = sectionName.equals(activeNav);

            setPreferredSize(new Dimension(236, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(tooltip);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (NAV_RETURN_USER.equals(sectionName)) {
                        if (listener != null) listener.onSwitchToUserApp();
                    } else if (NAV_LOGOUT.equals(sectionName)) {
                        if (listener != null) listener.onLogout();
                    } else {
                        if (listener != null) listener.onNavSelected(sectionName);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(0, 2, w, h - 4, 8, 8);
            } else if (isHovered) {
                g2.setColor(ThemeColors.HOVER);
                g2.fillRoundRect(0, 2, w, h - 4, 8, 8);
            }

            // Left vector icon
            Color iconColor = isActive ? Color.WHITE : ThemeColors.PRIMARY;
            drawAdminNavIcon(g2, sectionName, 12, (h - 14) / 2, 14, iconColor);

            // Title
            g2.setFont(isActive ? FontManager.BODY_BOLD : FontManager.BODY);
            g2.setColor(isActive ? Color.WHITE : ThemeColors.TEXT_MAIN);
            g2.drawString(sectionName, 36, h / 2 + 5);

            g2.dispose();
        }

        private void drawAdminNavIcon(Graphics2D g2, String section, int x, int y, int size, Color color) {
            g2.setColor(color);
            Stroke origStroke = g2.getStroke();
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (section) {
                case NAV_OVERVIEW: { // 4-quadrant layout
                    int s = (size - 2) / 2;
                    g2.drawRoundRect(x, y, s, s, 2, 2);
                    g2.drawRoundRect(x + s + 2, y, s, s, 2, 2);
                    g2.drawRoundRect(x, y + s + 2, s, s, 2, 2);
                    g2.drawRoundRect(x + s + 2, y + s + 2, s, s, 2, 2);
                    break;
                }
                case NAV_USERS: { // 2 users silhouette
                    g2.drawOval(x + 2, y + 1, 6, 6);
                    g2.drawArc(x, y + 8, 10, 8, 0, 180);
                    g2.drawOval(x + 8, y + 2, 5, 5);
                    g2.drawArc(x + 6, y + 8, 8, 7, 0, 180);
                    break;
                }
                case NAV_AUTH_RECOVERY: { // Key
                    g2.drawOval(x + 1, y + 2, 7, 7);
                    g2.drawLine(x + 8, y + 5, x + size, y + 5);
                    g2.drawLine(x + size - 2, y + 5, x + size - 2, y + 8);
                    g2.drawLine(x + size - 5, y + 5, x + size - 5, y + 8);
                    break;
                }
                case NAV_SESSIONS: { // Clock
                    g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                    int cx = x + size / 2;
                    int cy = y + size / 2;
                    g2.drawLine(cx, cy, cx, y + 3);
                    g2.drawLine(cx, cy, cx + 3, cy);
                    break;
                }
                case NAV_JDBC_CONTROL: { // Lightning bolt
                    int[] px = { x + size/2 + 1, x + size/2 - 3, x + size/2, x + size/2 - 2, x + size/2 + 4, x + size/2, x + size/2 + 2 };
                    int[] py = { y, y + size/2, y + size/2, y + size, y + size/2 - 1, y + size/2 - 1, y };
                    g2.drawPolyline(px, py, px.length);
                    break;
                }
                case NAV_JDBC_ARCH: { // Pipeline / flow nodes
                    g2.drawOval(x + 1, y + size / 2 - 2, 4, 4);
                    g2.drawLine(x + 5, y + size / 2, x + 9, y + size / 2);
                    g2.drawOval(x + 9, y + size / 2 - 2, 4, 4);
                    break;
                }
                case NAV_DATABASE_SCHEMA: { // Database cylinders
                    g2.drawOval(x + 1, y + 1, size - 2, 4);
                    g2.drawLine(x + 1, y + 3, x + 1, y + size - 3);
                    g2.drawLine(x + size - 1, y + 3, x + size - 1, y + size - 3);
                    g2.drawArc(x + 1, y + size - 5, size - 2, 4, 180, 180);
                    g2.drawArc(x + 1, y + size/2 - 2, size - 2, 4, 180, 180);
                    break;
                }
                case NAV_SECURITY_MONITOR: { // Shield
                    int[] sx = { x + 1, x + size - 1, x + size - 1, x + size/2, x + 1 };
                    int[] sy = { y + 2, y + 2, y + 7, y + size, y + 7 };
                    g2.drawPolygon(sx, sy, 5);
                    break;
                }
                case NAV_UI_CUSTOMIZATION: { // Palette
                    g2.drawOval(x + 1, y + 1, size - 2, size - 2);
                    g2.fillOval(x + 4, y + 4, 2, 2);
                    g2.fillOval(x + 8, y + 4, 2, 2);
                    g2.fillOval(x + 4, y + 8, 2, 2);
                    break;
                }
                case NAV_CONTENT_FIELDS: { // Edit document
                    g2.drawRect(x + 1, y + 1, size - 4, size - 2);
                    g2.drawLine(x + 3, y + 4, x + size - 5, y + 4);
                    g2.drawLine(x + 3, y + 7, x + size - 5, y + 7);
                    g2.drawLine(x + 3, y + 10, x + size - 7, y + 10);
                    break;
                }
                case NAV_ANALYTICS: { // Trending line with dots
                    g2.drawLine(x + 1, y + size - 2, x + size, y + size - 2);
                    g2.drawLine(x + 2, y + 9, x + 6, y + 5);
                    g2.drawLine(x + 6, y + 5, x + 11, y + 2);
                    break;
                }
                case NAV_AUDIT: { // List
                    g2.drawRect(x + 1, y + 1, size - 2, size - 2);
                    g2.drawLine(x + 4, y + 4, x + size - 4, y + 4);
                    g2.drawLine(x + 4, y + 7, x + size - 4, y + 7);
                    g2.drawLine(x + 4, y + 10, x + size - 4, y + 10);
                    break;
                }
                case NAV_SETTINGS: { // Gear
                    g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                    int cx = x + size / 2;
                    int cy = y + size / 2;
                    g2.drawLine(cx, y, cx, y + 2);
                    g2.drawLine(cx, y + size - 2, cx, y + size);
                    g2.drawLine(x, cy, x + 2, cy);
                    g2.drawLine(x + size - 2, cy, x + size, cy);
                    break;
                }
                case NAV_PROFILE: { // Single user
                    g2.drawOval(x + 3, y + 1, 7, 7);
                    g2.drawArc(x + 1, y + 8, 12, 8, 0, 180);
                    break;
                }
                case NAV_RETURN_USER: { // Left arrow
                    int cy = y + size / 2;
                    g2.drawLine(x + 2, cy, x + size - 2, cy);
                    g2.drawLine(x + 2, cy, x + 6, cy - 4);
                    g2.drawLine(x + 2, cy, x + 6, cy + 4);
                    break;
                }
                case NAV_LOGOUT: { // Doorway and arrow
                    g2.drawRect(x + 1, y + 1, 7, size - 2);
                    int cy = y + size / 2;
                    g2.drawLine(x + 6, cy, x + size, cy);
                    g2.drawLine(x + size, cy, x + size - 3, cy - 3);
                    g2.drawLine(x + size, cy, x + size - 3, cy + 3);
                    break;
                }
                default: {
                    g2.fillOval(x + size/2 - 2, y + size/2 - 2, 4, 4);
                    break;
                }
            }
            g2.setStroke(origStroke);
        }
    }
}
