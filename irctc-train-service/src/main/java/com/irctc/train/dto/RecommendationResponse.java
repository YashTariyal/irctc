package com.irctc.train.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecommendationResponse {
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double durationHours;
    private Integer distance;
    private Double availabilityScore;
    private BigDecimal predictedFare;
    private String recommendationReason;
}

