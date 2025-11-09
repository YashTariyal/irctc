package com.irctc.notification.entity;

import com.irctc.notification.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.FilterDef(
    name = "tenantFilter",
    parameters = @org.hibernate.annotations.ParamDef(name = "tenantId", type = String.class)
)
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@Data
public class SimpleNotification implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime sentTime;
    
    private String status;
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
