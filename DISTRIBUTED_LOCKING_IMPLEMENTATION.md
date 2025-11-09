# Distributed Locking Implementation Guide

## Overview

Comprehensive distributed locking implementation using Redis for critical operations across multiple service instances. Prevents race conditions and ensures atomic operations in distributed environments.

## Features

### 1. **Redis-Based Distributed Locks**
- ✅ Atomic lock acquisition using Lua scripts
- ✅ Lock timeout and automatic expiration
- ✅ Lock renewal mechanism
- ✅ Lock release with owner verification
- ✅ Lock status checking

### 2. **Annotation-Based Usage**
- ✅ `@DistributedLock` annotation for easy integration
- ✅ SpEL (Spring Expression Language) support for dynamic lock keys
- ✅ Automatic lock acquisition and release
- ✅ Configurable timeout and wait time

### 3. **Lock Management**
- ✅ Lock status API
- ✅ Lock information retrieval
- ✅ Manual lock operations (for testing/admin)
- ✅ Lock owner tracking

### 4. **Metrics and Monitoring**
- ✅ Lock acquisition metrics
- ✅ Lock release metrics
- ✅ Lock failure tracking
- ✅ Lock timeout tracking
- ✅ Prometheus integration

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Service A      │───▶│  Distributed    │───▶│     Redis       │
│  (Instance 1)   │    │  Lock Service   │    │   (Lock Store)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
┌─────────────────┐           │                        │
│  Service A      │───────────┼────────────────────────┘
│  (Instance 2)   │           │
└─────────────────┘           │
                              ▼
                    ┌─────────────────┐
                    │  Lock Acquired  │
                    │  or Rejected    │
                    └─────────────────┘
```

## Usage

### Annotation-Based (Recommended)

```java
@DistributedLock(key = "booking:#{#booking.trainId}", timeout = 30, waitTime = 5)
public SimpleBooking createBooking(SimpleBooking booking) {
    // Critical operation - only one instance can execute at a time
    // for the same trainId
    return bookingRepository.save(booking);
}
```

### Programmatic Usage

```java
@Autowired
private DistributedLockService lockService;

public void performCriticalOperation(Long resourceId) {
    String lockKey = "resource:" + resourceId;
    DistributedLockService.LockHandle lockHandle = 
        lockService.acquireLock(lockKey, 30, 5);
    
    if (lockHandle == null) {
        throw new LockTimeoutException("Failed to acquire lock");
    }
    
    try {
        // Critical operation
        performOperation(resourceId);
    } finally {
        lockService.releaseLock(lockHandle);
    }
}
```

### Lock Renewal

```java
DistributedLockService.LockHandle lockHandle = 
    lockService.acquireLock("long-operation", 60, 5);

try {
    // Long-running operation
    while (operationInProgress) {
        // Renew lock every 30 seconds
        if (lockHandle.getRemainingTimeSeconds() < 30) {
            lockService.renewLock(lockHandle, 60);
        }
        performWork();
    }
} finally {
    lockService.releaseLock(lockHandle);
}
```

## Lock Key Patterns

### Simple Keys
```java
@DistributedLock(key = "booking:123")
```

### Dynamic Keys (SpEL)
```java
@DistributedLock(key = "booking:#{#booking.trainId}")
@DistributedLock(key = "payment:#{#bookingId}")
@DistributedLock(key = "seat:#{#trainId}:#{#date}")
```

### Composite Keys
```java
@DistributedLock(key = "booking:#{#trainId}:#{#journeyDate}")
```

## Configuration

### Application Properties

```yaml
# Distributed Locking Configuration
distributed-lock:
  default-timeout: 30  # Default lock timeout in seconds
  default-wait-time: 5  # Default wait time to acquire lock in seconds
  enable-metrics: true  # Enable Prometheus metrics for locks
```

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

## API Endpoints

### Get Lock Status
```http
GET /api/locks/{lockKey}/status
```

**Response:**
```json
{
  "lockKey": "booking:123",
  "isLocked": true,
  "owner": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "ttl": 25
}
```

### Get Lock Information
```http
GET /api/locks/{lockKey}
```

**Response:**
```json
{
  "lockKey": "booking:123",
  "isLocked": true,
  "owner": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "ttl": 25
}
```

### Acquire Lock (Manual)
```http
POST /api/locks/{lockKey}/acquire?timeout=30
```

**Response:**
```json
{
  "success": true,
  "lockKey": "booking:123",
  "owner": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timeout": 30,
  "acquiredAt": 1699545600000
}
```

## Lock Implementation Details

### Atomic Operations

Locks use Redis Lua scripts for atomic operations:

**Lock Acquisition:**
```lua
if redis.call('get', KEYS[1]) == false then
  redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2])
  return 1
else
  return 0
end
```

**Lock Release:**
```lua
if redis.call('get', KEYS[1]) == ARGV[1] then
  return redis.call('del', KEYS[1])
else
  return 0
end
```

**Lock Renewal:**
```lua
if redis.call('get', KEYS[1]) == ARGV[1] then
  return redis.call('expire', KEYS[1], ARGV[2])
else
  return 0
