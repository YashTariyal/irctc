package com.irctc.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for route change request (source/destination change)
 */
@Data
public class RouteChangeRequest extends BookingModificationRequest {
    @NotBlank(message = "New source station is required")
    private String newSourceStation;
    
    @NotBlank(message = "New destination station is required")
    private String newDestinationStation;
    
    private Long newTrainId; // Optional: if changing to a different train
    
    @NotNull(message = "New fare is required")
    private java.math.BigDecimal newFare;
}

