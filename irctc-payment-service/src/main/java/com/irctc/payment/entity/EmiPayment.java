package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emi_payments", indexes = {
    @Index(name = "idx_emi_payment_plan", columnList = "paymentPlanId"),
    @Index(name = "idx_emi_payment_status", columnList = "status")
})
@Getter
@Setter
public class EmiPayment implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paymentPlanId", nullable = false)
    private Long paymentPlanId;

    @Column(nullable = false)
    private Integer installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid;

    private LocalDate paymentDate;

    @Column(nullable = false, length = 40)
    private String status;

    private String paymentReference;

    @Column(precision = 12, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(columnDefinition = "TEXT")
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

