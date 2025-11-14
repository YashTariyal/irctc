package com.irctc.notification.dto;

import lombok.Data;
import java.util.Map;

/**
 * DTO for notification request
 */
@Data
public class NotificationRequest {
    private Long userId;
    private String notificationType; // BOOKING_CONFIRMED, PAYMENT_SUCCESS, etc.
    private String channel; // EMAIL, SMS, WHATSAPP, PUSH
    private String recipient; // Email, phone, device token, etc.
    private String subject;
    private String message;
    private String templateId; // For template-based notifications
    private Map<String, String> templateVariables; // Variables for template
    private Map<String, Object> metadata; // Additional metadata
    private Long scheduledTime; // Unix timestamp for scheduled notifications
    private String priority; // HIGH, MEDIUM, LOW
}

