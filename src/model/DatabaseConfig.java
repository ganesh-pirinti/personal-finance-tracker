package model;

/**
 * Encapsulates database connection properties and credentials.
 */
public class DatabaseConfig {
    private String host;
    private int port;
    private String databaseName;
    private String username;
    private String password;

    public DatabaseConfig(String host, int port, String databaseName, String username, String password) {
        this.host = (host != null && !host.trim().isEmpty()) ? host.trim() : "localhost";
        this.port = (port > 0) ? port : 3306;
        this.databaseName = (databaseName != null && !databaseName.trim().isEmpty()) ? databaseName.trim() : "personal_finance_tracker";
        this.username = (username != null) ? username.trim() : "root";
        this.password = (password != null) ? password : "";
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = (host != null && !host.trim().isEmpty()) ? host.trim() : "localhost"; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = (port > 0) ? port : 3306; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = (databaseName != null && !databaseName.trim().isEmpty()) ? databaseName.trim() : "personal_finance_tracker"; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = (username != null) ? username.trim() : "root"; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = (password != null) ? password : ""; }

    /**
     * Checks if a non-empty password is configured.
     */
    public boolean hasPassword() {
        return password != null && !password.trim().isEmpty();
    }

    /**
     * Checks if a non-empty username is configured.
     */
    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    /**
     * Returns a masked representation of the password for UI and log safety.
     * Never exposes plain text password.
     */
    public String getMaskedPassword() {
        if (!hasPassword()) {
            return "(empty / not configured)";
        }
        return "********";
    }

    public String getJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000&socketTimeout=5000";
    }

    public String getServerOnlyJdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=3000&socketTimeout=5000";
    }
}
