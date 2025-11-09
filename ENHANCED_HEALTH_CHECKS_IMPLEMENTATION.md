# Enhanced Health Checks Implementation Guide

## Overview

Comprehensive health check implementation for all microservices with detailed dependency monitoring, Kubernetes-ready probes, and service statistics.

## Features

### 1. **Comprehensive Dependency Checks**
- ✅ Database connectivity and performance
- ✅ Kafka connectivity and producer status
- ✅ Redis connectivity and cache operations
- ✅ Eureka service discovery status
- ✅ Response time measurements

### 2. **Service Statistics**
- Booking counts and recent activity
- Train counts and active trains
- User counts and registrations
- Payment statistics
- Notification statistics

### 3. **Kubernetes-Ready Probes**
- Liveness probe: `/actuator/health/liveness`
- Readiness probe: `/actuator/health/readiness`
- Health probe: `/actuator/health`

### 4. **Detailed Health Information**
- Component-level status
- Response times
- Error messages
- Service metadata

## Health Check Structure

### Response Format

```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": true,
      "url": "jdbc:h2:mem:testdb",
      "driverName": "H2 JDBC Driver",
      "responseTime": "5ms",
      "queryTest": "success",
      "queryTime": "2ms",
      "totalBookings": 10
    },
    "kafka": {
      "status": true,
      "bootstrapServers": "localhost:9092",
      "responseTime": "3ms"
    },
    "redis": {
      "status": true,
      "ping": "PONG",
      "responseTime": "1ms",
      "cacheTest": "success",
      "required": false
    },
    "eureka": {
      "status": true,
      "instanceId": "irctc-booking-service:8093",
      "appName": "IRCTC-BOOKING-SERVICE",
      "status": "UP"
    },
    "statistics": {
      "totalBookings": 10,
      "recentBookings24h": 5,
      "lastBookingTime": "2024-11-09 10:30:00"
    }
  },
  "checkDuration": "25ms",
  "timestamp": "2024-11-09T10:30:00",
  "service": "irctc-booking-service",
  "version": "2.0.0"
}
```

## Service-Specific Health Checks

### Booking Service
**Dependencies:**
- Database (Required)
- Kafka (Required)
- Redis (Optional)
- Eureka (Optional)

**Statistics:**
- Total bookings
- Recent bookings (24h)
- Last booking time

### Train Service
**Dependencies:**
- Database (Required)
- Redis (Optional)
- Eureka (Optional)

**Statistics:**
- Total trains
- Active trains

### User Service
**Dependencies:**
- Database (Required)
- Kafka (Required)
- Redis (Optional)
- Eureka (Optional)

**Statistics:**
- Total users
- Last user registration

### Payment Service
**Dependencies:**
- Database (Required)
- Redis (Optional)
- Eureka (Optional)

**Statistics:**
- Total payments
- Completed payments
- Refunded payments

### Notification Service
**Dependencies:**
- Database (Required)
- Kafka (Required)
- Redis (Optional)
- Eureka (Optional)

**Statistics:**
- Total notifications
- Recent notifications (24h)

## Kubernetes Probes

### Liveness Probe
Checks if the service is alive and running.

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8093
  initialDelaySeconds: 60
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe
Checks if the service is ready to accept traffic.

```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8093
  initialDelaySeconds: 30
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

### Health Probe
Comprehensive health check with all details.

```yaml
healthProbe:
  httpGet:
    path: /actuator/health
    port: 8093
  initialDelaySeconds: 30
  periodSeconds: 30
  timeoutSeconds: 5
```

## API Endpoints

### Health Check
```http
GET /actuator/health
```

**Response:** Full health status with all components

### Liveness Probe
```http
GET /actuator/health/liveness
```

**Response:** Simple UP/DOWN status for liveness

### Readiness Probe
```http
GET /actuator/health/readiness
```

**Response:** Simple UP/DOWN status for readiness

## Configuration

### Application Properties

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true  # Enable Kubernetes probes
  health:
    liveness-state:
      enabled: true
    readiness-state:
      enabled: true
```

## Health Check Logic

### Status Determination

1. **UP**: All required dependencies are healthy
2. **DOWN**: Any required dependency is unhealthy
3. **UNKNOWN**: Health check failed or component not configured

