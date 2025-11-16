package com.irctc.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.user.entity.DataExportRequest;
import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.SocialAccount;
import com.irctc.user.exception.EntityNotFoundException;
import com.irctc.user.repository.DataExportRequestRepository;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.SocialAccountRepository;
import com.irctc.user.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for GDPR compliance features
 */
@Service
public class GDPRService {
    
    private static final Logger logger = LoggerFactory.getLogger(GDPRService.class);
    
    private static final int EXPORT_FILE_EXPIRY_DAYS = 30;
    
    @Autowired
    private SimpleUserRepository userRepository;
    
    @Autowired
    private DataExportRequestRepository exportRequestRepository;
    
    @Autowired
    private SocialAccountRepository socialAccountRepository;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Export all user data
     */
    @Transactional
    public DataExportRequest exportUserData(Long userId) {
        logger.info("Exporting data for user: {}", userId);
        
        SimpleUser user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        // Create export request
        DataExportRequest exportRequest = new DataExportRequest();
        exportRequest.setUserId(userId);
        exportRequest.setStatus("PROCESSING");
        exportRequest.setRequestedAt(LocalDateTime.now());
        exportRequest.setExpiresAt(LocalDateTime.now().plusDays(EXPORT_FILE_EXPIRY_DAYS));
        // Ensure requestId is set (PrePersist will set it, but set it explicitly for tests)
        if (exportRequest.getRequestId() == null) {
            exportRequest.setRequestId("EXPORT_" + System.currentTimeMillis() + "_" + userId);
        }
        
        if (TenantContext.hasTenant()) {
            exportRequest.setTenantId(TenantContext.getTenantId());
        }
        
        DataExportRequest savedRequest = exportRequestRepository.save(exportRequest);
        
        // Process export asynchronously
        processDataExportAsync(savedRequest, user);
        
        return savedRequest;
    }
    
