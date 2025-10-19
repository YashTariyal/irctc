package com.irctc_backend.irctc.dto;

import com.irctc_backend.irctc.entity.InsurancePlan;
import com.irctc_backend.irctc.entity.InsuranceProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for insurance quote response
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class InsuranceQuoteResponse {
    
    private Long planId;
    private String planName;
    private String planDescription;
    private String planType;
    private String planTypeDescription;
    
    private Long providerId;
    private String providerName;
    private String companyName;
    private String providerDescription;
    private BigDecimal providerRating;
    private String providerLogoUrl;
    
    private BigDecimal coverageAmount;
    private BigDecimal premiumRate;
    private BigDecimal basePremium;
    private BigDecimal ageMultiplier;
    private BigDecimal medicalMultiplier;
    private BigDecimal totalPremium;
    private BigDecimal gstAmount;
    private BigDecimal finalAmount;
    
    private LocalDate journeyStartDate;
    private LocalDate journeyEndDate;
    private LocalDate coverageStartDate;
    private LocalDate coverageEndDate;
    private Integer coverageDurationDays;
    
    private Integer travelerAge;
    private String travelerGender;
    private Boolean hasMedicalConditions;
    
    // Coverage details
    private Boolean coversMedicalExpenses;
    private Boolean coversTripCancellation;
    private Boolean coversBaggageLoss;
    private Boolean coversPersonalAccident;
    private Boolean coversEmergencyEvacuation;
    private Boolean covers24x7Support;
    
    // Coverage limits
    private BigDecimal medicalCoverageLimit;
    private BigDecimal tripCancellationLimit;
    private BigDecimal baggageCoverageLimit;
    private BigDecimal personalAccidentLimit;
    private BigDecimal deductibleAmount;
    
    // Additional information
    private String sourceStation;
    private String destinationStation;
    private String trainNumber;
    private String pnrNumber;
    
    private List<String> benefits;
    private List<String> exclusions;
    private String termsAndConditions;
    
    private Boolean isEligible;
    private String eligibilityMessage;
    
    // Constructors
    public InsuranceQuoteResponse() {}
    
    // Getters and Setters
    public Long getPlanId() {
        return planId;
    }
    
    public void setPlanId(Long planId) {
        this.planId = planId;
    }
    
    public String getPlanName() {
        return planName;
    }
    
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    
    public String getPlanDescription() {
        return planDescription;
    }
    
    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }
    
    public String getPlanType() {
        return planType;
    }
    
    public void setPlanType(String planType) {
        this.planType = planType;
    }
    
    public String getPlanTypeDescription() {
        return planTypeDescription;
    }
    
    public void setPlanTypeDescription(String planTypeDescription) {
        this.planTypeDescription = planTypeDescription;
    }
    
    public Long getProviderId() {
        return providerId;
    }
    
    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getProviderDescription() {
        return providerDescription;
    }
    
    public void setProviderDescription(String providerDescription) {
        this.providerDescription = providerDescription;
    }
    
    public BigDecimal getProviderRating() {
        return providerRating;
    }
    
    public void setProviderRating(BigDecimal providerRating) {
        this.providerRating = providerRating;
    }
    
    public String getProviderLogoUrl() {
        return providerLogoUrl;
    }
    
    public void setProviderLogoUrl(String providerLogoUrl) {
        this.providerLogoUrl = providerLogoUrl;
    }
    
    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }
    
    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }
    
    public BigDecimal getPremiumRate() {
        return premiumRate;
    }
    
    public void setPremiumRate(BigDecimal premiumRate) {
        this.premiumRate = premiumRate;
    }
    
    public BigDecimal getBasePremium() {
        return basePremium;
    }
    
    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
    }
    
    public BigDecimal getAgeMultiplier() {
        return ageMultiplier;
    }
    
    public void setAgeMultiplier(BigDecimal ageMultiplier) {
        this.ageMultiplier = ageMultiplier;
    }
    
    public BigDecimal getMedicalMultiplier() {
        return medicalMultiplier;
    }
    
    public void setMedicalMultiplier(BigDecimal medicalMultiplier) {
        this.medicalMultiplier = medicalMultiplier;
    }
    
    public BigDecimal getTotalPremium() {
        return totalPremium;
    }
    
    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }
    
    public BigDecimal getGstAmount() {
        return gstAmount;
    }
    
    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
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
    
    public LocalDate getCoverageStartDate() {
        return coverageStartDate;
    }
    
    public void setCoverageStartDate(LocalDate coverageStartDate) {
        this.coverageStartDate = coverageStartDate;
    }
    
    public LocalDate getCoverageEndDate() {
        return coverageEndDate;
    }
    
    public void setCoverageEndDate(LocalDate coverageEndDate) {
        this.coverageEndDate = coverageEndDate;
    }
    
    public Integer getCoverageDurationDays() {
        return coverageDurationDays;
    }
    
    public void setCoverageDurationDays(Integer coverageDurationDays) {
        this.coverageDurationDays = coverageDurationDays;
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
    
    public Boolean getCoversMedicalExpenses() {
        return coversMedicalExpenses;
    }
    
    public void setCoversMedicalExpenses(Boolean coversMedicalExpenses) {
        this.coversMedicalExpenses = coversMedicalExpenses;
    }
    
    public Boolean getCoversTripCancellation() {
        return coversTripCancellation;
    }
    
    public void setCoversTripCancellation(Boolean coversTripCancellation) {
        this.coversTripCancellation = coversTripCancellation;
    }
    
    public Boolean getCoversBaggageLoss() {
        return coversBaggageLoss;
    }
    
    public void setCoversBaggageLoss(Boolean coversBaggageLoss) {
        this.coversBaggageLoss = coversBaggageLoss;
    }
    
    public Boolean getCoversPersonalAccident() {
        return coversPersonalAccident;
    }
    
    public void setCoversPersonalAccident(Boolean coversPersonalAccident) {
        this.coversPersonalAccident = coversPersonalAccident;
    }
    
    public Boolean getCoversEmergencyEvacuation() {
        return coversEmergencyEvacuation;
    }
    
    public void setCoversEmergencyEvacuation(Boolean coversEmergencyEvacuation) {
        this.coversEmergencyEvacuation = coversEmergencyEvacuation;
    }
    
    public Boolean getCovers24x7Support() {
        return covers24x7Support;
    }
    
    public void setCovers24x7Support(Boolean covers24x7Support) {
        this.covers24x7Support = covers24x7Support;
    }
    
    public BigDecimal getMedicalCoverageLimit() {
        return medicalCoverageLimit;
    }
    
    public void setMedicalCoverageLimit(BigDecimal medicalCoverageLimit) {
        this.medicalCoverageLimit = medicalCoverageLimit;
    }
    
    public BigDecimal getTripCancellationLimit() {
        return tripCancellationLimit;
    }
    
    public void setTripCancellationLimit(BigDecimal tripCancellationLimit) {
        this.tripCancellationLimit = tripCancellationLimit;
    }
    
    public BigDecimal getBaggageCoverageLimit() {
        return baggageCoverageLimit;
    }
    
    public void setBaggageCoverageLimit(BigDecimal baggageCoverageLimit) {
        this.baggageCoverageLimit = baggageCoverageLimit;
    }
    
    public BigDecimal getPersonalAccidentLimit() {
        return personalAccidentLimit;
    }
    
    public void setPersonalAccidentLimit(BigDecimal personalAccidentLimit) {
        this.personalAccidentLimit = personalAccidentLimit;
    }
    
    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }
    
    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
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
    
    public List<String> getBenefits() {
        return benefits;
    }
    
    public void setBenefits(List<String> benefits) {
        this.benefits = benefits;
    }
    
    public List<String> getExclusions() {
        return exclusions;
    }
    
    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }
    
    public String getTermsAndConditions() {
        return termsAndConditions;
    }
    
    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }
    
    public Boolean getIsEligible() {
        return isEligible;
    }
    
    public void setIsEligible(Boolean isEligible) {
        this.isEligible = isEligible;
    }
    
    public String getEligibilityMessage() {
        return eligibilityMessage;
    }
    
    public void setEligibilityMessage(String eligibilityMessage) {
        this.eligibilityMessage = eligibilityMessage;
    }
}
