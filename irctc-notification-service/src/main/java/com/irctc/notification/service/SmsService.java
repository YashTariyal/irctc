package com.irctc.notification.service;

import com.irctc.notification.metrics.NotificationMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SmsService {
	@Autowired
	private NotificationMetrics metrics;

	public Mono<String> sendSms(String phone, String message) {
		return Mono.just("sent")
			.doOnSuccess(s -> metrics.incrementSmsSuccess())
			.doOnError(e -> metrics.incrementSmsFailure());
	}
}
