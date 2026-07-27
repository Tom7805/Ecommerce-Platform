package com.ecommerce.user.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class OtpGenerationService {

    public static final Duration OTP_VALIDITY = Duration.ofMinutes(15);

    private final SecureRandom secureRandom;
    private final Clock clock;

    public OtpGenerationService() {
        this(new SecureRandom(), Clock.systemUTC());
    }

    OtpGenerationService(SecureRandom secureRandom, Clock clock) {
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    public OtpDetails generate() {
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        return new OtpDetails(code, clock.instant().plus(OTP_VALIDITY));
    }

    public record OtpDetails(String code, Instant expiresAt) {

        public OtpDetails {
            if (code == null || !code.matches("\\d{6}")) {
                throw new IllegalArgumentException("OTP must contain exactly 6 digits");
            }
            if (expiresAt == null) {
                throw new IllegalArgumentException("OTP expiration time is required");
            }
        }

        public boolean isExpired(Clock clock) {
            return !clock.instant().isBefore(expiresAt);
        }
    }
}
