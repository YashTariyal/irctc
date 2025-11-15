package com.irctc.booking.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for QR code verification request
 */
@Data
public class QRCodeVerificationRequest {
    
    @NotBlank(message = "QR code is required")
    private String qrCode;
    
    private String verifiedBy; // Optional: User ID or device ID
}

