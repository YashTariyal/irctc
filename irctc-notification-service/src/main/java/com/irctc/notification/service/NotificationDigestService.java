package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.entity.NotificationPreferences;
import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.repository.SimpleNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating and sending notification digests
 */
@Service
public class NotificationDigestService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationDigestService.class);
    
    @Autowired(required = false)
    private SimpleNotificationRepository notificationRepository;
    
    @Autowired(required = false)
    private NotificationPreferencesService preferencesService;
    
    @Autowired(required = false)
    private NotificationOrchestratorService orchestratorService;
    
    /**
     * Generate and send daily digest
     */
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    @Transactional
    public void sendDailyDigests() {
        logger.info("Generating daily notification digests");
        
        // Get all users with digest enabled
        // In production, query NotificationPreferences where digestEnabled = true and digestFrequency = 'DAILY'
        
        // For each user, collect notifications from yesterday
        // Generate digest message
        // Send via preferred channel
        
        logger.info("Daily digests processed");
    }
    
    /**
     * Generate and send weekly digest
     */
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    @Transactional
    public void sendWeeklyDigests() {
        logger.info("Generating weekly notification digests");
        
        // Similar to daily digest but for weekly period
        
        logger.info("Weekly digests processed");
    }
    
    /**
     * Generate digest for a user
     */
    public void generateDigestForUser(Long userId, String frequency) {
        NotificationPreferences preferences = preferencesService != null ? 
            preferencesService.getPreferences(userId) : null;
        
        if (preferences == null || !preferences.getDigestEnabled()) {
            return;
        }
        
        if (!frequency.equals(preferences.getDigestFrequency())) {
            return;
        }
        
        // Get notifications for the period
        LocalDateTime startTime = frequency.equals("DAILY") ? 
            LocalDate.now().minusDays(1).atStartOfDay() :
            LocalDate.now().minusWeeks(1).atStartOfDay();
        LocalDateTime endTime = LocalDateTime.now();
        
        if (notificationRepository != null) {
            List<SimpleNotification> notifications = notificationRepository
                .findByUserIdAndSentTimeBetween(userId, startTime, endTime);
            
            if (notifications.isEmpty()) {
                return;
            }
            
            // Generate digest message
            String digestMessage = generateDigestMessage(notifications, frequency);
            
            // Send digest
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setNotificationType("DIGEST");
            request.setSubject(frequency.equals("DAILY") ? "Daily Notification Digest" : "Weekly Notification Digest");
            request.setMessage(digestMessage);
            request.setChannel("EMAIL"); // Default to email for digests
            
            if (orchestratorService != null) {
                orchestratorService.sendNotification(request);
            }
        }
    }
    
    /**
     * Generate digest message from notifications
     */
    private String generateDigestMessage(List<SimpleNotification> notifications, String frequency) {
        StringBuilder message = new StringBuilder();
        message.append("Here's your ").append(frequency.toLowerCase()).append(" notification summary:\n\n");
        
        // Group by type
        Map<String, List<SimpleNotification>> byType = notifications.stream()
            .collect(Collectors.groupingBy(SimpleNotification::getType));
        
        byType.forEach((type, notifs) -> {
            message.append(type).append(": ").append(notifs.size()).append(" notification(s)\n");
            notifs.forEach(notif -> {
                message.append("  - ").append(notif.getSubject()).append("\n");
            });
            message.append("\n");
        });
        
        message.append("Total: ").append(notifications.size()).append(" notifications");
        
        return message.toString();
    }
}

