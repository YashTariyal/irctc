package com.irctc.swagger.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

/**
 * Home Controller for Swagger Hub
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class HomeController {

    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to IRCTC Microservices API Documentation Hub");
        response.put("version", "1.0.0");
        response.put("description", "Central API Documentation Hub for all IRCTC Microservices");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("swagger-ui", "/swagger-ui/index.html");
        endpoints.put("api-docs", "/api-docs");
        endpoints.put("services", "/services");
        endpoints.put("health", "/actuator/health");
        response.put("endpoints", endpoints);
        
        Map<String, String> externalLinks = new HashMap<>();
        externalLinks.put("eureka-dashboard", "http://localhost:8761");
        externalLinks.put("api-gateway", "http://localhost:8090");
        response.put("externalLinks", externalLinks);
        
        return ResponseEntity.ok(response);
    }
}
