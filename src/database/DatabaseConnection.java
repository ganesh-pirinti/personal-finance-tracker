package database;

import model.DatabaseConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Manages JDBC connections to MySQL database with multi-tier configuration loading,
 * connection testing, robust diagnostics, and leak-free resource cleanup.
 */
public final class DatabaseConnection {
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String CONFIG_FILE = "resources/db.properties";

    private static DatabaseConfig currentConfig;

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {}

    /**
     * Loads database configuration following a 4-tier resolution hierarchy:
     * 1. JVM System Properties (-Ddb.host, -Ddb.user, -Ddb.password, etc.)
     * 2. Operating System Environment Variables (DB_HOST, DB_USER, DB_PASSWORD, etc.)
     * 3. Classpath Resource (/db.properties or db.properties)
     * 4. Filesystem Locations (resources/db.properties, db.properties)
     */
    public static synchronized void loadConfiguration() {
        Properties props = new Properties();

        // Tier 4: Filesystem configuration lookup
        String[] filePaths = {"resources/db.properties", "db.properties", "../resources/db.properties"};
        for (String fp : filePaths) {
            File f = new File(fp);
            if (f.exists() && f.canRead()) {
                try (FileInputStream in = new FileInputStream(f)) {
                    props.load(in);
                    break;
                } catch (IOException e) {
                    System.err.println("Could not read " + fp + ": " + e.getMessage());
                }
            }
        }

        // Tier 3: Classpath resource configuration lookup (if not loaded from file)
        if (props.isEmpty()) {
            try (InputStream cpStream = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
                if (cpStream != null) {
                    props.load(cpStream);
                }
            } catch (Exception ignored) {}

            if (props.isEmpty()) {
                try (InputStream cpStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                    if (cpStream != null) {
                        props.load(cpStream);
                    }
                } catch (Exception ignored) {}
            }
        }

        // Resolve base values with key aliases
        String host = getPropertyWithFallback(props, "localhost", "db.host", "host", "jdbc.host");
        int port = 3306;
        String portStr = getPropertyWithFallback(props, "3306", "db.port", "port", "jdbc.port");
        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException ignored) {}

        String dbName = getPropertyWithFallback(props, "personal_finance_tracker", "db.name", "db.database", "database", "jdbc.database");
        String user = getPropertyWithFallback(props, "root", "db.user", "db.username", "jdbc.username", "user", "username");
        String pass = getPropertyWithFallback(props, "", "db.password", "db.pass", "jdbc.password", "password", "pass");

        // Tier 2: Overlay Environment Variables (if set and non-empty)
        String envHost = System.getenv("DB_HOST");
        if (isNonEmpty(envHost)) host = envHost.trim();

        String envPort = System.getenv("DB_PORT");
        if (isNonEmpty(envPort)) {
            try { port = Integer.parseInt(envPort.trim()); } catch (NumberFormatException ignored) {}
        }

        String envDb = System.getenv("DB_NAME");
        if (!isNonEmpty(envDb)) envDb = System.getenv("DB_DATABASE");
        if (isNonEmpty(envDb)) dbName = envDb.trim();

        String envUser = System.getenv("DB_USER");
        if (!isNonEmpty(envUser)) envUser = System.getenv("DB_USERNAME");
        if (isNonEmpty(envUser)) user = envUser.trim();

        String envPass = System.getenv("DB_PASSWORD");
        if (!isNonEmpty(envPass)) envPass = System.getenv("DB_PASS");
        if (envPass != null) pass = envPass; // allow empty if explicitly provided

        String envUrl = System.getenv("DB_URL");
        if (isNonEmpty(envUrl)) {
            parseJdbcUrl(envUrl.trim(), host, port, dbName);
        }

        // Tier 1: Overlay JVM System Properties (highest precedence)
        String sysHost = System.getProperty("db.host");
        if (isNonEmpty(sysHost)) host = sysHost.trim();

        String sysPort = System.getProperty("db.port");
        if (isNonEmpty(sysPort)) {
            try { port = Integer.parseInt(sysPort.trim()); } catch (NumberFormatException ignored) {}
        }

        String sysDb = System.getProperty("db.name");
        if (!isNonEmpty(sysDb)) sysDb = System.getProperty("db.database");
        if (isNonEmpty(sysDb)) dbName = sysDb.trim();

        String sysUser = System.getProperty("db.user");
        if (!isNonEmpty(sysUser)) sysUser = System.getProperty("db.username");
        if (isNonEmpty(sysUser)) user = sysUser.trim();

