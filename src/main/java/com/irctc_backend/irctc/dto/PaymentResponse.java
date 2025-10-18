package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Payment Response DTO
 * 
 * This DTO represents the response structure for payment operations
 * including payment initiation, status updates, and payment confirmations.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long paymentId;
    
    private String transactionId;
    
    private BigDecimal amount;
    
    private String currency;
    
    private Payment.PaymentStatus status;
    
    private Payment.PaymentMethod paymentMethod;
    
    private String gatewayOrderId;
    
    private String gatewayPaymentId;
    
    private String paymentUrl;
    
    private String qrCode;
    
    private String upiId;
    
    private String failureReason;
    
    private LocalDateTime paymentDate;
    
    private LocalDateTime createdAt;
    
    private Map<String, Object> gatewayResponse;
    
    private String message;
    
    private boolean success;
    
    /**
     * Create successful payment response
     * 
     * @param paymentId Payment ID
     * @param transactionId Transaction ID
     * @param amount Payment amount
     * @param status Payment status
     * @param message Success message
     * @return PaymentResponse instance
     */
    public static PaymentResponse success(Long paymentId, String transactionId, 
                                        BigDecimal amount, Payment.PaymentStatus status, 
                                        String message) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setTransactionId(transactionId);
        response.setAmount(amount);
        response.setStatus(status);
        response.setMessage(message);
        response.setSuccess(true);
        response.setCurrency("INR");
        return response;
    }
    
    /**
     * Create failed payment response
     * 
     * @param paymentId Payment ID
     * @param transactionId Transaction ID
     * @param amount Payment amount
     * @param status Payment status
     * @param failureReason Failure reason
     * @return PaymentResponse instance
     */
    public static PaymentResponse failure(Long paymentId, String transactionId, 
                                        BigDecimal amount, Payment.PaymentStatus status, 
                                        String failureReason) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setTransactionId(transactionId);
        response.setAmount(amount);
        response.setStatus(status);
        response.setFailureReason(failureReason);
        response.setMessage("Payment failed: " + failureReason);
        response.setSuccess(false);
        response.setCurrency("INR");
        return response;
    }
    
    /**
     * Create payment initiation response with gateway details
     * 
     * @param paymentId Payment ID
     * @param transactionId Transaction ID
     * @param amount Payment amount
     * @param gatewayOrderId Gateway order ID
     * @param paymentUrl Payment URL for redirection
     * @param qrCode QR code for UPI payments
     * @return PaymentResponse instance
     */
    public static PaymentResponse initiated(Long paymentId, String transactionId, 
                                          BigDecimal amount, String gatewayOrderId, 
                                          String paymentUrl, String qrCode) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(paymentId);
        response.setTransactionId(transactionId);
        response.setAmount(amount);
        response.setStatus(Payment.PaymentStatus.PENDING);
        response.setGatewayOrderId(gatewayOrderId);
        response.setPaymentUrl(paymentUrl);
        response.setQrCode(qrCode);
        response.setMessage("Payment initiated successfully");
        response.setSuccess(true);
        response.setCurrency("INR");
        return response;
    }
}
