package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.InsuranceQuoteRequest;
import com.irctc_backend.irctc.dto.InsuranceQuoteResponse;
import com.irctc_backend.irctc.dto.InsurancePurchaseRequest;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for travel insurance management
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@Transactional
public class TravelInsuranceService {
    
    private static final Logger logger = LoggerFactory.getLogger(TravelInsuranceService.class);
    private static final BigDecimal GST_RATE = new BigDecimal("0.18"); // 18% GST
    
    @Autowired
    private InsuranceProviderRepository insuranceProviderRepository;
    
    @Autowired
    private InsurancePlanRepository insurancePlanRepository;
    
    @Autowired
    private TravelInsuranceRepository travelInsuranceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Get all active insurance providers
     */
    public List<InsuranceProvider> getActiveProviders() {
        logger.info("Getting all active insurance providers");
        return insuranceProviderRepository.findByIsActiveTrue();
    }
    
    /**
     * Get featured insurance providers
     */
    public List<InsuranceProvider> getFeaturedProviders() {
        logger.info("Getting featured insurance providers");
        return insuranceProviderRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }
    
    /**
     * Get all active insurance plans
     */
    public List<InsurancePlan> getActivePlans() {
        logger.info("Getting all active insurance plans");
        return insurancePlanRepository.findByIsActiveTrue();
    }
    
    /**
     * Get plans by provider
     */
    public List<InsurancePlan> getPlansByProvider(Long providerId) {
        logger.info("Getting plans for provider: {}", providerId);
        Optional<InsuranceProvider> provider = insuranceProviderRepository.findById(providerId);
        if (provider.isPresent()) {
            return insurancePlanRepository.findByProviderAndIsActiveTrue(provider.get());
        }
        return new ArrayList<>();
    }
    
    /**
     * Get plans by plan type
     */
    public List<InsurancePlan> getPlansByType(InsurancePlan.PlanType planType) {
        logger.info("Getting plans by type: {}", planType);
        return insurancePlanRepository.findByPlanTypeAndIsActiveTrue(planType);
    }
    
    /**
     * Get featured plans
     */
    public List<InsurancePlan> getFeaturedPlans() {
        logger.info("Getting featured insurance plans");
        return insurancePlanRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }
    
    /**
     * Calculate insurance quote
     */
    public InsuranceQuoteResponse calculateQuote(InsuranceQuoteRequest request) {
        logger.info("Calculating insurance quote for plan: {}, coverage: {}", request.getPlanId(), request.getCoverageAmount());
        
        Optional<InsurancePlan> planOpt = insurancePlanRepository.findById(request.getPlanId());
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Insurance plan not found");
        }
        
        InsurancePlan plan = planOpt.get();
        InsuranceProvider provider = plan.getProvider();
        
        // Validate eligibility
        if (!isEligibleForPlan(plan, request)) {
            InsuranceQuoteResponse response = new InsuranceQuoteResponse();
            response.setIsEligible(false);
            response.setEligibilityMessage("Not eligible for this plan based on age or coverage amount");
            return response;
        }
        
        // Calculate premium
        BigDecimal basePremium = calculateBasePremium(plan, request);
        BigDecimal ageMultiplier = calculateAgeMultiplier(request.getTravelerAge());
        BigDecimal medicalMultiplier = calculateMedicalMultiplier(request.getHasMedicalConditions());
        
        BigDecimal totalPremium = basePremium.multiply(ageMultiplier).multiply(medicalMultiplier);
        BigDecimal gstAmount = totalPremium.multiply(GST_RATE);
        BigDecimal finalAmount = totalPremium.add(gstAmount);
        
        // Build response
        InsuranceQuoteResponse response = new InsuranceQuoteResponse();
        response.setPlanId(plan.getId());
        response.setPlanName(plan.getPlanName());
        response.setPlanDescription(plan.getDescription());
        response.setPlanType(plan.getPlanType().name());
        response.setPlanTypeDescription(plan.getPlanType().getDescription());
        
