package com.irctc.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Password Policy Service for IRCTC User Service
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class PasswordPolicyService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordPolicyService.class);

    @Value("${security.password.min-length:8}")
    private int minLength;

    @Value("${security.password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${security.password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${security.password.require-digits:true}")
    private boolean requireDigits;

    @Value("${security.password.require-special-chars:true}")
    private boolean requireSpecialChars;

    /**
     * Validate password against policy
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        List<String> errors = new ArrayList<>();

        // Check minimum length
        if (password.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters long");
        }

        // Check for uppercase letters
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }

        // Check for lowercase letters
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }

        // Check for digits
        if (requireDigits && !password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one digit");
        }

        // Check for special characters
        if (requireSpecialChars && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errors.add("Password must contain at least one special character");
        }

        if (!errors.isEmpty()) {
            logger.warn("Password validation failed: {}", String.join(", ", errors));
            return false;
        }

        return true;
    }

    /**
     * Get password policy requirements
     */
    public PasswordPolicy getPasswordPolicy() {
        return new PasswordPolicy(
            minLength,
            requireUppercase,
            requireLowercase,
            requireDigits,
            requireSpecialChars
        );
    }

    /**
     * Get password strength score (0-100)
     */
    public int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (0-40 points)
        if (password.length() >= minLength) {
            score += Math.min(40, (password.length() - minLength) * 2);
        }

        // Character variety score (0-60 points)
        if (password.matches(".*[A-Z].*")) score += 15; // Uppercase
        if (password.matches(".*[a-z].*")) score += 15; // Lowercase
        if (password.matches(".*\\d.*")) score += 15;   // Digits
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15; // Special chars

        return Math.min(100, score);
    }

    /**
     * Get password strength level
     */
    public PasswordStrength getPasswordStrengthLevel(String password) {
        int score = getPasswordStrength(password);
        
        if (score < 30) {
            return PasswordStrength.WEAK;
        } else if (score < 60) {
            return PasswordStrength.MEDIUM;
        } else if (score < 80) {
            return PasswordStrength.STRONG;
        } else {
            return PasswordStrength.VERY_STRONG;
        }
    }

    /**
     * Password Policy class
     */
    public static class PasswordPolicy {
        private final int minLength;
        private final boolean requireUppercase;
        private final boolean requireLowercase;
        private final boolean requireDigits;
        private final boolean requireSpecialChars;

        public PasswordPolicy(int minLength, boolean requireUppercase, boolean requireLowercase, 
                            boolean requireDigits, boolean requireSpecialChars) {
            this.minLength = minLength;
            this.requireUppercase = requireUppercase;
            this.requireLowercase = requireLowercase;
            this.requireDigits = requireDigits;
            this.requireSpecialChars = requireSpecialChars;
        }

        public int getMinLength() { return minLength; }
        public boolean isRequireUppercase() { return requireUppercase; }
        public boolean isRequireLowercase() { return requireLowercase; }
        public boolean isRequireDigits() { return requireDigits; }
        public boolean isRequireSpecialChars() { return requireSpecialChars; }
    }

    /**
     * Password Strength enum
     */
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG, VERY_STRONG
    }
}
