package org.revhire.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilsTest {

    @Test
    public void testPasswordHashingAndVerification() {
        String password = "SecretPassword123!";
        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(PasswordUtils.checkPassword(password, hash));
        assertFalse(PasswordUtils.checkPassword("WrongPassword", hash));
    }

    @Test
    public void testVerifyWithInvalidHash() {
        assertFalse(PasswordUtils.checkPassword("password", "invalid-hash-string"));
    }
}
