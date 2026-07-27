package com.ecommerce.user.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class OtpGenerationServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-24T08:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void generatesSixDigitOtpWithFifteenMinuteExpiration() {
        SecureRandom random = new FixedSecureRandom(42);
        OtpGenerationService service = new OtpGenerationService(random, CLOCK);

        OtpGenerationService.OtpDetails otp = service.generate();

        assertEquals("000042", otp.code());
        assertEquals(NOW.plusSeconds(15 * 60), otp.expiresAt());
        assertFalse(otp.isExpired(CLOCK));
    }

    @Test
    void otpIsExpiredAtExpirationTime() {
        OtpGenerationService.OtpDetails otp =
                new OtpGenerationService.OtpDetails("123456", NOW);

        assertTrue(otp.isExpired(CLOCK));
    }

    @Test
    void rejectsInvalidOtpDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> new OtpGenerationService.OtpDetails("12345", NOW));
        assertThrows(IllegalArgumentException.class,
                () -> new OtpGenerationService.OtpDetails("abcdef", NOW));
        assertThrows(IllegalArgumentException.class,
                () -> new OtpGenerationService.OtpDetails("123456", null));
    }

    private static final class FixedSecureRandom extends SecureRandom {
        private final int value;

        private FixedSecureRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return value;
        }
    }
}
