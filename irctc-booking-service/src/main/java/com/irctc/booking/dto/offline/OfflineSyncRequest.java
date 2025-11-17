package com.irctc.booking.dto.offline;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request payload for generating offline bundles.
 */
@Data
public class OfflineSyncRequest {
    private Long userId;
    private List<String> trainNumbers;
    private List<Long> trainIds;
    private List<String> favoriteRoutes;
    private boolean includeTickets = true;
    private boolean includeSchedules = true;
    private LocalDateTime lastSyncTime;
}

