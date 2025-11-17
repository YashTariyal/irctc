package com.irctc.booking.entity;

import com.irctc.booking.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "offline_actions", indexes = {
    @Index(name = "idx_offline_action_user", columnList = "userId"),
    @Index(name = "idx_offline_action_status", columnList = "status"),
    @Index(name = "idx_offline_action_tenant_id", columnList = "tenantId")
})
@Data
public class OfflineAction implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long bookingId;

    @Column(nullable = false, length = 100)
    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 40)
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private LocalDateTime queuedAt;
    private LocalDateTime processedAt;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        queuedAt = queuedAt == null ? LocalDateTime.now() : queuedAt;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

