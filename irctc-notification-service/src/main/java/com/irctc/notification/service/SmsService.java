package com.irctc.notification.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SmsService {
	public Mono<String> sendSms(String phone, String message) {
		return Mono.just("sent");
	}
}
