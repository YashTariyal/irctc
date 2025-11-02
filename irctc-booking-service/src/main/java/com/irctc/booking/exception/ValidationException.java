package com.irctc.booking.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class ValidationException extends CustomException {
    
    private final Map<String, String> validationErrors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = null;
    }
    
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
    }
    
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}

