package com.irctc.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for notification response
 */
@Data
public class NotificationResponse {
    private String notificationId;
    private String channel;
    private String status; // SUCCESS, FAILED, PENDING
    private String messageId; // External service message ID
    private LocalDateTime sentTime;
    private String errorMessage; // If status is FAILED
    private Map<String, Object> metadata;
}

