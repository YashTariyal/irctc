package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing fare calculation rules
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "fare_rules")
public class FareRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Train is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;
    
    @NotNull(message = "Coach type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "coach_type", nullable = false)
    private Coach.CoachType coachType;
    
    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base fare must be greater than 0")
    @Column(name = "base_fare", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;
    
    @NotNull(message = "Distance is required")
    @Column(name = "distance_km", nullable = false)
    private Integer distanceKm;
    
    @Column(name = "tatkal_fare", precision = 10, scale = 2)
    private BigDecimal tatkalFare;
    
    @Column(name = "premium_tatkal_fare", precision = 10, scale = 2)
    private BigDecimal premiumTatkalFare;
    
    @Column(name = "ladies_quota_discount", precision = 5, scale = 2)
    private BigDecimal ladiesQuotaDiscount;
    
    @Column(name = "senior_citizen_discount", precision = 5, scale = 2)
    private BigDecimal seniorCitizenDiscount;
    
    @Column(name = "handicapped_discount", precision = 5, scale = 2)
    private BigDecimal handicappedDiscount;
    
    @Column(name = "surge_multiplier", precision = 3, scale = 2)
    private BigDecimal surgeMultiplier = BigDecimal.ONE;
    
    @Column(name = "peak_hour_multiplier", precision = 3, scale = 2)
    private BigDecimal peakHourMultiplier = BigDecimal.ONE;
    
    @Column(name = "weekend_multiplier", precision = 3, scale = 2)
    private BigDecimal weekendMultiplier = BigDecimal.ONE;
    
    @Column(name = "festival_multiplier", precision = 3, scale = 2)
    private BigDecimal festivalMultiplier = BigDecimal.ONE;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public FareRule() {}
    
    public FareRule(Train train, Coach.CoachType coachType, BigDecimal baseFare, Integer distanceKm) {
        this.train = train;
        this.coachType = coachType;
        this.baseFare = baseFare;
        this.distanceKm = distanceKm;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Train getTrain() {
        return train;
    }
    
    public void setTrain(Train train) {
        this.train = train;
    }
    
    public Coach.CoachType getCoachType() {
        return coachType;
    }
    
    public void setCoachType(Coach.CoachType coachType) {
        this.coachType = coachType;
    }
    
    public BigDecimal getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }
    
    public Integer getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public BigDecimal getTatkalFare() {
        return tatkalFare;
    }
    
    public void setTatkalFare(BigDecimal tatkalFare) {
        this.tatkalFare = tatkalFare;
    }
    
    public BigDecimal getPremiumTatkalFare() {
        return premiumTatkalFare;
    }
    
    public void setPremiumTatkalFare(BigDecimal premiumTatkalFare) {
        this.premiumTatkalFare = premiumTatkalFare;
    }
    
    public BigDecimal getLadiesQuotaDiscount() {
        return ladiesQuotaDiscount;
    }
    
    public void setLadiesQuotaDiscount(BigDecimal ladiesQuotaDiscount) {
        this.ladiesQuotaDiscount = ladiesQuotaDiscount;
    }
    
    public BigDecimal getSeniorCitizenDiscount() {
        return seniorCitizenDiscount;
    }
    
    public void setSeniorCitizenDiscount(BigDecimal seniorCitizenDiscount) {
        this.seniorCitizenDiscount = seniorCitizenDiscount;
    }
    
    public BigDecimal getHandicappedDiscount() {
        return handicappedDiscount;
    }
    
    public void setHandicappedDiscount(BigDecimal handicappedDiscount) {
        this.handicappedDiscount = handicappedDiscount;
    }
    
    public BigDecimal getSurgeMultiplier() {
        return surgeMultiplier;
    }
    
    public void setSurgeMultiplier(BigDecimal surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }
    
    public BigDecimal getPeakHourMultiplier() {
        return peakHourMultiplier;
    }
    
    public void setPeakHourMultiplier(BigDecimal peakHourMultiplier) {
        this.peakHourMultiplier = peakHourMultiplier;
    }
    
    public BigDecimal getWeekendMultiplier() {
        return weekendMultiplier;
    }
    
    public void setWeekendMultiplier(BigDecimal weekendMultiplier) {
        this.weekendMultiplier = weekendMultiplier;
    }
    
    public BigDecimal getFestivalMultiplier() {
        return festivalMultiplier;
    }
    
    public void setFestivalMultiplier(BigDecimal festivalMultiplier) {
        this.festivalMultiplier = festivalMultiplier;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getValidFrom() {
        return validFrom;
    }
    
    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }
    
    public LocalDateTime getValidUntil() {
        return validUntil;
    }
    
    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
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
