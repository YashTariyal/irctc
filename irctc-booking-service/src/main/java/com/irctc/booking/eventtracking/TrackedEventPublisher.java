package com.irctc.booking.eventtracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Tracked Event Publisher
 * Wraps KafkaTemplate to automatically track event production
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class TrackedEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(TrackedEventPublisher.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private EventTrackingService trackingService;
    
    /**
     * Publish event with tracking
     */
    @Transactional
    public CompletableFuture<SendResult<String, Object>> publishEvent(String topic, Object event) {
        return publishEvent(topic, null, event);
    }
    
    /**
     * Publish event with key and tracking
     */
    @Transactional
    public CompletableFuture<SendResult<String, Object>> publishEvent(String topic, String eventKey, Object event) {
        // 1. Log event production (PENDING)
        EventProductionLog log = trackingService.logEventProduction(topic, eventKey, event);
        
        // 2. Extract event ID for Kafka key (if not provided)
        String kafkaKey = eventKey != null ? eventKey : trackingService.extractEventId(event);
        
        // 3. Mark as publishing
        trackingService.markEventPublishing(log.getId());
        
        // 4. Publish to Kafka
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, kafkaKey, event);
        
        // 5. Handle result asynchronously
        future.whenComplete((result, error) -> {
            if (error == null) {
                // Success: Mark as PUBLISHED
                trackingService.markEventPublished(log.getId(), result);
            } else {
                // Failure: Mark as FAILED (or PENDING for retry)
                trackingService.markEventPublishFailed(log.getId(), error);
            }
        });
        
        return future;
    }
    
    /**
     * Mark event as publishing (internal use)
     */
    private void markEventPublishing(Long logId) {
        // This is handled by the repository method
        // We can add additional logic here if needed
    }
}

