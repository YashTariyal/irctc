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
 * PayU Payment Gateway Implementation
 */
@Component
@ConditionalOnProperty(name = "payment.gateway.payu.enabled", havingValue = "true", matchIfMissing = false)
public class PayUGateway implements PaymentGateway {
    
    private static final Logger logger = LoggerFactory.getLogger(PayUGateway.class);
    
    @Value("${payment.gateway.payu.merchant-key:}")
    private String merchantKey;
    
    @Value("${payment.gateway.payu.merchant-salt:}")
    private String merchantSalt;
    
    @Value("${payment.gateway.payu.fee-percentage:1.8}")
    private double feePercentage;
    
    @Value("${payment.gateway.payu.fixed-fee:0.0}")
    private double fixedFee;
    
    @Value("${payment.gateway.payu.enabled:false}")
    private boolean enabled;
    
    @Override
    public String getGatewayName() {
        return "PAYU";
    }
    
    @Override
    public boolean isEnabled() {
        return enabled && merchantKey != null && !merchantKey.isEmpty();
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
        logger.info("Processing payment via PayU: Booking ID {}, Amount {}", 
            request.getBookingId(), request.getAmount());
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("PAYU");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setTransactionTime(LocalDateTime.now());
        
        try {
            // Simulate PayU API call
            String payuOrderId = UUID.randomUUID().toString();
            String payuTransactionId = "TXN" + UUID.randomUUID().toString().replace("-", "");
            
            // Calculate gateway fee
            BigDecimal fee = request.getAmount()
                .multiply(BigDecimal.valueOf(feePercentage))
                .divide(BigDecimal.valueOf(100))
                .add(BigDecimal.valueOf(fixedFee));
            response.setGatewayFee(fee);
            
            // Simulate success (88% success rate for demo)
            boolean success = Math.random() > 0.12;
            
            if (success) {
                response.setStatus("SUCCESS");
                response.setTransactionId(UUID.randomUUID().toString());
                response.setGatewayTransactionId(payuTransactionId);
                
                Map<String, String> gatewayResponse = new HashMap<>();
                gatewayResponse.put("order_id", payuOrderId);
                gatewayResponse.put("transaction_id", payuTransactionId);
                gatewayResponse.put("status", "success");
                response.setGatewayResponse(gatewayResponse);
                
                logger.info("✅ PayU payment successful: Transaction ID {}", response.getTransactionId());
            } else {
                response.setStatus("FAILED");
                response.setFailureReason("Payment failed");
                logger.warn("❌ PayU payment failed: {}", response.getFailureReason());
            }
        } catch (Exception e) {
            logger.error("Error processing PayU payment: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public RefundResponse processRefund(RefundRequest request) {
        logger.info("Processing refund via PayU: Transaction ID {}, Amount {}", 
            request.getOriginalTransactionId(), request.getRefundAmount());
        
        RefundResponse response = new RefundResponse();
        response.setGatewayName("PAYU");
        response.setOriginalTransactionId(request.getOriginalTransactionId());
        response.setRefundAmount(request.getRefundAmount());
        response.setRefundTime(LocalDateTime.now());
        
        try {
            // Simulate PayU refund API call
            String payuRefundId = "REF" + UUID.randomUUID().toString().replace("-", "");
            
            response.setRefundId(UUID.randomUUID().toString());
            response.setGatewayRefundId(payuRefundId);
            response.setStatus("SUCCESS");
            
            logger.info("✅ PayU refund successful: Refund ID {}", response.getRefundId());
        } catch (Exception e) {
            logger.error("Error processing PayU refund: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setFailureReason("Gateway error: " + e.getMessage());
        }
        
        return response;
    }
    
    @Override
    public PaymentResponse verifyPayment(String transactionId) {
        logger.info("Verifying payment via PayU: Transaction ID {}", transactionId);
        
        PaymentResponse response = new PaymentResponse();
        response.setGatewayName("PAYU");
        response.setTransactionId(transactionId);
        response.setStatus("SUCCESS");
        response.setTransactionTime(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean supportsCurrency(String currency) {
        // PayU primarily supports INR
        return "INR".equalsIgnoreCase(currency);
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        // PayU supports multiple payment methods
        return paymentMethod != null && (
            paymentMethod.equalsIgnoreCase("CARD") ||
            paymentMethod.equalsIgnoreCase("UPI") ||
            paymentMethod.equalsIgnoreCase("NETBANKING") ||
            paymentMethod.equalsIgnoreCase("WALLET")
        );
    }
}

