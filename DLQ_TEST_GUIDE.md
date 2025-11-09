# DLQ Testing Guide

## Quick Test Commands

### 1. Check DLQ Statistics
```bash
# Get all DLQ statistics
curl http://localhost:8095/api/dlq/stats

# Get specific DLQ topic statistics
curl http://localhost:8095/api/dlq/stats/booking-created.DLT
```

### 2. Inspect DLQ Messages
```bash
curl "http://localhost:8095/api/dlq/inspect/booking-created.DLT?maxMessages=10"
```

### 3. Reprocess DLQ Messages
```bash
curl -X POST http://localhost:8095/api/dlq/reprocess \
  -H "Content-Type: application/json" \
  -d '{
    "dltTopic": "booking-created.DLT",
    "mainTopic": "booking-created",
    "maxRecords": 10
  }'
```

### 4. Run Comprehensive Test
```bash
./test-dlq-comprehensive.sh
```

## Simulating DLQ Messages

To test DLQ functionality, you need to create a scenario where message processing fails:

### Option 1: Create Invalid Event
Send a malformed event to a Kafka topic that the notification service consumes.

### Option 2: Temporarily Break Consumer
Modify the consumer to throw an exception for testing.

### Option 3: Use Existing Failures
If there are already failed messages, they should be in DLQ.

## Expected Behavior

1. **Message Processing Fails**: Consumer throws exception
2. **Retry Logic**: System retries 3 times with 1s delay
3. **DLQ Routing**: After retries, message routed to `<topic>.DLT`
4. **Monitoring**: DLQ statistics show message count
5. **Alerting**: Alert triggered if threshold exceeded
6. **Reprocessing**: Messages can be reprocessed via API

## Verification Checklist

- [ ] Service is running and healthy
- [ ] DLQ API endpoints are accessible
- [ ] DLQ statistics API returns data
- [ ] DLQ message inspection works
- [ ] DLQ reprocessing works
- [ ] Prometheus metrics are exposed
- [ ] Alerting is configured

## Troubleshooting

### No DLQ Messages
- This is normal if no processing failures occurred
- To test, simulate a failure scenario

### DLQ API Returns 404
- Ensure notification service is running
- Check service logs for errors

### Reprocessing Fails
- Verify main topic exists
- Check Kafka connectivity
- Review service logs

