package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API Controller for dashboard data endpoints.
 * Provides API endpoints for dashboard data without view resolution conflicts.
 */
@RestController
@RequestMapping("/dashboard/api")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard API", description = "Dashboard data API endpoints")
@Hidden
public class DashboardApiController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * REST API endpoint for dashboard statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get Dashboard Stats", description = "Returns dashboard statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * REST API endpoint for recent activities.
     */
    @GetMapping("/activities")
    @Operation(summary = "Get Recent Activities", description = "Returns recent API activities")
    public ResponseEntity<Object> getActivities() {
        Object activities = dashboardService.getRecentActivities();
        return ResponseEntity.ok(activities);
    }
    
    /**
     * REST API endpoint for performance alerts.
     */
    @GetMapping("/alerts")
    @Operation(summary = "Get Performance Alerts", description = "Returns performance alerts")
    public ResponseEntity<Object> getAlerts() {
        Object alerts = dashboardService.getPerformanceAlerts();
        return ResponseEntity.ok(alerts);
    }
    
    /**
     * REST API endpoint for chart data.
     */
    @GetMapping("/chart-data")
    @Operation(summary = "Get Chart Data", description = "Returns data for dashboard charts")
    public ResponseEntity<Object> getChartData(
            @RequestParam(value = "timeRange", defaultValue = "15") int timeRange) {
        Object chartData = dashboardService.getChartData(timeRange);
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * REST API endpoint for API performance details.
     */
    @GetMapping("/performance")
    @Operation(summary = "Get API Performance", description = "Returns detailed API performance data")
    public ResponseEntity<Object> getApiPerformance() {
        Object performance = dashboardService.getApiPerformance();
        return ResponseEntity.ok(performance);
    }
    
    /**
     * REST API endpoint for top performing APIs.
     */
    @GetMapping("/top-apis")
    @Operation(summary = "Get Top APIs", description = "Returns top performing APIs")
    public ResponseEntity<Object> getTopApis() {
        Object topApis = dashboardService.getTopApis();
        return ResponseEntity.ok(topApis);
    }
}
