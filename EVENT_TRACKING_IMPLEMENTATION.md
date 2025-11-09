# 📊 Event Tracking Implementation - Progress

## ✅ Completed for Booking Service

### 1. **Entities Created**
- ✅ `EventProductionLog` - Tracks event publishing
- ✅ `EventConsumptionLog` - Tracks event consumption

### 2. **Repositories Created**
- ✅ `EventProductionLogRepository` - CRUD + query methods
- ✅ `EventConsumptionLogRepository` - CRUD + query methods + idempotency checks

### 3. **Services Created**
- ✅ `EventTrackingService` - Core tracking logic
  - Extract eventId from events (using reflection)
  - Extract eventType from events
  - Extract correlationId from events
  - Log event production
  - Log event consumption
  - Mark events as published/processed/failed
  - Idempotency checks

- ✅ `TrackedEventPublisher` - Wrapper for KafkaTemplate
  - Automatically logs production before publishing
  - Updates status after publish success/failure

### 4. **Database Migration**
- ✅ `V4__Create_event_tracking_tables.sql`
  - `event_production_log` table with indexes
  - `event_consumption_log` table with indexes

---

## 🔄 Next Steps

### For Booking Service:
1. **Integrate TrackedEventPublisher** - Replace direct KafkaTemplate usage
2. **Create TrackedEventConsumer** - Wrapper for @KafkaListener
3. **Add monitoring endpoints** - REST APIs to query event logs

### For Other Services:
1. **Payment Service** - Copy same pattern
2. **Notification Service** - Copy same pattern (consumption only)
3. **User Service** - Copy same pattern (production only)

---

## 📋 Implementation Pattern

### Production Tracking Flow:
```
Service Operation
    ↓
TrackedEventPublisher.publishEvent()
    ↓
EventTrackingService.logEventProduction() → event_production_log (PENDING)
    ↓
KafkaTemplate.send()
    ↓
Success → markEventPublished() → status = PUBLISHED
Failure → markEventPublishFailed() → status = FAILED (or PENDING for retry)
```

### Consumption Tracking Flow:
```
@KafkaListener receives event
    ↓
EventTrackingService.logEventConsumption() → event_consumption_log (RECEIVED)
    ↓
Check idempotency (isEventProcessed?)
    ↓
markEventProcessing() → status = PROCESSING
    ↓
Process event
    ↓
Success → markEventProcessed() → status = PROCESSED
Failure → markEventConsumptionFailed() → status = FAILED (or RECEIVED for retry)
```

---

## 🎯 Key Features Implemented

### ✅ Event ID Extraction
- Uses reflection to extract `eventId` from event objects
- All events in `irctc-shared-events` already have `eventId` field (UUID)

### ✅ Idempotency
- `isEventProcessed()` checks if event already processed
- Prevents duplicate processing

### ✅ Status Tracking
- **Production**: PENDING → PUBLISHING → PUBLISHED / FAILED
- **Consumption**: RECEIVED → PROCESSING → PROCESSED / FAILED

### ✅ Retry Logic
- Automatic retry for failed events
- Configurable max retries (default: 3)

### ✅ Error Tracking
- Full error messages
- Stack traces for consumption failures
- Kafka partition/offset tracking

### ✅ Correlation ID Support
- Extracts correlationId from events
- Links events across services

---

## 📊 Database Schema

### event_production_log
- `event_id` (UUID from event) - UNIQUE
- `service_name` - Which service produced it
- `topic` - Kafka topic
- `event_key` - Kafka message key
- `event_type` - Event type
- `payload` - Full event JSON
- `status` - PENDING, PUBLISHING, PUBLISHED, FAILED
- `retry_count` - Current retry count
- `max_retries` - Max retries allowed
- `partition_number` - Kafka partition (after publish)
- `offset` - Kafka offset (after publish)
- `correlation_id` - For tracing
- `metadata` - Additional JSON metadata

### event_consumption_log
- `event_id` (UUID from event) - UNIQUE
- `service_name` - Which service consumed it
- `topic` - Kafka topic
- `partition_number` - Kafka partition
- `offset` - Kafka offset
- `consumer_group` - Consumer group
- `event_type` - Event type
- `payload` - Full event JSON
- `status` - RECEIVED, PROCESSING, PROCESSED, FAILED
- `retry_count` - Current retry count
- `max_retries` - Max retries allowed
- `processing_time_ms` - Time taken to process
- `error_message` - Error message if failed
- `error_stack_trace` - Full stack trace if failed
- `correlation_id` - For tracing
- `metadata` - Additional JSON metadata

---

## 🔧 Usage Examples

### Publishing Events (Production)
```java
@Autowired
private TrackedEventPublisher trackedPublisher;

// Publish event with automatic tracking
trackedPublisher.publishEvent("booking-events", bookingEvent)
    .whenComplete((result, error) -> {
        if (error == null) {
            logger.info("Event published successfully");
        } else {
            logger.error("Event publish failed", error);
        }
    });
```

### Consuming Events (Consumption)
```java
@KafkaListener(topics = "booking-events", groupId = "notification-service")
public void handleBookingEvent(BookingEvent event,
                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                               @Header(KafkaHeaders.OFFSET) long offset) {
    
    // Log consumption (automatically checks idempotency)
    EventConsumptionLog log = trackingService.logEventConsumption(
        topic, partition, offset, "notification-service", event
    );
    
    // Check if already processed
    if (trackingService.isEventProcessed(trackingService.extractEventId(event))) {
        logger.warn("Event already processed, skipping");
        return;
    }
    
    long startTime = System.currentTimeMillis();
    
    try {
        // Mark as processing
        trackingService.markEventProcessing(log.getId());
        
        // Process event
        processBookingEvent(event);
        
        // Mark as processed
        long processingTime = System.currentTimeMillis() - startTime;
        trackingService.markEventProcessed(log.getId(), processingTime);
        
    } catch (Exception e) {
        // Mark as failed
        trackingService.markEventConsumptionFailed(log.getId(), e);
        throw e; // Re-throw for Kafka retry
    }
}
```

---

## 📈 Monitoring Queries

### Production Metrics
```sql
-- Count events by status
SELECT status, COUNT(*) FROM event_production_log GROUP BY status;

-- Failed events
SELECT * FROM event_production_log WHERE status = 'FAILED';

-- Events by topic
SELECT topic, COUNT(*) FROM event_production_log GROUP BY topic;

-- Events by service
SELECT service_name, status, COUNT(*) 
FROM event_production_log 
GROUP BY service_name, status;
```

### Consumption Metrics
```sql
-- Count events by status
SELECT status, COUNT(*) FROM event_consumption_log GROUP BY status;

-- Failed events
SELECT * FROM event_consumption_log WHERE status = 'FAILED';

-- Average processing time
SELECT AVG(processing_time_ms) FROM event_consumption_log 
WHERE status = 'PROCESSED';

-- Events by consumer group
SELECT consumer_group, status, COUNT(*) 
FROM event_consumption_log 
GROUP BY consumer_group, status;
```

---

## 🚀 Next Implementation Steps

1. **Create TrackedEventConsumer** - AOP or wrapper for @KafkaListener
2. **Add REST endpoints** - Query event logs
3. **Replicate for Payment Service** - Same pattern
4. **Replicate for Notification Service** - Consumption only
5. **Replicate for User Service** - Production only
6. **Add monitoring dashboard** - Grafana panels for event metrics

---

*Last Updated: November 2025*

