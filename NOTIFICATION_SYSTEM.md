# Event-Driven Notification System

## Overview

The IRCTC backend system now includes an event-driven notification system using Apache Kafka. This system publishes booking events to Kafka topics, which are then consumed by different notification services for email, SMS, and WhatsApp notifications.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Booking       │    │     Kafka       │    │  Notification   │
│   Service       │───▶│     Topics      │───▶│   Consumers     │
│                 │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                        │
                              ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Event Store   │    │   Email/SMS/    │
                       │   (Optional)    │    │   WhatsApp      │
                       │                 │    │   Services      │
                       └─────────────────┘    └─────────────────┘
```

## Event Types

### 1. Booking Confirmed Event
- **Topic**: `booking-confirmed`
- **Trigger**: When a booking is successfully created
- **Payload**: Complete booking details including user, train, passenger, and fare information

### 2. Booking Cancelled Event
- **Topic**: `booking-cancelled`
- **Trigger**: When a booking is cancelled
- **Payload**: Booking details with cancellation information

### 3. Payment Completed Event
- **Topic**: `payment-completed`
- **Trigger**: When payment status is updated to COMPLETED
- **Payload**: Booking details with payment confirmation

## Event Structure

```json
{
  "eventId": "uuid-string",
  "eventType": "BOOKING_CONFIRMED",
  "bookingId": 123,
  "pnrNumber": "1234567890",
  "bookingStatus": "CONFIRMED",
  "paymentStatus": "PENDING",
  "userId": 1,
  "userEmail": "user@example.com",
  "userPhone": "9876543210",
  "userName": "John Doe",
  "trainId": 1,
  "trainNumber": "12345",
  "trainName": "Rajdhani Express",
  "sourceStation": "New Delhi",
  "destinationStation": "Mumbai Central",
  "journeyDate": "2024-01-15",
  "departureTime": "16:00:00",
  "arrivalTime": "08:30:00",
  "passengerName": "John Doe",
  "passengerAge": 30,
  "passengerGender": "MALE",
  "coachNumber": "A1",
  "seatNumber": "1",
  "coachType": "AC_FIRST_CLASS",
  "totalFare": 5000.00,
  "baseFare": 4500.00,
  "convenienceFee": 90.00,
  "gstAmount": 225.00,
  "bookingDate": "2024-01-01T10:00:00",
  "eventTimestamp": "2024-01-01T10:00:00",
  "quotaType": "GENERAL",
  "isTatkal": false,
  "bookingSource": "WEB"
}
```

## Kafka Topics

### 1. booking-confirmed
- **Purpose**: Notify users about successful booking confirmation
- **Consumers**: Email, SMS, WhatsApp notification services
- **Key**: PNR Number
- **Partitions**: 3 (configurable)

### 2. booking-cancelled
- **Purpose**: Notify users about booking cancellation
- **Consumers**: Email notification service
- **Key**: PNR Number
- **Partitions**: 3 (configurable)

### 3. payment-completed
- **Purpose**: Notify users about successful payment
- **Consumers**: Email notification service
- **Key**: PNR Number
- **Partitions**: 3 (configurable)

## Notification Channels

### 1. Email Notifications
- **Consumer Group**: `email-notification-group`
- **Topics**: All event types
- **Content**: Detailed booking information with formatting
- **Template**: HTML email with booking details

### 2. SMS Notifications
- **Consumer Group**: `sms-notification-group`
- **Topics**: `booking-confirmed`, `booking-cancelled`
- **Content**: Concise booking information
- **Format**: Short text message with key details

### 3. WhatsApp Notifications
- **Consumer Group**: `whatsapp-notification-group`
- **Topics**: `booking-confirmed`
- **Content**: Rich formatted message with emojis
- **Format**: WhatsApp Business API compatible

## Implementation Details

### Producer Side (Booking Service)

```java
@Service
public class NotificationService {
    
    @Autowired
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;
    
