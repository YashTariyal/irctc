package com.irctc.train.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class RecommendationRequest {
    private Long userId;
    private String sourceStation;
    private String destinationStation;
    private LocalDate travelDate;
    private LocalTime preferredDepartureTime;
    private List<String> preferredTrainTypes;
    private String seatClass;
}

