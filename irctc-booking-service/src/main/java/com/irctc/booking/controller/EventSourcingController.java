package com.irctc.booking.controller;

import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.eventsourcing.BookingEvent;
import com.irctc.booking.eventsourcing.BookingEventReplayService;
import com.irctc.booking.eventsourcing.BookingEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event Sourcing Controller
 * Provides endpoints for event sourcing operations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/events")
public class EventSourcingController {
    
    @Autowired
    private BookingEventStore eventStore;
    
    @Autowired
    private BookingEventReplayService replayService;
    
    /**
     * Get event stream for a booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingEvent>> getBookingEvents(@PathVariable Long bookingId) {
        List<BookingEvent> events = eventStore.getEventStream(bookingId.toString());
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get events by type for a booking
     */
    @GetMapping("/booking/{bookingId}/type/{eventType}")
    public ResponseEntity<List<BookingEvent>> getBookingEventsByType(
            @PathVariable Long bookingId,
            @PathVariable String eventType) {
        List<BookingEvent> events = eventStore.getEventsByType(bookingId.toString(), eventType);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Replay events to rebuild booking state
     */
    @PostMapping("/booking/{bookingId}/replay")
    public ResponseEntity<SimpleBooking> replayBookingEvents(@PathVariable Long bookingId) {
        SimpleBooking booking = replayService.replayEvents(bookingId.toString());
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Time-travel: Get booking state at a specific point in time
     */
    @GetMapping("/booking/{bookingId}/time-travel")
    public ResponseEntity<SimpleBooking> timeTravelBooking(
            @PathVariable Long bookingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime upToTime) {
        SimpleBooking booking = replayService.replayEventsUpTo(bookingId.toString(), upToTime);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking);
    }
    
    /**
     * Get event timeline for a booking
     */
    @GetMapping("/booking/{bookingId}/timeline")
    public ResponseEntity<List<BookingEvent>> getBookingTimeline(@PathVariable Long bookingId) {
        List<BookingEvent> timeline = replayService.getEventTimeline(bookingId.toString());
        return ResponseEntity.ok(timeline);
    }
    
    /**
     * Get events by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<BookingEvent>> getEventsByCorrelationId(
            @PathVariable String correlationId) {
        List<BookingEvent> events = eventStore.getEventsByCorrelationId(correlationId);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get event count for a booking
     */
    @GetMapping("/booking/{bookingId}/count")
    public ResponseEntity<Long> getBookingEventCount(@PathVariable Long bookingId) {
        long count = eventStore.getEventCount(bookingId.toString());
        return ResponseEntity.ok(count);
    }
}

