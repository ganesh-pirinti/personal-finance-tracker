package service;

import dao.UserDAO;
import model.User;
import util.InputValidator;
import util.PasswordHasher;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service orchestrating user authentication, registration, password hashing, and session state.
 * Enforces account status checks, records last login timestamps, and logs authentication audit events.
 */
public class AuthenticationService {
    private final UserDAO userDAO;
    private final AuditLogService auditLogService;
    private static User currentUser = null;
    private static boolean demoMode = false;

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setDemoMode(boolean mode) {
        demoMode = mode;
    }

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.auditLogService = new AuditLogService();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.auditLogService = new AuditLogService();
    }

    public AuthenticationService(UserDAO userDAO, AuditLogService auditLogService) {
        this.userDAO = userDAO;
        this.auditLogService = auditLogService;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        if (currentUser != null) {
            SessionService.getInstance().logoutCurrentSession(currentUser);
        }
        currentUser = null;
        demoMode = false;
    }

    /**
     * Registers a new user. Always assigns role USER and status ACTIVE.
     * Throws IllegalArgumentException on validation failure, or SQLException on DB error.
     */
    public User register(String firstName, String lastName, String email,
                         String mobile, LocalDate dob, String password,
                         String confirmPassword) throws SQLException, IllegalArgumentException {
        if (!InputValidator.isNotEmpty(firstName)) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (!InputValidator.isNotEmpty(lastName)) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (InputValidator.isNotEmpty(mobile) && !InputValidator.isValidPhone(mobile)) {
            throw new IllegalArgumentException("Please provide a valid mobile number (7-15 digits).");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);

        User newUser = new User();
        newUser.setFirstName(firstName.trim());
        newUser.setLastName(lastName.trim());
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setPasswordHash(hash);
        newUser.setPasswordSalt(salt);
        newUser.setMobileNumber(InputValidator.isNotEmpty(mobile) ? mobile.trim() : null);
        newUser.setDateOfBirth(dob);
        // Strictly assign USER role and ACTIVE status for standard registrations
        newUser.setRole(User.ROLE_USER);
        newUser.setAccountStatus(User.STATUS_ACTIVE);

        User created = userDAO.create(newUser);
        auditLogService.log(created.getUserId(), created.getEmail(), "USER_REGISTER",
                "USER", String.valueOf(created.getUserId()), "SUCCESS",
                "New user registered: " + created.getEmail());
        return created;
    }

    /**
     * Authenticates user against stored records. Enforces suspension block and updates last login.
     */
    public User login(String email, String password) throws SQLException, IllegalArgumentException {
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!InputValidator.isNotEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            auditLogService.log(0, email, "LOGIN_FAILED", "AUTH", email, "FAILURE",
                    "Login failed: User not found (" + email + ")");
            throw new IllegalArgumentException("No account found with this email address.");
        }

        boolean valid = PasswordHasher.verifyPassword(password, user.getPasswordHash(), user.getPasswordSalt());
        if (!valid) {
            auditLogService.log(user.getUserId(), email, "LOGIN_FAILED", "AUTH", email, "FAILURE",
                    "Login failed: Invalid password attempt for " + email);
            throw new IllegalArgumentException("Invalid password. Please check your credentials.");
        }

        // Account status check
        if (user.isSuspended()) {
            auditLogService.log(user.getUserId(), email, "LOGIN_BLOCKED", "AUTH", email, "BLOCKED",
                    "Login blocked: Account is suspended for " + email);
            throw new IllegalStateException("Your account is currently suspended. Please contact the administrator.");
        }

        // Update last login timestamp
        LocalDateTime now = LocalDateTime.now();
        userDAO.updateLastLogin(user.getUserId(), now);
        user.setLastLogin(now);

        auditLogService.log(user.getUserId(), email, "LOGIN_SUCCESS", "AUTH",
                String.valueOf(user.getUserId()), "SUCCESS",
                "Successful login for " + email + " (Role: " + user.getRole() + ")");

        currentUser = user;
        SessionService.getInstance().createSession(user, null);
        return user;
    }

    /**
     * Changes password for currently logged-in user.
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword, String confirmNew) throws SQLException {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }
        if (!newPassword.equals(confirmNew)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        if (!PasswordHasher.verifyPassword(oldPassword, user.getPasswordHash(), user.getPasswordSalt())) {
            throw new IllegalArgumentException("Current password does not match.");
        }

        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);

        boolean updated = userDAO.updatePassword(userId, newHash, newSalt);
        if (updated && currentUser != null && currentUser.getUserId() == userId) {
            currentUser.setPasswordHash(newHash);
            currentUser.setPasswordSalt(newSalt);
            auditLogService.log(userId, user.getEmail(), "PASSWORD_CHANGE", "USER",
                    String.valueOf(userId), "SUCCESS", "User changed password.");
        }
        return updated;
    }
}
