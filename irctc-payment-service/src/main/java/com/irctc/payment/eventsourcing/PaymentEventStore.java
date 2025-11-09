package com.irctc.payment.eventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Payment Event Store Service
 * Manages event storage and retrieval for Event Sourcing
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class PaymentEventStore {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventStore.class);
    
    @Autowired
    private PaymentEventRepository eventRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Append an event to the event store
     */
    @Transactional
    public PaymentEvent appendEvent(String aggregateId, String eventType, Object eventData, 
                                   String correlationId, String userId) {
        try {
            PaymentEvent event = new PaymentEvent();
            event.setEventId(UUID.randomUUID().toString());
            event.setAggregateId(aggregateId);
            event.setEventType(eventType);
            event.setTimestamp(LocalDateTime.now());
            event.setCorrelationId(correlationId);
            event.setUserId(userId);
            event.setVersion("1.0");
            
            // Serialize event data to JSON
            String eventDataJson = objectMapper.writeValueAsString(eventData);
            event.setEventData(eventDataJson);
            
            // Store metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "payment-service");
            metadata.put("timestamp", LocalDateTime.now().toString());
            event.setEventMetadata(objectMapper.writeValueAsString(metadata));
            
            PaymentEvent saved = eventRepository.save(event);
            logger.info("📝 Payment event stored: {} for aggregate: {}", eventType, aggregateId);
            
            return saved;
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializing payment event data", e);
            throw new RuntimeException("Failed to store payment event", e);
        }
    }
    
    /**
     * Get all events for an aggregate (event stream)
     */
    public List<PaymentEvent> getEventStream(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByTimestampAsc(aggregateId);
    }
    
    /**
     * Get events by type
     */
    public List<PaymentEvent> getEventsByType(String aggregateId, String eventType) {
        return eventRepository.findByAggregateIdAndEventTypeOrderByTimestampAsc(aggregateId, eventType);
    }
    
    /**
     * Get events within time range
     */
    public List<PaymentEvent> getEventsInTimeRange(String aggregateId, 
                                                   LocalDateTime startTime, 
                                                   LocalDateTime endTime) {
        return eventRepository.findByAggregateIdAndTimestampBetweenOrderByTimestampAsc(
            aggregateId, startTime, endTime
        );
    }
    
    /**
     * Get latest event for an aggregate
     */
    public PaymentEvent getLatestEvent(String aggregateId) {
        List<PaymentEvent> events = eventRepository.findLatestEventByAggregateId(aggregateId);
        return events.isEmpty() ? null : events.get(0);
    }
    
    /**
     * Get event count for an aggregate
     */
    public long getEventCount(String aggregateId) {
        return eventRepository.countByAggregateId(aggregateId);
    }
    
    /**
     * Get events by correlation ID (for distributed tracing)
     */
    public List<PaymentEvent> getEventsByCorrelationId(String correlationId) {
        return eventRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }
}

