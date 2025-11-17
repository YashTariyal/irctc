package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletPaymentResponse {
    private Long transactionId;
    private String walletReference;
    private String status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
}

