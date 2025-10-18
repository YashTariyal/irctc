package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.Train;

import java.time.LocalTime;

/**
 * Mobile-optimized train response DTO
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class MobileTrainResponse {
    
    private Long id;
    private String trainNumber;
    private String trainName;
    private String sourceStationCode;
    private String sourceStationName;
    private String destinationStationCode;
    private String destinationStationName;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer journeyDuration;
    private Double totalDistance;
    private String trainType;
    private String status;
    private Boolean isRunning;
    private Integer availableSeats;
    private Double startingFare;
    private String route;
    private String amenities;
    private Boolean isTatkalAvailable;
    private Boolean isPremiumTatkalAvailable;
    
    // Constructors
    public MobileTrainResponse() {}
    
    public MobileTrainResponse(Train train) {
        this.id = train.getId();
        this.trainNumber = train.getTrainNumber();
        this.trainName = train.getTrainName();
        this.sourceStationCode = train.getSourceStation() != null ? train.getSourceStation().getStationCode() : null;
        this.sourceStationName = train.getSourceStation() != null ? train.getSourceStation().getStationName() : null;
        this.destinationStationCode = train.getDestinationStation() != null ? train.getDestinationStation().getStationCode() : null;
        this.destinationStationName = train.getDestinationStation() != null ? train.getDestinationStation().getStationName() : null;
        this.departureTime = train.getDepartureTime();
        this.arrivalTime = train.getArrivalTime();
        this.journeyDuration = train.getJourneyDuration();
        this.totalDistance = train.getTotalDistance();
        this.trainType = train.getTrainType() != null ? train.getTrainType().name() : null;
        this.status = train.getStatus() != null ? train.getStatus().name() : null;
        this.isRunning = train.getIsRunning();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getSourceStationCode() {
        return sourceStationCode;
    }
    
    public void setSourceStationCode(String sourceStationCode) {
        this.sourceStationCode = sourceStationCode;
    }
    
    public String getSourceStationName() {
        return sourceStationName;
    }
    
    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }
    
    public String getDestinationStationCode() {
        return destinationStationCode;
    }
    
    public void setDestinationStationCode(String destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }
    
    public String getDestinationStationName() {
        return destinationStationName;
    }
    
    public void setDestinationStationName(String destinationStationName) {
        this.destinationStationName = destinationStationName;
    }
    
    public LocalTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public Integer getJourneyDuration() {
        return journeyDuration;
    }
    
    public void setJourneyDuration(Integer journeyDuration) {
        this.journeyDuration = journeyDuration;
    }
    
    public Double getTotalDistance() {
        return totalDistance;
    }
    
    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }
    
    public String getTrainType() {
        return trainType;
    }
    
    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getIsRunning() {
        return isRunning;
    }
    
    public void setIsRunning(Boolean isRunning) {
        this.isRunning = isRunning;
    }
    
    public Integer getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public Double getStartingFare() {
        return startingFare;
    }
    
    public void setStartingFare(Double startingFare) {
        this.startingFare = startingFare;
    }
    
    public String getRoute() {
        return route;
    }
    
    public void setRoute(String route) {
        this.route = route;
    }
    
    public String getAmenities() {
        return amenities;
    }
    
    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
    
    public Boolean getIsTatkalAvailable() {
        return isTatkalAvailable;
    }
    
    public void setIsTatkalAvailable(Boolean isTatkalAvailable) {
        this.isTatkalAvailable = isTatkalAvailable;
    }
    
    public Boolean getIsPremiumTatkalAvailable() {
        return isPremiumTatkalAvailable;
    }
    
    public void setIsPremiumTatkalAvailable(Boolean isPremiumTatkalAvailable) {
        this.isPremiumTatkalAvailable = isPremiumTatkalAvailable;
    }
}
