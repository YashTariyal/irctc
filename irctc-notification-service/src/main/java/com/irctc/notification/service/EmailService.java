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
public class EmailService {
	@Autowired
	private NotificationMetrics metrics;

	public Mono<String> sendEmail(String to, String subject, String htmlBody) {
		return Mono.just("sent")
			.doOnSuccess(s -> metrics.incrementEmailSuccess())
			.doOnError(e -> metrics.incrementEmailFailure());
	}
	
	/**
	 * Send email using NotificationRequest DTO
	 */
	public NotificationResponse sendEmail(NotificationRequest request) {
		NotificationResponse response = new NotificationResponse();
		response.setChannel("EMAIL");
		response.setSentTime(LocalDateTime.now());
		response.setNotificationId(UUID.randomUUID().toString());
		
		try {
			// In production, use actual email service (SendGrid, AWS SES, etc.)
			// For now, simulate
			boolean success = Math.random() > 0.05; // 95% success rate
			
			if (success) {
				response.setStatus("SUCCESS");
				response.setMessageId("email_" + UUID.randomUUID().toString());
				metrics.incrementEmailSuccess();
			} else {
				response.setStatus("FAILED");
				response.setErrorMessage("Email delivery failed");
				metrics.incrementEmailFailure();
			}
		} catch (Exception e) {
			response.setStatus("FAILED");
			response.setErrorMessage("Error: " + e.getMessage());
			metrics.incrementEmailFailure();
		}
		
		return response;
	}
}


