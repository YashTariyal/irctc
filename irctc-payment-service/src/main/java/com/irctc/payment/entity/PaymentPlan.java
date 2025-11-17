package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment_plans", indexes = {
    @Index(name = "idx_payment_plans_booking", columnList = "bookingId"),
    @Index(name = "idx_payment_plans_user", columnList = "userId"),
    @Index(name = "idx_payment_plans_status", columnList = "status")
})
@Getter
@Setter
public class PaymentPlan implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal downPayment;

    @Column(precision = 12, scale = 2)
    private BigDecimal emiAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(nullable = false)
    private Integer installments;

    @Column(length = 20)
    private String frequency = "MONTHLY";

    @Column(nullable = false, length = 40)
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;

    private String gatewayReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

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

