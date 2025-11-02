package com.irctc.notification.controller;

import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.service.SimpleNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class SimpleNotificationController {

    @Autowired
    private SimpleNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<SimpleNotification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleNotification> getNotificationById(@PathVariable Long id) {
        // Service will throw EntityNotFoundException if not found
        SimpleNotification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SimpleNotification>> getNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SimpleNotification>> getNotificationsByType(@PathVariable String type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<SimpleNotification>> getRecentNotificationsByUserId(
            @PathVariable Long userId,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(notificationService.getRecentNotificationsByUserId(userId, limit));
    }

    @PostMapping
    public ResponseEntity<SimpleNotification> createNotification(@RequestBody SimpleNotification notification) {
        SimpleNotification newNotification = notificationService.createNotification(notification);
        return ResponseEntity.ok(newNotification);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleNotification> updateNotification(@PathVariable Long id, @RequestBody SimpleNotification notification) {
        SimpleNotification updatedNotification = notificationService.updateNotification(id, notification);
        return ResponseEntity.ok(updatedNotification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // ===== ADVANCED NOTIFICATION APIs =====
    
    @PostMapping("/send/email")
    public ResponseEntity<SimpleNotification> sendEmailNotification(@RequestBody Map<String, Object> emailData) {
        try {
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(Long.valueOf(emailData.get("userId").toString()));
            notification.setType("EMAIL");
            notification.setSubject(emailData.get("title").toString());
            notification.setMessage(emailData.get("message").toString());
            notification.setStatus("SENT");
            
            SimpleNotification sentNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(sentNotification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/send/sms")
    public ResponseEntity<SimpleNotification> sendSmsNotification(@RequestBody Map<String, Object> smsData) {
        try {
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(Long.valueOf(smsData.get("userId").toString()));
            notification.setType("SMS");
            notification.setSubject("SMS Notification");
            notification.setMessage(smsData.get("message").toString());
            notification.setStatus("SENT");
            
            SimpleNotification sentNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(sentNotification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/send/push")
    public ResponseEntity<SimpleNotification> sendPushNotification(@RequestBody Map<String, Object> pushData) {
        try {
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(Long.valueOf(pushData.get("userId").toString()));
            notification.setType("PUSH");
            notification.setSubject(pushData.get("title").toString());
            notification.setMessage(pushData.get("message").toString());
            notification.setStatus("SENT");
            
            SimpleNotification sentNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(sentNotification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<SimpleNotification>> getUnreadNotificationsByUser(@PathVariable Long userId) {
        List<SimpleNotification> notifications = notificationService.getNotificationsByUserId(userId).stream()
                .filter(notification -> "UNREAD".equals(notification.getStatus()))
                .toList();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/read")
    public ResponseEntity<List<SimpleNotification>> getReadNotificationsByUser(@PathVariable Long userId) {
        List<SimpleNotification> notifications = notificationService.getNotificationsByUserId(userId).stream()
                .filter(notification -> "READ".equals(notification.getStatus()))
                .toList();
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SimpleNotification>> getNotificationsByStatus(@PathVariable String status) {
        List<SimpleNotification> notifications = notificationService.getAllNotifications().stream()
                .filter(notification -> status.equalsIgnoreCase(notification.getStatus()))
                .toList();
        return ResponseEntity.ok(notifications);
    }
    
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<SimpleNotification> markAsRead(@PathVariable Long id) {
        SimpleNotification notification = notificationService.getNotificationById(id);
        notification.setStatus("READ");
        SimpleNotification updatedNotification = notificationService.updateNotification(id, notification);
        return ResponseEntity.ok(updatedNotification);
    }
    
    @PutMapping("/{id}/mark-unread")
    public ResponseEntity<SimpleNotification> markAsUnread(@PathVariable Long id) {
        SimpleNotification notification = notificationService.getNotificationById(id);
        notification.setStatus("UNREAD");
        SimpleNotification updatedNotification = notificationService.updateNotification(id, notification);
        return ResponseEntity.ok(updatedNotification);
    }
    
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Void> clearAllNotificationsForUser(@PathVariable Long userId) {
        List<SimpleNotification> notifications = notificationService.getNotificationsByUserId(userId);
        for (SimpleNotification notification : notifications) {
            notificationService.deleteNotification(notification.getId());
        }
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationStatsForUser(@PathVariable Long userId) {
        List<SimpleNotification> notifications = notificationService.getNotificationsByUserId(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notifications.size());
        stats.put("unread", notifications.stream().filter(n -> "UNREAD".equals(n.getStatus())).count());
        stats.put("read", notifications.stream().filter(n -> "READ".equals(n.getStatus())).count());
        stats.put("sent", notifications.stream().filter(n -> "SENT".equals(n.getStatus())).count());
        
        return ResponseEntity.ok(stats);
    }
}
