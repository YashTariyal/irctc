package com.irctc.user.controller;

import com.irctc.user.eventtracking.EventProductionLog;
import com.irctc.user.eventtracking.EventProductionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event Tracking Controller for User Service
 * Provides endpoints for monitoring event production
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/event-tracking")
public class EventTrackingController {
    
    @Autowired
    private EventProductionLogRepository productionLogRepository;
    
    /**
     * Get production logs by status
     */
    @GetMapping("/production/status/{status}")
    public ResponseEntity<List<EventProductionLog>> getProductionLogsByStatus(
            @PathVariable String status) {
        try {
            EventProductionLog.ProductionStatus prodStatus = 
                EventProductionLog.ProductionStatus.valueOf(status.toUpperCase());
            List<EventProductionLog> logs = productionLogRepository.findByStatusOrderByCreatedAtAsc(prodStatus);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get production log by event ID
     */
    @GetMapping("/production/event/{eventId}")
    public ResponseEntity<EventProductionLog> getProductionLogByEventId(
            @PathVariable String eventId) {
        Optional<EventProductionLog> log = productionLogRepository.findByEventId(eventId);
        return log.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get production logs by topic
     */
    @GetMapping("/production/topic/{topic}")
    public ResponseEntity<List<EventProductionLog>> getProductionLogsByTopic(
            @PathVariable String topic) {
        List<EventProductionLog> logs = productionLogRepository.findByTopicAndStatusOrderByCreatedAtAsc(topic, null);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get all production logs
     */
    @GetMapping("/production/all")
    public ResponseEntity<List<EventProductionLog>> getAllProductionLogs() {
        List<EventProductionLog> logs = productionLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get event tracking statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventTrackingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Map<String, Long> production = new HashMap<>();
        production.put("PENDING", productionLogRepository.countByStatus(
            EventProductionLog.ProductionStatus.PENDING));
        production.put("PUBLISHING", productionLogRepository.countByStatus(
            EventProductionLog.ProductionStatus.PUBLISHING));
        production.put("PUBLISHED", productionLogRepository.countByStatus(
            EventProductionLog.ProductionStatus.PUBLISHED));
        production.put("FAILED", productionLogRepository.countByStatus(
            EventProductionLog.ProductionStatus.FAILED));
        
        stats.put("production", production);
        
        return ResponseEntity.ok(stats);
    }
}

