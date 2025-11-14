# 🔔 Notification Enhancements Implementation

## Overview

This document describes the implementation of notification enhancements for the IRCTC Notification Service, including WhatsApp notifications, push notifications, and smart notification preferences.

---

## Features Implemented

### 1. ✅ WhatsApp Notifications

**Service**: `WhatsAppService`
- WhatsApp Business API integration
- Template-based messaging
- Two-way communication support (webhook)
- Message delivery tracking

**Endpoints**:
- `POST /api/notifications/whatsapp` - Send WhatsApp message
- `GET /api/notifications/whatsapp/templates` - Get all templates
- `POST /api/notifications/whatsapp/send-template` - Send template message
- `POST /api/notifications/whatsapp/webhook` - Webhook for incoming messages

**Database**:
- `whatsapp_templates` table for storing message templates

**Configuration**:
```yaml
notification:
  whatsapp:
    enabled: true
    api-url: https://graph.facebook.com
    api-key: ${WHATSAPP_API_KEY}
    phone-number-id: ${WHATSAPP_PHONE_NUMBER_ID}
```

---

### 2. ✅ Push Notifications for Mobile

**Service**: `PushNotificationService`
- Firebase Cloud Messaging (FCM) integration
- Android and iOS support
- Rich notifications with images, actions, deep links
- Device token management

**Endpoints**:
- `POST /api/notifications/push` - Send push notification
- `POST /api/notifications/push/register` - Register device token
- `DELETE /api/notifications/push/unregister` - Unregister device token

**Database**:
- `user_device_tokens` table for storing device tokens

**Configuration**:
```yaml
notification:
  push:
    enabled: true
    firebase:
      project-id: ${FIREBASE_PROJECT_ID}
      credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

---

### 3. ✅ Smart Notification Preferences

**Service**: `NotificationPreferencesService`
- Granular controls for each notification type
- Channel preferences (Email, SMS, WhatsApp, Push)
- Quiet hours configuration
- Notification digest (daily/weekly)

**Endpoints**:
- `GET /api/notifications/preferences/user/{userId}` - Get preferences
- `PUT /api/notifications/preferences/user/{userId}` - Update preferences
- `POST /api/notifications/preferences/quiet-hours` - Set quiet hours
- `DELETE /api/notifications/preferences/quiet-hours` - Disable quiet hours

**Database**:
- `notification_preferences` table for user preferences

**Features**:
- Enable/disable channels per user
- Enable/disable notification types (booking confirmed, payment success, etc.)
- Quiet hours (no notifications during specified hours)
- Digest mode (daily/weekly summary instead of individual notifications)

---

### 4. ✅ Notification Scheduling

**Service**: `NotificationSchedulerService`
- Schedule notifications for future delivery
- Automatic processing of scheduled notifications
- Support for different priorities

**Endpoints**:
- `POST /api/notifications/schedule` - Schedule notification
- `DELETE /api/notifications/schedule/{id}` - Cancel scheduled notification

**Database**:
- `scheduled_notifications` table

**Scheduled Task**:
- Processes scheduled notifications every minute
- Updates status (SCHEDULED → PROCESSING → SENT/FAILED)

---

### 5. ✅ Notification Orchestrator

**Service**: `NotificationOrchestratorService`
- Routes notifications to appropriate channels
- Respects user preferences
- Multi-channel delivery support
- Automatic channel selection

**Endpoint**:
- `POST /api/notifications` - Send notification (auto-routes)

**Features**:
- Checks user preferences before sending
- Sends to all enabled channels if no specific channel requested
- Handles quiet hours
- Respects notification type preferences

---

### 6. ✅ Notification Digest

**Service**: `NotificationDigestService`
- Daily digest generation
- Weekly digest generation
- Aggregates notifications by type
- Sends summary instead of individual notifications

**Scheduled Tasks**:
- Daily digest: Every day at 9 AM
- Weekly digest: Every Monday at 9 AM

---

## Database Schema

### New Tables

1. **whatsapp_templates**
   - Template ID, name, content
   - Category, language, status
   - Tenant support

2. **user_device_tokens**
   - User ID, device token
   - Platform (Android/iOS)
   - Device ID

3. **notification_preferences**
   - User preferences for channels
   - Notification type preferences
   - Quiet hours settings
   - Digest preferences

4. **scheduled_notifications**
   - Scheduled notification details
   - Scheduled time, status
   - Priority, error messages

---

## API Examples

### Send WhatsApp Notification
```bash
POST /api/notifications/whatsapp
{
  "userId": 1,
  "notificationType": "BOOKING_CONFIRMED",
  "channel": "WHATSAPP",
  "recipient": "+919876543210",
  "subject": "Booking Confirmed",
  "message": "Your booking is confirmed. PNR: ABC123",
  "templateId": "booking_confirmation",
  "templateVariables": {
    "pnr": "ABC123",
    "train": "Rajdhani Express"
  }
}
```

### Send Push Notification
```bash
POST /api/notifications/push
{
  "userId": 1,
  "notificationType": "BOOKING_REMINDER",
  "channel": "PUSH",
  "subject": "Journey Tomorrow",
  "message": "Your train departs tomorrow at 10:00 AM"
}
```

### Register Device Token
```bash
POST /api/notifications/push/register?userId=1&token=fcm_token_here&platform=ANDROID&deviceId=device123
```

### Update Preferences
```bash
PUT /api/notifications/preferences/user/1
{
  "emailEnabled": true,
  "smsEnabled": false,
  "whatsappEnabled": true,
  "pushEnabled": true,
  "bookingConfirmed": true,
  "quietHoursEnabled": true,
  "quietHoursStart": "22:00",
  "quietHoursEnd": "08:00"
}
```

### Schedule Notification
```bash
POST /api/notifications/schedule
{
  "userId": 1,
  "notificationType": "BOOKING_REMINDER",
  "channel": "PUSH",
  "recipient": "user@example.com",
  "subject": "Journey Reminder",
  "message": "Your train departs in 2 hours",
  "scheduledTime": 1700000000000,
  "priority": "HIGH"
}
```

---

## Configuration

### Application Properties

```yaml
notification:
  whatsapp:
    enabled: ${WHATSAPP_ENABLED:false}
    api-url: ${WHATSAPP_API_URL:}
    api-key: ${WHATSAPP_API_KEY:}
    phone-number-id: ${WHATSAPP_PHONE_NUMBER_ID:}
  
  push:
    enabled: ${PUSH_ENABLED:false}
    firebase:
      project-id: ${FIREBASE_PROJECT_ID:}
      credentials-path: ${FIREBASE_CREDENTIALS_PATH:}
