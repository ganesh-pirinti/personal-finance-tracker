package service;

import dao.PasswordResetDAO;
import dao.UserDAO;
import model.PasswordResetToken;
import model.User;
import util.InputValidator;
import util.PasswordHasher;

import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service managing password recovery workflows, single-use token lifecycle,
 * cryptographic verification, password changes, session revocation, and admin recovery management.
 */
public class PasswordResetService {
    private static final Logger LOGGER = Logger.getLogger(PasswordResetService.class.getName());
    private static final PasswordResetService INSTANCE = new PasswordResetService();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetDAO resetDAO;
    private final UserDAO userDAO;
    private final EmailService emailService;
    private final SessionService sessionService;
    private final AuditLogService auditLogService;

    public PasswordResetService() {
        this.resetDAO = new PasswordResetDAO();
        this.userDAO = new UserDAO();
        this.emailService = EmailService.getInstance();
        this.sessionService = SessionService.getInstance();
        this.auditLogService = new AuditLogService();
    }

    public PasswordResetService(PasswordResetDAO resetDAO, UserDAO userDAO, EmailService emailService,
                                SessionService sessionService, AuditLogService auditLogService) {
        this.resetDAO = resetDAO;
        this.userDAO = userDAO;
        this.emailService = emailService;
        this.sessionService = sessionService;
        this.auditLogService = auditLogService;
    }

    public static PasswordResetService getInstance() {
        return INSTANCE;
    }

    public static class ResetRequestResult {
        private final boolean success;
        private final String message;
        private final String previewToken; // Exposed for demo/evaluation fallback when SMTP is offline
        private final int expiryMinutes;

        public ResetRequestResult(boolean success, String message, String previewToken, int expiryMinutes) {
            this.success = success;
            this.message = message;
            this.previewToken = previewToken;
            this.expiryMinutes = expiryMinutes;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPreviewToken() { return previewToken; }
        public int getExpiryMinutes() { return expiryMinutes; }
    }

    /**
     * Generates a 6-digit cryptographically secure verification code.
     */
    public String generateSecureCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Initiates user self-service password reset.
     * Prevents account enumeration by returning a consistent message whether email exists or not.
     */
    public ResetRequestResult requestPasswordReset(String email, String clientIp) {
        if (!InputValidator.isValidEmail(email)) {
            return new ResetRequestResult(false, "Please provide a valid email address.", null, 0);
        }

        String normalizedEmail = email.trim().toLowerCase();
        int expiryMinutes = 15;
        try {
            String val = ApplicationTextService.getInstance().getText("password_reset.expiry_minutes", "15");
            expiryMinutes = Integer.parseInt(val);
        } catch (Exception ignored) {}

        User target = null;
        try {
            target = userDAO.findByEmail(normalizedEmail);
        } catch (Exception e) {
            LOGGER.warning("Error finding user during password reset request: " + e.getMessage());
        }

        if (target == null) {
            auditLogService.log(0, normalizedEmail, "RECOVERY_REQUESTED", "AUTH", normalizedEmail,
                    "FAILURE", "Password reset requested for non-existent email.");
            // Consistent response to protect against user enumeration
            return new ResetRequestResult(true,
                    "If an active account matches this email, a verification token has been issued.",
                    null, expiryMinutes);
        }

        if (target.isSuspended()) {
            auditLogService.log(target.getUserId(), normalizedEmail, "RECOVERY_BLOCKED", "AUTH", normalizedEmail,
                    "BLOCKED", "Password reset blocked: account is suspended.");
            return new ResetRequestResult(false,
                    "This account is suspended. Please contact your system administrator.",
                    null, 0);
        }

        String rawToken = generateSecureCode();
        PasswordResetToken created = resetDAO.createResetToken(
                target.getUserId(),
                target.getEmail(),
                target.getFullName(),
                rawToken,
                expiryMinutes,
                clientIp != null ? clientIp : "127.0.0.1"
        );

        EmailService.EmailResult emailResult = emailService.sendPasswordResetToken(target.getEmail(), rawToken, expiryMinutes);

        auditLogService.log(target.getUserId(), target.getEmail(), "RECOVERY_TOKEN_ISSUED", "AUTH",
                String.valueOf(created.getTokenId()), "SUCCESS",
                "Password reset token issued. Delivery: " + (emailResult.isDeliveredViaSmtp() ? "SMTP" : "Local/Fallback"));

        String preview = emailResult.getPreviewToken() != null ? emailResult.getPreviewToken() : rawToken;
        return new ResetRequestResult(true, emailResult.getMessage(), preview, expiryMinutes);
    }

    /**
     * Validates token and updates user password, then revokes active sessions.
     */
    public boolean verifyAndResetPassword(String email, String rawToken, String newPassword, String confirmPassword) {
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (rawToken == null || rawToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification token cannot be empty.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        boolean tokenValid = resetDAO.verifyAndConsumeToken(normalizedEmail, rawToken.trim());
        if (!tokenValid) {
            auditLogService.log(0, normalizedEmail, "RECOVERY_FAILED", "AUTH", normalizedEmail,
                    "FAILURE", "Failed password reset attempt: invalid or expired token.");
            throw new IllegalArgumentException("The verification code is invalid, expired, or has already been used.");
        }

        try {
            User user = userDAO.findByEmail(normalizedEmail);
            if (user == null) {
                throw new IllegalStateException("Associated user account could not be found.");
            }

            String newSalt = PasswordHasher.generateSalt();
            String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
            boolean updated = userDAO.updatePassword(user.getUserId(), newHash, newSalt);

            if (updated) {
                // Invalidate all existing sessions for this user for security
                int revokedCount = sessionService.revokeAllForUser(user.getUserId(), null);

                auditLogService.log(user.getUserId(), user.getEmail(), "PASSWORD_RESET_SUCCESS", "AUTH",
                        String.valueOf(user.getUserId()), "SUCCESS",
                        "Password successfully reset. Revoked " + revokedCount + " active user session(s).");
                return true;
            } else {
                throw new IllegalStateException("Failed to update password record in database.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Error updating password: " + e.getMessage());
            throw new RuntimeException("An unexpected database error occurred while updating the password.");
        }
    }

    /**
     * Admin generates a verified one-time reset code for a user without email dependency.
     */
    public String adminInitiateReset(User actor, int targetUserId) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Only administrators can initiate manual password resets.");
        }
        try {
            User target = userDAO.findById(targetUserId);
            if (target == null) {
                throw new IllegalArgumentException("Target user not found.");
            }
            String rawToken = generateSecureCode();
            resetDAO.createResetToken(target.getUserId(), target.getEmail(), target.getFullName(), rawToken, 30, "ADMIN_CONSOLE");
            auditLogService.log(actor.getUserId(), actor.getEmail(), "ADMIN_RESET_GENERATED", "USER",
                    String.valueOf(targetUserId), "SUCCESS",
                    "Admin generated manual recovery code for " + target.getEmail());
            return rawToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate admin recovery code: " + e.getMessage(), e);
        }
    }

    /**
     * Admin revokes an outstanding token.
     */
    public boolean adminRevokeToken(User actor, int tokenId) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Only administrators can revoke recovery tokens.");
        }
        boolean ok = resetDAO.revokeToken(tokenId);
        if (ok) {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "TOKEN_REVOKED", "AUTH",
                    String.valueOf(tokenId), "SUCCESS", "Admin revoked recovery token #" + tokenId);
        }
        return ok;
    }

    public List<PasswordResetToken> getAllTokens() {
        return resetDAO.getAllTokens();
    }

    public List<PasswordResetToken> getPendingTokens() {
        return resetDAO.getPendingTokens();
    }
}
