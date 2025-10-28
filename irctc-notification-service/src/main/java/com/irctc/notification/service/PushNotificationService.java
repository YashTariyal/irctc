package com.irctc.notification.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class PushNotificationService {
	public Mono<String> sendPushNotification(Long userId, String title, String body, Map<String, Object> data) {
		return Mono.just("sent");
	}
}
