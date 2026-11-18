package dao;

import database.DatabaseConnection;
import model.User;
import model.UserSession;
import service.AuthenticationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for active and historical user authentication session records.
 * Provides resilient persistence with ACID operations and in-memory fallback
 * when database server is offline or when operating in explicit demonstration / test isolation mode.
 */
public class SessionDAO {
    private static final List<UserSession> memorySessions = new ArrayList<>();

    public static synchronized void resetMemoryStore() {
        memorySessions.clear();
    }

    public UserSession createSession(int userId, String userEmail, String userFullName, String role, int durationMinutes, String clientInfo) {
        User u = new User();
        u.setUserId(userId);
        u.setEmail(userEmail);
        u.setRole(role);
        String[] parts = (userFullName != null) ? userFullName.split(" ", 2) : new String[]{"User", ""};
        u.setFirstName(parts[0]);
        u.setLastName(parts.length > 1 ? parts[1] : "");

        int hours = Math.max(1, durationMinutes / 60);
        try {
            return createSession(u, clientInfo, hours);
        } catch (SQLException e) {
            return null;
        }
    }

    public UserSession findSessionById(String sessionId) {
        return findById(sessionId);
    }

    public int revokeOtherAdminSessions(String preserveId) {
        return revokeAllAdminsExcept(preserveId);
    }

    public List<UserSession> getAllSessions() {
        return getAllSessions(200);
    }

