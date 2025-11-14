package com.irctc.notification.entity;

import com.irctc.notification.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for storing user device tokens for push notifications
 */
@Entity
@Table(name = "user_device_tokens", indexes = {
    @Index(name = "idx_device_tokens_user_id", columnList = "userId"),
    @Index(name = "idx_device_tokens_token", columnList = "token"),
    @Index(name = "idx_device_tokens_tenant_id", columnList = "tenantId")
})
@Data
public class UserDeviceToken implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token; // FCM token
    
    @Column(nullable = false)
    private String platform; // ANDROID, IOS
    
    @Column(length = 200)
    private String deviceId; // Device identifier
    
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

