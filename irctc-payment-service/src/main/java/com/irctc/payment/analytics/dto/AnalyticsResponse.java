package com.irctc.payment.analytics.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTOs for analytics responses
 */
public class AnalyticsResponse {
    
    @Data
    public static class Overview {
        private Long totalTransactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal totalFees;
        private BigDecimal averageTransactionAmount;
        private Double successRate;
        private Long refundsCount;
        private BigDecimal refundsAmount;
    }
    
    @Data
    public static class DailyStats {
        private LocalDate date;
        private Long transactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal totalFees;
        private Double successRate;
    }
    
    @Data
    public static class WeeklyStats {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private Long transactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal totalFees;
        private Double successRate;
    }
    
    @Data
    public static class MonthlyStats {
        private Integer year;
        private Integer month;
        private Long transactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal totalFees;
        private Double successRate;
    }
    
    @Data
    public static class GatewayPerformance {
        private String gatewayName;
        private Long transactions;
        private Long successfulTransactions;
        private Long failedTransactions;
        private BigDecimal totalAmount;
        private BigDecimal totalFees;
        private Double successRate;
        private BigDecimal averageFee;
    }
    
    @Data
    public static class PaymentMethodStats {
        private String paymentMethod;
        private Long transactions;
        private Long successfulTransactions;
        private BigDecimal totalAmount;
        private Double percentage;
    }
}

