package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.WaitlistRequest;
import com.irctc_backend.irctc.dto.WaitlistResponse;
import com.irctc_backend.irctc.service.WaitlistRacService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for waitlist and RAC management
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/waitlist-rac")
@Tag(name = "Waitlist & RAC Management", description = "APIs for waitlist and RAC (Reservation Against Cancellation) management")
public class WaitlistRacController {
    
    private static final Logger logger = LoggerFactory.getLogger(WaitlistRacController.class);
    
    @Autowired
    private WaitlistRacService waitlistRacService;
    
    /**
     * Add user to waitlist
     */
    @PostMapping("/waitlist")
    @Operation(
        summary = "Add to waitlist",
        description = "Add user to waitlist for a specific train and coach"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully added to waitlist",
                    content = @Content(schema = @Schema(implementation = WaitlistResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or already in waitlist"),
        @ApiResponse(responseCode = "404", description = "Train or coach not found")
    })
    public ResponseEntity<WaitlistResponse> addToWaitlist(@Valid @RequestBody WaitlistRequest request) {
        logger.info("Adding user to waitlist for train: {}, coach: {}", request.getTrainId(), request.getCoachId());
        
        try {
            Long userId = getCurrentUserId();
            WaitlistResponse response = waitlistRacService.addToWaitlist(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error adding to waitlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * Get user's waitlist entries
     */
    @GetMapping("/waitlist")
    @Operation(
        summary = "Get user waitlist entries",
        description = "Retrieve all waitlist entries for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Waitlist entries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = WaitlistResponse.class)))
    })
    public ResponseEntity<List<WaitlistResponse>> getUserWaitlistEntries() {
        logger.info("Getting waitlist entries for current user");
        
        try {
            Long userId = getCurrentUserId();
            List<WaitlistResponse> entries = waitlistRacService.getUserWaitlistEntries(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.error("Error getting waitlist entries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Cancel waitlist entry
     */
    @DeleteMapping("/waitlist/{waitlistId}")
    @Operation(
        summary = "Cancel waitlist entry",
        description = "Cancel a specific waitlist entry"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Waitlist entry cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel waitlist entry"),
        @ApiResponse(responseCode = "404", description = "Waitlist entry not found")
    })
    public ResponseEntity<Map<String, Object>> cancelWaitlistEntry(
            @Parameter(description = "Waitlist entry ID") @PathVariable Long waitlistId) {
        logger.info("Cancelling waitlist entry: {}", waitlistId);
        
        try {
            Long userId = getCurrentUserId();
            boolean success = waitlistRacService.cancelWaitlistEntry(userId, waitlistId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Waitlist entry cancelled successfully",
                    "timestamp", LocalDateTime.now()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to cancel waitlist entry",
                    "timestamp", LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            logger.error("Error cancelling waitlist entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error cancelling waitlist entry: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Process waitlist for a train
     */
    @PostMapping("/process/{trainId}")
    @Operation(
        summary = "Process waitlist",
        description = "Process waitlist entries for a specific train and convert eligible entries to RAC"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Waitlist processed successfully"),
        @ApiResponse(responseCode = "404", description = "Train not found")
    })
    public ResponseEntity<Map<String, Object>> processWaitlist(
            @Parameter(description = "Train ID") @PathVariable Long trainId,
            @Parameter(description = "Journey date in YYYY-MM-DD format") @RequestParam String journeyDate) {
        logger.info("Processing waitlist for train: {}, date: {}", trainId, journeyDate);
        
        try {
            LocalDateTime journeyDateTime = LocalDateTime.parse(journeyDate + "T00:00:00");
            waitlistRacService.processWaitlistForTrain(trainId, journeyDateTime);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Waitlist processed successfully for train: " + trainId,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            logger.error("Error processing waitlist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error processing waitlist: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Get waitlist statistics
     */
    @GetMapping("/stats/{trainId}")
    @Operation(
        summary = "Get waitlist statistics",
        description = "Get waitlist and RAC statistics for a specific train"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getWaitlistStats(
            @Parameter(description = "Train ID") @PathVariable Long trainId,
            @Parameter(description = "Journey date in YYYY-MM-DD format") @RequestParam String journeyDate) {
        logger.info("Getting waitlist statistics for train: {}, date: {}", trainId, journeyDate);
        
        try {
            // This would be implemented in the service
            Map<String, Object> stats = Map.of(
                "trainId", trainId,
                "journeyDate", journeyDate,
                "totalWaitlistEntries", 0,
                "totalRacEntries", 0,
                "availableSeats", 0,
                "lastProcessed", LocalDateTime.now()
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting waitlist statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Get quota types
     */
    @GetMapping("/quota-types")
    @Operation(
        summary = "Get quota types",
        description = "Get available quota types for waitlist"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quota types retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getQuotaTypes() {
        logger.info("Getting quota types");
        
        Map<String, Object> quotaTypes = Map.of(
            "quotaTypes", new String[]{
                "GENERAL", "TATKAL", "LADIES", "SENIOR_CITIZEN", "HANDICAPPED",
                "DEFENCE", "PARLIAMENT", "FOREIGN_TOURIST", "PREMIUM_TATKAL"
            },
            "descriptions", Map.of(
                "GENERAL", "General quota for all passengers",
                "TATKAL", "Tatkal quota for urgent bookings",
                "LADIES", "Ladies quota for female passengers",
                "SENIOR_CITIZEN", "Senior citizen quota for passengers above 60",
                "HANDICAPPED", "Quota for handicapped passengers",
                "DEFENCE", "Defence personnel quota",
                "PARLIAMENT", "Parliament quota",
                "FOREIGN_TOURIST", "Foreign tourist quota",
                "PREMIUM_TATKAL", "Premium Tatkal quota"
            )
        );
        
        return ResponseEntity.ok(quotaTypes);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if the waitlist and RAC service is running"
    )
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Waitlist & RAC Management Service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // This would need to be implemented based on your user details structure
            // For now, return a default user ID
            return 1L;
        }
        throw new RuntimeException("User not authenticated");
    }
}
