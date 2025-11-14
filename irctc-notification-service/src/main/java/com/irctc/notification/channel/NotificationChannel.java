package com.irctc.notification.channel;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;

/**
 * Interface for notification channels (Email, SMS, WhatsApp, Push)
 */
public interface NotificationChannel {
    
    /**
     * Get the channel name
     */
    String getChannelName();
    
    /**
     * Check if channel is enabled
     */
    boolean isEnabled();
    
    /**
     * Send notification
     */
    NotificationResponse send(NotificationRequest request);
    
    /**
     * Check if channel supports the notification type
     */
    boolean supportsNotificationType(String notificationType);
}

