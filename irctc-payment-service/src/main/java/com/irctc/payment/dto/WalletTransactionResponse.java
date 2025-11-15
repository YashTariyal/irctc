package com.irctc.payment.dto;

import lombok.Data;
import com.irctc.payment.entity.WalletTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for wallet transaction response
 */
@Data
public class WalletTransactionResponse {
    private Long id;
    private Long walletId;
    private String userId;
    private WalletTransaction.TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String transactionId;
    private String status;
    private String description;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}

