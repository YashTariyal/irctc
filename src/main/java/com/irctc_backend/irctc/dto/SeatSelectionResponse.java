package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Seat;
import com.irctc_backend.irctc.entity.Coach;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for seat selection response
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class SeatSelectionResponse {
    
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private Long coachId;
    private String coachNumber;
    private Coach.CoachType coachType;
    private String journeyDate;
    private List<SeatInfo> availableSeats;
    private List<SeatInfo> selectedSeats;
    private BigDecimal totalFare;
    private Integer totalSeats;
    private Integer availableSeatsCount;
    private String selectionStatus;
    private String message;
    
    // Constructors
    public SeatSelectionResponse() {}
    
    public SeatSelectionResponse(Long trainId, String trainNumber, String trainName, 
                                Long coachId, String coachNumber, Coach.CoachType coachType,
                                String journeyDate, List<SeatInfo> availableSeats) {
        this.trainId = trainId;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.coachId = coachId;
        this.coachNumber = coachNumber;
        this.coachType = coachType;
        this.journeyDate = journeyDate;
        this.availableSeats = availableSeats;
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
    
    public Coach.CoachType getCoachType() {
        return coachType;
    }
    
    public void setCoachType(Coach.CoachType coachType) {
        this.coachType = coachType;
    }
    
    public String getJourneyDate() {
        return journeyDate;
    }
    
    public void setJourneyDate(String journeyDate) {
        this.journeyDate = journeyDate;
    }
    
    public List<SeatInfo> getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(List<SeatInfo> availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public List<SeatInfo> getSelectedSeats() {
        return selectedSeats;
    }
    
    public void setSelectedSeats(List<SeatInfo> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }
    
    public BigDecimal getTotalFare() {
        return totalFare;
    }
    
    public void setTotalFare(BigDecimal totalFare) {
        this.totalFare = totalFare;
    }
    
    public Integer getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public Integer getAvailableSeatsCount() {
        return availableSeatsCount;
    }
    
    public void setAvailableSeatsCount(Integer availableSeatsCount) {
        this.availableSeatsCount = availableSeatsCount;
    }
    
    public String getSelectionStatus() {
        return selectionStatus;
    }
    
    public void setSelectionStatus(String selectionStatus) {
        this.selectionStatus = selectionStatus;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Inner class for seat information
     */
    public static class SeatInfo {
        private Long seatId;
        private String seatNumber;
        private String berthNumber;
        private Seat.SeatType seatType;
        private Seat.BerthType berthType;
        private Seat.SeatStatus status;
        private BigDecimal fare;
        private Boolean isLadiesQuota;
        private Boolean isSeniorCitizenQuota;
        private Boolean isHandicappedFriendly;
        private String position; // "window", "aisle", "middle"
        private String color; // For UI color coding
        
        // Constructors
        public SeatInfo() {}
        
        public SeatInfo(Long seatId, String seatNumber, Seat.SeatType seatType, 
                       Seat.BerthType berthType, Seat.SeatStatus status, BigDecimal fare) {
            this.seatId = seatId;
            this.seatNumber = seatNumber;
            this.seatType = seatType;
            this.berthType = berthType;
            this.status = status;
            this.fare = fare;
        }
        
        // Getters and Setters
        public Long getSeatId() {
            return seatId;
        }
        
        public void setSeatId(Long seatId) {
            this.seatId = seatId;
        }
        
        public String getSeatNumber() {
            return seatNumber;
        }
        
        public void setSeatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
        }
        
        public String getBerthNumber() {
            return berthNumber;
        }
        
        public void setBerthNumber(String berthNumber) {
            this.berthNumber = berthNumber;
        }
        
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
        
        public Seat.SeatStatus getStatus() {
            return status;
        }
        
        public void setStatus(Seat.SeatStatus status) {
            this.status = status;
        }
        
        public BigDecimal getFare() {
            return fare;
        }
        
        public void setFare(BigDecimal fare) {
            this.fare = fare;
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
        
        public String getPosition() {
            return position;
        }
        
        public void setPosition(String position) {
            this.position = position;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
    }
}
