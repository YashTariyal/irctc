package com.irctc.booking.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Performance Profiler
 * 
 * Provides method-level performance profiling
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class PerformanceProfiler {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceProfiler.class);
    
    private final ConcurrentMap<String, ProfilerContext> activeProfiles = new ConcurrentHashMap<>();
    
    /**
     * Start profiling a method
     */
    public ProfilerContext start(String methodName) {
        ProfilerContext context = new ProfilerContext();
        context.setMethodName(methodName);
        context.setStartTime(System.currentTimeMillis());
        context.setStartMemory(getUsedMemory());
        
        activeProfiles.put(methodName, context);
        return context;
    }
    
    /**
     * Stop profiling and return results
     */
    public ProfilerResult stop(ProfilerContext context) {
        if (context == null) {
            return null;
        }
        
        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();
        
        ProfilerResult result = new ProfilerResult();
        result.setMethodName(context.getMethodName());
        result.setDurationMs(endTime - context.getStartTime());
        result.setMemoryUsedBytes(endMemory - context.getStartMemory());
        result.setStartTime(context.getStartTime());
        result.setEndTime(endTime);
        
        activeProfiles.remove(context.getMethodName());
        
        return result;
    }
    
    /**
     * Profile a method execution
     */
    public <T> T profile(String methodName, ProfiledOperation<T> operation) throws Exception {
        ProfilerContext context = start(methodName);
        try {
            T result = operation.execute();
            ProfilerResult profilerResult = stop(context);
            
            if (profilerResult != null) {
                logger.debug("📊 Profiled {}: {}ms, {} bytes", 
                    methodName, profilerResult.getDurationMs(), profilerResult.getMemoryUsedBytes());
            }
            
            return result;
        } catch (Exception e) {
            stop(context);
            throw e;
        }
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    @FunctionalInterface
    public interface ProfiledOperation<T> {
        T execute() throws Exception;
    }
    
    public static class ProfilerContext {
        private String methodName;
        private long startTime;
        private long startMemory;
        
        // Getters and setters
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getStartMemory() { return startMemory; }
        public void setStartMemory(long startMemory) { this.startMemory = startMemory; }
    }
    
    public static class ProfilerResult {
        private String methodName;
        private long durationMs;
        private long memoryUsedBytes;
        private long startTime;
        private long endTime;
        
        // Getters and setters
        public String getMethodName() { return methodName; }
        public void setMethodName(String methodName) { this.methodName = methodName; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public long getMemoryUsedBytes() { return memoryUsedBytes; }
        public void setMemoryUsedBytes(long memoryUsedBytes) { this.memoryUsedBytes = memoryUsedBytes; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }
    }
}

