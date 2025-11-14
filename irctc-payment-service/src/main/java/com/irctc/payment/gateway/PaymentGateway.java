package com.irctc.payment.gateway;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.dto.PaymentResponse;
import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;

/**
 * Interface for payment gateway implementations
 * Provides abstraction for multiple payment gateway integrations
 */
public interface PaymentGateway {
    
    /**
     * Get the name/identifier of this gateway
     */
    String getGatewayName();
    
    /**
     * Check if this gateway is enabled
     */
    boolean isEnabled();
    
    /**
     * Get the transaction fee percentage for this gateway
     */
    double getTransactionFeePercentage();
    
    /**
     * Get the fixed fee amount for this gateway
     */
    double getFixedFee();
    
    /**
     * Process a payment
     * @param request Payment request
     * @return Payment response with transaction details
     */
    PaymentResponse processPayment(PaymentRequest request);
    
    /**
     * Process a refund
     * @param request Refund request
     * @return Refund response with refund details
     */
    RefundResponse processRefund(RefundRequest request);
    
    /**
     * Verify payment status
     * @param transactionId Transaction ID to verify
     * @return Payment response with current status
     */
    PaymentResponse verifyPayment(String transactionId);
    
    /**
     * Check if gateway supports the given currency
     */
    boolean supportsCurrency(String currency);
    
    /**
     * Check if gateway supports the given payment method
     */
    boolean supportsPaymentMethod(String paymentMethod);
}

