package com.irctc.booking.dto.analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TravelTimelineEntry {
    private String pnrNumber;
    private String trainName;
    private String route;
    private LocalDateTime bookingTime;
    private String status;
    private BigDecimal fare;
}

