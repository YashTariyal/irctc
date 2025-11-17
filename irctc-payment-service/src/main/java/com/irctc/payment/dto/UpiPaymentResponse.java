package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpiPaymentResponse {
    private Long intentId;
    private String orderId;
    private String status;
    private String utr;
    private String qrPayload;
    private LocalDateTime expiresAt;
    private BigDecimal amount;
    private String currency;
}

