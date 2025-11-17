package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpiPaymentRequest {
    private String orderId;
    private Long bookingId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private String vpa;
    private String note;
}

