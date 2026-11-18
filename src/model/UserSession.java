package model;

import java.time.LocalDateTime;

/**
 * Encapsulates an active or historical user authentication session record.
 */
public class UserSession {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REVOKED = "REVOKED";

    private String sessionId;
    private int userId;
    private String userEmail;
    private String userFullName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private String status;
    private String clientInfo;

    public UserSession() {
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.status = STATUS_ACTIVE;
    }

    public UserSession(String sessionId, int userId, String userEmail, String userFullName,
                       String role, LocalDateTime createdAt, LocalDateTime lastActivity,
                       LocalDateTime expiresAt, LocalDateTime revokedAt, String status, String clientInfo) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.role = role != null ? role.toUpperCase() : User.ROLE_USER;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastActivity = lastActivity != null ? lastActivity : LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
        this.status = status != null ? status.toUpperCase() : STATUS_ACTIVE;
        this.clientInfo = clientInfo != null ? clientInfo : "Desktop Application Client";
    }

    public String getMaskedSessionId() {
        if (sessionId == null) return "Unknown";
        if (sessionId.length() <= 8) return sessionId;
        return sessionId.substring(0, 8) + "••••••••";
    }

    public boolean isActive() {
        if (!STATUS_ACTIVE.equalsIgnoreCase(status)) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            return false;
        }
        return true;
    }

    public boolean isExpired() {
        if (STATUS_EXPIRED.equalsIgnoreCase(status)) return true;
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return STATUS_REVOKED.equalsIgnoreCase(status);
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }

    @Override
    public String toString() {
        return "UserSession{" +
                "id='" + sessionId + '\'' +
                ", user='" + userEmail + '\'' +
                ", status='" + status + '\'' +
                ", expires=" + expiresAt +
                '}';
    }
}
