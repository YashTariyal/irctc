package com.irctc.booking.eventtracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
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
 * Event Tracking Service
 * Handles tracking of event production and consumption
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class EventTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventTrackingService.class);
    
    @Autowired
    private EventProductionLogRepository productionLogRepository;
    
    @Autowired
    private EventConsumptionLogRepository consumptionLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${spring.application.name:booking-service}")
    private String serviceName;
    
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
            logger.warn("Could not extract eventId from event: {}", e.getMessage());
        }
        // Fallback: generate UUID
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Extract event type from event object
     */
    public String extractEventType(Object event) {
        try {
            Method getEventType = event.getClass().getMethod("getEventType");
            Object eventType = getEventType.invoke(event);
            if (eventType != null) {
                return eventType.toString();
            }
        } catch (Exception e) {
            logger.warn("Could not extract eventType from event: {}", e.getMessage());
        }
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
     * Log event production (before publishing)
     */
    @Transactional
    public EventProductionLog logEventProduction(String topic, String eventKey, Object event) {
        try {
            String eventId = extractEventId(event);
            String eventType = extractEventType(event);
            String correlationId = extractCorrelationId(event);
            String payload = objectMapper.writeValueAsString(event);
            
            // Check if already logged
            Optional<EventProductionLog> existing = productionLogRepository.findByEventId(eventId);
            if (existing.isPresent()) {
                logger.warn("Event {} already logged for production", eventId);
                return existing.get();
            }
            
            EventProductionLog log = new EventProductionLog();
            log.setEventId(eventId);
            log.setServiceName(serviceName);
            log.setTopic(topic);
            log.setEventKey(eventKey);
            log.setEventType(eventType);
            log.setPayload(payload);
            log.setStatus(EventProductionLog.ProductionStatus.PENDING);
            log.setCorrelationId(correlationId);
            
            // Store metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventClass", event.getClass().getName());
            metadata.put("loggedAt", LocalDateTime.now().toString());
            log.setMetadata(objectMapper.writeValueAsString(metadata));
            
            EventProductionLog saved = productionLogRepository.save(log);
            logger.info("📤 Event production logged: eventId={}, topic={}, type={}", 
                       eventId, topic, eventType);
            
            return saved;
            
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializing event for production log", e);
            throw new RuntimeException("Failed to log event production", e);
        }
    }
    
    /**
     * Mark event as publishing
     */
    @Transactional
    public void markEventPublishing(Long logId) {
        productionLogRepository.markAsPublishing(logId);
        logger.debug("🔄 Event marked as PUBLISHING: logId={}", logId);
    }
    
    /**
     * Update production log after successful publish
     */
    @Transactional
    public void markEventPublished(Long logId, SendResult<String, Object> result) {
        try {
            Integer partition = result.getRecordMetadata().partition();
            Long offset = result.getRecordMetadata().offset();
            
            productionLogRepository.markAsPublished(
                logId, 
                LocalDateTime.now(), 
                partition, 
                offset
            );
            
            logger.info("✅ Event production marked as PUBLISHED: logId={}, partition={}, offset={}", 
                       logId, partition, offset);
            
        } catch (Exception e) {
            logger.error("❌ Error marking event as published", e);
        }
    }
    
    /**
     * Update production log after failed publish
     */
    @Transactional
    public void markEventPublishFailed(Long logId, Throwable error) {
        try {
            EventProductionLog log = productionLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Production log not found: " + logId));
            
            String errorMessage = error.getMessage();
            if (errorMessage != null && errorMessage.length() > 1000) {
                errorMessage = errorMessage.substring(0, 1000);
            }
            
            EventProductionLog.ProductionStatus newStatus = 
                (log.getRetryCount() + 1 >= log.getMaxRetries()) 
                    ? EventProductionLog.ProductionStatus.FAILED 
                    : EventProductionLog.ProductionStatus.PENDING;
            
            productionLogRepository.markAsFailed(logId, newStatus, errorMessage);
            
            logger.warn("⚠️ Event production marked as {}: logId={}, retryCount={}/{}", 
                       newStatus, logId, log.getRetryCount() + 1, log.getMaxRetries());
            
        } catch (Exception e) {
            logger.error("❌ Error marking event publish as failed", e);
        }
    }
    
    /**
     * Log event consumption (when received from Kafka)
     */
    @Transactional
    public EventConsumptionLog logEventConsumption(String topic, Integer partition, Long offset,
                                                   String consumerGroup, Object event) {
        try {
            String eventId = extractEventId(event);
            String eventType = extractEventType(event);
            String correlationId = extractCorrelationId(event);
            String payload = objectMapper.writeValueAsString(event);
            
            // Check idempotency
            if (consumptionLogRepository.existsByEventIdAndStatus(
                eventId, EventConsumptionLog.ConsumptionStatus.PROCESSED)) {
                logger.warn("⚠️ Event {} already processed, skipping", eventId);
                return consumptionLogRepository.findByEventId(eventId)
                    .orElseThrow(() -> new RuntimeException("Event log not found"));
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

