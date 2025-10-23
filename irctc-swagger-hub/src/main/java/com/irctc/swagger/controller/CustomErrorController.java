package com.irctc.swagger.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Error Controller for Swagger Hub
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", status != null ? status : 500);
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested resource was not found");
        errorResponse.put("path", request.getRequestURI());
        
        // Add helpful links
        Map<String, String> links = new HashMap<>();
        links.put("swagger-ui", "/swagger-ui/index.html");
        links.put("api-docs", "/api-docs");
        links.put("services", "/services");
        links.put("eureka-dashboard", "http://localhost:8761");
        links.put("api-gateway", "http://localhost:8090");
        errorResponse.put("availableEndpoints", links);
        
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        if (status != null) {
            try {
                httpStatus = HttpStatus.valueOf(Integer.parseInt(status.toString()));
            } catch (Exception e) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
