package com.irctc.notification.eventtracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Tracked Event Consumer Helper
 * Provides utility methods for tracking event consumption with idempotency
 * 
 * This is a helper class that can be used in @KafkaListener methods
 * to automatically track event consumption and prevent duplicate processing.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class TrackedEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackedEventConsumer.class);
    
    @Autowired
    private EventTrackingService trackingService;
    
    /**
     * Track event consumption in a Kafka listener with idempotency check
     * 
     * Usage:
     * ```java
     * @KafkaListener(topics = "booking-events", groupId = "notification-service")
     * public void handleEvent(BookingEvent event,
     *                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
     *                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
     *                        @Header(KafkaHeaders.OFFSET) long offset) {
     *     TrackedEventResult result = trackedConsumer.trackConsumption(
     *         topic, partition, offset, "notification-service", event
     *     );
     *     
     *     if (result.isAlreadyProcessed()) {
     *         logger.info("Event {} already processed, skipping", result.getEventId());
     *         return; // Skip duplicate
     *     }
     *     
     *     try {
     *         result.markProcessing();
     *         // Process event
     *         processEvent(event);
     *         result.markProcessed();
     *     } catch (Exception e) {
     *         result.markFailed(e);
     *         throw e;
     *     }
     * }
     * ```
     */
    public TrackedEventResult trackConsumption(String topic, Integer partition, Long offset,
                                               String consumerGroup, Object event) {
        EventConsumptionLog log = trackingService.logEventConsumption(
            topic, partition, offset, consumerGroup, event
        );
        
        // Check if already processed
        String eventId = trackingService.extractEventId(event);
        boolean alreadyProcessed = (log.getStatus() == EventConsumptionLog.ConsumptionStatus.PROCESSED);
        
        return new TrackedEventResult(log, alreadyProcessed, eventId);
    }
    
    /**
     * Result object for tracked event consumption
     */
    public class TrackedEventResult {
        private final EventConsumptionLog log;
        private final boolean alreadyProcessed;
        private final String eventId;
        private final long startTime;
        
        public TrackedEventResult(EventConsumptionLog log, boolean alreadyProcessed, String eventId) {
            this.log = log;
            this.alreadyProcessed = alreadyProcessed;
            this.eventId = eventId;
            this.startTime = System.currentTimeMillis();
        }
        
        public boolean isAlreadyProcessed() {
            return alreadyProcessed;
        }
        
        public EventConsumptionLog getLog() {
            return log;
        }
        
        public String getEventId() {
            return eventId;
        }
        
        /**
         * Mark event as processing
         */
        public void markProcessing() {
            trackingService.markEventProcessing(log.getId());
        }
        
        /**
         * Mark event as processed (success)
         */
        public void markProcessed() {
            long processingTime = System.currentTimeMillis() - startTime;
            trackingService.markEventProcessed(log.getId(), processingTime);
        }
        
        /**
         * Mark event as failed
         */
        public void markFailed(Throwable error) {
            trackingService.markEventConsumptionFailed(log.getId(), error);
        }
    }
}

