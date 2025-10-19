package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing insurance providers
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "insurance_providers")
public class InsuranceProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Provider name is required")
    @Column(name = "provider_name", nullable = false, length = 200)
    private String providerName;
    
    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;
    
    @Column(name = "website_url")
    private String websiteUrl;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @NotNull(message = "Base premium rate is required")
    @Column(name = "base_premium_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal basePremiumRate; // Rate per ₹1000 of coverage
    
    @Column(name = "min_coverage_amount", precision = 10, scale = 2)
    private BigDecimal minCoverageAmount = new BigDecimal("10000"); // Minimum ₹10,000
    
    @Column(name = "max_coverage_amount", precision = 10, scale = 2)
    private BigDecimal maxCoverageAmount = new BigDecimal("1000000"); // Maximum ₹10,00,000
    
    @Column(name = "claim_settlement_ratio", precision = 5, scale = 2)
    private BigDecimal claimSettlementRatio = new BigDecimal("95.00"); // 95% settlement ratio
    
    @Column(name = "average_settlement_days")
    private Integer averageSettlementDays = 7; // Average 7 days for claim settlement
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("4.50"); // Out of 5.0
    
    @Column(name = "total_policies_sold")
    private Long totalPoliciesSold = 0L;
    
    @Column(name = "total_claims_processed")
    private Long totalClaimsProcessed = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public InsuranceProvider() {}
    
    public InsuranceProvider(String providerName, String companyName, String contactEmail, 
                           String contactPhone, BigDecimal basePremiumRate) {
        this.providerName = providerName;
        this.companyName = companyName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.basePremiumRate = basePremiumRate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContactEmail() {
        return contactEmail;
    }
    
    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public BigDecimal getBasePremiumRate() {
        return basePremiumRate;
    }
    
    public void setBasePremiumRate(BigDecimal basePremiumRate) {
        this.basePremiumRate = basePremiumRate;
    }
    
    public BigDecimal getMinCoverageAmount() {
        return minCoverageAmount;
    }
    
    public void setMinCoverageAmount(BigDecimal minCoverageAmount) {
        this.minCoverageAmount = minCoverageAmount;
    }
    
    public BigDecimal getMaxCoverageAmount() {
        return maxCoverageAmount;
    }
    
    public void setMaxCoverageAmount(BigDecimal maxCoverageAmount) {
        this.maxCoverageAmount = maxCoverageAmount;
    }
    
    public BigDecimal getClaimSettlementRatio() {
        return claimSettlementRatio;
    }
    
    public void setClaimSettlementRatio(BigDecimal claimSettlementRatio) {
        this.claimSettlementRatio = claimSettlementRatio;
    }
    
    public Integer getAverageSettlementDays() {
        return averageSettlementDays;
    }
    
    public void setAverageSettlementDays(Integer averageSettlementDays) {
        this.averageSettlementDays = averageSettlementDays;
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
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    
    public Long getTotalPoliciesSold() {
        return totalPoliciesSold;
    }
    
    public void setTotalPoliciesSold(Long totalPoliciesSold) {
        this.totalPoliciesSold = totalPoliciesSold;
    }
    
    public Long getTotalClaimsProcessed() {
        return totalClaimsProcessed;
    }
    
    public void setTotalClaimsProcessed(Long totalClaimsProcessed) {
        this.totalClaimsProcessed = totalClaimsProcessed;
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
}
