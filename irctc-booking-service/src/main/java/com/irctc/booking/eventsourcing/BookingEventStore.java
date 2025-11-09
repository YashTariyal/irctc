package com.irctc.booking.eventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
 * Booking Event Store Service
 * Manages event storage and retrieval for Event Sourcing
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class BookingEventStore {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingEventStore.class);
    
    @Autowired
    private BookingEventRepository eventRepository;
    
    private final ObjectMapper objectMapper;
    
    public BookingEventStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Append an event to the event store
     */
    @Transactional
    public BookingEvent appendEvent(String aggregateId, String eventType, Object eventData, 
                                   String correlationId, String userId) {
        try {
            BookingEvent event = new BookingEvent();
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
            metadata.put("source", "booking-service");
            metadata.put("timestamp", LocalDateTime.now().toString());
            event.setEventMetadata(objectMapper.writeValueAsString(metadata));
            
            BookingEvent saved = eventRepository.save(event);
            logger.info("📝 Event stored: {} for aggregate: {}", eventType, aggregateId);
            
            return saved;
        } catch (JsonProcessingException e) {
            logger.error("❌ Error serializing event data", e);
            throw new RuntimeException("Failed to store event", e);
        }
    }
    
    /**
     * Get all events for an aggregate (event stream)
     */
    public List<BookingEvent> getEventStream(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByTimestampAsc(aggregateId);
    }
    
    /**
     * Get events by type
     */
    public List<BookingEvent> getEventsByType(String aggregateId, String eventType) {
        return eventRepository.findByAggregateIdAndEventTypeOrderByTimestampAsc(aggregateId, eventType);
    }
    
    /**
     * Get events within time range
     */
    public List<BookingEvent> getEventsInTimeRange(String aggregateId, 
                                                   LocalDateTime startTime, 
                                                   LocalDateTime endTime) {
        return eventRepository.findByAggregateIdAndTimestampBetweenOrderByTimestampAsc(
            aggregateId, startTime, endTime
        );
    }
    
    /**
     * Get latest event for an aggregate
     */
    public BookingEvent getLatestEvent(String aggregateId) {
        List<BookingEvent> events = eventRepository.findLatestEventByAggregateId(aggregateId);
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
    public List<BookingEvent> getEventsByCorrelationId(String correlationId) {
        return eventRepository.findByCorrelationIdOrderByTimestampAsc(correlationId);
    }
}

