package com.irctc.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO for wallet top-up request
 */
@Data
public class WalletTopUpRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String currency = "INR";
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CARD, UPI, NETBANKING
    
    private String description;
    
    private String gatewayPreference; // Optional: preferred gateway
}

