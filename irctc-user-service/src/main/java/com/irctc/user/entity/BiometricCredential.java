package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_credentials", indexes = {
    @Index(name = "idx_biometric_user", columnList = "userId"),
    @Index(name = "idx_biometric_status", columnList = "status")
})
@Getter
@Setter
public class BiometricCredential implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 50)
    private String biometricType;

    @Column(nullable = false, length = 256)
    private String templateHash;

    @Column(columnDefinition = "TEXT")
    private String publicKey;

    @Column(columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime registeredAt;
    private LocalDateTime lastVerifiedAt;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.registeredAt == null) {
            this.registeredAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

