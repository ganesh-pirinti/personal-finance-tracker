package dao;

import database.DatabaseConnection;
import model.User;
import service.AuthenticationService;
import util.PasswordHasher;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User persistence.
 * Uses PreparedStatement against MySQL with explicit demonstration mode isolation.
 * Supports role management, account status lifecycle, and last login tracking.
 */
public class UserDAO {
    private static final List<User> memoryUsers = new ArrayList<>();
    private static int memoryIdCounter = 2;

    static {
        resetMemoryStore();
    }

    public static synchronized void resetMemoryStore() {
        memoryUsers.clear();
        String salt = "c2FsdF9kZW1vXzEyMzQ1Ng==";
        String hash = PasswordHasher.hashPassword("Demo@123", salt);
        User demo = new User(1, "Alex", "Sharma", "alex@example.com", hash, salt,
                "+91 9876543210", LocalDate.of(1998, 5, 15), LocalDateTime.now(),
                User.ROLE_USER, User.STATUS_ACTIVE, LocalDateTime.now().minusHours(3));
        memoryUsers.add(demo);
        memoryIdCounter = 2;
    }

    public User create(User user) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                user.setUserId(memoryIdCounter++);
                user.setCreatedAt(LocalDateTime.now());
                if (user.getRole() == null) user.setRole(User.ROLE_USER);
                if (user.getAccountStatus() == null) user.setAccountStatus(User.STATUS_ACTIVE);
                memoryUsers.add(user);
            }
            return user;
        }

        String sql = "INSERT INTO users (first_name, last_name, email, password_hash, password_salt, " +
                     "mobile_number, date_of_birth, role, account_status, last_login) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail().trim().toLowerCase());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPasswordSalt());
            ps.setString(6, user.getMobileNumber());
            if (user.getDateOfBirth() != null) {
                ps.setDate(7, Date.valueOf(user.getDateOfBirth()));
            } else {
                ps.setNull(7, java.sql.Types.DATE);
            }
            ps.setString(8, user.getRole() != null ? user.getRole() : User.ROLE_USER);
            ps.setString(9, user.getAccountStatus() != null ? user.getAccountStatus() : User.STATUS_ACTIVE);
            if (user.getLastLogin() != null) {
                ps.setTimestamp(10, Timestamp.valueOf(user.getLastLogin()));
            } else {
                ps.setNull(10, java.sql.Types.TIMESTAMP);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
            }
            user.setCreatedAt(LocalDateTime.now());
            return user;
        }
    }

    public User findByEmail(String email) throws SQLException {
        if (email == null) return null;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getEmail().equalsIgnoreCase(email.trim())) {
                        return u;
                    }
                }
            }
            return null;
        }

        String sql = "SELECT user_id, first_name, last_name, email, password_hash, password_salt, " +
                     "mobile_number, date_of_birth, created_at, role, account_status, last_login " +
                     "FROM users WHERE LOWER(email) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getUserId() == userId) return u;
                }
            }
            return null;
        }

        String sql = "SELECT user_id, first_name, last_name, email, password_hash, password_salt, " +
                     "mobile_number, date_of_birth, created_at, role, account_status, last_login " +
                     "FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        return findAllUsers();
    }

    public List<User> findAllUsers() throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                return new ArrayList<>(memoryUsers);
            }
        }

        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, first_name, last_name, email, password_hash, password_salt, " +
                     "mobile_number, date_of_birth, created_at, role, account_status, last_login " +
                     "FROM users ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        if (email == null) return false;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getEmail().equalsIgnoreCase(email.trim())) return true;
                }
            }
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE LOWER(email) = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean updatePassword(int userId, String newHash, String newSalt) throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getUserId() == userId) {
                        u.setPasswordHash(newHash);
                        u.setPasswordSalt(newSalt);
                        return true;
                    }
                }
            }
            return false;
        }

        String sql = "UPDATE users SET password_hash = ?, password_salt = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, newSalt);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRole(int userId, String newRole) throws SQLException {
        String roleNormalized = (newRole != null) ? newRole.toUpperCase() : User.ROLE_USER;
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getUserId() == userId) {
                        u.setRole(roleNormalized);
                        return true;
                    }
                }
            }
            return false;
        }

        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleNormalized);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAccountStatus(int userId, String newStatus) throws SQLException {
        String statusNormalized = (newStatus != null) ? newStatus.toUpperCase() : User.STATUS_ACTIVE;
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getUserId() == userId) {
                        u.setAccountStatus(statusNormalized);
                        return true;
                    }
                }
            }
            return false;
        }

        String sql = "UPDATE users SET account_status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statusNormalized);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateLastLogin(int userId, LocalDateTime timestamp) throws SQLException {
        LocalDateTime time = (timestamp != null) ? timestamp : LocalDateTime.now();
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.getUserId() == userId) {
                        u.setLastLogin(time);
                        return true;
                    }
                }
            }
            return false;
        }

        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(time));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAdmins() throws SQLException {
        if (AuthenticationService.isDemoMode()) {
            int count = 0;
            synchronized (memoryUsers) {
                for (User u : memoryUsers) {
                    if (u.isAdmin() && u.isActive()) {
                        count++;
                    }
                }
            }
            return count;
        }

        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND account_status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPasswordSalt(rs.getString("password_salt"));
        user.setMobileNumber(rs.getString("mobile_number"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }

        // Support for new columns with backwards compatibility check
        try {
            String r = rs.getString("role");
            user.setRole(r != null ? r : User.ROLE_USER);
        } catch (SQLException ignored) {
            user.setRole(User.ROLE_USER);
        }

        try {
            String s = rs.getString("account_status");
            user.setAccountStatus(s != null ? s : User.STATUS_ACTIVE);
        } catch (SQLException ignored) {
            user.setAccountStatus(User.STATUS_ACTIVE);
        }

        try {
            Timestamp ll = rs.getTimestamp("last_login");
            if (ll != null) {
                user.setLastLogin(ll.toLocalDateTime());
            }
        } catch (SQLException ignored) {}

        return user;
    }
}
