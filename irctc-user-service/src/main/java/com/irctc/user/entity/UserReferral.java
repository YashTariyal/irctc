package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_referrals", indexes = {
    @Index(name = "idx_user_referrals_referrer", columnList = "referrerUserId"),
    @Index(name = "idx_user_referrals_referred", columnList = "referredUserId"),
    @Index(name = "idx_user_referrals_status", columnList = "status")
})
@EntityListeners(com.irctc.user.audit.EntityAuditListener.class)
@Getter
@Setter
public class UserReferral implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long referrerUserId;

    private Long referredUserId;

    @Column(length = 32)
    private String referralCodeUsed;

    private Long bookingId;

    @Column(nullable = false, length = 40)
    private String status; // REGISTERED, BOOKED, REWARDED

    private Integer rewardPoints = 0;

    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

