package com.irctc.booking.validation;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Sanitization Utilities
 * 
 * Provides methods for sanitizing user input
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class SanitizationUtils {

    // HTML tags pattern
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    
    // Script pattern
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // Style pattern
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>.*?</style>", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    /**
     * Sanitize HTML input by removing HTML tags
     */
    public static String sanitizeHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove script tags
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        
        // Remove style tags
        sanitized = STYLE_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove all HTML tags
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        
        // Decode HTML entities
        sanitized = sanitized.replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&amp;", "&")
                            .replace("&quot;", "\"")
                            .replace("&#39;", "'");
        
        return sanitized.trim();
    }
    
    /**
     * Sanitize SQL input by escaping special characters
     */
    public static String sanitizeSql(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Escape single quotes
        String sanitized = input.replace("'", "''");
        
        // Remove SQL comment patterns
        sanitized = sanitized.replace("--", "");
        sanitized = sanitized.replace("/*", "");
        sanitized = sanitized.replace("*/", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize file path to prevent path traversal
     */
    public static String sanitizePath(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove path traversal patterns
        String sanitized = input.replace("../", "")
                               .replace("..\\", "")
                               .replace("%2e%2e%2f", "")
                               .replace("%2e%2e%5c", "");
        
        // Remove null bytes
        sanitized = sanitized.replace("\0", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize email input
     */
    public static String sanitizeEmail(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove whitespace
        String sanitized = input.trim().toLowerCase();
        
        // Remove any HTML tags
        sanitized = sanitizeHtml(sanitized);
        
        return sanitized;
    }
    
    /**
     * Sanitize phone number input
     */
    public static String sanitizePhone(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove all non-digit characters except +
        String sanitized = input.replaceAll("[^0-9+]", "");
        
        return sanitized;
    }
    
    /**
     * Sanitize general text input
     */
    public static String sanitizeText(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove HTML tags
        String sanitized = sanitizeHtml(input);
        
        // Remove control characters
        sanitized = sanitized.replaceAll("[\\p{Cntrl}]", "");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }
    
    /**
     * Sanitize numeric input
     */
    public static String sanitizeNumeric(String input) {
        if (!StringUtils.hasText(input)) {
            return input;
        }
        
        // Remove all non-numeric characters except decimal point and minus sign
        String sanitized = input.replaceAll("[^0-9.\\-]", "");
        
        return sanitized;
    }
}

