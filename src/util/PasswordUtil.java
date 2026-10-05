package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Bam mat khau SHA-256 (Minh: User.passwordHash).
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not supported", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword != null && hashedPassword != null && hash(rawPassword).equals(hashedPassword);
    }
}
