package ui;

import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Screen 10: Database Tables & Relational Schema Documentation built with Swing & AWT.
 */
public class DatabaseTablesPanel extends JPanel {

    public DatabaseTablesPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setBackground(ThemeColors.BG_MAIN);

        // Header Title Panel
        JPanel titlePanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(720, 44); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(FontManager.SECTION_TITLE);
                g2.setColor(ThemeColors.TEXT_MAIN);
                g2.drawString("Database Schema & Entities (MySQL Engine)", 10, 28);
                g2.dispose();
            }
        };
        titlePanel.setOpaque(false);
        container.add(titlePanel, BorderLayout.NORTH);

        SchemaViewerPanel schemaPanel = new SchemaViewerPanel();
        container.add(schemaPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private static class SchemaViewerPanel extends JPanel {
        public SchemaViewerPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(720, 960));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            g2.setColor(ThemeColors.SURFACE);
            g2.fillRoundRect(2, 2, w - 4, getHeight() - 4, 12, 12);
            g2.setColor(ThemeColors.BORDER);
            g2.drawRoundRect(2, 2, w - 5, getHeight() - 5, 12, 12);

            int margin = 24;
            int y = 24;

            // TABLE 1: USERS
            y = drawTableSchema(g2, margin, y,
                "1. users Table (Authentication & User Identity)",
                "Stores registered user accounts, encrypted salted PBKDF2 password credentials, and demographic fields.",
                new String[]{"Column Name", "Data Type", "Constraint / Key", "Description"},
                new String[][]{
                    {"user_id", "INT", "PRIMARY KEY, AUTO_INCREMENT", "Unique internal surrogate key identifying the user account"},
                    {"first_name", "VARCHAR(60)", "NOT NULL", "User's first name"},
                    {"last_name", "VARCHAR(60)", "NOT NULL", "User's last name"},
                    {"email", "VARCHAR(120)", "NOT NULL, UNIQUE", "Unique login email address (case-insensitive indexed)"},
                    {"password_hash", "VARCHAR(64)", "NOT NULL", "Hex-encoded PBKDF2-HMAC-SHA-256 cryptographic digest of password + salt"},
                    {"password_salt", "VARCHAR(32)", "NOT NULL", "16-byte cryptographically secure random salt (Base64)"},
                    {"mobile_number", "VARCHAR(20)", "NULL", "Optional phone number for contact verification"},
                    {"date_of_birth", "DATE", "NULL", "Optional date of birth for financial demographic profiling"},
                    {"created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP", "Immutable registration creation timestamp"}
                }
            );

            y += 20;

            // TABLE 2: TRANSACTIONS
            y = drawTableSchema(g2, margin, y,
                "2. transactions Table (Financial Ledger)",
                "Maintains all incoming cash flows (INCOME) and outgoing spending entries (EXPENSE) scoped by user_id.",
                new String[]{"Column Name", "Data Type", "Constraint / Key", "Description"},
                new String[][]{
                    {"transaction_id", "INT", "PRIMARY KEY, AUTO_INCREMENT", "Unique transaction serial identifier"},
                    {"user_id", "INT", "FOREIGN KEY -> users(user_id) ON DELETE CASCADE", "Owner account foreign key ensuring strict isolation"},
                    {"transaction_type", "VARCHAR(10)", "NOT NULL ('INCOME' | 'EXPENSE')", "Discriminator classifying incoming vs outgoing cash flow"},
                    {"amount", "DECIMAL(12,2)", "NOT NULL, CHECK (amount > 0)", "Monetary value represented with two-digit precision"},
                    {"category", "VARCHAR(60)", "NOT NULL, INDEXED", "Category descriptor (e.g. Salary, Food, Travel, Bills)"},
                    {"description", "VARCHAR(255)", "NOT NULL", "Human-readable context note for the transaction"},
                    {"transaction_date", "DATE", "NOT NULL, INDEXED", "Official calendar date the financial event took place"},
                    {"created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP", "System record timestamp for audit trails"},
                    {"updated_at", "TIMESTAMP", "ON UPDATE CURRENT_TIMESTAMP", "Timestamp automatically refreshed on record updates"}
                }
            );

            y += 20;

            // TABLE 3: BUDGETS
            y = drawTableSchema(g2, margin, y,
                "3. budgets Table (Monthly Spending Limits)",
                "Enforces monthly ceiling thresholds per category to track spending against budget targets.",
                new String[]{"Column Name", "Data Type", "Constraint / Key", "Description"},
                new String[][]{
                    {"budget_id", "INT", "PRIMARY KEY, AUTO_INCREMENT", "Surrogate primary key for budget rule"},
                    {"user_id", "INT", "FOREIGN KEY -> users(user_id) ON DELETE CASCADE", "User ownership reference with cascade integrity"},
                    {"category", "VARCHAR(60)", "NOT NULL", "Expense category subject to the budget limitation"},
                    {"budget_month", "VARCHAR(7)", "NOT NULL (Format: 'YYYY-MM')", "Target calendar month for budget tracking"},
                    {"budget_limit", "DECIMAL(12,2)", "NOT NULL, CHECK (budget_limit > 0)", "Upper monetary limit allowed for this category and month"},
                    {"created_at", "TIMESTAMP", "DEFAULT CURRENT_TIMESTAMP", "Date and time the budget target was defined"}
                }
            );

            g2.dispose();
        }

        private int drawTableSchema(Graphics2D g2, int x, int y, String title, String desc, String[] headers, String[][] rows) {
            int tableW = 670;

            // Title
            g2.setFont(FontManager.CARD_TITLE);
            g2.setColor(ThemeColors.PRIMARY);
            g2.drawString(title, x, y);

            // Description
            g2.setFont(FontManager.SMALL);
            g2.setColor(ThemeColors.TEXT_MUTED);
            g2.drawString(desc, x, y + 16);

            y += 28;

            int[] colWidths = {130, 90, 200, 250};

            // Table Header
            g2.setColor(new Color(241, 245, 249));
            g2.fillRect(x, y, tableW, 26);
            g2.setColor(ThemeColors.BORDER);
            g2.drawRect(x, y, tableW, 26);

            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(ThemeColors.TEXT_MAIN);
            int curX = x;
            for (int i = 0; i < headers.length; i++) {
                g2.drawString(headers[i], curX + 8, y + 17);
                curX += colWidths[i];
            }
            y += 26;

            // Rows
            g2.setFont(FontManager.SMALL);
            for (int r = 0; r < rows.length; r++) {
                g2.setColor((r % 2 == 1) ? new Color(248, 250, 252) : Color.WHITE);
                g2.fillRect(x, y, tableW, 24);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRect(x, y, tableW, 24);

                curX = x;
                for (int c = 0; c < rows[r].length; c++) {
                    String val = rows[r][c];
                    if (c == 0) {
                        g2.setColor(ThemeColors.PRIMARY_TEXT);
                        g2.setFont(FontManager.SMALL_BUTTON);
                    } else if (c == 2) {
                        g2.setColor(val.contains("PRIMARY") ? ThemeColors.INCOME :
                                   (val.contains("FOREIGN") ? ThemeColors.INFO : ThemeColors.TEXT_MAIN));
                        g2.setFont(FontManager.SMALL);
                    } else {
                        g2.setColor(ThemeColors.TEXT_MAIN);
                        g2.setFont(FontManager.SMALL);
                    }
                    g2.drawString(val, curX + 8, y + 16);
                    curX += colWidths[c];
                }
                y += 24;
            }
            return y + 16;
        }
    }
}
