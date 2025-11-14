package com.irctc.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for booking date change request
 */
@Data
public class DateChangeRequest extends BookingModificationRequest {
    @NotNull(message = "New journey date is required")
    private LocalDateTime newJourneyDate;
    
    private Long newTrainId; // Optional: if changing to a different train
}

