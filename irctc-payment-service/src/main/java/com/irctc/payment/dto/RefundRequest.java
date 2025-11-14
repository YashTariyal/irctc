package com.irctc.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for refund request
 */
@Data
public class RefundRequest {
    private String originalTransactionId;
    private String gatewayTransactionId; // Gateway's original transaction ID
    private BigDecimal refundAmount;
    private String reason;
    private String gatewayName; // Gateway used for original payment
}

