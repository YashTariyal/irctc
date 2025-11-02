package com.irctc.booking.exception;

/**
 * Exception thrown for business logic violations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class BusinessException extends CustomException {
    
    public BusinessException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION");
    }
    
    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }
}

