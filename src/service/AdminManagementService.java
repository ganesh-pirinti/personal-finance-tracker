package service;

import dao.UserDAO;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing user role delegation, account suspension, and admin security boundaries.
 * Enforces strict authorization, session validation, and protection against removing the last active administrator.
 */
public class AdminManagementService {
    private final UserDAO userDAO;
    private final AuditLogService auditLogService;

    public AdminManagementService() {
        this.userDAO = new UserDAO();
        this.auditLogService = new AuditLogService();
    }

    public AdminManagementService(UserDAO userDAO, AuditLogService auditLogService) {
        this.userDAO = userDAO;
        this.auditLogService = auditLogService;
    }

    private void checkAdminAccess(User actor) {
        if (actor == null || !actor.isAdmin()) {
            throw new SecurityException("Unauthorized: Administrator privileges are strictly required for this operation.");
        }
        if (actor.isSuspended()) {
            throw new SecurityException("Unauthorized: Your administrator account is currently suspended.");
        }
        // Enforce active session validation if desktop session tracking is active
        if (SessionService.getCurrentSessionId() != null && !SessionService.getInstance().validateCurrentSession()) {
            throw new SecurityException("Unauthorized: Administrator session has been revoked or expired. Operation denied.");
        }
    }

    public List<User> getAllUsers(User actor) throws SQLException {
        checkAdminAccess(actor);
        return userDAO.findAllUsers();
    }

    public List<User> searchAndFilterUsers(User actor, String query, String roleFilter, String statusFilter) throws SQLException {
        checkAdminAccess(actor);
        List<User> all = userDAO.findAllUsers();
        List<User> filtered = new ArrayList<>();

        String q = (query != null) ? query.trim().toLowerCase() : "";
        boolean filterRole = roleFilter != null && !roleFilter.equalsIgnoreCase("ALL");
        boolean filterStatus = statusFilter != null && !statusFilter.equalsIgnoreCase("ALL");

        for (User u : all) {
            boolean matchesQuery = q.isEmpty() ||
                    u.getFullName().toLowerCase().contains(q) ||
                    u.getEmail().toLowerCase().contains(q) ||
                    String.valueOf(u.getUserId()).equals(q);

            boolean matchesRole = !filterRole || u.getRole().equalsIgnoreCase(roleFilter);
            boolean matchesStatus = !filterStatus || u.getAccountStatus().equalsIgnoreCase(statusFilter);

            if (matchesQuery && matchesRole && matchesStatus) {
                filtered.add(u);
            }
        }
        return filtered;
    }

    public boolean changeUserRole(User actor, int targetUserId, String newRole) throws SQLException {
        checkAdminAccess(actor);
        String normalizedRole = (newRole != null) ? newRole.toUpperCase() : User.ROLE_USER;

        User target = userDAO.findById(targetUserId);
        if (target == null) {
            throw new IllegalArgumentException("User with ID #" + targetUserId + " does not exist.");
        }

        // Safety Guard: Cannot demote the last active admin
        if (target.isAdmin() && User.ROLE_USER.equalsIgnoreCase(normalizedRole)) {
            int activeAdmins = userDAO.countAdmins();
            if (activeAdmins <= 1) {
                auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_ROLE_CHANGE",
                        "USER", String.valueOf(targetUserId), "BLOCKED",
                        "Blocked attempt to demote the last active administrator (" + target.getEmail() + ")");
                throw new IllegalStateException("Operation rejected: You cannot demote the last active administrator. The system must retain at least one active admin.");
            }
        }

