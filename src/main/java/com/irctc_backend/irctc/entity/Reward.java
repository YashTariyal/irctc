package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing rewards available for redemption
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "rewards")
public class Reward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Reward name is required")
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private RewardCategory category;
    
    @NotNull(message = "Points required is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Points required must be greater than 0")
    @Column(name = "points_required", precision = 10, scale = 2, nullable = false)
    private BigDecimal pointsRequired;
    
    @Column(name = "cash_value", precision = 10, scale = 2)
    private BigDecimal cashValue;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "validity_days")
    private Integer validityDays;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "redemption_limit")
    private Integer redemptionLimit;
    
    @Column(name = "redemption_count")
    private Integer redemptionCount = 0;
    
    @Column(name = "min_tier_required")
    private String minTierRequired = "BRONZE";
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "terms_conditions", length = 2000)
    private String termsConditions;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Reward() {}
    
    public Reward(String name, String description, RewardCategory category, BigDecimal pointsRequired) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.pointsRequired = pointsRequired;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public RewardCategory getCategory() {
        return category;
    }
    
    public void setCategory(RewardCategory category) {
        this.category = category;
    }
    
    public BigDecimal getPointsRequired() {
        return pointsRequired;
    }
    
    public void setPointsRequired(BigDecimal pointsRequired) {
        this.pointsRequired = pointsRequired;
    }
    
    public BigDecimal getCashValue() {
        return cashValue;
    }
    
    public void setCashValue(BigDecimal cashValue) {
        this.cashValue = cashValue;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }
    
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }
    
    public Integer getValidityDays() {
        return validityDays;
    }
    
    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsFeatured() {
        return isFeatured;
    }
    
    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
    
    public Integer getRedemptionLimit() {
        return redemptionLimit;
    }
    
    public void setRedemptionLimit(Integer redemptionLimit) {
        this.redemptionLimit = redemptionLimit;
    }
    
    public Integer getRedemptionCount() {
        return redemptionCount;
    }
    
    public void setRedemptionCount(Integer redemptionCount) {
        this.redemptionCount = redemptionCount;
    }
    
    public String getMinTierRequired() {
        return minTierRequired;
    }
    
    public void setMinTierRequired(String minTierRequired) {
        this.minTierRequired = minTierRequired;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getTermsConditions() {
        return termsConditions;
    }
    
    public void setTermsConditions(String termsConditions) {
        this.termsConditions = termsConditions;
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
     * Enum for reward categories
     */
    public enum RewardCategory {
        TRAVEL_VOUCHER("Travel Voucher", "Discount vouchers for train bookings"),
        CASHBACK("Cashback", "Cashback rewards"),
        UPGRADE("Upgrade", "Free seat/class upgrades"),
        MEAL_VOUCHER("Meal Voucher", "Free meal vouchers"),
        LOUNGE_ACCESS("Lounge Access", "Free lounge access at stations"),
        PRIORITY_BOOKING("Priority Booking", "Priority booking privileges"),
        BONUS_POINTS("Bonus Points", "Additional loyalty points"),
        MERCHANDISE("Merchandise", "IRCTC branded merchandise");
        
        private final String displayName;
        private final String description;
        
        RewardCategory(String displayName, String description) {
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
