package org.revhire.util;

import org.mindrot.jbcrypt.BCrypt;

// Helper for password hashing and verification
public class PasswordUtils {

    // Hash plain text password
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    // Verify plain text against hashed password
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            // Log error or handle cases where hash is invalid/malformed
            return false;
        }
    }
}
