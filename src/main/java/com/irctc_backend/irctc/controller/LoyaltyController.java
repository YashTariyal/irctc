package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.LoyaltyAccountResponse;
import com.irctc_backend.irctc.dto.RewardRedemptionRequest;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.service.LoyaltyService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for loyalty points and rewards system
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/loyalty")
@Tag(name = "Loyalty System", description = "IRCTC loyalty points and rewards management APIs")
public class LoyaltyController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoyaltyController.class);
    
    @Autowired
    private LoyaltyService loyaltyService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the loyalty system is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Loyalty Points and Rewards System");
        response.put("timestamp", System.currentTimeMillis());
        response.put("features", new String[]{
            "Loyalty Account Management",
            "Points Earning and Redemption",
            "Tier-based Benefits",
            "Reward Catalog",
            "Redemption History",
            "Expired Points Processing"
        });
        
        logger.info("Loyalty system health check requested");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create loyalty account for user
     */
    @PostMapping("/account/create")
    @Operation(
        summary = "Create loyalty account",
        description = "Create a new loyalty account for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loyalty account created successfully"),
        @ApiResponse(responseCode = "400", description = "User already has a loyalty account"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createLoyaltyAccount(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Creating loyalty account for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            LoyaltyAccount loyaltyAccount = loyaltyService.createLoyaltyAccount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loyalty account created successfully");
            response.put("loyaltyNumber", loyaltyAccount.getLoyaltyNumber());
            response.put("tier", loyaltyAccount.getTier().getDisplayName());
            response.put("welcomePoints", 100);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating loyalty account: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get user's loyalty account
     */
    @GetMapping("/account")
    @Operation(
        summary = "Get loyalty account",
        description = "Get the authenticated user's loyalty account details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loyalty account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LoyaltyAccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Loyalty account not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> getLoyaltyAccount(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Getting loyalty account for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            return loyaltyService.getLoyaltyAccount(user)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting loyalty account: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Get available rewards for user
     */
    @GetMapping("/rewards")
    @Operation(
        summary = "Get available rewards",
        description = "Get rewards available for redemption based on user's tier and points"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Reward>> getAvailableRewards(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Getting available rewards for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            List<Reward> rewards = loyaltyService.getAvailableRewards(user);
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            logger.error("Error getting available rewards: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Redeem reward
     */
    @PostMapping("/rewards/redeem")
    @Operation(
        summary = "Redeem reward",
        description = "Redeem a reward using loyalty points"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reward redeemed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid redemption request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> redeemReward(
            @Valid @RequestBody RewardRedemptionRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        logger.info("Redeeming reward {} for user: {}", request.getRewardId(), username);
        
        try {
            User user = (User) authentication.getPrincipal();
            RewardRedemption redemption = loyaltyService.redeemReward(user, request.getRewardId(), request.getNotes());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reward redeemed successfully");
            response.put("redemptionCode", redemption.getRedemptionCode());
            response.put("rewardName", redemption.getReward().getName());
            response.put("pointsUsed", redemption.getPointsUsed());
            response.put("expiryDate", redemption.getExpiryDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error redeeming reward: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get redemption history
     */
    @GetMapping("/redemptions")
    @Operation(
        summary = "Get redemption history",
        description = "Get the authenticated user's reward redemption history"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Redemption history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RewardRedemption>> getRedemptionHistory(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Getting redemption history for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            List<RewardRedemption> redemptions = loyaltyService.getRedemptionHistory(user);
            return ResponseEntity.ok(redemptions);
        } catch (Exception e) {
            logger.error("Error getting redemption history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Get loyalty tiers information
     */
    @GetMapping("/tiers")
    @Operation(
        summary = "Get loyalty tiers",
        description = "Get information about all loyalty tiers and their benefits"
    )
    @ApiResponse(responseCode = "200", description = "Loyalty tiers retrieved successfully")
    public ResponseEntity<Map<String, Object>> getLoyaltyTiers() {
        logger.info("Getting loyalty tiers information");
        
        Map<String, Object> response = new HashMap<>();
        
        LoyaltyAccount.LoyaltyTier[] tiers = LoyaltyAccount.LoyaltyTier.values();
        Map<String, Object> tierInfo = new HashMap<>();
        
        for (LoyaltyAccount.LoyaltyTier tier : tiers) {
            Map<String, Object> tierDetails = new HashMap<>();
            tierDetails.put("displayName", tier.getDisplayName());
            tierDetails.put("minimumPoints", tier.getMinimumPoints());
            tierDetails.put("multiplier", tier.getMultiplier());
            tierDetails.put("description", tier.getDescription());
            tierInfo.put(tier.name(), tierDetails);
        }
        
        response.put("tiers", tierInfo);
        response.put("totalTiers", tiers.length);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process expired points (Admin only)
     */
    @PostMapping("/admin/process-expired-points")
    @Operation(
        summary = "Process expired points",
        description = "Process and expire points that have reached their expiry date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Expired points processed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> processExpiredPoints() {
        logger.info("Processing expired points");
        
        try {
            loyaltyService.processExpiredPoints();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Expired points processed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing expired points: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
