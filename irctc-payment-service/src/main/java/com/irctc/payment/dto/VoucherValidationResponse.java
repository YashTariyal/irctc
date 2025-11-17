package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherValidationResponse {
    private String code;
    private boolean valid;
    private String message;
    private BigDecimal discountAmount;
    private LocalDateTime expiresAt;
}

