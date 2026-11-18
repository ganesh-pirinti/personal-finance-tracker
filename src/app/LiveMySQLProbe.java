package app;

import database.DatabaseConnection;
import model.DatabaseConfig;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Standalone Command-Line Live MySQL Verification Probe.
 * Tests configuration loading, network socket reachability, MySQL driver handshake,
 * JDBC authentication, target database existence, schema table presence, and query execution.
 *
 * Usage:
 *   java -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.LiveMySQLProbe
 */
public class LiveMySQLProbe {

    public static void main(String[] args) {
        System.out.println("====================================================================");
        System.out.println("   PERSONAL FINANCE TRACKER - LIVE JDBC MYSQL PROBE");
        System.out.println("   Direct Diagnostics & Connectivity Verification Tool");
        System.out.println("====================================================================");

        DatabaseConfig cfg = DatabaseConnection.getCurrentConfig();
        System.out.println("1. RESOLVED CONFIGURATION:");
        System.out.println("   Host:            " + cfg.getHost());
        System.out.println("   Port:            " + cfg.getPort());
        System.out.println("   Database:        " + cfg.getDatabaseName());
        System.out.println("   Username:        " + cfg.getUsername());
        System.out.println("   Password Status: " + cfg.getMaskedPassword());
        System.out.println("   Has Password:    " + cfg.hasPassword());
        System.out.println("   JDBC URL:        " + cfg.getJdbcUrl());
        System.out.println();

        System.out.println("2. NETWORK SOCKET REACHABILITY:");
        boolean socketReachable = DatabaseConnection.isPortReachable(cfg.getHost(), cfg.getPort(), 2000);
        if (socketReachable) {
            System.out.println("   [OK] TCP socket connected successfully to " + cfg.getHost() + ":" + cfg.getPort());
        } else {
            System.out.println("   [FAILED] Cannot connect TCP socket to " + cfg.getHost() + ":" + cfg.getPort());
            System.out.println("   Verify that the MySQL Windows service (e.g. MySQL2610) is running.");
            System.exit(1);
        }
        System.out.println();

        System.out.println("3. SERVER-LEVEL JDBC HANDSHAKE & AUTHENTICATION:");
        long start = System.currentTimeMillis();
        try (Connection serverConn = DatabaseConnection.getServerConnection()) {
            long latency = System.currentTimeMillis() - start;
            DatabaseMetaData meta = serverConn.getMetaData();
            System.out.println("   [OK] Connected to MySQL Server in " + latency + "ms");
            System.out.println("   Driver:          " + meta.getDriverName() + " v" + meta.getDriverVersion());
            System.out.println("   Server Product:  " + meta.getDatabaseProductName() + " v" + meta.getDatabaseProductVersion());

            try (Statement s = serverConn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("   Query Test:      SELECT 1 returned " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            long latency = System.currentTimeMillis() - start;
            System.out.println("   [FAILED] Handshake failed in " + latency + "ms");
            System.out.println("   SQLState:   " + e.getSQLState());
            System.out.println("   ErrorCode:  " + e.getErrorCode());
            System.out.println("   Message:    " + e.getMessage());

            if (e.getErrorCode() == 1045 || "28000".equals(e.getSQLState())) {
                System.out.println();
                System.out.println("   DIAGNOSTIC GUIDANCE:");
                if (!cfg.hasPassword()) {
                    System.out.println("   -> Root/user password is not configured in resources/db.properties.");
                    System.out.println("   -> MySQL rejected the connection with (using password: NO).");
                    System.out.println("   -> Set 'db.password=<your_mysql_password>' in resources/db.properties,");
                    System.out.println("      or set the DB_PASSWORD environment variable.");
                } else {
                    System.out.println("   -> Password was supplied, but MySQL rejected user '" + cfg.getUsername() + "'.");
                    System.out.println("   -> Verify the credentials in resources/db.properties.");
                }
            }
            System.exit(1);
        }
        System.out.println();

        System.out.println("4. TARGET DATABASE & APPLICATION SCHEMA VERIFICATION:");
        try (Connection dbConn = DatabaseConnection.getConnection()) {
            System.out.println("   [OK] Successfully connected to database '" + cfg.getDatabaseName() + "'");

            String checkSql = "SELECT table_name, table_rows, engine FROM information_schema.tables " +
                              "WHERE table_schema = ? ORDER BY table_name";
            try (PreparedStatement ps = dbConn.prepareStatement(checkSql)) {
                ps.setString(1, cfg.getDatabaseName());
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("   TABLE INVENTORY:");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.printf("     - %-25s Engine: %-8s Rows: %d%n",
                                rs.getString("table_name"),
                                rs.getString("engine"),
                                rs.getLong("table_rows"));
                    }
                    if (count == 0) {
                        System.out.println("     (No application tables found in schema. Run DatabaseInitializer to initialize.)");
                    } else {
                        System.out.println("   Total core tables detected: " + count);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("   [FAILED] Could not connect to target schema: " + e.getMessage());
            System.exit(1);
        }
        System.out.println();
        System.out.println("====================================================================");
        System.out.println("   [SUCCESS] Live MySQL Database Verified and Operational!");
        System.out.println("====================================================================");
    }
}
