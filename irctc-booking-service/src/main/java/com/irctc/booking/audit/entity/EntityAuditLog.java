package com.irctc.booking.audit.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity Audit Log
 * Generic audit table for tracking all entity changes
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "entity_audit_log", indexes = {
    @Index(name = "idx_audit_entity_name_id", columnList = "entityName, entityId"),
    @Index(name = "idx_audit_revision", columnList = "entityName, entityId, revisionNumber"),
    @Index(name = "idx_audit_changed_by", columnList = "changedBy"),
    @Index(name = "idx_audit_changed_at", columnList = "changedAt"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Data
public class EntityAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName; // e.g., "SimpleBooking", "SimplePayment"
    
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // ID of the audited entity
    
    @Column(name = "revision_number", nullable = false)
    private Long revisionNumber; // Sequential revision number for this entity
    
    @Column(nullable = false, length = 50)
    private String action; // CREATE, UPDATE, DELETE
    
    @Column(name = "changed_by", length = 255)
    private String changedBy; // User ID who made the change
    
    @Column(name = "changed_by_username", length = 255)
    private String changedByUsername; // Username for audit trail
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress; // Client IP address
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt; // When the change occurred
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // Previous state (JSON)
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // New state (JSON)
    
    @Column(name = "changed_fields", columnDefinition = "TEXT")
    private String changedFields; // List of changed field names (JSON array)
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // Additional metadata (JSON)
    
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
        // Set revision number if not set
        if (revisionNumber == null) {
            revisionNumber = 1L; // Will be updated by repository
        }
    }
}

