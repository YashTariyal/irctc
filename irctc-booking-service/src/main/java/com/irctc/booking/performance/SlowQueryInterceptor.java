package com.irctc.booking.performance;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Slow Query Interceptor
 * 
 * Intercepts Hibernate/JPA queries and detects slow queries
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
@ConditionalOnProperty(name = "performance.monitoring.enabled", havingValue = "true", matchIfMissing = true)
public class SlowQueryInterceptor implements StatementInspector {

    private static final Logger logger = LoggerFactory.getLogger(SlowQueryInterceptor.class);
    
    @Autowired(required = false)
    private PerformanceMonitoringService performanceMonitoringService;
    
    private final ConcurrentMap<String, QueryExecution> activeQueries = new ConcurrentHashMap<>();
    private final AtomicLong totalQueries = new AtomicLong(0);
    private final AtomicLong slowQueries = new AtomicLong(0);
    
    @Override
    public String inspect(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        String queryId = generateQueryId();
        long startTime = System.currentTimeMillis();
        
        QueryExecution execution = new QueryExecution();
        execution.setQuery(sql);
        execution.setStartTime(startTime);
        execution.setQueryId(queryId);
        
        activeQueries.put(queryId, execution);
        totalQueries.incrementAndGet();
        
        // Log query start (only for non-select queries or in debug mode)
        if (logger.isDebugEnabled() || !sql.trim().toUpperCase().startsWith("SELECT")) {
            logger.debug("🔍 Query started [{}]: {}", queryId, truncateQuery(sql));
        }
        
        return sql;
    }
    
    /**
     * Called when query execution completes
     */
    public void onQueryComplete(String queryId, long durationMs) {
        QueryExecution execution = activeQueries.remove(queryId);
        if (execution == null) {
            return;
        }
        
        execution.setDurationMs(durationMs);
        execution.setCompleted(true);
        
        // Check if query is slow (threshold: 1000ms)
        if (performanceMonitoringService != null) {
            long threshold = 1000; // Default threshold, can be made configurable
            if (durationMs >= threshold) {
                slowQueries.incrementAndGet();
                performanceMonitoringService.recordSlowQuery(
                    execution.getQuery(),
                    durationMs,
                    "database"
                );
                
                logger.warn("⚠️  Slow query detected [{}]: {}ms - {}", 
                    queryId, durationMs, truncateQuery(execution.getQuery()));
            }
        }
        
        // Log query completion
        if (logger.isDebugEnabled() || durationMs > 500) {
            logger.debug("✅ Query completed [{}]: {}ms", queryId, durationMs);
        }
    }
    
    private String generateQueryId() {
        return "Q" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }
    
    private String truncateQuery(String query) {
        if (query == null) {
            return "null";
        }
        if (query.length() > 200) {
            return query.substring(0, 200) + "...";
        }
        return query;
    }
    
    public long getTotalQueries() {
        return totalQueries.get();
    }
    
    public long getSlowQueries() {
        return slowQueries.get();
    }
    
    private static class QueryExecution {
        private String queryId;
        private String query;
        private long startTime;
        private long durationMs;
        private boolean completed;
        
        // Getters and setters
        public String getQueryId() { return queryId; }
        public void setQueryId(String queryId) { this.queryId = queryId; }
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}