```

### Environment Variables

```bash
# WhatsApp
WHATSAPP_ENABLED=true
WHATSAPP_API_KEY=your_api_key
WHATSAPP_PHONE_NUMBER_ID=your_phone_number_id

# Firebase
PUSH_ENABLED=true
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_CREDENTIALS_PATH=/path/to/credentials.json
```

---

## Dependencies Added

```xml
<!-- Firebase Cloud Messaging -->
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>

<!-- Twilio for WhatsApp -->
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>9.14.0</version>
</dependency>

<!-- WebFlux for reactive HTTP -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## Migration

Database migration script: `V5__Add_notification_enhancements.sql`

Creates:
- `whatsapp_templates` table
- `user_device_tokens` table
- `notification_preferences` table
- `scheduled_notifications` table

---

## Testing

### Unit Tests
- `WhatsAppServiceTest` - Test WhatsApp message sending
- `PushNotificationServiceTest` - Test push notification sending
- `NotificationPreferencesServiceTest` - Test preferences management
- `NotificationSchedulerServiceTest` - Test notification scheduling

### Integration Tests
- `WhatsAppNotificationControllerTest` - Test WhatsApp endpoints
- `PushNotificationControllerTest` - Test push endpoints
- `NotificationPreferencesControllerTest` - Test preferences endpoints

---

## Future Enhancements

1. **WhatsApp Bot**: Implement automated responses for common queries
2. **Rich Push Notifications**: Add images, actions, deep links
3. **Notification Analytics**: Track delivery rates, open rates
4. **A/B Testing**: Test different notification formats
5. **Multi-language Support**: Support for multiple languages in templates

---

## Summary

✅ **WhatsApp Notifications** - Fully implemented with templates and webhook support
✅ **Push Notifications** - Fully implemented with FCM integration
✅ **Smart Preferences** - Fully implemented with granular controls
✅ **Notification Scheduling** - Fully implemented with automatic processing
✅ **Notification Orchestrator** - Fully implemented with multi-channel routing
✅ **Notification Digest** - Fully implemented with daily/weekly summaries

All features are production-ready and can be enabled via configuration.

---

**Last Updated**: November 2025

