# 📊 Event Tracking System - Design Discussion

## 🎯 Overview

This document discusses the design for tracking event production and consumption across all microservices. This will provide complete observability and reliability for event-driven operations.

---

## 🏗️ Proposed Architecture

### Two-Table Design

```
┌─────────────────────────────────────┐
│    Event Production Tracking        │
│    (event_production_log)           │
│                                     │
│  - Track events being PUBLISHED     │
│  - Status: PENDING → PUBLISHED      │
│  - Retry mechanism                  │
│  - Error tracking                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│    Event Consumption Tracking       │
│    (event_consumption_log)          │
│                                     │
│  - Track events being CONSUMED      │
│  - Status: RECEIVED → PROCESSED     │
│  - Idempotency check                │
│  - Processing time                  │
└─────────────────────────────────────┘
```

---

## 📋 Table Design

### 1. Event Production Log Table

**Purpose**: Track all events being produced/published to Kafka

**Fields**:
```sql
CREATE TABLE event_production_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,        -- Unique event identifier
  service_name VARCHAR(100) NOT NULL,          -- Which service produced it
  topic VARCHAR(100) NOT NULL,                  -- Kafka topic
  event_key VARCHAR(255),                      -- Kafka message key
  event_type VARCHAR(100) NOT NULL,           -- Event type (BOOKING_CREATED, etc.)
  payload TEXT NOT NULL,                        -- Event payload (JSON)
  status VARCHAR(50) NOT NULL,                 -- PENDING, PUBLISHED, FAILED
  retry_count INT NOT NULL DEFAULT 0,
  max_retries INT NOT NULL DEFAULT 3,
  error_message TEXT,                           -- Error if failed
  correlation_id VARCHAR(100),                 -- For tracing
  partition_number INT,                         -- Kafka partition
  offset BIGINT,                                -- Kafka offset (after publish)
  published_at TIMESTAMP,                       -- When successfully published
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  metadata TEXT                                 -- Additional metadata (JSON)
);
```

**Status Flow**:
```
PENDING → PUBLISHING → PUBLISHED
    │
    └─→ FAILED (with retry)
```

**Indexes**:
- `idx_prod_status_created` (status, created_at)
- `idx_prod_topic` (topic, created_at)
- `idx_prod_correlation` (correlation_id)
- `idx_prod_service` (service_name, status)

---

### 2. Event Consumption Log Table

**Purpose**: Track all events being consumed/processed from Kafka

**Fields**:
```sql
CREATE TABLE event_consumption_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_id VARCHAR(100) NOT NULL UNIQUE,        -- Unique event identifier
  service_name VARCHAR(100) NOT NULL,          -- Which service consumed it
  topic VARCHAR(100) NOT NULL,                  -- Kafka topic
  partition_number INT NOT NULL,                -- Kafka partition
  offset BIGINT NOT NULL,                        -- Kafka offset
  consumer_group VARCHAR(100) NOT NULL,         -- Consumer group
  event_type VARCHAR(100) NOT NULL,             -- Event type
  payload TEXT NOT NULL,                        -- Event payload (JSON)
  status VARCHAR(50) NOT NULL,                  -- RECEIVED, PROCESSING, PROCESSED, FAILED
  retry_count INT NOT NULL DEFAULT 0,
  max_retries INT NOT NULL DEFAULT 3,
  processing_time_ms BIGINT,                    -- Time taken to process
  error_message TEXT,                           -- Error if failed
  error_stack_trace TEXT,                       -- Full stack trace
  correlation_id VARCHAR(100),                 -- For tracing
  received_at TIMESTAMP NOT NULL,               -- When received from Kafka
  processed_at TIMESTAMP,                        -- When successfully processed
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  metadata TEXT                                 -- Additional metadata (JSON)
);
```

**Status Flow**:
```
RECEIVED → PROCESSING → PROCESSED
    │
    └─→ FAILED (with retry)
```

**Indexes**:
- `idx_cons_status_created` (status, created_at)
- `idx_cons_topic_partition_offset` (topic, partition_number, offset)
- `idx_cons_consumer_group` (consumer_group, status)
- `idx_cons_correlation` (correlation_id)
- `idx_cons_service` (service_name, status)
- `idx_cons_event_id` (event_id) -- For idempotency checks

---

## 🔄 Integration Points

### Production Side (Publisher)

**Current Flow**:
```
Service Operation
    ↓
KafkaTemplate.send()
    ↓
Event Published (or failed)
```

**Enhanced Flow with Tracking**:
```
Service Operation
    ↓
Save to event_production_log (PENDING)
    ↓
KafkaTemplate.send()
    ↓
Update status to PUBLISHED (or FAILED)
    ↓
Retry if failed
```

**Example**:
```java
@Service
public class TrackedEventPublisher {
    
    public void publishEvent(String topic, Object event, String eventId) {
        // 1. Save to production log (PENDING)
        EventProductionLog log = saveProductionLog(topic, event, eventId);
        
        try {
            // 2. Publish to Kafka
            kafkaTemplate.send(topic, eventId, event)
                .whenComplete((result, error) -> {
                    if (error == null) {
                        // 3. Update status to PUBLISHED
                        updateProductionLog(log.getId(), "PUBLISHED", result);
                    } else {
                        // 4. Update status to FAILED and retry
                        handlePublishFailure(log, error);
                    }
                });
        } catch (Exception e) {
            updateProductionLog(log.getId(), "FAILED", e.getMessage());
        }
    }
}
```

---

### Consumption Side (Consumer)

**Current Flow**:
```
@KafkaListener receives event
    ↓
Process event
    ↓
Success or failure
```

