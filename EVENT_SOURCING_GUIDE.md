# 📚 Event Sourcing Implementation Guide

## 🎯 Overview

This guide explains the Event Sourcing implementation in IRCTC microservices. Event Sourcing stores all changes to application state as a sequence of events, providing complete audit trail and replay capabilities.

---

## 📋 What is Event Sourcing?

Event Sourcing is a pattern where:
- **State changes are stored as events** instead of just current state
- **Current state is derived** by replaying events
- **Complete audit trail** of all changes
- **Time-travel debugging** by replaying events up to a point in time

### Key Benefits

- **Complete Audit Trail**: Every change is recorded
- **Event Replay**: Rebuild state from events
- **Time-Travel Debugging**: See state at any point in time
- **Better Data Consistency**: Events are immutable
- **Event-Driven Architecture**: Natural fit for microservices

---

## 🏗️ Architecture

### Event Store Structure

```
┌─────────────────────────────────────┐
│         Event Store                 │
│  ┌───────────────────────────────┐  │
│  │  booking_events               │  │
│  │  - event_id (PK)              │  │
│  │  - aggregate_id (Booking ID) │  │
│  │  - event_type                 │  │
│  │  - event_data (JSON)          │  │
│  │  - timestamp                  │  │
│  │  - correlation_id             │  │
│  └───────────────────────────────┘  │
│  ┌───────────────────────────────┐  │
│  │  payment_events                │  │
│  │  - event_id (PK)              │  │
│  │  - aggregate_id (Payment ID) │  │
│  │  - event_type                 │  │
│  │  - event_data (JSON)          │  │
│  │  - timestamp                  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

### Event Flow

```
Service Operation
    ↓
Create/Update Entity
    ↓
Store Event in Event Store
    ↓
Event Available for Replay
```

---

## 🔧 Implementation

### Booking Service

**Event Store**: `BookingEventStore`
**Event Entity**: `BookingEvent`
**Replay Service**: `BookingEventReplayService`

**Events Captured**:
- `BOOKING_CREATED`
- `BOOKING_UPDATED`
- `BOOKING_CANCELLED`
- `BOOKING_CONFIRMED`
- `BOOKING_STATUS_CHANGED`
- `FARE_UPDATED`

### Payment Service

**Event Store**: `PaymentEventStore`
**Event Entity**: `PaymentEvent`

**Events Captured**:
- `PAYMENT_INITIATED`
- `PAYMENT_COMPLETED`
- `PAYMENT_FAILED`
- `PAYMENT_REFUNDED`
- `PAYMENT_STATUS_CHANGED`

---

## 📊 Database Schema

### Booking Events Table

```sql
CREATE TABLE booking_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,
  aggregate_id VARCHAR(50) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_data TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  correlation_id VARCHAR(100),
  user_id VARCHAR(100),
  version VARCHAR(50),
  event_metadata TEXT
);
```

### Payment Events Table

```sql
CREATE TABLE payment_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,
  aggregate_id VARCHAR(50) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_data TEXT NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  correlation_id VARCHAR(100),
  user_id VARCHAR(100),
  version VARCHAR(50),
  event_metadata TEXT
);
```

---

## 🚀 Usage

### Automatic Event Storage

Events are automatically stored when operations occur:

```java
// In SimpleBookingService.createBooking()
SimpleBooking saved = bookingRepository.save(booking);

// Event automatically stored
eventStore.appendEvent(
    saved.getId().toString(),
    "BOOKING_CREATED",
    eventData,
    correlationId,
    userId
);
```

### Get Event Stream

```bash
# Get all events for a booking
curl http://localhost:8093/api/events/booking/123
```

**Response**:
```json
[
  {
    "eventId": "evt-001",
    "aggregateId": "123",
    "eventType": "BOOKING_CREATED",
    "eventData": "{\"bookingId\":123,\"userId\":1,\"trainId\":1}",
    "timestamp": "2024-11-09T10:00:00"
  },
  {
    "eventId": "evt-002",
    "aggregateId": "123",
    "eventType": "BOOKING_CONFIRMED",
    "eventData": "{\"bookingId\":123,\"status\":\"CONFIRMED\"}",
    "timestamp": "2024-11-09T10:01:00"
  }
]
```

### Replay Events

```bash
# Replay events to rebuild booking state
curl -X POST http://localhost:8093/api/events/booking/123/replay
```

**Response**:
```json
{
  "id": 123,
  "userId": 1,
  "trainId": 1,
  "pnrNumber": "ABC123XYZ",
  "status": "CONFIRMED",
  "totalFare": 1000.0
}
```

### Time-Travel Debugging

```bash
# Get booking state at a specific point in time
curl "http://localhost:8093/api/events/booking/123/time-travel?upToTime=2024-11-09T10:00:30"
```

### Get Event Timeline

```bash
# Get complete event timeline
curl http://localhost:8093/api/events/booking/123/timeline
```

### Get Events by Type

```bash
# Get only cancellation events
curl http://localhost:8093/api/events/booking/123/type/BOOKING_CANCELLED
```

---

## 🔄 Event Replay

### How It Works

1. **Load Events**: Get all events for aggregate, ordered by timestamp
2. **Apply Events**: Apply each event in sequence to rebuild state
3. **Return State**: Return the reconstructed aggregate

### Example Replay

```java
// Replay events
List<BookingEvent> events = eventStore.getEventStream("123");

