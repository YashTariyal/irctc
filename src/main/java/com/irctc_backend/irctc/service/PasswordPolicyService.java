package com.irctc_backend.irctc.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password Policy Service for enforcing strong password requirements
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class PasswordPolicyService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordPolicyService.class);
    
    // Password policy constants
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final int MIN_UPPERCASE = 1;
    private static final int MIN_LOWERCASE = 1;
    private static final int MIN_DIGITS = 1;
    private static final int MIN_SPECIAL_CHARS = 1;
    
    // Common weak passwords
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "123456789", "qwerty", "abc123", "password123",
        "admin", "letmein", "welcome", "monkey", "1234567890", "password1",
        "qwerty123", "dragon", "master", "hello", "freedom", "whatever",
        "qazwsx", "trustno1", "jordan23", "harley", "ranger", "jordan",
        "hunter", "fuck", "hockey", "killer", "george", "sexy", "andrew",
        "charlie", "superman", "asshole", "fuckyou", "dallas", "jessica",
        "panties", "pepper", "1234", "696969", "killer", "trustno1"
    };
    
    // Regex patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern REPEATING_CHARS_PATTERN = Pattern.compile("(.)\\1{2,}");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile("(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)");
    
    /**
     * Validate password against security policy
     */
    public PasswordValidationResult validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new PasswordValidationResult(false, errors, warnings);
        }
        
        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            errors.add(String.format("Password must be at least %d characters long", MIN_LENGTH));
        }
        
        // Check maximum length
        if (password.length() > MAX_LENGTH) {
            errors.add(String.format("Password must not exceed %d characters", MAX_LENGTH));
        }
        
        // Check for uppercase letters
        if (countMatches(password, UPPERCASE_PATTERN) < MIN_UPPERCASE) {
            errors.add(String.format("Password must contain at least %d uppercase letter(s)", MIN_UPPERCASE));
        }
        
        // Check for lowercase letters
        if (countMatches(password, LOWERCASE_PATTERN) < MIN_LOWERCASE) {
            errors.add(String.format("Password must contain at least %d lowercase letter(s)", MIN_LOWERCASE));
        }
        
        // Check for digits
        if (countMatches(password, DIGIT_PATTERN) < MIN_DIGITS) {
            errors.add(String.format("Password must contain at least %d digit(s)", MIN_DIGITS));
        }
        
        // Check for special characters
        if (countMatches(password, SPECIAL_CHAR_PATTERN) < MIN_SPECIAL_CHARS) {
            errors.add(String.format("Password must contain at least %d special character(s)", MIN_SPECIAL_CHARS));
        }
        
        // Check for common passwords
        if (isCommonPassword(password)) {
            errors.add("Password is too common and easily guessable");
        }
        
        // Check for repeating characters
        if (REPEATING_CHARS_PATTERN.matcher(password).find()) {
            warnings.add("Password contains repeating characters which may reduce security");
        }
        
        // Check for sequential patterns
        if (SEQUENTIAL_PATTERN.matcher(password.toLowerCase()).find()) {
            warnings.add("Password contains sequential patterns which may reduce security");
        }
        
        // Check for personal information patterns (basic check)
        if (containsPersonalInfo(password)) {
            warnings.add("Password should not contain personal information");
        }
        
        boolean isValid = errors.isEmpty();
        
        if (isValid) {
            logger.info("Password validation successful");
        } else {
            logger.warn("Password validation failed: {}", errors);
        }
        
        return new PasswordValidationResult(isValid, errors, warnings);
    }
    
    /**
     * Generate a secure password suggestion
     */
    public String generateSecurePassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(uppercase.charAt((int) (Math.random() * uppercase.length())));
        password.append(lowercase.charAt((int) (Math.random() * lowercase.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));
        
        // Fill remaining length with random characters
        String allChars = uppercase + lowercase + digits + special;
        for (int i = 4; i < 12; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    /**
     * Check if password is common/weak
     */
    private boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if password contains personal information patterns
     */
    private boolean containsPersonalInfo(String password) {
        // Basic patterns that might indicate personal information
        String[] personalPatterns = {
            "name", "email", "phone", "birth", "date", "year", "month", "day"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String pattern : personalPatterns) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Count matches for a regex pattern
     */
    private int countMatches(String input, Pattern pattern) {
        return (int) pattern.matcher(input).results().count();
    }
    
    /**
     * Shuffle a string
     */
    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = (int) (Math.random() * characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }
    
    /**
     * Password validation result
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public PasswordValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
}
