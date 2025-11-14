package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.metrics.NotificationMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SmsService {
	@Autowired
	private NotificationMetrics metrics;

	public Mono<String> sendSms(String phone, String message) {
		return Mono.just("sent")
			.doOnSuccess(s -> metrics.incrementSmsSuccess())
			.doOnError(e -> metrics.incrementSmsFailure());
	}
	
	/**
	 * Send SMS using NotificationRequest DTO
	 */
	public NotificationResponse sendSms(NotificationRequest request) {
		NotificationResponse response = new NotificationResponse();
		response.setChannel("SMS");
		response.setSentTime(LocalDateTime.now());
		response.setNotificationId(UUID.randomUUID().toString());
		
		try {
			// In production, use actual SMS service (Twilio, AWS SNS, etc.)
			// For now, simulate
			boolean success = Math.random() > 0.05; // 95% success rate
			
			if (success) {
				response.setStatus("SUCCESS");
				response.setMessageId("sms_" + UUID.randomUUID().toString());
				metrics.incrementSmsSuccess();
			} else {
				response.setStatus("FAILED");
				response.setErrorMessage("SMS delivery failed");
				metrics.incrementSmsFailure();
			}
		} catch (Exception e) {
			response.setStatus("FAILED");
			response.setErrorMessage("Error: " + e.getMessage());
			metrics.incrementSmsFailure();
		}
		
		return response;
	}
}
