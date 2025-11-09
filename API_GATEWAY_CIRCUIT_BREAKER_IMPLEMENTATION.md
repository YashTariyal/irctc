# API Gateway Circuit Breaker Implementation Guide

## Overview

Comprehensive circuit breaker implementation using Resilience4j for the API Gateway to prevent cascading failures and provide graceful degradation when downstream services are unavailable.

## Features

### 1. **Service-Specific Circuit Breakers**
- ✅ Booking Service Circuit Breaker
- ✅ Payment Service Circuit Breaker
- ✅ Train Service Circuit Breaker
- ✅ User Service Circuit Breaker
- ✅ Notification Service Circuit Breaker

### 2. **Fallback Handlers**
- ✅ Service-specific fallback responses
- ✅ User-friendly error messages
- ✅ Graceful degradation
- ✅ Appropriate HTTP status codes

### 3. **State Management**
- ✅ State change monitoring
- ✅ Automatic state transitions
- ✅ Manual state control (for testing/admin)
- ✅ State persistence

### 4. **Monitoring & Metrics**
- ✅ Circuit breaker metrics API
- ✅ State change listeners
- ✅ Prometheus integration
- ✅ Real-time status monitoring

## Architecture

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Client    │───▶│  API Gateway     │───▶│  Microservice  │
│             │    │  (Circuit Breaker)│    │  (Downstream)  │
└─────────────┘    └──────────────────┘    └─────────────────┘
      │                    │                         │
      │                    ▼                         │
      │            ┌─────────────────┐              │
      │            │  Fallback       │              │
      │            │  Handler        │              │
      │            └─────────────────┘              │
      │                                              │
      └──────────────────────────────────────────────┘
              Fallback Response (if circuit open)
```

## Circuit Breaker States

### 1. **CLOSED (Normal Operation)**
- Requests flow through normally
- Failures are tracked
- Metrics are collected

### 2. **OPEN (Service Unavailable)**
- Requests are immediately rejected
- Fallback handler is invoked
- No calls to downstream service
- Waits for configured duration before transitioning to HALF_OPEN

### 3. **HALF_OPEN (Testing Recovery)**
- Allows limited number of test calls
- If successful, transitions to CLOSED
- If failed, transitions back to OPEN

## Configuration

### Service-Specific Circuit Breakers

```yaml
resilience4j:
  circuitbreaker:
    instances:
      booking-service:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 20
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 5
        slow-call-rate-threshold: 80
        slow-call-duration-threshold: 5s
      
      payment-service:
        failure-rate-threshold: 40
        wait-duration-in-open-state: 60s
        sliding-window-size: 15
        minimum-number-of-calls: 5
        slow-call-rate-threshold: 70
        slow-call-duration-threshold: 10s
```

### Gateway Route Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: booking-service
          uri: lb://IRCTC-BOOKING-SERVICE
          predicates:
            - Path=/api/bookings/**
          filters:
            - name: CircuitBreaker
              args:
                name: booking-service
                fallbackUri: forward:/fallback/booking
```

## Fallback Responses

### Booking Service Fallback

```json
{
  "status": "BOOKING_SERVICE_UNAVAILABLE",
  "message": "Booking service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-12-28T10:30:00",
  "suggestion": "You can check your booking status later or contact customer support."
}
```

### Payment Service Fallback

```json
{
  "status": "PAYMENT_SERVICE_UNAVAILABLE",
  "message": "Payment service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-12-28T10:30:00",
  "suggestion": "Your payment has not been processed. Please retry after a few moments."
}
```

### Train Service Fallback

```json
{
  "status": "TRAIN_SERVICE_UNAVAILABLE",
  "message": "Train service is temporarily unavailable. Please try again later.",
  "timestamp": "2024-12-28T10:30:00",
  "suggestion": "You can try searching for trains again in a few moments."
}
```

## Circuit Breaker Management API

### Get All Circuit Breakers

```http
GET /api/circuit-breakers
```

**Response:**
```json
{
  "circuitBreakers": {
    "booking-service": {
      "name": "booking-service",
      "state": "CLOSED",
      "failureRateThreshold": 50.0,
      "waitDurationInOpenState": "PT30S",
      "slidingWindowSize": 20,
      "minimumNumberOfCalls": 10,
      "metrics": {
        "failureRate": 0.0,
        "numberOfFailedCalls": 0,
        "numberOfSuccessfulCalls": 150,
        "numberOfNotPermittedCalls": 0,
        "numberOfBufferedCalls": 150
      }
    }
  },
  "totalCount": 5
}
```

### Get Circuit Breaker Status

```http
GET /api/circuit-breakers/{name}
```

**Response:**
```json
{
  "name": "booking-service",
  "state": "CLOSED",
  "failureRateThreshold": 50.0,
  "waitDurationInOpenState": "PT30S",
  "slidingWindowSize": 20,
  "minimumNumberOfCalls": 10,
  "metrics": {
    "failureRate": 0.0,
    "numberOfFailedCalls": 0,
    "numberOfSuccessfulCalls": 150
  }
}
```

### Get Circuit Breaker Metrics

```http
GET /api/circuit-breakers/{name}/metrics
```

**Response:**
```json
{
  "name": "booking-service",
  "state": "CLOSED",
  "failureRate": 0.0,
  "numberOfFailedCalls": 0,
  "numberOfSuccessfulCalls": 150,
  "numberOfNotPermittedCalls": 0,
  "numberOfBufferedCalls": 150,
  "numberOfSlowCalls": 2,
  "slowCallRate": 1.33
}
```

