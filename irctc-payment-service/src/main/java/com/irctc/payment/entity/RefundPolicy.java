package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity for configurable refund policies based on cancellation time
 */
@Entity
@Table(name = "refund_policies", indexes = {
    @Index(name = "idx_refund_policies_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_refund_policies_active", columnList = "active")
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
public class RefundPolicy implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name; // e.g., "Full Refund - 48 hours before"
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "hours_before_departure", nullable = false)
    private Integer hoursBeforeDeparture; // Hours before departure for this policy
    
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage; // Percentage of amount to refund (0-100)
    
    @Column(name = "fixed_charges", precision = 10, scale = 2)
    private BigDecimal fixedCharges; // Fixed charges deducted from refund
    
    @Column(name = "gateway_fee_refundable")
    private Boolean gatewayFeeRefundable; // Whether gateway fee is refundable
    
    @Column(nullable = false)
    private Boolean active; // Whether this policy is active
    
    @Column(name = "priority", nullable = false)
    private Integer priority; // Lower number = higher priority (checked first)
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (priority == null) {
            priority = 100;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this policy applies to the given cancellation time
     */
    public boolean appliesTo(LocalDateTime cancellationTime, LocalDateTime departureTime) {
        if (!active) {
            return false;
        }
        
        Duration duration = Duration.between(cancellationTime, departureTime);
        long hours = duration.toHours();
        
        return hours >= hoursBeforeDeparture;
    }
    
    /**
     * Calculate refund amount based on this policy
     */
    public BigDecimal calculateRefundAmount(BigDecimal originalAmount, BigDecimal gatewayFee) {
        BigDecimal percentageRefund = originalAmount
            .multiply(refundPercentage)
            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        BigDecimal totalRefund = percentageRefund;
        
        // Deduct fixed charges
        if (fixedCharges != null && fixedCharges.compareTo(BigDecimal.ZERO) > 0) {
            totalRefund = totalRefund.subtract(fixedCharges);
        }
        
        // Add gateway fee if refundable
        if (gatewayFeeRefundable != null && gatewayFeeRefundable && gatewayFee != null) {
            totalRefund = totalRefund.add(gatewayFee);
        }
        
        // Ensure refund doesn't exceed original amount
        return totalRefund.min(originalAmount).max(BigDecimal.ZERO);
    }
}

