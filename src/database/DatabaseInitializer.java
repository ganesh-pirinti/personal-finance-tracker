package database;

import util.PasswordHasher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Initializes database schema, creates required tables, runs automated migrations,
 * and seeds initial demo data when connected to MySQL.
 */
public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    /**
     * Initializes database and tables. Returns true if successful or already initialized.
     */
    public static boolean initialize() {
        // Step 1: Ensure database exists
        try (Connection serverConn = DatabaseConnection.getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " +
                    DatabaseConnection.getCurrentConfig().getDatabaseName() +
                    " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            System.err.println("Could not create database schema: " + e.getMessage());
            return false;
        }

        // Step 2: Ensure tables exist & apply migrations
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Users Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  first_name VARCHAR(60) NOT NULL," +
                "  last_name VARCHAR(60) NOT NULL," +
                "  email VARCHAR(120) NOT NULL UNIQUE," +
                "  password_hash VARCHAR(64) NOT NULL," +
                "  password_salt VARCHAR(32) NOT NULL," +
                "  mobile_number VARCHAR(20) NULL," +
                "  date_of_birth DATE NULL," +
                "  role VARCHAR(20) NOT NULL DEFAULT 'USER'," +
                "  account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "  last_login TIMESTAMP NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB"
            );

            // Safe migration for existing users table
            addColumnIfNotExists(conn, "users", "role", "VARCHAR(20) NOT NULL DEFAULT 'USER'");
            addColumnIfNotExists(conn, "users", "account_status", "VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
            addColumnIfNotExists(conn, "users", "last_login", "TIMESTAMP NULL");

            // Transactions Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  transaction_type VARCHAR(10) NOT NULL," +
                "  amount DECIMAL(12, 2) NOT NULL," +
                "  category VARCHAR(60) NOT NULL," +
                "  description VARCHAR(255) NOT NULL," +
                "  transaction_date DATE NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );

            // Budgets Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS budgets (" +
                "  budget_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  category VARCHAR(60) NOT NULL," +
                "  budget_month VARCHAR(7) NOT NULL," +
                "  budget_limit DECIMAL(12, 2) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_budget_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE," +
                "  CONSTRAINT uq_user_cat_month UNIQUE (user_id, category, budget_month)" +
                ") ENGINE=InnoDB"
            );

            // User Sessions Table (Session Management Center)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_sessions (" +
                "  session_id VARCHAR(64) PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  expires_at TIMESTAMP NOT NULL," +
                "  revoked_at TIMESTAMP NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                "  client_info VARCHAR(255) NULL," +
                "  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );

            // Password Reset Tokens Table (Authentication & Recovery)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                "  token_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  token_hash VARCHAR(64) NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  expires_at TIMESTAMP NOT NULL," +
                "  used_at TIMESTAMP NULL," +
                "  revoked_at TIMESTAMP NULL," +
                "  client_ip VARCHAR(60) NULL," +
                "  CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );

            // Admin Audit Logs Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS admin_audit_logs (" +
                "  event_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  actor_user_id INT NOT NULL," +
                "  actor_email VARCHAR(120) NOT NULL," +
                "  action_type VARCHAR(60) NOT NULL," +
                "  target_type VARCHAR(60) NOT NULL," +
                "  target_id VARCHAR(60) NOT NULL," +
                "  outcome VARCHAR(20) NOT NULL," +
                "  description TEXT NOT NULL," +
                "  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB"
            );

            // UI Text Settings Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ui_text_settings (" +
                "  text_key VARCHAR(100) PRIMARY KEY," +
                "  text_value TEXT NOT NULL," +
                "  category VARCHAR(50) NOT NULL DEFAULT 'GENERAL'," +
                "  description VARCHAR(255) NULL," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  updated_by INT NOT NULL DEFAULT 1" +
                ") ENGINE=InnoDB"
            );

            // Theme Versions Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS theme_versions (" +
                "  version_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  theme_name VARCHAR(100) NOT NULL," +
                "  is_dark BOOLEAN NOT NULL DEFAULT FALSE," +
                "  color_primary VARCHAR(10) NOT NULL," +
                "  color_primary_hover VARCHAR(10) NOT NULL," +
                "  color_bg_main VARCHAR(10) NOT NULL," +
                "  color_surface VARCHAR(10) NOT NULL," +
                "  color_surface_hover VARCHAR(10) NOT NULL," +
                "  color_sidebar_bg VARCHAR(10) NOT NULL," +
                "  color_header_bg VARCHAR(10) NOT NULL," +
                "  color_text_main VARCHAR(10) NOT NULL," +
                "  color_text_muted VARCHAR(10) NOT NULL," +
                "  color_border VARCHAR(10) NOT NULL," +
                "  color_income VARCHAR(10) NOT NULL," +
                "  color_expense VARCHAR(10) NOT NULL," +
                "  color_balance VARCHAR(10) NOT NULL," +
                "  color_warning VARCHAR(10) NOT NULL," +
                "  font_family VARCHAR(50) NOT NULL DEFAULT 'Segoe UI'," +
                "  font_scale_percent INT NOT NULL DEFAULT 100," +
                "  card_corner_radius INT NOT NULL DEFAULT 16," +
                "  layout_density VARCHAR(20) NOT NULL DEFAULT 'COMFORTABLE'," +
                "  is_active BOOLEAN NOT NULL DEFAULT FALSE," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  created_by INT NOT NULL DEFAULT 1" +
                ") ENGINE=InnoDB"
            );

            // Indexes
            createIndexIfNotExists(conn, "transactions", "idx_tx_user_date", "(user_id, transaction_date)");
            createIndexIfNotExists(conn, "transactions", "idx_tx_user_type", "(user_id, transaction_type)");
            createIndexIfNotExists(conn, "transactions", "idx_tx_user_category", "(user_id, category)");
            createIndexIfNotExists(conn, "budgets", "idx_budget_user_month", "(user_id, budget_month)");
            createIndexIfNotExists(conn, "user_sessions", "idx_session_user", "(user_id)");
            createIndexIfNotExists(conn, "user_sessions", "idx_session_status", "(status)");
            createIndexIfNotExists(conn, "password_reset_tokens", "idx_reset_user", "(user_id)");
            createIndexIfNotExists(conn, "password_reset_tokens", "idx_reset_hash", "(token_hash)");
            createIndexIfNotExists(conn, "admin_audit_logs", "idx_audit_time", "(timestamp)");
            createIndexIfNotExists(conn, "admin_audit_logs", "idx_audit_action", "(action_type)");

            // Check if demo user exists; if not, seed sample data
            seedDemoDataIfEmpty(conn);

            return true;
        } catch (SQLException e) {
            System.err.println("Database table initialization failed: " + e.getMessage());
            return false;
        }
    }

    private static void addColumnIfNotExists(Connection conn, String table, String column, String definition) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException ignored) {
            // Column already exists or table cannot be altered
        }
    }

    private static void createIndexIfNotExists(Connection conn, String table, String indexName, String columns) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE INDEX " + indexName + " ON " + table + " " + columns);
        } catch (SQLException ignored) {
            // Index already exists
        }
    }

    private static void seedDemoDataIfEmpty(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Seed standard user: alex@example.com strictly as USER role (zero default ADMIN accounts)
                String salt = PasswordHasher.generateSalt();
                String hash = PasswordHasher.hashPassword("Demo@123", salt);

                String insertUser = "INSERT INTO users (first_name, last_name, email, password_hash, password_salt, " +
                                    "mobile_number, date_of_birth, role, account_status) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'USER', 'ACTIVE')";
                int userId = -1;
                try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Alex");
                    ps.setString(2, "Sharma");
                    ps.setString(3, "alex@example.com");
                    ps.setString(4, hash);
                    ps.setString(5, salt);
                    ps.setString(6, "+91 9876543210");
                    ps.setDate(7, Date.valueOf(LocalDate.of(1998, 5, 15)));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            userId = keys.getInt(1);
                        }
                    }
                }

                if (userId > 0) {
                    // Seed sample transactions
                    String insertTx = "INSERT INTO transactions (user_id, transaction_type, amount, category, description, transaction_date) " +
                                      "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertTx)) {
                        LocalDate today = LocalDate.now();

                        // Incomes
                        addTx(ps, userId, "INCOME", 65000.00, "Salary", "Monthly Software Engineering Salary", today.withDayOfMonth(1));
                        addTx(ps, userId, "INCOME", 12500.00, "Freelance", "Mobile App UI Design Project", today.minusDays(10));
                        addTx(ps, userId, "INCOME", 5000.00, "Scholarship", "Research Fellowship Stipend", today.minusDays(18));

                        // Expenses
                        addTx(ps, userId, "EXPENSE", 18000.00, "Education", "Semester Tuition & Lab Fee", today.minusDays(14));
                        addTx(ps, userId, "EXPENSE", 6500.00, "Food", "Monthly Groceries & Dining", today.minusDays(4));
                        addTx(ps, userId, "EXPENSE", 3200.00, "Bills", "Broadband & Electricity Utility", today.minusDays(8));
                        addTx(ps, userId, "EXPENSE", 2400.00, "Travel", "Metro Rail Pass & Fuel", today.minusDays(6));
                        addTx(ps, userId, "EXPENSE", 4500.00, "Shopping", "Tech Accessories & Books", today.minusDays(2));
                        addTx(ps, userId, "EXPENSE", 1500.00, "Entertainment", "Streaming & Cinema", today.minusDays(1));
                        ps.executeBatch();
                    }

                    // Seed sample budgets
                    String insertBudget = "INSERT INTO budgets (user_id, category, budget_month, budget_limit) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertBudget)) {
                        String currentMonth = todayMonthKey();
                        addBudget(ps, userId, "Food", currentMonth, 10000.00);
                        addBudget(ps, userId, "Travel", currentMonth, 5000.00);
                        addBudget(ps, userId, "Shopping", currentMonth, 6000.00);
                        addBudget(ps, userId, "Bills", currentMonth, 4000.00);
                        addBudget(ps, userId, "Entertainment", currentMonth, 3000.00);
                        ps.executeBatch();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Seed demo data failed: " + e.getMessage());
        }
    }

    private static void addTx(PreparedStatement ps, int userId, String type, double amount, String category, String desc, LocalDate date) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, type);
        ps.setDouble(3, amount);
        ps.setString(4, category);
        ps.setString(5, desc);
        ps.setDate(6, Date.valueOf(date));
        ps.addBatch();
    }

    private static void addBudget(PreparedStatement ps, int userId, String category, String month, double limit) throws SQLException {
        ps.setInt(1, userId);
        ps.setString(2, category);
        ps.setString(3, month);
        ps.setDouble(4, limit);
        ps.addBatch();
    }

    private static String todayMonthKey() {
        return util.DateUtils.getCurrentMonthKey();
    }
}
