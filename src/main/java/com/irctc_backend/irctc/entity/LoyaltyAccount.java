package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing user loyalty account
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "loyalty_accounts")
public class LoyaltyAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "User is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "loyalty_number", unique = true, nullable = false)
    private String loyaltyNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private LoyaltyTier tier = LoyaltyTier.BRONZE;
    
    @Column(name = "total_points", precision = 10, scale = 2)
    private BigDecimal totalPoints = BigDecimal.ZERO;
    
    @Column(name = "available_points", precision = 10, scale = 2)
    private BigDecimal availablePoints = BigDecimal.ZERO;
    
    @Column(name = "redeemed_points", precision = 10, scale = 2)
    private BigDecimal redeemedPoints = BigDecimal.ZERO;
    
    @Column(name = "expired_points", precision = 10, scale = 2)
    private BigDecimal expiredPoints = BigDecimal.ZERO;
    
    @Column(name = "total_spent", precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;
    
    @Column(name = "total_bookings")
    private Integer totalBookings = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "joined_date")
    private LocalDateTime joinedDate;
    
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;
    
    @Column(name = "points_expiry_date")
    private LocalDateTime pointsExpiryDate;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public LoyaltyAccount() {}
    
    public LoyaltyAccount(User user, String loyaltyNumber) {
        this.user = user;
        this.loyaltyNumber = loyaltyNumber;
        this.joinedDate = LocalDateTime.now();
        this.lastActivityDate = LocalDateTime.now();
        this.pointsExpiryDate = LocalDateTime.now().plusYears(2); // Points expire in 2 years
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getLoyaltyNumber() {
        return loyaltyNumber;
    }
    
    public void setLoyaltyNumber(String loyaltyNumber) {
        this.loyaltyNumber = loyaltyNumber;
    }
    
    public LoyaltyTier getTier() {
        return tier;
    }
    
    public void setTier(LoyaltyTier tier) {
        this.tier = tier;
    }
    
    public BigDecimal getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public BigDecimal getAvailablePoints() {
        return availablePoints;
    }
    
    public void setAvailablePoints(BigDecimal availablePoints) {
        this.availablePoints = availablePoints;
    }
    
    public BigDecimal getRedeemedPoints() {
        return redeemedPoints;
    }
    
    public void setRedeemedPoints(BigDecimal redeemedPoints) {
        this.redeemedPoints = redeemedPoints;
    }
    
    public BigDecimal getExpiredPoints() {
        return expiredPoints;
    }
    
    public void setExpiredPoints(BigDecimal expiredPoints) {
        this.expiredPoints = expiredPoints;
    }
    
    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }
    
    public Integer getTotalBookings() {
        return totalBookings;
    }
    
    public void setTotalBookings(Integer totalBookings) {
        this.totalBookings = totalBookings;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }
    
    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }
    
    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(LocalDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public LocalDateTime getPointsExpiryDate() {
        return pointsExpiryDate;
    }
    
    public void setPointsExpiryDate(LocalDateTime pointsExpiryDate) {
        this.pointsExpiryDate = pointsExpiryDate;
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
     * Enum for loyalty tiers
     */
    public enum LoyaltyTier {
        BRONZE("Bronze", 0, 1.0, "Basic benefits"),
        SILVER("Silver", 10000, 1.2, "Enhanced benefits"),
        GOLD("Gold", 25000, 1.5, "Premium benefits"),
        PLATINUM("Platinum", 50000, 2.0, "Elite benefits"),
        DIAMOND("Diamond", 100000, 2.5, "Ultimate benefits");
        
        private final String displayName;
        private final Integer minimumPoints;
        private final Double multiplier;
        private final String description;
        
        LoyaltyTier(String displayName, Integer minimumPoints, Double multiplier, String description) {
            this.displayName = displayName;
            this.minimumPoints = minimumPoints;
            this.multiplier = multiplier;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public Integer getMinimumPoints() {
            return minimumPoints;
        }
        
        public Double getMultiplier() {
            return multiplier;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
