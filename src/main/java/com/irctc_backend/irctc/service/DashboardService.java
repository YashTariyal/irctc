package com.irctc_backend.irctc.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for providing dashboard data and statistics.
 * This service aggregates AOP timing data and provides it to the dashboard frontend.
 */
@Service
public class DashboardService {
    
    // In-memory storage for demo purposes
    // In production, this would be replaced with database queries
    private final Map<String, Object> dashboardData = new ConcurrentHashMap<>();
    private final List<Map<String, Object>> activities = new ArrayList<>();
    private final List<Map<String, Object>> alerts = new ArrayList<>();
    
    public DashboardService() {
        initializeDemoData();
    }
    
    /**
     * Get dashboard statistics.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", 1250);
        stats.put("activeApis", 8);
        stats.put("avgResponseTime", 245);
        stats.put("successCount", 1180);
        stats.put("errorCount", 45);
        stats.put("slowCount", 25);
        stats.put("uptime", "2h 15m");
        stats.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        return stats;
    }
    
    /**
     * Get recent activities.
     */
    public List<Map<String, Object>> getRecentActivities() {
        synchronized (activities) {
            return new ArrayList<>(activities);
        }
    }
    
    /**
     * Get performance alerts.
     */
    public List<Map<String, Object>> getPerformanceAlerts() {
        synchronized (alerts) {
            return new ArrayList<>(alerts);
        }
    }
    
    /**
     * Get chart data for the specified time range.
     */
    public Map<String, Object> getChartData(int timeRange) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Response time data
        List<Map<String, Object>> responseTimeData = generateResponseTimeData(timeRange);
        chartData.put("responseTimeData", responseTimeData);
        
        // Request volume data
        List<Map<String, Object>> requestVolumeData = generateRequestVolumeData();
        chartData.put("requestVolumeData", requestVolumeData);
        
        // API performance data
        List<Map<String, Object>> apiPerformanceData = generateApiPerformanceData();
        chartData.put("apiPerformanceData", apiPerformanceData);
        
