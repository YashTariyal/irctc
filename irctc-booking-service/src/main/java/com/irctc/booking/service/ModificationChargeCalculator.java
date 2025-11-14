package com.irctc.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for calculating modification charges based on business rules
 */
@Service
public class ModificationChargeCalculator {
    
    private static final Logger logger = LoggerFactory.getLogger(ModificationChargeCalculator.class);
    
    // Base modification charges (in INR)
    private static final BigDecimal BASE_DATE_CHANGE_CHARGE = new BigDecimal("200.00");
    private static final BigDecimal BASE_SEAT_UPGRADE_CHARGE = new BigDecimal("100.00");
    private static final BigDecimal BASE_ROUTE_CHANGE_CHARGE = new BigDecimal("300.00");
    private static final BigDecimal BASE_PASSENGER_MODIFICATION_CHARGE = new BigDecimal("150.00");
    
    // Time-based multipliers
    private static final BigDecimal SAME_DAY_MULTIPLIER = new BigDecimal("2.0"); // 2x charge for same-day changes
    private static final BigDecimal WITHIN_24H_MULTIPLIER = new BigDecimal("1.5"); // 1.5x charge within 24 hours
    private static final BigDecimal WITHIN_48H_MULTIPLIER = new BigDecimal("1.2"); // 1.2x charge within 48 hours
    
    /**
     * Calculate modification charge for date change
     */
    public BigDecimal calculateDateChangeCharge(LocalDateTime originalDate, LocalDateTime modificationTime) {
        BigDecimal baseCharge = BASE_DATE_CHANGE_CHARGE;
        
        long hoursUntilJourney = ChronoUnit.HOURS.between(modificationTime, originalDate);
        
        if (hoursUntilJourney < 0) {
            // Past date - should not happen, but handle gracefully
            logger.warn("Date change requested for past journey date");
            return baseCharge;
        }
        
        if (hoursUntilJourney < 24) {
            // Same day or within 24 hours
            baseCharge = baseCharge.multiply(SAME_DAY_MULTIPLIER);
        } else if (hoursUntilJourney < 48) {
            // Within 48 hours
            baseCharge = baseCharge.multiply(WITHIN_48H_MULTIPLIER);
        } else if (hoursUntilJourney < 72) {
            // Within 72 hours
            baseCharge = baseCharge.multiply(WITHIN_24H_MULTIPLIER);
        }
        
        return baseCharge.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate modification charge for seat upgrade/downgrade
     */
    public BigDecimal calculateSeatUpgradeCharge(String currentClass, String newClass, 
                                                 LocalDateTime journeyDate, LocalDateTime modificationTime) {
        BigDecimal baseCharge = BASE_SEAT_UPGRADE_CHARGE;
        
        // Higher charge for downgrade (less common)
        if (isDowngrade(currentClass, newClass)) {
            baseCharge = baseCharge.multiply(new BigDecimal("0.5")); // 50% of base for downgrade
        }
        
        // Time-based multiplier
        long hoursUntilJourney = ChronoUnit.HOURS.between(modificationTime, journeyDate);
        if (hoursUntilJourney < 24) {
            baseCharge = baseCharge.multiply(SAME_DAY_MULTIPLIER);
        } else if (hoursUntilJourney < 48) {
            baseCharge = baseCharge.multiply(WITHIN_48H_MULTIPLIER);
        }
        
        return baseCharge.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate modification charge for route change
     */
    public BigDecimal calculateRouteChangeCharge(LocalDateTime journeyDate, LocalDateTime modificationTime) {
        BigDecimal baseCharge = BASE_ROUTE_CHANGE_CHARGE;
        
        long hoursUntilJourney = ChronoUnit.HOURS.between(modificationTime, journeyDate);
        if (hoursUntilJourney < 24) {
            baseCharge = baseCharge.multiply(SAME_DAY_MULTIPLIER);
        } else if (hoursUntilJourney < 48) {
            baseCharge = baseCharge.multiply(WITHIN_48H_MULTIPLIER);
        }
        
        return baseCharge.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate modification charge for passenger modification
     */
    public BigDecimal calculatePassengerModificationCharge(int passengersAdded, int passengersRemoved,
                                                           LocalDateTime journeyDate, LocalDateTime modificationTime) {
        BigDecimal baseCharge = BASE_PASSENGER_MODIFICATION_CHARGE;
        
        // Charge per passenger added/removed
        int totalChanges = passengersAdded + passengersRemoved;
        baseCharge = baseCharge.multiply(new BigDecimal(totalChanges));
        
        // Time-based multiplier
        long hoursUntilJourney = ChronoUnit.HOURS.between(modificationTime, journeyDate);
        if (hoursUntilJourney < 24) {
            baseCharge = baseCharge.multiply(SAME_DAY_MULTIPLIER);
        } else if (hoursUntilJourney < 48) {
            baseCharge = baseCharge.multiply(WITHIN_48H_MULTIPLIER);
        }
        
        return baseCharge.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Check if seat change is a downgrade
     */
    private boolean isDowngrade(String currentClass, String newClass) {
        // Define class hierarchy (higher number = better class)
        int currentRank = getClassRank(currentClass);
        int newRank = getClassRank(newClass);
        return newRank < currentRank;
    }
    
    /**
     * Get rank of seat class (higher = better)
     */
    private int getClassRank(String seatClass) {
        return switch (seatClass.toUpperCase()) {
            case "1AC", "FIRST_AC" -> 5;
            case "2AC", "SECOND_AC" -> 4;
            case "3AC", "THIRD_AC" -> 3;
            case "AC", "AC_CHAIR" -> 2;
            case "SLEEPER", "SL" -> 1;
            default -> 0;
        };
    }
    
    /**
     * Calculate fare difference between old and new fare
     */
    public BigDecimal calculateFareDifference(BigDecimal originalFare, BigDecimal newFare) {
        return newFare.subtract(originalFare).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate total amount to be paid/refunded
     * Positive = amount to pay, Negative = amount to refund
     */
    public BigDecimal calculateTotalAmount(BigDecimal fareDifference, BigDecimal modificationCharge) {
        return fareDifference.add(modificationCharge).setScale(2, RoundingMode.HALF_UP);
    }
}

