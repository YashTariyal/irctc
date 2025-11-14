package com.irctc.booking.dto;

import com.irctc.booking.entity.SimplePassenger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * DTO for adding/removing passengers from booking
 */
@Data
public class PassengerModificationRequest extends BookingModificationRequest {
    @Valid
    private List<SimplePassenger> passengersToAdd;
    
    private List<Long> passengerIdsToRemove; // Optional: can be empty list
    
    private java.math.BigDecimal additionalFare; // Fare for new passengers
}

