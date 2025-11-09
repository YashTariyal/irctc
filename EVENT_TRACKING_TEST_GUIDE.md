# 🧪 Event Tracking Test Guide

## ✅ Implementation Complete

Event tracking has been implemented for **Booking Service** with:
- ✅ Event Production Logging
- ✅ Event Consumption Logging
- ✅ Idempotency Checks
- ✅ REST API Endpoints for Monitoring

---

## 🧪 Testing the Implementation

### 1. **Start the Services**

```bash
# Start all microservices
./start-microservices.sh

# Or start individually:
# - Eureka Server (port 8761)
# - Booking Service (port 8093)
# - Kafka (if not running)
```

### 2. **Test Event Production**

#### Option A: Create a Booking (triggers event production)

```bash
# Create a booking
curl -X POST http://localhost:8093/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "totalFare": 1000.0,
    "status": "CONFIRMED"
  }'
```

This will:
1. Create the booking
2. Trigger saga orchestrator
3. Publish events using `TrackedEventPublisher`
4. Events will be logged in `event_production_log` table

#### Option B: Check Production Logs

```bash
# Get all production logs
curl http://localhost:8093/api/event-tracking/production/status/PUBLISHED | jq '.'

# Get pending events
curl http://localhost:8093/api/event-tracking/production/status/PENDING | jq '.'

# Get failed events
curl http://localhost:8093/api/event-tracking/production/failed | jq '.'
```

### 3. **Test Event Consumption**

Events consumed by other services (e.g., Notification Service) will be automatically logged.

```bash
# Get consumption logs
curl http://localhost:8093/api/event-tracking/consumption/status/PROCESSED | jq '.'

# Get received events
curl http://localhost:8093/api/event-tracking/consumption/status/RECEIVED | jq '.'
```

### 4. **Test Event Tracking Statistics**

```bash
# Get overall statistics
curl http://localhost:8093/api/event-tracking/stats | jq '.'
```

**Expected Response:**
```json
{
  "production": {
    "PENDING": 0,
    "PUBLISHING": 0,
    "PUBLISHED": 5,
    "FAILED": 0
  },
  "consumption": {
    "RECEIVED": 0,
    "PROCESSING": 0,
    "PROCESSED": 3,
    "FAILED": 0
  }
}
```

### 5. **Test Idempotency**

```bash
# Get event by ID
curl http://localhost:8093/api/event-tracking/consumption/event/{eventId} | jq '.'

# Try to process same event twice - should be skipped
```

### 6. **Test Correlation ID Tracking**

```bash
# Get events by correlation ID
curl http://localhost:8093/api/event-tracking/correlation/{correlationId} | jq '.'
```

---

## 📊 Monitoring Endpoints

### Production Logs

| Endpoint | Description |
|----------|-------------|
| `GET /api/event-tracking/production/status/{status}` | Get production logs by status |
| `GET /api/event-tracking/production/event/{eventId}` | Get production log by event ID |
| `GET /api/event-tracking/production/topic/{topic}` | Get production logs by topic |
| `GET /api/event-tracking/production/failed` | Get failed production events |

### Consumption Logs

| Endpoint | Description |
|----------|-------------|
| `GET /api/event-tracking/consumption/status/{status}` | Get consumption logs by status |
| `GET /api/event-tracking/consumption/event/{eventId}` | Get consumption log by event ID |
| `GET /api/event-tracking/consumption/failed` | Get failed consumption events |

### Statistics

| Endpoint | Description |
|----------|-------------|
| `GET /api/event-tracking/stats` | Overall statistics |
| `GET /api/event-tracking/correlation/{correlationId}` | Get events by correlation ID |

---

## 🔍 Database Verification

### Check Production Logs

```sql
-- View all production logs
SELECT * FROM event_production_log ORDER BY created_at DESC LIMIT 10;

-- Count by status
SELECT status, COUNT(*) FROM event_production_log GROUP BY status;

-- View failed events
SELECT * FROM event_production_log WHERE status = 'FAILED';
```

### Check Consumption Logs

```sql
-- View all consumption logs
SELECT * FROM event_consumption_log ORDER BY received_at DESC LIMIT 10;

-- Count by status
SELECT status, COUNT(*) FROM event_consumption_log GROUP BY status;

-- View failed events
SELECT * FROM event_consumption_log WHERE status = 'FAILED';

-- Average processing time
SELECT AVG(processing_time_ms) FROM event_consumption_log 
WHERE status = 'PROCESSED';
```

---

## 🎯 Expected Behavior

### Event Production Flow

1. **Service Operation** → Creates event object
2. **TrackedEventPublisher.publishEvent()** → Logs to `event_production_log` (PENDING)
3. **KafkaTemplate.send()** → Publishes to Kafka
4. **Success** → Status updated to PUBLISHED (with partition/offset)
5. **Failure** → Status updated to FAILED (or PENDING for retry)

### Event Consumption Flow

1. **@KafkaListener** → Receives event from Kafka
2. **EventTrackingService.logEventConsumption()** → Logs to `event_consumption_log` (RECEIVED)
3. **Idempotency Check** → Checks if already processed
4. **Mark Processing** → Status updated to PROCESSING
5. **Process Event** → Business logic execution
6. **Success** → Status updated to PROCESSED (with processing time)
7. **Failure** → Status updated to FAILED (or RECEIVED for retry)

---

## 🐛 Troubleshooting

### Issue: Events not being logged

**Check:**
1. Is `TrackedEventPublisher` being used instead of `KafkaTemplate`?
2. Are events being published? Check Kafka topics
3. Check application logs for errors

### Issue: Idempotency not working

**Check:**
1. Is `eventId` present in event objects?
2. Is `isEventProcessed()` being called?
3. Check database for duplicate event IDs

### Issue: Status not updating

**Check:**
1. Are async callbacks completing?
2. Check for transaction issues
3. Verify repository methods are being called

---

## 📝 Test Script

Use the provided test script:

```bash
./test-event-tracking.sh
```

This will:
- Get event tracking statistics
- Show production logs
- Show consumption logs
- Show failed events

---

## ✅ Verification Checklist

- [ ] Production logs are created when events are published
- [ ] Consumption logs are created when events are consumed
- [ ] Status transitions work correctly (PENDING → PUBLISHED, etc.)
- [ ] Idempotency prevents duplicate processing
- [ ] Failed events are logged with error details
- [ ] REST endpoints return correct data
- [ ] Statistics endpoint shows accurate counts
- [ ] Correlation ID tracking works across services

---

*Last Updated: November 2025*

