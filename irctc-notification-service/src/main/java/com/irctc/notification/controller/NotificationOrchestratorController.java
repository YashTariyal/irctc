package com.irctc.notification.controller;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.service.NotificationOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Main controller for sending notifications
 * Routes to appropriate channels based on preferences
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationOrchestratorController {
    
    @Autowired(required = false)
    private NotificationOrchestratorService orchestratorService;
    
    /**
     * Send notification (auto-routes based on preferences)
     */
    @PostMapping
    public ResponseEntity<List<NotificationResponse>> sendNotification(@RequestBody NotificationRequest request) {
        if (orchestratorService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<NotificationResponse> responses = orchestratorService.sendNotification(request);
        return ResponseEntity.ok(responses);
    }
}

