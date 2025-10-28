package com.irctc.notification.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EmailService {
	public Mono<String> sendEmail(String to, String subject, String htmlBody) {
		return Mono.just("sent");
	}
}


