package ui.admin;

import dao.AdminDAO;
import model.User;
import ui.components.CustomButton;
import ui.components.MetricCard;
import util.FontManager;
import util.ThemeColors;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Super Admin Panel: Database Health & Schema Inspector.
 * Features an interactive ER relationship canvas, table schema details (PK/FK/Indexes),
 * and live row counts across all database entities.
 */
public class DatabaseHealthSchemaPanel extends JPanel {
    private final User currentAdmin;
    private final AdminDAO adminDAO;

    private DefaultTableModel colTableModel;
    private JTable colTable;
    private JComboBox<String> cmbTables;
    private JLabel lblTableSummary;
    private Map<String, Integer> rowCounts = new LinkedHashMap<>();

    private static class ColumnDef {
        String name;
        String type;
        boolean isPk;
        boolean isFk;
        String fkRef;
        boolean notNull;
        String defaultValue;

        ColumnDef(String name, String type, boolean isPk, boolean isFk, String fkRef, boolean notNull, String defaultValue) {
            this.name = name;
            this.type = type;
            this.isPk = isPk;
            this.isFk = isFk;
            this.fkRef = fkRef;
            this.notNull = notNull;
            this.defaultValue = defaultValue;
        }
    }

    private final Map<String, List<ColumnDef>> schemaMap = new LinkedHashMap<>();

    public DatabaseHealthSchemaPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        this.adminDAO = new AdminDAO();