### Transition Circuit Breaker State

```http
POST /api/circuit-breakers/{name}/transition?state=OPEN
```

**Response:**
```json
{
  "message": "Circuit breaker transitioned to OPEN state",
  "name": "booking-service",
  "newState": "OPEN"
}
```

### Reset Circuit Breaker

```http
POST /api/circuit-breakers/{name}/reset
```

**Response:**
```json
{
  "message": "Circuit breaker metrics reset",
  "name": "booking-service",
  "state": "CLOSED"
}
```

## Circuit Breaker Configuration Parameters

### Failure Rate Threshold
- **Default**: 50%
- **Description**: Percentage of failed calls that trigger circuit breaker to OPEN
- **Booking Service**: 50%
- **Payment Service**: 40% (more sensitive)

### Wait Duration in Open State
- **Default**: 10s
- **Description**: Time to wait before transitioning from OPEN to HALF_OPEN
- **Booking Service**: 30s
- **Payment Service**: 60s (longer wait)

### Sliding Window Size
- **Default**: 10
- **Description**: Number of calls to evaluate for failure rate
- **Booking Service**: 20
- **Payment Service**: 15

### Minimum Number of Calls
- **Default**: 5
- **Description**: Minimum calls required before evaluating failure rate
- **Booking Service**: 10
- **Payment Service**: 5

### Slow Call Rate Threshold
- **Default**: 100%
- **Description**: Percentage of slow calls that trigger circuit breaker
- **Booking Service**: 80%
- **Payment Service**: 70%

### Slow Call Duration Threshold
- **Default**: 60s
- **Description**: Duration threshold for considering a call as slow
- **Booking Service**: 5s
- **Payment Service**: 10s

## State Change Monitoring

### Logs

**Circuit Breaker Opened:**
```
❌ Circuit Breaker 'booking-service' state changed: CLOSED → OPEN
   Circuit breaker opened due to high failure rate. Service unavailable.
```

**Circuit Breaker Half-Open:**
```
⚠️  Circuit Breaker 'booking-service' state changed: OPEN → HALF_OPEN
   Testing if service has recovered...
```

**Circuit Breaker Closed:**
```
✅ Circuit Breaker 'booking-service' state changed: HALF_OPEN → CLOSED
   Service is healthy. Circuit breaker closed.
```

## Best Practices

### 1. **Failure Rate Thresholds**
- Set appropriate thresholds based on service criticality
- Payment services: Lower threshold (40%)
- Read services: Higher threshold (50-60%)

### 2. **Wait Duration**
- Longer wait for critical services
- Shorter wait for non-critical services
- Balance between quick recovery and stability

### 3. **Sliding Window Size**
- Larger window for stable services
- Smaller window for volatile services
- Consider call volume

### 4. **Slow Call Detection**
- Configure slow call thresholds
- Monitor slow call rates
- Adjust based on service SLAs

### 5. **Fallback Responses**
- Provide helpful error messages
- Include retry suggestions
- Maintain user experience

### 6. **Monitoring**
- Monitor circuit breaker states
- Track metrics regularly
- Set up alerts for OPEN state

## Example Scenarios

### Scenario 1: Service Failure

1. Booking service starts failing
2. Failure rate exceeds 50%
3. Circuit breaker opens after 10 calls
4. Subsequent requests get fallback response
5. After 30 seconds, circuit breaker transitions to HALF_OPEN
6. Test calls are made
7. If successful, circuit breaker closes
8. If failed, circuit breaker opens again

### Scenario 2: Slow Service

1. Booking service becomes slow (>5s)
2. Slow call rate exceeds 80%
3. Circuit breaker opens
4. Fallback responses are returned
5. Service recovers
6. Circuit breaker closes after successful test calls

### Scenario 3: Manual Intervention

1. Admin detects issue
2. Manually opens circuit breaker
3. Service is taken offline for maintenance
4. After maintenance, manually closes circuit breaker
5. Service resumes normal operation

## Integration with Monitoring

### Prometheus Metrics

```
resilience4j_circuitbreaker_calls_total{name="booking-service",state="closed"}
resilience4j_circuitbreaker_calls_total{name="booking-service",state="open"}
resilience4j_circuitbreaker_failure_rate{name="booking-service"}
resilience4j_circuitbreaker_slow_calls_total{name="booking-service"}
```

### Grafana Dashboard

- Circuit breaker state over time
- Failure rates
- Number of calls (successful/failed)
- Slow call rates
- State transition events

## Files Created

### Core Components
- `CircuitBreakerFallbackController.java` - Fallback handlers
- `CircuitBreakerStateChangeListener.java` - State monitoring
- `CircuitBreakerController.java` - Management API

### Configuration
- `application.yml` - Circuit breaker configuration

## Benefits

1. **Prevent Cascading Failures**
   - Isolate failing services
   - Protect healthy services
   - Maintain system stability

2. **Graceful Degradation**
   - Fallback responses
   - User-friendly messages
   - Maintain user experience

3. **Automatic Recovery**
   - Automatic state transitions
   - Self-healing capability
   - Reduced manual intervention

4. **Observability**
   - State monitoring
   - Metrics collection
   - Real-time status

5. **Flexibility**
   - Service-specific configuration
   - Manual control
   - Customizable thresholds

## Conclusion

The API Gateway Circuit Breaker implementation provides robust fault tolerance, preventing cascading failures and ensuring graceful degradation when downstream services are unavailable. The service-specific configuration, comprehensive monitoring, and management API make it a production-ready solution.

