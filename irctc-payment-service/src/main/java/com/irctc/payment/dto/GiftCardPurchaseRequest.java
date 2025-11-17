package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GiftCardPurchaseRequest {
    private Long purchaserUserId;
    private String recipientEmail;
    private String message;
    private BigDecimal amount;
    private LocalDateTime expiresAt;
}

