package com.irctc.notification.controller;

import com.irctc.notification.eventtracking.EventConsumptionLog;
import com.irctc.notification.eventtracking.EventConsumptionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event Tracking Controller for Notification Service
 * Provides endpoints for monitoring event consumption with idempotency tracking
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/event-tracking")
public class EventTrackingController {
    
    @Autowired
    private EventConsumptionLogRepository consumptionLogRepository;
    
    /**
     * Get consumption logs by status
     */
    @GetMapping("/consumption/status/{status}")
    public ResponseEntity<List<EventConsumptionLog>> getConsumptionLogsByStatus(
            @PathVariable String status) {
        try {
            EventConsumptionLog.ConsumptionStatus consStatus = 
                EventConsumptionLog.ConsumptionStatus.valueOf(status.toUpperCase());
            List<EventConsumptionLog> logs = consumptionLogRepository.findByStatusOrderByReceivedAtAsc(consStatus);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get consumption log by event ID
     */
    @GetMapping("/consumption/event/{eventId}")
    public ResponseEntity<EventConsumptionLog> getConsumptionLogByEventId(
            @PathVariable String eventId) {
        Optional<EventConsumptionLog> log = consumptionLogRepository.findByEventId(eventId);
        return log.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all consumption logs
     */
    @GetMapping("/consumption/all")
    public ResponseEntity<List<EventConsumptionLog>> getAllConsumptionLogs() {
        List<EventConsumptionLog> logs = consumptionLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get event tracking statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventTrackingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        Map<String, Long> consumption = new HashMap<>();
        consumption.put("RECEIVED", consumptionLogRepository.countByStatus(
            EventConsumptionLog.ConsumptionStatus.RECEIVED));
        consumption.put("PROCESSING", consumptionLogRepository.countByStatus(
            EventConsumptionLog.ConsumptionStatus.PROCESSING));
        consumption.put("PROCESSED", consumptionLogRepository.countByStatus(
            EventConsumptionLog.ConsumptionStatus.PROCESSED));
        consumption.put("FAILED", consumptionLogRepository.countByStatus(
            EventConsumptionLog.ConsumptionStatus.FAILED));
        
        stats.put("consumption", consumption);
        
        return ResponseEntity.ok(stats);
    }
}

