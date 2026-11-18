package ui;

import util.FontManager;
import util.ThemeColors;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Screen 8: Comprehensive Academic Project Overview and Technical Documentation
 * built with Java Swing and Java AWT.
 * Covers all 13 required sections in strict hierarchical order.
 */
public class ProjectOverviewPanel extends JPanel {

    public ProjectOverviewPanel() {
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));

        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setBackground(ThemeColors.BG_MAIN);

        // Header Hero Banner
        JPanel heroPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() { return new Dimension(720, 100); }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                g2.setColor(ThemeColors.PRIMARY);
                g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);

                g2.setFont(FontManager.APP_TITLE);
                g2.setColor(Color.WHITE);
                g2.drawString("Personal Finance Tracker", 24, 38);

                g2.setFont(FontManager.BODY);
                g2.setColor(new Color(224, 231, 255));
                g2.drawString("Smart Income, Expense & Budget Management | Technical Project Documentation", 24, 62);

                g2.setFont(FontManager.SMALL);
                g2.setColor(new Color(199, 210, 254));
                g2.drawString("Tech Stack: Java Swing (UI), Java AWT (Graphics & Layouts), JDBC, MySQL Connector/J 8.4", 24, 82);
                g2.dispose();
            }
        };
        heroPanel.setOpaque(false);
        container.add(heroPanel, BorderLayout.NORTH);

        // Main Document Content Panel
        DocumentationPanel docPanel = new DocumentationPanel();
        container.add(docPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(container);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);
    }

    private static class DocumentationPanel extends JPanel {
        public DocumentationPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(740, 2700));
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

            int y = 30;
            int margin = 30;
            int textW = w - 60;

            // SECTION: Table of Contents
            y = drawHeading(g2, "Table of Contents", margin, y);
            String[] toc = {
                "1. Abstract", "2. Introduction", "3. Problem Statement", "4. Objectives",
                "5. Application Architecture", "6. Modules & Components", "7. JDBC Architecture",
                "8. JDBC in Tabular Format", "9. Database Design", "10. Working Principle",
                "11. Advantages & Limitations", "12. Future Enhancements", "13. Conclusion"
            };
            g2.setFont(FontManager.BODY);
            g2.setColor(ThemeColors.PRIMARY);
            for (int i = 0; i < toc.length; i++) {
                int col = i % 2;
                int row = i / 2;
                g2.drawString(toc[i], margin + (col * 320), y + (row * 22));
            }
            y += (toc.length / 2 + 1) * 22 + 20;

            // 1. ABSTRACT
            y = drawSectionHeader(g2, "1. Abstract", margin, y);
            String abstractText =
                "The Personal Finance Tracker is a desktop software application designed to empower individuals with real-time " +
                "income, expense, and budget management. Built using both Java Swing and Java AWT, the application leverages Swing " +
                "for robust window management, tables, and dialogs (JFrame, JPanel, JTable, JComboBox, JScrollPane), while utilizing " +
                "Java AWT for responsive layout managers (BorderLayout, GridBagLayout, GridLayout, FlowLayout), event dispatching, and " +
                "Graphics2D custom rendering of financial charts and progress bars. The system provides secure multi-user authentication " +
                "with PBKDF2-HMAC-SHA-256 key derivation, transaction categorization, dynamic visual analytics (monthly trend bars, category donuts), " +
                "proactive spending budget warnings, and persistent relational storage in MySQL via standard JDBC. Developed for an Advanced " +
                "Java Programming academic demonstration, it exhibits strong Object-Oriented design, thread safety, and data isolation.";
            y = drawParagraph(g2, abstractText, margin, y, textW);

            // 2. INTRODUCTION
            y = drawSectionHeader(g2, "2. Introduction", margin, y);
            String introText =
                "Managing personal cash flow manually through paper ledgers or unstructured spreadsheets frequently produces calculation " +
                "errors, lack of visibility, and accidental data corruption. Modern users require an automated, interactive desktop solution. " +
                "In this project, Java's object-oriented paradigm models users, transactions, and budgets with encapsulation and abstraction. " +
                "Java Swing delivers a rich desktop graphical user interface, complemented by AWT layouts and Graphics2D custom vector drawing. " +
                "JDBC bridges the application with MySQL, enabling transactional integrity, safe SQL queries via PreparedStatement, and reliable " +
                "long-term data persistence.";
            y = drawParagraph(g2, introText, margin, y, textW);

            // 3. PROBLEM STATEMENT
            y = drawSectionHeader(g2, "3. Problem Statement", margin, y);
            String problemText =
                "Traditional personal money management suffers from three primary shortcomings: (1) fragmented expense logging causing " +
                "budget overruns, (2) insecure local record storage with vulnerable plain-text credentials, and (3) lack of immediate visual " +
                "insights into monthly trends. There is an explicit need for an integrated system that:\n" +
                "• Records and classifies all incoming revenues and outgoing expenditures.\n" +
                "• Computes live net balance, spending ratios, and budget limits automatically.\n" +
                "• Enforces strict client-side and server-side data validation on dates, amounts, and credentials.\n" +
                "• Completely safeguards user privacy through database authentication and salted PBKDF2 key derivation.\n" +
                "• Renders polished visual analytics directly on the desktop canvas.";
            y = drawParagraph(g2, problemText, margin, y, textW);

            // 4. OBJECTIVES
            y = drawSectionHeader(g2, "4. Objectives", margin, y);
            String[] objs = {
                "1. User Authentication & Profile Security: Implement secure registration, PBKDF2 credential derivation, and user-scoped data isolation.",
                "2. Financial Dashboard & Metric Visuals: Deliver immediate overview cards for Total Income, Total Expenses, and Net Balance.",
                "3. Transaction Lifecycle Management: Enable full CRUD operations (Create, Read, Update, Delete) with search, multi-criteria filters, and JTable rendering.",
                "4. Budget Monitoring & Warnings: Provide proactive monthly threshold alerts before category budgets are exceeded.",
                "5. Analytical Data Visualization: Build custom Graphics2D components for monthly trend charts and expense donuts.",
                "6. Robust JDBC Data Layer: Implement safe, parameterized SQL queries via PreparedStatement and reliable connection handling."
            };
            for (String o : objs) {
                y = drawParagraph(g2, o, margin, y, textW);
            }

            // 5. APPLICATION ARCHITECTURE
            y = drawSectionHeader(g2, "5. Application Architecture", margin, y);
            String archDesc =
                "The application strictly follows a 4-tier architectural separation of concerns:\n" +
                "• Presentation Layer (Java Swing & AWT): JFrame, JPanel, JTable, JComboBox, CardLayout, custom buttons, and Graphics2D charts.\n" +
                "• Business Logic Layer: Services governing authentication, transaction integrity, budget progress, and report synthesis.\n" +
                "• Data Access Layer (DAO): UserDAO, TransactionDAO, and BudgetDAO executing parameterized JDBC queries.\n" +
                "• Database Layer (MySQL): InnoDB relational storage maintaining referential integrity across users, transactions, and budgets.";
            y = drawParagraph(g2, archDesc, margin, y, textW);

            // 6. MODULES & COMPONENTS TABLE
            y = drawSectionHeader(g2, "6. Modules and Components", margin, y);
            y = drawTable(g2, margin, y, new String[]{"Module", "Core Responsibilities", "Primary Java Classes"},
                new String[][]{
                    {"Authentication", "User signup, salted PBKDF2 verification, login session", "LoginFrame, RegisterFrame, AuthenticationService, UserDAO"},
                    {"Dashboard", "Metric cards, 6-month trends, category donut, recent JTable", "DashboardFrame, DashboardPanel, MetricCard, ChartCanvas"},
                    {"Transactions", "Add, edit, delete, search, multi-filter, sort, CSV export", "TransactionPanel, TransactionFormDialog, TransactionDAO"},
                    {"Budgeting", "Category spending limits, real-time consumption progress", "BudgetPanel, BudgetService, ProgressBarComponent, BudgetDAO"},
                    {"Reports", "Financial analytics, category breakdown, CSV generation", "ReportsPanel, ReportService, CsvExporter"},
                    {"JDBC & DB", "Connection pooling/testing, DDL initialization, CRUD SQL", "DatabaseConnection, DatabaseInitializer, PreparedStatement"},
                    {"Design System", "Theme colors, font hierarchy, vector icons, custom button", "ThemeColors, FontManager, CustomButton, CardPanel"}
                }
            );

            // 7. JDBC ARCHITECTURE
            y = drawSectionHeader(g2, "7. JDBC Architecture", margin, y);
            String jdbcDesc =
                "JDBC (Java Database Connectivity) provides the API enabling Java code to interact seamlessly with MySQL. " +
                "The connection workflow proceeds as follows:\n" +
                "1. Driver Registration: DriverManager loads 'com.mysql.cj.jdbc.Driver' into the JVM.\n" +
                "2. Connection Factory: DatabaseConnection.getConnection() issues a connection URL with TCP handshake to port 3306.\n" +
                "3. Parameterized Execution: PreparedStatement compiles the SQL query once and binds typed parameters to prevent SQL injection.\n" +
                "4. Result Set Processing: ResultSet cursor scrolls through tuples, which are mapped into Java Model POJOs.\n" +
                "5. Deterministic Teardown: try-with-resources blocks guarantee immediate closure of ResultSet, Statement, and Connection.";
            y = drawParagraph(g2, jdbcDesc, margin, y, textW);

            // 8. JDBC IN TABULAR FORMAT
            y = drawSectionHeader(g2, "8. JDBC in Tabular Format", margin, y);
            y = drawTable(g2, margin, y, new String[]{"JDBC Component", "Architectural Purpose", "Application Usage in Code"},
                new String[][]{
                    {"DriverManager", "Manages database drivers & establishes connections", "DatabaseConnection.getConnection() to instantiate MySQL sessions"},
                    {"Connection", "Represents an active database communication session", "Underlies transaction operations and statements"},
                    {"PreparedStatement", "Executes pre-compiled parameterized SQL securely", "All inserts, updates, deletes, and queries with ? placeholders"},
                    {"ResultSet", "Scrollable cursor over query result rows", "Maps SQL rows to User, Transaction, and Budget POJO models"},
                    {"SQLException", "Encapsulates database errors, constraint violations", "Caught gracefully; triggers friendly error labels in UI"},
                    {"try-with-resources", "Guarantees resource closure upon block completion", "Ensures zero database connection or cursor leaks"}
                }
            );

            // 9. DATABASE DESIGN
            y = drawSectionHeader(g2, "9. Database Design (Entity Relationships)", margin, y);
            String dbDesignText =
                "The relational schema comprises three primary tables in InnoDB engine:\n" +
                "• users (user_id PK, first_name, last_name, email UNIQUE, password_hash, password_salt, mobile_number, created_at)\n" +
                "• transactions (transaction_id PK, user_id FK -> users(user_id) ON DELETE CASCADE, transaction_type, amount, category, description, transaction_date)\n" +
                "• budgets (budget_id PK, user_id FK -> users(user_id) ON DELETE CASCADE, category, budget_month, budget_limit, UNIQUE(user_id, category, budget_month))\n" +
                "Referential integrity with CASCADE ensures orphan transactions or budgets never exist when a user account is removed.";
            y = drawParagraph(g2, dbDesignText, margin, y, textW);

            // 10. WORKING PRINCIPLE
            y = drawSectionHeader(g2, "10. Working Principle", margin, y);
            String workText =
                "Workflow Cycle:\n" +
                "Launch Application → Initialize Database & Check Tables → Present Swing LoginFrame → Verify Salted PBKDF2 Credentials → " +
                "Open Main DashboardFrame → Retrieve Current Month Aggregates via JDBC → Render Metric Cards and Graphics2D Charts → " +
                "User Selects Operation (e.g. Add Transaction) → Client-Side Field Validation → Execute PreparedStatement via DAO → " +
                "Commit MySQL Transaction → Trigger Real-Time Dashboard & Chart Refresh on Swing EDT.";
            y = drawParagraph(g2, workText, margin, y, textW);

            // 11. ADVANTAGES & LIMITATIONS
            y = drawSectionHeader(g2, "11. Advantages and Limitations", margin, y);
            String advText =
                "Advantages:\n" +
                "• Dual Swing & AWT Foundation: Combines Swing's rich components with AWT's lightweight layouts and Graphics2D drawing.\n" +
                "• Strict Relational Integrity: MySQL foreign keys and UNIQUE constraints eliminate data corruption.\n" +
                "• Hardened Security: Salted PBKDF2-HMAC-SHA-256 derivation prevents credential compromise.\n" +
                "• Non-blocking UI: Concurrency patterns keep long database queries off Swing's Event Dispatch Thread.\n\n" +
                "Limitations:\n" +
                "• Requires local or networked MySQL instance for persistent storage.\n" +
                "• Desktop client interface without mobile web push notifications.";
            y = drawParagraph(g2, advText, margin, y, textW);

            // 12. FUTURE ENHANCEMENTS
            y = drawSectionHeader(g2, "12. Future Enhancements", margin, y);
            String futureText =
                "• PDF Report Export: Integration of iText or Apache PDFBox for printable statements.\n" +
                "• Recurring Scheduled Transactions: Auto-logging monthly subscriptions and rent.\n" +
                "• Multi-Currency Conversion: Live exchange rate fetching via REST APIs.\n" +
                "• Automated Email/SMS Overrun Alerts: Scheduled notification daemon for budget limits.";
            y = drawParagraph(g2, futureText, margin, y, textW);

            // 13. CONCLUSION
            y = drawSectionHeader(g2, "13. Conclusion", margin, y);
            String conclText =
                "The Personal Finance Tracker successfully demonstrates the combined application of Java Swing and Java AWT—including " +
                "modular desktop windows, responsive layout management, custom Graphics2D charts, secure database persistence via JDBC, " +
                "and transactional integrity in MySQL. The resulting system merges academic engineering rigor with an intuitive, polished user experience.";
            y = drawParagraph(g2, conclText, margin, y, textW);

            g2.dispose();
        }

        private int drawHeading(Graphics2D g2, String text, int x, int y) {
            g2.setFont(FontManager.SECTION_TITLE);
            g2.setColor(ThemeColors.PRIMARY);
            g2.drawString(text, x, y);
            g2.setColor(ThemeColors.BORDER);
            g2.drawLine(x, y + 8, x + 660, y + 8);
            return y + 28;
        }

        private int drawSectionHeader(Graphics2D g2, String text, int x, int y) {
            y += 14;
            g2.setFont(FontManager.CARD_TITLE);
            g2.setColor(ThemeColors.TEXT_MAIN);
            g2.drawString(text, x, y);
            g2.setColor(ThemeColors.DIVIDER);
            g2.drawLine(x, y + 6, x + 660, y + 6);
            return y + 24;
        }

        private int drawParagraph(Graphics2D g2, String text, int x, int y, int width) {
            g2.setFont(FontManager.BODY);
            g2.setColor(ThemeColors.TEXT_MAIN);
            FontMetrics fm = g2.getFontMetrics();

            String[] lines = text.split("\n");
            for (String line : lines) {
                if (line.startsWith("•")) {
                    g2.drawString(line, x + 10, y);
                    y += 20;
                    continue;
                }
                String[] words = line.split(" ");
                StringBuilder sb = new StringBuilder();
                for (String word : words) {
                    if (fm.stringWidth(sb + word) < width) {
                        sb.append(word).append(" ");
                    } else {
                        g2.drawString(sb.toString(), x, y);
                        y += 19;
                        sb = new StringBuilder(word).append(" ");
                    }
                }
                if (sb.length() > 0) {
                    g2.drawString(sb.toString(), x, y);
                    y += 19;
                }
                y += 6;
            }
            return y;
        }

        private int drawTable(Graphics2D g2, int x, int y, String[] headers, String[][] rows) {
            int tableW = 660;
            int colW = tableW / headers.length;

            // Header background
            g2.setColor(new Color(241, 245, 249));
            g2.fillRect(x, y, tableW, 28);
            g2.setColor(ThemeColors.BORDER);
            g2.drawRect(x, y, tableW, 28);

            g2.setFont(FontManager.SMALL_BUTTON);
            g2.setColor(ThemeColors.TEXT_MAIN);
            for (int i = 0; i < headers.length; i++) {
                g2.drawString(headers[i], x + (i * colW) + 8, y + 18);
            }
            y += 28;

            g2.setFont(FontManager.SMALL);
            for (int r = 0; r < rows.length; r++) {
                g2.setColor((r % 2 == 1) ? new Color(248, 250, 252) : Color.WHITE);
                g2.fillRect(x, y, tableW, 26);
                g2.setColor(ThemeColors.BORDER);
                g2.drawRect(x, y, tableW, 26);

                g2.setColor(ThemeColors.TEXT_MAIN);
                for (int c = 0; c < rows[r].length; c++) {
                    String val = rows[r][c];
                    if (val.length() > 36) val = val.substring(0, 34) + "…";
                    g2.drawString(val, x + (c * colW) + 8, y + 17);
                }
                y += 26;
            }
            return y + 16;
        }
    }
}
