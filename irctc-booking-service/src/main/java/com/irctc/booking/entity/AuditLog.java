package com.irctc.booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity_type", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_user", columnList = "userId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String entityType; // e.g., "Booking", "Payment"
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE, READ
    
    @Column(length = 255)
    private String userId; // User who performed the action
    
    @Column(length = 255)
    private String username; // Username for audit trail
    
    @Column(length = 50)
    private String ipAddress; // Client IP address
    
    @Column(length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE
    
    @Column(length = 500)
    private String requestPath; // API endpoint
    
    @Column(columnDefinition = "TEXT")
    private String requestBody; // Request payload (for sensitive data, can be redacted)
    
    @Column(columnDefinition = "TEXT")
    private String responseBody; // Response payload
    
    @Column
    private Integer responseStatus; // HTTP status code
    
    @Column(columnDefinition = "TEXT")
    private String oldValues; // Previous state (JSON)
    
    @Column(columnDefinition = "TEXT")
    private String newValues; // New state (JSON)
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage; // Error details if any
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 1000)
    private String additionalInfo; // Any additional context
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}

