package com.irctc.booking.saga;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga Instance Entity
 * Tracks the state of a distributed transaction saga
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "saga_instances", indexes = {
    @Index(name = "idx_saga_status_created", columnList = "status,created_at"),
    @Index(name = "idx_saga_correlation", columnList = "correlation_id")
})
@Data
public class SagaInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String sagaId;
    
    @Column(nullable = false, length = 50)
    private String sagaType;
    
    @Column(nullable = false, length = 100)
    private String correlationId; // Links to booking, payment, etc.
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    
    @Column(nullable = false)
    private Integer currentStep;
    
    @Column(nullable = false)
    private Integer totalSteps;
    
    @Column(columnDefinition = "TEXT")
    private String sagaData; // JSON data for saga context
    
    @Column(columnDefinition = "TEXT")
    private String compensationData; // JSON data for compensation
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = SagaStatus.STARTED;
        }
        if (currentStep == null) {
            currentStep = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == SagaStatus.COMPLETED || status == SagaStatus.COMPENSATED) {
            completedAt = LocalDateTime.now();
        }
    }
    
    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        COMPENSATING,
        COMPENSATED,
        FAILED
    }
}

