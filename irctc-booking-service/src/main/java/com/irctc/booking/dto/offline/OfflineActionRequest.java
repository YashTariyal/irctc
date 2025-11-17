package com.irctc.booking.dto.offline;

import lombok.Data;

/**
 * Represents an action performed offline that needs to be replayed when connectivity is restored.
 */
@Data
public class OfflineActionRequest {
    private Long userId;
    private Long bookingId;
    private String actionType;
    private String payload; // JSON payload describing the action (seat change, cancellation etc.)
}

