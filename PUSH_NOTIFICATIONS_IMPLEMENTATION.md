# 📱 Push Notifications for Mobile - Implementation Guide

## Overview

This document describes the implementation of Push Notifications for Mobile feature in the IRCTC Notification Service, including Firebase Cloud Messaging (FCM) integration, rich notifications with images and actions, and notification scheduling.

---

## Features Implemented

### 1. ✅ Firebase Cloud Messaging Integration

**Service**: `PushNotificationService`
- Firebase Admin SDK integration (ready for production)
- Android and iOS platform support
- Device token management
- Multi-device support per user

**Configuration**:
```yaml
notification:
  push:
    enabled: true
    firebase:
      project-id: ${FIREBASE_PROJECT_ID}
      credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

### 2. ✅ Rich Notifications

**Features**:
- **Images**: Support for notification images
- **Icons**: Custom notification icons
- **Actions**: Action buttons for Android notifications
- **Deep Links**: Click actions to open specific app screens
- **Sounds**: Custom notification sounds
- **Badges**: iOS badge count support
- **Silent Notifications**: Background notifications

**DTO**: `PushNotificationRequest`
```java
{
  "userId": 123,
  "title": "Booking Confirmed",
  "body": "Your train ticket has been confirmed",
  "imageUrl": "https://example.com/ticket.jpg",
  "iconUrl": "https://example.com/icon.png",
  "sound": "notification.wav",
  "clickAction": "irctc://booking/12345",
  "data": {
    "bookingId": "12345",
    "pnr": "ABC123"
  },
  "actions": {
    "view": "View Ticket",
    "cancel": "Cancel Booking"
  },
  "priority": "high",
  "channelId": "booking_channel",
  "badge": "1",
  "notificationType": "BOOKING_CONFIRMED"
}
```

### 3. ✅ Notification Scheduling

**Integration**: Uses `NotificationSchedulerService`
- Schedule notifications for future events
- Automatic processing via scheduled tasks
- Support for booking reminders, departure alerts, etc.

**Example**:
```java
{
  "userId": 123,
  "title": "Departure Reminder",
  "body": "Your train departs in 2 hours",
  "scheduledTime": 1700000000000, // Unix timestamp
  "notificationType": "DEPARTURE_REMINDER"
}
```

### 4. ✅ Device Token Management

**Endpoints**:
- `POST /api/notifications/push/register` - Register device token
- `DELETE /api/notifications/push/unregister` - Unregister device token
- `GET /api/notifications/push/devices/{userId}` - Get user devices

**Entity**: `UserDeviceToken`
- Stores FCM tokens per user
- Supports multiple devices per user
- Platform tracking (Android/iOS)
- Device ID tracking

---

## APIs

### 1. Send Push Notification (Legacy)
```http
POST /api/notifications/push
Content-Type: application/json

{
  "userId": 123,
  "subject": "Booking Confirmed",
  "message": "Your ticket has been confirmed",
  "notificationType": "PUSH"
}
```

### 2. Send Rich Push Notification
```http
POST /api/notifications/push/rich
Content-Type: application/json

{
  "userId": 123,
  "title": "Booking Confirmed",
  "body": "Your train ticket has been confirmed",
  "imageUrl": "https://example.com/ticket.jpg",
  "clickAction": "irctc://booking/12345",
  "data": {
    "bookingId": "12345"
  },
  "priority": "high",
  "notificationType": "BOOKING_CONFIRMED"
}
```

### 3. Register Device Token
```http
POST /api/notifications/push/register?userId=123&token=fcm_token_abc&platform=ANDROID&deviceId=device_123
```

### 4. Unregister Device Token
```http
DELETE /api/notifications/push/unregister?token=fcm_token_abc
```

### 5. Get User Devices
```http
GET /api/notifications/push/devices/123
```

### 6. Schedule Notification
```http
POST /api/notifications/schedule
Content-Type: application/json

{
  "userId": 123,
  "subject": "Departure Reminder",
  "message": "Your train departs in 2 hours",
  "scheduledTime": 1700000000000,
  "channel": "PUSH",
  "notificationType": "DEPARTURE_REMINDER"
}
```

### 7. Get Notification Preferences
```http
GET /api/notifications/preferences/user/123
```

### 8. Update Notification Preferences
```http
PUT /api/notifications/preferences/user/123
Content-Type: application/json