    public UserSession createSession(User user, String clientInfo, int durationHours) throws SQLException {
        if (user == null) return null;
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusHours(durationHours > 0 ? durationHours : 12);

        UserSession session = new UserSession(
            sessionId, user.getUserId(), user.getEmail(), user.getFullName(),
            user.getRole(), now, now, expires, null, UserSession.STATUS_ACTIVE,
            clientInfo != null ? clientInfo : "Desktop Application Client"
        );

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySessions) {
                memorySessions.add(session);
            }
            return session;
        }

        try {
            String sql = "INSERT INTO user_sessions (session_id, user_id, created_at, last_activity, " +
                         "expires_at, status, client_info) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, session.getSessionId());
                ps.setInt(2, session.getUserId());
                ps.setTimestamp(3, Timestamp.valueOf(session.getCreatedAt()));
                ps.setTimestamp(4, Timestamp.valueOf(session.getLastActivity()));
                ps.setTimestamp(5, Timestamp.valueOf(session.getExpiresAt()));
                ps.setString(6, session.getStatus());
                ps.setString(7, session.getClientInfo());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            synchronized (memorySessions) {
                memorySessions.add(session);
            }
            return session;
        }

        synchronized (memorySessions) {
            memorySessions.add(session);
        }
        return session;
    }

    public UserSession findById(String sessionId) {
        if (sessionId == null) return null;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySessions) {
                for (UserSession s : memorySessions) {
                    if (s.getSessionId().equals(sessionId)) {
                        return s;
                    }
                }
            }
            return null;
        }

        try {
            String sql = "SELECT s.session_id, s.user_id, u.email, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                         "u.role, s.created_at, s.last_activity, s.expires_at, s.revoked_at, s.status, s.client_info " +
                         "FROM user_sessions s LEFT JOIN users u ON s.user_id = u.user_id " +
                         "WHERE s.session_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sessionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException ignored) {}

        synchronized (memorySessions) {
            for (UserSession s : memorySessions) {
                if (s.getSessionId().equals(sessionId)) {
                    return s;
                }
            }
        }
        return null;
    }

    public boolean updateLastActivity(String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        if (AuthenticationService.isDemoMode()) {
            return updateMemoryActivity(sessionId, now);
        }

        try {
            String sql = "UPDATE user_sessions SET last_activity = ? WHERE session_id = ? AND status = 'ACTIVE'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setString(2, sessionId);
                boolean ok = ps.executeUpdate() > 0;
                if (ok) updateMemoryActivity(sessionId, now);
                return ok;
            }
        } catch (SQLException e) {
            return updateMemoryActivity(sessionId, now);
        }
    }

    private boolean updateMemoryActivity(String sessionId, LocalDateTime now) {
        synchronized (memorySessions) {
            for (UserSession s : memorySessions) {
                if (s.getSessionId().equals(sessionId) && s.isActive()) {
                    s.setLastActivity(now);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean revokeSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();

        if (AuthenticationService.isDemoMode()) {
            return revokeInMemory(sessionId, now);
        }

        try {
            String sql = "UPDATE user_sessions SET status = 'REVOKED', revoked_at = ? WHERE session_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setString(2, sessionId);
                boolean ok = ps.executeUpdate() > 0;
                revokeInMemory(sessionId, now);
                return ok;
            }
        } catch (SQLException e) {
            return revokeInMemory(sessionId, now);
        }
    }

    private boolean revokeInMemory(String sessionId, LocalDateTime now) {
        synchronized (memorySessions) {
            for (UserSession s : memorySessions) {
                if (s.getSessionId().equals(sessionId)) {
                    s.setStatus(UserSession.STATUS_REVOKED);
                    s.setRevokedAt(now);
                    return true;
                }
            }
        }
        return false;
    }

    public int revokeAllForUser(int userId) {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySessions) {
                for (UserSession s : memorySessions) {
                    if (s.getUserId() == userId && s.isActive()) {
                        s.setStatus(UserSession.STATUS_REVOKED);
                        s.setRevokedAt(now);
                        count++;
                    }
                }
            }
            return count;
        }

        try {
            String sql = "UPDATE user_sessions SET status = 'REVOKED', revoked_at = ? WHERE user_id = ? AND status = 'ACTIVE'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setInt(2, userId);
                count = ps.executeUpdate();
            }
        } catch (SQLException ignored) {}

        synchronized (memorySessions) {
            for (UserSession s : memorySessions) {
                if (s.getUserId() == userId && s.isActive()) {
                    s.setStatus(UserSession.STATUS_REVOKED);
                    s.setRevokedAt(now);
                    count++;
                }
            }
        }
        return count;
    }

    public int revokeAllAdminsExcept(String keepSessionId) {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySessions) {
                for (UserSession s : memorySessions) {
                    if (User.ROLE_ADMIN.equalsIgnoreCase(s.getRole()) &&
                        !s.getSessionId().equals(keepSessionId) && s.isActive()) {
                        s.setStatus(UserSession.STATUS_REVOKED);
                        s.setRevokedAt(now);
                        count++;
                    }
                }
            }
            return count;
        }

        try {
            String sql = "UPDATE user_sessions s JOIN users u ON s.user_id = u.user_id " +
                         "SET s.status = 'REVOKED', s.revoked_at = ? " +
                         "WHERE u.role = 'ADMIN' AND s.session_id <> ? AND s.status = 'ACTIVE'";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setString(2, keepSessionId != null ? keepSessionId : "");
                count = ps.executeUpdate();
            }
        } catch (SQLException ignored) {}

        synchronized (memorySessions) {
            for (UserSession s : memorySessions) {
                if (User.ROLE_ADMIN.equalsIgnoreCase(s.getRole()) &&
                    !s.getSessionId().equals(keepSessionId) && s.isActive()) {
                    s.setStatus(UserSession.STATUS_REVOKED);
                    s.setRevokedAt(now);
                    count++;
                }
            }
        }
        return count;
    }

    public List<UserSession> getAllSessions(int limit) {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memorySessions) {
                List<UserSession> copy = new ArrayList<>(memorySessions);
                Collections.reverse(copy);
                return copy.subList(0, Math.min(limit, copy.size()));
            }
        }

        List<UserSession> list = new ArrayList<>();
        try {
            String sql = "SELECT s.session_id, s.user_id, u.email, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                         "u.role, s.created_at, s.last_activity, s.expires_at, s.revoked_at, s.status, s.client_info " +
                         "FROM user_sessions s LEFT JOIN users u ON s.user_id = u.user_id " +
                         "ORDER BY s.last_activity DESC LIMIT ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limit > 0 ? limit : 100);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    if (!list.isEmpty()) return list;
                }
            }
        } catch (SQLException ignored) {}

        synchronized (memorySessions) {
            List<UserSession> copy = new ArrayList<>(memorySessions);
            Collections.reverse(copy);
            return copy.subList(0, Math.min(limit, copy.size()));
        }
    }

    public List<UserSession> getActiveSessions() {
        List<UserSession> all = getAllSessions(200);
        List<UserSession> active = new ArrayList<>();
        for (UserSession s : all) {
            if (s.isActive()) active.add(s);
        }
        return active;
    }

    private UserSession mapRow(ResultSet rs) throws SQLException {
        UserSession s = new UserSession();
        s.setSessionId(rs.getString("session_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setUserEmail(rs.getString("email"));
        s.setUserFullName(rs.getString("full_name"));
        s.setRole(rs.getString("role"));

        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) s.setCreatedAt(c.toLocalDateTime());

        Timestamp la = rs.getTimestamp("last_activity");
        if (la != null) s.setLastActivity(la.toLocalDateTime());

        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) s.setExpiresAt(exp.toLocalDateTime());

        Timestamp rev = rs.getTimestamp("revoked_at");
        if (rev != null) s.setRevokedAt(rev.toLocalDateTime());

        s.setStatus(rs.getString("status"));
        s.setClientInfo(rs.getString("client_info"));

        // Evaluate expiry dynamically if status is ACTIVE
        if (UserSession.STATUS_ACTIVE.equalsIgnoreCase(s.getStatus()) && s.getExpiresAt() != null &&
            LocalDateTime.now().isAfter(s.getExpiresAt())) {
            s.setStatus(UserSession.STATUS_EXPIRED);
        }

        return s;
    }
}
