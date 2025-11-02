package com.irctc.booking.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Global Exception Handler
 * 
 * Centralized exception handling for all REST endpoints.
 * Provides consistent error response format with correlation IDs.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Autowired(required = false)
    private Tracer tracer;
    
    /**
     * Handle custom entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, 
            HttpServletRequest request) {
        
        logger.warn("Entity not found: {}", ex.getMessage());
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getErrorCode(),
            ex.getMessage(),
            request
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            ex.getErrorCode(),
            ex.getMessage(),
            request
        );
        
        if (ex.getValidationErrors() != null) {
            error.setErrors(Map.of("validationErrors", ex.getValidationErrors()));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        logger.warn("Business rule violation: {}", ex.getMessage());
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getErrorCode(),
            ex.getMessage(),
            request
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
    
    /**
     * Handle custom exceptions
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException ex,
            HttpServletRequest request) {
        
        logger.error("Custom exception: {}", ex.getMessage(), ex);
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ex.getErrorCode(),
            ex.getMessage(),
            request
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle method argument validation errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed: {}", errors);
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_ERROR",
            "Validation failed for the request",
            request
        );
        
        error.setErrors(Map.of("fieldErrors", errors));
        error.setDetail(String.format("Validation failed for %d field(s)", errors.size()));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        logger.warn("Constraint violation: {}", errors);
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "CONSTRAINT_VIOLATION",
            "Constraint validation failed",
            request
        );
        
        error.setErrors(Map.of("constraintViolations", errors));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        String message = String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        logger.warn("Type mismatch: {}", message);
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "TYPE_MISMATCH",
            message,
            request
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        
        logger.warn("Malformed request body: {}", ex.getMessage());
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "MALFORMED_REQUEST",
            "Request body is malformed or invalid JSON",
            request
        );
        
        error.setDetail(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle 404 Not Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex,
            HttpServletRequest request) {
        
        logger.warn("No handler found: {}", ex.getRequestURL());
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "ENDPOINT_NOT_FOUND",
            String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
            request
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            request
        );
        
        // Include stack trace in development mode
        boolean isDevelopment = "dev".equals(System.getProperty("spring.profiles.active"));
        if (isDevelopment) {
            StringBuilder stackTrace = new StringBuilder();
            for (StackTraceElement element : ex.getStackTrace()) {
                stackTrace.append(element.toString()).append("\n");
            }
            error.setStackTrace(stackTrace.toString());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Build standardized error response
     */
    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request) {
        
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(status.value())
            .errorCode(errorCode)
            .message(message)
            .path(request.getRequestURI())
            .method(request.getMethod());
        
        // Add correlation ID from MDC
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            builder.correlationId(correlationId);
        }
        
        // Add trace ID from OpenTelemetry
        if (tracer != null && tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceId();
            builder.traceId(traceId);
        }
        
        return builder.build();
    }
}

