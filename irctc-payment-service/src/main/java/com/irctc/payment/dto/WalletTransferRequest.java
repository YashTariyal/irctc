package com.irctc.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO for wallet transfer request
 */
@Data
public class WalletTransferRequest {
    
    @NotNull(message = "Recipient user ID is required")
    private String recipientUserId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    private String description;
}

