package model;

import java.time.LocalDateTime;

/**
 * Model representing a time-limited, single-use password recovery token record.
 * Stored securely as a cryptographic hash in MySQL.
 */
public class PasswordResetToken {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REVOKED = "REVOKED";

    private int tokenId;
    private int userId;
    private String userEmail;
    private String userFullName;
    private String tokenHash;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime revokedAt;
    private String clientIp;

    public PasswordResetToken() {
        this.createdAt = LocalDateTime.now();
    }

    public PasswordResetToken(int tokenId, int userId, String userEmail, String userFullName,
                              String tokenHash, LocalDateTime createdAt, LocalDateTime expiresAt,
                              LocalDateTime usedAt, LocalDateTime revokedAt, String clientIp) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userFullName = userFullName;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.revokedAt = revokedAt;
        this.clientIp = clientIp != null ? clientIp : "Desktop Client";
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isUsed() && !isRevoked();
    }

    public boolean isPending() {
        return isValid();
    }

    public String getStatus() {
        return getDerivedStatus();
    }

    public String getMaskedTokenHash() {
        if (tokenHash == null) return "None";
        if (tokenHash.length() <= 12) return tokenHash;
        return tokenHash.substring(0, 12) + "••••••••";
    }

    public String getDerivedStatus() {
        if (isUsed()) return STATUS_USED;
        if (isRevoked()) return STATUS_REVOKED;
        if (isExpired()) return STATUS_EXPIRED;
        return STATUS_PENDING;
    }

    public int getTokenId() { return tokenId; }
    public void setTokenId(int tokenId) { this.tokenId = tokenId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + tokenId +
                ", user=" + userEmail +
                ", status=" + getDerivedStatus() +
                ", expires=" + expiresAt +
                '}';
    }
}
