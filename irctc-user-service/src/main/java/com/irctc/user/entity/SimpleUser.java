package com.irctc.user.entity;

import com.irctc.user.tenant.TenantAware;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simple_users", indexes = {
    @Index(name = "idx_users_tenant_id", columnList = "tenantId")
})
@org.hibernate.annotations.Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
@EntityListeners(com.irctc.user.audit.EntityAuditListener.class)
public class SimpleUser implements TenantAware {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String roles;
    
    @Column(name = "referral_code", unique = true, length = 32)
    private String referralCode;
    
    @Column(name = "referred_by_user_id")
    private Long referredByUserId;
    
    @Column(name = "referral_points")
    private Integer referralPoints = 0;
    
    @Column(name = "tenant_id", length = 50)
    private String tenantId;
    
    @Column(nullable = false)
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
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    
    public Long getReferredByUserId() { return referredByUserId; }
    public void setReferredByUserId(Long referredByUserId) { this.referredByUserId = referredByUserId; }
    
    public Integer getReferralPoints() { return referralPoints; }
    public void setReferralPoints(Integer referralPoints) { this.referralPoints = referralPoints; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // TenantAware interface methods
    @Override
    public String getTenantId() { return tenantId; }
    
    @Override
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
