package com.irctc.booking.exception;

/**
 * Base Custom Exception
 * 
 * All custom exceptions should extend this class
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class CustomException extends RuntimeException {
    
    private final String errorCode;
    
    public CustomException(String message) {
        super(message);
        this.errorCode = "GENERIC_ERROR";
    }
    
    public CustomException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CustomException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERIC_ERROR";
    }
    
    public CustomException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

