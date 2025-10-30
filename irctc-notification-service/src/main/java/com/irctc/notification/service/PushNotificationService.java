package com.irctc.notification.service;

import com.irctc.notification.metrics.NotificationMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class PushNotificationService {
	@Autowired
	private NotificationMetrics metrics;

	public Mono<String> sendPushNotification(Long userId, String title, String body, Map<String, Object> data) {
		return Mono.just("sent")
			.doOnSuccess(s -> metrics.incrementPushSuccess())
			.doOnError(e -> metrics.incrementPushFailure());
	}
}
