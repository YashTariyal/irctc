package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.SeatSelectionRequest;
import com.irctc_backend.irctc.dto.SeatSelectionResponse;
import com.irctc_backend.irctc.service.SeatSelectionService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for seat selection functionality
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/seat-selection")
@Tag(name = "Seat Selection", description = "APIs for interactive seat selection and coach layout")
public class SeatSelectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SeatSelectionController.class);
    
    @Autowired
    private SeatSelectionService seatSelectionService;
    
    /**
     * Get available seats for a specific train and coach
     */
    @GetMapping("/trains/{trainId}/coaches/{coachId}/seats")
    @Operation(
        summary = "Get available seats",
        description = "Retrieve all available seats for a specific train and coach on a given journey date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seats retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SeatSelectionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Train or coach not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<SeatSelectionResponse> getAvailableSeats(
            @Parameter(description = "Train ID") @PathVariable Long trainId,
            @Parameter(description = "Coach ID") @PathVariable Long coachId,
            @Parameter(description = "Journey date in YYYY-MM-DD format") @RequestParam String journeyDate) {
        
        logger.info("Getting available seats for train: {}, coach: {}, date: {}", trainId, coachId, journeyDate);
        
        try {
            SeatSelectionResponse response = seatSelectionService.getAvailableSeats(trainId, coachId, journeyDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting available seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Select seats based on preferences
     */
    @PostMapping("/select")
    @Operation(
        summary = "Select seats",
        description = "Select seats based on user preferences and requirements"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seats selected successfully",
                    content = @Content(schema = @Schema(implementation = SeatSelectionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or no seats available"),
        @ApiResponse(responseCode = "404", description = "Train or coach not found")
    })
    public ResponseEntity<SeatSelectionResponse> selectSeats(
            @Valid @RequestBody SeatSelectionRequest request) {
        
        logger.info("Selecting seats for request: {}", request);
        
        try {
            SeatSelectionResponse response = seatSelectionService.selectSeats(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error selecting seats: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get coach layout for visual representation
     */
    @GetMapping("/coaches/{coachId}/layout")
    @Operation(
        summary = "Get coach layout",
        description = "Retrieve the visual layout of a coach for seat selection interface"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coach layout retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Coach not found")
    })
    public ResponseEntity<Map<String, Object>> getCoachLayout(
            @Parameter(description = "Coach ID") @PathVariable Long coachId) {
        
        logger.info("Getting coach layout for coach: {}", coachId);
        
        try {
            Map<String, Object> layout = seatSelectionService.getCoachLayout(coachId);
            return ResponseEntity.ok(layout);
        } catch (Exception e) {
            logger.error("Error getting coach layout: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    /**
     * Get seat preferences options
     */
    @GetMapping("/preferences")
    @Operation(
        summary = "Get seat preferences",
        description = "Get available seat type and berth type options for selection"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getSeatPreferences() {
        logger.info("Getting seat preferences options");
        
        Map<String, Object> preferences = Map.of(
            "seatTypes", new String[]{"WINDOW", "AISLE", "MIDDLE", "SIDE_UPPER", "SIDE_LOWER"},
            "berthTypes", new String[]{"LOWER", "MIDDLE", "UPPER", "SIDE_LOWER", "SIDE_UPPER"},
            "quotaTypes", new String[]{"GENERAL", "LADIES", "SENIOR_CITIZEN", "HANDICAPPED"},
            "seatStatuses", new String[]{"AVAILABLE", "BOOKED", "RESERVED", "MAINTENANCE", "BLOCKED"}
        );
        
        return ResponseEntity.ok(preferences);
    }
    
    /**
     * Get coach types and their characteristics
     */
    @GetMapping("/coach-types")
    @Operation(
        summary = "Get coach types",
        description = "Get information about different coach types and their seat configurations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coach types retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getCoachTypes() {
        logger.info("Getting coach types information");
        
        Map<String, Object> coachTypes = Map.of(
            "AC_FIRST_CLASS", Map.of(
                "description", "Air-conditioned first class with 2-berth compartments",
                "seatsPerCompartment", 2,
                "totalSeats", 18,
                "amenities", new String[]{"AC", "Bedding", "Meals", "Charging Point"}
            ),
            "AC_2_TIER", Map.of(
                "description", "Air-conditioned 2-tier sleeper with 4-berth compartments",
                "seatsPerCompartment", 4,
                "totalSeats", 48,
                "amenities", new String[]{"AC", "Bedding", "Charging Point"}
            ),
            "AC_3_TIER", Map.of(
                "description", "Air-conditioned 3-tier sleeper with 6-berth compartments",
                "seatsPerCompartment", 6,
                "totalSeats", 72,
                "amenities", new String[]{"AC", "Bedding", "Charging Point"}
            ),
            "SLEEPER_CLASS", Map.of(
                "description", "Non-AC sleeper with 6-berth compartments",
                "seatsPerCompartment", 6,
                "totalSeats", 72,
                "amenities", new String[]{"Bedding", "Fan"}
            ),
            "AC_CHAIR_CAR", Map.of(
                "description", "Air-conditioned chair car with 2+2 seating",
                "seatsPerRow", 4,
                "totalSeats", 78,
                "amenities", new String[]{"AC", "Charging Point", "Reclining Seats"}
            )
        );
        
        return ResponseEntity.ok(coachTypes);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if the seat selection service is running"
    )
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Seat Selection Service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}
