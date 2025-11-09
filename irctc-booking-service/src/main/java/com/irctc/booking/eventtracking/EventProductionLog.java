package com.irctc.booking.eventtracking;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Event Production Log Entity
 * Tracks all events being produced/published to Kafka
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "event_production_log", indexes = {
    @Index(name = "idx_prod_status_created", columnList = "status,created_at"),
    @Index(name = "idx_prod_topic", columnList = "topic,created_at"),
    @Index(name = "idx_prod_correlation", columnList = "correlation_id"),
    @Index(name = "idx_prod_event_id", columnList = "event_id")
})
@Data
public class EventProductionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, unique = true)
    private String eventId; // UUID from event payload
    
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName; // Which service produced it
    
    @Column(nullable = false, length = 100)
    private String topic; // Kafka topic
    
    @Column(name = "event_key", length = 255)
    private String eventKey; // Kafka message key
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // Event type (BOOKING_CREATED, etc.)
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // Event payload (JSON)
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ProductionStatus status = ProductionStatus.PENDING;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "correlation_id", length = 100)
    private String correlationId; // For tracing
    
    @Column(name = "partition_number")
    private Integer partitionNumber; // Kafka partition
    
    @Column(name = "\"offset\"")
    private Long offset; // Kafka offset (after publish)
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt; // When successfully published
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional metadata (JSON)
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ProductionStatus {
        PENDING,      // Event saved, waiting to be published
        PUBLISHING,   // Currently being published
        PUBLISHED,    // Successfully published to Kafka
        FAILED        // Failed to publish (after max retries)
    }
}

