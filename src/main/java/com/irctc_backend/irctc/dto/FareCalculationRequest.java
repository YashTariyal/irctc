package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Coach;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for fare calculation request
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class FareCalculationRequest {
    
    @NotNull(message = "Train ID is required")
    private Long trainId;
    
    @NotNull(message = "Coach type is required")
    private Coach.CoachType coachType;
    
    @NotNull(message = "Journey date is required")
    private LocalDateTime journeyDate;
    
    @NotNull(message = "Number of passengers is required")
    private Integer numberOfPassengers;
    
    @NotNull(message = "Distance is required")
    private Integer distanceKm;
    
    private String quotaType = "GENERAL";
    private Boolean isTatkal = false;
    private Boolean isPremiumTatkal = false;
    private Boolean isLadiesQuota = false;
    private Boolean isSeniorCitizenQuota = false;
    private Boolean isHandicappedFriendly = false;
    private List<PassengerInfo> passengers;
    
    // Constructors
    public FareCalculationRequest() {}
    
    public FareCalculationRequest(Long trainId, Coach.CoachType coachType, LocalDateTime journeyDate, 
                                 Integer numberOfPassengers, Integer distanceKm) {
        this.trainId = trainId;
        this.coachType = coachType;
        this.journeyDate = journeyDate;
        this.numberOfPassengers = numberOfPassengers;
        this.distanceKm = distanceKm;
    }
    
    // Getters and Setters
    public Long getTrainId() {
        return trainId;
    }
    
    public void setTrainId(Long trainId) {
        this.trainId = trainId;
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
    
    public Boolean getIsTatkal() {
        return isTatkal;
    }
    
    public void setIsTatkal(Boolean isTatkal) {
        this.isTatkal = isTatkal;
    }
    
    public Boolean getIsPremiumTatkal() {
        return isPremiumTatkal;
    }
    
    public void setIsPremiumTatkal(Boolean isPremiumTatkal) {
        this.isPremiumTatkal = isPremiumTatkal;
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
    
    public List<PassengerInfo> getPassengers() {
        return passengers;
    }
    
    public void setPassengers(List<PassengerInfo> passengers) {
        this.passengers = passengers;
    }
    
    /**
     * Inner class for passenger information
     */
    public static class PassengerInfo {
        private String name;
        private Integer age;
        private String gender;
        private Boolean isSeniorCitizen = false;
        private Boolean isHandicapped = false;
        private Boolean isLadiesQuota = false;
        
        // Constructors
        public PassengerInfo() {}
        
        public PassengerInfo(String name, Integer age, String gender) {
            this.name = name;
            this.age = age;
            this.gender = gender;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Integer getAge() {
            return age;
        }
        
        public void setAge(Integer age) {
            this.age = age;
            this.isSeniorCitizen = age != null && age >= 60;
        }
        
        public String getGender() {
            return gender;
        }
        
        public void setGender(String gender) {
            this.gender = gender;
        }
        
        public Boolean getIsSeniorCitizen() {
            return isSeniorCitizen;
        }
        
        public void setIsSeniorCitizen(Boolean isSeniorCitizen) {
            this.isSeniorCitizen = isSeniorCitizen;
        }
        
        public Boolean getIsHandicapped() {
            return isHandicapped;
        }
        
        public void setIsHandicapped(Boolean isHandicapped) {
            this.isHandicapped = isHandicapped;
        }
        
        public Boolean getIsLadiesQuota() {
            return isLadiesQuota;
        }
        
        public void setIsLadiesQuota(Boolean isLadiesQuota) {
            this.isLadiesQuota = isLadiesQuota;
        }
    }
}
