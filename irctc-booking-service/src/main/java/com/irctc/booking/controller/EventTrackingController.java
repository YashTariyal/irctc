package com.irctc.booking.controller;

import com.irctc.booking.eventtracking.EventConsumptionLog;
import com.irctc.booking.eventtracking.EventProductionLog;
import com.irctc.booking.eventtracking.EventConsumptionLogRepository;
import com.irctc.booking.eventtracking.EventProductionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event Tracking Controller
 * Provides endpoints for monitoring event production and consumption
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/event-tracking")
public class EventTrackingController {
    
    @Autowired
    private EventProductionLogRepository productionLogRepository;
    
    @Autowired
    private EventConsumptionLogRepository consumptionLogRepository;
    
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
     * Get production logs by topic
     */
    @GetMapping("/production/topic/{topic}")
    public ResponseEntity<List<EventProductionLog>> getProductionLogsByTopic(
            @PathVariable String topic) {
        List<EventProductionLog> logs = productionLogRepository.findByTopicOrderByCreatedAtDesc(topic);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get failed production events for retry
     */
    @GetMapping("/production/failed")
    public ResponseEntity<List<EventProductionLog>> getFailedProductionEvents() {
        List<EventProductionLog> logs = productionLogRepository.findFailedEventsForRetry();
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get failed consumption events for retry
     */
    @GetMapping("/consumption/failed")
    public ResponseEntity<List<EventConsumptionLog>> getFailedConsumptionEvents() {
        List<EventConsumptionLog> logs = consumptionLogRepository.findFailedEventsForRetry();
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get event tracking statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventTrackingStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Production stats
        Map<String, Long> productionStats = new HashMap<>();
        for (EventProductionLog.ProductionStatus status : EventProductionLog.ProductionStatus.values()) {
            productionStats.put(status.name(), productionLogRepository.countByStatus(status));
        }
        stats.put("production", productionStats);
        
        // Consumption stats
        Map<String, Long> consumptionStats = new HashMap<>();
        for (EventConsumptionLog.ConsumptionStatus status : EventConsumptionLog.ConsumptionStatus.values()) {
            consumptionStats.put(status.name(), consumptionLogRepository.countByStatus(status));
        }
        stats.put("consumption", consumptionStats);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get events by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<Map<String, Object>> getEventsByCorrelationId(
            @PathVariable String correlationId) {
        Map<String, Object> result = new HashMap<>();
        result.put("production", productionLogRepository.findByCorrelationIdOrderByCreatedAtAsc(correlationId));
        result.put("consumption", consumptionLogRepository.findByCorrelationIdOrderByReceivedAtAsc(correlationId));
        return ResponseEntity.ok(result);
    }
}

