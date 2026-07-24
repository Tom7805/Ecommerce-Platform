package com.ecommerce.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordService {

    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,}$"
    );

    private final BCryptPasswordEncoder passwordEncoder;

    public PasswordService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword != null
                && hashedPassword != null
                && passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public boolean isStrong(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }

    public void validateStrength(String password) {
        if (!isStrong(password)) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters, one uppercase letter, "
                            + "one lowercase letter, one number and one symbol"
            );
        }
    }

    public boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    public void validatePasswordMatch(String password, String confirmPassword) {
        if (!passwordsMatch(password, confirmPassword)) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
    }
}
