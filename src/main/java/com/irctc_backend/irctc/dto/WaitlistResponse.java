package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.WaitlistEntry;
import com.irctc_backend.irctc.entity.RacEntry;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for waitlist response
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class WaitlistResponse {
    
    private Long id;
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Long coachId;
    private String coachNumber;
    private String coachType;
    private LocalDateTime journeyDate;
    private Integer waitlistNumber;
    private WaitlistEntry.WaitlistStatus status;
    private WaitlistEntry.QuotaType quotaType;
    private Integer passengerCount;
    private String preferredBerthType;
    private String preferredSeatType;
    private Boolean isLadiesQuota;
    private Boolean isSeniorCitizenQuota;
    private Boolean isHandicappedFriendly;
    private Integer priorityScore;
    private Boolean autoUpgradeEnabled;
    private Boolean notificationSent;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional information
    private Integer currentPosition;
    private Integer totalWaitlistEntries;
    private Integer estimatedConfirmationChance;
    private String message;
    private List<RacEntry> racEntries;
    
    // Constructors
    public WaitlistResponse() {}
    
    public WaitlistResponse(Long id, Long trainId, String trainNumber, String trainName,
                           Long coachId, String coachNumber, String coachType,
                           LocalDateTime journeyDate, Integer waitlistNumber,
                           WaitlistEntry.WaitlistStatus status, WaitlistEntry.QuotaType quotaType) {
        this.id = id;
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.coachId = coachId;
        this.coachNumber = coachNumber;
        this.coachType = coachType;
        this.journeyDate = journeyDate;
        this.waitlistNumber = waitlistNumber;
        this.status = status;
        this.quotaType = quotaType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Long getCoachId() {
        return coachId;
    }
    
    public void setCoachId(Long coachId) {
        this.coachId = coachId;
    }
    
    public String getCoachNumber() {
        return coachNumber;
    }
    
    public void setCoachNumber(String coachNumber) {
        this.coachNumber = coachNumber;
    }
    
    public String getCoachType() {
        return coachType;
    }
    
    public void setCoachType(String coachType) {
        this.coachType = coachType;
    }
    
    public LocalDateTime getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(LocalDateTime journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public Integer getWaitlistNumber() {
        return waitlistNumber;
    }
    
    public void setWaitlistNumber(Integer waitlistNumber) {
        this.waitlistNumber = waitlistNumber;
    }
    
    public WaitlistEntry.WaitlistStatus getStatus() {
        return status;
    }
    
    public void setStatus(WaitlistEntry.WaitlistStatus status) {
        this.status = status;
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
    
    public Integer getPriorityScore() {
        return priorityScore;
    }
    
    public void setPriorityScore(Integer priorityScore) {
        this.priorityScore = priorityScore;
    }
    
    public Boolean getAutoUpgradeEnabled() {
        return autoUpgradeEnabled;
    }
    
    public void setAutoUpgradeEnabled(Boolean autoUpgradeEnabled) {
        this.autoUpgradeEnabled = autoUpgradeEnabled;
    }
    
    public Boolean getNotificationSent() {
        return notificationSent;
    }
    
    public void setNotificationSent(Boolean notificationSent) {
        this.notificationSent = notificationSent;
    }
    
    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }
    
    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
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
    
    public Integer getCurrentPosition() {
        return currentPosition;
    }
    
    public void setCurrentPosition(Integer currentPosition) {
        this.currentPosition = currentPosition;
    }
    
    public Integer getTotalWaitlistEntries() {
        return totalWaitlistEntries;
    }
    
    public void setTotalWaitlistEntries(Integer totalWaitlistEntries) {
        this.totalWaitlistEntries = totalWaitlistEntries;
    }
    
    public Integer getEstimatedConfirmationChance() {
        return estimatedConfirmationChance;
    }
    
    public void setEstimatedConfirmationChance(Integer estimatedConfirmationChance) {
        this.estimatedConfirmationChance = estimatedConfirmationChance;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<RacEntry> getRacEntries() {
        return racEntries;
    }
    
    public void setRacEntries(List<RacEntry> racEntries) {
        this.racEntries = racEntries;
    }
}
