package com.irctc.payment.entity;

import com.irctc.payment.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.payment.audit.EntityAuditListener.class)
@Data
public class SimplePayment implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long bookingId;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private String currency;
    
    @Column(nullable = false)
    private String paymentMethod;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "gateway_name", length = 50)
    private String gatewayName; // RAZORPAY, STRIPE, PAYU, etc.
    
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId; // Gateway's transaction ID
    
    @Column(name = "gateway_fee")
    private Double gatewayFee; // Fee charged by gateway
    
    private LocalDateTime paymentTime;
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
