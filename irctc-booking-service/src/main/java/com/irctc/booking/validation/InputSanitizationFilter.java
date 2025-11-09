package com.irctc.booking.validation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Input Sanitization Filter
 * 
 * Sanitizes request inputs to prevent XSS, SQL injection, and other security vulnerabilities
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "validation.sanitization.enabled", havingValue = "true", matchIfMissing = true)
public class InputSanitizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InputSanitizationFilter.class);
    
    // XSS patterns
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    };
    
    // SQL injection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("('|(\\-\\-)|(;)|(\\|)|(\\*)|(%))", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(union|select|insert|update|delete|drop|create|alter|exec|execute)\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(or|and)\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(or|and)\\s+['\"].*?['\"]\\s*=\\s*['\"].*?['\"]", Pattern.CASE_INSENSITIVE)
    };
    
    // Path traversal patterns
    private static final Pattern[] PATH_TRAVERSAL_PATTERNS = {
        Pattern.compile("\\.\\./"),
        Pattern.compile("\\.\\.\\\\"),
        Pattern.compile("%2e%2e%2f", Pattern.CASE_INSENSITIVE),
        Pattern.compile("%2e%2e%5c", Pattern.CASE_INSENSITIVE)
    };
    
    @Value("${validation.sanitization.block-on-violation:true}")
    private boolean blockOnViolation;
    
    @Value("${validation.sanitization.log-violations:true}")
    private boolean logViolations;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Check query parameters
        if (hasMaliciousInput(request.getQueryString())) {
            handleViolation(request, response, "Malicious input detected in query parameters");
            return;
        }
        
        // Check path parameters
        if (hasMaliciousInput(request.getRequestURI())) {
            handleViolation(request, response, "Malicious input detected in path");
            return;
        }
        
        // Check headers (except sensitive ones)
        if (hasMaliciousHeaders(request)) {
            handleViolation(request, response, "Malicious input detected in headers");
            return;
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    private boolean hasMaliciousInput(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        // Check for XSS
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                if (logViolations) {
                    logger.warn("⚠️  XSS pattern detected: {} in input: {}", pattern.pattern(), sanitizeForLog(input));
                }
                return true;
            }
        }
        
        // Check for SQL injection
        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                if (logViolations) {
                    logger.warn("⚠️  SQL injection pattern detected: {} in input: {}", pattern.pattern(), sanitizeForLog(input));
                }
                return true;
            }
        }
        
        // Check for path traversal
        for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
            if (pattern.matcher(input).find()) {
                if (logViolations) {
                    logger.warn("⚠️  Path traversal pattern detected: {} in input: {}", pattern.pattern(), sanitizeForLog(input));
                }
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasMaliciousHeaders(HttpServletRequest request) {
        // Check common headers that might contain user input
        String[] headersToCheck = {
            "X-Forwarded-For",
            "X-Real-IP",
            "User-Agent",
            "Referer"
        };
        
        for (String headerName : headersToCheck) {
            String headerValue = request.getHeader(headerName);
            if (headerValue != null && hasMaliciousInput(headerValue)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void handleViolation(HttpServletRequest request, HttpServletResponse response, 
                                String message) throws IOException {
        if (logViolations) {
            logger.error("🚫 Security violation blocked: {} - Path: {}", message, request.getRequestURI());
        }
        
        if (blockOnViolation) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"VALIDATION_ERROR\",\"message\":\"%s\",\"status\":400}",
                "Invalid input detected. Request blocked for security reasons."
            ));
            response.getWriter().flush();
        }
    }
    
    private String sanitizeForLog(String input) {
        if (input == null) {
            return null;
        }
        // Truncate long inputs for logging
        if (input.length() > 100) {
            return input.substring(0, 100) + "...";
        }
        return input;
    }
}

