package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.metrics.TicketConfirmationMetrics;
import com.irctc_backend.irctc.service.TicketConfirmationBatchService;
import com.irctc_backend.irctc.service.WaitlistRacService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Controller for batch processing operations and monitoring
 * Provides endpoints for testing and monitoring the ticket confirmation batch system
 */
@RestController
@RequestMapping("/api/batch")
@CrossOrigin(origins = "*")
public class BatchProcessingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingController.class);
    
    @Autowired
    private TicketConfirmationBatchService batchService;
    
    @Autowired
    private WaitlistRacService waitlistRacService;
    
    @Autowired
    private TicketConfirmationMetrics metrics;
    
    /**
     * Manually trigger batch processing for a specific train
     */
    @PostMapping("/process-confirmations/{trainId}")
    public ResponseEntity<Map<String, Object>> processConfirmations(@PathVariable Long trainId) {
        logger.info("Manual trigger for batch processing - TrainId: {}", trainId);
        
        try {
            int confirmations = batchService.processTrainConfirmationsManually(trainId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "trainId", trainId,
                "confirmationsProcessed", confirmations,
                "message", "Batch processing completed successfully",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in manual batch processing for train: {}", trainId, e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "trainId", trainId,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get confirmation statistics for a specific train and date
     */
    @GetMapping("/statistics/{trainId}/{journeyDate}")
    public ResponseEntity<Map<String, Object>> getConfirmationStatistics(
            @PathVariable Long trainId, 
            @PathVariable String journeyDate) {
        
        logger.info("Getting confirmation statistics - TrainId: {}, Date: {}", trainId, journeyDate);
        
        try {
            LocalDate date = LocalDate.parse(journeyDate);
            Map<String, Object> statistics = waitlistRacService.getConfirmationStatistics(trainId, date);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting confirmation statistics - TrainId: {}, Date: {}", trainId, journeyDate, e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get batch processing metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getBatchMetrics() {
        logger.info("Getting batch processing metrics");
        
        try {
            TicketConfirmationMetrics.BatchProcessingMetricsSummary metricsSummary = metrics.getMetricsSummary();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "metrics", metricsSummary,
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting batch metrics", e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get all trains available for batch processing
     */
    @GetMapping("/trains")
    public ResponseEntity<Map<String, Object>> getAvailableTrains() {
        logger.info("Getting available trains for batch processing");
        
        try {
            // This would typically call a service method to get trains
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Available trains retrieved successfully",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting available trains", e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reset daily metrics counters
     */
    @PostMapping("/reset-daily-metrics")
    public ResponseEntity<Map<String, Object>> resetDailyMetrics() {
        logger.info("Resetting daily metrics counters");
        
        try {
            metrics.resetDailyCounters();
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Daily metrics counters reset successfully",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error resetting daily metrics", e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get batch processing health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getBatchHealth() {
        logger.info("Getting batch processing health status");
        
        try {
            TicketConfirmationMetrics.BatchProcessingMetricsSummary metricsSummary = metrics.getMetricsSummary();
            
            boolean isHealthy = metricsSummary.getSuccessRate() > 90.0 && 
                              metricsSummary.getKafkaSuccessRate() > 95.0;
            
            Map<String, Object> response = Map.of(
                "success", true,
                "healthy", isHealthy,
                "successRate", metricsSummary.getSuccessRate(),
                "kafkaSuccessRate", metricsSummary.getKafkaSuccessRate(),
                "activeBatchJobs", metricsSummary.getActiveBatchJobs(),
                "totalConfirmationsToday", metricsSummary.getTotalConfirmationsToday(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting batch health", e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "healthy", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * Trigger chart preparation batch processing
     */
    @PostMapping("/chart-preparation")
    public ResponseEntity<Map<String, Object>> triggerChartPreparation() {
        logger.info("Triggering chart preparation batch processing");
        
        try {
            // This would trigger the chart preparation batch job
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Chart preparation batch processing triggered",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering chart preparation", e);
            
            Map<String, Object> response = Map.of(
                "success", false,
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
