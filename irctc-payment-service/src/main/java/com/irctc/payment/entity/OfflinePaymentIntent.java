package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "offline_payment_intents", indexes = {
    @Index(name = "idx_offline_payment_user", columnList = "userId"),
    @Index(name = "idx_offline_payment_status", columnList = "status"),
    @Index(name = "idx_offline_payment_tenant_id", columnList = "tenantId")
})
@Data
public class OfflinePaymentIntent implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, length = 30)
    private String paymentMethod;

    @Column(length = 50)
    private String gatewayPreference;

    @Column(nullable = false, length = 40)
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private Long processedPaymentId;

    private LocalDateTime queuedAt;
    private LocalDateTime lastAttemptAt;
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

