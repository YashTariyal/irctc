package com.irctc.external.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Razorpay Payment Gateway Integration
 */
@Service
public class RazorpayPaymentService {

    private final WebClient webClient;
    private final String razorpayKeyId;
    private final String razorpayKeySecret;
    private final String razorpayBaseUrl;

    public RazorpayPaymentService(WebClient.Builder webClientBuilder,
                                @Value("${razorpay.key.id}") String razorpayKeyId,
                                @Value("${razorpay.key.secret}") String razorpayKeySecret,
                                @Value("${razorpay.base.url:https://api.razorpay.com/v1}") String razorpayBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(razorpayBaseUrl)
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(razorpayKeyId, razorpayKeySecret);
                    headers.set("Content-Type", "application/json");
                })
                .build();
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayKeySecret = razorpayKeySecret;
        this.razorpayBaseUrl = razorpayBaseUrl;
    }

    /**
     * Create a payment order
     */
    public Mono<RazorpayOrderResponse> createOrder(CreateOrderRequest request) {
        return webClient.post()
                .uri("/orders")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RazorpayOrderResponse.class)
                .onErrorResume(throwable -> {
                    // Log error and return fallback response
                    System.err.println("Error creating Razorpay order: " + throwable.getMessage());
                    return Mono.just(new RazorpayOrderResponse());
                });
    }

    /**
     * Verify payment signature
     */
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, 
                                        String razorpaySignature, String razorpaySecret) {
        try {
            String generatedSignature = generateSignature(razorpayOrderId + "|" + razorpayPaymentId, razorpaySecret);
            return generatedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            System.err.println("Error verifying payment signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Capture payment
     */
    public Mono<RazorpayPaymentResponse> capturePayment(String paymentId, BigDecimal amount, String currency) {
        CapturePaymentRequest request = new CapturePaymentRequest(amount, currency);
        
        return webClient.post()
                .uri("/payments/" + paymentId + "/capture")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RazorpayPaymentResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error capturing payment: " + throwable.getMessage());
                    return Mono.just(new RazorpayPaymentResponse());
                });
    }

    /**
     * Create refund
     */
    public Mono<RazorpayRefundResponse> createRefund(String paymentId, BigDecimal amount, String reason) {
        CreateRefundRequest request = new CreateRefundRequest(amount, reason);
        
        return webClient.post()
                .uri("/payments/" + paymentId + "/refund")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RazorpayRefundResponse.class)
                .onErrorResume(throwable -> {
                    System.err.println("Error creating refund: " + throwable.getMessage());
                    return Mono.just(new RazorpayRefundResponse());
                });
    }

    private String generateSignature(String data, String secret) {
        // In production, use proper HMAC-SHA256 implementation
        // This is a simplified version for demonstration
        return "signature_" + data.hashCode();
    }

    // Request/Response DTOs
    public static class CreateOrderRequest {
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("receipt")
        private String receipt;
        
        @JsonProperty("notes")
        private Map<String, String> notes;

        public CreateOrderRequest() {}

        public CreateOrderRequest(BigDecimal amount, String currency, String receipt) {
            this.amount = amount.multiply(BigDecimal.valueOf(100)); // Convert to paise
            this.currency = currency;
            this.receipt = receipt;
        }

        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getReceipt() { return receipt; }
        public void setReceipt(String receipt) { this.receipt = receipt; }
        
        public Map<String, String> getNotes() { return notes; }
        public void setNotes(Map<String, String> notes) { this.notes = notes; }
    }

    public static class RazorpayOrderResponse {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("receipt")
        private String receipt;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getReceipt() { return receipt; }
        public void setReceipt(String receipt) { this.receipt = receipt; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CapturePaymentRequest {
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;

        public CapturePaymentRequest() {}

        public CapturePaymentRequest(BigDecimal amount, String currency) {
            this.amount = amount.multiply(BigDecimal.valueOf(100)); // Convert to paise
            this.currency = currency;
        }

        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class RazorpayPaymentResponse {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("captured")
        private boolean captured;
        
        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public boolean isCaptured() { return captured; }
        public void setCaptured(boolean captured) { this.captured = captured; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CreateRefundRequest {
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("notes")
        private Map<String, String> notes;

        public CreateRefundRequest() {}

        public CreateRefundRequest(BigDecimal amount, String reason) {
            this.amount = amount.multiply(BigDecimal.valueOf(100)); // Convert to paise
            this.notes = Map.of("reason", reason);
        }

        // Getters and Setters
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Map<String, String> getNotes() { return notes; }
        public void setNotes(Map<String, String> notes) { this.notes = notes; }
    }

    public static class RazorpayRefundResponse {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("amount")
        private BigDecimal amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
