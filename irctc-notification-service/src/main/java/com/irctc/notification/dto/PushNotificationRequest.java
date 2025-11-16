package com.irctc.notification.dto;

import lombok.Data;
import java.util.Map;

/**
 * DTO for push notification request with rich content support
 */
@Data
public class PushNotificationRequest {
    private Long userId;
    private String title;
    private String body;
    private String imageUrl; // Image URL for rich notifications
    private String iconUrl; // Icon URL
    private String sound; // Sound file name
    private String clickAction; // Deep link URL
    private Map<String, String> data; // Custom data payload
    private Map<String, String> actions; // Action buttons (Android)
    private String priority; // high, normal
    private String channelId; // Android notification channel
    private String badge; // iOS badge count
    private Boolean silent; // Silent notification
    private Long scheduledTime; // Unix timestamp for scheduling
    private String notificationType; // BOOKING, REMINDER, etc.
}

