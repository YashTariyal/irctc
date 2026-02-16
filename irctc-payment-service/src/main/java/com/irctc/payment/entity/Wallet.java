package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Entity
 * Represents a user's digital wallet for storing money
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallets_user_id", columnList = "userId"),
    @Index(name = "idx_wallets_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.payment.audit.EntityAuditListener.class)
@Data
public class Wallet implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String userId; // User ID from User Service
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalTopUp = BigDecimal.ZERO; // Total amount ever added
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO; // Total amount ever spent
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastTransactionAt;
    
    @Version
    private Long version; // Optimistic locking for concurrent updates
    
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

