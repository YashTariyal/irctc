package com.irctc.notification.controller;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.entity.ScheduledNotification;
import com.irctc.notification.service.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for scheduling notifications
 */
@RestController
@RequestMapping("/api/notifications/schedule")
public class NotificationSchedulerController {
    
    @Autowired(required = false)
    private NotificationSchedulerService schedulerService;
    
    /**
     * Schedule a notification
     */
    @PostMapping
    public ResponseEntity<ScheduledNotification> scheduleNotification(@RequestBody NotificationRequest request) {
        if (schedulerService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getScheduledTime() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        ScheduledNotification scheduled = schedulerService.scheduleNotification(request);
        return ResponseEntity.ok(scheduled);
    }
    
    /**
     * Cancel scheduled notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelScheduledNotification(@PathVariable Long id) {
        if (schedulerService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        schedulerService.cancelScheduledNotification(id);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Notification cancelled"));
    }
}

