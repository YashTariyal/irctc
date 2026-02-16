package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet Transaction Entity
 * Records all wallet transactions (top-up, payment, transfer, refund)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wallet_txn_wallet_id", columnList = "walletId"),
    @Index(name = "idx_wallet_txn_user_id", columnList = "userId"),
    @Index(name = "idx_wallet_txn_type", columnList = "transactionType"),
    @Index(name = "idx_wallet_txn_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_wallet_txn_created", columnList = "createdAt")
})
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.payment.audit.EntityAuditListener.class)
@Data
public class WalletTransaction implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long walletId;
    
    @Column(nullable = false, length = 100)
    private String userId;
    
    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceBefore;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceAfter;
    
    @Column(nullable = false, length = 100)
    private String transactionId; // Unique transaction ID
    
    @Column(length = 50)
    private String status; // SUCCESS, FAILED, PENDING
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 100)
    private String referenceId; // Reference to payment/booking/transfer
    
    @Column(length = 50)
    private String referenceType; // PAYMENT, TOP_UP, TRANSFER, REFUND
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (transactionId == null) {
            transactionId = java.util.UUID.randomUUID().toString();
        }
        if (status == null) {
            status = "SUCCESS";
        }
    }
    
    /**
     * Transaction Types
     */
    public enum TransactionType {
        TOP_UP,           // Money added to wallet
        PAYMENT,          // Payment made using wallet
        REFUND,           // Refund credited to wallet
        TRANSFER,         // Transfer to another wallet
        TRANSFER_RECEIVED, // Money received from another wallet
        CASHBACK,         // Cashback credited
        ADJUSTMENT,        // Manual adjustment
        WITHDRAWAL        // Money withdrawn from wallet
    }
}

