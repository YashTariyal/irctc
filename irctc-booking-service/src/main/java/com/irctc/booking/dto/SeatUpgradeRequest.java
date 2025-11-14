package com.irctc.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for seat upgrade/downgrade request
 */
@Data
public class SeatUpgradeRequest extends BookingModificationRequest {
    @NotBlank(message = "New seat class is required")
    private String newSeatClass; // e.g., "AC", "SLEEPER", "3AC", "2AC", "1AC"
    
    @NotNull(message = "New fare is required")
    private java.math.BigDecimal newFare;
    
    private String newSeatNumber; // Optional: specific seat preference
}