SimpleBooking booking = new SimpleBooking();
for (BookingEvent event : events) {
    booking = applyEvent(booking, event);
}
```

### Event Application

```java
switch (event.getEventType()) {
    case "BOOKING_CREATED":
        booking.setUserId(eventData.get("userId"));
        booking.setTrainId(eventData.get("trainId"));
        booking.setStatus("PENDING");
        break;
    case "BOOKING_CONFIRMED":
        booking.setStatus("CONFIRMED");
        break;
    case "BOOKING_CANCELLED":
        booking.setStatus("CANCELLED");
        break;
}
```

---

## 📈 Benefits

### 1. **Complete Audit Trail**

Every change is recorded:
- Who made the change
- When it was made
- What changed
- Why it changed (correlation ID)

### 2. **Event Replay**

Rebuild state from events:
- Debugging: See how state evolved
- Recovery: Rebuild after data loss
- Testing: Replay production events

### 3. **Time-Travel Debugging**

See state at any point in time:
- Investigate issues at specific times
- Understand state evolution
- Debug complex scenarios

### 4. **Event-Driven Architecture**

Natural fit for microservices:
- Events can trigger other services
- Loose coupling between services
- Better scalability

---

## 🎯 Best Practices

### 1. **Immutable Events**

Events should never be modified:
- Append-only event store
- Events are facts, not opinions
- Version events for schema changes

### 2. **Event Versioning**

Handle schema evolution:

```java
event.setVersion("1.0");

// Later, when schema changes
if (event.getVersion().equals("1.0")) {
    // Handle old format
} else if (event.getVersion().equals("2.0")) {
    // Handle new format
}
```

### 3. **Event Data Structure**

Store complete event data:

```json
{
  "bookingId": 123,
  "userId": 1,
  "trainId": 1,
  "pnrNumber": "ABC123XYZ",
  "totalFare": 1000.0,
  "status": "CONFIRMED",
  "bookingTime": "2024-11-09T10:00:00"
}
```

### 4. **Correlation IDs**

Link events across services:

```java
String correlationId = UUID.randomUUID().toString();
// Use same correlationId for all related events
```

### 5. **Event Snapshots**

For performance, create snapshots:

```java
// Store snapshot every N events
if (eventCount % 100 == 0) {
    createSnapshot(aggregateId, currentState);
}
```

---

## 🐛 Troubleshooting

### Issue: Event Replay Too Slow

**Symptoms**: Replay takes too long for aggregates with many events

**Solutions**:
1. Implement snapshots
2. Cache replayed state
3. Optimize event queries with indexes

### Issue: Event Data Schema Changed

**Symptoms**: Old events can't be deserialized

**Solutions**:
1. Use event versioning
2. Implement migration logic
3. Support multiple schema versions

### Issue: Missing Events

**Symptoms**: State doesn't match after replay

**Solutions**:
1. Verify event storage is transactional
2. Check for event loss in async operations
3. Implement event validation

---

## 📊 Monitoring

### Event Metrics

Track event sourcing metrics:

- **Events Stored**: Total events stored
- **Replay Duration**: Time to replay events
- **Event Count per Aggregate**: Average events per aggregate
- **Event Store Size**: Total storage used

### Prometheus Metrics

```java
// Event metrics
events_stored_total{service="booking", event_type="BOOKING_CREATED"}
events_replayed_total{service="booking"}
event_replay_duration_seconds{service="booking"}
```

---

## 🔄 Integration with Other Patterns

### Event Sourcing + Saga Pattern

Events can trigger saga steps:

```java
@KafkaListener(topics = "booking-events")
public void handleBookingEvent(BookingEvent event) {
    if (event.getEventType().equals("BOOKING_CREATED")) {
        sagaOrchestrator.startBookingSaga(event);
    }
}
```

### Event Sourcing + CQRS

- **Write Side**: Store events
- **Read Side**: Project events to read models

---

## 📚 Additional Resources

- [Event Sourcing Pattern](https://martinfowler.com/eaaDev/EventSourcing.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Store](https://eventstore.org/)

---

## 🎯 Summary

Event Sourcing provides:

✅ **Complete Audit Trail**: Every change recorded  
✅ **Event Replay**: Rebuild state from events  
✅ **Time-Travel Debugging**: See state at any time  
✅ **Better Consistency**: Immutable events  
✅ **Event-Driven**: Natural microservices fit  

---

*Last Updated: November 2025*

