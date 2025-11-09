package com.irctc.notification.controller;

import com.irctc.notification.service.DlqManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DLQ Management Controller
 * 
 * REST API for managing Dead Letter Queue (DLQ) operations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/dlq")
public class DlqManagementController {

    private static final Logger logger = LoggerFactory.getLogger(DlqManagementController.class);

    @Autowired
    private DlqManagementService dlqManagementService;

    /**
     * Get DLQ statistics for a topic
     * GET /api/dlq/stats/{dltTopic}
     */
    @GetMapping("/stats/{dltTopic}")
    public ResponseEntity<DlqManagementService.DlqStatistics> getDlqStatistics(
            @PathVariable String dltTopic) {
        logger.info("📊 Getting DLQ statistics for topic: {}", dltTopic);
        DlqManagementService.DlqStatistics stats = dlqManagementService.getDlqStatistics(dltTopic);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all DLQ statistics
     * GET /api/dlq/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, DlqManagementService.DlqStatistics>> getAllDlqStatistics() {
        logger.info("📊 Getting all DLQ statistics");
        
        Map<String, DlqManagementService.DlqStatistics> allStats = new HashMap<>();
        
        // Common DLQ topics
        String[] commonTopics = {
            "booking-created.DLT",
            "booking-confirmed.DLT",
            "booking-cancelled.DLT",
            "payment-initiated.DLT",
            "payment-completed.DLT",
            "ticket-confirmation-events.DLT",
            "user-events.DLT"
        };
        
        for (String topic : commonTopics) {
            try {
                DlqManagementService.DlqStatistics stats = dlqManagementService.getDlqStatistics(topic);
                allStats.put(topic, stats);
            } catch (Exception e) {
                logger.warn("Error getting stats for topic {}: {}", topic, e.getMessage());
            }
        }
        
        return ResponseEntity.ok(allStats);
    }

    /**
     * Reprocess messages from DLQ
     * POST /api/dlq/reprocess
     * Body: { "dltTopic": "booking-created.DLT", "mainTopic": "booking-created", "maxRecords": 10 }
     */
    @PostMapping("/reprocess")
    public ResponseEntity<DlqManagementService.ReprocessResult> reprocessDlq(
            @RequestBody ReprocessRequest request) {
        logger.info("🔄 Reprocessing DLQ: {} -> {} (max: {})", 
                   request.getDltTopic(), request.getMainTopic(), request.getMaxRecords());
        
        DlqManagementService.ReprocessResult result = dlqManagementService.reprocessDlq(
            request.getDltTopic(),
            request.getMainTopic(),
            request.getMaxRecords()
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Inspect messages in DLQ
     * GET /api/dlq/inspect/{dltTopic}?maxMessages=10
     */
    @GetMapping("/inspect/{dltTopic}")
    public ResponseEntity<List<DlqManagementService.DlqMessage>> inspectDlqMessages(
            @PathVariable String dltTopic,
            @RequestParam(defaultValue = "10") int maxMessages) {
        logger.info("🔍 Inspecting DLQ messages for topic: {} (max: {})", dltTopic, maxMessages);
        
        List<DlqManagementService.DlqMessage> messages = 
            dlqManagementService.inspectDlqMessages(dltTopic, maxMessages);
        
        return ResponseEntity.ok(messages);
    }

    /**
     * Reprocess Request DTO
     */
    public static class ReprocessRequest {
        private String dltTopic;
        private String mainTopic;
        private int maxRecords = 10;

        public String getDltTopic() { return dltTopic; }
        public void setDltTopic(String dltTopic) { this.dltTopic = dltTopic; }
        public String getMainTopic() { return mainTopic; }
        public void setMainTopic(String mainTopic) { this.mainTopic = mainTopic; }
        public int getMaxRecords() { return maxRecords; }
        public void setMaxRecords(int maxRecords) { this.maxRecords = maxRecords; }
    }
}

