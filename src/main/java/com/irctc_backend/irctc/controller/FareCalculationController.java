package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.FareCalculationRequest;
import com.irctc_backend.irctc.dto.FareCalculationResponse;
import com.irctc_backend.irctc.service.FareCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for dynamic fare calculation with surge pricing
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/fare-calculation")
@Tag(name = "Fare Calculation", description = "Dynamic fare calculation with surge pricing APIs")
public class FareCalculationController {
    
    private static final Logger logger = LoggerFactory.getLogger(FareCalculationController.class);
    
    @Autowired
    private FareCalculationService fareCalculationService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the fare calculation service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Fare Calculation Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("features", new String[]{
            "Dynamic Fare Calculation",
            "Surge Pricing",
            "Peak Hour Pricing",
            "Weekend Pricing",
            "Festival Pricing",
            "Discount Calculation",
            "GST Calculation"
        });
        
        logger.info("Fare calculation service health check requested");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculate dynamic fare with surge pricing
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate dynamic fare",
        description = "Calculate fare with surge pricing, discounts, and GST for train booking"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fare calculated successfully",
                    content = @Content(schema = @Schema(implementation = FareCalculationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Train or fare rule not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<FareCalculationResponse> calculateFare(
            @Valid @RequestBody FareCalculationRequest request) {
        
        logger.info("Calculating fare for train: {}, coach: {}, passengers: {}", 
                   request.getTrainId(), request.getCoachType(), request.getNumberOfPassengers());
        
        try {
            FareCalculationResponse response = fareCalculationService.calculateFare(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating fare: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get fare estimate for a quick calculation
     */
    @GetMapping("/estimate")
    @Operation(
        summary = "Get fare estimate",
        description = "Get a quick fare estimate without detailed breakdown"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fare estimate calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Train not found")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getFareEstimate(
            @Parameter(description = "Train ID") @RequestParam Long trainId,
            @Parameter(description = "Coach type") @RequestParam String coachType,
            @Parameter(description = "Number of passengers") @RequestParam Integer numberOfPassengers,
            @Parameter(description = "Distance in KM") @RequestParam Integer distanceKm,
            @Parameter(description = "Journey date (YYYY-MM-DD)") @RequestParam String journeyDate) {
        
        logger.info("Getting fare estimate for train: {}, coach: {}, passengers: {}", 
                   trainId, coachType, numberOfPassengers);
        
        try {
            // Create a simplified request for estimation
            FareCalculationRequest request = new FareCalculationRequest();
            request.setTrainId(trainId);
            request.setCoachType(com.irctc_backend.irctc.entity.Coach.CoachType.valueOf(coachType));
            request.setNumberOfPassengers(numberOfPassengers);
            request.setDistanceKm(distanceKm);
            request.setJourneyDate(java.time.LocalDateTime.parse(journeyDate + "T00:00:00"));
            
            FareCalculationResponse response = fareCalculationService.calculateFare(request);
            
            Map<String, Object> estimate = new HashMap<>();
            estimate.put("trainId", response.getTrainId());
            estimate.put("trainNumber", response.getTrainNumber());
            estimate.put("trainName", response.getTrainName());
            estimate.put("coachType", response.getCoachType());
            estimate.put("numberOfPassengers", response.getNumberOfPassengers());
            estimate.put("baseFare", response.getBaseFare());
            estimate.put("totalFare", response.getTotalFare());
            estimate.put("finalAmount", response.getFinalAmount());
            estimate.put("isSurgeActive", response.getIsSurgeActive());
            estimate.put("surgeReason", response.getSurgeReason());
            estimate.put("currency", response.getCurrency());
            
            return ResponseEntity.ok(estimate);
        } catch (Exception e) {
            logger.error("Error getting fare estimate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get surge pricing information
     */
    @GetMapping("/surge-info")
    @Operation(
        summary = "Get surge pricing information",
        description = "Get current surge pricing multipliers and reasons"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Surge pricing information retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSurgeInfo(
            @Parameter(description = "Train ID") @RequestParam Long trainId,
            @Parameter(description = "Coach type") @RequestParam String coachType,
            @Parameter(description = "Journey date (YYYY-MM-DD)") @RequestParam String journeyDate) {
        
        logger.info("Getting surge pricing info for train: {}, coach: {}, date: {}", 
                   trainId, coachType, journeyDate);
        
        try {
            Map<String, Object> surgeInfo = new HashMap<>();
            
            // Check if it's peak hour
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(journeyDate + "T12:00:00");
            boolean isPeakHour = isPeakHour(dateTime);
            boolean isWeekend = isWeekend(dateTime);
            boolean isFestival = isFestivalPeriod(dateTime);
            
            surgeInfo.put("isPeakHour", isPeakHour);
            surgeInfo.put("isWeekend", isWeekend);
            surgeInfo.put("isFestival", isFestival);
            surgeInfo.put("peakHourMultiplier", isPeakHour ? 1.2 : 1.0);
            surgeInfo.put("weekendMultiplier", isWeekend ? 1.15 : 1.0);
            surgeInfo.put("festivalMultiplier", isFestival ? 1.3 : 1.0);
            
            // Calculate total surge multiplier
            double totalMultiplier = 1.0;
            if (isPeakHour) totalMultiplier *= 1.2;
            if (isWeekend) totalMultiplier *= 1.15;
            if (isFestival) totalMultiplier *= 1.3;
            
            surgeInfo.put("totalSurgeMultiplier", totalMultiplier);
            surgeInfo.put("isSurgeActive", totalMultiplier > 1.0);
            
            return ResponseEntity.ok(surgeInfo);
        } catch (Exception e) {
            logger.error("Error getting surge info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get available discounts
     */
    @GetMapping("/discounts")
    @Operation(
        summary = "Get available discounts",
        description = "Get information about available discounts and their rates"
    )
    @ApiResponse(responseCode = "200", description = "Discount information retrieved successfully")
    public ResponseEntity<Map<String, Object>> getAvailableDiscounts() {
        logger.info("Getting available discounts information");
        
        Map<String, Object> discounts = new HashMap<>();
        
        Map<String, Object> ladiesQuota = new HashMap<>();
        ladiesQuota.put("discount", "5%");
        ladiesQuota.put("description", "Ladies quota discount");
        ladiesQuota.put("applicable", "Female passengers in ladies quota");
        
        Map<String, Object> seniorCitizen = new HashMap<>();
        seniorCitizen.put("discount", "10%");
        seniorCitizen.put("description", "Senior citizen discount");
        seniorCitizen.put("applicable", "Passengers aged 60 and above");
        
        Map<String, Object> handicapped = new HashMap<>();
        handicapped.put("discount", "20%");
        handicapped.put("description", "Handicapped passenger discount");
        handicapped.put("applicable", "Passengers with valid handicap certificate");
        
        discounts.put("ladiesQuota", ladiesQuota);
        discounts.put("seniorCitizen", seniorCitizen);
        discounts.put("handicapped", handicapped);
        
        return ResponseEntity.ok(discounts);
    }
    
    // Helper methods for surge calculation
    private boolean isPeakHour(java.time.LocalDateTime dateTime) {
        java.time.LocalTime time = dateTime.toLocalTime();
        return (time.isAfter(java.time.LocalTime.of(6, 0)) && time.isBefore(java.time.LocalTime.of(10, 0))) ||
               (time.isAfter(java.time.LocalTime.of(18, 0)) && time.isBefore(java.time.LocalTime.of(22, 0)));
    }
    
    private boolean isWeekend(java.time.LocalDateTime dateTime) {
        java.time.DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY;
    }
    
    private boolean isFestivalPeriod(java.time.LocalDateTime dateTime) {
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        
        return (month == 10 && day >= 20 && day <= 30) || // Diwali period
               (month == 3 && day >= 10 && day <= 20) ||  // Holi period
               (month == 12 && day >= 20 && day <= 31);   // Christmas/New Year period
    }
}
