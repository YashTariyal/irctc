package com.irctc.user.controller;

import com.irctc.user.dto.ConsentRequest;
import com.irctc.user.dto.DataExportResponse;
import com.irctc.user.entity.DataExportRequest;
import com.irctc.user.entity.UserConsent;
import com.irctc.user.service.ConsentService;
import com.irctc.user.service.GDPRService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for GDPR compliance features
 */
@RestController
@RequestMapping("/api/users")
public class GDPRController {
    
    @Autowired
    private GDPRService gdprService;
    
    @Autowired
    private ConsentService consentService;
    
    /**
     * GET /api/users/{id}/export-data
     * Export all user data
     */
    @GetMapping("/{id}/export-data")
    public ResponseEntity<DataExportResponse> exportUserData(@PathVariable Long id) {
        DataExportRequest exportRequest = gdprService.exportUserData(id);
        return ResponseEntity.ok(convertToResponse(exportRequest));
    }
    
    /**
     * GET /api/users/{id}/export-data/{requestId}
     * Get export request status or download exported data
     */
    @GetMapping("/{id}/export-data/{requestId}")
    public ResponseEntity<?> getExportData(@PathVariable Long id, @PathVariable String requestId) {
        DataExportRequest exportRequest = gdprService.getExportRequestStatus(requestId);
        
        if (!exportRequest.getUserId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if ("COMPLETED".equals(exportRequest.getStatus())) {
            // In production, return file download
            // For now, return status
            return ResponseEntity.ok(convertToResponse(exportRequest));
        }
        
        return ResponseEntity.ok(convertToResponse(exportRequest));
    }
    
    /**
     * GET /api/users/{id}/export-requests
     * Get all export requests for a user
     */
    @GetMapping("/{id}/export-requests")
    public ResponseEntity<Map<String, Object>> getExportRequests(@PathVariable Long id) {
        List<DataExportRequest> requests = gdprService.getUserExportRequests(id);
        return ResponseEntity.ok(Map.of(
            "userId", id,
            "exportRequests", requests.stream().map(this::convertToResponse).collect(Collectors.toList()),
            "count", requests.size()
        ));
    }
    
    /**
     * DELETE /api/users/{id}/data
     * Delete all user data (Right to be forgotten)
     */
    @DeleteMapping("/{id}/data")
    public ResponseEntity<Map<String, String>> deleteUserData(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        gdprService.deleteUserData(id, reason != null ? reason : "User requested data deletion");
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "User data has been deleted/anonymized",
            "userId", id.toString()
        ));
    }
    
    /**
     * GET /api/users/{id}/consents
     * Get all user consents
     */
    @GetMapping("/{id}/consents")
    public ResponseEntity<Map<String, Object>> getUserConsents(@PathVariable Long id) {
        List<UserConsent> consents = consentService.getUserConsents(id);
        return ResponseEntity.ok(Map.of(
            "userId", id,
            "consents", consents,
            "count", consents.size()
        ));
    }
    
    /**
     * PUT /api/users/{id}/consents
     * Update user consent
     */
    @PutMapping("/{id}/consents")
    public ResponseEntity<UserConsent> updateConsent(
            @PathVariable Long id,
            @RequestBody ConsentRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        UserConsent consent = consentService.updateConsent(
            id,
            request.getConsentType(),
            request.getGranted(),
            request.getPurpose(),
            request.getVersion(),
            ipAddress,
            userAgent
        );
        
        return ResponseEntity.ok(consent);
    }
    
    /**
     * GET /api/users/{id}/privacy-dashboard
     * Get privacy dashboard with all privacy-related information
     */
    @GetMapping("/{id}/privacy-dashboard")
    public ResponseEntity<Map<String, Object>> getPrivacyDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = consentService.getPrivacyDashboard(id);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Convert DataExportRequest to response DTO
     */
    private DataExportResponse convertToResponse(DataExportRequest exportRequest) {
        DataExportResponse response = new DataExportResponse();
        response.setId(exportRequest.getId());
        response.setUserId(exportRequest.getUserId());
        response.setRequestId(exportRequest.getRequestId());
        response.setStatus(exportRequest.getStatus());
        response.setFileUrl(exportRequest.getFileUrl());
        response.setExpiresAt(exportRequest.getExpiresAt());
        response.setRequestedAt(exportRequest.getRequestedAt());
        response.setCompletedAt(exportRequest.getCompletedAt());
        response.setErrorMessage(exportRequest.getErrorMessage());
        response.setDataCategories(exportRequest.getDataCategories());
        return response;
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

