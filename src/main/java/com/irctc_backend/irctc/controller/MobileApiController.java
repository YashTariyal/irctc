package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.MobileBookingResponse;
import com.irctc_backend.irctc.dto.MobileTrainResponse;
import com.irctc_backend.irctc.dto.PagedResponse;
import com.irctc_backend.irctc.entity.Booking;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.entity.User;
import com.irctc_backend.irctc.repository.BookingRepository;
import com.irctc_backend.irctc.repository.TrainRepository;
import com.irctc_backend.irctc.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mobile-optimized API controller with pagination
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/mobile")
@Tag(name = "Mobile API", description = "Mobile-optimized API endpoints with pagination")
public class MobileApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileApiController.class);
    
    @Autowired
    private TrainRepository trainRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get trains with pagination and mobile optimization
     */
    @GetMapping("/trains")
    @Operation(
        summary = "Get trains (mobile optimized)",
        description = "Get paginated list of trains optimized for mobile devices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trains retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<MobileTrainResponse>> getTrains(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "trainNumber") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Filter by train type") @RequestParam(required = false) String trainType,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Search by train number or name") @RequestParam(required = false) String search) {
        
        logger.info("Getting trains for mobile - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                   page, size, sortBy, sortDirection);
        
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get trains with pagination
            Page<Train> trainPage = trainRepository.findAll(pageable);
            
            // Convert to mobile response
            List<MobileTrainResponse> mobileTrains = trainPage.getContent().stream()
                .map(this::convertToMobileTrainResponse)
                .collect(Collectors.toList());
            
            // Create paged response
            PagedResponse<MobileTrainResponse> response = new PagedResponse<>(
                mobileTrains,
                trainPage.getNumber(),
                trainPage.getSize(),
                trainPage.getTotalElements(),
                trainPage.getTotalPages()
            );
            response.setSortBy(sortBy);
            response.setSortDirection(sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting trains for mobile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    /**
     * Search trains with mobile optimization
     */
    @GetMapping("/trains/search")
    @Operation(
        summary = "Search trains (mobile optimized)",
        description = "Search trains with pagination optimized for mobile devices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<MobileTrainResponse>> searchTrains(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "trainNumber") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {
        
        logger.info("Searching trains for mobile - query: {}, page: {}, size: {}", query, page, size);
        
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Search trains (simplified search - in real implementation, you'd use a more sophisticated search)
            Page<Train> trainPage = trainRepository.findByTrainNumberContainingIgnoreCaseOrTrainNameContainingIgnoreCase(
                query, query, pageable);
            
            // Convert to mobile response
            List<MobileTrainResponse> mobileTrains = trainPage.getContent().stream()
                .map(this::convertToMobileTrainResponse)
                .collect(Collectors.toList());
            
            // Create paged response
            PagedResponse<MobileTrainResponse> response = new PagedResponse<>(
                mobileTrains,
                trainPage.getNumber(),
                trainPage.getSize(),
                trainPage.getTotalElements(),
                trainPage.getTotalPages()
            );
            response.setSortBy(sortBy);
            response.setSortDirection(sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error searching trains for mobile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    /**
     * Get user's bookings with pagination and mobile optimization
     */
    @GetMapping("/bookings")
    @Operation(
        summary = "Get user bookings (mobile optimized)",
        description = "Get paginated list of user's bookings optimized for mobile devices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    public ResponseEntity<PagedResponse<MobileBookingResponse>> getUserBookings(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "bookingDate") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by journey date from") @RequestParam(required = false) String fromDate,
            @Parameter(description = "Filter by journey date to") @RequestParam(required = false) String toDate) {
        
        logger.info("Getting user bookings for mobile - page: {}, size: {}", page, size);
        
        try {
            Long userId = getCurrentUserId();
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get user's bookings with pagination
            Page<Booking> bookingPage = bookingRepository.findByUser(user, pageable);
            
            // Convert to mobile response
            List<MobileBookingResponse> mobileBookings = bookingPage.getContent().stream()
                .map(this::convertToMobileBookingResponse)
                .collect(Collectors.toList());
            
            // Create paged response
            PagedResponse<MobileBookingResponse> response = new PagedResponse<>(
                mobileBookings,
                bookingPage.getNumber(),
                bookingPage.getSize(),
                bookingPage.getTotalElements(),
                bookingPage.getTotalPages()
            );
            response.setSortBy(sortBy);
            response.setSortDirection(sortDirection);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting user bookings for mobile: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
    
    /**
     * Get booking by PNR with mobile optimization
     */
    @GetMapping("/bookings/pnr/{pnrNumber}")
    @Operation(
        summary = "Get booking by PNR (mobile optimized)",
        description = "Get booking details by PNR number optimized for mobile devices"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MobileBookingResponse.class))),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<MobileBookingResponse> getBookingByPnr(
            @Parameter(description = "PNR number") @PathVariable String pnrNumber) {
        
        logger.info("Getting booking by PNR for mobile - PNR: {}", pnrNumber);
        
        try {
            Booking booking = bookingRepository.findByPnrNumber(pnrNumber)
                .orElseThrow(() -> new RuntimeException("Booking not found with PNR: " + pnrNumber));
            
            MobileBookingResponse response = convertToMobileBookingResponse(booking);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting booking by PNR for mobile: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get mobile app configuration
     */
    @GetMapping("/config")
    @Operation(
        summary = "Get mobile app configuration",
        description = "Get configuration settings for mobile app"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getMobileConfig() {
        logger.info("Getting mobile app configuration");
        
        Map<String, Object> config = Map.of(
            "appVersion", "1.0.0",
            "minSupportedVersion", "1.0.0",
            "maxPageSize", 50,
            "defaultPageSize", 10,
            "supportedSortFields", new String[]{"trainNumber", "trainName", "departureTime", "arrivalTime", "journeyDuration"},
            "supportedSortDirections", new String[]{"asc", "desc"},
            "features", Map.of(
                "seatSelection", true,
                "waitlistRac", true,
                "paymentGateway", true,
                "notifications", true,
                "offlineMode", false
            ),
            "apiEndpoints", Map.of(
                "trains", "/api/mobile/trains",
                "search", "/api/mobile/trains/search",
                "bookings", "/api/mobile/bookings",
                "pnr", "/api/mobile/bookings/pnr/{pnrNumber}"
            )
        );
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Health check for mobile API
     */
    @GetMapping("/health")
    @Operation(
        summary = "Mobile API health check",
        description = "Check if the mobile API is running"
    )
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Mobile API Service",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    /**
     * Convert Train entity to MobileTrainResponse
     */
    private MobileTrainResponse convertToMobileTrainResponse(Train train) {
        MobileTrainResponse response = new MobileTrainResponse(train);
        
        // Add mobile-specific optimizations
        response.setAvailableSeats(calculateAvailableSeats(train));
        response.setStartingFare(calculateStartingFare(train));
        response.setRoute(buildRouteString(train));
        response.setAmenities(buildAmenitiesString(train));
        response.setIsTatkalAvailable(true); // Simplified
        response.setIsPremiumTatkalAvailable(true); // Simplified
        
        return response;
    }
    
    /**
     * Convert Booking entity to MobileBookingResponse
     */
    private MobileBookingResponse convertToMobileBookingResponse(Booking booking) {
        MobileBookingResponse response = new MobileBookingResponse(booking);
        
        // Add mobile-specific optimizations
        response.setBookingClass(booking.getCoach() != null ? booking.getCoach().getCoachType().name() : null);
        response.setJourneyType("One Way"); // Simplified
        response.setBoardingStation(booking.getTrain() != null && booking.getTrain().getSourceStation() != null ? 
            booking.getTrain().getSourceStation().getStationName() : null);
        response.setReservationUpto(booking.getTrain() != null && booking.getTrain().getDestinationStation() != null ? 
            booking.getTrain().getDestinationStation().getStationName() : null);
        
        return response;
    }
    
    /**
     * Calculate available seats for a train
     */
    private Integer calculateAvailableSeats(Train train) {
        // Simplified calculation - in real implementation, you'd calculate based on actual bookings
        return 100; // Placeholder
    }
    
    /**
     * Calculate starting fare for a train
     */
    private Double calculateStartingFare(Train train) {
        // Simplified calculation - in real implementation, you'd calculate based on coach types
        return 500.0; // Placeholder
    }
    
    /**
     * Build route string for a train
     */
    private String buildRouteString(Train train) {
        if (train.getSourceStation() != null && train.getDestinationStation() != null) {
            return train.getSourceStation().getStationName() + " → " + train.getDestinationStation().getStationName();
        }
        return "Route not available";
    }
    
    /**
     * Build amenities string for a train
     */
    private String buildAmenitiesString(Train train) {
        // Simplified amenities based on train type
        switch (train.getTrainType()) {
            case RAJDHANI:
                return "AC, Bedding, Meals, Charging Point";
            case SHATABDI:
                return "AC, Meals, Charging Point";
            case EXPRESS:
                return "Fan, Charging Point";
            default:
                return "Basic amenities";
        }
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