**Enhanced Flow with Tracking**:
```
@KafkaListener receives event
    ↓
Check idempotency (event_consumption_log)
    ↓
Save to event_consumption_log (RECEIVED)
    ↓
Update status to PROCESSING
    ↓
Process event
    ↓
Update status to PROCESSED (or FAILED)
    ↓
Retry if failed
```

**Example**:
```java
@Service
public class TrackedEventConsumer {
    
    @KafkaListener(topics = "booking-confirmed")
    public void handleBookingConfirmed(BookingEvent event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                       @Header(KafkaHeaders.OFFSET) long offset) {
        
        String eventId = extractEventId(event);
        
        // 1. Check idempotency
        if (isAlreadyProcessed(eventId)) {
            logger.warn("Event already processed: {}", eventId);
            return;
        }
        
        // 2. Save to consumption log (RECEIVED)
        EventConsumptionLog log = saveConsumptionLog(topic, partition, offset, event, eventId);
        
        try {
            // 3. Update status to PROCESSING
            updateConsumptionLog(log.getId(), "PROCESSING");
            
            long startTime = System.currentTimeMillis();
            
            // 4. Process event
            processBookingConfirmed(event);
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // 5. Update status to PROCESSED
            updateConsumptionLog(log.getId(), "PROCESSED", processingTime);
            
        } catch (Exception e) {
            // 6. Update status to FAILED and retry
            handleConsumptionFailure(log, e);
        }
    }
}
```

---

## 🎯 Benefits

### 1. **Complete Observability**

- See all events being produced
- See all events being consumed
- Track success/failure rates
- Monitor processing times

### 2. **Idempotency**

- Prevent duplicate processing
- Check if event already consumed
- Handle retries safely

### 3. **Reliability**

- Track failed events
- Automatic retry mechanism
- Dead letter queue integration
- Manual reprocessing capability

### 4. **Debugging**

- Full audit trail
- See event flow
- Identify bottlenecks
- Trace correlation IDs

### 5. **Monitoring**

- Event production rate
- Event consumption rate
- Failure rates
- Processing latency
- Backlog monitoring

---

## 📊 Use Cases

### 1. **Idempotency Check**

```java
// Before processing
if (consumptionLogRepository.existsByEventIdAndStatus(
    eventId, "PROCESSED")) {
    logger.info("Event already processed: {}", eventId);
    return; // Skip processing
}
```

### 2. **Retry Failed Events**

```java
// Find failed events
List<EventConsumptionLog> failedEvents = 
    consumptionLogRepository.findByStatusAndRetryCountLessThan(
        "FAILED", maxRetries);

for (EventConsumptionLog log : failedEvents) {
    retryEvent(log);
}
```

### 3. **Monitor Event Backlog**

```java
// Count pending events
long pendingCount = productionLogRepository.countByStatus("PENDING");
long processingCount = consumptionLogRepository.countByStatus("PROCESSING");

// Alert if backlog is high
if (pendingCount > threshold) {
    alert("High event production backlog");
}
```

### 4. **Event Replay**

```java
// Replay events from a specific time
List<EventConsumptionLog> events = 
    consumptionLogRepository.findByTopicAndReceivedAtBetween(
        topic, startTime, endTime);

for (EventConsumptionLog log : events) {
    replayEvent(log);
}
```

---

## 🔧 Implementation Considerations

### 1. **Performance**

- **Indexes**: Critical for fast lookups
- **Partitioning**: Consider partitioning by date for large tables
- **Archival**: Archive old logs periodically

### 2. **Idempotency Key**

- Use event ID from event payload
- Or generate from topic + partition + offset
- Ensure uniqueness

### 3. **Transaction Management**

- Production log: Save before publishing (transactional)
- Consumption log: Save after receiving (before processing)

### 4. **Error Handling**

- Store full error details
- Include stack traces for debugging
- Track retry attempts

### 5. **Correlation IDs**

- Link production and consumption logs
- Track events across services
- Full request trace

---

## 📈 Metrics to Track

### Production Metrics
- Events published per minute
- Events failed per minute
- Average publish latency
- Retry rate

### Consumption Metrics
- Events consumed per minute
- Events processed per minute
- Events failed per minute
- Average processing time
- Processing backlog

### Service-Level Metrics
- Events per service
- Success rate per service
- Error rate per service

---

## 🎯 Questions for Discussion

1. **Table Location**: 
   - Single shared table per service?
   - Or separate tables per service?

2. **Event ID Strategy**:
   - Use UUID from event?
   - Or generate from topic+partition+offset?

3. **Retention Policy**:
   - How long to keep logs?
   - Archive strategy?

4. **Performance**:
   - Expected event volume?
   - Need partitioning?

5. **Idempotency**:
   - Check on every consumption?
   - Or only for critical events?

6. **Integration**:
   - Replace existing OutboxEvent?
   - Or use alongside?

---

## 💡 Recommendations

### Option 1: Enhanced Outbox Pattern
- Extend existing `outbox_events` table
- Add consumption tracking separately
- **Pros**: Reuse existing infrastructure
- **Cons**: Mixing concerns

### Option 2: Separate Tables (Recommended)
- `event_production_log` for publishing
- `event_consumption_log` for consumption
- **Pros**: Clear separation, better queries
- **Cons**: More tables to manage

### Option 3: Unified Event Log
- Single table for both production and consumption
- Use `log_type` field (PRODUCTION/CONSUMPTION)
- **Pros**: Single source of truth
- **Cons**: More complex queries

---

## 🚀 Next Steps

1. **Decide on table design** (Option 2 recommended)
2. **Define event ID strategy**
3. **Create entities and repositories**
4. **Implement tracked publishers**
5. **Implement tracked consumers**
6. **Add monitoring dashboards**
7. **Create retry mechanisms**

---

*What are your thoughts on this design? Should we proceed with Option 2 (separate tables)?*

