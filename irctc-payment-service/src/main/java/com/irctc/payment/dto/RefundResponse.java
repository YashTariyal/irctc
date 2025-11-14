package com.irctc.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for refund response
 */
@Data
public class RefundResponse {
    private String refundId;
    private String gatewayRefundId; // Gateway's refund ID
    private String gatewayName;
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String status; // SUCCESS, FAILED, PENDING
    private LocalDateTime refundTime;
    private String failureReason; // If status is FAILED
}

