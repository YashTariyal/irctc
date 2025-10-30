package com.irctc.notification.service;

import com.irctc.notification.metrics.NotificationMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EmailService {
	@Autowired
	private NotificationMetrics metrics;

	public Mono<String> sendEmail(String to, String subject, String htmlBody) {
		return Mono.just("sent")
			.doOnSuccess(s -> metrics.incrementEmailSuccess())
			.doOnError(e -> metrics.incrementEmailFailure());
	}
}


