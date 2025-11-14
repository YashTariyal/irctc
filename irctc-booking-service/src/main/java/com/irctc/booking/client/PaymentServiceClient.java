package com.irctc.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Feign client for Payment Service integration
 */
@FeignClient(name = "irctc-payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {
    
    /**
     * Process payment for booking modification
     */
    @PostMapping("/api/payments/process")
    PaymentResponse processPayment(@RequestBody PaymentRequest request);
    
    /**
     * Process refund for booking modification
     */
    @PostMapping("/api/payments/refund/process")
    PaymentResponse processRefund(@RequestBody RefundRequest request);
    
    /**
     * Get payment by booking ID
     */
    @GetMapping("/api/payments/booking/{bookingId}")
    List<PaymentResponse> getPaymentsByBookingId(@PathVariable Long bookingId);
    
    /**
     * Payment Request DTO
     */
    class PaymentRequest {
        private Long bookingId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String description;
        private String modificationId;
        
        // Getters and setters
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getModificationId() { return modificationId; }
        public void setModificationId(String modificationId) { this.modificationId = modificationId; }
    }
    
    /**
     * Refund Request DTO
     */
    class RefundRequest {
        private Long paymentId;
        private BigDecimal refundAmount;
        private String reason;
        private String modificationId;
        
        // Getters and setters
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getModificationId() { return modificationId; }
        public void setModificationId(String modificationId) { this.modificationId = modificationId; }
    }
    
    /**
     * Payment Response DTO
     */
    class PaymentResponse {
        private Long id;
        private Long bookingId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String status;
        private String transactionId;
        private String message;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}

