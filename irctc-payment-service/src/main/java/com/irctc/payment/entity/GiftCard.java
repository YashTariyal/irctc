package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_cards", indexes = {
    @Index(name = "idx_gift_card_status", columnList = "status")
})
@Getter
@Setter
public class GiftCard implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    private Long purchaserUserId;

    @Column(length = 150)
    private String recipientEmail;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal initialAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAmount;

    @Column(nullable = false, length = 30)
    private String status;

    private LocalDateTime expiresAt;

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

