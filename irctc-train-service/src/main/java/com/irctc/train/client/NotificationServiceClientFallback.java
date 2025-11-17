package com.irctc.train.client;

import com.irctc.train.dto.PushNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceClientFallback.class);

    @Override
    public ResponseEntity<Void> sendPushNotification(PushNotificationRequest request) {
        logger.warn("Notification service unavailable. Unable to send price alert notification for user {}", request.getUserId());
        return ResponseEntity.accepted().build();
    }
}

