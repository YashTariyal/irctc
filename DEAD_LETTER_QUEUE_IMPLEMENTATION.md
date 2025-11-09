# Dead Letter Queue (DLQ) Implementation Guide

## Overview

The Dead Letter Queue (DLQ) system ensures that failed Kafka messages are not lost and can be reprocessed. This implementation provides comprehensive DLQ management including monitoring, alerting, and reprocessing capabilities.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Main Topic    │───▶│  Consumer       │───▶│  Processing     │
│  (e.g.,        │    │  (Retry 3x)     │    │  (Success)      │
│   booking-      │    │                 │    │                 │
│   created)      │    └─────────────────┘    └─────────────────┘
└─────────────────┘           │
                              │ (After 3 retries)
                              ▼
                     ┌─────────────────┐
                     │   DLQ Topic     │
                     │  (booking-      │
                     │   created.DLT)  │
                     └─────────────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  DLQ Management │
                     │  - Monitoring   │
                     │  - Alerting     │
                     │  - Reprocessing │
                     └─────────────────┘
```

## Features

### 1. Automatic DLQ Routing
- Failed messages are automatically routed to `<topic>.DLT` (Dead Letter Topic)
- Retry logic: 3 retries with 1-second delay before routing to DLQ
- Non-retryable exceptions (IllegalArgumentException, NullPointerException) skip retries

### 2. DLQ Monitoring
- Real-time DLQ statistics (message count, partition count)
- Prometheus metrics integration
- REST API for querying DLQ status

### 3. DLQ Alerting
- Automatic monitoring every 5 minutes
- Alerts when DLQ exceeds threshold (default: 10 messages)
- Cooldown period to prevent alert spam (1 hour)

### 4. DLQ Reprocessing
- Manual reprocessing of DLQ messages back to main topic
- Message inspection without consumption
- Batch reprocessing with configurable limits

## Configuration

### Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service
      auto-offset-reset: earliest
      enable-auto-commit: false
    producer:
      acks: all
      retries: 3

# DLQ Configuration
dlq:
  alerting:
    enabled: true
    threshold: 10  # Alert if DLQ has more than 10 messages
    check-interval: 300000  # Check every 5 minutes
```

### Error Handler Configuration

```java
@Bean
public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, ex) -> {
            String dltTopic = record.topic() + ".DLT";
            return new TopicPartition(dltTopic, record.partition());
        }
    );
    
    DefaultErrorHandler handler = new DefaultErrorHandler(
        recoverer,
        new FixedBackOff(1000L, 3) // 1 second delay, 3 retries
    );
    
    handler.addNotRetryableExceptions(
        IllegalArgumentException.class,
        NullPointerException.class
    );
    
    return handler;
}
```

## API Endpoints

### 1. Get DLQ Statistics
```http
GET /api/dlq/stats/{dltTopic}
```

**Response:**
```json
{
  "topic": "booking-created.DLT",
  "messageCount": 5,
  "partitionCount": 1,
  "timestamp": "2025-11-09T18:30:00",
  "error": null
}
```

### 2. Get All DLQ Statistics
```http
GET /api/dlq/stats
```

**Response:**
```json
{
  "booking-created.DLT": {
    "topic": "booking-created.DLT",
    "messageCount": 5,
    "partitionCount": 1
  },
  "payment-initiated.DLT": {
    "topic": "payment-initiated.DLT",
    "messageCount": 2,
    "partitionCount": 1
  }
}
```

### 3. Reprocess DLQ Messages
```http
POST /api/dlq/reprocess
Content-Type: application/json

{
  "dltTopic": "booking-created.DLT",
  "mainTopic": "booking-created",
  "maxRecords": 10
}
```

**Response:**
```json
{
  "dltTopic": "booking-created.DLT",
  "mainTopic": "booking-created",
  "reprocessedCount": 8,
  "failedCount": 2,
  "startTime": "2025-11-09T18:30:00",
  "endTime": "2025-11-09T18:30:05",
  "error": null
}
```

### 4. Inspect DLQ Messages
```http
GET /api/dlq/inspect/{dltTopic}?maxMessages=10
```

**Response:**
```json
[
  {
    "topic": "booking-created.DLT",
    "partition": 0,
    "offset": 123,
    "key": "PNR123456",
    "timestamp": "2025-11-09T18:25:00",
    "valueString": "{\"bookingId\":123,\"userId\":1,...}"
  }
]
```

## Prometheus Metrics

