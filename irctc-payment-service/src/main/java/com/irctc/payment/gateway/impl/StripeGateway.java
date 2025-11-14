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
 * Stripe Payment Gateway Implementation
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.stripe.enabled", havingValue = "true", matchIfMissing = false)
public class StripeGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(StripeGateway.class);
    
    @Value("${payment.gateway.stripe.secret-key:}")
    private String secretKey;
    
    @Value("${payment.gateway.stripe.publishable-key:}")
    private String publishableKey;
    
    @Value("${payment.gateway.stripe.fee-percentage:2.9}")
    private double feePercentage;
    
    @Value("${payment.gateway.stripe.fixed-fee:0.30}")
    private double fixedFee;
    
    @Value("${payment.gateway.stripe.enabled:false}")
    private boolean enabled;
    
    @Override
    public String getGatewayName() {
        return "STRIPE";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled && secretKey != null && !secretKey.isEmpty();
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
        logger.info("Processing payment via Stripe: Booking ID {}, Amount {}", 
            request.getBookingId(), request.getAmount());
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("STRIPE");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setTransactionTime(LocalDateTime.now());
        
        try {
            // Simulate Stripe API call
            String stripePaymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "");
            String stripeChargeId = "ch_" + UUID.randomUUID().toString().replace("-", "");
            
            // Calculate gateway fee (Stripe charges 2.9% + $0.30)
            BigDecimal fee = request.getAmount()
                .multiply(BigDecimal.valueOf(feePercentage))
                .divide(BigDecimal.valueOf(100))
                .add(BigDecimal.valueOf(fixedFee));
            response.setGatewayFee(fee);
            
            // Simulate success (85% success rate for demo)
            boolean success = Math.random() > 0.15;
            
            if (success) {
                response.setStatus("SUCCESS");
                response.setTransactionId(UUID.randomUUID().toString());
                response.setGatewayTransactionId(stripeChargeId);
                
                Map<String, String> gatewayResponse = new HashMap<>();
                gatewayResponse.put("payment_intent_id", stripePaymentIntentId);
                gatewayResponse.put("charge_id", stripeChargeId);
                gatewayResponse.put("status", "succeeded");
                response.setGatewayResponse(gatewayResponse);
                
                logger.info("✅ Stripe payment successful: Transaction ID {}", response.getTransactionId());
            } else {
                response.setStatus("FAILED");
                response.setFailureReason("Card declined");
                logger.warn("❌ Stripe payment failed: {}", response.getFailureReason());
            }
        } catch (Exception e) {
            logger.error("Error processing Stripe payment: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public RefundResponse processRefund(RefundRequest request) {
        logger.info("Processing refund via Stripe: Transaction ID {}, Amount {}", 
            request.getOriginalTransactionId(), request.getRefundAmount());
        
        RefundResponse response = new RefundResponse();
        response.setGatewayName("STRIPE");
        response.setOriginalTransactionId(request.getOriginalTransactionId());
        response.setRefundAmount(request.getRefundAmount());
        response.setRefundTime(LocalDateTime.now());
        
        try {
            // Simulate Stripe refund API call
            String stripeRefundId = "re_" + UUID.randomUUID().toString().replace("-", "");
            
            response.setRefundId(UUID.randomUUID().toString());
            response.setGatewayRefundId(stripeRefundId);
            response.setStatus("SUCCESS");
            
            logger.info("✅ Stripe refund successful: Refund ID {}", response.getRefundId());
        } catch (Exception e) {
            logger.error("Error processing Stripe refund: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public PaymentResponse verifyPayment(String transactionId) {
        logger.info("Verifying payment via Stripe: Transaction ID {}", transactionId);
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("STRIPE");
        response.setTransactionId(transactionId);
        response.setStatus("SUCCESS");
        response.setTransactionTime(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean supportsCurrency(String currency) {
        // Stripe supports many currencies
        return currency != null && (
            currency.equalsIgnoreCase("USD") ||
            currency.equalsIgnoreCase("EUR") ||
            currency.equalsIgnoreCase("GBP") ||
            currency.equalsIgnoreCase("INR")
        );
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        // Stripe primarily supports card payments
        return paymentMethod != null && paymentMethod.equalsIgnoreCase("CARD");
    }
}