{
  "pushEnabled": true,
  "bookingNotifications": true,
  "reminderNotifications": true
}
```

---

## Architecture

### Components

1. **PushNotificationService**
   - Core service for push notification processing
   - Firebase integration
   - Device token management
   - Rich notification building

2. **PushNotificationController**
   - REST API endpoints
   - Request/response handling

3. **UserDeviceToken Entity**
   - Stores device tokens
   - Multi-tenant support
   - Platform tracking

4. **NotificationSchedulerService**
   - Scheduled notification processing
   - Integration with push notifications

### Flow

1. **Device Registration**:
   - Mobile app registers device token via `/api/notifications/push/register`
   - Token stored in `user_device_tokens` table

2. **Sending Notification**:
   - Service receives push notification request
   - Retrieves device tokens for user
   - Builds FCM message with rich content
   - Sends via Firebase Admin SDK
   - Saves notification record

3. **Scheduled Notifications**:
   - Notification scheduled with future timestamp
   - Stored in `scheduled_notifications` table
   - Processed by scheduled task every minute
   - Converted to push notification when due

---

## Firebase Setup

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Enable Cloud Messaging

### 2. Generate Service Account Key
1. Go to Project Settings > Service Accounts
2. Click "Generate New Private Key"
3. Save the JSON file securely

### 3. Configure Application
```yaml
notification:
  push:
    enabled: true
    firebase:
      project-id: your-project-id
      credentials-path: /path/to/service-account-key.json
```

### 4. Android Setup
- Add `google-services.json` to Android app
- Configure FCM in `AndroidManifest.xml`
- Request notification permissions

### 5. iOS Setup
- Add `GoogleService-Info.plist` to iOS app
- Configure APNs certificates
- Request notification permissions

---

## Database Schema

### user_device_tokens
```sql
CREATE TABLE user_device_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(20) NOT NULL,
    device_id VARCHAR(200),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_device_tokens_user_id (user_id),
    INDEX idx_device_tokens_token (token),
    INDEX idx_device_tokens_tenant_id (tenant_id)
);
```

---

## Testing

### Unit Tests
- `PushNotificationServiceTest` - 8 tests covering:
  - Rich notification sending
  - Device token registration
  - Device token unregistration
  - User device retrieval
  - Scheduled notifications
  - Error handling

### Integration Tests
- `PushNotificationControllerTest` - Controller API tests

### Running Tests
```bash
cd irctc-notification-service
./mvnw test -Dtest=PushNotificationServiceTest
```

---

## Usage Examples

### Example 1: Booking Confirmation
```java
PushNotificationRequest request = new PushNotificationRequest();
request.setUserId(123L);
request.setTitle("Booking Confirmed");
request.setBody("Your train ticket has been confirmed");
request.setImageUrl("https://irctc.com/tickets/12345.jpg");
request.setClickAction("irctc://booking/12345");
request.setNotificationType("BOOKING_CONFIRMED");
request.setPriority("high");

Map<String, String> data = new HashMap<>();
data.put("bookingId", "12345");
data.put("pnr", "ABC123");
request.setData(data);

PushNotificationResponse response = pushNotificationService.sendRichPushNotification(request);
```

### Example 2: Departure Reminder (Scheduled)
```java
PushNotificationRequest request = new PushNotificationRequest();
request.setUserId(123L);
request.setTitle("Departure Reminder");
request.setBody("Your train departs in 2 hours");
request.setScheduledTime(System.currentTimeMillis() + 7200000); // 2 hours
request.setNotificationType("DEPARTURE_REMINDER");
request.setPriority("high");

PushNotificationResponse response = pushNotificationService.sendRichPushNotification(request);
```

### Example 3: Register Device
```java
pushNotificationService.registerDeviceToken(
    123L, 
    "fcm_token_abc123", 
    "ANDROID", 
    "device_xyz789"
);
```

---

## Production Considerations

1. **Firebase Credentials**: Store securely (use secrets management)
2. **Error Handling**: Implement retry logic for failed notifications
3. **Rate Limiting**: Implement rate limiting for API endpoints
4. **Monitoring**: Monitor notification delivery rates
5. **Token Refresh**: Handle token refresh on mobile apps
6. **Invalid Tokens**: Clean up invalid/expired tokens
7. **Batch Sending**: Use batch API for multiple notifications
8. **Analytics**: Track notification open rates

---

## Future Enhancements

1. **Topic-based Notifications**: Subscribe users to topics
2. **Conditional Notifications**: Send based on user location
3. **A/B Testing**: Test different notification formats
4. **Personalization**: Customize notifications per user
5. **Notification Groups**: Group related notifications
6. **Rich Media**: Support for videos in notifications

---

## Related Documentation

- [Notification Preferences Implementation](./NOTIFICATION_ENHANCEMENTS_IMPLEMENTATION.md)
- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Push Notifications Guide](https://developer.android.com/develop/ui/views/notifications)
- [iOS Push Notifications Guide](https://developer.apple.com/documentation/usernotifications)

---

## Summary

✅ **Firebase Cloud Messaging**: Integrated and ready for production  
✅ **Rich Notifications**: Images, actions, deep links supported  
✅ **Notification Scheduling**: Integrated with scheduler service  
✅ **Device Management**: Register, unregister, and retrieve devices  
✅ **Multi-platform**: Android and iOS support  
✅ **Testing**: Comprehensive unit and integration tests  
✅ **Documentation**: Complete implementation guide  

The Push Notifications feature is production-ready and can be enabled by configuring Firebase credentials.

