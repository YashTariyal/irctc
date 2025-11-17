package com.irctc.booking.dto.offline;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Snapshot of a booking that can be cached on-device for offline ticket viewing.
 */
@Data
public class OfflineTicketDTO {
    private Long bookingId;
    private String pnrNumber;
    private Long trainId;
    private String status;
    private BigDecimal totalFare;
    private LocalDateTime bookingTime;
    private LocalDateTime lastUpdated;
    private List<OfflinePassengerDTO> passengers;
}

