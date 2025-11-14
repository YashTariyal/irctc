package com.irctc.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for payment request
 */
@Data
public class PaymentRequest {
    private Long bookingId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // CARD, UPI, NETBANKING, WALLET, etc.
    private String description;
    private String customerId;
    private String customerEmail;
    private String customerPhone;
    private Map<String, String> metadata; // Additional gateway-specific data
    private String gatewayPreference; // Optional: preferred gateway name
}

