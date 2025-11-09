package com.irctc.notification.eventtracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Event Tracking Service for Notification Service
 * Handles tracking of event consumption with idempotency checks
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class EventTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventTrackingService.class);
    
    @Autowired
    private EventConsumptionLogRepository consumptionLogRepository;
    
    private final ObjectMapper objectMapper;
    
    @Value("${spring.application.name:notification-service}")
    private String serviceName;
    
    public EventTrackingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Extract event ID from event object using reflection
     */
    public String extractEventId(Object event) {
        try {
            Method getEventId = event.getClass().getMethod("getEventId");
            Object eventId = getEventId.invoke(event);
            if (eventId != null) {
                return eventId.toString();
            }
        } catch (Exception e) {
            logger.debug("Could not extract eventId from event: {}", e.getMessage());
        }
        // Fallback: try requestId or generate UUID
        try {
            Method getRequestId = event.getClass().getMethod("getRequestId");
            Object requestId = getRequestId.invoke(event);
            if (requestId != null) {
                return requestId.toString();
            }
        } catch (Exception e) {
            // Ignore
        }
        // Final fallback: generate UUID based on event content
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Extract event type from event object
     */
    public String extractEventType(Object event) {
        return event.getClass().getSimpleName();
    }
    
    /**
     * Extract correlation ID from event (if available)
     */
    public String extractCorrelationId(Object event) {
        try {
            // Try common correlation ID fields
            String[] correlationFields = {"correlationId", "requestId", "traceId"};
            for (String field : correlationFields) {
                try {
                    String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
                    Method method = event.getClass().getMethod(methodName);
                    Object value = method.invoke(event);
                    if (value != null) {
                        return value.toString();
                    }
                } catch (NoSuchMethodException e) {
                    // Continue to next field
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    /**
     * Log event consumption (when received from Kafka)
     * Returns null if event already processed (idempotency check)
     */
    @Transactional
    public EventConsumptionLog logEventConsumption(String topic, Integer partition, Long offset,
                                                   String consumerGroup, Object event) {
        try {
            String eventId = extractEventId(event);
            String eventType = extractEventType(event);
            String correlationId = extractCorrelationId(event);
            String payload = objectMapper.writeValueAsString(event);
            
            // Check idempotency - if already processed, return existing log
            Optional<EventConsumptionLog> existing = consumptionLogRepository.findByEventId(eventId);
            if (existing.isPresent()) {
                EventConsumptionLog existingLog = existing.get();
                if (existingLog.getStatus() == EventConsumptionLog.ConsumptionStatus.PROCESSED) {
                    logger.warn("⚠️ Event {} already processed, skipping duplicate", eventId);
                    return existingLog; // Return existing to indicate already processed
                }
                // If it's in PROCESSING or FAILED state, we can retry
                logger.info("📥 Event {} found in state {}, allowing retry", eventId, existingLog.getStatus());
                return existingLog;
            }
            
            EventConsumptionLog log = new EventConsumptionLog();
            log.setEventId(eventId);
            log.setServiceName(serviceName);
            log.setTopic(topic);
            log.setPartitionNumber(partition);
            log.setOffset(offset);
            log.setConsumerGroup(consumerGroup);
            log.setEventType(eventType);
            log.setPayload(payload);
            log.setStatus(EventConsumptionLog.ConsumptionStatus.RECEIVED);
            log.setCorrelationId(correlationId);
            log.setReceivedAt(LocalDateTime.now());
            
            // Store metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventClass", event.getClass().getName());
            metadata.put("receivedAt", LocalDateTime.now().toString());
            log.setMetadata(objectMapper.writeValueAsString(metadata));
            
            EventConsumptionLog saved = consumptionLogRepository.save(log);
            logger.info("📥 Event consumption logged: eventId={}, topic={}, partition={}, offset={}", 
                       eventId, topic, partition, offset);
            
            return saved;
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializing event for consumption log", e);
            throw new RuntimeException("Failed to log event consumption", e);
        }
    }
    
    /**
     * Mark event as processing
     */
    @Transactional
    public void markEventProcessing(Long logId) {
        consumptionLogRepository.markAsProcessing(logId);
        logger.debug("🔄 Event marked as PROCESSING: logId={}", logId);
    }
    
    /**
     * Mark event as processed
     */
    @Transactional
    public void markEventProcessed(Long logId, long processingTimeMs) {
        consumptionLogRepository.markAsProcessed(
            logId, 
            LocalDateTime.now(), 
            processingTimeMs
        );
        logger.info("✅ Event marked as PROCESSED: logId={}, processingTime={}ms", 
                   logId, processingTimeMs);
    }
    
    /**
     * Mark event consumption as failed
     */
    @Transactional
    public void markEventConsumptionFailed(Long logId, Throwable error) {
        try {
            EventConsumptionLog log = consumptionLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Consumption log not found: " + logId));
            
            String errorMessage = error.getMessage();
            if (errorMessage != null && errorMessage.length() > 1000) {
                errorMessage = errorMessage.substring(0, 1000);
            }
            
            String stackTrace = getStackTrace(error);
            if (stackTrace.length() > 5000) {
                stackTrace = stackTrace.substring(0, 5000);
            }
            
            EventConsumptionLog.ConsumptionStatus newStatus = 
                (log.getRetryCount() + 1 >= log.getMaxRetries()) 
                    ? EventConsumptionLog.ConsumptionStatus.FAILED 
                    : EventConsumptionLog.ConsumptionStatus.RECEIVED;
            
            consumptionLogRepository.markAsFailed(logId, newStatus, errorMessage, stackTrace);
            
            logger.warn("⚠️ Event consumption marked as {}: logId={}, retryCount={}/{}", 
                       newStatus, logId, log.getRetryCount() + 1, log.getMaxRetries());
            
        } catch (Exception e) {
            logger.error("❌ Error marking event consumption as failed", e);
        }
    }
    
    /**
     * Check if event is already processed (idempotency)
     */
    public boolean isEventProcessed(String eventId) {
        return consumptionLogRepository.existsByEventIdAndStatus(
            eventId, EventConsumptionLog.ConsumptionStatus.PROCESSED
        );
    }
    
    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return sw.toString();
    }
}

