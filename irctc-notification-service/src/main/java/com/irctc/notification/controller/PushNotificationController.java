package com.irctc.notification.controller;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.dto.PushNotificationRequest;
import com.irctc.notification.dto.PushNotificationResponse;
import com.irctc.notification.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for push notifications with rich content support
 */
@RestController
@RequestMapping("/api/notifications/push")
public class PushNotificationController {
    
    @Autowired(required = false)
    private PushNotificationService pushNotificationService;
    
    /**
     * POST /api/notifications/push
     * Send push notification (legacy - uses NotificationRequest)
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
     * POST /api/notifications/push/rich
     * Send rich push notification with images, actions, and deep links
     */
    @PostMapping("/rich")
    public ResponseEntity<PushNotificationResponse> sendRichPushNotification(
            @RequestBody PushNotificationRequest request) {
        if (pushNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        PushNotificationResponse response = pushNotificationService.sendRichPushNotification(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/notifications/push/register
     * Register device token for push notifications
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
     * DELETE /api/notifications/push/unregister
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
    
    /**
     * GET /api/notifications/push/devices/{userId}
     * Get all registered devices for a user
     */
    @GetMapping("/devices/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDevices(@PathVariable Long userId) {
        if (pushNotificationService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<com.irctc.notification.entity.UserDeviceToken> devices = 
            pushNotificationService.getUserDevices(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "devices", devices));
    }
}

