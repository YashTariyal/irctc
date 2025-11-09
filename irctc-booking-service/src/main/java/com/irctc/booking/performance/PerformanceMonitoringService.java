package com.irctc.booking.performance;

import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance Monitoring Service
 * 
 * Comprehensive APM solution for monitoring:
 * - Memory usage and leak detection
 * - CPU usage
 * - Thread statistics
 * - Slow query detection
 * - Performance metrics
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Value("${performance.monitoring.enabled:true}")
    private boolean enabled;
    
    @Value("${performance.monitoring.slow-query-threshold:1000}")
    private long slowQueryThresholdMs;
    
    @Value("${performance.monitoring.memory-leak-threshold:80}")
    private double memoryLeakThresholdPercent;
    
    @Value("${performance.monitoring.cpu-threshold:80}")
    private double cpuThresholdPercent;
    
    // Memory tracking
    private final ConcurrentMap<String, MemorySnapshot> memoryHistory = new ConcurrentHashMap<>();
    private final AtomicLong memoryLeakCount = new AtomicLong(0);
    
    // Slow query tracking
    private final ConcurrentMap<String, SlowQueryInfo> slowQueries = new ConcurrentHashMap<>();
    private final AtomicLong slowQueryCount = new AtomicLong(0);
    
    // Performance metrics
    private Gauge memoryUsageGauge;
    private Gauge memoryUsedGauge;
    private Gauge memoryMaxGauge;
    private Gauge threadCountGauge;
    private Counter memoryLeakDetectedCounter;
    private Counter slowQueryDetectedCounter;
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    @PostConstruct
    public void init() {
        if (!enabled) {
            logger.info("Performance monitoring disabled");
            return;
        }
        
        // Register memory metrics
        memoryUsageGauge = Gauge.builder("jvm.memory.usage.percent", this, 
            service -> service.getMemoryUsagePercent())
            .description("JVM memory usage percentage")
            .register(meterRegistry);
        
        memoryUsedGauge = Gauge.builder("jvm.memory.used", this,
            service -> service.getMemoryUsed())
            .description("JVM memory used in bytes")
            .register(meterRegistry);
        
        memoryMaxGauge = Gauge.builder("jvm.memory.max", this,
            service -> service.getMemoryMax())
            .description("JVM memory max in bytes")
            .register(meterRegistry);
        
        threadCountGauge = Gauge.builder("jvm.threads.count", this,
            service -> service.getThreadCount())
            .description("JVM thread count")
            .register(meterRegistry);
        
        memoryLeakDetectedCounter = Counter.builder("performance.memory.leak.detected")
            .description("Number of memory leak detections")
            .register(meterRegistry);
        
        slowQueryDetectedCounter = Counter.builder("performance.slow.query.detected")
            .description("Number of slow query detections")
            .register(meterRegistry);
        
        logger.info("✅ Performance Monitoring Service initialized");
    }
    
    /**
     * Record a slow query
     */
    public void recordSlowQuery(String query, long durationMs, String operation) {
        if (!enabled) {
            return;
        }
        
        if (durationMs >= slowQueryThresholdMs) {
            slowQueryCount.incrementAndGet();
            slowQueryDetectedCounter.increment();
            
            SlowQueryInfo info = new SlowQueryInfo();
            info.setQuery(query);
            info.setDurationMs(durationMs);
            info.setOperation(operation);
            info.setTimestamp(LocalDateTime.now());
            
            String key = operation + "_" + System.currentTimeMillis();
            slowQueries.put(key, info);
            
            // Keep only last 100 slow queries
            if (slowQueries.size() > 100) {
                String oldestKey = slowQueries.keySet().iterator().next();
                slowQueries.remove(oldestKey);
            }
            
            logger.warn("⚠️  Slow query detected: {} - Duration: {}ms - Operation: {}", 
                truncateQuery(query), durationMs, operation);
        }
    }
    
    /**
     * Get memory usage percentage
     */
    public double getMemoryUsagePercent() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        
        if (max == -1) {
            return 0.0; // Max not set
        }
        
        return (double) used / max * 100.0;
    }
    
    /**
     * Get memory used in bytes
     */
    public long getMemoryUsed() {
        return memoryBean.getHeapMemoryUsage().getUsed();
    }
    
    /**
     * Get memory max in bytes
     */
    public long getMemoryMax() {
        return memoryBean.getHeapMemoryUsage().getMax();
    }
    
    /**
     * Get thread count
     */
    public int getThreadCount() {
        return threadBean.getThreadCount();
    }
    
    /**
     * Check for memory leaks (scheduled task)
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkMemoryLeaks() {
        if (!enabled) {
            return;
        }
        
        double usagePercent = getMemoryUsagePercent();
        String timestamp = LocalDateTime.now().toString();
        
        MemorySnapshot snapshot = new MemorySnapshot();
        snapshot.setTimestamp(LocalDateTime.now());
        snapshot.setUsagePercent(usagePercent);
        snapshot.setUsedBytes(getMemoryUsed());
        snapshot.setMaxBytes(getMemoryMax());
        
        memoryHistory.put(timestamp, snapshot);
        
        // Keep only last 10 snapshots
        if (memoryHistory.size() > 10) {
            String oldestKey = memoryHistory.keySet().iterator().next();
            memoryHistory.remove(oldestKey);
        }
        
        // Check for memory leak (increasing trend)
        if (memoryHistory.size() >= 5) {
            List<MemorySnapshot> snapshots = new ArrayList<>(memoryHistory.values());
            boolean isLeaking = detectMemoryLeak(snapshots);
            
            if (isLeaking && usagePercent > memoryLeakThresholdPercent) {
                memoryLeakCount.incrementAndGet();
                memoryLeakDetectedCounter.increment();
                
                logger.error("🚨 Memory leak detected! Usage: {}% (Threshold: {}%)", 
                    String.format("%.2f", usagePercent), memoryLeakThresholdPercent);
            }
        }
        
        // Log warning if memory usage is high
        if (usagePercent > memoryLeakThresholdPercent) {
            logger.warn("⚠️  High memory usage: {}%", String.format("%.2f", usagePercent));
        }
    }
    
    /**
     * Detect memory leak by analyzing trend
     */
    private boolean detectMemoryLeak(List<MemorySnapshot> snapshots) {
        if (snapshots.size() < 5) {
            return false;
        }
        
        // Check if memory usage is consistently increasing
        int increasingCount = 0;
        for (int i = 1; i < snapshots.size(); i++) {
            if (snapshots.get(i).getUsagePercent() > snapshots.get(i - 1).getUsagePercent()) {
                increasingCount++;
            }
        }
        
        // If 80% of snapshots show increasing trend, potential leak
        return (double) increasingCount / (snapshots.size() - 1) >= 0.8;
    }
    
    /**
     * Get performance summary
     */
    public PerformanceSummary getPerformanceSummary() {
        PerformanceSummary summary = new PerformanceSummary();
        summary.setMemoryUsagePercent(getMemoryUsagePercent());
        summary.setMemoryUsedBytes(getMemoryUsed());
        summary.setMemoryMaxBytes(getMemoryMax());
        summary.setThreadCount(getThreadCount());
        summary.setSlowQueryCount(slowQueryCount.get());
        summary.setMemoryLeakCount(memoryLeakCount.get());
        summary.setSlowQueries(new ArrayList<>(slowQueries.values()));
        summary.setTimestamp(LocalDateTime.now());
        return summary;
    }
    
    /**
     * Get slow queries
     */
    public List<SlowQueryInfo> getSlowQueries() {
        return new ArrayList<>(slowQueries.values());
    }
    
    /**
     * Clear slow queries
     */
    public void clearSlowQueries() {
        slowQueries.clear();
        slowQueryCount.set(0);
    }
    
    private String truncateQuery(String query) {
        if (query == null) {
            return "null";
        }
        if (query.length() > 100) {
            return query.substring(0, 100) + "...";
        }
        return query;
    }
    
    // Inner classes
    public static class MemorySnapshot {
        private LocalDateTime timestamp;
        private double usagePercent;
        private long usedBytes;
        private long maxBytes;
        
        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(double usagePercent) { this.usagePercent = usagePercent; }
        public long getUsedBytes() { return usedBytes; }
        public void setUsedBytes(long usedBytes) { this.usedBytes = usedBytes; }
        public long getMaxBytes() { return maxBytes; }
        public void setMaxBytes(long maxBytes) { this.maxBytes = maxBytes; }
    }
    
    public static class SlowQueryInfo {
        private String query;
        private long durationMs;
        private String operation;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class PerformanceSummary {
        private double memoryUsagePercent;
        private long memoryUsedBytes;
        private long memoryMaxBytes;
        private int threadCount;
        private long slowQueryCount;
        private long memoryLeakCount;
        private List<SlowQueryInfo> slowQueries;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public double getMemoryUsagePercent() { return memoryUsagePercent; }
        public void setMemoryUsagePercent(double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }
        public long getMemoryUsedBytes() { return memoryUsedBytes; }
        public void setMemoryUsedBytes(long memoryUsedBytes) { this.memoryUsedBytes = memoryUsedBytes; }
        public long getMemoryMaxBytes() { return memoryMaxBytes; }
        public void setMemoryMaxBytes(long memoryMaxBytes) { this.memoryMaxBytes = memoryMaxBytes; }
        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
        public long getSlowQueryCount() { return slowQueryCount; }
        public void setSlowQueryCount(long slowQueryCount) { this.slowQueryCount = slowQueryCount; }
        public long getMemoryLeakCount() { return memoryLeakCount; }
        public void setMemoryLeakCount(long memoryLeakCount) { this.memoryLeakCount = memoryLeakCount; }
        public List<SlowQueryInfo> getSlowQueries() { return slowQueries; }
        public void setSlowQueries(List<SlowQueryInfo> slowQueries) { this.slowQueries = slowQueries; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}

