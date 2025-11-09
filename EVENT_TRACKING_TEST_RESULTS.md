# 🧪 Event Tracking Test Results

## Test Execution Summary

### Test Date
**Date**: $(date)

### Test Environment
- **Booking Service**: http://localhost:8093
- **Database**: H2 (in-memory)
- **Kafka**: Required for event publishing

---

## Test Scenarios

### ✅ 1. Service Health Check
- **Status**: Service must be running
- **Endpoint**: `/actuator/health`
- **Expected**: HTTP 200

### ✅ 2. Event Production Tracking
- **Test**: Create a booking to trigger event production
- **Expected**: 
  - Event logged in `event_production_log` table
  - Status: PENDING → PUBLISHING → PUBLISHED
  - Event ID extracted from event object (UUID)

### ✅ 3. Event Consumption Tracking
- **Test**: Events consumed by other services
- **Expected**:
  - Event logged in `event_consumption_log` table
  - Status: RECEIVED → PROCESSING → PROCESSED
  - Idempotency check prevents duplicates

### ✅ 4. REST API Endpoints
- **Endpoints to Test**:
  - `GET /api/event-tracking/stats` - Statistics
  - `GET /api/event-tracking/production/status/{status}` - Production logs
  - `GET /api/event-tracking/consumption/status/{status}` - Consumption logs
  - `GET /api/event-tracking/production/event/{eventId}` - Get by event ID
  - `GET /api/event-tracking/consumption/event/{eventId}` - Get by event ID
  - `GET /api/event-tracking/production/topic/{topic}` - Get by topic
  - `GET /api/event-tracking/production/failed` - Failed events
  - `GET /api/event-tracking/consumption/failed` - Failed events

### ✅ 5. Idempotency Check
- **Test**: Process same event twice
- **Expected**: Second attempt should be skipped

### ✅ 6. Error Handling
- **Test**: Failed event publishing/consumption
- **Expected**: 
  - Error logged with stack trace
  - Retry count incremented
  - Status updated to FAILED after max retries

---

## Manual Testing Steps

### Step 1: Start Services
```bash
./start-microservices.sh
```

### Step 2: Create a Booking
```bash
curl -X POST http://localhost:8093/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "trainId": 1,
    "totalFare": 1000.0,
    "status": "CONFIRMED"
  }'
```

### Step 3: Check Production Logs
```bash
curl http://localhost:8093/api/event-tracking/production/status/PUBLISHED | jq '.'
```

### Step 4: Check Statistics
```bash
curl http://localhost:8093/api/event-tracking/stats | jq '.'
```

### Step 5: Verify Database
```sql
-- Check production logs
SELECT * FROM event_production_log ORDER BY created_at DESC LIMIT 10;

-- Check consumption logs
SELECT * FROM event_consumption_log ORDER BY received_at DESC LIMIT 10;

-- Count by status
SELECT status, COUNT(*) FROM event_production_log GROUP BY status;
SELECT status, COUNT(*) FROM event_consumption_log GROUP BY status;
```

---

## Expected Results

### Production Logs
- Events should have:
  - `event_id` (UUID from event)
  - `service_name` = "booking-service"
  - `topic` = event topic name
  - `event_type` = extracted from event
  - `status` = PUBLISHED (after successful publish)
  - `partition_number` and `offset` (after publish)

### Consumption Logs
- Events should have:
  - `event_id` (UUID from event)
  - `service_name` = consuming service name
  - `topic`, `partition_number`, `offset`
  - `consumer_group` = consumer group name
  - `status` = PROCESSED (after successful processing)
  - `processing_time_ms` = time taken

### Statistics
```json
{
  "production": {
    "PENDING": 0,
    "PUBLISHING": 0,
    "PUBLISHED": <count>,
    "FAILED": 0
  },
  "consumption": {
    "RECEIVED": 0,
    "PROCESSING": 0,
    "PROCESSED": <count>,
    "FAILED": 0
  }
}
```

---

## Troubleshooting

### Issue: No events in logs
**Solution**: 
- Check if Kafka is running
- Verify events are being published
- Check application logs for errors

### Issue: Events stuck in PENDING
**Solution**:
- Check Kafka connectivity
- Verify KafkaTemplate is configured
- Check for network issues

### Issue: Idempotency not working
**Solution**:
- Verify eventId is present in events
- Check database for duplicate event IDs
- Verify isEventProcessed() is being called

---

## Test Results Template

```
Test Date: [Date]
Tester: [Name]

✅ Service Health: [PASS/FAIL]
✅ Event Production: [PASS/FAIL]
✅ Event Consumption: [PASS/FAIL]
✅ REST API Endpoints: [PASS/FAIL]
✅ Idempotency: [PASS/FAIL]
✅ Error Handling: [PASS/FAIL]

Notes:
[Any observations or issues]
```

---

*Last Updated: November 2025*

