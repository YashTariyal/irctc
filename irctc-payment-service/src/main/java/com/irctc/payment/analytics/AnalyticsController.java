package com.irctc.payment.analytics;

import com.irctc.payment.analytics.dto.AnalyticsResponse;
import com.irctc.payment.analytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for payment analytics and dashboard
 */
@RestController
@RequestMapping("/api/payments/analytics")
public class AnalyticsController {
    
    @Autowired(required = false)
    private AnalyticsService analyticsService;
    
    /**
     * Get overall payment statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponse.Overview> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(new AnalyticsResponse.Overview());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getOverview(startDate, endDate));
    }
    
    /**
     * Get daily payment statistics
     */
    @GetMapping("/daily")
    public ResponseEntity<List<AnalyticsResponse.DailyStats>> getDailyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getDailyStats(startDate, endDate));
    }
    
    /**
     * Get weekly payment statistics
     */
    @GetMapping("/weekly")
    public ResponseEntity<List<AnalyticsResponse.WeeklyStats>> getWeeklyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusWeeks(12);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getWeeklyStats(startDate, endDate));
    }
    
    /**
     * Get monthly payment statistics
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<AnalyticsResponse.MonthlyStats>> getMonthlyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(12);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getMonthlyStats(startDate, endDate));
    }
    
    /**
     * Get gateway performance comparison
     */
    @GetMapping("/gateway-performance")
    public ResponseEntity<List<AnalyticsResponse.GatewayPerformance>> getGatewayPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getGatewayPerformance(startDate, endDate));
    }
    
    /**
     * Get payment method distribution
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<List<AnalyticsResponse.PaymentMethodStats>> getPaymentMethodStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (analyticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        return ResponseEntity.ok(analyticsService.getPaymentMethodStats(startDate, endDate));
    }
}

