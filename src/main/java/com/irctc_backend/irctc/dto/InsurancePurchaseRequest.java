package com.irctc_backend.irctc.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

/**
 * DTO for insurance purchase request
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class InsurancePurchaseRequest {
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    @NotNull(message = "Coverage amount is required")
    private java.math.BigDecimal coverageAmount;
    
    @NotNull(message = "Journey start date is required")
    private LocalDate journeyStartDate;
    
    @NotNull(message = "Journey end date is required")
    private LocalDate journeyEndDate;
    
    // Traveler details
    @NotBlank(message = "Traveler name is required")
    private String travelerName;
    
    @NotNull(message = "Traveler age is required")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 100, message = "Age must be at most 100")
    private Integer travelerAge;
    
    private String travelerGender;
    
    @NotBlank(message = "Traveler phone is required")
    private String travelerPhone;
    
    @Email(message = "Invalid email format")
    private String travelerEmail;
    
    private String travelerIdProofType;
    
    private String travelerIdProofNumber;
    
    // Travel details
    private String sourceStation;
    
    private String destinationStation;
    
    private String trainNumber;
    
    private String pnrNumber;
    
    // Emergency contact
    private String emergencyContactName;
    
    private String emergencyContactPhone;
    
    private String emergencyContactRelation;
    
    // Additional information
    private String medicalConditions;
    
    private String specialRequirements;
    
    @NotNull(message = "Terms acceptance is required")
    private Boolean termsAccepted;
    
    // Constructors
    public InsurancePurchaseRequest() {}
    
    // Getters and Setters
    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public java.math.BigDecimal getCoverageAmount() {
        return coverageAmount;
    }
    
    public void setCoverageAmount(java.math.BigDecimal coverageAmount) {
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
    
    public String getTravelerName() {
        return travelerName;
    }
    
    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
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
    
    public String getTravelerPhone() {
        return travelerPhone;
    }
    
    public void setTravelerPhone(String travelerPhone) {
        this.travelerPhone = travelerPhone;
    }
    
    public String getTravelerEmail() {
        return travelerEmail;
    }
    
    public void setTravelerEmail(String travelerEmail) {
        this.travelerEmail = travelerEmail;
    }
    
    public String getTravelerIdProofType() {
        return travelerIdProofType;
    }
    
    public void setTravelerIdProofType(String travelerIdProofType) {
        this.travelerIdProofType = travelerIdProofType;
    }
    
    public String getTravelerIdProofNumber() {
        return travelerIdProofNumber;
    }
    
    public void setTravelerIdProofNumber(String travelerIdProofNumber) {
        this.travelerIdProofNumber = travelerIdProofNumber;
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
    
    public String getEmergencyContactName() {
        return emergencyContactName;
    }
    
    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }
    
    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }
    
    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }
    
    public String getEmergencyContactRelation() {
        return emergencyContactRelation;
    }
    
    public void setEmergencyContactRelation(String emergencyContactRelation) {
        this.emergencyContactRelation = emergencyContactRelation;
    }
    
    public String getMedicalConditions() {
        return medicalConditions;
    }
    
    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }
    
    public String getSpecialRequirements() {
        return specialRequirements;
    }
    
    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }
    
    public Boolean getTermsAccepted() {
        return termsAccepted;
    }
    
    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }
}
