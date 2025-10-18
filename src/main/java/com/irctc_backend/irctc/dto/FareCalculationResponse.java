package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Coach;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for fare calculation response
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class FareCalculationResponse {
    
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Coach.CoachType coachType;
    private LocalDateTime journeyDate;
    private Integer numberOfPassengers;
    private Integer distanceKm;
    private String quotaType;
    
    // Fare breakdown
    private BigDecimal baseFare;
    private BigDecimal tatkalFare;
    private BigDecimal premiumTatkalFare;
    private BigDecimal surgeFare;
    private BigDecimal peakHourFare;
    private BigDecimal weekendFare;
    private BigDecimal festivalFare;
    private BigDecimal ladiesQuotaDiscount;
    private BigDecimal seniorCitizenDiscount;
    private BigDecimal handicappedDiscount;
    
    // Final amounts
    private BigDecimal subtotalFare;
    private BigDecimal totalDiscount;
    private BigDecimal totalFare;
    private BigDecimal gstAmount;
    private BigDecimal finalAmount;
    
    // Surge pricing details
    private BigDecimal surgeMultiplier;
    private BigDecimal peakHourMultiplier;
    private BigDecimal weekendMultiplier;
    private BigDecimal festivalMultiplier;
    private String surgeReason;
    private Boolean isSurgeActive;
    
    // Passenger-wise breakdown
    private List<PassengerFare> passengerFares;
    
    // Additional information
    private String fareRuleId;
    private LocalDateTime calculatedAt;
    private String currency = "INR";
    
    // Constructors
    public FareCalculationResponse() {}
    
    public FareCalculationResponse(Long trainId, String trainNumber, String trainName, 
                                  Coach.CoachType coachType, LocalDateTime journeyDate) {
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.coachType = coachType;
        this.journeyDate = journeyDate;
        this.calculatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getTrainId() {
        return trainId;
    }
    
    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }
    
    public String getTrainNumber() {
        return trainNumber;
    }
    
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }
    
    public String getTrainName() {
        return trainName;
    }
    
    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }
    
    public Coach.CoachType getCoachType() {
        return coachType;
    }
    
    public void setCoachType(Coach.CoachType coachType) {
        this.coachType = coachType;
    }
    
    public LocalDateTime getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(LocalDateTime journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public Integer getNumberOfPassengers() {
        return numberOfPassengers;
    }
    
    public void setNumberOfPassengers(Integer numberOfPassengers) {
        this.numberOfPassengers = numberOfPassengers;
    }
    
    public Integer getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Integer distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public String getQuotaType() {
        return quotaType;
    }
    
    public void setQuotaType(String quotaType) {
        this.quotaType = quotaType;
    }
    
    public BigDecimal getBaseFare() {
        return baseFare;
    }
    
    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
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
    
    public BigDecimal getSurgeFare() {
        return surgeFare;
    }
    
    public void setSurgeFare(BigDecimal surgeFare) {
        this.surgeFare = surgeFare;
    }
    
    public BigDecimal getPeakHourFare() {
        return peakHourFare;
    }
    
    public void setPeakHourFare(BigDecimal peakHourFare) {
        this.peakHourFare = peakHourFare;
    }
    
    public BigDecimal getWeekendFare() {
        return weekendFare;
    }
    
    public void setWeekendFare(BigDecimal weekendFare) {
        this.weekendFare = weekendFare;
    }
    
    public BigDecimal getFestivalFare() {
        return festivalFare;
    }
    
    public void setFestivalFare(BigDecimal festivalFare) {
        this.festivalFare = festivalFare;
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
    
    public BigDecimal getSubtotalFare() {
        return subtotalFare;
    }
    
    public void setSubtotalFare(BigDecimal subtotalFare) {
        this.subtotalFare = subtotalFare;
    }
    
    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }
    
    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }
    
    public BigDecimal getTotalFare() {
        return totalFare;
    }
    
    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }
    
    public BigDecimal getGstAmount() {
        return gstAmount;
    }
    
    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
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
    
    public String getSurgeReason() {
        return surgeReason;
    }
    
    public void setSurgeReason(String surgeReason) {
        this.surgeReason = surgeReason;
    }
    
    public Boolean getIsSurgeActive() {
        return isSurgeActive;
    }
    
    public void setIsSurgeActive(Boolean isSurgeActive) {
        this.isSurgeActive = isSurgeActive;
    }
    
    public List<PassengerFare> getPassengerFares() {
        return passengerFares;
    }
    
    public void setPassengerFares(List<PassengerFare> passengerFares) {
        this.passengerFares = passengerFares;
    }
    
    public String getFareRuleId() {
        return fareRuleId;
    }
    
    public void setFareRuleId(String fareRuleId) {
        this.fareRuleId = fareRuleId;
    }
    
    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }
    
    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    /**
     * Inner class for passenger-wise fare breakdown
     */
    public static class PassengerFare {
        private String passengerName;
        private Integer passengerAge;
        private String passengerGender;
        private BigDecimal baseFare;
        private BigDecimal applicableDiscount;
        private BigDecimal finalFare;
        private String discountReason;
        
        // Constructors
        public PassengerFare() {}
        
        public PassengerFare(String passengerName, Integer passengerAge, String passengerGender) {
            this.passengerName = passengerName;
            this.passengerAge = passengerAge;
            this.passengerGender = passengerGender;
        }
        
        // Getters and Setters
        public String getPassengerName() {
            return passengerName;
        }
        
        public void setPassengerName(String passengerName) {
            this.passengerName = passengerName;
        }
        
        public Integer getPassengerAge() {
            return passengerAge;
        }
        
        public void setPassengerAge(Integer passengerAge) {
            this.passengerAge = passengerAge;
        }
        
        public String getPassengerGender() {
            return passengerGender;
        }
        
        public void setPassengerGender(String passengerGender) {
            this.passengerGender = passengerGender;
        }
        
        public BigDecimal getBaseFare() {
            return baseFare;
        }
        
        public void setBaseFare(BigDecimal baseFare) {
            this.baseFare = baseFare;
        }
        
        public BigDecimal getApplicableDiscount() {
            return applicableDiscount;
        }
        
        public void setApplicableDiscount(BigDecimal applicableDiscount) {
            this.applicableDiscount = applicableDiscount;
        }
        
        public BigDecimal getFinalFare() {
            return finalFare;
        }
        
        public void setFinalFare(BigDecimal finalFare) {
            this.finalFare = finalFare;
        }
        
        public String getDiscountReason() {
            return discountReason;
        }
        
        public void setDiscountReason(String discountReason) {
            this.discountReason = discountReason;
        }
    }
}
