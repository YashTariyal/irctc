package com.irctc.train.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PriceAlertRequest {
    @NotNull
    private Long userId;

    @Size(max = 255)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 50)
    private String trainNumber;

    @Size(max = 100)
    private String sourceStation;

    @Size(max = 100)
    private String destinationStation;

    private LocalDate travelDate;

    @NotBlank
    private String alertType; // PRICE_DROP, AVAILABILITY

    private String notificationChannel; // PUSH, EMAIL, SMS

    @DecimalMin(value = "0.0", inclusive = false, message = "Target price must be positive")
    private BigDecimal targetPrice;

    private Integer minAvailability;

    private String recurrence = "ONE_TIME";
}

