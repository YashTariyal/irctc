package com.irctc.train.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PriceAlertResponse {
    private Long id;
    private Long userId;
    private String email;
    private String phoneNumber;
    private String trainNumber;
    private String sourceStation;
    private String destinationStation;
    private LocalDate travelDate;
    private String alertType;
    private String notificationChannel;
    private BigDecimal targetPrice;
    private Integer minAvailability;
    private String status;
    private String recurrence;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

