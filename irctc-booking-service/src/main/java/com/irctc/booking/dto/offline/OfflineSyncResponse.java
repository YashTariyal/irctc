package com.irctc.booking.dto.offline;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Response produced when generating offline bundles.
 */
@Data
public class OfflineSyncResponse {
    private List<OfflineTicketDTO> tickets = Collections.emptyList();
    private List<OfflineTrainScheduleDTO> trainSchedules = Collections.emptyList();
    private List<OfflineActionResponse> pendingActions = Collections.emptyList();
    private SyncMetadata metadata = new SyncMetadata();

    @Data
    public static class SyncMetadata {
        private LocalDateTime generatedAt = LocalDateTime.now();
        private LocalDateTime lastSyncTime;
        private Integer ticketCount;
        private Integer scheduleCount;
        private Integer pendingActionCount;
        private boolean incremental;
    }
}

