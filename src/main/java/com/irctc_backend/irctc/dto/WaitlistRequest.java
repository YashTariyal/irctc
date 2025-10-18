package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.WaitlistEntry;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * DTO for waitlist request
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class WaitlistRequest {
    
    @NotNull(message = "Train ID is required")
    private Long trainId;
    
    @NotNull(message = "Coach ID is required")
    private Long coachId;
    
    @NotNull(message = "Journey date is required")
    private LocalDateTime journeyDate;
    
    @NotNull(message = "Quota type is required")
    private WaitlistEntry.QuotaType quotaType;
    
    @NotNull(message = "Passenger count is required")
    @Positive(message = "Passenger count must be positive")
    private Integer passengerCount;
    
    private String preferredBerthType;
    private String preferredSeatType;
    private Boolean isLadiesQuota = false;
    private Boolean isSeniorCitizenQuota = false;
    private Boolean isHandicappedFriendly = false;
    private Boolean autoUpgradeEnabled = true;
    
    // Constructors
    public WaitlistRequest() {}
    
    public WaitlistRequest(Long trainId, Long coachId, LocalDateTime journeyDate, 
                          WaitlistEntry.QuotaType quotaType, Integer passengerCount) {
        this.trainId = trainId;
        this.coachId = coachId;
        this.journeyDate = journeyDate;
        this.quotaType = quotaType;
        this.passengerCount = passengerCount;
    }
    
    // Getters and Setters
    public Long getTrainId() {
        return trainId;
    }
    
    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }
    
    public Long getCoachId() {
        return coachId;
    }
    
    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }
    
    public LocalDateTime getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(LocalDateTime journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public WaitlistEntry.QuotaType getQuotaType() {
        return quotaType;
    }
    
    public void setQuotaType(WaitlistEntry.QuotaType quotaType) {
        this.quotaType = quotaType;
    }
    
    public Integer getPassengerCount() {
        return passengerCount;
    }
    
    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }
    
    public String getPreferredBerthType() {
        return preferredBerthType;
    }
    
    public void setPreferredBerthType(String preferredBerthType) {
        this.preferredBerthType = preferredBerthType;
    }
    
    public String getPreferredSeatType() {
        return preferredSeatType;
    }
    
    public void setPreferredSeatType(String preferredSeatType) {
        this.preferredSeatType = preferredSeatType;
    }
    
    public Boolean getIsLadiesQuota() {
        return isLadiesQuota;
    }
    
    public void setIsLadiesQuota(Boolean isLadiesQuota) {
        this.isLadiesQuota = isLadiesQuota;
    }
    
    public Boolean getIsSeniorCitizenQuota() {
        return isSeniorCitizenQuota;
    }
    
    public void setIsSeniorCitizenQuota(Boolean isSeniorCitizenQuota) {
        this.isSeniorCitizenQuota = isSeniorCitizenQuota;
    }
    
    public Boolean getIsHandicappedFriendly() {
        return isHandicappedFriendly;
    }
    
    public void setIsHandicappedFriendly(Boolean isHandicappedFriendly) {
        this.isHandicappedFriendly = isHandicappedFriendly;
    }
    
    public Boolean getAutoUpgradeEnabled() {
        return autoUpgradeEnabled;
    }
    
    public void setAutoUpgradeEnabled(Boolean autoUpgradeEnabled) {
        this.autoUpgradeEnabled = autoUpgradeEnabled;
    }
}