        response.setProviderId(provider.getId());
        response.setProviderName(provider.getProviderName());
        response.setCompanyName(provider.getCompanyName());
        response.setProviderDescription(provider.getDescription());
        response.setProviderRating(provider.getRating());
        response.setProviderLogoUrl(provider.getLogoUrl());
        
        response.setCoverageAmount(request.getCoverageAmount());
        response.setPremiumRate(plan.getPremiumRate());
        response.setBasePremium(basePremium);
        response.setAgeMultiplier(ageMultiplier);
        response.setMedicalMultiplier(medicalMultiplier);
        response.setTotalPremium(totalPremium);
        response.setGstAmount(gstAmount);
        response.setFinalAmount(finalAmount);
        
        response.setJourneyStartDate(request.getJourneyStartDate());
        response.setJourneyEndDate(request.getJourneyEndDate());
        response.setCoverageStartDate(request.getJourneyStartDate().minusDays(1));
        response.setCoverageEndDate(request.getJourneyEndDate().plusDays(1));
        response.setCoverageDurationDays((int) ChronoUnit.DAYS.between(request.getJourneyStartDate(), request.getJourneyEndDate()) + 2);
        
        response.setTravelerAge(request.getTravelerAge());
        response.setTravelerGender(request.getTravelerGender());
        response.setHasMedicalConditions(request.getHasMedicalConditions());
        
        // Coverage details
        response.setCoversMedicalExpenses(plan.getCoversMedicalExpenses());
        response.setCoversTripCancellation(plan.getCoversTripCancellation());
        response.setCoversBaggageLoss(plan.getCoversBaggageLoss());
        response.setCoversPersonalAccident(plan.getCoversPersonalAccident());
        response.setCoversEmergencyEvacuation(plan.getCoversEmergencyEvacuation());
        response.setCovers24x7Support(plan.getCovers24x7Support());
        
        // Coverage limits
        response.setMedicalCoverageLimit(plan.getMedicalCoverageLimit());
        response.setTripCancellationLimit(plan.getTripCancellationLimit());
        response.setBaggageCoverageLimit(plan.getBaggageCoverageLimit());
        response.setPersonalAccidentLimit(plan.getPersonalAccidentLimit());
        response.setDeductibleAmount(plan.getDeductibleAmount());
        
        // Travel details
        response.setSourceStation(request.getSourceStation());
        response.setDestinationStation(request.getDestinationStation());
        response.setTrainNumber(request.getTrainNumber());
        response.setPnrNumber(request.getPnrNumber());
        
        // Benefits and exclusions
        response.setBenefits(generateBenefits(plan));
        response.setExclusions(generateExclusions(plan));
        response.setTermsAndConditions(generateTermsAndConditions(plan, provider));
        
        response.setIsEligible(true);
        response.setEligibilityMessage("Eligible for this plan");
        
