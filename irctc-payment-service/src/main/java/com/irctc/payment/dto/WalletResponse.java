package com.irctc.payment.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for wallet response
 */
@Data
public class WalletResponse {
    private Long id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal totalTopUp;
    private BigDecimal totalSpent;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionAt;
}

