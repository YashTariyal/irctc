package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking data export requests for GDPR compliance
 */
@Entity
@Table(
    name = "data_export_requests",
    indexes = {
        @Index(name = "idx_export_requests_user_id", columnList = "userId"),
        @Index(name = "idx_export_requests_status", columnList = "status"),
        @Index(name = "idx_export_requests_tenant_id", columnList = "tenantId")
    }
)
@Data
public class DataExportRequest implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 100)
    private String requestId; // Unique request identifier
    
    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @Column(name = "file_path", length = 500)
    private String filePath; // Path to exported data file
    
    @Column(name = "file_url", length = 500)
    private String fileUrl; // URL to download exported data
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Export file expiration time
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "data_categories", length = 500)
    private String dataCategories; // Comma-separated list of data categories exported
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (requestId == null) {
            requestId = "EXPORT_" + System.currentTimeMillis() + "_" + userId;
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

