package com.irctc_backend.irctc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TripPlanRequest {

    @NotBlank
    private String sourceStationCode;

    @NotBlank
    private String destinationStationCode;

    @NotNull
    private LocalDate journeyDate;

    private LocalTime earliestDeparture;

    private LocalTime latestArrival;

    @Positive
    private Integer maxConnections = 2; // number of transfers allowed

    @Positive
    private Integer maxTotalDurationMinutes = 24 * 60; // 24 hours default

    private Boolean preferFastest = true;

    private Boolean allowOvernight = true;

    private List<String> preferredTrainTypes; // e.g., RAJDHANI, EXPRESS

    // Getters and setters
    public String getSourceStationCode() { return sourceStationCode; }
    public void setSourceStationCode(String sourceStationCode) { this.sourceStationCode = sourceStationCode; }
    public String getDestinationStationCode() { return destinationStationCode; }
    public void setDestinationStationCode(String destinationStationCode) { this.destinationStationCode = destinationStationCode; }
    public LocalDate getJourneyDate() { return journeyDate; }
    public void setJourneyDate(LocalDate journeyDate) { this.journeyDate = journeyDate; }
    public LocalTime getEarliestDeparture() { return earliestDeparture; }
    public void setEarliestDeparture(LocalTime earliestDeparture) { this.earliestDeparture = earliestDeparture; }
    public LocalTime getLatestArrival() { return latestArrival; }
    public void setLatestArrival(LocalTime latestArrival) { this.latestArrival = latestArrival; }
    public Integer getMaxConnections() { return maxConnections; }
    public void setMaxConnections(Integer maxConnections) { this.maxConnections = maxConnections; }
    public Integer getMaxTotalDurationMinutes() { return maxTotalDurationMinutes; }
    public void setMaxTotalDurationMinutes(Integer maxTotalDurationMinutes) { this.maxTotalDurationMinutes = maxTotalDurationMinutes; }
    public Boolean getPreferFastest() { return preferFastest; }
    public void setPreferFastest(Boolean preferFastest) { this.preferFastest = preferFastest; }
    public Boolean getAllowOvernight() { return allowOvernight; }
    public void setAllowOvernight(Boolean allowOvernight) { this.allowOvernight = allowOvernight; }
    public List<String> getPreferredTrainTypes() { return preferredTrainTypes; }
    public void setPreferredTrainTypes(List<String> preferredTrainTypes) { this.preferredTrainTypes = preferredTrainTypes; }
}


