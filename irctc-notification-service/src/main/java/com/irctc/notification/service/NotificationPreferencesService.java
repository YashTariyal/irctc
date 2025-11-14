package com.irctc.notification.service;

import com.irctc.notification.entity.NotificationPreferences;
import com.irctc.notification.repository.NotificationPreferencesRepository;
import com.irctc.notification.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;

/**
 * Service for managing notification preferences
 */
@Service
public class NotificationPreferencesService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationPreferencesService.class);
    
    @Autowired
    private NotificationPreferencesRepository preferencesRepository;
    
    /**
     * Get preferences for user
     */
    public NotificationPreferences getPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultPreferences(userId));
    }
    
    /**
     * Update preferences
     */
    @Transactional
    public NotificationPreferences updatePreferences(Long userId, NotificationPreferences preferences) {
        NotificationPreferences existing = getPreferences(userId);
        
        // Update channel preferences
        if (preferences.getEmailEnabled() != null) {
            existing.setEmailEnabled(preferences.getEmailEnabled());
        }
        if (preferences.getSmsEnabled() != null) {
            existing.setSmsEnabled(preferences.getSmsEnabled());
        }
        if (preferences.getWhatsappEnabled() != null) {
            existing.setWhatsappEnabled(preferences.getWhatsappEnabled());
        }
        if (preferences.getPushEnabled() != null) {
            existing.setPushEnabled(preferences.getPushEnabled());
        }
        
        // Update notification type preferences
        if (preferences.getBookingConfirmed() != null) {
            existing.setBookingConfirmed(preferences.getBookingConfirmed());
        }
        if (preferences.getPaymentSuccess() != null) {
            existing.setPaymentSuccess(preferences.getPaymentSuccess());
        }
        if (preferences.getBookingReminder() != null) {
            existing.setBookingReminder(preferences.getBookingReminder());
        }
        if (preferences.getCancellation() != null) {
            existing.setCancellation(preferences.getCancellation());
        }
        if (preferences.getModification() != null) {
            existing.setModification(preferences.getModification());
        }
        
        // Update quiet hours
        if (preferences.getQuietHoursEnabled() != null) {
            existing.setQuietHoursEnabled(preferences.getQuietHoursEnabled());
        }
        if (preferences.getQuietHoursStart() != null) {
            existing.setQuietHoursStart(preferences.getQuietHoursStart());
        }
        if (preferences.getQuietHoursEnd() != null) {
            existing.setQuietHoursEnd(preferences.getQuietHoursEnd());
        }
        
        // Update digest
        if (preferences.getDigestEnabled() != null) {
            existing.setDigestEnabled(preferences.getDigestEnabled());
        }
        if (preferences.getDigestFrequency() != null) {
            existing.setDigestFrequency(preferences.getDigestFrequency());
        }
        
        if (preferences.getChannelPreferences() != null) {
            existing.setChannelPreferences(preferences.getChannelPreferences());
        }
        
        return preferencesRepository.save(existing);
    }
    
    /**
     * Set quiet hours
     */
    @Transactional
    public NotificationPreferences setQuietHours(Long userId, LocalTime start, LocalTime end) {
        NotificationPreferences preferences = getPreferences(userId);
        preferences.setQuietHoursEnabled(true);
        preferences.setQuietHoursStart(start);
        preferences.setQuietHoursEnd(end);
        return preferencesRepository.save(preferences);
    }
    
    /**
     * Disable quiet hours
     */
    @Transactional
    public NotificationPreferences disableQuietHours(Long userId) {
        NotificationPreferences preferences = getPreferences(userId);
        preferences.setQuietHoursEnabled(false);
        return preferencesRepository.save(preferences);
    }
    
    /**
     * Check if notification should be sent based on preferences
     */
    public boolean shouldSendNotification(Long userId, String notificationType, String channel) {
        NotificationPreferences preferences = getPreferences(userId);
        
        // Check if notification type is enabled
        boolean typeEnabled = switch (notificationType) {
            case "BOOKING_CONFIRMED" -> preferences.getBookingConfirmed();
            case "PAYMENT_SUCCESS" -> preferences.getPaymentSuccess();
            case "BOOKING_REMINDER" -> preferences.getBookingReminder();
            case "CANCELLATION" -> preferences.getCancellation();
            case "MODIFICATION" -> preferences.getModification();
            default -> true;
        };
        
        if (!typeEnabled) {
            logger.debug("Notification type {} disabled for user {}", notificationType, userId);
            return false;
        }
        
        // Check if channel is enabled
        boolean channelEnabled = switch (channel) {
            case "EMAIL" -> preferences.getEmailEnabled();
            case "SMS" -> preferences.getSmsEnabled();
            case "WHATSAPP" -> preferences.getWhatsappEnabled();
            case "PUSH" -> preferences.getPushEnabled();
            default -> true;
        };
        
        if (!channelEnabled) {
            logger.debug("Channel {} disabled for user {}", channel, userId);
            return false;
        }
        
        // Check quiet hours
        if (preferences.getQuietHoursEnabled() && isQuietHours()) {
            LocalTime now = LocalTime.now();
            LocalTime start = preferences.getQuietHoursStart();
            LocalTime end = preferences.getQuietHoursEnd();
            
            if (start != null && end != null) {
                if (start.isBefore(end)) {
                    // Same day quiet hours (e.g., 22:00 to 08:00 next day)
                    if (now.isAfter(start) || now.isBefore(end)) {
                        logger.debug("Quiet hours active for user {}", userId);
                        return false;
                    }
                } else {
                    // Overnight quiet hours (e.g., 22:00 to 08:00)
                    if (now.isAfter(start) || now.isBefore(end)) {
                        logger.debug("Quiet hours active for user {}", userId);
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check if currently in quiet hours
     */
    private boolean isQuietHours() {
        // This is a simplified check
        // In production, consider timezone
        return false;
    }
    
    /**
     * Create default preferences
     */
    private NotificationPreferences createDefaultPreferences(Long userId) {
        NotificationPreferences preferences = new NotificationPreferences();
        preferences.setUserId(userId);
        
        if (TenantContext.hasTenant()) {
            preferences.setTenantId(TenantContext.getTenantId());
        }
        
        return preferencesRepository.save(preferences);
    }
}

