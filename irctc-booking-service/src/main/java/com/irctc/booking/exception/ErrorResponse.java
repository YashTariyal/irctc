package com.irctc.booking.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standard Error Response DTO
 * 
 * Provides a consistent error response format across all APIs
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private Instant timestamp;
    
    /**
     * HTTP status code
     */
    private Integer status;
    
    /**
     * Error code (e.g., "ENTITY_NOT_FOUND", "VALIDATION_ERROR")
     */
    private String errorCode;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Detailed error description
     */
    private String detail;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * HTTP method
     */
    private String method;
    
    /**
     * Correlation ID for tracing the request
     */
    private String correlationId;
    
    /**
     * Trace ID from OpenTelemetry
     */
    private String traceId;
    
    /**
     * Additional error details (e.g., validation errors)
     */
    private Map<String, Object> errors;
    
    /**
     * Stack trace (only in development mode)
     */
    private String stackTrace;
}

