package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing reward redemptions
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "reward_redemptions")
public class RewardRedemption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Loyalty account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;
    
    @NotNull(message = "Reward is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;
    
    @Column(name = "redemption_code", unique = true, nullable = false)
    private String redemptionCode;
    
    @Column(name = "points_used", precision = 10, scale = 2, nullable = false)
    private BigDecimal pointsUsed;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RedemptionStatus status = RedemptionStatus.PENDING;
    
    @Column(name = "redemption_date")
    private LocalDateTime redemptionDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "used_date")
    private LocalDateTime usedDate;
    
    @Column(name = "booking_reference")
    private String bookingReference;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public RewardRedemption() {}
    
    public RewardRedemption(LoyaltyAccount loyaltyAccount, Reward reward, String redemptionCode, 
                          BigDecimal pointsUsed) {
        this.loyaltyAccount = loyaltyAccount;
        this.reward = reward;
        this.redemptionCode = redemptionCode;
        this.pointsUsed = pointsUsed;
        this.redemptionDate = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusDays(reward.getValidityDays() != null ? 
                                                      reward.getValidityDays() : 365);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LoyaltyAccount getLoyaltyAccount() {
        return loyaltyAccount;
    }
    
    public void setLoyaltyAccount(LoyaltyAccount loyaltyAccount) {
        this.loyaltyAccount = loyaltyAccount;
    }
    
    public Reward getReward() {
        return reward;
    }
    
    public void setReward(Reward reward) {
        this.reward = reward;
    }
    
    public String getRedemptionCode() {
        return redemptionCode;
    }
    
    public void setRedemptionCode(String redemptionCode) {
        this.redemptionCode = redemptionCode;
    }
    
    public BigDecimal getPointsUsed() {
        return pointsUsed;
    }
    
    public void setPointsUsed(BigDecimal pointsUsed) {
        this.pointsUsed = pointsUsed;
    }
    
    public RedemptionStatus getStatus() {
        return status;
    }
    
    public void setStatus(RedemptionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getRedemptionDate() {
        return redemptionDate;
    }
    
    public void setRedemptionDate(LocalDateTime redemptionDate) {
        this.redemptionDate = redemptionDate;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public LocalDateTime getUsedDate() {
        return usedDate;
    }
    
    public void setUsedDate(LocalDateTime usedDate) {
        this.usedDate = usedDate;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Enum for redemption status
     */
    public enum RedemptionStatus {
        PENDING("Pending", "Redemption pending approval"),
        ACTIVE("Active", "Redemption is active and can be used"),
        USED("Used", "Redemption has been used"),
        EXPIRED("Expired", "Redemption has expired"),
        CANCELLED("Cancelled", "Redemption has been cancelled");
        
        private final String displayName;
        private final String description;
        
        RedemptionStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