        logger.info("Calculated quote: Premium ₹{}, Final Amount ₹{}", totalPremium, finalAmount);
        return response;
    }
    
    /**
     * Purchase insurance
     */
    public TravelInsurance purchaseInsurance(User user, InsurancePurchaseRequest request) {
        logger.info("Processing insurance purchase for user: {}, plan: {}", user.getUsername(), request.getPlanId());
        
        // Validate terms acceptance
        if (!request.getTermsAccepted()) {
            throw new RuntimeException("Terms and conditions must be accepted");
        }
        
        // Get plan
        Optional<InsurancePlan> planOpt = insurancePlanRepository.findById(request.getPlanId());
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Insurance plan not found");
        }
        
        InsurancePlan plan = planOpt.get();
        
        // Calculate premium
        InsuranceQuoteRequest quoteRequest = new InsuranceQuoteRequest();
        quoteRequest.setPlanId(request.getPlanId());
        quoteRequest.setCoverageAmount(request.getCoverageAmount());
        quoteRequest.setJourneyStartDate(request.getJourneyStartDate());
        quoteRequest.setJourneyEndDate(request.getJourneyEndDate());
        quoteRequest.setTravelerAge(request.getTravelerAge());
        quoteRequest.setHasMedicalConditions(request.getMedicalConditions() != null && !request.getMedicalConditions().isEmpty());
        
        InsuranceQuoteResponse quote = calculateQuote(quoteRequest);
        
        // Create insurance policy
        TravelInsurance insurance = new TravelInsurance();
        insurance.setUser(user);
        insurance.setPlan(plan);
        insurance.setCoverageAmount(request.getCoverageAmount());
        insurance.setPremiumAmount(quote.getTotalPremium());
        insurance.setGstAmount(quote.getGstAmount());
        insurance.setTotalAmount(quote.getFinalAmount());
        
        insurance.setJourneyStartDate(request.getJourneyStartDate());
        insurance.setJourneyEndDate(request.getJourneyEndDate());
        insurance.setCoverageStartDate(quote.getCoverageStartDate());
        insurance.setCoverageEndDate(quote.getCoverageEndDate());
        
        insurance.setTravelerName(request.getTravelerName());
        insurance.setTravelerAge(request.getTravelerAge());
        if (request.getTravelerGender() != null) {
            insurance.setTravelerGender(User.Gender.valueOf(request.getTravelerGender()));
        }
        insurance.setTravelerPhone(request.getTravelerPhone());
        insurance.setTravelerEmail(request.getTravelerEmail());
        insurance.setTravelerIdProofType(request.getTravelerIdProofType());
        insurance.setTravelerIdProofNumber(request.getTravelerIdProofNumber());
        
        insurance.setSourceStation(request.getSourceStation());
        insurance.setDestinationStation(request.getDestinationStation());
        insurance.setTrainNumber(request.getTrainNumber());
        insurance.setPnrNumber(request.getPnrNumber());
        
        insurance.setEmergencyContactName(request.getEmergencyContactName());
        insurance.setEmergencyContactPhone(request.getEmergencyContactPhone());
        insurance.setEmergencyContactRelation(request.getEmergencyContactRelation());
        
        insurance.setMedicalConditions(request.getMedicalConditions());
        insurance.setSpecialRequirements(request.getSpecialRequirements());
        insurance.setTermsAccepted(request.getTermsAccepted());
        
        // Set claim contact information
        insurance.setClaimContactNumber(plan.getProvider().getContactPhone());
        insurance.setClaimEmail(plan.getProvider().getContactEmail());
        
        insurance = travelInsuranceRepository.save(insurance);
        
        // Update provider statistics
        InsuranceProvider provider = plan.getProvider();
        provider.setTotalPoliciesSold(provider.getTotalPoliciesSold() + 1);
        insuranceProviderRepository.save(provider);
        
        // Update plan popularity
        plan.setPopularityScore(plan.getPopularityScore() + 1);
        insurancePlanRepository.save(plan);
        
        logger.info("Insurance policy created: {}", insurance.getPolicyNumber());
        return insurance;
    }
    
    /**
     * Get user's insurance policies
     */
    public List<TravelInsurance> getUserPolicies(User user) {
        logger.info("Getting insurance policies for user: {}", user.getUsername());
        return travelInsuranceRepository.findByUser(user);
    }
    
    /**
     * Get insurance policy by policy number
     */
    public Optional<TravelInsurance> getPolicyByNumber(String policyNumber) {
        logger.info("Getting insurance policy: {}", policyNumber);
        return travelInsuranceRepository.findByPolicyNumber(policyNumber);
    }
    
    /**
     * Activate insurance policy (after payment)
     */
    public TravelInsurance activatePolicy(String policyNumber) {
        logger.info("Activating insurance policy: {}", policyNumber);
        
        Optional<TravelInsurance> insuranceOpt = travelInsuranceRepository.findByPolicyNumber(policyNumber);
        if (insuranceOpt.isEmpty()) {
            throw new RuntimeException("Insurance policy not found");
        }
        
        TravelInsurance insurance = insuranceOpt.get();
        insurance.setPolicyStatus(TravelInsurance.PolicyStatus.ACTIVE);
        insurance.setPaymentStatus(TravelInsurance.PaymentStatus.PAID);
        insurance.setActivationDate(LocalDateTime.now());
        insurance.setExpiryDate(insurance.getCoverageEndDate().atTime(23, 59, 59));
        
        return travelInsuranceRepository.save(insurance);
    }
    
    /**
     * Cancel insurance policy
     */
    public TravelInsurance cancelPolicy(String policyNumber, String reason) {
        logger.info("Cancelling insurance policy: {}, reason: {}", policyNumber, reason);
        
        Optional<TravelInsurance> insuranceOpt = travelInsuranceRepository.findByPolicyNumber(policyNumber);
        if (insuranceOpt.isEmpty()) {
            throw new RuntimeException("Insurance policy not found");
        }
        
        TravelInsurance insurance = insuranceOpt.get();
        insurance.setPolicyStatus(TravelInsurance.PolicyStatus.CANCELLED);
        
        return travelInsuranceRepository.save(insurance);
    }
    
    // Helper methods
    
    private boolean isEligibleForPlan(InsurancePlan plan, InsuranceQuoteRequest request) {
        // Check age eligibility
        if (request.getTravelerAge() < plan.getAgeMin() || request.getTravelerAge() > plan.getAgeMax()) {
            return false;
        }
        
        // Check coverage amount eligibility
        if (plan.getMinCoverageAmount() != null && request.getCoverageAmount().compareTo(plan.getMinCoverageAmount()) < 0) {
            return false;
        }
        if (plan.getMaxCoverageAmount() != null && request.getCoverageAmount().compareTo(plan.getMaxCoverageAmount()) > 0) {
            return false;
        }
        
        return true;
    }
    
    private BigDecimal calculateBasePremium(InsurancePlan plan, InsuranceQuoteRequest request) {
        // Base premium = (Coverage Amount / 1000) * Premium Rate
        BigDecimal coverageInThousands = request.getCoverageAmount().divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP);
        return coverageInThousands.multiply(plan.getPremiumRate()).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateAgeMultiplier(Integer age) {
        if (age <= 30) {
            return new BigDecimal("1.0");
        } else if (age <= 50) {
            return new BigDecimal("1.2");
        } else if (age <= 65) {
            return new BigDecimal("1.5");
        } else {
            return new BigDecimal("2.0");
        }
    }
    
    private BigDecimal calculateMedicalMultiplier(Boolean hasMedicalConditions) {
        return hasMedicalConditions ? new BigDecimal("1.5") : new BigDecimal("1.0");
    }
    
    private List<String> generateBenefits(InsurancePlan plan) {
        List<String> benefits = new ArrayList<>();
        
        if (plan.getCoversMedicalExpenses()) {
            benefits.add("Medical expenses coverage up to ₹" + plan.getMedicalCoverageLimit());
        }
        if (plan.getCoversTripCancellation()) {
            benefits.add("Trip cancellation coverage up to ₹" + plan.getTripCancellationLimit());
        }
        if (plan.getCoversBaggageLoss()) {
            benefits.add("Baggage loss coverage up to ₹" + plan.getBaggageCoverageLimit());
        }
        if (plan.getCoversPersonalAccident()) {
            benefits.add("Personal accident coverage up to ₹" + plan.getPersonalAccidentLimit());
        }
        if (plan.getCoversEmergencyEvacuation()) {
            benefits.add("Emergency evacuation coverage");
        }
        if (plan.getCovers24x7Support()) {
            benefits.add("24x7 emergency support");
        }
        
        return benefits;
    }
    
    private List<String> generateExclusions(InsurancePlan plan) {
        List<String> exclusions = new ArrayList<>();
        exclusions.add("Pre-existing medical conditions");
        exclusions.add("Suicide or self-inflicted injuries");
        exclusions.add("War, terrorism, or nuclear incidents");
        exclusions.add("Alcohol or drug-related incidents");
        exclusions.add("Adventure sports or extreme activities");
        return exclusions;
    }
    
    private String generateTermsAndConditions(InsurancePlan plan, InsuranceProvider provider) {
        return "Terms and conditions for " + plan.getPlanName() + " by " + provider.getProviderName() + 
               ". Coverage valid from journey start to end date. Claims must be reported within 30 days. " +
               "Deductible amount: ₹" + plan.getDeductibleAmount() + ". For full terms, visit " + 
               (provider.getWebsiteUrl() != null ? provider.getWebsiteUrl() : "provider website");
    }
}
