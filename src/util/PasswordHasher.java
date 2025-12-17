package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Hashes a plain text password using a cryptographic algorithm (SHA-256).
     * @param password The plain text password.
     * @return The base64-encoded hash string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes());
            // Encode the byte array to a Base64 string for easy database storage
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing algorithm not found: " + ALGORITHM);
            // In a real application, this should never happen with standard algorithms
            throw new RuntimeException("Security error during password hashing.", e);
        }
    }

    /**
     * Verifies a plain text password against a stored hash.
     * @param plainPassword The password entered by the user.
     * @param storedHash The hash retrieved from the database.
     * @return True if the passwords match after hashing, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        // Hash the input password and compare it to the stored hash
        String newHash = hashPassword(plainPassword);
        return newHash.equals(storedHash);
    }
}