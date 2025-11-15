package com.irctc.booking.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for QR code verification response
 */
@Data
public class QRCodeVerificationResponse {
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isAlreadyVerified;
    private String message;
    private Long bookingId;
    private String pnrNumber;
    private String bookingStatus;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
}

