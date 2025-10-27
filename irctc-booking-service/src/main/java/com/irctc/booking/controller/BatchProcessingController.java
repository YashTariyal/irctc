package com.irctc.booking.controller;

import com.irctc.booking.service.TicketConfirmationBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Batch Processing Controller for Booking Service
 * 
 * Provides REST endpoints for managing and monitoring the ticket confirmation
 * batch processing system.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/batch")
public class BatchProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingController.class);

    @Autowired
    private TicketConfirmationBatchService batchService;

    /**
     * Manually trigger the ticket confirmation batch job
     */
    @PostMapping("/trigger-confirmations")
    public ResponseEntity<Map<String, Object>> triggerTicketConfirmations() {
        logger.info("🔧 Manual trigger for ticket confirmation batch job received");
        
        try {
            batchService.triggerManualProcessing();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Ticket confirmation batch job triggered successfully");
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("service", "Booking Service (Port 8093)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error triggering batch job", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to trigger batch job: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get batch processing statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBatchStatistics() {
        logger.info("📊 Fetching batch processing statistics");
        
        try {
            String stats = batchService.getBatchStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("statistics", stats);
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("service", "Booking Service (Port 8093)");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error fetching statistics", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to fetch statistics: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Health check for batch processing
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getBatchHealth() {
        logger.info("🏥 Batch processing health check");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Booking Service Batch Processing");
        response.put("port", 8093);
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("features", new String[]{
            "Scheduled Processing",
            "Manual Trigger",
            "Kafka Event Publishing",
            "Statistics Monitoring"
        });
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "IRCTC Booking Service");
        response.put("version", "1.0.0");
        response.put("port", 8093);
        response.put("description", "Microservice for ticket booking and batch processing");
        response.put("features", new String[]{
            "Ticket Booking",
            "Booking Management", 
            "Batch Confirmation Processing",
            "Kafka Integration",
            "REST API"
        });
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}
