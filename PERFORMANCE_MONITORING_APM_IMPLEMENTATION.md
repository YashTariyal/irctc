# Performance Monitoring & APM Implementation Guide

## Overview

Comprehensive Application Performance Monitoring (APM) solution for tracking performance metrics, detecting slow queries, monitoring memory usage, and identifying performance bottlenecks.

## Features

### 1. **Memory Monitoring**
- ✅ Real-time memory usage tracking
- ✅ Memory leak detection
- ✅ Memory usage percentage
- ✅ Heap memory statistics
- ✅ Prometheus metrics integration

### 2. **Slow Query Detection**
- ✅ Automatic slow query detection
- ✅ Configurable threshold
- ✅ Query duration tracking
- ✅ Slow query logging
- ✅ Slow query history

### 3. **Thread Monitoring**
- ✅ Thread count tracking
- ✅ Thread statistics
- ✅ Prometheus metrics

### 4. **Performance Profiling**
- ✅ Method-level profiling
- ✅ Memory usage per method
- ✅ Execution time tracking
- ✅ Custom profiling utilities

### 5. **Performance API**
- ✅ Performance summary endpoint
- ✅ Memory statistics endpoint
- ✅ Thread statistics endpoint
- ✅ Slow queries endpoint

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Service   │───▶│  Performance     │───▶│   Prometheus    │
│   Methods   │    │  Monitoring      │    │   Metrics       │
└─────────────┘    └──────────────────┘    └─────────────────┘
      │                    │                         │
      │                    ▼                         │
      │            ┌─────────────────┐              │
      │            │  Memory Leak    │              │
      │            │  Detection      │              │
      │            └─────────────────┘              │
      │                                              │
      └──────────────────────────────────────────────┘
              Performance Metrics & Alerts
```

## Memory Monitoring

### Memory Metrics

**Prometheus Metrics:**
```
jvm_memory_usage_percent - JVM memory usage percentage
jvm_memory_used - JVM memory used in bytes
jvm_memory_max - JVM memory max in bytes
```

**API Endpoint:**
```http
GET /api/performance/memory
```

**Response:**
```json
{
  "usagePercent": 65.5,
  "usedBytes": 2147483648,
  "maxBytes": 4294967296,
  "usedMB": 2048,
  "maxMB": 4096,
  "freeMB": 2048
}
```

### Memory Leak Detection

**Detection Algorithm:**
- Tracks memory usage over time (last 10 snapshots)
- Analyzes trend (increasing pattern)
- Alerts if 80% of snapshots show increasing trend
- Threshold: 80% memory usage

**Logs:**
```
🚨 Memory leak detected! Usage: 85.23% (Threshold: 80%)
⚠️  High memory usage: 82.15%
```

## Slow Query Detection

### Configuration

```yaml
performance:
  monitoring:
    enabled: true
    slow-query-threshold: 1000  # milliseconds
```

### Detection

**Automatic Detection:**
- Monitors all database queries
- Tracks query execution time
- Logs queries exceeding threshold
- Stores slow query history

**Logs:**
```
⚠️  Slow query detected: createBooking - Duration: 1250ms - Operation: booking_creation
🔍 Query started [Q1234567890_1]: SELECT * FROM bookings...
✅ Query completed [Q1234567890_1]: 1250ms
```

### Slow Query API

**Get Slow Queries:**
```http
GET /api/performance/slow-queries
```

**Response:**
```json
[
  {
    "query": "createBooking",
    "durationMs": 1250,
    "operation": "booking_creation",
    "timestamp": "2024-12-28T10:30:00"
  }
]
```

**Clear Slow Queries:**
```http
DELETE /api/performance/slow-queries
```

## Performance Profiling

### Usage

```java
@Autowired
private PerformanceProfiler profiler;

public void someMethod() {
    profiler.profile("someMethod", () -> {
        // Your code here
        return result;
    });
}
```

### Manual Profiling

```java
ProfilerContext context = profiler.start("methodName");
try {
    // Your code
} finally {
    ProfilerResult result = profiler.stop(context);
    logger.info("Method {} took {}ms", result.getMethodName(), result.getDurationMs());
}
```

## Performance API Endpoints

### Get Performance Summary

```http
GET /api/performance/summary
```

**Response:**
```json
{
  "memoryUsagePercent": 65.5,
  "memoryUsedBytes": 2147483648,
  "memoryMaxBytes": 4294967296,
  "threadCount": 45,
  "slowQueryCount": 5,
  "memoryLeakCount": 0,
  "slowQueries": [...],
  "timestamp": "2024-12-28T10:30:00"
}
```

### Get Memory Statistics

```http
GET /api/performance/memory
```

### Get Thread Statistics

```http
GET /api/performance/threads
```

**Response:**
```json
{
  "threadCount": 45
}
```

### Get Performance Metrics

```http
GET /api/performance/metrics
```

**Response:**
```json
{
  "memory": {
    "usagePercent": 65.5,
    "usedBytes": 2147483648,
    "maxBytes": 4294967296,
    "usedMB": 2048,
    "maxMB": 4096
  },
  "threads": {
    "count": 45
  },
  "slowQueries": {
    "count": 5,
    "recent": 3
  },
  "memoryLeaks": {
    "count": 0
  },
  "timestamp": "2024-12-28T10:30:00"
}
```

## Prometheus Metrics

### Memory Metrics

```
# HELP jvm_memory_usage_percent JVM memory usage percentage
# TYPE jvm_memory_usage_percent gauge
jvm_memory_usage_percent 65.5