    public void publishBookingConfirmedEvent(Booking booking) {
        BookingEvent event = createBookingEvent(booking, "BOOKING_CONFIRMED");
        kafkaTemplate.send("booking-confirmed", booking.getPnrNumber(), event);
    }
}
```

### Consumer Side (Notification Services)

```java
@Service
public class NotificationConsumerService {
    
    @KafkaListener(topics = "booking-confirmed", groupId = "email-notification-group")
    public void handleEmailNotification(BookingEvent event) {
        // Send email notification
    }
    
    @KafkaListener(topics = "booking-confirmed", groupId = "sms-notification-group")
    public void handleSmsNotification(BookingEvent event) {
        // Send SMS notification
    }
}
```

## Configuration

### Kafka Producer Configuration
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
```

### Kafka Consumer Configuration
```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.group-id=irctc-notification-group
```

## Error Handling

### Producer Error Handling
- **Retry Logic**: 3 retries with exponential backoff
- **Fallback**: Log errors but don't fail the booking process
- **Monitoring**: Log successful and failed event publications

### Consumer Error Handling
- **Dead Letter Queue**: Failed messages sent to DLQ
- **Retry Policy**: Configurable retry attempts
- **Circuit Breaker**: Prevent cascading failures

## Monitoring and Observability

### Metrics
- **Producer Metrics**: Messages sent, failed, latency
- **Consumer Metrics**: Messages consumed, lag, error rate
- **Topic Metrics**: Partition count, replication factor

### Logging
- **Event Publication**: Success/failure logs with event details
- **Event Consumption**: Processing logs with notification details
- **Error Logging**: Detailed error information for debugging

## Deployment Considerations

### Kafka Setup
1. **Single Node**: For development/testing
2. **Multi-Node Cluster**: For production with replication
3. **Topic Management**: Auto-creation with proper partitions

### Scaling
- **Horizontal Scaling**: Multiple consumer instances
- **Partition Strategy**: Based on PNR number for ordering
- **Load Balancing**: Kafka handles consumer group balancing

## Testing

### Unit Testing
```java
@Test
public void testBookingConfirmedEvent() {
    // Test event creation and publication
}
```

### Integration Testing
```java
@Test
public void testNotificationFlow() {
    // Test complete flow from booking to notification
}
```

### Load Testing
- **Producer Load**: Multiple concurrent bookings
- **Consumer Load**: Multiple notification services
- **Topic Load**: High message volume testing

## Future Enhancements

### 1. Real-time Notifications
- **WebSocket Integration**: Real-time updates to web clients
- **Push Notifications**: Mobile app notifications
- **In-app Notifications**: Dashboard notifications

### 2. Advanced Features
- **Notification Preferences**: User-configurable notification settings
- **Template Management**: Dynamic notification templates
- **Delivery Tracking**: Track notification delivery status

### 3. Analytics
- **Notification Analytics**: Track open rates, click rates
- **User Behavior**: Analyze notification preferences
- **Performance Metrics**: Monitor notification delivery performance

## Security Considerations

### Data Protection
- **PII Handling**: Secure handling of personal information
- **Encryption**: Encrypt sensitive data in transit and at rest
- **Access Control**: Proper authentication and authorization

### Compliance
- **GDPR Compliance**: Right to be forgotten
- **Data Retention**: Configurable data retention policies
- **Audit Logging**: Complete audit trail for compliance

## Troubleshooting

### Common Issues
1. **Kafka Connection**: Check bootstrap servers configuration
2. **Serialization Errors**: Verify JSON structure
3. **Consumer Lag**: Monitor consumer group lag
4. **Topic Not Found**: Ensure topics are created

### Debug Commands
```bash
# List topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics.sh --describe --topic booking-confirmed --bootstrap-server localhost:9092

# Monitor consumer group
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group email-notification-group --describe
```

This event-driven notification system provides a scalable, reliable, and maintainable solution for handling multiple notification channels in the IRCTC backend system. 