package com.irctc_backend.irctc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utility class for structured logging with MDC support
 * Provides methods for consistent logging across the application
 */
public class LoggingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);
    
    // MDC Keys for structured logging
    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String OPERATION = "operation";
    public static final String ENTITY_TYPE = "entityType";
    public static final String ENTITY_ID = "entityId";
    public static final String DURATION = "duration";
    public static final String STATUS = "status";
    
    /**
     * Generate a unique request ID
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * Set request context in MDC
     */
    public static void setRequestContext(String requestId, String userId, String operation) {
        MDC.put(REQUEST_ID, requestId);
        MDC.put(USER_ID, userId);
        MDC.put(OPERATION, operation);
    }
    
    /**
     * Set entity context in MDC
     */
    public static void setEntityContext(String entityType, String entityId) {
        MDC.put(ENTITY_TYPE, entityType);
        MDC.put(ENTITY_ID, entityId);
    }
    
    /**
     * Set performance context in MDC
     */
    public static void setPerformanceContext(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        MDC.put(DURATION, String.valueOf(duration));
    }
    
    /**
     * Set status in MDC
     */
    public static void setStatus(String status) {
        MDC.put(STATUS, status);
    }
    
    /**
     * Clear all MDC context
     */
    public static void clearContext() {
        MDC.clear();
    }
    
    /**
     * Log API request start
     */
    public static void logApiRequestStart(String endpoint, String method, String requestId) {
        setRequestContext(requestId, "SYSTEM", method + " " + endpoint);
        logger.info("API Request Started - Endpoint: {}, Method: {}, RequestId: {}", 
                   endpoint, method, requestId);
    }
    
    /**
     * Log API request completion
     */
    public static void logApiRequestComplete(String endpoint, String method, String requestId, 
                                           int statusCode, long startTime) {
        setRequestContext(requestId, "SYSTEM", method + " " + endpoint);
        setPerformanceContext(startTime);
        setStatus(String.valueOf(statusCode));
        logger.info("API Request Completed - Endpoint: {}, Method: {}, Status: {}, Duration: {}ms", 
                   endpoint, method, statusCode, System.currentTimeMillis() - startTime);
        clearContext();
    }
    
    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String operation, String entityType, String entityId, 
                                          String requestId, long startTime) {
        setRequestContext(requestId, "SYSTEM", operation);
        setEntityContext(entityType, entityId);
        setPerformanceContext(startTime);
        logger.info("Database Operation - Operation: {}, Entity: {}, EntityId: {}, Duration: {}ms", 
                   operation, entityType, entityId, System.currentTimeMillis() - startTime);
        clearContext();
    }
    
    /**
     * Log Kafka event
     */
    public static void logKafkaEvent(String eventType, String topic, String requestId, String status) {
        setRequestContext(requestId, "SYSTEM", "KAFKA_" + eventType);
        setStatus(status);
        logger.info("Kafka Event - Type: {}, Topic: {}, Status: {}", eventType, topic, status);
        clearContext();
    }
    
    /**
     * Log security event
     */
    public static void logSecurityEvent(String eventType, String userId, String details, String status) {
        setRequestContext(generateRequestId(), userId, "SECURITY_" + eventType);
        setStatus(status);
        logger.info("Security Event - Type: {}, User: {}, Details: {}, Status: {}", 
                   eventType, userId, details, status);
        clearContext();
    }
    
    /**
     * Log business operation
     */
    public static void logBusinessOperation(String operation, String entityType, String entityId, 
                                          String userId, String details) {
        setRequestContext(generateRequestId(), userId, operation);
        setEntityContext(entityType, entityId);
        logger.info("Business Operation - Operation: {}, Entity: {}, EntityId: {}, User: {}, Details: {}", 
                   operation, entityType, entityId, userId, details);
        clearContext();
    }
    
    /**
     * Log error with context
     */
    public static void logError(String operation, String entityType, String entityId, 
                              String userId, String errorMessage, Throwable throwable) {
        setRequestContext(generateRequestId(), userId, operation);
        setEntityContext(entityType, entityId);
        setStatus("ERROR");
        logger.error("Error occurred - Operation: {}, Entity: {}, EntityId: {}, User: {}, Error: {}", 
                    operation, entityType, entityId, userId, errorMessage, throwable);
        clearContext();
    }
    
    /**
     * Log performance metric
     */
    public static void logPerformanceMetric(String metricName, long value, String unit) {
        setRequestContext(generateRequestId(), "SYSTEM", "PERFORMANCE");
        MDC.put("metricName", metricName);
        MDC.put("metricValue", String.valueOf(value));
        MDC.put("metricUnit", unit);
        logger.info("Performance Metric - Name: {}, Value: {}, Unit: {}", metricName, value, unit);
        clearContext();
    }
} 