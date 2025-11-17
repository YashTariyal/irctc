package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmiPaymentRequest {
    private BigDecimal amount;
}

