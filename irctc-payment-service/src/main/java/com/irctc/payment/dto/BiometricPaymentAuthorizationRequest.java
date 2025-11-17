package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BiometricPaymentAuthorizationRequest {
    private Long userId;
    private Long paymentId;
    private String deviceId;
    private BigDecimal amount;
    private String currency;
    private String verificationToken;
}

