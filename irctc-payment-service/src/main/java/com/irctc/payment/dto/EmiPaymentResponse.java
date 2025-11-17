package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiPaymentResponse {
    private Long id;
    private Long paymentPlanId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String status;
    private BigDecimal penaltyAmount;
    private String paymentReference;
}

