package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity for tracking user consents for GDPR compliance
 */
@Entity
@Table(
    name = "user_consents",
    indexes = {
        @Index(name = "idx_consents_user_id", columnList = "userId"),
        @Index(name = "idx_consents_consent_type", columnList = "consentType"),
        @Index(name = "idx_consents_tenant_id", columnList = "tenantId")
    }
)
@Data
public class UserConsent implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 50)
    private String consentType; // MARKETING, ANALYTICS, COOKIES, THIRD_PARTY, DATA_PROCESSING
    
    @Column(nullable = false)
    private Boolean granted; // true if consent granted, false if withdrawn
    
    @Column(length = 1000)
    private String purpose; // Purpose of consent
    
    @Column(length = 500)
    private String version; // Version of consent terms
    
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;
    
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress; // IP address when consent was given
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // User agent when consent was given
    
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

