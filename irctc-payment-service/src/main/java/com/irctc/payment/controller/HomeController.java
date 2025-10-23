package com.irctc.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller for Payment Service
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "IRCTC Payment Service");
        response.put("version", "1.0.0");
        response.put("description", "Payment Processing and Management Service");
        response.put("port", 8094);
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("swagger-ui", "/swagger-ui/index.html");
        endpoints.put("api-docs", "/api-docs");
        endpoints.put("health", "/actuator/health");
        endpoints.put("payments", "/api/payments");
        response.put("endpoints", endpoints);
        
        return ResponseEntity.ok(response);
    }
}