        boolean ok = userDAO.updateRole(targetUserId, normalizedRole);
        if (ok) {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_ROLE_CHANGE",
                    "USER", String.valueOf(targetUserId), "SUCCESS",
                    "Changed role of user " + target.getEmail() + " from " + target.getRole() + " to " + normalizedRole);
        } else {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_ROLE_CHANGE",
                    "USER", String.valueOf(targetUserId), "FAILURE",
                    "Failed to change role of user " + target.getEmail() + " to " + normalizedRole);
        }
        return ok;
    }

    public boolean changeUserStatus(User actor, int targetUserId, String newStatus, String reason) throws SQLException {
        checkAdminAccess(actor);
        String normalizedStatus = (newStatus != null) ? newStatus.toUpperCase() : User.STATUS_ACTIVE;

        User target = userDAO.findById(targetUserId);
        if (target == null) {
            throw new IllegalArgumentException("User with ID #" + targetUserId + " does not exist.");
        }

        // Safety Guard: Cannot suspend the last active admin
        if (target.isAdmin() && User.STATUS_SUSPENDED.equalsIgnoreCase(normalizedStatus)) {
            int activeAdmins = userDAO.countAdmins();
            if (activeAdmins <= 1) {
                auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_STATUS_CHANGE",
                        "USER", String.valueOf(targetUserId), "BLOCKED",
                        "Blocked attempt to suspend the last active administrator (" + target.getEmail() + ")");
                throw new IllegalStateException("Operation rejected: You cannot suspend the last active administrator.");
            }
        }

        boolean ok = userDAO.updateAccountStatus(targetUserId, normalizedStatus);
        String desc = (reason != null && !reason.trim().isEmpty()) ? " (" + reason.trim() + ")" : "";
        if (ok) {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_STATUS_CHANGE",
                    "USER", String.valueOf(targetUserId), "SUCCESS",
                    "Changed status of user " + target.getEmail() + " from " + target.getAccountStatus() + " to " + normalizedStatus + desc);
        } else {
            auditLogService.log(actor.getUserId(), actor.getEmail(), "USER_STATUS_CHANGE",
                    "USER", String.valueOf(targetUserId), "FAILURE",
                    "Failed to change status of user " + target.getEmail() + " to " + normalizedStatus);
        }
        return ok;
    }

    /**
     * Resolves the configured administrator bootstrap token.
     * Looks up environment variable ADMIN_BOOTSTRAP_TOKEN, or system property ADMIN_BOOTSTRAP_TOKEN.
     * Returns null if unconfigured (zero hardcoded fallback secrets).
     */
    public static String getBootstrapTokenConfigured() {
        String token = System.getenv("ADMIN_BOOTSTRAP_TOKEN");
        if (token == null || token.trim().isEmpty()) {
            token = System.getProperty("ADMIN_BOOTSTRAP_TOKEN");
        }
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return token.trim();
    }

    /**
     * Checks if administrator bootstrap procedure is currently available.
     * Available ONLY when no active administrator exists in the database
     * AND the ADMIN_BOOTSTRAP_TOKEN environment variable is explicitly configured.
     */
    public boolean isBootstrapAvailable() {
        String token = getBootstrapTokenConfigured();
        if (token == null) {
            return false;
        }
        try {
            return userDAO.countAdmins() == 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Controlled one-time initial administrator bootstrap procedure.
     * Accessible ONLY when no active administrator exists in the database
     * AND the ADMIN_BOOTSTRAP_TOKEN environment variable is explicitly configured.
     * Permanently disabled once the first administrator is established.
     */
    public User bootstrapFirstAdmin(String email, String secretToken) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter the email address to promote.");
        }
        if (secretToken == null || secretToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter the administrator setup secret key.");
        }

        // 1. One-time restriction: Check if an active administrator already exists
        int existingAdmins = userDAO.countAdmins();
        if (existingAdmins > 0) {
            auditLogService.log(0, email, "BOOTSTRAP_ADMIN", "SYSTEM", email, "BLOCKED",
                    "Bootstrap attempt blocked: an active administrator already exists. Setup wizard is permanently disabled.");
            throw new IllegalStateException("Initial administrator setup has already been completed. Further role modifications require an authenticated administrator.");
        }

        // 2. Environment variable check: strictly require ADMIN_BOOTSTRAP_TOKEN (no hardcoded fallback)
        String expectedToken = getBootstrapTokenConfigured();
        if (expectedToken == null) {
            auditLogService.log(0, email, "BOOTSTRAP_ADMIN", "SYSTEM", email, "BLOCKED",
                    "Administrator bootstrap attempted while ADMIN_BOOTSTRAP_TOKEN environment variable is not configured.");
            throw new SecurityException("Administrator bootstrap is disabled because the ADMIN_BOOTSTRAP_TOKEN environment variable is not configured.");
        }

        // 3. Constant-time comparison to prevent timing attacks
        byte[] expectedBytes = expectedToken.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = secretToken.trim().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expectedBytes, providedBytes)) {
            auditLogService.log(0, email, "BOOTSTRAP_ADMIN", "SYSTEM", email, "FAILURE",
                    "Invalid bootstrap token attempt for email " + email);
            throw new SecurityException("Invalid setup token. Bootstrap authorization denied.");
        }

        User target = userDAO.findByEmail(email.trim());
        if (target == null) {
            throw new IllegalArgumentException("No existing account found with email " + email + ". Please register first.");
        }

        userDAO.updateRole(target.getUserId(), User.ROLE_ADMIN);
        userDAO.updateAccountStatus(target.getUserId(), User.STATUS_ACTIVE);
        target.setRole(User.ROLE_ADMIN);
        target.setAccountStatus(User.STATUS_ACTIVE);

        auditLogService.log(target.getUserId(), email, "BOOTSTRAP_ADMIN", "USER",
                String.valueOf(target.getUserId()), "SUCCESS",
                "Promoted user " + email + " to ADMIN via one-time bootstrap setup token.");

        return target;
    }
}
