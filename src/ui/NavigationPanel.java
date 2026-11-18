package ui;

import model.User;
import service.AuthenticationService;
import ui.components.ConfirmationDialog;
import ui.components.CustomButton;
import util.FontManager;
import util.ThemeColors;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern left-hand navigation sidebar built with Swing JPanel,
 * custom active indicators, user session badge, and admin portal shortcut.
 */
public class NavigationPanel extends JPanel {
    public interface NavigationListener {
        void onNavigate(String target);
        void onLogout();
    }

    private final NavigationListener listener;
    private final Frame parentFrame;
    private String activeItem = "Dashboard";
    private final List<NavItemPanel> navButtons = new ArrayList<>();

    public static final String NAV_DASHBOARD = "Dashboard";
    public static final String NAV_TRANSACTIONS = "Transactions";
    public static final String NAV_ADD_INCOME = "Add Income";
    public static final String NAV_ADD_EXPENSE = "Add Expense";
    public static final String NAV_BUDGETS = "Budgets";
    public static final String NAV_REPORTS = "Reports";
    public static final String NAV_PROJECT_OVERVIEW = "Project Overview";
    public static final String NAV_JDBC_ARCHITECTURE = "JDBC Architecture";
    public static final String NAV_DATABASE_TABLES = "Database Tables";
    public static final String NAV_SETTINGS = "Settings";
    public static final String NAV_ADMIN_PORTAL = "★ Super Admin Portal";

    private static final String[] MENU_ITEMS = {
        NAV_DASHBOARD,
        NAV_TRANSACTIONS,
        NAV_ADD_INCOME,
        NAV_ADD_EXPENSE,
        NAV_BUDGETS,
        NAV_REPORTS,
        NAV_PROJECT_OVERVIEW,
        NAV_JDBC_ARCHITECTURE,
        NAV_DATABASE_TABLES,
        NAV_SETTINGS
    };

    public NavigationPanel(Frame parentFrame, NavigationListener listener) {
        this.parentFrame = parentFrame;
        this.listener = listener;

        setLayout(new BorderLayout());
        setBackground(ThemeColors.SIDEBAR_BG);
        setPreferredSize(new Dimension(240, 720));

        // 1. Brand Header
        JPanel brandPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(240, 80);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SIDEBAR_BG);
                g2.fillRect(0, 0, w, h);

                // Brand Logo Mark: Stylized rounded badge
                int logoX = 20;
                int logoY = 22;
                int logoSize = 36;
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(logoX, logoY, logoSize, logoSize, 10, 10);

                // Financial symbol inside logo (₹ symbol)
                g2.setColor(Color.WHITE);
                g2.setFont(FontManager.APP_TITLE);
                g2.drawString("₹", logoX + 11, logoY + 25);

