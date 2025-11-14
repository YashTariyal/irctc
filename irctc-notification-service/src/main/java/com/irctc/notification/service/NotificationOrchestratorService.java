package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.entity.NotificationPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator service that routes notifications to appropriate channels
 * based on user preferences
 */
@Service
public class NotificationOrchestratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationOrchestratorService.class);
    
    @Autowired(required = false)
    private NotificationPreferencesService preferencesService;
    
    @Autowired(required = false)
    private WhatsAppService whatsAppService;
    
    @Autowired(required = false)
    private PushNotificationService pushNotificationService;
    
    @Autowired(required = false)
    private EmailService emailService;
    
    @Autowired(required = false)
    private SmsService smsService;
    
    /**
     * Send notification using appropriate channels based on preferences
     */
    public List<NotificationResponse> sendNotification(NotificationRequest request) {
        logger.info("Sending notification to user {}: type={}, channel={}", 
            request.getUserId(), request.getNotificationType(), request.getChannel());
        
        List<NotificationResponse> responses = new ArrayList<>();
        
        // Check preferences
        if (preferencesService != null) {
            // If specific channel requested, check if enabled
            if (request.getChannel() != null) {
                if (!preferencesService.shouldSendNotification(
                    request.getUserId(), 
                    request.getNotificationType(), 
                    request.getChannel())) {
                    logger.debug("Notification blocked by preferences: user={}, type={}, channel={}",
                        request.getUserId(), request.getNotificationType(), request.getChannel());
                    return responses;
                }
                
                // Send to specific channel
                NotificationResponse response = sendToChannel(request, request.getChannel());
                if (response != null) {
                    responses.add(response);
                }
            } else {
                // Send to all enabled channels based on preferences
                NotificationPreferences preferences = preferencesService.getPreferences(request.getUserId());
                
                if (preferences.getEmailEnabled()) {
                    NotificationResponse response = sendToChannel(request, "EMAIL");
                    if (response != null) responses.add(response);
                }
                
                if (preferences.getSmsEnabled()) {
                    NotificationResponse response = sendToChannel(request, "SMS");
                    if (response != null) responses.add(response);
                }
                
                if (preferences.getWhatsappEnabled()) {
                    NotificationResponse response = sendToChannel(request, "WHATSAPP");
                    if (response != null) responses.add(response);
                }
                
                if (preferences.getPushEnabled()) {
                    NotificationResponse response = sendToChannel(request, "PUSH");
                    if (response != null) responses.add(response);
                }
            }
        } else {
            // No preferences service, send to requested channel or default
            String channel = request.getChannel() != null ? request.getChannel() : "EMAIL";
            NotificationResponse response = sendToChannel(request, channel);
            if (response != null) {
                responses.add(response);
            }
        }
        
        logger.info("Notification sent via {} channels", responses.size());
        return responses;
    }
    
    /**
     * Send notification to specific channel
     */
    private NotificationResponse sendToChannel(NotificationRequest request, String channel) {
        try {
            return switch (channel.toUpperCase()) {
                case "WHATSAPP" -> whatsAppService != null ? 
                    whatsAppService.sendMessage(request) : null;
                case "PUSH" -> pushNotificationService != null ? 
                    pushNotificationService.sendPushNotification(request) : null;
                case "EMAIL" -> emailService != null ? 
                    emailService.sendEmail(request) : null;
                case "SMS" -> smsService != null ? 
                    smsService.sendSms(request) : null;
                default -> {
                    logger.warn("Unknown channel: {}", channel);
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Error sending to channel {}: {}", channel, e.getMessage(), e);
            return null;
        }
    }
}

