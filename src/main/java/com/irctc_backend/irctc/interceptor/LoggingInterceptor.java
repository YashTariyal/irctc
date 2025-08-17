package com.irctc_backend.irctc.interceptor;

import com.irctc_backend.irctc.util.LoggingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor to log all API requests and responses
 * Provides automatic logging for performance monitoring and debugging
 */
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    
    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        String requestId = LoggingUtil.generateRequestId();
        
        // Store start time and request ID in request attributes
        request.setAttribute(START_TIME, startTime);
        request.setAttribute(REQUEST_ID, requestId);
        
        // Log request start
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);
        
        LoggingUtil.logApiRequestStart(endpoint, method, requestId);
        
        logger.debug("Request Details - IP: {}, User-Agent: {}, Headers: {}", 
                   clientIP, userAgent, getRequestHeaders(request));
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // This method is called after the handler is executed but before the view is rendered
        // We don't need to do anything here as we'll log in afterCompletion
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute(START_TIME);
        String requestId = (String) request.getAttribute(REQUEST_ID);
        int statusCode = response.getStatus();
        
        // Log request completion
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        
        LoggingUtil.logApiRequestComplete(endpoint, method, requestId, statusCode, startTime);
        
        // Log errors if any
        if (ex != null) {
            LoggingUtil.logError(method + " " + endpoint, "API", requestId, 
                               "SYSTEM", "Request failed with exception", ex);
        }
        
        // Log performance metrics for slow requests (> 1 second)
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 1000) {
            LoggingUtil.logPerformanceMetric("slow_request", duration, "ms");
            logger.warn("Slow request detected - Endpoint: {}, Method: {}, Duration: {}ms", 
                       endpoint, method, duration);
        }
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Get request headers as string (excluding sensitive headers)
     */
    private String getRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            
            // Skip sensitive headers
            if (!isSensitiveHeader(headerName)) {
                headers.append(headerName).append(": ").append(headerValue).append(", ");
            }
        }
        
        return headers.toString();
    }
    
    /**
     * Check if header is sensitive and should not be logged
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerHeaderName = headerName.toLowerCase();
        return lowerHeaderName.contains("authorization") ||
               lowerHeaderName.contains("cookie") ||
               lowerHeaderName.contains("password") ||
               lowerHeaderName.contains("token") ||
               lowerHeaderName.contains("secret");
    }
} 