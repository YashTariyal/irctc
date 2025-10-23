package com.irctc.notification.controller;

import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.service.SimpleNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SimpleNotification>> getNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<SimpleNotification>> getNotificationsByType(@PathVariable String type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
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
}
