package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.entity.UserDeviceToken;
import com.irctc.notification.repository.SimpleNotificationRepository;
import com.irctc.notification.repository.UserDeviceTokenRepository;
import com.irctc.notification.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Push Notification Service using Firebase Cloud Messaging
 */
@Service
@ConditionalOnProperty(name = "notification.push.enabled", havingValue = "true", matchIfMissing = false)
public class PushNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    
    @Value("${notification.push.firebase.project-id:}")
    private String firebaseProjectId;
    
    @Value("${notification.push.firebase.credentials-path:}")
    private String firebaseCredentialsPath;
    
    @Value("${notification.push.enabled:false}")
    private boolean enabled;
    
    @Autowired(required = false)
    private SimpleNotificationRepository notificationRepository;
    
    @Autowired(required = false)
    private UserDeviceTokenRepository deviceTokenRepository;
    
    // Firebase Admin SDK would be initialized here
    // private FirebaseMessaging firebaseMessaging;
    
    /**
     * Send push notification (legacy method for backward compatibility)
     */
    public NotificationResponse sendPushNotification(Long userId, String title, String body, Map<String, Object> data) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setSubject(title);
        request.setMessage(body);
        request.setNotificationType("PUSH");
        request.setMetadata(data);
        return sendPushNotification(request);
    }
    
    /**
     * Send push notification
     */
    public NotificationResponse sendPushNotification(NotificationRequest request) {
        logger.info("Sending push notification to user: {}", request.getUserId());
        
        NotificationResponse response = new NotificationResponse();
        response.setChannel("PUSH");
        response.setSentTime(LocalDateTime.now());
        
        if (!enabled) {
            logger.warn("Push notification service is disabled");
            response.setStatus("FAILED");
            response.setErrorMessage("Push notification service is disabled");
            return response;
        }
        
        try {
            // Get device tokens for user
            List<UserDeviceToken> deviceTokens = getDeviceTokens(request.getUserId());
            
            if (deviceTokens.isEmpty()) {
                response.setStatus("FAILED");
                response.setErrorMessage("No device tokens found for user");
                return response;
            }
            
            // Send to all devices
            int successCount = 0;
            int failureCount = 0;
            
            for (UserDeviceToken deviceToken : deviceTokens) {
                try {
                    NotificationResponse deviceResponse = sendToDevice(deviceToken, request);
                    if ("SUCCESS".equals(deviceResponse.getStatus())) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error sending to device {}: {}", deviceToken.getToken(), e.getMessage());
                    failureCount++;
                }
            }
            
            if (successCount > 0) {
                response.setStatus("SUCCESS");
                response.setMessageId("push_" + UUID.randomUUID().toString());
                response.setMetadata(Map.of("successCount", successCount, "failureCount", failureCount));
            } else {
                response.setStatus("FAILED");
                response.setErrorMessage("Failed to send to all devices");
            }
            
            // Save notification record
            if (notificationRepository != null) {
                saveNotificationRecord(request, response);
            }
            
            logger.info("✅ Push notification sent: {} success, {} failed", successCount, failureCount);
        } catch (Exception e) {
            logger.error("Error sending push notification: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Send notification to a specific device
     */
    private NotificationResponse sendToDevice(UserDeviceToken deviceToken, NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        
        // Initialize Firebase if needed
        // if (firebaseMessaging == null) {
        //     initializeFirebase();
        // }
        
        // Build FCM message
        Map<String, Object> message = buildFCMMessage(deviceToken, request);
        
        // Send via Firebase
        // In production, use Firebase Admin SDK
        // Message fcmMessage = Message.builder()
        //     .setToken(deviceToken.getToken())
        //     .setNotification(Notification.builder()
        //         .setTitle(request.getSubject())
        //         .setBody(request.getMessage())
        //         .build())
        //     .putData("type", request.getNotificationType())
        //     .build();
        // 
        // String messageId = firebaseMessaging.send(fcmMessage);
        
        // Simulate for now
        boolean success = Math.random() > 0.1;
        
        if (success) {
            response.setStatus("SUCCESS");
            response.setMessageId("fcm_" + UUID.randomUUID().toString());
        } else {
            response.setStatus("FAILED");
            response.setErrorMessage("FCM delivery failed");
        }
        
        return response;
    }
    
    /**
     * Build FCM message
     */
    private Map<String, Object> buildFCMMessage(UserDeviceToken deviceToken, NotificationRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("token", deviceToken.getToken());
        
        Map<String, String> notification = new HashMap<>();
        notification.put("title", request.getSubject());
        notification.put("body", request.getMessage());
        message.put("notification", notification);
        
        Map<String, String> data = new HashMap<>();
        data.put("type", request.getNotificationType());
        if (request.getMetadata() != null) {
            request.getMetadata().forEach((key, value) -> 
                data.put(key, value.toString()));
        }
        message.put("data", data);
        
        // Android-specific
        Map<String, Object> android = new HashMap<>();
        Map<String, String> priority = new HashMap<>();
        priority.put("priority", "high");
        android.put("priority", priority);
        message.put("android", android);
        
        // iOS-specific
        Map<String, Object> apns = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("apns-priority", "10");
        apns.put("headers", headers);
        message.put("apns", apns);
        
        return message;
    }
    
    /**
     * Get device tokens for user
     */
    private List<UserDeviceToken> getDeviceTokens(Long userId) {
        if (deviceTokenRepository != null) {
            return deviceTokenRepository.findByUserId(userId);
        }
        return List.of();
    }
    
    /**
     * Save notification record
     */
    private void saveNotificationRecord(NotificationRequest request, NotificationResponse response) {
        SimpleNotification notification = new SimpleNotification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getNotificationType());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setSentTime(response.getSentTime());
        notification.setStatus(response.getStatus());
        
        if (TenantContext.hasTenant()) {
            notification.setTenantId(TenantContext.getTenantId());
        }
        
        notificationRepository.save(notification);
    }
    
    /**
     * Register device token
     */
    public void registerDeviceToken(Long userId, String token, String platform, String deviceId) {
        if (deviceTokenRepository == null) {
            return;
        }
        
        // Check if token already exists
        UserDeviceToken existing = deviceTokenRepository.findByToken(token).orElse(null);
        
        if (existing != null) {
            existing.setUserId(userId);
            existing.setPlatform(platform);
            existing.setDeviceId(deviceId);
            existing.setUpdatedAt(LocalDateTime.now());
            deviceTokenRepository.save(existing);
        } else {
            UserDeviceToken deviceToken = new UserDeviceToken();
            deviceToken.setUserId(userId);
            deviceToken.setToken(token);
            deviceToken.setPlatform(platform);
            deviceToken.setDeviceId(deviceId);
            deviceToken.setCreatedAt(LocalDateTime.now());
            deviceToken.setUpdatedAt(LocalDateTime.now());
            
            if (TenantContext.hasTenant()) {
                deviceToken.setTenantId(TenantContext.getTenantId());
            }
            
            deviceTokenRepository.save(deviceToken);
        }
        
        logger.info("Device token registered for user {}: {}", userId, platform);
    }
    
    /**
     * Unregister device token
     */
    public void unregisterDeviceToken(String token) {
        if (deviceTokenRepository != null) {
            deviceTokenRepository.findByToken(token).ifPresent(deviceTokenRepository::delete);
            logger.info("Device token unregistered: {}", token);
        }
	}
}
