package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to track refund status and history
 */
@Entity
@Table(name = "refund_status", indexes = {
    @Index(name = "idx_refund_status_payment_id", columnList = "paymentId"),
    @Index(name = "idx_refund_status_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_refund_status_status", columnList = "status")
})
@org.hibernate.annotations.FilterDef(
    name = "tenantFilter",
    parameters = @org.hibernate.annotations.ParamDef(name = "tenantId", type = String.class)
)
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.payment.audit.EntityAuditListener.class)
@Data
public class RefundStatus implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long paymentId;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;
    
    @Column(nullable = false, length = 50)
    private String status; // INITIATED, PROCESSING, COMPLETED, FAILED, PARTIALLY_REFUNDED
    
    @Column(length = 100)
    private String refundId; // Internal refund ID
    
    @Column(name = "gateway_refund_id", length = 100)
    private String gatewayRefundId; // Gateway's refund ID
    
    @Column(length = 50)
    private String gatewayName;
    
    @Column(length = 500)
    private String reason;
    
    @Column(name = "refund_policy_applied", length = 100)
    private String refundPolicyApplied; // Which policy was applied
    
    @Column(name = "refund_percentage", precision = 5, scale = 2)
    private BigDecimal refundPercentage; // Percentage of original amount refunded
    
    @Column(name = "cancellation_time")
    private LocalDateTime cancellationTime;
    
    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(name = "reconciliation_status", length = 50)
    private String reconciliationStatus; // PENDING, RECONCILED, MISMATCH
    
    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (initiatedAt == null) {
            initiatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if ("COMPLETED".equals(status) && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}

