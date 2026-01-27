package org.revhire.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    public void shouldValidateCorrectEmails() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.in"));
    }

    @Test
    public void shouldRejectInvalidEmails() {
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("test@"));
        assertFalse(ValidationUtils.isValidEmail("@domain.com"));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    public void shouldAcceptComplexityCompliantPasswords() {
        assertTrue(ValidationUtils.isValidPassword("P@ssw0rd123"));
        assertTrue(ValidationUtils.isValidPassword("Strong!123"));
        assertTrue(ValidationUtils.isValidPassword("Pass!123"));
    }

    @Test
    public void shouldRejectWeakPasswords() {
        assertFalse(ValidationUtils.isValidPassword("weak")); // Too short
        assertFalse(ValidationUtils.isValidPassword("password123")); // No upper/special
        assertFalse(ValidationUtils.isValidPassword("PASSWORD!123")); // No lower
        assertFalse(ValidationUtils.isValidPassword("Short1!")); // Length 7
    }
}
