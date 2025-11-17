package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mobile_wallet_transactions", indexes = {
    @Index(name = "idx_wallet_tx_user", columnList = "userId"),
    @Index(name = "idx_wallet_tx_status", columnList = "status")
})
@Getter
@Setter
public class MobileWalletTransaction implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String walletReference;

    @Column(nullable = false, length = 50)
    private String walletProvider;

    private Long bookingId;
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(columnDefinition = "TEXT")
    private String deviceInfo;

    @Column(length = 10)
    private String tokenLastFour;

    @Column(nullable = false, length = 30)
    private String status;

    private LocalDateTime processedAt;

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

