package com.irctc.payment.gateway.impl;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.dto.PaymentResponse;
import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;
import com.irctc.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Razorpay Payment Gateway Implementation
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.razorpay.enabled", havingValue = "true", matchIfMissing = true)
public class RazorpayGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(RazorpayGateway.class);
    
    @Value("${payment.gateway.razorpay.key-id:}")
    private String keyId;
    
    @Value("${payment.gateway.razorpay.key-secret:}")
    private String keySecret;
    
    @Value("${payment.gateway.razorpay.fee-percentage:2.0}")
    private double feePercentage;
    
    @Value("${payment.gateway.razorpay.fixed-fee:0.0}")
    private double fixedFee;
    
    @Value("${payment.gateway.razorpay.enabled:true}")
    private boolean enabled;
    
    @Override
    public String getGatewayName() {
        return "RAZORPAY";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled && keyId != null && !keyId.isEmpty();
    }
    
    @Override
    public double getTransactionFeePercentage() {
        return feePercentage;
    }
    
    @Override
    public double getFixedFee() {
        return fixedFee;
    }
    
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.info("Processing payment via Razorpay: Booking ID {}, Amount {}", 
            request.getBookingId(), request.getAmount());
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("RAZORPAY");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setTransactionTime(LocalDateTime.now());
        
        try {
            // Simulate Razorpay API call
            // In real implementation, this would call Razorpay SDK/API
            String razorpayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "");
            String razorpayPaymentId = "pay_" + UUID.randomUUID().toString().replace("-", "");
            
            // Calculate gateway fee
            BigDecimal fee = request.getAmount()
                .multiply(BigDecimal.valueOf(feePercentage))
                .divide(BigDecimal.valueOf(100))
                .add(BigDecimal.valueOf(fixedFee));
            response.setGatewayFee(fee);
            
            // Simulate success (90% success rate for demo)
            boolean success = Math.random() > 0.1;
            
            if (success) {
                response.setStatus("SUCCESS");
                response.setTransactionId(UUID.randomUUID().toString());
                response.setGatewayTransactionId(razorpayPaymentId);
                
                Map<String, String> gatewayResponse = new HashMap<>();
                gatewayResponse.put("order_id", razorpayOrderId);
                gatewayResponse.put("payment_id", razorpayPaymentId);
                gatewayResponse.put("status", "captured");
                response.setGatewayResponse(gatewayResponse);
                
                logger.info("✅ Razorpay payment successful: Transaction ID {}", response.getTransactionId());
            } else {
                response.setStatus("FAILED");
                response.setFailureReason("Payment declined by bank");
                logger.warn("❌ Razorpay payment failed: {}", response.getFailureReason());
            }
        } catch (Exception e) {
            logger.error("Error processing Razorpay payment: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public RefundResponse processRefund(RefundRequest request) {
        logger.info("Processing refund via Razorpay: Transaction ID {}, Amount {}", 
            request.getOriginalTransactionId(), request.getRefundAmount());
        
        RefundResponse response = new RefundResponse();
        response.setGatewayName("RAZORPAY");
        response.setOriginalTransactionId(request.getOriginalTransactionId());
        response.setRefundAmount(request.getRefundAmount());
        response.setRefundTime(LocalDateTime.now());
        
        try {
            // Simulate Razorpay refund API call
            String razorpayRefundId = "rfnd_" + UUID.randomUUID().toString().replace("-", "");
            
            response.setRefundId(UUID.randomUUID().toString());
            response.setGatewayRefundId(razorpayRefundId);
            response.setStatus("SUCCESS");
            
            logger.info("✅ Razorpay refund successful: Refund ID {}", response.getRefundId());
        } catch (Exception e) {
            logger.error("Error processing Razorpay refund: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public PaymentResponse verifyPayment(String transactionId) {
        logger.info("Verifying payment via Razorpay: Transaction ID {}", transactionId);
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("RAZORPAY");
        response.setTransactionId(transactionId);
        
        // Simulate verification
        response.setStatus("SUCCESS");
        response.setTransactionTime(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean supportsCurrency(String currency) {
        // Razorpay supports INR and other currencies
        return "INR".equalsIgnoreCase(currency) || 
               "USD".equalsIgnoreCase(currency) ||
               "EUR".equalsIgnoreCase(currency);
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        // Razorpay supports multiple payment methods
        return paymentMethod != null && (
            paymentMethod.equalsIgnoreCase("CARD") ||
            paymentMethod.equalsIgnoreCase("UPI") ||
            paymentMethod.equalsIgnoreCase("NETBANKING") ||
            paymentMethod.equalsIgnoreCase("WALLET")
        );
    }
}

