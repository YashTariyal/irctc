package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.LoyaltyAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for loyalty account response
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class LoyaltyAccountResponse {
    
    private Long id;
    private String loyaltyNumber;
    private String tier;
    private String tierDisplayName;
    private BigDecimal totalPoints;
    private BigDecimal availablePoints;
    private BigDecimal redeemedPoints;
    private BigDecimal expiredPoints;
    private BigDecimal totalSpent;
    private Integer totalBookings;
    private Boolean isActive;
    private LocalDateTime joinedDate;
    private LocalDateTime lastActivityDate;
    private LocalDateTime pointsExpiryDate;
    
    // Tier information
    private String nextTier;
    private String nextTierDisplayName;
    private BigDecimal pointsToNextTier;
    private Double tierMultiplier;
    private String tierDescription;
    
    // User information
    private String userName;
    private String userEmail;
    
    // Constructors
    public LoyaltyAccountResponse() {}
    
    public LoyaltyAccountResponse(LoyaltyAccount loyaltyAccount) {
        this.id = loyaltyAccount.getId();
        this.loyaltyNumber = loyaltyAccount.getLoyaltyNumber();
        this.tier = loyaltyAccount.getTier().name();
        this.tierDisplayName = loyaltyAccount.getTier().getDisplayName();
        this.totalPoints = loyaltyAccount.getTotalPoints();
        this.availablePoints = loyaltyAccount.getAvailablePoints();
        this.redeemedPoints = loyaltyAccount.getRedeemedPoints();
        this.expiredPoints = loyaltyAccount.getExpiredPoints();
        this.totalSpent = loyaltyAccount.getTotalSpent();
        this.totalBookings = loyaltyAccount.getTotalBookings();
        this.isActive = loyaltyAccount.getIsActive();
        this.joinedDate = loyaltyAccount.getJoinedDate();
        this.lastActivityDate = loyaltyAccount.getLastActivityDate();
        this.pointsExpiryDate = loyaltyAccount.getPointsExpiryDate();
        this.tierMultiplier = loyaltyAccount.getTier().getMultiplier();
        this.tierDescription = loyaltyAccount.getTier().getDescription();
        
        // Calculate next tier information
        calculateNextTierInfo(loyaltyAccount);
        
        // Set user information
        if (loyaltyAccount.getUser() != null) {
            this.userName = loyaltyAccount.getUser().getUsername();
            this.userEmail = loyaltyAccount.getUser().getEmail();
        }
    }
    
    private void calculateNextTierInfo(LoyaltyAccount loyaltyAccount) {
        LoyaltyAccount.LoyaltyTier currentTier = loyaltyAccount.getTier();
        LoyaltyAccount.LoyaltyTier[] tiers = LoyaltyAccount.LoyaltyTier.values();
        
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i] == currentTier) {
                LoyaltyAccount.LoyaltyTier nextTier = tiers[i + 1];
                this.nextTier = nextTier.name();
                this.nextTierDisplayName = nextTier.getDisplayName();
                this.pointsToNextTier = new BigDecimal(nextTier.getMinimumPoints())
                        .subtract(loyaltyAccount.getTotalPoints());
                break;
            }
        }
        
        // If already at highest tier
        if (this.nextTier == null) {
            this.nextTier = "MAX";
            this.nextTierDisplayName = "Maximum Tier";
            this.pointsToNextTier = BigDecimal.ZERO;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLoyaltyNumber() {
        return loyaltyNumber;
    }
    
    public void setLoyaltyNumber(String loyaltyNumber) {
        this.loyaltyNumber = loyaltyNumber;
    }
    
    public String getTier() {
        return tier;
    }
    
    public void setTier(String tier) {
        this.tier = tier;
    }
    
    public String getTierDisplayName() {
        return tierDisplayName;
    }
    
    public void setTierDisplayName(String tierDisplayName) {
        this.tierDisplayName = tierDisplayName;
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
    
    public String getNextTier() {
        return nextTier;
    }
    
    public void setNextTier(String nextTier) {
        this.nextTier = nextTier;
    }
    
    public String getNextTierDisplayName() {
        return nextTierDisplayName;
    }
    
    public void setNextTierDisplayName(String nextTierDisplayName) {
        this.nextTierDisplayName = nextTierDisplayName;
    }
    
    public BigDecimal getPointsToNextTier() {
        return pointsToNextTier;
    }
    
    public void setPointsToNextTier(BigDecimal pointsToNextTier) {
        this.pointsToNextTier = pointsToNextTier;
    }
    
    public Double getTierMultiplier() {
        return tierMultiplier;
    }
    
    public void setTierMultiplier(Double tierMultiplier) {
        this.tierMultiplier = tierMultiplier;
    }
    
    public String getTierDescription() {
        return tierDescription;
    }
    
    public void setTierDescription(String tierDescription) {
        this.tierDescription = tierDescription;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
