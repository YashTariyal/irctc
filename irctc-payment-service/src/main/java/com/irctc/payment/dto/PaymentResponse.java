package com.irctc.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for payment response
 */
@Data
public class PaymentResponse {
    private String transactionId;
    private String gatewayTransactionId; // Gateway's transaction ID
    private String gatewayName;
    private BigDecimal amount;
    private String currency;
    private String status; // SUCCESS, FAILED, PENDING
    private String paymentMethod;
    private LocalDateTime transactionTime;
    private BigDecimal gatewayFee; // Fee charged by gateway
    private String failureReason; // If status is FAILED
    private Map<String, String> gatewayResponse; // Raw gateway response
    private String paymentLink; // For payment links/redirects
    private boolean requiresRedirect;
}