                // Application Title
                g2.setFont(FontManager.CARD_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString("Finance Tracker", logoX + logoSize + 12, logoY + 16);

                // Subtitle
                g2.setFont(FontManager.SMALL);
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString("Personal Wealth Suite", logoX + logoSize + 12, logoY + 30);

                // Bottom divider line
                g2.setColor(ThemeColors.BORDER);
                g2.drawLine(0, h - 1, w, h - 1);

                g2.dispose();
            }
        };
        add(brandPanel, BorderLayout.NORTH);

        // 2. Navigation Menu Links
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new GridLayout(MENU_ITEMS.length, 1, 0, 4));
        menuContainer.setBackground(ThemeColors.SIDEBAR_BG);

        for (String item : MENU_ITEMS) {
            NavItemPanel btn = new NavItemPanel(item);
            navButtons.add(btn);
            menuContainer.add(btn);
        }

        JPanel menuWrapper = new JPanel(new BorderLayout());
        menuWrapper.setBackground(ThemeColors.SIDEBAR_BG);
        menuWrapper.add(menuContainer, BorderLayout.NORTH);
        add(menuWrapper, BorderLayout.CENTER);

        // 3. User Session Profile Card & Logout
        JPanel footerPanel = new JPanel(new BorderLayout(0, 6));
        footerPanel.setBackground(ThemeColors.SIDEBAR_BG);

        JPanel userCardPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(240, 60);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.SIDEBAR_BG);
                g2.fillRect(0, 0, w, h);

                // Top divider
                g2.setColor(ThemeColors.BORDER);
                g2.drawLine(0, 0, w, 0);

                User user = AuthenticationService.getCurrentUser();
                String name = user != null ? user.getFullName() : "Demo User";
                String email = user != null ? user.getEmail() : "user@example.com";

                // Avatar Circle
                int avSize = 34;
                int avX = 20;
                int avY = 13;
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillOval(avX, avY, avSize, avSize);
                g2.setColor(ThemeColors.PRIMARY);
                g2.drawOval(avX, avY, avSize, avSize);

                // Initials
                String initials = (name.length() > 0 ? name.substring(0, 1) : "U").toUpperCase();
                g2.setFont(FontManager.BUTTON_TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, avX + (avSize - fm.stringWidth(initials)) / 2, avY + 22);

                // User name & email
                g2.setFont(FontManager.BODY_BOLD);
                g2.setColor(ThemeColors.TEXT_MAIN);
                if (name.length() > 14) name = name.substring(0, 13) + "…";
                g2.drawString(name, avX + avSize + 10, avY + 16);

                g2.setFont(FontManager.SMALL);
                g2.setColor(user != null && user.isAdmin() ? ThemeColors.PRIMARY : ThemeColors.TEXT_MUTED);
                String sub = (user != null && user.isAdmin()) ? "ADMINISTRATOR" : email;
                if (sub.length() > 16) sub = sub.substring(0, 15) + "…";
                g2.drawString(sub, avX + avSize + 10, avY + 30);

                g2.dispose();
            }
        };

        JPanel actionsPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        actionsPanel.setBackground(ThemeColors.SIDEBAR_BG);

        User currentUser = AuthenticationService.getCurrentUser();
        if (currentUser != null && currentUser.isAdmin()) {
            CustomButton btnAdmin = new CustomButton("★ Super Admin Portal", CustomButton.Style.PRIMARY);
            btnAdmin.setCustomFont(FontManager.SMALL_BUTTON);
            btnAdmin.setPreferredSize(new Dimension(200, 32));
            btnAdmin.addActionListener(e -> {
                if (listener != null) {
                    listener.onNavigate(NAV_ADMIN_PORTAL);
                }
            });
            actionsPanel.add(btnAdmin);
        }

        CustomButton btnLogout = new CustomButton("Log Out", CustomButton.Style.OUTLINE);
        btnLogout.setCustomFont(FontManager.SMALL_BUTTON);
        btnLogout.setPreferredSize(new Dimension(200, 32));
        btnLogout.addActionListener(e -> {
            boolean confirm = ConfirmationDialog.showConfirm(
                parentFrame,
                "Confirm Logout",
                "Are you sure you want to log out of your session?",
                "Log Out",
                true
            );
            if (confirm && listener != null) {
                listener.onLogout();
            }
        });
        actionsPanel.add(btnLogout);

        JPanel actionPadded = new JPanel();
        actionPadded.setBackground(ThemeColors.SIDEBAR_BG);
        actionPadded.add(actionsPanel);

        footerPanel.add(userCardPanel, BorderLayout.NORTH);
        footerPanel.add(actionPadded, BorderLayout.SOUTH);

        add(footerPanel, BorderLayout.SOUTH);
    }

    public void setActiveItem(String item) {
        this.activeItem = item;
        for (NavItemPanel btn : navButtons) {
            btn.repaint();
        }
    }

    private class NavItemPanel extends JPanel {
        private final String itemName;
        private boolean isHovered = false;

        public NavItemPanel(String itemName) {
            this.itemName = itemName;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(240, 42));

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
                    if (listener != null) {
                        listener.onNavigate(itemName);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            boolean isActive = itemName.equals(activeItem);

            // Active / Hover Pill Background
            if (isActive) {
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillRoundRect(12, 4, w - 24, h - 8, 8, 8);

                // Vertical accent bar on active item
                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(12, 8, 4, h - 16, 2, 2);
            } else if (isHovered) {
                g2.setColor(ThemeColors.SURFACE_HOVER);
                g2.fillRoundRect(12, 4, w - 24, h - 8, 8, 8);
            }

            // Draw Vector Icon & Label Text
            Color iconColor = isActive ? ThemeColors.PRIMARY : ThemeColors.TEXT_MUTED;
            drawNavIcon(g2, itemName, 24, (h - 14) / 2, 14, iconColor);

            g2.setFont(isActive ? FontManager.NAV_ITEM_ACTIVE : FontManager.NAV_ITEM);
            g2.setColor(isActive ? ThemeColors.PRIMARY_TEXT : ThemeColors.TEXT_MAIN);
            g2.drawString(itemName, 48, h / 2 + 5);

            g2.dispose();
        }

        private void drawNavIcon(Graphics2D g2, String name, int x, int y, int size, Color color) {
            g2.setColor(color);
            java.awt.Stroke origStroke = g2.getStroke();
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));

            switch (name) {
                case NAV_DASHBOARD: { // 2x2 grid
                    int s = (size - 3) / 2;
                    g2.drawRoundRect(x, y, s, s, 2, 2);
                    g2.drawRoundRect(x + s + 3, y, s, s, 2, 2);
                    g2.drawRoundRect(x, y + s + 3, s, s, 2, 2);
                    g2.drawRoundRect(x + s + 3, y + s + 3, s, s, 2, 2);
                    break;
                }
                case NAV_TRANSACTIONS: { // 2 opposite arrows
                    int mid = y + size / 2;
                    g2.drawLine(x, mid - 3, x + size, mid - 3);
                    g2.drawLine(x + size - 3, mid - 6, x + size, mid - 3);
                    g2.drawLine(x + size - 3, mid, x + size, mid - 3);
                    g2.drawLine(x, mid + 3, x + size, mid + 3);
                    g2.drawLine(x + 3, mid, x, mid + 3);
                    g2.drawLine(x + 3, mid + 6, x, mid + 3);
                    break;
                }
                case NAV_ADD_INCOME: { // Down arrow into base
                    int cx = x + size / 2;
                    g2.drawLine(cx, y + 1, cx, y + size - 3);
                    g2.drawLine(cx - 3, y + size - 6, cx, y + size - 3);
                    g2.drawLine(cx + 3, y + size - 6, cx, y + size - 3);
                    g2.drawLine(x + 1, y + size, x + size - 1, y + size);
                    break;
                }
                case NAV_ADD_EXPENSE: { // Up arrow out of base
                    int cx = x + size / 2;
                    g2.drawLine(cx, y + 3, cx, y + size - 1);
                    g2.drawLine(cx - 3, y + 6, cx, y + 3);
                    g2.drawLine(cx + 3, y + 6, cx, y + 3);
                    g2.drawLine(x + 1, y, x + size - 1, y);
                    break;
                }
                case NAV_BUDGETS: { // Target circle
                    g2.drawOval(x, y, size, size);
                    g2.fillOval(x + size / 2 - 2, y + size / 2 - 2, 4, 4);
                    break;
                }
                case NAV_REPORTS: { // 3-bar histogram
                    int bw = size / 4;
                    int bgap = 2;
                    g2.fillRect(x, y + size - 5, bw, 5);
                    g2.fillRect(x + bw + bgap, y + size - 9, bw, 9);
                    g2.fillRect(x + 2 * (bw + bgap), y + size - 13, bw, 13);
                    break;
                }
                case NAV_PROJECT_OVERVIEW: { // Info circle
                    g2.drawOval(x, y, size, size);
                    int cx = x + size / 2;
                    g2.fillOval(cx - 1, y + 3, 2, 2);
                    g2.drawLine(cx, y + 6, cx, y + size - 3);
                    break;
                }
                case NAV_JDBC_ARCHITECTURE: { // Lightning bolt
                    int[] px = { x + size/2 + 1, x + size/2 - 3, x + size/2, x + size/2 - 2, x + size/2 + 4, x + size/2, x + size/2 + 2 };
                    int[] py = { y, y + size/2, y + size/2, y + size, y + size/2 - 1, y + size/2 - 1, y };
                    g2.drawPolyline(px, py, px.length);
                    break;
                }
                case NAV_DATABASE_TABLES: { // Table grid
                    g2.drawRoundRect(x, y, size, size, 2, 2);
                    g2.drawLine(x, y + 4, x + size, y + 4);
                    g2.drawLine(x + size / 2, y + 4, x + size / 2, y + size);
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
                    g2.fillOval(cx - 1, cy - 1, 3, 3);
                    break;
                }
                default: {
                    g2.fillOval(x + size / 2 - 2, y + size / 2 - 2, 4, 4);
                    break;
                }
            }
            g2.setStroke(origStroke);
        }
    }
}
