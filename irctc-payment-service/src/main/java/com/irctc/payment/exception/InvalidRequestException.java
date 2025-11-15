package com.irctc.payment.exception;

public class InvalidRequestException extends CustomException {
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }
}

