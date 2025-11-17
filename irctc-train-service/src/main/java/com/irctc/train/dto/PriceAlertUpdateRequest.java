package com.irctc.train.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PriceAlertUpdateRequest {
    private String alertType;
    private String notificationChannel;

    @DecimalMin(value = "0.0", inclusive = false, message = "Target price must be positive")
    private BigDecimal targetPrice;

    private Integer minAvailability;
    private LocalDate travelDate;

    @Size(max = 50)
    private String trainNumber;

    private String sourceStation;
    private String destinationStation;
    private String recurrence;
    private String status;
}

