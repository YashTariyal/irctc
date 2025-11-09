# 🔄 Saga Pattern Implementation Guide

## 🎯 Overview

This guide explains the Saga Pattern implementation in IRCTC microservices. The Saga Pattern manages distributed transactions across multiple services without using traditional two-phase commit (2PC).

---

## 📋 What is the Saga Pattern?

The Saga Pattern is a design pattern for managing distributed transactions. Instead of using ACID transactions across services, it uses a sequence of local transactions with compensating actions.

### Key Concepts

- **Saga Steps**: Each step is a local transaction in a service
- **Compensation**: If a step fails, previous steps are rolled back using compensating transactions
- **Orchestration**: A central orchestrator coordinates the saga steps
- **Eventual Consistency**: The system eventually reaches a consistent state

---

## 🏗️ Architecture

### Booking Saga Flow

```
┌─────────────────────────────────────────────────────────┐
│              Booking Saga Orchestrator                  │
└─────────────────────────────────────────────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
    Step 1      Step 2      Step 3
        │           │           │
    Create      Process      Send
    Booking     Payment    Notification
        │           │           │
        └───────────┼───────────┘
                    │
            If any step fails:
                    │
        ┌───────────┼───────────┐
        │           │           │
    Compensate Compensate  (No action)
    Step 3      Step 2
        │           │
    (None)    Refund Payment
        │           │
        └───────────┼───────────┘
                    │
            Compensate Step 1
                    │
            Cancel Booking
```

### Saga Steps

1. **Step 1: Create Booking**
   - Create booking in PENDING status
   - Store booking ID in saga data
   - **Compensation**: Cancel booking

2. **Step 2: Process Payment**
   - Initiate payment processing
   - Store payment ID in saga data
   - **Compensation**: Refund payment

3. **Step 3: Send Notification**
   - Update booking to CONFIRMED
   - Send confirmation notification
   - **Compensation**: None (idempotent)

---

## 🔧 Implementation

### Saga Instance Entity

**Location**: `irctc-booking-service/src/main/java/com/irctc/booking/saga/SagaInstance.java`

```java
@Entity
@Table(name = "saga_instances")
public class SagaInstance {
    private String sagaId;
    private String sagaType;
    private String correlationId;
    private SagaStatus status;
    private Integer currentStep;
    private Integer totalSteps;
    private String sagaData; // JSON context
    private String compensationData; // JSON compensation info
}
```

### Saga Orchestrator

**Location**: `irctc-booking-service/src/main/java/com/irctc/booking/saga/BookingSagaOrchestrator.java`

```java
@Service
public class BookingSagaOrchestrator {
    
    public SagaInstance startBookingSaga(SimpleBooking bookingRequest) {
        // 1. Create saga instance
        // 2. Execute Step 1: Create Booking
        // 3. Execute Step 2: Process Payment
        // 4. Execute Step 3: Send Notification
        // 5. If any step fails, compensate previous steps
    }
}
```

---

## 📊 Saga States

### Saga Status

- **STARTED**: Saga initiated
- **IN_PROGRESS**: Saga steps executing
- **COMPLETED**: All steps completed successfully
- **COMPENSATING**: Compensation in progress
- **COMPENSATED**: Compensation completed
- **FAILED**: Saga failed (unrecoverable)

### State Transitions

```
STARTED → IN_PROGRESS → COMPLETED
    │
    ├─→ COMPENSATING → COMPENSATED
    │
    └─→ FAILED
```

---

## 🚀 Usage

### Starting a Saga

```bash
# Start booking saga
curl -X POST http://localhost:8093/api/saga/booking/start \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "totalFare": 1000.0
  }'
```

### Response

```json
{
  "sagaId": "550e8400-e29b-41d4-a716-446655440000",
  "sagaType": "BOOKING_SAGA",
  "correlationId": "BOOKING_1234567890",
  "status": "COMPLETED",
  "currentStep": 3,
  "totalSteps": 3,
  "sagaData": "{\"bookingId\": 123, \"paymentId\": \"PAY_...\"}",
  "createdAt": "2024-11-09T10:00:00",
  "completedAt": "2024-11-09T10:00:05"
}
```

### Querying Saga Status

```bash
# Get saga by ID
curl http://localhost:8093/api/saga/{sagaId}

# Get saga by correlation ID
curl http://localhost:8093/api/saga/correlation/{correlationId}
```

---

## 🔄 Compensation Logic

