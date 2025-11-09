package com.irctc.user.eventtracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Event Tracking Service for User Service
 * Handles tracking of event production
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class EventTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventTrackingService.class);
    
    @Autowired
    private EventProductionLogRepository productionLogRepository;
    
    private final ObjectMapper objectMapper;
    
    @Value("${spring.application.name:user-service}")
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
            logger.debug("Could not extract eventType from event: {}", e.getMessage());
        }
        return event.getClass().getSimpleName();
    }
    
    /**
     * Extract correlation ID from event (if available)
     */
    public String extractCorrelationId(Object event) {
        try {
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
}

