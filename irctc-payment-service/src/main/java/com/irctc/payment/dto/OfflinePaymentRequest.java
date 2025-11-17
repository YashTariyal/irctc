package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfflinePaymentRequest {
    private Long userId;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String gatewayPreference;
    private String metadata;
}

