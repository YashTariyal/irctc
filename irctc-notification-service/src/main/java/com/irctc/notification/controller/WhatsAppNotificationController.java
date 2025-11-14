package com.irctc.notification.controller;

import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.entity.WhatsAppTemplate;
import com.irctc.notification.service.WhatsAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for WhatsApp notifications
 */
@RestController
@RequestMapping("/api/notifications/whatsapp")
public class WhatsAppNotificationController {
    
    @Autowired(required = false)
    private WhatsAppService whatsAppService;
    
    /**
     * Send WhatsApp message
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> sendWhatsAppMessage(@RequestBody NotificationRequest request) {
        if (whatsAppService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationResponse response = whatsAppService.sendMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all WhatsApp templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<WhatsAppTemplate>> getTemplates() {
        if (whatsAppService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        return ResponseEntity.ok(whatsAppService.getTemplates());
    }
    
    /**
     * Send WhatsApp message using template
     */
    @PostMapping("/send-template")
    public ResponseEntity<NotificationResponse> sendTemplateMessage(@RequestBody NotificationRequest request) {
        if (whatsAppService == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (request.getTemplateId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        NotificationResponse response = whatsAppService.sendMessage(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Webhook endpoint for incoming WhatsApp messages
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        if (whatsAppService == null) {
            return ResponseEntity.ok("Service not available");
        }
        
        whatsAppService.processIncomingMessage(payload);
        return ResponseEntity.ok("Webhook processed");
    }
}

