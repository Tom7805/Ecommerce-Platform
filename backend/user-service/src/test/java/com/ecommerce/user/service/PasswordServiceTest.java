package com.ecommerce.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(new BCryptPasswordEncoder(4));
    }

    @Test
    void hashPasswordUsesBCryptAndDoesNotStorePlainText() {
        String rawPassword = "Valid@123";

        String hash = passwordService.hashPassword(rawPassword);

        assertNotEquals(rawPassword, hash);
        assertTrue(hash.startsWith("$2"));
        assertTrue(passwordService.matches(rawPassword, hash));
        assertFalse(passwordService.matches("Wrong@123", hash));
    }

    @Test
    void hashPasswordRejectsNullAndBlankValues() {
        assertThrows(IllegalArgumentException.class, () -> passwordService.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> passwordService.hashPassword("  "));
    }

    @Test
    void matchesReturnsFalseForNullValues() {
        assertFalse(passwordService.matches(null, "hash"));
        assertFalse(passwordService.matches("Valid@123", null));
    }

    @Test
    void acceptsPasswordContainingEveryRequiredCharacterType() {
        assertTrue(passwordService.isStrong("Valid@123"));
        assertDoesNotThrow(() -> passwordService.validateStrength("Valid@123"));
    }

    @Test
    void rejectsPasswordsThatDoNotMeetStrengthRequirements() {
        assertFalse(passwordService.isStrong(null));
        assertFalse(passwordService.isStrong("Short@1"));
        assertFalse(passwordService.isStrong("valid@123"));
        assertFalse(passwordService.isStrong("VALID@123"));
        assertFalse(passwordService.isStrong("ValidPass@"));
        assertFalse(passwordService.isStrong("Valid1234"));
        assertFalse(passwordService.isStrong("Valid @123"));
        assertThrows(IllegalArgumentException.class,
                () -> passwordService.validateStrength("weak"));
    }

    @Test
    void validatesPasswordConfirmation() {
        assertTrue(passwordService.passwordsMatch("Valid@123", "Valid@123"));
        assertDoesNotThrow(() ->
                passwordService.validatePasswordMatch("Valid@123", "Valid@123"));
    }

    @Test
    void rejectsMismatchedOrNullPasswordConfirmation() {
        assertFalse(passwordService.passwordsMatch("Valid@123", "Different@123"));
        assertFalse(passwordService.passwordsMatch(null, null));
        assertThrows(IllegalArgumentException.class,
                () -> passwordService.validatePasswordMatch("Valid@123", "Different@123"));
    }
}
