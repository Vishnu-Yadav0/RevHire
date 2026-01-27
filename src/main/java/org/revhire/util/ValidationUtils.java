package org.revhire.util;

import java.util.regex.Pattern;

// Utility for validating user input like emails and passwords
public class ValidationUtils {

    // Standard email regex
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    // Password must be 8+ chars, with upper, lower, digit, and special char
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    // Check for valid email format
    public static boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    // Check if password meets complexity rules
    public static boolean isValidPassword(String password) {
        if (password == null)
            return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
