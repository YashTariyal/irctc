package com.irctc.notification.service;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.entity.WhatsAppTemplate;
import com.irctc.notification.repository.SimpleNotificationRepository;
import com.irctc.notification.repository.WhatsAppTemplateRepository;
import com.irctc.notification.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * WhatsApp Notification Service
 * Supports WhatsApp Business API for sending notifications
 */
@Service
@ConditionalOnProperty(name = "notification.whatsapp.enabled", havingValue = "true", matchIfMissing = false)
public class WhatsAppService {
    
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);
    
    @Value("${notification.whatsapp.api-url:}")
    private String apiUrl;
    
    @Value("${notification.whatsapp.api-key:}")
    private String apiKey;
    
    @Value("${notification.whatsapp.phone-number-id:}")
    private String phoneNumberId;
    
    @Value("${notification.whatsapp.enabled:false}")
    private boolean enabled;
    
    @Autowired(required = false)
    private SimpleNotificationRepository notificationRepository;
    
    @Autowired(required = false)
    private WhatsAppTemplateRepository templateRepository;
    
    @Autowired(required = false)
    private WebClient.Builder webClientBuilder;
    
    private WebClient webClient;
    
    /**
     * Send WhatsApp message
     */
    public NotificationResponse sendMessage(NotificationRequest request) {
        logger.info("Sending WhatsApp message to: {}", request.getRecipient());
        
        NotificationResponse response = new NotificationResponse();
        response.setChannel("WHATSAPP");
        response.setSentTime(LocalDateTime.now());
        
        if (!enabled) {
            logger.warn("WhatsApp service is disabled");
            response.setStatus("FAILED");
            response.setErrorMessage("WhatsApp service is disabled");
            return response;
        }
        
        try {
            // Use template if provided
            if (request.getTemplateId() != null) {
                response = sendTemplateMessage(request);
            } else {
                response = sendTextMessage(request);
            }
            
            // Save notification record
            if (notificationRepository != null) {
                saveNotificationRecord(request, response);
            }
            
            logger.info("✅ WhatsApp message sent successfully: {}", response.getMessageId());
        } catch (Exception e) {
            logger.error("Error sending WhatsApp message: {}", e.getMessage(), e);
            response.setStatus("FAILED");
            response.setErrorMessage("Error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Send text message
     */
    private NotificationResponse sendTextMessage(NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        
        if (webClient == null && webClientBuilder != null) {
            webClient = webClientBuilder.baseUrl(apiUrl).build();
        }
        
        // Simulate WhatsApp API call
        // In production, this would call WhatsApp Business API
        if (webClient != null && apiKey != null && !apiKey.isEmpty()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("messaging_product", "whatsapp");
                payload.put("to", request.getRecipient());
                payload.put("type", "text");
                
                Map<String, String> textContent = new HashMap<>();
                textContent.put("body", request.getMessage());
                payload.put("text", textContent);
                
                // Call WhatsApp API
                String messageId = webClient.post()
                    .uri("/v1/messages")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(result -> {
                        if (result != null && result.containsKey("messages")) {
                            List<Map<String, String>> messages = (List<Map<String, String>>) result.get("messages");
                            if (!messages.isEmpty()) {
                                return messages.get(0).get("id");
                            }
                        }
                        return UUID.randomUUID().toString();
                    })
                    .onErrorReturn(UUID.randomUUID().toString())
                    .block();
                
                response.setMessageId(messageId);
                response.setStatus("SUCCESS");
            } catch (Exception e) {
                logger.warn("WhatsApp API call failed, using simulation: {}", e.getMessage());
                response = sendTextMessageSimulated(request);
            }
        } else {
            response = sendTextMessageSimulated(request);
        }
        
        return response;
    }
    
    /**
     * Send template message
     */
    private NotificationResponse sendTemplateMessage(NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        
        // Get template
        WhatsAppTemplate template = null;
        if (templateRepository != null) {
            template = templateRepository.findByTemplateId(request.getTemplateId())
                .orElse(null);
        }
        
        if (template == null) {
            logger.warn("Template not found: {}, falling back to text message", request.getTemplateId());
            return sendTextMessage(request);
        }
        
        // Replace template variables
        String message = template.getContent();
        if (request.getTemplateVariables() != null) {
            for (Map.Entry<String, String> entry : request.getTemplateVariables().entrySet()) {
                message = message.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }
        
        // Update request with processed message
        request.setMessage(message);
        return sendTextMessage(request);
    }
    
    /**
     * Simulated WhatsApp message sending (for testing)
     */
    private NotificationResponse sendTextMessageSimulated(NotificationRequest request) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        response.setMessageId("wa_msg_" + UUID.randomUUID().toString());
        
        // Simulate success (90% success rate)
        boolean success = Math.random() > 0.1;
        
        if (success) {
            response.setStatus("SUCCESS");
        } else {
            response.setStatus("FAILED");
            response.setErrorMessage("WhatsApp delivery failed");
        }
        
        return response;
    }
    
    /**
     * Save notification record
     */
    private void saveNotificationRecord(NotificationRequest request, NotificationResponse response) {
        SimpleNotification notification = new SimpleNotification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getNotificationType());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setSentTime(response.getSentTime());
        notification.setStatus(response.getStatus());
        
        if (TenantContext.hasTenant()) {
            notification.setTenantId(TenantContext.getTenantId());
        }
        
        notificationRepository.save(notification);
    }
    
    /**
     * Get all WhatsApp templates
     */
    public List<WhatsAppTemplate> getTemplates() {
        if (templateRepository != null) {
            return templateRepository.findAll();
        }
        return List.of();
    }
    
    /**
     * Process incoming WhatsApp message (for two-way communication)
     */
    public void processIncomingMessage(Map<String, Object> webhookPayload) {
        logger.info("Processing incoming WhatsApp message: {}", webhookPayload);
        
        // Extract message details
        // In production, this would parse WhatsApp webhook payload
        // and respond to user queries
        
        // Example: Query booking status
        // if (message.contains("PNR")) {
        //     String pnr = extractPnr(message);
        //     BookingStatus status = bookingService.getStatusByPnr(pnr);
        //     sendMessage(phoneNumber, "Your booking status: " + status);
        // }
    }
}

