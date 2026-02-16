package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to store linked social accounts (Google, Facebook, Apple)
 */
@Entity
@Table(name = "social_accounts", indexes = {
    @Index(name = "idx_social_accounts_user_id", columnList = "userId"),
    @Index(name = "idx_social_accounts_provider_id", columnList = "provider,providerUserId"),
    @Index(name = "idx_social_accounts_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.user.audit.EntityAuditListener.class)
public class SocialAccount implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId; // Foreign key to SimpleUser
    
    @Column(nullable = false, length = 50)
    private String provider; // GOOGLE, FACEBOOK, APPLE
    
    @Column(nullable = false, name = "provider_user_id")
    private String providerUserId; // User ID from the provider
    
    @Column(name = "provider_email")
    private String providerEmail; // Email from the provider
    
    @Column(name = "provider_name")
    private String providerName; // Display name from the provider
    
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken; // OAuth2 access token (encrypted in production)
    
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken; // OAuth2 refresh token (encrypted in production)
    
    @Column(name = "id_token", columnDefinition = "TEXT")
    private String idToken; // ID token (for Apple Sign-In)
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "picture_url")
    private String pictureUrl; // Profile picture URL
    
    @Column(nullable = false)
    private Boolean active; // Whether this social account is active
    
    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (linkedAt == null) {
            linkedAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }
    
    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public LocalDateTime getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(LocalDateTime tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    
    public String getPictureUrl() { return pictureUrl; }
    public void setPictureUrl(String pictureUrl) { this.pictureUrl = pictureUrl; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getLinkedAt() { return linkedAt; }
    public void setLinkedAt(LocalDateTime linkedAt) { this.linkedAt = linkedAt; }
    
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
    
    // TenantAware interface methods
    @Override
    public String getTenantId() { return tenantId; }
    
    @Override
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}

