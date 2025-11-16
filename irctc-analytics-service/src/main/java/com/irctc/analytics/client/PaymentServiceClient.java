package com.irctc.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Feign client for Payment Service integration
 */
@FeignClient(name = "irctc-payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {
    
    /**
     * Get payment analytics overview
     */
    @GetMapping("/api/payments/analytics/overview")
    Map<String, Object> getPaymentOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
    
    /**
     * Get daily payment statistics
     */
    @GetMapping("/api/payments/analytics/daily")
    List<Map<String, Object>> getDailyPaymentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
    
    /**
     * Get payments by booking ID
     */
    @GetMapping("/api/payments/booking/{bookingId}")
    List<PaymentDTO> getPaymentsByBookingId(@PathVariable Long bookingId);
    
    /**
     * Get refund statistics
     */
    @GetMapping("/api/payments/refunds/statistics")
    Map<String, Object> getRefundStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
    
    /**
     * Payment DTO
     */
    class PaymentDTO {
        private Long id;
        private Long bookingId;
        private Double amount;
        private String currency;
        private String paymentMethod;
        private String status;
        private String transactionId;
        private LocalDate paymentDate;
        private Boolean isRefunded;
        private Double refundAmount;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
        public Boolean getIsRefunded() { return isRefunded; }
        public void setIsRefunded(Boolean isRefunded) { this.isRefunded = isRefunded; }
        public Double getRefundAmount() { return refundAmount; }
        public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }
    }
}

