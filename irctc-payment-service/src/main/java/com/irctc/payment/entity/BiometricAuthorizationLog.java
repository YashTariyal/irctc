package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "biometric_authorization_logs", indexes = {
    @Index(name = "idx_biometric_auth_user", columnList = "userId"),
    @Index(name = "idx_biometric_auth_status", columnList = "status")
})
@Getter
@Setter
public class BiometricAuthorizationLog implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 100)
    private String verificationId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

