package dao;

import database.DatabaseConnection;
import model.PasswordResetToken;
import model.User;
import service.AuthenticationService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

/**
 * Data Access Object for single-use password recovery tokens.
 * Enforces cryptographic hashing (only SHA-256 hashes are stored in the database).
 */
public class PasswordResetDAO {
    private static final List<PasswordResetToken> memoryTokens = new ArrayList<>();
    private static int memoryTokenIdCounter = 1;

    public static synchronized void resetMemoryStore() {
        memoryTokens.clear();
        memoryTokenIdCounter = 1;
    }

    public static String hashToken(String rawToken) {
        if (rawToken == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(rawToken.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encoded) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public PasswordResetToken createResetToken(int userId, String userEmail, String userFullName, String rawToken, int expiryMinutes, String clientIp) {
        User u = new User();
        u.setUserId(userId);
        u.setEmail(userEmail);
        String[] parts = (userFullName != null) ? userFullName.split(" ", 2) : new String[]{"User", ""};
        u.setFirstName(parts[0]);
        u.setLastName(parts.length > 1 ? parts[1] : "");
        try {
            return createToken(u, rawToken, expiryMinutes, clientIp);
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean verifyAndConsumeToken(String email, String rawToken) {
        if (email == null || rawToken == null) return false;
        String hash = hashToken(rawToken);

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryTokens) {
                for (PasswordResetToken t : memoryTokens) {
                    if (t.getUserEmail() != null && t.getUserEmail().equalsIgnoreCase(email.trim()) &&
                        t.getTokenHash().equals(hash) && t.isValid()) {
                        return consumeToken(t.getTokenId());
                    }
                }
            }
            return false;
        }

        try {
            String sql = "SELECT t.token_id FROM password_reset_tokens t JOIN users u ON t.user_id = u.user_id " +
                         "WHERE LOWER(u.email) = ? AND t.token_hash = ? AND t.used_at IS NULL AND t.revoked_at IS NULL AND t.expires_at > ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email.trim().toLowerCase());
                ps.setString(2, hash);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int tokenId = rs.getInt("token_id");
                        return consumeToken(tokenId);
                    }
                }
            }
        } catch (SQLException ignored) {}

