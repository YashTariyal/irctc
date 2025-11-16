package com.irctc.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for push notification response
 */
@Data
public class PushNotificationResponse {
    private String notificationId;
    private String messageId; // FCM message ID
    private String status; // SUCCESS, FAILED, PENDING
    private String errorMessage;
    private LocalDateTime sentTime;
    private Integer successCount; // Number of devices notified successfully
    private Integer failureCount; // Number of devices that failed
    private Map<String, Object> metadata; // Additional metadata
}

