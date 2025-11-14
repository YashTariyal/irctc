package com.irctc.booking.dto;

import com.irctc.booking.entity.SimpleBooking;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for booking modification operations
 */
@Data
public class ModificationResponse {
    private Long bookingId;
    private String pnrNumber;
    private String modificationType; // DATE_CHANGE, SEAT_UPGRADE, ROUTE_CHANGE, PASSENGER_MODIFICATION
    private String status; // SUCCESS, FAILED, PENDING
    private String message;
    
    private SimpleBooking modifiedBooking;
    
    // Financial details
    private BigDecimal originalFare;
    private BigDecimal newFare;
    private BigDecimal fareDifference;
    private BigDecimal modificationCharge;
    private BigDecimal totalAmount; // fareDifference + modificationCharge
    
    private LocalDateTime modificationDate;
    private String modificationId; // Unique ID for this modification
    
    // Refund details (if applicable)
    private BigDecimal refundAmount;
    private String refundStatus;
}

