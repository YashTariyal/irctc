package com.irctc.analytics.controller;

import com.irctc.analytics.dto.AnalyticsResponse;
import com.irctc.analytics.service.RevenueAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for Revenue Analytics Dashboard
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Revenue Analytics", description = "Revenue Analytics Dashboard APIs")
public class AnalyticsController {
    
    @Autowired
    private RevenueAnalyticsService analyticsService;
    
    /**
     * Get revenue trends
     * GET /api/analytics/revenue?period={daily|weekly|monthly}
     */
    @GetMapping("/revenue")
    @Operation(summary = "Get revenue trends", description = "Get revenue trends for daily, weekly, or monthly periods")
    public ResponseEntity<AnalyticsResponse.RevenueTrends> getRevenueTrends(
            @Parameter(description = "Period: daily, weekly, or monthly", required = true)
            @RequestParam(defaultValue = "daily") String period,
            @Parameter(description = "Start date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // Validate period
        if (!period.equals("daily") && !period.equals("weekly") && !period.equals("monthly")) {
            period = "daily";
        }
        
        AnalyticsResponse.RevenueTrends trends = analyticsService.getRevenueTrends(period, startDate, endDate);
        return ResponseEntity.ok(trends);
    }
    
    /**
     * Get booking trends
     * GET /api/analytics/bookings/trends
     */
    @GetMapping("/bookings/trends")
    @Operation(summary = "Get booking trends", description = "Get booking trends including cancellation rates and refund analytics")
    public ResponseEntity<AnalyticsResponse.BookingTrends> getBookingTrends(
            @Parameter(description = "Start date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        AnalyticsResponse.BookingTrends trends = analyticsService.getBookingTrends(startDate, endDate);
        return ResponseEntity.ok(trends);
    }
    
    /**
     * Get route performance
     * GET /api/analytics/routes/performance
     */
    @GetMapping("/routes/performance")
    @Operation(summary = "Get route performance", description = "Get most profitable routes and popular routes")
    public ResponseEntity<AnalyticsResponse.RoutePerformance> getRoutePerformance(
            @Parameter(description = "Start date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        AnalyticsResponse.RoutePerformance performance = analyticsService.getRoutePerformance(startDate, endDate);
        return ResponseEntity.ok(performance);
    }
    
    /**
     * Get user segmentation
     * GET /api/analytics/users/segmentation
     */
    @GetMapping("/users/segmentation")
    @Operation(summary = "Get user segmentation", description = "Analyze user behavior by segments (VIP, Regular, New, Inactive)")
    public ResponseEntity<AnalyticsResponse.UserSegmentation> getUserSegmentation() {
        AnalyticsResponse.UserSegmentation segmentation = analyticsService.getUserSegmentation();
        return ResponseEntity.ok(segmentation);
    }
    
    /**
     * Get forecast
     * GET /api/analytics/forecast
     */
    @GetMapping("/forecast")
    @Operation(summary = "Get revenue and booking forecast", description = "Get revenue and booking predictions for future dates")
    public ResponseEntity<AnalyticsResponse.Forecast> getForecast(
            @Parameter(description = "Forecast type: revenue or bookings", required = true)
            @RequestParam(defaultValue = "revenue") String forecastType,
            @Parameter(description = "Number of days to forecast")
            @RequestParam(defaultValue = "30") int days) {
        
        // Validate forecast type
        if (!forecastType.equals("revenue") && !forecastType.equals("bookings")) {
            forecastType = "revenue";
        }
        
        // Validate days
        if (days < 1 || days > 365) {
            days = 30;
        }
        
        AnalyticsResponse.Forecast forecast = analyticsService.getForecast(forecastType, days);
        return ResponseEntity.ok(forecast);
    }
}

