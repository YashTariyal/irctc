package com.irctc.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for data export response
 */
@Data
public class DataExportResponse {
    private Long id;
    private Long userId;
    private String requestId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String fileUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private String dataCategories;
}

