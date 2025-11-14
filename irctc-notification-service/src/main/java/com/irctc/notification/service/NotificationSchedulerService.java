package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.entity.ScheduledNotification;
import com.irctc.notification.repository.ScheduledNotificationRepository;
import com.irctc.notification.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for scheduling notifications
 */
@Service
public class NotificationSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    
    @Autowired
    private ScheduledNotificationRepository scheduledNotificationRepository;
    
    @Autowired(required = false)
    private NotificationOrchestratorService orchestratorService;
    
    /**
     * Schedule a notification
     */
    @Transactional
    public ScheduledNotification scheduleNotification(NotificationRequest request) {
        if (request.getScheduledTime() == null) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        
        ScheduledNotification scheduled = new ScheduledNotification();
        scheduled.setUserId(request.getUserId());
        scheduled.setNotificationType(request.getNotificationType());
        scheduled.setChannel(request.getChannel());
        scheduled.setRecipient(request.getRecipient());
        scheduled.setSubject(request.getSubject());
        scheduled.setMessage(request.getMessage());
        scheduled.setTemplateId(request.getTemplateId());
        scheduled.setTemplateVariables(request.getTemplateVariables() != null ? 
            request.getTemplateVariables().toString() : null);
        scheduled.setScheduledTime(LocalDateTime.ofEpochSecond(
            request.getScheduledTime() / 1000, 0, 
            java.time.ZoneOffset.UTC));
        scheduled.setStatus("SCHEDULED");
        scheduled.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        
        if (TenantContext.hasTenant()) {
            scheduled.setTenantId(TenantContext.getTenantId());
        }
        
        ScheduledNotification saved = scheduledNotificationRepository.save(scheduled);
        logger.info("Notification scheduled: ID {}, Time {}", saved.getId(), saved.getScheduledTime());
        
        return saved;
    }
    
    /**
     * Process scheduled notifications (runs every minute)
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processScheduledNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledNotification> dueNotifications = scheduledNotificationRepository
            .findByStatusAndScheduledTimeBefore("SCHEDULED", now);
        
        if (dueNotifications.isEmpty()) {
            return;
        }
        
        logger.info("Processing {} scheduled notifications", dueNotifications.size());
        
        for (ScheduledNotification scheduled : dueNotifications) {
            try {
                // Mark as processing
                scheduled.setStatus("PROCESSING");
                scheduledNotificationRepository.save(scheduled);
                
                // Convert to notification request
                NotificationRequest request = convertToRequest(scheduled);
                
                // Send notification
                if (orchestratorService != null) {
                    orchestratorService.sendNotification(request);
                    scheduled.setStatus("SENT");
                    scheduled.setSentTime(LocalDateTime.now());
                } else {
                    scheduled.setStatus("FAILED");
                    scheduled.setErrorMessage("Orchestrator service not available");
                }
                
                scheduledNotificationRepository.save(scheduled);
                logger.info("✅ Scheduled notification sent: ID {}", scheduled.getId());
            } catch (Exception e) {
                logger.error("Error processing scheduled notification {}: {}", scheduled.getId(), e.getMessage(), e);
                scheduled.setStatus("FAILED");
                scheduled.setErrorMessage(e.getMessage());
                scheduledNotificationRepository.save(scheduled);
            }
        }
    }
    
    /**
     * Cancel scheduled notification
     */
    @Transactional
    public void cancelScheduledNotification(Long id) {
        ScheduledNotification scheduled = scheduledNotificationRepository.findById(id)
            .orElseThrow(() -> new com.irctc.notification.exception.EntityNotFoundException("ScheduledNotification", id));
        
        if (!"SCHEDULED".equals(scheduled.getStatus())) {
            throw new IllegalStateException("Cannot cancel notification that is not scheduled");
        }
        
        scheduled.setStatus("CANCELLED");
        scheduledNotificationRepository.save(scheduled);
        logger.info("Scheduled notification cancelled: ID {}", id);
    }
    
    /**
     * Convert scheduled notification to request
     */
    private NotificationRequest convertToRequest(ScheduledNotification scheduled) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(scheduled.getUserId());
        request.setNotificationType(scheduled.getNotificationType());
        request.setChannel(scheduled.getChannel());
        request.setRecipient(scheduled.getRecipient());
        request.setSubject(scheduled.getSubject());
        request.setMessage(scheduled.getMessage());
        request.setTemplateId(scheduled.getTemplateId());
        // Parse template variables if needed
        request.setPriority(scheduled.getPriority());
        return request;
    }
}