### Step 1 Compensation: Cancel Booking

```java
private void compensateStep1_CancelBooking(SagaInstance saga) {
    Long bookingId = extractBookingId(saga);
    bookingService.cancelBooking(bookingId);
}
```

### Step 2 Compensation: Refund Payment

```java
private void compensateStep2_RefundPayment(SagaInstance saga) {
    String paymentId = extractPaymentId(saga);
    // Publish refund event
    kafkaTemplate.send("refund-processed", refundEvent);
}
```

### Step 3 Compensation: None

Step 3 (notification) is idempotent and doesn't require compensation.

---

## 📈 Benefits

### 1. **No Distributed Locks**

- Each step uses local transactions
- No need for distributed locking
- Better performance and scalability

### 2. **Failure Handling**

- Automatic compensation on failures
- System eventually reaches consistent state
- Clear failure points and recovery

### 3. **Service Independence**

- Services remain loosely coupled
- Each service manages its own data
- No tight coordination required

### 4. **Observability**

- Saga state tracked in database
- Can query saga status at any time
- Full audit trail of saga execution

---

## 🎯 Best Practices

### 1. **Idempotent Operations**

Ensure all saga steps are idempotent:

```java
// Good: Idempotent
public SimpleBooking createBooking(SimpleBooking booking) {
    if (booking.getId() != null && exists(booking.getId())) {
        return getById(booking.getId()); // Return existing
    }
    return save(booking);
}
```

### 2. **Compensatable Actions**

All saga steps must have compensating actions:

```java
// Step: Create booking
// Compensation: Cancel booking

// Step: Process payment
// Compensation: Refund payment
```

### 3. **Saga Data Storage**

Store all necessary data for compensation:

```json
{
  "bookingId": 123,
  "paymentId": "PAY_456",
  "userId": 1,
  "totalFare": 1000.0
}
```

### 4. **Error Handling**

Handle errors gracefully:

```java
try {
    saga = executeStep2_ProcessPayment(saga);
} catch (Exception e) {
    compensateStep1_CancelBooking(saga);
    saga.setStatus(SagaStatus.COMPENSATED);
}
```

---

## 🐛 Troubleshooting

### Issue: Saga Stuck in IN_PROGRESS

**Symptoms**: Saga status remains IN_PROGRESS

**Solutions**:
1. Check service logs for errors
2. Verify Kafka connectivity
3. Manually trigger compensation if needed
4. Implement saga timeout mechanism

### Issue: Compensation Fails

**Symptoms**: Compensation step throws exception

**Solutions**:
1. Make compensation idempotent
2. Implement retry mechanism
3. Log compensation failures
4. Alert on compensation failures

### Issue: Duplicate Saga Execution

**Symptoms**: Same saga executed multiple times

**Solutions**:
1. Use idempotency keys
2. Check saga existence before creating
3. Use correlation IDs to prevent duplicates

---

## 📊 Monitoring

### Saga Metrics

Track saga execution:

- **Saga Success Rate**: Completed / Total
- **Compensation Rate**: Compensated / Total
- **Average Saga Duration**: Time to complete
- **Step Failure Rate**: Failed steps / Total steps

### Prometheus Metrics

```java
// Saga metrics
saga_started_total
saga_completed_total
saga_compensated_total
saga_duration_seconds
saga_step_failures_total
```

---

## 🔄 Event-Driven Enhancement

### Current Implementation

- Orchestration-based (synchronous)
- Steps executed sequentially
- Compensation triggered immediately

### Future Enhancement

- Event-driven saga (asynchronous)
- Steps triggered by events
- Better scalability and resilience

```java
@KafkaListener(topics = "payment-completed")
public void handlePaymentCompleted(PaymentCompletedEvent event) {
    sagaOrchestrator.proceedToNextStep(event.getSagaId());
}
```

---

## 📚 Additional Resources

- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Distributed Transactions](https://martinfowler.com/articles/patterns-of-distributed-systems/saga.html)
- [Event Sourcing and CQRS](https://martinfowler.com/eaaDev/EventSourcing.html)

---

## 🎯 Summary

The Saga Pattern provides:

✅ **Distributed Transaction Management**: Without 2PC  
✅ **Automatic Compensation**: On failures  
✅ **Service Independence**: Loose coupling  
✅ **Observability**: Full saga tracking  
✅ **Scalability**: No distributed locks  

---

*Last Updated: November 2025*