    /**
     * Process data export asynchronously
     */
    @Async
    public CompletableFuture<Void> processDataExportAsync(DataExportRequest exportRequest, SimpleUser user) {
        try {
            logger.info("Processing data export for user: {}, requestId: {}", 
                user.getId(), exportRequest.getRequestId());
            
            // Collect user data
            Map<String, Object> userData = new HashMap<>();
            
            // User profile data
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("roles", user.getRoles());
            profile.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            profile.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
            userData.put("profile", profile);
            
            // Social accounts
            List<SocialAccount> socialAccounts = socialAccountRepository.findByUserId(user.getId());
            List<Map<String, Object>> socialAccountsData = new ArrayList<>();
            for (SocialAccount account : socialAccounts) {
                Map<String, Object> accountData = new HashMap<>();
                accountData.put("provider", account.getProvider());
                accountData.put("providerEmail", account.getProviderEmail());
                accountData.put("providerName", account.getProviderName());
                accountData.put("linkedAt", account.getLinkedAt() != null ? account.getLinkedAt().toString() : null);
                accountData.put("lastUsedAt", account.getLastUsedAt() != null ? account.getLastUsedAt().toString() : null);
                socialAccountsData.add(accountData);
            }
            userData.put("socialAccounts", socialAccountsData);
            
            // Fetch data from other services (simulated - in production, call actual services)
            userData.put("bookings", fetchBookingsFromBookingService(user.getId()));
            userData.put("payments", fetchPaymentsFromPaymentService(user.getId()));
            userData.put("notifications", fetchNotificationsFromNotificationService(user.getId()));
            
            // Convert to JSON
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userData);
            
            // In production, save to file storage (S3, Azure Blob, etc.)
            // For now, store in database or return as response
            exportRequest.setStatus("COMPLETED");
            exportRequest.setCompletedAt(LocalDateTime.now());
            exportRequest.setDataCategories("PROFILE,SOCIAL_ACCOUNTS,BOOKINGS,PAYMENTS,NOTIFICATIONS");
            // In production: exportRequest.setFileUrl("https://storage.example.com/exports/" + exportRequest.getRequestId() + ".json");
            exportRequest.setFileUrl("/api/users/" + user.getId() + "/export-data/" + exportRequest.getRequestId());
            
            // Save updated request
            if (exportRequestRepository != null) {
                exportRequestRepository.save(exportRequest);
            }
            
            // Publish export event
            publishDataExportEvent(exportRequest);
            
            logger.info("✅ Data export completed for user: {}, requestId: {}", 
                user.getId(), exportRequest.getRequestId());
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Error processing data export for user {}: {}", 
                user.getId(), e.getMessage(), e);
            exportRequest.setStatus("FAILED");
            exportRequest.setErrorMessage(e.getMessage());
            exportRequestRepository.save(exportRequest);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Fetch bookings from booking service (simulated)
     */
    private List<Map<String, Object>> fetchBookingsFromBookingService(Long userId) {
        // In production, call booking service via Feign client
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Fetch payments from payment service (simulated)
     */
    private List<Map<String, Object>> fetchPaymentsFromPaymentService(Long userId) {
        // In production, call payment service via Feign client
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Fetch notifications from notification service (simulated)
     */
    private List<Map<String, Object>> fetchNotificationsFromNotificationService(Long userId) {
        // In production, call notification service via Feign client
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Delete all user data (Right to be forgotten)
     */
    @Transactional
    public void deleteUserData(Long userId, String reason) {
        logger.info("Deleting all data for user: {}, reason: {}", userId, reason);
        
        SimpleUser user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        // Anonymize user data instead of hard delete (GDPR best practice)
        user.setEmail("deleted_" + System.currentTimeMillis() + "@deleted.irctc");
        user.setUsername("deleted_" + System.currentTimeMillis());
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPhoneNumber(null);
        user.setPassword("DELETED_" + UUID.randomUUID().toString());
        userRepository.save(user);
        
        // Delete social accounts
        List<SocialAccount> socialAccounts = socialAccountRepository.findByUserId(userId);
        socialAccountRepository.deleteAll(socialAccounts);
        
        // Publish deletion event to other services
        publishDataDeletionEvent(userId, reason);
        
        logger.info("✅ User data deleted/anonymized for user: {}", userId);
    }
    
    /**
     * Get export request status
     */
    public DataExportRequest getExportRequestStatus(String requestId) {
        return exportRequestRepository.findByRequestId(requestId)
            .orElseThrow(() -> new EntityNotFoundException("DataExportRequest", requestId));
    }
    
    /**
     * Get all export requests for a user
     */
    public List<DataExportRequest> getUserExportRequests(Long userId) {
        return exportRequestRepository.findByUserId(userId);
    }
    
    /**
     * Publish data export event
     */
    private void publishDataExportEvent(DataExportRequest exportRequest) {
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("requestId", exportRequest.getRequestId());
                event.put("userId", exportRequest.getUserId());
                event.put("status", exportRequest.getStatus());
                event.put("fileUrl", exportRequest.getFileUrl());
                event.put("expiresAt", exportRequest.getExpiresAt());
                
                kafkaTemplate.send("data-export-completed", exportRequest.getRequestId(), event);
                logger.info("Published data export event: {}", exportRequest.getRequestId());
            } catch (Exception e) {
                logger.error("Error publishing data export event: {}", e.getMessage(), e);
            }
        }
    }
    
    /**
     * Publish data deletion event
     */
    private void publishDataDeletionEvent(Long userId, String reason) {
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("userId", userId);
                event.put("reason", reason);
                event.put("timestamp", LocalDateTime.now().toString());
                event.put("type", "DATA_DELETION");
                
                kafkaTemplate.send("data-deletion-requested", userId.toString(), event);
                logger.info("Published data deletion event for user: {}", userId);
            } catch (Exception e) {
                logger.error("Error publishing data deletion event: {}", e.getMessage(), e);
            }
        }
    }
}