        synchronized (memoryTokens) {
            for (PasswordResetToken t : memoryTokens) {
                if (t.getUserEmail() != null && t.getUserEmail().equalsIgnoreCase(email.trim()) &&
                    t.getTokenHash().equals(hash) && t.isValid()) {
                    return consumeToken(t.getTokenId());
                }
            }
        }
        return false;
    }

    public List<PasswordResetToken> getAllTokens() {
        return getAllTokens(200);
    }

    public List<PasswordResetToken> getPendingTokens() {
        List<PasswordResetToken> all = getAllTokens(200);
        List<PasswordResetToken> pending = new ArrayList<>();
        for (PasswordResetToken t : all) {
            if (t.isPending()) pending.add(t);
        }
        return pending;
    }

    public PasswordResetToken createToken(User user, String rawToken, int expiryMinutes, String clientIp) throws SQLException {
        if (user == null || rawToken == null) return null;
        String hash = hashToken(rawToken);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusMinutes(expiryMinutes > 0 ? expiryMinutes : 15);

        PasswordResetToken token = new PasswordResetToken(
            0, user.getUserId(), user.getEmail(), user.getFullName(),
            hash, now, expires, null, null, clientIp
        );

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryTokens) {
                token.setTokenId(memoryTokenIdCounter++);
                memoryTokens.add(token);
            }
            return token;
        }

        try {
            String sql = "INSERT INTO password_reset_tokens (user_id, token_hash, created_at, expires_at, client_ip) " +
                         "VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, token.getUserId());
                ps.setString(2, token.getTokenHash());
                ps.setTimestamp(3, Timestamp.valueOf(token.getCreatedAt()));
                ps.setTimestamp(4, Timestamp.valueOf(token.getExpiresAt()));
                ps.setString(5, token.getClientIp());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        token.setTokenId(keys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            synchronized (memoryTokens) {
                token.setTokenId(memoryTokenIdCounter++);
                memoryTokens.add(token);
            }
            return token;
        }

        synchronized (memoryTokens) {
            if (token.getTokenId() == 0) token.setTokenId(memoryTokenIdCounter++);
            memoryTokens.add(token);
        }
        return token;
    }

    public PasswordResetToken findValidToken(int userId, String rawToken) {
        String hash = hashToken(rawToken);

        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryTokens) {
                for (PasswordResetToken t : memoryTokens) {
                    if (t.getUserId() == userId && t.getTokenHash().equals(hash) && t.isValid()) {
                        return t;
                    }
                }
            }
            return null;
        }

        try {
            String sql = "SELECT t.token_id, t.user_id, u.email, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                         "t.token_hash, t.created_at, t.expires_at, t.used_at, t.revoked_at, t.client_ip " +
                         "FROM password_reset_tokens t JOIN users u ON t.user_id = u.user_id " +
                         "WHERE t.user_id = ? AND t.token_hash = ? AND t.used_at IS NULL AND t.revoked_at IS NULL AND t.expires_at > ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, hash);
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException ignored) {}

        synchronized (memoryTokens) {
            for (PasswordResetToken t : memoryTokens) {
                if (t.getUserId() == userId && t.getTokenHash().equals(hash) && t.isValid()) {
                    return t;
                }
            }
        }
        return null;
    }

    public boolean consumeToken(int tokenId) {
        LocalDateTime now = LocalDateTime.now();

        if (AuthenticationService.isDemoMode()) {
            return consumeInMemory(tokenId, now);
        }

        try {
            String sql = "UPDATE password_reset_tokens SET used_at = ? WHERE token_id = ? AND used_at IS NULL AND revoked_at IS NULL";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setInt(2, tokenId);
                boolean ok = ps.executeUpdate() > 0;
                consumeInMemory(tokenId, now);
                return ok;
            }
        } catch (SQLException e) {
            return consumeInMemory(tokenId, now);
        }
    }

    private boolean consumeInMemory(int tokenId, LocalDateTime now) {
        synchronized (memoryTokens) {
            for (PasswordResetToken t : memoryTokens) {
                if (t.getTokenId() == tokenId && t.isValid()) {
                    t.setUsedAt(now);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean revokeToken(int tokenId) {
        LocalDateTime now = LocalDateTime.now();

        if (AuthenticationService.isDemoMode()) {
            return revokeInMemory(tokenId, now);
        }

        try {
            String sql = "UPDATE password_reset_tokens SET revoked_at = ? WHERE token_id = ? AND used_at IS NULL";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setInt(2, tokenId);
                boolean ok = ps.executeUpdate() > 0;
                revokeInMemory(tokenId, now);
                return ok;
            }
        } catch (SQLException e) {
            return revokeInMemory(tokenId, now);
        }
    }

    private boolean revokeInMemory(int tokenId, LocalDateTime now) {
        synchronized (memoryTokens) {
            for (PasswordResetToken t : memoryTokens) {
                if (t.getTokenId() == tokenId && !t.isUsed()) {
                    t.setRevokedAt(now);
                    return true;
                }
            }
        }
        return false;
    }

    public List<PasswordResetToken> getAllTokens(int limit) {
        if (AuthenticationService.isDemoMode()) {
            synchronized (memoryTokens) {
                List<PasswordResetToken> copy = new ArrayList<>(memoryTokens);
                Collections.reverse(copy);
                return copy.subList(0, Math.min(limit, copy.size()));
            }
        }

        List<PasswordResetToken> list = new ArrayList<>();
        try {
            String sql = "SELECT t.token_id, t.user_id, u.email, CONCAT(u.first_name, ' ', u.last_name) AS full_name, " +
                         "t.token_hash, t.created_at, t.expires_at, t.used_at, t.revoked_at, t.client_ip " +
                         "FROM password_reset_tokens t JOIN users u ON t.user_id = u.user_id " +
                         "ORDER BY t.created_at DESC LIMIT ?";
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

        synchronized (memoryTokens) {
            List<PasswordResetToken> copy = new ArrayList<>(memoryTokens);
            Collections.reverse(copy);
            return copy.subList(0, Math.min(limit, copy.size()));
        }
    }

    private PasswordResetToken mapRow(ResultSet rs) throws SQLException {
        PasswordResetToken t = new PasswordResetToken();
        t.setTokenId(rs.getInt("token_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setUserEmail(rs.getString("email"));
        t.setUserFullName(rs.getString("full_name"));
        t.setTokenHash(rs.getString("token_hash"));

        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) t.setCreatedAt(c.toLocalDateTime());

        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) t.setExpiresAt(exp.toLocalDateTime());

        Timestamp used = rs.getTimestamp("used_at");
        if (used != null) t.setUsedAt(used.toLocalDateTime());

        Timestamp rev = rs.getTimestamp("revoked_at");
        if (rev != null) t.setRevokedAt(rev.toLocalDateTime());

        t.setClientIp(rs.getString("client_ip"));
        return t;
    }
}
