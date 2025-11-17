package com.irctc.train.client;

import com.irctc.train.dto.PushNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "irctc-notification-service", path = "/api/notifications", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/push")
    ResponseEntity<Void> sendPushNotification(@RequestBody PushNotificationRequest request);
}

