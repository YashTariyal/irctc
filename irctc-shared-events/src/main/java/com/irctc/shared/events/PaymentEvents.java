package com.irctc.shared.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Payment-related events for Kafka messaging
 */
public class PaymentEvents {

    public static class PaymentInitiatedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("paymentId")
        private Long paymentId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("paymentMethod")
        private String paymentMethod;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
        
        @JsonProperty("eventType")
        private String eventType = "PAYMENT_INITIATED";

        // Constructors
        public PaymentInitiatedEvent() {}

        public PaymentInitiatedEvent(Long paymentId, Long bookingId, Long userId, 
                                   BigDecimal amount, String currency, String paymentMethod) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.paymentId = paymentId;
            this.bookingId = bookingId;
            this.userId = userId;
            this.amount = amount;
            this.currency = currency;
            this.paymentMethod = paymentMethod;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class PaymentCompletedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("paymentId")
        private Long paymentId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("transactionId")
        private String transactionId;
        
        @JsonProperty("paymentGateway")
        private String paymentGateway;
        
        @JsonProperty("completionTime")
        private LocalDateTime completionTime;
        
        @JsonProperty("eventType")
        private String eventType = "PAYMENT_COMPLETED";

        // Constructors
        public PaymentCompletedEvent() {}

        public PaymentCompletedEvent(Long paymentId, Long bookingId, Long userId, 
                                   BigDecimal amount, String transactionId, String paymentGateway) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.paymentId = paymentId;
            this.bookingId = bookingId;
            this.userId = userId;
            this.amount = amount;
            this.transactionId = transactionId;
            this.paymentGateway = paymentGateway;
            this.completionTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getPaymentGateway() { return paymentGateway; }
        public void setPaymentGateway(String paymentGateway) { this.paymentGateway = paymentGateway; }
        
        public LocalDateTime getCompletionTime() { return completionTime; }
        public void setCompletionTime(LocalDateTime completionTime) { this.completionTime = completionTime; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class PaymentFailedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("paymentId")
        private Long paymentId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("failureReason")
        private String failureReason;
        
        @JsonProperty("errorCode")
        private String errorCode;
        
        @JsonProperty("failureTime")
        private LocalDateTime failureTime;
        
        @JsonProperty("eventType")
        private String eventType = "PAYMENT_FAILED";

        // Constructors
        public PaymentFailedEvent() {}

        public PaymentFailedEvent(Long paymentId, Long bookingId, Long userId, 
                                BigDecimal amount, String failureReason, String errorCode) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.paymentId = paymentId;
            this.bookingId = bookingId;
            this.userId = userId;
            this.amount = amount;
            this.failureReason = failureReason;
            this.errorCode = errorCode;
            this.failureTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getFailureReason() { return failureReason; }
        public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public LocalDateTime getFailureTime() { return failureTime; }
        public void setFailureTime(LocalDateTime failureTime) { this.failureTime = failureTime; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }

    public static class RefundProcessedEvent {
        @JsonProperty("eventId")
        private String eventId;
        
        @JsonProperty("refundId")
        private Long refundId;
        
        @JsonProperty("paymentId")
        private Long paymentId;
        
        @JsonProperty("bookingId")
        private Long bookingId;
        
        @JsonProperty("userId")
        private Long userId;
        
        @JsonProperty("refundAmount")
        private BigDecimal refundAmount;
        
        @JsonProperty("refundReason")
        private String refundReason;
        
        @JsonProperty("refundTime")
        private LocalDateTime refundTime;
        
        @JsonProperty("eventType")
        private String eventType = "REFUND_PROCESSED";

        // Constructors
        public RefundProcessedEvent() {}

        public RefundProcessedEvent(Long refundId, Long paymentId, Long bookingId, Long userId, 
                                  BigDecimal refundAmount, String refundReason) {
            this.eventId = java.util.UUID.randomUUID().toString();
            this.refundId = refundId;
            this.paymentId = paymentId;
            this.bookingId = bookingId;
            this.userId = userId;
            this.refundAmount = refundAmount;
            this.refundReason = refundReason;
            this.refundTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public Long getRefundId() { return refundId; }
        public void setRefundId(Long refundId) { this.refundId = refundId; }
        
        public Long getPaymentId() { return paymentId; }
        public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
        
        public Long getBookingId() { return bookingId; }
        public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public String getRefundReason() { return refundReason; }
        public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
        
        public LocalDateTime getRefundTime() { return refundTime; }
        public void setRefundTime(LocalDateTime refundTime) { this.refundTime = refundTime; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
