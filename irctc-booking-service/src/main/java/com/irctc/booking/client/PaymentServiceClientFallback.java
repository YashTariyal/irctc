package com.irctc.booking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fallback implementation for Payment Service Client
 * Used when Payment Service is unavailable
 */
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceClientFallback.class);
    
    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        logger.warn("Payment Service unavailable - using fallback for payment processing");
        PaymentResponse response = new PaymentResponse();
        response.setStatus("FAILED");
        response.setMessage("Payment service unavailable - please try again later");
        return response;
    }
    
    @Override
    public PaymentResponse processRefund(RefundRequest request) {
        logger.warn("Payment Service unavailable - using fallback for refund processing");
        PaymentResponse response = new PaymentResponse();
        response.setStatus("FAILED");
        response.setMessage("Payment service unavailable - please try again later");
        return response;
    }
    
    @Override
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        logger.warn("Payment Service unavailable - using fallback for payment lookup");
        return new ArrayList<>();
    }
}

