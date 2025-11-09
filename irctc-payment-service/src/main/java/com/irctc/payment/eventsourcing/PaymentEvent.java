package com.irctc.payment.eventsourcing;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Payment Event Entity for Event Sourcing
 * Stores all events that occur for a payment aggregate
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "payment_events", indexes = {
    @Index(name = "idx_payment_events_aggregate", columnList = "aggregateId,timestamp"),
    @Index(name = "idx_payment_events_type", columnList = "eventType,timestamp"),
    @Index(name = "idx_payment_events_correlation", columnList = "correlationId")
})
@Data
public class PaymentEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String eventId; // Unique event identifier
    
    @Column(nullable = false, length = 50)
    private String aggregateId; // Payment ID
    
    @Column(nullable = false, length = 100)
    private String eventType; // PAYMENT_INITIATED, PAYMENT_COMPLETED, PAYMENT_FAILED, REFUND_PROCESSED, etc.
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData; // JSON representation of event payload
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 100)
    private String correlationId; // For tracing across services
    
    @Column(length = 100)
    private String userId; // User who triggered the event
    
    @Column(length = 50)
    private String version; // Event version for schema evolution
    
    @Column(name = "event_metadata", columnDefinition = "TEXT")
    private String eventMetadata; // Additional metadata as JSON
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (eventId == null) {
            eventId = java.util.UUID.randomUUID().toString();
        }
    }
    
    /**
     * Event Types
     */
    public enum EventType {
        PAYMENT_INITIATED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED,
        PAYMENT_STATUS_CHANGED,
        PAYMENT_AMOUNT_UPDATED
    }
}

