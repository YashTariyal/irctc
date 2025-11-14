package com.irctc.notification.controller;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for push notifications
 */
@RestController
@RequestMapping("/api/notifications/push")
public class PushNotificationController {
    
    @Autowired(required = false)
    private PushNotificationService pushNotificationService;
    
    /**
     * Send push notification
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendPushNotification(@RequestBody NotificationRequest request) {
        if (pushNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationResponse response = pushNotificationService.sendPushNotification(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Register device token
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerDevice(
            @RequestParam Long userId,
            @RequestParam String token,
            @RequestParam String platform,
            @RequestParam(required = false) String deviceId) {
        
        if (pushNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        pushNotificationService.registerDeviceToken(userId, token, platform, deviceId);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Device registered"));
    }
    
    /**
     * Unregister device token
     */
    @DeleteMapping("/unregister")
    public ResponseEntity<Map<String, String>> unregisterDevice(@RequestParam String token) {
        if (pushNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        pushNotificationService.unregisterDeviceToken(token);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Device unregistered"));
    }
}

