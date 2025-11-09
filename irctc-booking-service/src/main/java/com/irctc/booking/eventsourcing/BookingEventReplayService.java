package com.irctc.booking.eventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.SimpleBooking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Booking Event Replay Service
 * Rebuilds aggregate state by replaying events
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class BookingEventReplayService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingEventReplayService.class);
    
    @Autowired
    private BookingEventStore eventStore;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Replay events to rebuild booking state
     */
    public SimpleBooking replayEvents(String aggregateId) {
        logger.info("🔄 Replaying events for booking: {}", aggregateId);
        
        List<BookingEvent> events = eventStore.getEventStream(aggregateId);
        
        if (events.isEmpty()) {
            logger.warn("⚠️  No events found for aggregate: {}", aggregateId);
            return null;
        }
        
        // Start with empty booking
        SimpleBooking booking = new SimpleBooking();
        booking.setId(Long.parseLong(aggregateId));
        
        // Replay each event
        for (BookingEvent event : events) {
            try {
                booking = applyEvent(booking, event);
            } catch (Exception e) {
                logger.error("❌ Error applying event {}: {}", event.getEventType(), e.getMessage());
                throw new RuntimeException("Event replay failed", e);
            }
        }
        
        logger.info("✅ Replayed {} events for booking: {}", events.size(), aggregateId);
        return booking;
    }
    
    /**
     * Apply a single event to the aggregate
     */
    private SimpleBooking applyEvent(SimpleBooking booking, BookingEvent event) 
            throws JsonProcessingException {
        
        Map<String, Object> eventData = objectMapper.readValue(
            event.getEventData(), 
            Map.class
        );
        
        switch (event.getEventType()) {
            case "BOOKING_CREATED":
                return applyBookingCreated(booking, eventData);
            case "BOOKING_UPDATED":
                return applyBookingUpdated(booking, eventData);
            case "BOOKING_CANCELLED":
                return applyBookingCancelled(booking, eventData);
            case "BOOKING_CONFIRMED":
                return applyBookingConfirmed(booking, eventData);
            case "BOOKING_STATUS_CHANGED":
                return applyStatusChanged(booking, eventData);
            case "FARE_UPDATED":
                return applyFareUpdated(booking, eventData);
            default:
                logger.warn("⚠️  Unknown event type: {}", event.getEventType());
                return booking;
        }
    }
    
    private SimpleBooking applyBookingCreated(SimpleBooking booking, Map<String, Object> eventData) {
        if (booking.getUserId() == null) {
            booking.setUserId(Long.valueOf(eventData.get("userId").toString()));
        }
        if (booking.getTrainId() == null) {
            booking.setTrainId(Long.valueOf(eventData.get("trainId").toString()));
        }
        if (booking.getPnrNumber() == null) {
            booking.setPnrNumber(eventData.get("pnrNumber").toString());
        }
        if (booking.getTotalFare() == null) {
            booking.setTotalFare(new BigDecimal(eventData.get("totalFare").toString()));
        }
        booking.setStatus("PENDING");
        if (booking.getBookingTime() == null && eventData.containsKey("bookingTime")) {
            booking.setBookingTime(LocalDateTime.parse(eventData.get("bookingTime").toString()));
        }
        return booking;
    }
    
    private SimpleBooking applyBookingUpdated(SimpleBooking booking, Map<String, Object> eventData) {
        if (eventData.containsKey("status")) {
            booking.setStatus(eventData.get("status").toString());
        }
        if (eventData.containsKey("totalFare")) {
            booking.setTotalFare(new BigDecimal(eventData.get("totalFare").toString()));
        }
        return booking;
    }
    
    private SimpleBooking applyBookingCancelled(SimpleBooking booking, Map<String, Object> eventData) {
        booking.setStatus("CANCELLED");
        return booking;
    }
    
    private SimpleBooking applyBookingConfirmed(SimpleBooking booking, Map<String, Object> eventData) {
        booking.setStatus("CONFIRMED");
        return booking;
    }
    
    private SimpleBooking applyStatusChanged(SimpleBooking booking, Map<String, Object> eventData) {
        if (eventData.containsKey("newStatus")) {
            booking.setStatus(eventData.get("newStatus").toString());
        }
        return booking;
    }
    
    private SimpleBooking applyFareUpdated(SimpleBooking booking, Map<String, Object> eventData) {
        if (eventData.containsKey("newFare")) {
            booking.setTotalFare(new BigDecimal(eventData.get("newFare").toString()));
        }
        return booking;
    }
    
    /**
     * Get event timeline for a booking
     */
    public List<BookingEvent> getEventTimeline(String aggregateId) {
        return eventStore.getEventStream(aggregateId);
    }
    
    /**
     * Get events up to a specific point in time (time-travel)
     */
    public SimpleBooking replayEventsUpTo(String aggregateId, LocalDateTime upToTime) {
        logger.info("🕐 Replaying events up to {} for booking: {}", upToTime, aggregateId);
        
        List<BookingEvent> events = eventStore.getEventsInTimeRange(
            aggregateId, 
            LocalDateTime.of(2000, 1, 1, 0, 0), 
            upToTime
        );
        
        SimpleBooking booking = new SimpleBooking();
        booking.setId(Long.parseLong(aggregateId));
        
        for (BookingEvent event : events) {
            try {
                booking = applyEvent(booking, event);
            } catch (Exception e) {
                logger.error("❌ Error applying event in time-travel: {}", e.getMessage());
                throw new RuntimeException("Time-travel replay failed", e);
            }
        }
        
        return booking;
    }
}

