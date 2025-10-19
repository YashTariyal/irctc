package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.InsurancePlan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for insurance quote request
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class InsuranceQuoteRequest {
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "1000.0", message = "Coverage amount must be at least ₹1,000")
    private BigDecimal coverageAmount;
    
    @NotNull(message = "Journey start date is required")
    private LocalDate journeyStartDate;
    
    @NotNull(message = "Journey end date is required")
    private LocalDate journeyEndDate;
    
    @NotNull(message = "Traveler age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer travelerAge;
    
    private String travelerGender;
    
    private Boolean hasMedicalConditions = false;
    
    private String sourceStation;
    
    private String destinationStation;
    
    private String trainNumber;
    
    private String pnrNumber;
    
    // Constructors
    public InsuranceQuoteRequest() {}
    
    public InsuranceQuoteRequest(Long planId, BigDecimal coverageAmount, LocalDate journeyStartDate, 
                               LocalDate journeyEndDate, Integer travelerAge) {
        this.planId = planId;
        this.coverageAmount = coverageAmount;
        this.journeyStartDate = journeyStartDate;
        this.journeyEndDate = journeyEndDate;
        this.travelerAge = travelerAge;
    }
    
    // Getters and Setters
    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }
    
    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }
    
    public LocalDate getJourneyStartDate() {
        return journeyStartDate;
    }
    
    public void setJourneyStartDate(LocalDate journeyStartDate) {
        this.journeyStartDate = journeyStartDate;
    }
    
    public LocalDate getJourneyEndDate() {
        return journeyEndDate;
    }
    
    public void setJourneyEndDate(LocalDate journeyEndDate) {
        this.journeyEndDate = journeyEndDate;
    }
    
    public Integer getTravelerAge() {
        return travelerAge;
    }
    
    public void setTravelerAge(Integer travelerAge) {
        this.travelerAge = travelerAge;
    }
    
    public String getTravelerGender() {
        return travelerGender;
    }
    
    public void setTravelerGender(String travelerGender) {
        this.travelerGender = travelerGender;
    }
    
    public Boolean getHasMedicalConditions() {
        return hasMedicalConditions;
    }
    
    public void setHasMedicalConditions(Boolean hasMedicalConditions) {
        this.hasMedicalConditions = hasMedicalConditions;
    }
    
    public String getSourceStation() {
        return sourceStation;
    }
    
    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }
    
    public String getDestinationStation() {
        return destinationStation;
    }
    
    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }
    
    public String getTrainNumber() {
        return trainNumber;
    }
    
    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }
    
    public String getPnrNumber() {
        return pnrNumber;
    }
    
    public void setPnrNumber(String pnrNumber) {
        this.pnrNumber = pnrNumber;
    }
}
