package com.irctc.booking.dto.offline;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OfflineActionResponse {
    private Long id;
    private Long userId;
    private Long bookingId;
    private String actionType;
    private String status;
    private String failureReason;
    private LocalDateTime queuedAt;
    private LocalDateTime processedAt;
}

