package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing loyalty points transactions
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "loyalty_transactions")
public class LoyaltyTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Loyalty account is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "points", precision = 10, scale = 2, nullable = false)
    private BigDecimal points;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "reference_id")
    private String referenceId; // Booking ID, Promotion ID, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "is_expired")
    private Boolean isExpired = false;
    
    @Column(name = "tier_at_transaction")
    private String tierAtTransaction;
    
    @Column(name = "multiplier_applied", precision = 3, scale = 2)
    private BigDecimal multiplierApplied = BigDecimal.ONE;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public LoyaltyTransaction() {}
    
    public LoyaltyTransaction(LoyaltyAccount loyaltyAccount, TransactionType transactionType, 
                            BigDecimal points, String description) {
        this.loyaltyAccount = loyaltyAccount;
        this.transactionType = transactionType;
        this.points = points;
        this.description = description;
        this.tierAtTransaction = loyaltyAccount.getTier().getDisplayName();
        this.expiryDate = LocalDateTime.now().plusYears(2); // Points expire in 2 years
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
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getPoints() {
        return points;
    }
    
    public void setPoints(BigDecimal points) {
        this.points = points;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public ReferenceType getReferenceType() {
        return referenceType;
    }
    
    public void setReferenceType(ReferenceType referenceType) {
        this.referenceType = referenceType;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Boolean getIsExpired() {
        return isExpired;
    }
    
    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }
    
    public String getTierAtTransaction() {
        return tierAtTransaction;
    }
    
    public void setTierAtTransaction(String tierAtTransaction) {
        this.tierAtTransaction = tierAtTransaction;
    }
    
    public BigDecimal getMultiplierApplied() {
        return multiplierApplied;
    }
    
    public void setMultiplierApplied(BigDecimal multiplierApplied) {
        this.multiplierApplied = multiplierApplied;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Enum for transaction types
     */
    public enum TransactionType {
        EARNED("Earned", "Points earned from booking"),
        REDEEMED("Redeemed", "Points redeemed for rewards"),
        EXPIRED("Expired", "Points expired due to inactivity"),
        BONUS("Bonus", "Bonus points from promotions"),
        ADJUSTMENT("Adjustment", "Manual adjustment by admin"),
        REFUND("Refund", "Points refunded due to cancellation");
        
        private final String displayName;
        private final String description;
        
        TransactionType(String displayName, String description) {
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
    
    /**
     * Enum for reference types
     */
    public enum ReferenceType {
        BOOKING("Booking", "Points from train booking"),
        PROMOTION("Promotion", "Points from promotional offer"),
        REFERRAL("Referral", "Points from referring friends"),
        BIRTHDAY("Birthday", "Birthday bonus points"),
        ANNIVERSARY("Anniversary", "Anniversary bonus points"),
        ADMIN("Admin", "Manual adjustment by administrator");
        
        private final String displayName;
        private final String description;
        
        ReferenceType(String displayName, String description) {
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
