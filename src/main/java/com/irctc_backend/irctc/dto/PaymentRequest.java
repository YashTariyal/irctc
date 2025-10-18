package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Payment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payment Request DTO
 * 
 * This DTO represents the payment request data structure for processing
 * payments through various payment gateways in the IRCTC system.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @NotNull(message = "Booking ID is required")
    private Long bookingId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1.0")
    @DecimalMax(value = "100000.0", message = "Amount cannot exceed 100,000")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;
    
    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "INR|USD|EUR", message = "Currency must be INR, USD, or EUR")
    private String currency = "INR";
    
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    private String customerEmail;
    
    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Customer phone must be a valid 10-digit Indian mobile number")
    private String customerPhone;
    
    private String description;
    
    private String returnUrl;
    
    private String cancelUrl;
    
    // Additional fields for specific payment methods
    private String upiId;
    
    private String cardNumber;
    
    private String cardExpiry;
    
    private String cardCvv;
    
    private String cardHolderName;
    
    private String bankCode;
    
    private String walletType;
    
    /**
     * Get payment description with booking context
     * 
     * @return formatted payment description
     */
    public String getFormattedDescription() {
        if (description != null && !description.isEmpty()) {
            return description;
        }
        return String.format("IRCTC Booking Payment - Booking ID: %d", bookingId);
    }
}
