package com.irctc.booking.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Version Interceptor
 * 
 * Adds version headers to responses and handles deprecation warnings
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiVersionInterceptor.class);
    private static final Pattern VERSION_PATTERN = Pattern.compile("/v(\\d+)/");
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String version = extractVersionFromPath(path);
        
        if (version != null) {
            // Add version header
            response.setHeader("X-API-Version", version);
            
            // Check if version is deprecated (example: v1 is deprecated)
            if ("v1".equals(version)) {
                // In production, this would be configurable
                // For now, v1 is still active
                response.setHeader("X-API-Version-Status", "active");
            } else {
                response.setHeader("X-API-Version-Status", "active");
            }
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) {
        // Additional processing if needed
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        // Cleanup if needed
    }
    
    private String extractVersionFromPath(String path) {
        if (path == null) {
            return null;
        }
        Matcher matcher = VERSION_PATTERN.matcher(path);
        if (matcher.find()) {
            return "v" + matcher.group(1);
        }
        return null;
    }
}

