package com.irctc.booking.eventtracking;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Event Consumption Log Entity
 * Tracks all events being consumed/processed from Kafka
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "event_consumption_log", indexes = {
    @Index(name = "idx_cons_status_created", columnList = "status,created_at"),
    @Index(name = "idx_cons_topic_partition_offset", columnList = "topic,partition_number,offset"),
    @Index(name = "idx_cons_consumer_group", columnList = "consumer_group,status"),
    @Index(name = "idx_cons_correlation", columnList = "correlation_id"),
    @Index(name = "idx_cons_event_id", columnList = "event_id", unique = true)
})
@Data
public class EventConsumptionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100, unique = true)
    private String eventId; // UUID from event payload
    
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName; // Which service consumed it
    
    @Column(nullable = false, length = 100)
    private String topic; // Kafka topic
    
    @Column(name = "partition_number", nullable = false)
    private Integer partitionNumber; // Kafka partition
    
    @Column(name = "\"offset\"", nullable = false)
    private Long offset; // Kafka offset
    
    @Column(name = "consumer_group", nullable = false, length = 100)
    private String consumerGroup; // Consumer group
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType; // Event type
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // Event payload (JSON)
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConsumptionStatus status = ConsumptionStatus.RECEIVED;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs; // Time taken to process
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace; // Full stack trace
    
    @Column(name = "correlation_id", length = 100)
    private String correlationId; // For tracing
    
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt; // When received from Kafka
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt; // When successfully processed
    
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
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ConsumptionStatus {
        RECEIVED,     // Event received from Kafka
        PROCESSING,   // Currently being processed
        PROCESSED,    // Successfully processed
        FAILED        // Failed to process (after max retries)
    }
}

