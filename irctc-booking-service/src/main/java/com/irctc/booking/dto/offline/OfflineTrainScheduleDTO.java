package com.irctc.booking.dto.offline;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Minimal train schedule payload for offline search.
 */
@Data
public class OfflineTrainScheduleDTO {
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private String trainType;
    private String trainClass;
    private Integer availableSeats;
    private Double baseFare;
    private LocalDateTime snapshotTime;
}

