package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing insurance plans
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "insurance_plans")
public class InsurancePlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Insurance provider is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private InsuranceProvider provider;
    
    @NotBlank(message = "Plan name is required")
    @Column(name = "plan_name", nullable = false, length = 200)
    private String planName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;
    
    @NotNull(message = "Premium rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Premium rate must be greater than 0")
    @Column(name = "premium_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal premiumRate; // Rate per ₹1000 of coverage
    
    @Column(name = "min_coverage_amount", precision = 10, scale = 2)
    private BigDecimal minCoverageAmount;
    
    @Column(name = "max_coverage_amount", precision = 10, scale = 2)
    private BigDecimal maxCoverageAmount;
    
    @Column(name = "min_premium", precision = 10, scale = 2)
    private BigDecimal minPremium;
    
    @Column(name = "max_premium", precision = 10, scale = 2)
    private BigDecimal maxPremium;
    
    @Column(name = "coverage_duration_days")
    private Integer coverageDurationDays = 30; // Default 30 days coverage
    
    @Column(name = "age_min")
    private Integer ageMin = 0; // Minimum age for coverage
    
    @Column(name = "age_max")
    private Integer ageMax = 100; // Maximum age for coverage
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "popularity_score")
    private Integer popularityScore = 0; // For sorting popular plans
    
    // Coverage details
    @Column(name = "covers_medical_expenses")
    private Boolean coversMedicalExpenses = true;
    
    @Column(name = "covers_trip_cancellation")
    private Boolean coversTripCancellation = true;
    
    @Column(name = "covers_baggage_loss")
    private Boolean coversBaggageLoss = true;
    
    @Column(name = "covers_personal_accident")
    private Boolean coversPersonalAccident = true;
    
    @Column(name = "covers_emergency_evacuation")
    private Boolean coversEmergencyEvacuation = false;
    
    @Column(name = "covers_24x7_support")
    private Boolean covers24x7Support = true;
    
    // Limits and deductibles
    @Column(name = "medical_coverage_limit", precision = 10, scale = 2)
    private BigDecimal medicalCoverageLimit;
    
    @Column(name = "trip_cancellation_limit", precision = 10, scale = 2)
    private BigDecimal tripCancellationLimit;
    
    @Column(name = "baggage_coverage_limit", precision = 10, scale = 2)
    private BigDecimal baggageCoverageLimit;
    
    @Column(name = "personal_accident_limit", precision = 10, scale = 2)
    private BigDecimal personalAccidentLimit;
    
    @Column(name = "deductible_amount", precision = 10, scale = 2)
    private BigDecimal deductibleAmount = BigDecimal.ZERO;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public InsurancePlan() {}
    
    public InsurancePlan(InsuranceProvider provider, String planName, PlanType planType, BigDecimal premiumRate) {
        this.provider = provider;
        this.planName = planName;
        this.planType = planType;
        this.premiumRate = premiumRate;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public InsuranceProvider getProvider() {
        return provider;
    }
    
    public void setProvider(InsuranceProvider provider) {
        this.provider = provider;
    }
    
    public String getPlanName() {
        return planName;
    }
    
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PlanType getPlanType() {
        return planType;
    }
    
    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }
    
    public BigDecimal getPremiumRate() {
        return premiumRate;
    }
    
    public void setPremiumRate(BigDecimal premiumRate) {
        this.premiumRate = premiumRate;
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
    
    public BigDecimal getMinPremium() {
        return minPremium;
    }
    
    public void setMinPremium(BigDecimal minPremium) {
        this.minPremium = minPremium;
    }
    
    public BigDecimal getMaxPremium() {
        return maxPremium;
    }
    
    public void setMaxPremium(BigDecimal maxPremium) {
        this.maxPremium = maxPremium;
    }
    
    public Integer getCoverageDurationDays() {
        return coverageDurationDays;
    }
    
    public void setCoverageDurationDays(Integer coverageDurationDays) {
        this.coverageDurationDays = coverageDurationDays;
    }
    
    public Integer getAgeMin() {
        return ageMin;
    }
    
    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }
    
    public Integer getAgeMax() {
        return ageMax;
    }
    
    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
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
    
    public Integer getPopularityScore() {
        return popularityScore;
    }
    
    public void setPopularityScore(Integer popularityScore) {
        this.popularityScore = popularityScore;
    }
    
    public Boolean getCoversMedicalExpenses() {
        return coversMedicalExpenses;
    }
    
    public void setCoversMedicalExpenses(Boolean coversMedicalExpenses) {
        this.coversMedicalExpenses = coversMedicalExpenses;
    }
    
    public Boolean getCoversTripCancellation() {
        return coversTripCancellation;
    }
    
    public void setCoversTripCancellation(Boolean coversTripCancellation) {
        this.coversTripCancellation = coversTripCancellation;
    }
    
    public Boolean getCoversBaggageLoss() {
        return coversBaggageLoss;
    }
    
    public void setCoversBaggageLoss(Boolean coversBaggageLoss) {
        this.coversBaggageLoss = coversBaggageLoss;
    }
    
    public Boolean getCoversPersonalAccident() {
        return coversPersonalAccident;
    }
    
    public void setCoversPersonalAccident(Boolean coversPersonalAccident) {
        this.coversPersonalAccident = coversPersonalAccident;
    }
    
    public Boolean getCoversEmergencyEvacuation() {
        return coversEmergencyEvacuation;
    }
    
    public void setCoversEmergencyEvacuation(Boolean coversEmergencyEvacuation) {
        this.coversEmergencyEvacuation = coversEmergencyEvacuation;
    }
    
    public Boolean getCovers24x7Support() {
        return covers24x7Support;
    }
    
    public void setCovers24x7Support(Boolean covers24x7Support) {
        this.covers24x7Support = covers24x7Support;
    }
    
    public BigDecimal getMedicalCoverageLimit() {
        return medicalCoverageLimit;
    }
    
    public void setMedicalCoverageLimit(BigDecimal medicalCoverageLimit) {
        this.medicalCoverageLimit = medicalCoverageLimit;
    }
    
    public BigDecimal getTripCancellationLimit() {
        return tripCancellationLimit;
    }
    
    public void setTripCancellationLimit(BigDecimal tripCancellationLimit) {
        this.tripCancellationLimit = tripCancellationLimit;
    }
    
    public BigDecimal getBaggageCoverageLimit() {
        return baggageCoverageLimit;
    }
    
    public void setBaggageCoverageLimit(BigDecimal baggageCoverageLimit) {
        this.baggageCoverageLimit = baggageCoverageLimit;
    }
    
    public BigDecimal getPersonalAccidentLimit() {
        return personalAccidentLimit;
    }
    
    public void setPersonalAccidentLimit(BigDecimal personalAccidentLimit) {
        this.personalAccidentLimit = personalAccidentLimit;
    }
    
    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }
    
    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
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
     * Enum for plan types
     */
    public enum PlanType {
        BASIC("Basic", "Essential coverage for basic travel needs"),
        STANDARD("Standard", "Comprehensive coverage with standard benefits"),
        PREMIUM("Premium", "Premium coverage with enhanced benefits"),
        FAMILY("Family", "Family coverage for multiple travelers"),
        SENIOR("Senior", "Specialized coverage for senior citizens"),
        STUDENT("Student", "Affordable coverage for students");
        
        private final String displayName;
        private final String description;
        
        PlanType(String displayName, String description) {
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