# HELP jvm_memory_used JVM memory used in bytes
# TYPE jvm_memory_used gauge
jvm_memory_used 2.147483648e+09

# HELP jvm_memory_max JVM memory max in bytes
# TYPE jvm_memory_max gauge
jvm_memory_max 4.294967296e+09
```

### Thread Metrics

```
# HELP jvm_threads_count JVM thread count
# TYPE jvm_threads_count gauge
jvm_threads_count 45
```

### Performance Metrics

```
# HELP performance_memory_leak_detected Number of memory leak detections
# TYPE performance_memory_leak_detected counter
performance_memory_leak_detected_total 0

# HELP performance_slow_query_detected Number of slow query detections
# TYPE performance_slow_query_detected counter
performance_slow_query_detected_total 5
```

## Configuration

### Application Properties

```yaml
performance:
  monitoring:
    enabled: true  # Enable performance monitoring
    slow-query-threshold: 1000  # Slow query threshold in milliseconds
    memory-leak-threshold: 80  # Memory leak threshold percentage
    cpu-threshold: 80  # CPU threshold percentage
```

### Scheduling

Performance monitoring uses Spring's `@Scheduled` annotation:
- Memory leak detection: Every 60 seconds
- Memory snapshots: Every 60 seconds

## Integration Examples

### Service Integration

```java
@Service
public class SimpleBookingService {
    
    @Autowired(required = false)
    private PerformanceMonitoringService performanceMonitoringService;
    
    public SimpleBooking createBooking(SimpleBooking booking) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Your business logic
            SimpleBooking saved = bookingRepository.save(booking);
            
            // Record slow query if applicable
            long duration = System.currentTimeMillis() - startTime;
            if (performanceMonitoringService != null && duration > 1000) {
                performanceMonitoringService.recordSlowQuery(
                    "createBooking", duration, "booking_creation"
                );
            }
            
            return saved;
        } catch (Exception e) {
            // Error handling
            throw e;
        }
    }
}
```

### Profiler Integration

```java
@Service
public class SomeService {
    
    @Autowired
    private PerformanceProfiler profiler;
    
    public Result complexOperation() {
        return profiler.profile("complexOperation", () -> {
            // Complex operation
            return result;
        });
    }
}
```

## Best Practices

### 1. **Slow Query Threshold**
- Set based on SLA requirements
- Typical: 1000ms for database queries
- Adjust based on service criticality

### 2. **Memory Leak Detection**
- Monitor over time (not just single snapshot)
- Use trend analysis
- Set appropriate threshold (80% default)

### 3. **Performance Profiling**
- Profile critical paths
- Don't profile everything (overhead)
- Use in development/testing

### 4. **Monitoring Frequency**
- Balance between accuracy and overhead
- Memory checks: Every 60 seconds
- Query monitoring: Real-time

### 5. **Alerting**
- Set up alerts for memory leaks
- Alert on high slow query count
- Monitor thread count spikes

## Grafana Dashboard

### Recommended Panels

1. **Memory Usage**
   - Memory usage percentage over time
   - Memory used/available
   - Memory leak alerts

2. **Slow Queries**
   - Slow query count over time
   - Slow query duration distribution
   - Top slow queries

3. **Thread Statistics**
   - Thread count over time
   - Thread count by state

4. **Performance Summary**
   - Overall performance health
   - Key metrics at a glance

## Files Created

### Core Components
- `PerformanceMonitoringService.java` - Main monitoring service
- `SlowQueryInterceptor.java` - Slow query detection
- `PerformanceProfiler.java` - Method profiling
- `PerformanceMonitoringController.java` - REST API
- `PerformanceMonitoringConfig.java` - Configuration

### Configuration
- `application.yml` - Performance monitoring configuration

## Benefits

1. **Proactive Issue Detection**
   - Memory leaks detected early
   - Slow queries identified
   - Performance bottlenecks found

2. **Performance Optimization**
   - Identify slow operations
   - Memory usage optimization
   - Thread pool tuning

3. **Observability**
   - Real-time performance metrics
   - Historical performance data
   - Prometheus integration

4. **Resource Management**
   - Memory usage monitoring
   - Thread pool monitoring
   - Resource optimization

5. **Debugging**
   - Slow query history
   - Performance profiling
   - Memory leak tracking

## Conclusion

The Performance Monitoring & APM implementation provides comprehensive performance tracking, memory leak detection, slow query identification, and performance profiling capabilities. This enables proactive performance optimization and issue detection.

