package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Seat;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for seat selection request
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class SeatSelectionRequest {
    
    @NotNull(message = "Train ID is required")
    private Long trainId;
    
    @NotNull(message = "Coach ID is required")
    private Long coachId;
    
    @NotNull(message = "Journey date is required")
    private String journeyDate;
    
    @NotNull(message = "Seat preferences are required")
    private List<SeatPreference> seatPreferences;
    
    private String passengerName;
    private Integer passengerAge;
    private String passengerGender;
    
    // Constructors
    public SeatSelectionRequest() {}
    
    public SeatSelectionRequest(Long trainId, Long coachId, String journeyDate, 
                               List<SeatPreference> seatPreferences) {
        this.trainId = trainId;
        this.coachId = coachId;
        this.journeyDate = journeyDate;
        this.seatPreferences = seatPreferences;
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
    
    public String getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public List<SeatPreference> getSeatPreferences() {
        return seatPreferences;
    }
    
    public void setSeatPreferences(List<SeatPreference> seatPreferences) {
        this.seatPreferences = seatPreferences;
    }
    
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
    
    /**
     * Inner class for seat preferences
     */
    public static class SeatPreference {
        
        @NotNull(message = "Seat type is required")
        private Seat.SeatType seatType;
        
        @NotNull(message = "Berth type is required")
        private Seat.BerthType berthType;
        
        private Boolean isLadiesQuota = false;
        private Boolean isSeniorCitizenQuota = false;
        private Boolean isHandicappedFriendly = false;
        
        // Constructors
        public SeatPreference() {}
        
        public SeatPreference(Seat.SeatType seatType, Seat.BerthType berthType) {
            this.seatType = seatType;
            this.berthType = berthType;
        }
        
        // Getters and Setters
        public Seat.SeatType getSeatType() {
            return seatType;
        }
        
        public void setSeatType(Seat.SeatType seatType) {
            this.seatType = seatType;
        }
        
        public Seat.BerthType getBerthType() {
            return berthType;
        }
        
        public void setBerthType(Seat.BerthType berthType) {
            this.berthType = berthType;
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
    }
}
