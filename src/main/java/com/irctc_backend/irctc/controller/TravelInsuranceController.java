package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.InsuranceQuoteRequest;
import com.irctc_backend.irctc.dto.InsuranceQuoteResponse;
import com.irctc_backend.irctc.dto.InsurancePurchaseRequest;
import com.irctc_backend.irctc.entity.*;
import com.irctc_backend.irctc.service.TravelInsuranceService;
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
import java.util.Optional;

/**
 * REST Controller for travel insurance management
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/travel-insurance")
@Tag(name = "Travel Insurance", description = "IRCTC travel insurance booking and management APIs")
public class TravelInsuranceController {
    
    private static final Logger logger = LoggerFactory.getLogger(TravelInsuranceController.class);
    
    @Autowired
    private TravelInsuranceService travelInsuranceService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the travel insurance system is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Travel Insurance System");
        response.put("timestamp", System.currentTimeMillis());
        response.put("features", new String[]{
            "Insurance Provider Management",
            "Insurance Plan Catalog",
            "Quote Calculation",
            "Policy Purchase",
            "Policy Management",
            "Claims Support"
        });
        
        logger.info("Travel insurance system health check requested");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all active insurance providers
     */
    @GetMapping("/providers")
    @Operation(
        summary = "Get insurance providers",
        description = "Get list of all active insurance providers"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Providers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsuranceProvider>> getProviders() {
        logger.info("Getting all insurance providers");
        List<InsuranceProvider> providers = travelInsuranceService.getActiveProviders();
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get featured insurance providers
     */
    @GetMapping("/providers/featured")
    @Operation(
        summary = "Get featured providers",
        description = "Get list of featured insurance providers"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured providers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsuranceProvider>> getFeaturedProviders() {
        logger.info("Getting featured insurance providers");
        List<InsuranceProvider> providers = travelInsuranceService.getFeaturedProviders();
        return ResponseEntity.ok(providers);
    }
    
    /**
     * Get all active insurance plans
     */
    @GetMapping("/plans")
    @Operation(
        summary = "Get insurance plans",
        description = "Get list of all active insurance plans"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsurancePlan>> getPlans() {
        logger.info("Getting all insurance plans");
        List<InsurancePlan> plans = travelInsuranceService.getActivePlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get plans by provider
     */
    @GetMapping("/plans/provider/{providerId}")
    @Operation(
        summary = "Get plans by provider",
        description = "Get insurance plans for a specific provider"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Provider not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsurancePlan>> getPlansByProvider(
            @Parameter(description = "Provider ID") @PathVariable Long providerId) {
        logger.info("Getting plans for provider: {}", providerId);
        List<InsurancePlan> plans = travelInsuranceService.getPlansByProvider(providerId);
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get plans by type
     */
    @GetMapping("/plans/type/{planType}")
    @Operation(
        summary = "Get plans by type",
        description = "Get insurance plans by plan type (BASIC, STANDARD, PREMIUM, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plans retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid plan type"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsurancePlan>> getPlansByType(
            @Parameter(description = "Plan type") @PathVariable String planType) {
        logger.info("Getting plans by type: {}", planType);
        try {
            InsurancePlan.PlanType type = InsurancePlan.PlanType.valueOf(planType.toUpperCase());
            List<InsurancePlan> plans = travelInsuranceService.getPlansByType(type);
            return ResponseEntity.ok(plans);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get featured plans
     */
    @GetMapping("/plans/featured")
    @Operation(
        summary = "Get featured plans",
        description = "Get list of featured insurance plans"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured plans retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<InsurancePlan>> getFeaturedPlans() {
        logger.info("Getting featured insurance plans");
        List<InsurancePlan> plans = travelInsuranceService.getFeaturedPlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Calculate insurance quote
     */
    @PostMapping("/quote")
    @Operation(
        summary = "Calculate insurance quote",
        description = "Calculate premium and get detailed quote for travel insurance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quote calculated successfully",
                    content = @Content(schema = @Schema(implementation = InsuranceQuoteResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Plan not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<InsuranceQuoteResponse> calculateQuote(@Valid @RequestBody InsuranceQuoteRequest request) {
        logger.info("Calculating insurance quote for plan: {}", request.getPlanId());
        try {
            InsuranceQuoteResponse response = travelInsuranceService.calculateQuote(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error calculating quote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Purchase insurance
     */
    @PostMapping("/purchase")
    @Operation(
        summary = "Purchase insurance",
        description = "Purchase travel insurance policy"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Insurance purchased successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid purchase request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> purchaseInsurance(
            @Valid @RequestBody InsurancePurchaseRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        logger.info("Processing insurance purchase for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            TravelInsurance insurance = travelInsuranceService.purchaseInsurance(user, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Insurance policy created successfully");
            response.put("policyNumber", insurance.getPolicyNumber());
            response.put("totalAmount", insurance.getTotalAmount());
            response.put("coverageAmount", insurance.getCoverageAmount());
            response.put("coverageStartDate", insurance.getCoverageStartDate());
            response.put("coverageEndDate", insurance.getCoverageEndDate());
            response.put("paymentStatus", insurance.getPaymentStatus().getDisplayName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error purchasing insurance: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Get user's insurance policies
     */
    @GetMapping("/policies")
    @Operation(
        summary = "Get user policies",
        description = "Get all insurance policies for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policies retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TravelInsurance>> getUserPolicies(Authentication authentication) {
        String username = authentication.getName();
        logger.info("Getting insurance policies for user: {}", username);
        
        try {
            User user = (User) authentication.getPrincipal();
            List<TravelInsurance> policies = travelInsuranceService.getUserPolicies(user);
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            logger.error("Error getting user policies: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get insurance policy by policy number
     */
    @GetMapping("/policies/{policyNumber}")
    @Operation(
        summary = "Get policy details",
        description = "Get detailed information about a specific insurance policy"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TravelInsurance> getPolicyByNumber(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        logger.info("Getting insurance policy: {}", policyNumber);
        
        Optional<TravelInsurance> insurance = travelInsuranceService.getPolicyByNumber(policyNumber);
        return insurance.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Activate insurance policy (after payment)
     */
    @PostMapping("/policies/{policyNumber}/activate")
    @Operation(
        summary = "Activate policy",
        description = "Activate insurance policy after successful payment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy activated successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> activatePolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber) {
        logger.info("Activating insurance policy: {}", policyNumber);
        
        try {
            TravelInsurance insurance = travelInsuranceService.activatePolicy(policyNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Insurance policy activated successfully");
            response.put("policyNumber", insurance.getPolicyNumber());
            response.put("policyStatus", insurance.getPolicyStatus().getDisplayName());
            response.put("activationDate", insurance.getActivationDate());
            response.put("expiryDate", insurance.getExpiryDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error activating policy: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * Cancel insurance policy
     */
    @PostMapping("/policies/{policyNumber}/cancel")
    @Operation(
        summary = "Cancel policy",
        description = "Cancel insurance policy"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Policy cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Policy not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelPolicy(
            @Parameter(description = "Policy number") @PathVariable String policyNumber,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {
        logger.info("Cancelling insurance policy: {}", policyNumber);
        
        try {
            TravelInsurance insurance = travelInsuranceService.cancelPolicy(policyNumber, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Insurance policy cancelled successfully");
            response.put("policyNumber", insurance.getPolicyNumber());
            response.put("policyStatus", insurance.getPolicyStatus().getDisplayName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cancelling policy: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
