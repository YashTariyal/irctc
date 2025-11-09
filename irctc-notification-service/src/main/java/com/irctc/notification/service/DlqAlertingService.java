package com.irctc.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DLQ Alerting Service
 * 
 * Monitors DLQ topics and sends alerts when thresholds are exceeded
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class DlqAlertingService {

    private static final Logger logger = LoggerFactory.getLogger(DlqAlertingService.class);

    @Autowired
    private DlqManagementService dlqManagementService;

    @Value("${dlq.alerting.enabled:true}")
    private boolean alertingEnabled;

    @Value("${dlq.alerting.threshold:10}")
    private long alertThreshold;

    @Value("${dlq.alerting.check-interval:300000}") // 5 minutes
    private long checkInterval;

    private final Map<String, LocalDateTime> lastAlertTime = new HashMap<>();
    private static final long ALERT_COOLDOWN_MINUTES = 60; // Don't alert more than once per hour

    /**
     * Scheduled task to check DLQ sizes and send alerts
     * Runs every 5 minutes by default
     */
    @Scheduled(fixedDelayString = "${dlq.alerting.check-interval:300000}")
    public void checkDlqSizes() {
        if (!alertingEnabled) {
            return;
        }

        logger.debug("🔍 Checking DLQ sizes for alerting...");

        String[] topics = {
            "booking-created.DLT",
            "booking-confirmed.DLT",
            "booking-cancelled.DLT",
            "payment-initiated.DLT",
            "payment-completed.DLT",
            "ticket-confirmation-events.DLT",
            "user-events.DLT"
        };

        for (String topic : topics) {
            try {
                DlqManagementService.DlqStatistics stats = dlqManagementService.getDlqStatistics(topic);
                
                if (stats.getMessageCount() > alertThreshold) {
                    sendAlert(topic, stats);
                }
            } catch (Exception e) {
                logger.warn("Error checking DLQ size for topic {}: {}", topic, e.getMessage());
            }
        }
    }

    /**
     * Send alert for DLQ threshold exceeded
     */
    private void sendAlert(String topic, DlqManagementService.DlqStatistics stats) {
        // Check cooldown period
        LocalDateTime lastAlert = lastAlertTime.get(topic);
        if (lastAlert != null && 
            lastAlert.plusMinutes(ALERT_COOLDOWN_MINUTES).isAfter(LocalDateTime.now())) {
            logger.debug("⏸️ Alert cooldown active for topic: {}", topic);
            return;
        }

        String message = String.format(
            "🚨 DLQ Alert: Topic '%s' has %d messages (threshold: %d)",
            topic, stats.getMessageCount(), alertThreshold
        );

        logger.warn(message);
        logger.warn("   Topic: {}", topic);
        logger.warn("   Message Count: {}", stats.getMessageCount());
        logger.warn("   Partitions: {}", stats.getPartitionCount());
        logger.warn("   Timestamp: {}", stats.getTimestamp());

        // Update last alert time
        lastAlertTime.put(topic, LocalDateTime.now());

        // TODO: Integrate with external alerting systems:
        // - Send email notification
        // - Send Slack notification
        // - Send PagerDuty alert
        // - Update Prometheus alert
    }

    /**
     * Manually trigger alert check
     */
    public void triggerAlertCheck() {
        logger.info("🔔 Manually triggering DLQ alert check...");
        checkDlqSizes();
    }
}

