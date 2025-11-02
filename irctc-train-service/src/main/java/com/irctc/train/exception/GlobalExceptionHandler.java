package com.irctc.train.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred", ex);
        ErrorResponse error = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected error occurred", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    private ErrorResponse buildErrorResponse(HttpStatus status, String errorCode, String message, HttpServletRequest request) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .errorCode(errorCode)
            .message(message)
            .path(request.getRequestURI())
            .method(request.getMethod());
        
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            builder.correlationId(correlationId);
        }
        
        // Trace ID would be added here if tracing is configured
        // For now, traceId is optional and will be null
        
        return builder.build();
    }
}

