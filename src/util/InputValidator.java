package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Validates user inputs across all screens and forms.
 */
public final class InputValidator {
    private InputValidator() {}

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9+\\-\\s()]{7,15}$"
    );

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return true; // Optional field
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isPositiveAmount(String amountStr) {
        if (!isNotEmpty(amountStr)) return false;
        try {
            BigDecimal bd = new BigDecimal(amountStr.trim());
            return bd.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static BigDecimal parseAmount(String amountStr) {
        if (!isPositiveAmount(amountStr)) return null;
        return new BigDecimal(amountStr.trim());
    }

    public static boolean isValidDate(String dateStr) {
        return DateUtils.isValidIsoDate(dateStr);
    }

    public static LocalDate parseDate(String dateStr) {
        return DateUtils.parseIso(dateStr);
    }

    public enum PasswordStrength {
        WEAK("Weak", ThemeColors.EXPENSE),
        MEDIUM("Medium", ThemeColors.WARNING),
        STRONG("Strong", ThemeColors.INCOME);

        private final String label;
        private final java.awt.Color color;

        PasswordStrength(String label, java.awt.Color color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public java.awt.Color getColor() { return color; }
    }

    public static PasswordStrength evaluatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        if (score >= 4) return PasswordStrength.STRONG;
        if (score >= 2) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }
}
