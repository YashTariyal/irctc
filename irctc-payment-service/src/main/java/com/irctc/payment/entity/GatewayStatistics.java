package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking payment gateway statistics
 */
@Entity
@Table(name = "gateway_statistics", indexes = {
    @Index(name = "idx_gateway_stats_name", columnList = "gatewayName")
})
@Data
public class GatewayStatistics implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String gatewayName;
    
    @Column(nullable = false)
    private Long totalTransactions = 0L;
    
    @Column(nullable = false)
    private Long successfulTransactions = 0L;
    
    @Column(nullable = false)
    private Long failedTransactions = 0L;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFees = BigDecimal.ZERO;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionTime;
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