        setLayout(new BorderLayout(0, 16));
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        initSchemaDefinitions();
        initUI();
        refreshCounts();
    }

    private void initSchemaDefinitions() {
        // 1. users
        List<ColumnDef> users = new ArrayList<>();
        users.add(new ColumnDef("user_id", "INT AUTO_INCREMENT", true, false, null, true, null));
        users.add(new ColumnDef("first_name", "VARCHAR(50)", false, false, null, true, null));
        users.add(new ColumnDef("last_name", "VARCHAR(50)", false, false, null, true, null));
        users.add(new ColumnDef("email", "VARCHAR(100) UNIQUE", false, false, null, true, null));
        users.add(new ColumnDef("password_hash", "VARCHAR(255)", false, false, null, true, null));
        users.add(new ColumnDef("password_salt", "VARCHAR(64)", false, false, null, true, null));
        users.add(new ColumnDef("mobile_number", "VARCHAR(20)", false, false, null, false, "NULL"));
        users.add(new ColumnDef("date_of_birth", "DATE", false, false, null, false, "NULL"));
        users.add(new ColumnDef("role", "ENUM('USER','ADMIN')", false, false, null, true, "'USER'"));
        users.add(new ColumnDef("account_status", "ENUM('ACTIVE','SUSPENDED')", false, false, null, true, "'ACTIVE'"));
        users.add(new ColumnDef("last_login", "DATETIME", false, false, null, false, "NULL"));
        users.add(new ColumnDef("created_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        schemaMap.put("users", users);

        // 2. transactions
        List<ColumnDef> tx = new ArrayList<>();
        tx.add(new ColumnDef("transaction_id", "INT AUTO_INCREMENT", true, false, null, true, null));
        tx.add(new ColumnDef("user_id", "INT", false, true, "users(user_id)", true, null));
        tx.add(new ColumnDef("transaction_date", "DATE", false, false, null, true, null));
        tx.add(new ColumnDef("type", "ENUM('INCOME','EXPENSE')", false, false, null, true, null));
        tx.add(new ColumnDef("category", "VARCHAR(50)", false, false, null, true, null));
        tx.add(new ColumnDef("amount", "DECIMAL(12,2)", false, false, null, true, null));
        tx.add(new ColumnDef("payment_method", "VARCHAR(30)", false, false, null, true, "'Cash'"));
        tx.add(new ColumnDef("description", "VARCHAR(255)", false, false, null, false, "NULL"));
        tx.add(new ColumnDef("created_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        schemaMap.put("transactions", tx);

        // 3. budgets
        List<ColumnDef> bg = new ArrayList<>();
        bg.add(new ColumnDef("budget_id", "INT AUTO_INCREMENT", true, false, null, true, null));
        bg.add(new ColumnDef("user_id", "INT", false, true, "users(user_id)", true, null));
        bg.add(new ColumnDef("category", "VARCHAR(50)", false, false, null, true, null));
        bg.add(new ColumnDef("monthly_limit", "DECIMAL(12,2)", false, false, null, true, null));
        bg.add(new ColumnDef("month_year", "VARCHAR(7)", false, false, null, true, null));
        bg.add(new ColumnDef("created_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        schemaMap.put("budgets", bg);

        // 4. audit_logs
        List<ColumnDef> al = new ArrayList<>();
        al.add(new ColumnDef("log_id", "INT AUTO_INCREMENT", true, false, null, true, null));
        al.add(new ColumnDef("user_id", "INT", false, false, "Nullable", false, "NULL"));
        al.add(new ColumnDef("user_email", "VARCHAR(100)", false, false, null, false, "NULL"));
        al.add(new ColumnDef("action_type", "VARCHAR(50)", false, false, null, true, null));
        al.add(new ColumnDef("target_entity", "VARCHAR(50)", false, false, null, false, "NULL"));
        al.add(new ColumnDef("target_id", "VARCHAR(50)", false, false, null, false, "NULL"));
        al.add(new ColumnDef("status", "VARCHAR(20)", false, false, null, true, null));
        al.add(new ColumnDef("details", "TEXT", false, false, null, false, "NULL"));
        al.add(new ColumnDef("timestamp", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        schemaMap.put("audit_logs", al);

        // 5. application_settings
        List<ColumnDef> st = new ArrayList<>();
        st.add(new ColumnDef("setting_key", "VARCHAR(100)", true, false, null, true, null));
        st.add(new ColumnDef("setting_value", "TEXT", false, false, null, true, null));
        st.add(new ColumnDef("category", "VARCHAR(50)", false, false, null, true, "'GENERAL'"));
        st.add(new ColumnDef("description", "VARCHAR(255)", false, false, null, false, "NULL"));
        st.add(new ColumnDef("updated_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        st.add(new ColumnDef("updated_by", "INT", false, false, "users(user_id)", false, "NULL"));
        schemaMap.put("application_settings", st);

        // 6. user_sessions
        List<ColumnDef> us = new ArrayList<>();
        us.add(new ColumnDef("session_id", "VARCHAR(64)", true, false, null, true, null));
        us.add(new ColumnDef("user_id", "INT", false, true, "users(user_id)", true, null));
        us.add(new ColumnDef("user_email", "VARCHAR(100)", false, false, null, true, null));
        us.add(new ColumnDef("user_fullname", "VARCHAR(100)", false, false, null, true, null));
        us.add(new ColumnDef("role", "VARCHAR(20)", false, false, null, true, null));
        us.add(new ColumnDef("client_info", "VARCHAR(255)", false, false, null, false, "NULL"));
        us.add(new ColumnDef("status", "VARCHAR(20)", false, false, null, true, "'ACTIVE'"));
        us.add(new ColumnDef("created_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        us.add(new ColumnDef("last_activity", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        us.add(new ColumnDef("expires_at", "DATETIME", false, false, null, true, null));
        schemaMap.put("user_sessions", us);

        // 7. password_reset_tokens
        List<ColumnDef> pr = new ArrayList<>();
        pr.add(new ColumnDef("token_id", "INT AUTO_INCREMENT", true, false, null, true, null));
        pr.add(new ColumnDef("user_id", "INT", false, true, "users(user_id)", true, null));
        pr.add(new ColumnDef("user_email", "VARCHAR(100)", false, false, null, true, null));
        pr.add(new ColumnDef("user_fullname", "VARCHAR(100)", false, false, null, true, null));
        pr.add(new ColumnDef("token_hash", "VARCHAR(128)", false, false, null, true, null));
        pr.add(new ColumnDef("client_ip", "VARCHAR(45)", false, false, null, false, "NULL"));
        pr.add(new ColumnDef("status", "VARCHAR(20)", false, false, null, true, "'PENDING'"));
        pr.add(new ColumnDef("created_at", "DATETIME", false, false, null, true, "CURRENT_TIMESTAMP"));
        pr.add(new ColumnDef("expires_at", "DATETIME", false, false, null, true, null));
        schemaMap.put("password_reset_tokens", pr);
    }

    private void initUI() {
        // TOP HEADER
        JPanel topHeader = new JPanel(new BorderLayout(0, 10));
        topHeader.setOpaque(false);

        JPanel headerTitle = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTitle.setOpaque(false);
        JLabel titleLbl = new JLabel("Database Health & Relational Schema");
        titleLbl.setFont(FontManager.SECTION_TITLE);
        titleLbl.setForeground(ThemeColors.TEXT_MAIN);

        JLabel subLbl = new JLabel("Entity-Relationship structure, primary/foreign key constraints, and live table record counts.");
        subLbl.setFont(FontManager.SMALL);
        subLbl.setForeground(ThemeColors.TEXT_MUTED);
        headerTitle.add(titleLbl);
        headerTitle.add(subLbl);
        topHeader.add(headerTitle, BorderLayout.WEST);

        CustomButton btnRefresh = new CustomButton("🔄 Refresh Counts", CustomButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(140, 32));
        btnRefresh.addActionListener(e -> refreshCounts());
        topHeader.add(btnRefresh, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // CENTER: Top ER Relationship Canvas & Bottom Schema Table
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(260);
        splitPane.setResizeWeight(0.45);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // ER Canvas
        ErCanvas erCanvas = new ErCanvas();
        splitPane.setTopComponent(erCanvas);

        // Schema Details Panel
        JPanel schemaDetails = new JPanel(new BorderLayout(0, 8));
        schemaDetails.setBackground(ThemeColors.SURFACE);
        schemaDetails.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel tableSelectorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tableSelectorRow.setOpaque(false);
        tableSelectorRow.add(new JLabel("Inspect Table:"));

        cmbTables = new JComboBox<>(schemaMap.keySet().toArray(new String[0]));
        cmbTables.setFont(FontManager.BODY_BOLD);
        cmbTables.addActionListener(e -> updateTableColumns());
        tableSelectorRow.add(cmbTables);

        lblTableSummary = new JLabel("Rows: -");
        lblTableSummary.setFont(FontManager.BODY);
        lblTableSummary.setForeground(ThemeColors.PRIMARY);
        tableSelectorRow.add(lblTableSummary);

        schemaDetails.add(tableSelectorRow, BorderLayout.NORTH);

        String[] cols = {"Column Name", "Data Type", "Key", "Null", "Default", "Foreign Key Reference"};
        colTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        colTable = new JTable(colTableModel);
        colTable.setRowHeight(28);
        colTable.setFont(FontManager.BODY);
        colTable.getTableHeader().setFont(FontManager.FORM_LABEL);
        colTable.getTableHeader().setBackground(ThemeColors.SURFACE);
        colTable.getTableHeader().setForeground(ThemeColors.TEXT_MAIN);

        colTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        colTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                String k = String.valueOf(val);
                if ("PK".equals(k)) {
                    setForeground(ThemeColors.INCOME);
                } else if ("FK".equals(k)) {
                    setForeground(ThemeColors.ACCENT);
                } else {
                    setForeground(ThemeColors.TEXT_MUTED);
                }
                return comp;
            }
        });

        JScrollPane colScroll = new JScrollPane(colTable);
        colScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.CARD_BORDER));
        schemaDetails.add(colScroll, BorderLayout.CENTER);

        splitPane.setBottomComponent(schemaDetails);
        add(splitPane, BorderLayout.CENTER);

        updateTableColumns();
    }

    public void refreshCounts() {
        rowCounts = adminDAO.getTableCounts();
        updateTableColumns();
        repaint();
    }

    private void updateTableColumns() {
        String selectedTable = (String) cmbTables.getSelectedItem();
        if (selectedTable == null) return;

        List<ColumnDef> cols = schemaMap.get(selectedTable);
        colTableModel.setRowCount(0);

        if (cols != null) {
            for (ColumnDef c : cols) {
                String keyStr = c.isPk ? "PK" : (c.isFk ? "FK" : "-");
                colTableModel.addRow(new Object[]{
                        c.name,
                        c.type,
                        keyStr,
                        c.notNull ? "NO" : "YES",
                        c.defaultValue != null ? c.defaultValue : "NULL",
                        c.fkRef != null ? c.fkRef : "-"
                });
            }
        }

        int count = rowCounts.getOrDefault(selectedTable, 0);
        lblTableSummary.setText("Total Records in Database: " + count);
    }

    /**
     * Custom ER Relationship Graphic Canvas.
     */
    private class ErCanvas extends JPanel {
        ErCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(720, 240));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(ThemeColors.SURFACE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
            g2.setColor(ThemeColors.CARD_BORDER);
            g2.drawRoundRect(2, 2, w - 4, h - 4, 12, 12);

            // Title
            g2.setFont(FontManager.BODY_BOLD);
            g2.setColor(ThemeColors.TEXT_MAIN);
            g2.drawString("Entity-Relationship (ER) Relational Topology", 16, 24);

            // Draw Central Entity (users)
            int uX = w / 2 - 70;
            int uY = 60;
            int uW = 140;
            int uH = 75;

            drawTableBox(g2, "users", uX, uY, uW, uH, ThemeColors.PRIMARY, rowCounts.getOrDefault("users", 0));

            // Related Entities branching out
            int tW = 130;
            int tH = 65;

            // Transactions (Left)
            int txX = 30;
            int txY = 40;
            drawTableBox(g2, "transactions", txX, txY, tW, tH, ThemeColors.INCOME, rowCounts.getOrDefault("transactions", 0));
            drawRelationship(g2, uX, uY + 30, txX + tW, txY + 30, "1 : N (user_id)");

            // Budgets (Left lower)
            int bgX = 30;
            int bgY = 135;
            drawTableBox(g2, "budgets", bgX, bgY, tW, tH, ThemeColors.WARNING, rowCounts.getOrDefault("budgets", 0));
            drawRelationship(g2, uX, uY + 50, bgX + tW, bgY + 30, "1 : N (user_id)");

            // User Sessions (Right)
            int usX = w - 160;
            int usY = 40;
            drawTableBox(g2, "user_sessions", usX, usY, tW, tH, new Color(99, 102, 241), rowCounts.getOrDefault("user_sessions", 0));
            drawRelationship(g2, uX + uW, uY + 30, usX, usY + 30, "1 : N (user_id)");

            // Password Reset Tokens (Right lower)
            int prX = w - 160;
            int prY = 135;
            drawTableBox(g2, "password_reset_tokens", prX, prY, tW, tH, new Color(236, 72, 153), rowCounts.getOrDefault("password_reset_tokens", 0));
            drawRelationship(g2, uX + uW, uY + 50, prX, prY + 30, "1 : N (user_id)");

            g2.dispose();
        }

        private void drawTableBox(Graphics2D g2, String title, int x, int y, int w, int h, Color color, int count) {
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, w, h, 8, 8);

            // Title bar
            g2.setColor(color);
            g2.fillRoundRect(x, y, w, 22, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(FontManager.SMALL_BUTTON);
            g2.drawString(title, x + 8, y + 15);

            // Row count
            g2.setColor(ThemeColors.TEXT_MUTED);
            g2.setFont(FontManager.SMALL);
            g2.drawString("Rows: " + count, x + 8, y + 42);
        }

        private void drawRelationship(Graphics2D g2, int x1, int y1, int x2, int y2, String label) {
            g2.setColor(new Color(148, 163, 184));
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{4.0f, 4.0f}, 0.0f));
            g2.drawLine(x1, y1, x2, y2);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(ThemeColors.TEXT_MUTED);
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2 - 4;
            g2.drawString(label, midX - 25, midY);
        }
    }
}
