package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GiftCardBalanceResponse {
    private String code;
    private BigDecimal balance;
    private String status;
    private LocalDateTime expiresAt;
}