The DLQ system exposes the following metrics:

- `kafka.dlq.messages.total` - Total messages sent to DLQ
- `kafka.dlq.reprocessed.total` - Total messages reprocessed from DLQ
- `kafka.dlq.reprocess.failed.total` - Total failed reprocessing attempts
- `kafka.dlq.messages.count{topic="..."}` - Current message count in DLQ per topic

## Monitoring and Alerting

### Automatic Monitoring
- Scheduled task runs every 5 minutes
- Checks all DLQ topics for threshold violations
- Logs warnings when threshold exceeded
- Respects cooldown period (1 hour between alerts)

### Alert Thresholds
- Default threshold: 10 messages
- Configurable via `dlq.alerting.threshold`
- Can be disabled via `dlq.alerting.enabled=false`

### Integration Points
The alerting system can be extended to integrate with:
- Email notifications
- Slack webhooks
- PagerDuty alerts
- Prometheus AlertManager

## Usage Examples

### 1. Check DLQ Status
```bash
curl http://localhost:8095/api/dlq/stats/booking-created.DLT
```

### 2. Reprocess Failed Messages
```bash
curl -X POST http://localhost:8095/api/dlq/reprocess \
  -H "Content-Type: application/json" \
  -d '{
    "dltTopic": "booking-created.DLT",
    "mainTopic": "booking-created",
    "maxRecords": 10
  }'
```

### 3. Inspect DLQ Messages
```bash
curl http://localhost:8095/api/dlq/inspect/booking-created.DLT?maxMessages=5
```

## Best Practices

1. **Monitor DLQ Regularly**
   - Set up alerts for DLQ threshold violations
   - Review DLQ messages weekly
   - Investigate root causes of failures

2. **Reprocess with Caution**
   - Fix the root cause before reprocessing
   - Reprocess in small batches
   - Monitor reprocessing results

3. **DLQ Retention**
   - Configure Kafka retention policies for DLQ topics
   - Archive old DLQ messages for analysis
   - Don't let DLQ grow indefinitely

4. **Error Handling**
   - Log detailed error information
   - Include correlation IDs for tracing
   - Document common failure patterns

## Troubleshooting

### DLQ Growing Rapidly
1. Check consumer logs for error patterns
2. Verify external service dependencies
3. Review retry configuration
4. Check for serialization issues

### Reprocessing Fails
1. Verify main topic exists and is accessible
2. Check message format compatibility
3. Review consumer group configuration
4. Check Kafka connectivity

### No Alerts Received
1. Verify `dlq.alerting.enabled=true`
2. Check alert threshold configuration
3. Review application logs for alert messages
4. Verify scheduling is enabled

## Future Enhancements

1. **Automatic Reprocessing**
   - Scheduled reprocessing of DLQ messages
   - Exponential backoff for failed reprocessing
   - Success rate monitoring

2. **DLQ Analytics**
   - Failure pattern analysis
   - Root cause identification
   - Trend visualization

3. **Advanced Alerting**
   - Integration with external alerting systems
   - Custom alert rules
   - Alert escalation policies

4. **DLQ Dashboard**
   - Real-time DLQ visualization
   - Historical trends
   - Interactive reprocessing UI

## Files Created/Modified

### New Files
- `DlqManagementService.java` - DLQ management operations
- `DlqManagementController.java` - REST API for DLQ management
- `DlqAlertingService.java` - DLQ monitoring and alerting
- `SchedulingConfig.java` - Scheduling configuration
- `DEAD_LETTER_QUEUE_IMPLEMENTATION.md` - This documentation

### Modified Files
- `KafkaConfig.java` - Enhanced error handler with retry logic
- `application.yml` - Added DLQ configuration

## Testing

### Test DLQ Functionality
```bash
# 1. Create a booking that will fail processing
# 2. Check DLQ statistics
curl http://localhost:8095/api/dlq/stats/booking-created.DLT

# 3. Inspect failed messages
curl http://localhost:8095/api/dlq/inspect/booking-created.DLT

# 4. Reprocess messages
curl -X POST http://localhost:8095/api/dlq/reprocess \
  -H "Content-Type: application/json" \
  -d '{"dltTopic":"booking-created.DLT","mainTopic":"booking-created","maxRecords":5}'
```

## Conclusion

The DLQ implementation provides a robust solution for handling failed Kafka messages, ensuring no messages are lost and providing tools for monitoring, alerting, and reprocessing. This significantly improves the reliability and observability of the event-driven architecture.

