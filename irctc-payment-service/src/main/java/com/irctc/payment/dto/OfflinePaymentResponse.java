package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OfflinePaymentResponse {
    private Long id;
    private Long userId;
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String status;
    private String failureReason;
    private Long processedPaymentId;
    private LocalDateTime queuedAt;
    private LocalDateTime processedAt;
}

