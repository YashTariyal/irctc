package com.irctc.notification.entity;

import com.irctc.notification.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for scheduled notifications
 */
@Entity
@Table(name = "scheduled_notifications", indexes = {
    @Index(name = "idx_scheduled_notifications_user_id", columnList = "userId"),
    @Index(name = "idx_scheduled_notifications_status_time", columnList = "status,scheduledTime"),
    @Index(name = "idx_scheduled_notifications_tenant_id", columnList = "tenantId")
})
@Data
public class ScheduledNotification implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String notificationType;
    
    @Column(nullable = false)
    private String channel; // EMAIL, SMS, WHATSAPP, PUSH
    
    @Column(nullable = false)
    private String recipient;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    private String templateId;
    
    @Column(columnDefinition = "TEXT")
    private String templateVariables; // JSON string
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column(nullable = false)
    private String status; // SCHEDULED, PROCESSING, SENT, FAILED, CANCELLED
    
    private String priority; // HIGH, MEDIUM, LOW
    
    private LocalDateTime sentTime;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

