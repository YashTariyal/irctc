package com.irctc.payment.exception;

public class InsufficientBalanceException extends CustomException {
    public InsufficientBalanceException(String message) {
        super(message, "INSUFFICIENT_BALANCE");
    }
}

