package com.irctc.payment.dto;

import lombok.Data;

@Data
public class BiometricPaymentAuthorizationResponse {
    private boolean authorized;
    private String verificationId;
    private String message;
    private Long logId;
}

