package com.irctc_backend.irctc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TripPlanResponse {

    private String sourceStationCode;
    private String destinationStationCode;
    private LocalDate journeyDate;
    private Integer totalDurationMinutes;
    private Integer totalConnections;
    private BigDecimal estimatedFare;
    private Boolean overnight;
    private List<TripLeg> legs = new ArrayList<>();

    public static class TripLeg {
        private Long trainId;
        private String trainNumber;
        private String trainName;
        private String fromStationCode;
        private String fromStationName;
        private String toStationCode;
        private String toStationName;
        private LocalTime departureTime;
        private LocalTime arrivalTime;
        private Integer durationMinutes;
        private String coachType;
        private Boolean tatkalAvailable;

        public Long getTrainId() { return trainId; }
        public void setTrainId(Long trainId) { this.trainId = trainId; }
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        public String getFromStationCode() { return fromStationCode; }
        public void setFromStationCode(String fromStationCode) { this.fromStationCode = fromStationCode; }
        public String getFromStationName() { return fromStationName; }
        public void setFromStationName(String fromStationName) { this.fromStationName = fromStationName; }
        public String getToStationCode() { return toStationCode; }
        public void setToStationCode(String toStationCode) { this.toStationCode = toStationCode; }
        public String getToStationName() { return toStationName; }
        public void setToStationName(String toStationName) { this.toStationName = toStationName; }
        public LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
        public String getCoachType() { return coachType; }
        public void setCoachType(String coachType) { this.coachType = coachType; }
        public Boolean getTatkalAvailable() { return tatkalAvailable; }
        public void setTatkalAvailable(Boolean tatkalAvailable) { this.tatkalAvailable = tatkalAvailable; }
    }

    public String getSourceStationCode() { return sourceStationCode; }
    public void setSourceStationCode(String sourceStationCode) { this.sourceStationCode = sourceStationCode; }
    public String getDestinationStationCode() { return destinationStationCode; }
    public void setDestinationStationCode(String destinationStationCode) { this.destinationStationCode = destinationStationCode; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }
    public Integer getTotalConnections() { return totalConnections; }
    public void setTotalConnections(Integer totalConnections) { this.totalConnections = totalConnections; }
    public BigDecimal getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(BigDecimal estimatedFare) { this.estimatedFare = estimatedFare; }
    public Boolean getOvernight() { return overnight; }
    public void setOvernight(Boolean overnight) { this.overnight = overnight; }
    public List<TripLeg> getLegs() { return legs; }
    public void setLegs(List<TripLeg> legs) { this.legs = legs; }
}


