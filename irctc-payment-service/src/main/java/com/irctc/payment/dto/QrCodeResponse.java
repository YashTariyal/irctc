package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QrCodeResponse {
    private String referenceId;
    private String qrPayload;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime expiresAt;
}