        return chartData;
    }
    
    /**
     * Get API performance details.
     */
    public List<Map<String, Object>> getApiPerformance() {
        return Arrays.asList(
            createApiPerformanceEntry("GET /api/bookings/{id}", "GET", 156, 45, 12, 890, 98.5, "success"),
            createApiPerformanceEntry("POST /api/bookings", "POST", 89, 234, 156, 445, 95.2, "success"),
            createApiPerformanceEntry("GET /api/trains", "GET", 234, 123, 67, 567, 99.1, "success"),
            createApiPerformanceEntry("PUT /api/bookings/{id}", "PUT", 67, 189, 134, 234, 97.8, "success"),
            createApiPerformanceEntry("DELETE /api/bookings/{id}", "DELETE", 45, 145, 89, 123, 96.3, "success"),
            createApiPerformanceEntry("GET /api/users/{id}", "GET", 123, 78, 23, 345, 99.5, "success"),
            createApiPerformanceEntry("POST /api/users", "POST", 78, 167, 123, 234, 94.7, "warning"),
            createApiPerformanceEntry("GET /api/stations", "GET", 345, 56, 12, 678, 99.8, "success")
        );
    }
    
    /**
     * Get top performing APIs.
     */
    public List<Map<String, Object>> getTopApis() {
        return Arrays.asList(
            createTopApiEntry("GET /api/stations", 56),
            createTopApiEntry("GET /api/trains", 123),
            createTopApiEntry("GET /api/bookings/{id}", 145),
            createTopApiEntry("POST /api/bookings", 167),
            createTopApiEntry("GET /api/users/{id}", 178)
        );
    }
    
    /**
     * Add a new activity to the dashboard.
     */
    public void addActivity(String type, String title, String details) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("type", type);
        activity.put("title", title);
        activity.put("details", details);
        activity.put("timestamp", LocalDateTime.now());
        
        synchronized (activities) {
            activities.add(0, activity);
            // Keep only last 100 activities
            if (activities.size() > 100) {
                activities.remove(activities.size() - 1);
            }
        }
    }
    
    /**
     * Add a new alert to the dashboard.
     */
    public void addAlert(String type, String title, String details) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("type", type);
        alert.put("title", title);
        alert.put("details", details);
        alert.put("timestamp", LocalDateTime.now());
        
        synchronized (alerts) {
            alerts.add(0, alert);
            // Keep only last 50 alerts
            if (alerts.size() > 50) {
                alerts.remove(alerts.size() - 1);
            }
        }
    }
    
    // Private helper methods
    
    private void initializeDemoData() {
        // Add some demo activities
        addActivity("api_request", "GET /api/bookings/123", "BookingController.getBookingById");
        addActivity("api_response", "GET /api/bookings/123", "Response time: 45ms");
        addActivity("method_start", "Create Train Booking", "BookingService.createBooking");
        addActivity("method_complete", "Create Train Booking", "Execution time: 234ms");
        addActivity("slow_operation", "POST /api/bookings", "Slow API detected: 2500ms");
        
        // Add some demo alerts
        addAlert("warning", "Slow API Response", "POST /api/bookings took 2500ms");
        addAlert("warning", "High Memory Usage", "Memory usage at 85%");
        addAlert("error", "Database Connection Error", "Failed to connect to PostgreSQL");
    }
    
    private List<Map<String, Object>> generateResponseTimeData(int timeRange) {
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = timeRange; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            LocalDateTime time = now.minusMinutes(i);
            point.put("time", time.format(DateTimeFormatter.ofPattern("HH:mm")));
            point.put("avgTime", 200 + (int)(Math.random() * 100));
            point.put("maxTime", 400 + (int)(Math.random() * 200));
            data.add(point);
        }
        
        return data;
    }
    
    private List<Map<String, Object>> generateRequestVolumeData() {
        return Arrays.asList(
            createRequestVolumeEntry("GET /api/bookings/{id}", 156),
            createRequestVolumeEntry("POST /api/bookings", 89),
            createRequestVolumeEntry("GET /api/trains", 234),
            createRequestVolumeEntry("PUT /api/bookings/{id}", 67),
            createRequestVolumeEntry("DELETE /api/bookings/{id}", 45)
        );
    }
    
    private List<Map<String, Object>> generateApiPerformanceData() {
        return Arrays.asList(
            createApiPerformanceEntry("GET /api/stations", 56),
            createApiPerformanceEntry("GET /api/trains", 123),
            createApiPerformanceEntry("GET /api/bookings/{id}", 145),
            createApiPerformanceEntry("POST /api/bookings", 167),
            createApiPerformanceEntry("GET /api/users/{id}", 178)
        );
    }
    
    private Map<String, Object> createApiPerformanceEntry(String endpoint, String method, 
                                                         int totalRequests, int avgTime, 
                                                         int minTime, int maxTime, 
                                                         double successRate, String status) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("endpoint", endpoint);
        entry.put("method", method);
        entry.put("totalRequests", totalRequests);
        entry.put("avgTime", avgTime);
        entry.put("minTime", minTime);
        entry.put("maxTime", maxTime);
        entry.put("successRate", successRate);
        entry.put("status", status);
        return entry;
    }
    
    private Map<String, Object> createTopApiEntry(String endpoint, int avgTime) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("endpoint", endpoint);
        entry.put("avgTime", avgTime);
        return entry;
    }
    
    private Map<String, Object> createRequestVolumeEntry(String endpoint, int count) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("endpoint", endpoint);
        entry.put("count", count);
        return entry;
    }
    
    private Map<String, Object> createApiPerformanceEntry(String endpoint, int avgTime) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("endpoint", endpoint);
        entry.put("avgTime", avgTime);
        return entry;
    }
}