end
```

### Lock Handle

The `LockHandle` class provides:
- Lock key
- Lock owner (UUID)
- Timeout in seconds
- Acquisition timestamp
- Remaining time calculation

## Use Cases

### 1. **Prevent Duplicate Bookings**
```java
@DistributedLock(key = "booking:#{#trainId}:#{#date}", timeout = 30)
public Booking createBooking(Long trainId, LocalDate date, ...) {
    // Ensure only one booking per train/date at a time
}
```

### 2. **Seat Allocation**
```java
@DistributedLock(key = "seat:#{#trainId}:#{#seatNumber}", timeout = 10)
public void allocateSeat(Long trainId, String seatNumber) {
    // Prevent concurrent seat allocation
}
```

### 3. **Payment Processing**
```java
@DistributedLock(key = "payment:#{#bookingId}", timeout = 60)
public Payment processPayment(Long bookingId, ...) {
    // Ensure only one payment processing per booking
}
```

### 4. **Inventory Management**
```java
@DistributedLock(key = "inventory:#{#itemId}", timeout = 30)
public void updateInventory(Long itemId, int quantity) {
    // Prevent race conditions in inventory updates
}
```

## Metrics

### Prometheus Metrics

**Lock Acquisition:**
```
distributed_lock_acquired_total{lock_key="booking:123"}
```

**Lock Release:**
```
distributed_lock_released_total{lock_key="booking:123"}
```

**Lock Failures:**
```
distributed_lock_failed_total{lock_key="booking:123"}
```

**Lock Timeouts:**
```
distributed_lock_timeout_total{lock_key="booking:123"}
```

**Lock Acquisition Time:**
```
distributed_lock_acquisition_time_seconds{lock_key="booking:123"}
```

## Best Practices

### 1. **Lock Key Design**
- Use descriptive, unique keys
- Include all relevant identifiers
- Avoid too generic keys (may cause contention)

### 2. **Timeout Selection**
- Short timeouts for quick operations (5-10 seconds)
- Medium timeouts for normal operations (30-60 seconds)
- Long timeouts for batch operations (5+ minutes)

### 3. **Wait Time**
- Set appropriate wait time based on expected contention
- Use 0 for non-blocking operations
- Use 5-10 seconds for most cases

### 4. **Lock Renewal**
- Renew locks for long-running operations
- Renew before lock expires
- Monitor remaining time

### 5. **Error Handling**
- Always release locks in finally blocks
- Handle lock acquisition failures gracefully
- Log lock operations for debugging

### 6. **Deadlock Prevention**
- Use consistent lock ordering
- Avoid nested locks when possible
- Set reasonable timeouts

## Troubleshooting

### Lock Not Acquired

**Possible Causes:**
1. Lock already held by another instance
2. Redis connection issues
3. Lock timeout too short
4. Wait time too short

**Solutions:**
- Check lock status via API
- Verify Redis connectivity
- Increase timeout/wait time
- Check for deadlocks

### Lock Not Released

**Possible Causes:**
1. Application crash before release
2. Exception in critical section
3. Lock expired (automatic release)

**Solutions:**
- Locks automatically expire (timeout)
- Use try-finally blocks
- Monitor lock metrics

### High Lock Contention

**Symptoms:**
- Many lock failures
- Slow operations
- High wait times

**Solutions:**
- Review lock key granularity
- Optimize critical sections
- Consider lock-free alternatives where possible

## Integration Examples

### Booking Service

```java
@Service
public class SimpleBookingService {
    
    @DistributedLock(key = "booking:#{#booking.trainId}", timeout = 30)
    public SimpleBooking createBooking(SimpleBooking booking) {
        // Prevent concurrent bookings for same train
        return bookingRepository.save(booking);
    }
}
```

### Payment Service

```java
@Service
public class SimplePaymentService {
    
    @DistributedLock(key = "payment:#{#bookingId}", timeout = 60)
    public Payment processPayment(Long bookingId, Payment payment) {
        // Ensure only one payment processing per booking
        return paymentRepository.save(payment);
    }
}
```

### Train Service

```java
@Service
public class SimpleTrainService {
    
    @DistributedLock(key = "train-update:#{#id}", timeout = 10)
    public SimpleTrain updateTrain(Long id, SimpleTrain train) {
        // Prevent concurrent train updates
        return trainRepository.save(train);
    }
}
```

## Comparison with ShedLock

| Feature | DistributedLockService | ShedLock |
|---------|----------------------|----------|
| Use Case | General operations | Scheduled tasks |
| Annotation | `@DistributedLock` | `@SchedulerLock` |
| Backend | Redis | Database/Redis |
| Lock Key | Custom (SpEL) | Method name |
| Timeout | Configurable | Configurable |
| Renewal | Manual | Automatic |

## Files Created

### Core Components
- `DistributedLockService.java` - Core locking service
- `DistributedLock.java` - Annotation for locking
- `DistributedLockAspect.java` - AOP aspect for automatic locking
- `DistributedLockController.java` - REST API for lock management
- `DistributedLockConfig.java` - Redis configuration

### Configuration
- `application.yml` - Lock configuration (all services)

## Benefits

1. **Data Consistency**
   - Prevents race conditions
   - Ensures atomic operations
   - Prevents duplicate processing

2. **Reliability**
   - Automatic lock expiration
   - Owner verification
   - Deadlock prevention

3. **Observability**
   - Lock metrics
   - Status API
   - Owner tracking

4. **Ease of Use**
   - Annotation-based
   - Automatic management
   - SpEL support

## Conclusion

Distributed Locking provides a robust solution for managing concurrent operations across multiple service instances. The Redis-based implementation ensures atomicity, reliability, and observability for critical operations.

