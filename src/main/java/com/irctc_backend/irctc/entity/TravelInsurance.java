package com.irctc_backend.irctc.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing travel insurance policies
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "travel_insurance")
public class TravelInsurance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "policy_number", unique = true, nullable = false)
    private String policyNumber;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking; // Optional - insurance can be standalone
    
    @NotNull(message = "Insurance plan is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan plan;
    
    @NotNull(message = "Coverage amount is required")
    @Column(name = "coverage_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal coverageAmount;
    
    @NotNull(message = "Premium amount is required")
    @Column(name = "premium_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal premiumAmount;
    
    @Column(name = "gst_amount", precision = 10, scale = 2)
    private BigDecimal gstAmount;
    
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "journey_start_date", nullable = false)
    private LocalDate journeyStartDate;
    
    @Column(name = "journey_end_date", nullable = false)
    private LocalDate journeyEndDate;
    
    @Column(name = "coverage_start_date", nullable = false)
    private LocalDate coverageStartDate;
    
    @Column(name = "coverage_end_date", nullable = false)
    private LocalDate coverageEndDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false)
    private PolicyStatus policyStatus = PolicyStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;
    
    @Column(name = "activation_date")
    private LocalDateTime activationDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Traveler details
    @Column(name = "traveler_name", nullable = false, length = 200)
    private String travelerName;
    
    @Column(name = "traveler_age")
    private Integer travelerAge;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "traveler_gender")
    private User.Gender travelerGender;
    
    @Column(name = "traveler_phone", length = 20)
    private String travelerPhone;
    
    @Column(name = "traveler_email")
    private String travelerEmail;
    
    @Column(name = "traveler_id_proof_type")
    private String travelerIdProofType;
    
    @Column(name = "traveler_id_proof_number", length = 50)
    private String travelerIdProofNumber;
    
    // Travel details
    @Column(name = "source_station", length = 100)
    private String sourceStation;
    
    @Column(name = "destination_station", length = 100)
    private String destinationStation;
    
    @Column(name = "train_number", length = 20)
    private String trainNumber;
    
    @Column(name = "pnr_number", length = 20)
    private String pnrNumber;
    
    // Additional information
    @Column(name = "emergency_contact_name", length = 200)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;
    
    @Column(name = "medical_conditions", length = 500)
    private String medicalConditions;
    
    @Column(name = "special_requirements", length = 500)
    private String specialRequirements;
    
    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;
    
    @Column(name = "policy_document_url")
    private String policyDocumentUrl;
    
    @Column(name = "claim_contact_number", length = 20)
    private String claimContactNumber;
    
    @Column(name = "claim_email")
    private String claimEmail;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TravelInsurance() {}
    
    public TravelInsurance(User user, InsurancePlan plan, BigDecimal coverageAmount, 
                          BigDecimal premiumAmount, String travelerName) {
        this.user = user;
        this.plan = plan;
        this.coverageAmount = coverageAmount;
        this.premiumAmount = premiumAmount;
        this.travelerName = travelerName;
        this.purchaseDate = LocalDateTime.now();
        this.policyNumber = generatePolicyNumber();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPolicyNumber() {
        return policyNumber;
    }
    
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public InsurancePlan getPlan() {
        return plan;
    }
    
    public void setPlan(InsurancePlan plan) {
        this.plan = plan;
    }
    
    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }
    
    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }
    
    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }
    
    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }
    
    public BigDecimal getGstAmount() {
        return gstAmount;
    }
    
    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
    
    public PolicyStatus getPolicyStatus() {
        return policyStatus;
    }
    
    public void setPolicyStatus(PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDateTime getActivationDate() {
        return activationDate;
    }
    
    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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
    
    public User.Gender getTravelerGender() {
        return travelerGender;
    }
    
    public void setTravelerGender(User.Gender travelerGender) {
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
    
    public String getPolicyDocumentUrl() {
        return policyDocumentUrl;
    }
    
    public void setPolicyDocumentUrl(String policyDocumentUrl) {
        this.policyDocumentUrl = policyDocumentUrl;
    }
    
    public String getClaimContactNumber() {
        return claimContactNumber;
    }
    
    public void setClaimContactNumber(String claimContactNumber) {
        this.claimContactNumber = claimContactNumber;
    }
    
    public String getClaimEmail() {
        return claimEmail;
    }
    
    public void setClaimEmail(String claimEmail) {
        this.claimEmail = claimEmail;
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
    
    /**
     * Generate unique policy number
     */
    private String generatePolicyNumber() {
        return "TI" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
    
    /**
     * Enum for policy status
     */
    public enum PolicyStatus {
        ACTIVE("Active", "Policy is active and provides coverage"),
        EXPIRED("Expired", "Policy has expired"),
        CANCELLED("Cancelled", "Policy has been cancelled"),
        SUSPENDED("Suspended", "Policy is temporarily suspended"),
        CLAIMED("Claimed", "Policy has been claimed");
        
        private final String displayName;
        private final String description;
        
        PolicyStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Enum for payment status
     */
    public enum PaymentStatus {
        PENDING("Pending", "Payment is pending"),
        PAID("Paid", "Payment has been completed"),
        FAILED("Failed", "Payment has failed"),
        REFUNDED("Refunded", "Payment has been refunded");
        
        private final String displayName;
        private final String description;
        
        PaymentStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
