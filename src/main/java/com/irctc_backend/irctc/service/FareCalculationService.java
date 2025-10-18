package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.FareCalculationRequest;
import com.irctc_backend.irctc.dto.FareCalculationResponse;
import com.irctc_backend.irctc.entity.Coach;
import com.irctc_backend.irctc.entity.FareRule;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.repository.FareRuleRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for dynamic fare calculation with surge pricing
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class FareCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FareCalculationService.class);
    
    @Autowired
    private FareRuleRepository fareRuleRepository;
    
    @Autowired
    private TrainRepository trainRepository;
    
    // GST rate (18%)
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    
    // Peak hours (6 AM - 10 AM, 6 PM - 10 PM)
    private static final LocalTime PEAK_START_MORNING = LocalTime.of(6, 0);
    private static final LocalTime PEAK_END_MORNING = LocalTime.of(10, 0);
    private static final LocalTime PEAK_START_EVENING = LocalTime.of(18, 0);
    private static final LocalTime PEAK_END_EVENING = LocalTime.of(22, 0);
    
    /**
     * Calculate dynamic fare with surge pricing
     */
    public FareCalculationResponse calculateFare(FareCalculationRequest request) {
        logger.info("Calculating fare for train: {}, coach: {}, passengers: {}", 
                   request.getTrainId(), request.getCoachType(), request.getNumberOfPassengers());
        
        try {
            // Get train information
            Train train = trainRepository.findById(request.getTrainId())
                    .orElseThrow(() -> new RuntimeException("Train not found"));
            
            // Find applicable fare rule
            FareRule fareRule = findApplicableFareRule(train, request.getCoachType(), request.getJourneyDate());
            
            // Create response object
            FareCalculationResponse response = new FareCalculationResponse(
                    train.getId(), train.getTrainNumber(), train.getTrainName(), 
                    request.getCoachType(), request.getJourneyDate());
            
            // Set basic information
            response.setNumberOfPassengers(request.getNumberOfPassengers());
            response.setDistanceKm(request.getDistanceKm());
            response.setQuotaType(request.getQuotaType());
            response.setFareRuleId(fareRule.getId().toString());
            
            // Calculate base fare
            BigDecimal baseFare = calculateBaseFare(fareRule, request);
            response.setBaseFare(baseFare);
            
            // Calculate surge pricing
            calculateSurgePricing(response, request, fareRule);
            
            // Calculate discounts
            calculateDiscounts(response, request, fareRule);
            
            // Calculate final amounts
            calculateFinalAmounts(response, request);
            
            // Calculate passenger-wise breakdown
            calculatePassengerBreakdown(response, request);
            
            logger.info("Fare calculation completed. Final amount: {}", response.getFinalAmount());
            return response;
            
        } catch (Exception e) {
            logger.error("Error calculating fare: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate fare: " + e.getMessage());
        }
    }
    
    /**
     * Find applicable fare rule for the given criteria
     */
    private FareRule findApplicableFareRule(Train train, Coach.CoachType coachType, LocalDateTime journeyDate) {
        // First try to find exact match
        Optional<FareRule> exactMatch = fareRuleRepository.findActiveFareRule(train, coachType, journeyDate);
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }
        
        // If no exact match, find the closest distance-based rule
        List<FareRule> closestRules = fareRuleRepository.findClosestFareRuleByDistance(train, coachType, 0);
        if (!closestRules.isEmpty()) {
            return closestRules.get(0);
        }
        
        throw new RuntimeException("No fare rule found for train: " + train.getTrainNumber() + 
                                 " and coach type: " + coachType);
    }
    
    /**
     * Calculate base fare
     */
    private BigDecimal calculateBaseFare(FareRule fareRule, FareCalculationRequest request) {
        BigDecimal baseFare = fareRule.getBaseFare();
        
        // Apply distance-based calculation if needed
        if (request.getDistanceKm() != null && fareRule.getDistanceKm() != null) {
            if (!request.getDistanceKm().equals(fareRule.getDistanceKm())) {
                // Calculate per km rate and apply to actual distance
                BigDecimal perKmRate = fareRule.getBaseFare().divide(
                        new BigDecimal(fareRule.getDistanceKm()), 2, RoundingMode.HALF_UP);
                baseFare = perKmRate.multiply(new BigDecimal(request.getDistanceKm()));
            }
        }
        
        return baseFare.multiply(new BigDecimal(request.getNumberOfPassengers()));
    }
    
    /**
     * Calculate surge pricing based on various factors
     */
    private void calculateSurgePricing(FareCalculationResponse response, FareCalculationRequest request, FareRule fareRule) {
        BigDecimal totalSurgeMultiplier = BigDecimal.ONE;
        List<String> surgeReasons = new ArrayList<>();
        
        // Peak hour surge
        if (isPeakHour(request.getJourneyDate())) {
            BigDecimal peakMultiplier = fareRule.getPeakHourMultiplier() != null ? 
                    fareRule.getPeakHourMultiplier() : new BigDecimal("1.2");
            totalSurgeMultiplier = totalSurgeMultiplier.multiply(peakMultiplier);
            response.setPeakHourMultiplier(peakMultiplier);
            response.setPeakHourFare(response.getBaseFare().multiply(peakMultiplier.subtract(BigDecimal.ONE)));
            surgeReasons.add("Peak Hour");
        }
        
        // Weekend surge
        if (isWeekend(request.getJourneyDate())) {
            BigDecimal weekendMultiplier = fareRule.getWeekendMultiplier() != null ? 
                    fareRule.getWeekendMultiplier() : new BigDecimal("1.15");
            totalSurgeMultiplier = totalSurgeMultiplier.multiply(weekendMultiplier);
            response.setWeekendMultiplier(weekendMultiplier);
            response.setWeekendFare(response.getBaseFare().multiply(weekendMultiplier.subtract(BigDecimal.ONE)));
            surgeReasons.add("Weekend");
        }
        
        // Festival surge (simplified - in real implementation, this would check festival calendar)
        if (isFestivalPeriod(request.getJourneyDate())) {
            BigDecimal festivalMultiplier = fareRule.getFestivalMultiplier() != null ? 
                    fareRule.getFestivalMultiplier() : new BigDecimal("1.3");
            totalSurgeMultiplier = totalSurgeMultiplier.multiply(festivalMultiplier);
            response.setFestivalMultiplier(festivalMultiplier);
            response.setFestivalFare(response.getBaseFare().multiply(festivalMultiplier.subtract(BigDecimal.ONE)));
            surgeReasons.add("Festival Period");
        }
        
        // General surge multiplier
        BigDecimal generalSurgeMultiplier = fareRule.getSurgeMultiplier() != null ? 
                fareRule.getSurgeMultiplier() : BigDecimal.ONE;
        totalSurgeMultiplier = totalSurgeMultiplier.multiply(generalSurgeMultiplier);
        response.setSurgeMultiplier(generalSurgeMultiplier);
        
        // Calculate surge fare
        if (totalSurgeMultiplier.compareTo(BigDecimal.ONE) > 0) {
            BigDecimal surgeFare = response.getBaseFare().multiply(totalSurgeMultiplier.subtract(BigDecimal.ONE));
            response.setSurgeFare(surgeFare);
            response.setIsSurgeActive(true);
            response.setSurgeReason(String.join(", ", surgeReasons));
        } else {
            response.setIsSurgeActive(false);
        }
        
        // Calculate subtotal with surge
        BigDecimal subtotal = response.getBaseFare().multiply(totalSurgeMultiplier);
        response.setSubtotalFare(subtotal);
    }
    
    /**
     * Calculate applicable discounts
     */
    private void calculateDiscounts(FareCalculationResponse response, FareCalculationRequest request, FareRule fareRule) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        
        // Ladies quota discount
        if (request.getIsLadiesQuota() && fareRule.getLadiesQuotaDiscount() != null) {
            BigDecimal ladiesDiscount = response.getSubtotalFare().multiply(fareRule.getLadiesQuotaDiscount());
            response.setLadiesQuotaDiscount(ladiesDiscount);
            totalDiscount = totalDiscount.add(ladiesDiscount);
        }
        
        // Senior citizen discount
        if (request.getIsSeniorCitizenQuota() && fareRule.getSeniorCitizenDiscount() != null) {
            BigDecimal seniorDiscount = response.getSubtotalFare().multiply(fareRule.getSeniorCitizenDiscount());
            response.setSeniorCitizenDiscount(seniorDiscount);
            totalDiscount = totalDiscount.add(seniorDiscount);
        }
        
        // Handicapped discount
        if (request.getIsHandicappedFriendly() && fareRule.getHandicappedDiscount() != null) {
            BigDecimal handicappedDiscount = response.getSubtotalFare().multiply(fareRule.getHandicappedDiscount());
            response.setHandicappedDiscount(handicappedDiscount);
            totalDiscount = totalDiscount.add(handicappedDiscount);
        }
        
        response.setTotalDiscount(totalDiscount);
    }
    
    /**
     * Calculate final amounts including GST
     */
    private void calculateFinalAmounts(FareCalculationResponse response, FareCalculationRequest request) {
        // Calculate total fare before GST
        BigDecimal totalFare = response.getSubtotalFare().subtract(response.getTotalDiscount());
        response.setTotalFare(totalFare);
        
        // Add Tatkal charges if applicable
        if (request.getIsTatkal()) {
            // In real implementation, this would be calculated based on fare rule
            BigDecimal tatkalCharge = totalFare.multiply(new BigDecimal("0.1")); // 10% of base fare
            response.setTatkalFare(tatkalCharge);
            totalFare = totalFare.add(tatkalCharge);
        }
        
        if (request.getIsPremiumTatkal()) {
            BigDecimal premiumTatkalCharge = totalFare.multiply(new BigDecimal("0.15")); // 15% of base fare
            response.setPremiumTatkalFare(premiumTatkalCharge);
            totalFare = totalFare.add(premiumTatkalCharge);
        }
        
        // Calculate GST
        BigDecimal gstAmount = totalFare.multiply(GST_RATE);
        response.setGstAmount(gstAmount);
        
        // Final amount
        BigDecimal finalAmount = totalFare.add(gstAmount);
        response.setFinalAmount(finalAmount.setScale(2, RoundingMode.HALF_UP));
    }
    
    /**
     * Calculate passenger-wise fare breakdown
     */
    private void calculatePassengerBreakdown(FareCalculationResponse response, FareCalculationRequest request) {
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            return;
        }
        
        List<FareCalculationResponse.PassengerFare> passengerFares = new ArrayList<>();
        BigDecimal farePerPassenger = response.getTotalFare().divide(
                new BigDecimal(request.getNumberOfPassengers()), 2, RoundingMode.HALF_UP);
        
        for (FareCalculationRequest.PassengerInfo passenger : request.getPassengers()) {
            FareCalculationResponse.PassengerFare passengerFare = 
                    new FareCalculationResponse.PassengerFare(
                            passenger.getName(), passenger.getAge(), passenger.getGender());
            
            passengerFare.setBaseFare(farePerPassenger);
            
            // Apply individual discounts
            BigDecimal discount = BigDecimal.ZERO;
            String discountReason = "";
            
            if (passenger.getIsSeniorCitizen() && response.getSeniorCitizenDiscount() != null) {
                discount = farePerPassenger.multiply(new BigDecimal("0.1")); // 10% for senior citizens
                discountReason = "Senior Citizen";
            } else if (passenger.getIsHandicapped() && response.getHandicappedDiscount() != null) {
                discount = farePerPassenger.multiply(new BigDecimal("0.2")); // 20% for handicapped
                discountReason = "Handicapped";
            } else if (passenger.getIsLadiesQuota() && response.getLadiesQuotaDiscount() != null) {
                discount = farePerPassenger.multiply(new BigDecimal("0.05")); // 5% for ladies quota
                discountReason = "Ladies Quota";
            }
            
            passengerFare.setApplicableDiscount(discount);
            passengerFare.setDiscountReason(discountReason);
            passengerFare.setFinalFare(farePerPassenger.subtract(discount));
            
            passengerFares.add(passengerFare);
        }
        
        response.setPassengerFares(passengerFares);
    }
    
    /**
     * Check if the given time is during peak hours
     */
    private boolean isPeakHour(LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        return (time.isAfter(PEAK_START_MORNING) && time.isBefore(PEAK_END_MORNING)) ||
               (time.isAfter(PEAK_START_EVENING) && time.isBefore(PEAK_END_EVENING));
    }
    
    /**
     * Check if the given date is a weekend
     */
    private boolean isWeekend(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Check if the given date is during a festival period
     * This is a simplified implementation - in real scenario, it would check a festival calendar
     */
    private boolean isFestivalPeriod(LocalDateTime dateTime) {
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        
        // Simplified festival periods (Diwali, Holi, etc.)
        return (month == 10 && day >= 20 && day <= 30) || // Diwali period
               (month == 3 && day >= 10 && day <= 20) ||  // Holi period
               (month == 12 && day >= 20 && day <= 31);   // Christmas/New Year period
    }
}
