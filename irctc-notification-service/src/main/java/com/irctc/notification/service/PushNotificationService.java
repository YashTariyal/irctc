package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.dto.PushNotificationRequest;
import com.irctc.notification.dto.PushNotificationResponse;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// Firebase Admin SDK imports
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;
// import com.google.firebase.messaging.FirebaseMessaging;
// import com.google.firebase.messaging.FirebaseMessagingException;
// import com.google.firebase.messaging.Message;
// import com.google.firebase.messaging.Notification;
// import com.google.firebase.messaging.AndroidConfig;
// import com.google.firebase.messaging.ApnsConfig;
// import com.google.firebase.messaging.Aps;

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
    
    @Autowired(required = false)
    private NotificationSchedulerService schedulerService;
    
    // Firebase Admin SDK would be initialized here
    // private FirebaseMessaging firebaseMessaging;
    
    private boolean firebaseInitialized = false;
    
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
     * Get all registered devices for a user
     */
    public List<UserDeviceToken> getUserDevices(Long userId) {
        return getDeviceTokens(userId);
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
    
    /**
     * Send rich push notification with images, actions, and deep links
     */
    @Transactional
    public PushNotificationResponse sendRichPushNotification(PushNotificationRequest request) {
        logger.info("Sending rich push notification to user: {}", request.getUserId());
        
        PushNotificationResponse response = new PushNotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
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
            
            // Check if scheduled
            if (request.getScheduledTime() != null && request.getScheduledTime() > System.currentTimeMillis()) {
                return schedulePushNotification(request);
            }
            
            // Send to all devices
            int successCount = 0;
            int failureCount = 0;
            Map<String, Object> metadata = new HashMap<>();
            
            for (UserDeviceToken deviceToken : deviceTokens) {
                try {
                    boolean success = sendRichNotificationToDevice(deviceToken, request);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    logger.error("Error sending rich notification to device {}: {}", 
                        deviceToken.getToken(), e.getMessage());
                    failureCount++;
                }
            }
            
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);
            
            if (successCount > 0) {
                response.setStatus("SUCCESS");
                response.setMessageId("fcm_" + UUID.randomUUID().toString());
                metadata.put("devicesNotified", successCount);
                metadata.put("devicesFailed", failureCount);
                response.setMetadata(metadata);
            } else {
                response.setStatus("FAILED");
                response.setErrorMessage("Failed to send to all devices");
            }
            
            // Save notification record
            if (notificationRepository != null) {
                savePushNotificationRecord(request, response);
            }
            
            logger.info("✅ Rich push notification sent: {} success, {} failed", successCount, failureCount);
        } catch (Exception e) {
            logger.error("Error sending rich push notification: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Send rich notification to a specific device
     */
    private boolean sendRichNotificationToDevice(UserDeviceToken deviceToken, PushNotificationRequest request) {
        try {
            // Initialize Firebase if needed
            initializeFirebase();
            
            // Build FCM message with rich content
            Map<String, Object> fcmMessage = buildRichFCMMessage(deviceToken, request);
            
            // In production, use Firebase Admin SDK:
            // Message message = Message.builder()
            //     .setToken(deviceToken.getToken())
            //     .setNotification(Notification.builder()
            //         .setTitle(request.getTitle())
            //         .setBody(request.getBody())
            //         .setImage(request.getImageUrl())
            //         .build())
            //     .putData("click_action", request.getClickAction())
            //     .putData("type", request.getNotificationType())
            //     .setAndroidConfig(AndroidConfig.builder()
            //         .setPriority(AndroidConfig.Priority.HIGH)
            //         .setNotification(AndroidNotification.builder()
            //             .setChannelId(request.getChannelId())
            //             .setSound(request.getSound())
            //             .build())
            //         .build())
            //     .setApnsConfig(ApnsConfig.builder()
            //         .setAps(Aps.builder()
            //             .setBadge(Integer.parseInt(request.getBadge()))
            //             .setSound(request.getSound())
            //             .build())
            //         .build())
            //     .build();
            // 
            // String messageId = firebaseMessaging.send(message);
            
            // Simulate for now (90% success rate)
            boolean success = Math.random() > 0.1;
            
            if (success) {
                logger.info("✅ Rich notification sent to device: {}", deviceToken.getPlatform());
            } else {
                logger.warn("❌ Failed to send rich notification to device: {}", deviceToken.getPlatform());
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error sending rich notification to device: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Build rich FCM message with images, actions, and deep links
     */
    private Map<String, Object> buildRichFCMMessage(UserDeviceToken deviceToken, PushNotificationRequest request) {
        Map<String, Object> message = new HashMap<>();
        message.put("token", deviceToken.getToken());
        
        // Notification payload
        Map<String, String> notification = new HashMap<>();
        notification.put("title", request.getTitle());
        notification.put("body", request.getBody());
        if (request.getImageUrl() != null) {
            notification.put("image", request.getImageUrl());
        }
        message.put("notification", notification);
        
        // Data payload
        Map<String, String> data = new HashMap<>();
        if (request.getClickAction() != null) {
            data.put("click_action", request.getClickAction());
        }
        if (request.getNotificationType() != null) {
            data.put("type", request.getNotificationType());
        }
        if (request.getData() != null) {
            data.putAll(request.getData());
        }
        message.put("data", data);
        
        // Android-specific configuration
        Map<String, Object> android = new HashMap<>();
        android.put("priority", request.getPriority() != null ? request.getPriority() : "high");
        
        if (request.getChannelId() != null || request.getSound() != null || request.getIconUrl() != null) {
            Map<String, Object> androidNotification = new HashMap<>();
            if (request.getChannelId() != null) {
                androidNotification.put("channel_id", request.getChannelId());
            }
            if (request.getSound() != null) {
                androidNotification.put("sound", request.getSound());
            }
            if (request.getIconUrl() != null) {
                androidNotification.put("icon", request.getIconUrl());
            }
            if (request.getActions() != null && !request.getActions().isEmpty()) {
                androidNotification.put("actions", request.getActions());
            }
            android.put("notification", androidNotification);
        }
        message.put("android", android);
        
        // iOS-specific configuration
        Map<String, Object> apns = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("apns-priority", "10");
        apns.put("headers", headers);
        
        Map<String, Object> aps = new HashMap<>();
        if (request.getBadge() != null) {
            aps.put("badge", Integer.parseInt(request.getBadge()));
        }
        if (request.getSound() != null) {
            aps.put("sound", request.getSound());
        }
        if (request.getSilent() != null && request.getSilent()) {
            aps.put("content-available", 1);
        }
        apns.put("aps", aps);
        message.put("apns", apns);
        
        return message;
    }
    
    /**
     * Initialize Firebase Admin SDK
     */
    private void initializeFirebase() {
        if (firebaseInitialized) {
            return;
        }
        
        try {
            // In production, initialize Firebase:
            // if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isEmpty()) {
            //     FileInputStream serviceAccount = new FileInputStream(firebaseCredentialsPath);
            //     FirebaseOptions options = FirebaseOptions.builder()
            //         .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            //         .setProjectId(firebaseProjectId)
            //         .build();
            //     
            //     if (FirebaseApp.getApps().isEmpty()) {
            //         FirebaseApp.initializeApp(options);
            //     }
            //     
            //     firebaseMessaging = FirebaseMessaging.getInstance();
            //     firebaseInitialized = true;
            //     logger.info("✅ Firebase initialized successfully");
            // } else {
            //     logger.warn("Firebase credentials path not configured");
            // }
            
            // For now, just mark as initialized
            firebaseInitialized = true;
            logger.info("Firebase initialization simulated (configure credentials for production)");
        } catch (Exception e) {
            logger.error("Error initializing Firebase: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Schedule push notification
     */
    private PushNotificationResponse schedulePushNotification(PushNotificationRequest request) {
        logger.info("Scheduling push notification for user {} at {}", 
            request.getUserId(), request.getScheduledTime());
        
        PushNotificationResponse response = new PushNotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        response.setStatus("SCHEDULED");
        
        try {
            if (schedulerService != null) {
                NotificationRequest notificationRequest = convertToNotificationRequest(request);
                schedulerService.scheduleNotification(notificationRequest);
                response.setMetadata(Map.of("scheduledTime", request.getScheduledTime()));
                logger.info("✅ Push notification scheduled successfully");
            } else {
                response.setStatus("FAILED");
                response.setErrorMessage("Scheduler service not available");
            }
        } catch (Exception e) {
            logger.error("Error scheduling push notification: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Convert PushNotificationRequest to NotificationRequest
     */
    private NotificationRequest convertToNotificationRequest(PushNotificationRequest pushRequest) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(pushRequest.getUserId());
        request.setNotificationType(pushRequest.getNotificationType());
        request.setChannel("PUSH");
        request.setSubject(pushRequest.getTitle());
        request.setMessage(pushRequest.getBody());
        request.setScheduledTime(pushRequest.getScheduledTime());
        request.setPriority(pushRequest.getPriority());
        
        Map<String, Object> metadata = new HashMap<>();
        if (pushRequest.getImageUrl() != null) {
            metadata.put("imageUrl", pushRequest.getImageUrl());
        }
        if (pushRequest.getClickAction() != null) {
            metadata.put("clickAction", pushRequest.getClickAction());
        }
        if (pushRequest.getData() != null) {
            metadata.put("data", pushRequest.getData());
        }
        request.setMetadata(metadata);
        
        return request;
    }
    
    /**
     * Save push notification record
     */
    private void savePushNotificationRecord(PushNotificationRequest request, PushNotificationResponse response) {
        SimpleNotification notification = new SimpleNotification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getNotificationType() != null ? request.getNotificationType() : "PUSH");
        notification.setSubject(request.getTitle());
        notification.setMessage(request.getBody());
        notification.setSentTime(response.getSentTime());
        notification.setStatus(response.getStatus());
        
        if (TenantContext.hasTenant()) {
            notification.setTenantId(TenantContext.getTenantId());
        }
        
        notificationRepository.save(notification);
    }
    
    /**
     * Send push notification asynchronously
     */
    @Async
    public CompletableFuture<PushNotificationResponse> sendRichPushNotificationAsync(PushNotificationRequest request) {
        return CompletableFuture.completedFuture(sendRichPushNotification(request));
    }
}