### Required vs Optional Dependencies

- **Required**: Database, Kafka (for services that use it)
  - If down, overall health is DOWN
  
- **Optional**: Redis, Eureka
  - If down, overall health remains UP
  - Status is reported but doesn't affect overall health

## Performance Considerations

### Health Check Duration
- Target: < 100ms
- Database check: ~5-10ms
- Kafka check: ~3-5ms
- Redis check: ~1-3ms
- Eureka check: ~2-5ms
- Total: ~15-30ms typical

### Caching
- Health checks are not cached
- Each request performs fresh checks
- Ensures accurate status reporting

### Timeouts
- Database: 2 seconds
- Kafka: Configuration check only
- Redis: Connection timeout
- Eureka: Instance info retrieval

## Monitoring Integration

### Prometheus Metrics
Health check results are exposed via Prometheus:

```
health_status{service="irctc-booking-service",component="database"} 1
health_status{service="irctc-booking-service",component="kafka"} 1
health_status{service="irctc-booking-service",component="redis"} 1
```

### Grafana Dashboards
Health check metrics can be visualized in Grafana:
- Service health status
- Component health trends
- Response time trends
- Failure rates

## Troubleshooting

### Database Health Check Fails
1. Check database connection string
2. Verify database is running
3. Check network connectivity
4. Review connection pool settings

### Kafka Health Check Fails
1. Verify Kafka is running
2. Check bootstrap servers configuration
3. Review Kafka producer configuration
4. Check network connectivity

### Redis Health Check Fails
1. Verify Redis is running
2. Check Redis connection configuration
3. Review network connectivity
4. Note: Redis is optional, doesn't fail overall health

### Eureka Health Check Fails
1. Verify Eureka server is running
2. Check Eureka client configuration
3. Review service registration
4. Note: Eureka is optional for some services

## Best Practices

1. **Health Check Frequency**
   - Liveness: Every 10-30 seconds
   - Readiness: Every 5-10 seconds
   - Full health: Every 30-60 seconds

2. **Timeout Settings**
   - Keep timeouts short (< 5 seconds)
   - Fail fast for better responsiveness

3. **Failure Thresholds**
   - Liveness: 3 failures before restart
   - Readiness: 3 failures before removing from load balancer

4. **Initial Delays**
   - Liveness: 60 seconds (allow startup time)
   - Readiness: 30 seconds (allow initialization)

5. **Monitoring**
   - Alert on health check failures
   - Track health check response times
   - Monitor component health trends

## Files Created

### Enhanced Health Indicators
- `EnhancedBookingHealthIndicator.java`
- `EnhancedTrainHealthIndicator.java`
- `EnhancedUserHealthIndicator.java`
- `EnhancedPaymentHealthIndicator.java`
- `EnhancedNotificationHealthIndicator.java`

### Configuration Updates
- `application.yml` (all services) - Added probe configuration

## Migration from Old Health Indicators

The old health indicators are still present but the enhanced versions provide:
- More detailed information
- Better error handling
- Performance metrics
- Kubernetes probe support
- Component-level status

Both can coexist, but the enhanced versions are recommended for production use.

## Testing

### Test Health Check
```bash
curl http://localhost:8093/actuator/health | jq
```

### Test Liveness Probe
```bash
curl http://localhost:8093/actuator/health/liveness
```

### Test Readiness Probe
```bash
curl http://localhost:8093/actuator/health/readiness
```

### Test All Services
```bash
for port in 8091 8092 8093 8094 8095; do
  echo "Service on port $port:"
  curl -s http://localhost:$port/actuator/health | jq -r '.status'
done
```

## Benefits

1. **Better Observability**
   - Detailed component status
   - Performance metrics
   - Service statistics

2. **Kubernetes Integration**
   - Ready for container orchestration
   - Proper liveness/readiness probes
   - Automatic restart and traffic management

3. **Faster Issue Detection**
   - Component-level health checks
   - Response time monitoring
   - Proactive alerting

4. **Improved Reliability**
   - Dependency health visibility
   - Better deployment strategies
   - Graceful degradation support

## Conclusion

Enhanced health checks provide comprehensive monitoring and Kubernetes-ready probes for all microservices. The implementation ensures better observability, faster issue detection, and improved reliability.

