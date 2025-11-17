package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentPlanRequest {
    private Long bookingId;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private Integer installments;
    private BigDecimal interestRate;
    private String frequency;
}

