package com.irctc.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentPlanResponse {
    private Long id;
    private Long bookingId;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal downPayment;
    private BigDecimal emiAmount;
    private BigDecimal interestRate;
    private Integer installments;
    private String frequency;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<EmiPaymentSummary> schedule;

    @Data
    public static class EmiPaymentSummary {
        private Long id;
        private Integer installmentNumber;
        private LocalDate dueDate;
        private BigDecimal amountDue;
        private BigDecimal amountPaid;
        private String status;
    }
}

