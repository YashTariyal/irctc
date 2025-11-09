package com.irctc.booking.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance Monitoring Controller
 * 
 * REST API for accessing performance metrics and monitoring data
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceMonitoringController {

    @Autowired(required = false)
    private PerformanceMonitoringService performanceMonitoringService;
    
    /**
     * Get performance summary
     */
    @GetMapping("/summary")
    public ResponseEntity<PerformanceMonitoringService.PerformanceSummary> getPerformanceSummary() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(performanceMonitoringService.getPerformanceSummary());
    }
    
    /**
     * Get memory statistics
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryStats() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("usagePercent", performanceMonitoringService.getMemoryUsagePercent());
        stats.put("usedBytes", performanceMonitoringService.getMemoryUsed());
        stats.put("maxBytes", performanceMonitoringService.getMemoryMax());
        stats.put("usedMB", performanceMonitoringService.getMemoryUsed() / (1024 * 1024));
        stats.put("maxMB", performanceMonitoringService.getMemoryMax() / (1024 * 1024));
        stats.put("freeMB", (performanceMonitoringService.getMemoryMax() - performanceMonitoringService.getMemoryUsed()) / (1024 * 1024));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get thread statistics
     */
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> getThreadStats() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("threadCount", performanceMonitoringService.getThreadCount());
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get slow queries
     */
    @GetMapping("/slow-queries")
    public ResponseEntity<List<PerformanceMonitoringService.SlowQueryInfo>> getSlowQueries() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(performanceMonitoringService.getSlowQueries());
    }
    
    /**
     * Clear slow queries
     */
    @DeleteMapping("/slow-queries")
    public ResponseEntity<Map<String, String>> clearSlowQueries() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        performanceMonitoringService.clearSlowQueries();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Slow queries cleared");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        if (performanceMonitoringService == null) {
            return ResponseEntity.notFound().build();
        }
        
        PerformanceMonitoringService.PerformanceSummary summary = 
            performanceMonitoringService.getPerformanceSummary();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("memory", Map.of(
            "usagePercent", summary.getMemoryUsagePercent(),
            "usedBytes", summary.getMemoryUsedBytes(),
            "maxBytes", summary.getMemoryMaxBytes(),
            "usedMB", summary.getMemoryUsedBytes() / (1024 * 1024),
            "maxMB", summary.getMemoryMaxBytes() / (1024 * 1024)
        ));
        metrics.put("threads", Map.of(
            "count", summary.getThreadCount()
        ));
        metrics.put("slowQueries", Map.of(
            "count", summary.getSlowQueryCount(),
            "recent", summary.getSlowQueries().size()
        ));
        metrics.put("memoryLeaks", Map.of(
            "count", summary.getMemoryLeakCount()
        ));
        metrics.put("timestamp", summary.getTimestamp());
        
        return ResponseEntity.ok(metrics);
    }
}