        String sysPass = System.getProperty("db.password");
        if (!isNonEmpty(sysPass)) sysPass = System.getProperty("db.pass");
        if (sysPass != null) pass = sysPass;

        currentConfig = new DatabaseConfig(host, port, dbName, user, pass);
    }

    private static String getPropertyWithFallback(Properties props, String defaultValue, String... keys) {
        for (String k : keys) {
            String val = props.getProperty(k);
            if (val != null) {
                return val.trim();
            }
        }
        return defaultValue;
    }

    private static boolean isNonEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static void parseJdbcUrl(String url, String defaultHost, int defaultPort, String defaultDb) {
        try {
            if (url.startsWith("jdbc:mysql://")) {
                String clean = url.substring("jdbc:mysql://".length());
                int slashIdx = clean.indexOf('/');
                if (slashIdx > 0) {
                    String hostPort = clean.substring(0, slashIdx);
                    String dbAndParams = clean.substring(slashIdx + 1);
                    int qIdx = dbAndParams.indexOf('?');
                    String db = (qIdx > 0) ? dbAndParams.substring(0, qIdx) : dbAndParams;

                    String[] hp = hostPort.split(":");
                    String host = hp[0];
                    int port = hp.length > 1 ? Integer.parseInt(hp[1]) : defaultPort;
                    currentConfig = new DatabaseConfig(host, port, db,
                            currentConfig != null ? currentConfig.getUsername() : "root",
                            currentConfig != null ? currentConfig.getPassword() : "");
                }
            }
        } catch (Exception ignored) {}
    }

    public static synchronized DatabaseConfig getCurrentConfig() {
        if (currentConfig == null) {
            loadConfiguration();
        }
        return currentConfig;
    }

    public static String getHost() {
        return getCurrentConfig() != null ? getCurrentConfig().getHost() : "localhost";
    }

    public static int getPort() {
        return getCurrentConfig() != null ? getCurrentConfig().getPort() : 3306;
    }

    public static String getDatabaseName() {
        return getCurrentConfig() != null ? getCurrentConfig().getDatabaseName() : "personal_finance_tracker";
    }

    public static synchronized void updateConfig(String host, int port, String dbName, String user, String pass) {
        currentConfig = new DatabaseConfig(host, port, dbName, user, pass);
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.name", dbName);
        props.setProperty("db.user", user);
        props.setProperty("db.password", pass);

        File file = new File(CONFIG_FILE);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "Personal Finance Tracker Database Settings");
        } catch (IOException e) {
            System.err.println("Failed to save db.properties: " + e.getMessage());
        }
        // Invalidate cached status so subsequent probes reflect changes
        cachedStatus = null;
    }

    /**
     * Checks if the target host and port are accepting TCP connections.
     */
    public static boolean isPortReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtains a new JDBC connection to the target application database schema using DriverManager.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath: " + DRIVER_CLASS, e);
        }

        DatabaseConfig cfg = getCurrentConfig();
        return DriverManager.getConnection(cfg.getJdbcUrl(), cfg.getUsername(), cfg.getPassword());
    }

    /**
     * Obtains connection to MySQL server without specifying schema (for database creation).
     */
    public static Connection getServerConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath: " + DRIVER_CLASS, e);
        }

        DatabaseConfig cfg = getCurrentConfig();
        return DriverManager.getConnection(cfg.getServerOnlyJdbcUrl(), cfg.getUsername(), cfg.getPassword());
    }

    public enum ConnectionState {
        CONNECTED("Connected"),
        DISCONNECTED("MySQL Disconnected"),
        AUTH_FAILED("Authentication Failed"),
        DATABASE_UNAVAILABLE("Database Unavailable"),
        SCHEMA_MIGRATION_REQUIRED("Schema Migration Required"),
        DEMONSTRATION_MODE("Demonstration Mode");

        private final String displayName;

        ConnectionState(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Result of a connection test with diagnostics.
     */
    public static class ConnectionStatus {
        public boolean success;
        public ConnectionState state = ConnectionState.DISCONNECTED;
        public String driverName = "com.mysql.cj.jdbc.Driver";
        public String driverVersion = "8.4.0";
        public String databaseProductName = "MySQL";
        public String databaseProductVersion = "Unknown";
        public String host = "localhost";
        public int port = 3306;
        public String databaseName = "personal_finance_tracker";
        public String username = "root";
        public long latencyMs = -1;
        public String errorMessage = null;
        public String sanitizedDiagnosis = "Database disconnected.";
        public String testTimestamp = null;

        public ConnectionStatus() {}

        public ConnectionStatus(boolean success, ConnectionState state, long latencyMs,
                                String sanitizedDiagnosis, String host, int port,
                                String databaseName, String testTimestamp) {
            this.success = success;
            this.state = state;
            this.latencyMs = latencyMs;
            this.sanitizedDiagnosis = sanitizedDiagnosis;
            this.host = host;
            this.port = port;
            this.databaseName = databaseName;
            this.testTimestamp = testTimestamp;
        }

        @Override
        public String toString() {
            return state.getDisplayName() + (latencyMs >= 0 ? " (" + latencyMs + "ms)" : "") +
                    (sanitizedDiagnosis != null ? ": " + sanitizedDiagnosis : "");
        }
    }

    private static ConnectionStatus cachedStatus = null;
    private static long lastCheckTime = 0;
    private static final long CACHE_TTL_MS = 2500; // 2.5 second cache to prevent hammering on EDT

    /**
     * Runs a live test connection query with caching.
     */
    public static ConnectionStatus testConnection() {
        return testConnection(false);
    }

    /**
     * Runs a connection test, optionally forcing a live probe.
     */
    public static synchronized ConnectionStatus testConnection(boolean force) {
        long now = System.currentTimeMillis();
        boolean currentDemo = service.AuthenticationService.isDemoMode();
        boolean cachedDemo = (cachedStatus != null && cachedStatus.state == ConnectionState.DEMONSTRATION_MODE);
        if (!force && cachedStatus != null && (now - lastCheckTime < CACHE_TTL_MS) && (currentDemo == cachedDemo)) {
            return cachedStatus;
        }

        ConnectionStatus status = new ConnectionStatus();
        DatabaseConfig cfg = getCurrentConfig();
        status.host = cfg.getHost();
        status.port = cfg.getPort();
        status.databaseName = cfg.getDatabaseName();
        status.username = cfg.getUsername();
        status.testTimestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Check if currently operating in explicit Demonstration Mode
        if (service.AuthenticationService.isDemoMode()) {
            status.success = false;
            status.state = ConnectionState.DEMONSTRATION_MODE;
            status.sanitizedDiagnosis = "Operating in isolated in-memory demonstration sandbox. Changes are not persisted to MySQL.";
            cachedStatus = status;
            lastCheckTime = now;
            return status;
        }

        // Fast TCP probe to distinguish service offline vs authentication / schema errors
        boolean portReachable = isPortReachable(cfg.getHost(), cfg.getPort(), 1500);
        if (!portReachable) {
            status.success = false;
            status.state = ConnectionState.DISCONNECTED;
            status.sanitizedDiagnosis = "Cannot connect to MySQL server at " + cfg.getHost() + ":" + cfg.getPort() +
                    ". Verify the MySQL Windows service is running.";
            cachedStatus = status;
            lastCheckTime = now;
            return status;
        }

        // If port is reachable but password is not configured and username is empty
        if (!cfg.hasUsername()) {
            status.success = false;
            status.state = ConnectionState.AUTH_FAILED;
            status.sanitizedDiagnosis = "Authentication Failed: Database username is missing. Please set 'db.user' in resources/db.properties.";
            cachedStatus = status;
            lastCheckTime = now;
            return status;
        }

        long start = System.currentTimeMillis();

        // 1. Probe MySQL Server connectivity (server-only URL without target schema)
        try (Connection serverConn = getServerConnection()) {
            DatabaseMetaData meta = serverConn.getMetaData();
            status.driverName = meta.getDriverName();
            status.driverVersion = meta.getDriverVersion();
            status.databaseProductName = meta.getDatabaseProductName();
            status.databaseProductVersion = meta.getDatabaseProductVersion();
        } catch (SQLException e) {
            status.success = false;
            status.latencyMs = System.currentTimeMillis() - start;
            status.errorMessage = e.getMessage();

            int code = e.getErrorCode();
            String sqlState = e.getSQLState();
            String msg = (e.getMessage() != null) ? e.getMessage() : "";

            if (code == 1045 || "28000".equals(sqlState) || msg.contains("Access denied")) {
                status.state = ConnectionState.AUTH_FAILED;
                if (!cfg.hasPassword()) {
                    status.sanitizedDiagnosis = "Authentication Failed: 'db.password' is not configured in resources/db.properties (MySQL reports: using password: NO). Set your MySQL root or application password, or set the DB_PASSWORD environment variable.";
                } else {
                    status.sanitizedDiagnosis = "Authentication Failed: Access denied for user '" + cfg.getUsername() + "' with configured password. Verify credentials in resources/db.properties or environment.";
                }
            } else if ((sqlState != null && sqlState.startsWith("08")) ||
                    msg.contains("Communications link failure") ||
                    msg.contains("Connection refused") ||
                    msg.contains("ConnectException")) {
                status.state = ConnectionState.DISCONNECTED;
                status.sanitizedDiagnosis = "Cannot connect to MySQL server at " + cfg.getHost() + ":" + cfg.getPort() +
                        ". Verify the MySQL Windows service is running.";
            } else {
                status.state = ConnectionState.DISCONNECTED;
                status.sanitizedDiagnosis = "Server connection failed: " + sanitizeError(msg);
            }

            cachedStatus = status;
            lastCheckTime = now;
            return status;
        } catch (Exception e) {
            status.success = false;
            status.state = ConnectionState.DISCONNECTED;
            status.errorMessage = e.getMessage();
            status.sanitizedDiagnosis = "Driver error: " + e.getClass().getSimpleName();
            cachedStatus = status;
            lastCheckTime = now;
            return status;
        }

        // 2. Probe connection to the specific target database schema
        try (Connection dbConn = getConnection()) {
            try (Statement stmt = dbConn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    status.latencyMs = System.currentTimeMillis() - start;
                }
            }

            // 3. Verify application schema completeness (Check for core tables)
            int tableCount = 0;
            String checkSql = "SELECT COUNT(*) FROM information_schema.tables " +
                              "WHERE table_schema = ? AND table_name IN ('users', 'transactions', 'budgets', " +
                              "'admin_audit_logs', 'application_settings', 'user_sessions', 'password_reset_tokens')";
            try (PreparedStatement ps = dbConn.prepareStatement(checkSql)) {
                ps.setString(1, cfg.getDatabaseName());
                try (ResultSet trs = ps.executeQuery()) {
                    if (trs.next()) {
                        tableCount = trs.getInt(1);
                    }
                }
            } catch (SQLException ignored) {
                // If information_schema query fails, perform quick fallback check on users table
                try (Statement s = dbConn.createStatement();
                     ResultSet urs = s.executeQuery("SELECT 1 FROM users LIMIT 1")) {
                    tableCount = 7;
                } catch (SQLException ex) {
                    tableCount = 0;
                }
            }

            if (tableCount < 3) {
                status.success = false;
                status.state = ConnectionState.SCHEMA_MIGRATION_REQUIRED;
                status.sanitizedDiagnosis = "Database exists, but application tables are missing. Initialization required.";
            } else {
                status.success = true;
                status.state = ConnectionState.CONNECTED;
                status.sanitizedDiagnosis = "Connected to MySQL successfully. All application tables active.";
            }
        } catch (SQLException e) {
            status.success = false;
            status.latencyMs = System.currentTimeMillis() - start;
            status.errorMessage = e.getMessage();

            int code = e.getErrorCode();
            String sqlState = e.getSQLState();
            String msg = (e.getMessage() != null) ? e.getMessage() : "";

            if (code == 1049 || "42000".equals(sqlState) || msg.contains("Unknown database")) {
                status.state = ConnectionState.DATABASE_UNAVAILABLE;
                status.sanitizedDiagnosis = "MySQL is running, but database '" + cfg.getDatabaseName() +
                        "' does not exist. Run initialization script.";
            } else if (code == 1045 || "28000".equals(sqlState)) {
                status.state = ConnectionState.AUTH_FAILED;
                status.sanitizedDiagnosis = "Access denied for database '" + cfg.getDatabaseName() + "'.";
            } else {
                status.state = ConnectionState.DATABASE_UNAVAILABLE;
                status.sanitizedDiagnosis = "Database unavailable: " + sanitizeError(msg);
            }
        }

        cachedStatus = status;
        lastCheckTime = now;
        return status;
    }

    private static String sanitizeError(String msg) {
        if (msg == null) return "Unknown database error";
        if (msg.contains("password")) {
            return msg.replaceAll("password=[^&; ]*", "password=********");
        }
        return msg;
    }

    /**
     * Safely closes one or more JDBC resources without throwing exceptions.
     */
    public static void closeQuietly(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
