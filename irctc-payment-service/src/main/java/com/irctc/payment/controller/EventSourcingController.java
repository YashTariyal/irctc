package com.irctc.payment.controller;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.eventsourcing.PaymentEvent;
import com.irctc.payment.eventsourcing.PaymentEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Payment Event Sourcing Controller
 * Provides endpoints for payment event sourcing operations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/events")
public class EventSourcingController {
    
    @Autowired
    private PaymentEventStore eventStore;
    
    /**
     * Get event stream for a payment
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<List<PaymentEvent>> getPaymentEvents(@PathVariable Long paymentId) {
        List<PaymentEvent> events = eventStore.getEventStream(paymentId.toString());
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get events by type for a payment
     */
    @GetMapping("/payment/{paymentId}/type/{eventType}")
    public ResponseEntity<List<PaymentEvent>> getPaymentEventsByType(
            @PathVariable Long paymentId,
            @PathVariable String eventType) {
        List<PaymentEvent> events = eventStore.getEventsByType(paymentId.toString(), eventType);
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get events within time range
     */
    @GetMapping("/payment/{paymentId}/time-range")
    public ResponseEntity<List<PaymentEvent>> getPaymentEventsInTimeRange(
            @PathVariable Long paymentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<PaymentEvent> events = eventStore.getEventsInTimeRange(
            paymentId.toString(), startTime, endTime
        );
        return ResponseEntity.ok(events);
    }
    
    /**
     * Get latest event for a payment
     */
    @GetMapping("/payment/{paymentId}/latest")
    public ResponseEntity<PaymentEvent> getLatestPaymentEvent(@PathVariable Long paymentId) {
        PaymentEvent event = eventStore.getLatestEvent(paymentId.toString());
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(event);
    }
    
    /**
     * Get event count for a payment
     */
    @GetMapping("/payment/{paymentId}/count")
    public ResponseEntity<Long> getPaymentEventCount(@PathVariable Long paymentId) {
        long count = eventStore.getEventCount(paymentId.toString());
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get events by correlation ID
     */
    @GetMapping("/correlation/{correlationId}")
    public ResponseEntity<List<PaymentEvent>> getEventsByCorrelationId(
            @PathVariable String correlationId) {
        List<PaymentEvent> events = eventStore.getEventsByCorrelationId(correlationId);
        return ResponseEntity.ok(events);
    }
}

