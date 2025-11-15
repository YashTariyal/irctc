package com.irctc.booking.exception;

/**
 * Exception thrown when a request is invalid
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class InvalidRequestException extends CustomException {
    
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }
}

