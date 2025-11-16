package com.irctc.user.service;

import com.irctc.user.entity.UserConsent;
import com.irctc.user.exception.EntityNotFoundException;
import com.irctc.user.repository.DataExportRequestRepository;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.UserConsentRepository;
import com.irctc.user.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing user consents for GDPR compliance
 */
@Service
public class ConsentService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConsentService.class);
    
    @Autowired
    private UserConsentRepository consentRepository;
    
    @Autowired
    private SimpleUserRepository userRepository;
    
    @Autowired(required = false)
    private DataExportRequestRepository exportRequestRepository;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * Get all consents for a user
     */
    public List<UserConsent> getUserConsents(Long userId) {
        return consentRepository.findByUserId(userId);
    }
    
    /**
     * Update user consent
     */
    @Transactional
    public UserConsent updateConsent(Long userId, String consentType, Boolean granted, 
                                     String purpose, String version, String ipAddress, String userAgent) {
        logger.info("Updating consent for user: {}, type: {}, granted: {}", userId, consentType, granted);
        
        // Validate user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        Optional<UserConsent> existingConsent = consentRepository.findByUserIdAndConsentType(userId, consentType);
        
        UserConsent consent;
        if (existingConsent.isPresent()) {
            consent = existingConsent.get();
        } else {
            consent = new UserConsent();
            consent.setUserId(userId);
            consent.setConsentType(consentType);
        }
        
        consent.setGranted(granted);
        consent.setPurpose(purpose);
        consent.setVersion(version);
        consent.setIpAddress(ipAddress);
        consent.setUserAgent(userAgent);
        
        if (granted) {
            consent.setGrantedAt(LocalDateTime.now());
            consent.setWithdrawnAt(null);
        } else {
            consent.setWithdrawnAt(LocalDateTime.now());
            consent.setGrantedAt(null);
        }
        
        if (TenantContext.hasTenant()) {
            consent.setTenantId(TenantContext.getTenantId());
        }
        
        UserConsent savedConsent = consentRepository.save(consent);
        
        // Publish consent event
        publishConsentEvent(savedConsent);
        
        logger.info("✅ Consent updated for user: {}, type: {}, granted: {}", 
            userId, consentType, granted);
        
        return savedConsent;
    }
    
    /**
     * Check if user has granted consent
     */
    public boolean hasConsent(Long userId, String consentType) {
        Optional<UserConsent> consent = consentRepository.findActiveConsent(userId, consentType);
        return consent.isPresent();
    }
    
    /**
     * Get privacy dashboard data
     */
    public Map<String, Object> getPrivacyDashboard(Long userId) {
        logger.info("Getting privacy dashboard for user: {}", userId);
        
        userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        List<UserConsent> consents = consentRepository.findByUserId(userId);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userId", userId);
        dashboard.put("consents", consents);
        dashboard.put("consentCount", consents.size());
        dashboard.put("activeConsents", consents.stream().filter(c -> c.getGranted() != null && c.getGranted()).count());
        
        // Add export requests if available
        if (exportRequestRepository != null) {
            List<com.irctc.user.entity.DataExportRequest> exportRequests = exportRequestRepository.findByUserId(userId);
            dashboard.put("exportRequests", exportRequests);
            dashboard.put("exportCount", exportRequests.size());
        }
        
        return dashboard;
    }
    
    /**
     * Publish consent event
     */
    private void publishConsentEvent(UserConsent consent) {
        if (kafkaTemplate != null) {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("userId", consent.getUserId());
                event.put("consentType", consent.getConsentType());
                event.put("granted", consent.getGranted());
                event.put("purpose", consent.getPurpose());
                event.put("version", consent.getVersion());
                event.put("timestamp", LocalDateTime.now().toString());
                
                kafkaTemplate.send("user-consent-updated", consent.getUserId().toString(), event);
                logger.info("Published consent event: user={}, type={}, granted={}", 
                    consent.getUserId(), consent.getConsentType(), consent.getGranted());
            } catch (Exception e) {
                logger.error("Error publishing consent event: {}", e.getMessage(), e);
            }
        }
    }
}

