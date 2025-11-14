package com.irctc.notification.controller;

import com.irctc.notification.entity.NotificationPreferences;
import com.irctc.notification.service.NotificationPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

/**
 * Controller for notification preferences
 */
@RestController
@RequestMapping("/api/notifications/preferences")
public class NotificationPreferencesController {
    
    @Autowired(required = false)
    private NotificationPreferencesService preferencesService;
    
    /**
     * Get user notification preferences
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationPreferences> getPreferences(@PathVariable Long userId) {
        if (preferencesService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationPreferences preferences = preferencesService.getPreferences(userId);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Update user notification preferences
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<NotificationPreferences> updatePreferences(
            @PathVariable Long userId,
            @RequestBody NotificationPreferences preferences) {
        
        if (preferencesService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationPreferences updated = preferencesService.updatePreferences(userId, preferences);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Set quiet hours
     */
    @PostMapping("/quiet-hours")
    public ResponseEntity<NotificationPreferences> setQuietHours(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end) {
        
        if (preferencesService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationPreferences preferences = preferencesService.setQuietHours(userId, start, end);
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Disable quiet hours
     */
    @DeleteMapping("/quiet-hours")
    public ResponseEntity<NotificationPreferences> disableQuietHours(@RequestParam Long userId) {
        if (preferencesService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationPreferences preferences = preferencesService.disableQuietHours(userId);
        return ResponseEntity.ok(preferences);
    }
}

