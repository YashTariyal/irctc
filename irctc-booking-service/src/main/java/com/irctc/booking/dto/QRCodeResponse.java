package com.irctc.booking.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for QR code response
 */
@Data
public class QRCodeResponse {
    private Long id;
    private Long bookingId;
    private String qrCode;
    private String pnrNumber;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private String qrCodeImage; // Base64 encoded QR code image
    private LocalDateTime createdAt;
}

