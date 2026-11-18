package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Encapsulates user entity attributes, security role, and account lifecycle state.
 */
public class User {
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private String passwordSalt;
    private String mobileNumber;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;

    // Security & Lifecycle
    private String role = ROLE_USER;
    private String accountStatus = STATUS_ACTIVE;
    private LocalDateTime lastLogin;

    public User() {}

    public User(int userId, String firstName, String lastName, String email,
                String passwordHash, String passwordSalt, String mobileNumber,
                LocalDate dateOfBirth, LocalDateTime createdAt) {
        this(userId, firstName, lastName, email, passwordHash, passwordSalt,
             mobileNumber, dateOfBirth, createdAt, ROLE_USER, STATUS_ACTIVE, null);
    }

    public User(int userId, String firstName, String lastName, String email,
                String passwordHash, String passwordSalt, String mobileNumber,
                LocalDate dateOfBirth, LocalDateTime createdAt,
                String role, String accountStatus, LocalDateTime lastLogin) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.mobileNumber = mobileNumber;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = createdAt;
        this.role = (role != null) ? role.toUpperCase() : ROLE_USER;
        this.accountStatus = (accountStatus != null) ? accountStatus.toUpperCase() : STATUS_ACTIVE;
        this.lastLogin = lastLogin;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        if (firstName == null && lastName == null) return "User";
        if (lastName == null || lastName.trim().isEmpty()) return firstName;
        return firstName + " " + lastName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getRole() { return role; }
    public void setRole(String role) {
        this.role = (role != null) ? role.toUpperCase() : ROLE_USER;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = (accountStatus != null) ? accountStatus.toUpperCase() : STATUS_ACTIVE;
    }

    public boolean isSuspended() {
        return STATUS_SUSPENDED.equalsIgnoreCase(this.accountStatus);
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(this.accountStatus);
    }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", status='" + accountStatus + '\'' +
                '}';
    }
}
