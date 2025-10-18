package com.irctc_backend.irctc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment Callback Request DTO
 * 
 * This DTO represents the callback data received from payment gateways
 * after payment processing. It includes all necessary fields for payment
 * verification and status updates.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {
    
    private String razorpay_payment_id;
    
    private String razorpay_order_id;
    
    private String razorpay_signature;
    
    private String payment_id;
    
    private String order_id;
    
    private String signature;
    
    private String status;
    
    private String amount;
    
    private String currency;
    
    private String method;
    
    private String description;
    
    private String email;
    
    private String contact;
    
    private String fee;
    
    private String tax;
    
    private String error_code;
    
    private String error_description;
    
    private String error_source;
    
    private String error_step;
    
    private String error_reason;
    
    /**
     * Get payment ID from callback (supports both formats)
     * 
     * @return payment ID
     */
    public String getPaymentId() {
        return razorpay_payment_id != null ? razorpay_payment_id : payment_id;
    }
    
    /**
     * Get order ID from callback (supports both formats)
     * 
     * @return order ID
     */
    public String getOrderId() {
        return razorpay_order_id != null ? razorpay_order_id : order_id;
    }
    
    /**
     * Get signature from callback (supports both formats)
     * 
     * @return signature
     */
    public String getSignature() {
        return razorpay_signature != null ? razorpay_signature : signature;
    }
    
    /**
     * Check if payment was successful
     * 
     * @return true if payment is successful
     */
    public boolean isSuccessful() {
        return "captured".equalsIgnoreCase(status) || "authorized".equalsIgnoreCase(status);
    }
    
    /**
     * Check if payment failed
     * 
     * @return true if payment failed
     */
    public boolean isFailed() {
        return "failed".equalsIgnoreCase(status) || error_code != null;
    }
    
    /**
     * Get error message if payment failed
     * 
     * @return error message
     */
    public String getErrorMessage() {
        if (error_description != null) {
            return error_description;
        }
        if (error_reason != null) {
            return error_reason;
        }
        return "Payment failed";
    }
}
