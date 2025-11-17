package com.irctc.train.entity;

import com.irctc.train.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts", indexes = {
    @Index(name = "idx_price_alerts_user", columnList = "userId"),
    @Index(name = "idx_price_alerts_status", columnList = "status"),
    @Index(name = "idx_price_alerts_train", columnList = "trainNumber"),
    @Index(name = "idx_price_alerts_tenant", columnList = "tenantId")
})
@Data
public class PriceAlert implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String email;
    private String phoneNumber;

    private String trainNumber;
    private String sourceStation;
    private String destinationStation;
    private LocalDate travelDate;

    @Column(nullable = false, length = 40)
    private String alertType; // PRICE_DROP, AVAILABILITY

    @Column(length = 50)
    private String notificationChannel; // PUSH, EMAIL, SMS

    @Column(precision = 10, scale = 2)
    private BigDecimal targetPrice;

    private Integer minAvailability;

    @Column(nullable = false, length = 40)
    private String status = "ACTIVE"; // ACTIVE, TRIGGERED, CANCELLED

    @Column(length = 40)
    private String recurrence = "ONE_TIME"; // ONE_TIME, RECURRING

    private LocalDateTime lastTriggeredAt;

    private String metadata;

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

