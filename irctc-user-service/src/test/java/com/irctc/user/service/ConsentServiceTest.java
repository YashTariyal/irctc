package com.irctc.user.service;

import com.irctc.user.entity.SimpleUser;
import com.irctc.user.entity.UserConsent;
import com.irctc.user.exception.EntityNotFoundException;
import com.irctc.user.repository.DataExportRequestRepository;
import com.irctc.user.repository.SimpleUserRepository;
import com.irctc.user.repository.UserConsentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {
    
    @Mock
    private UserConsentRepository consentRepository;
    
    @Mock
    private SimpleUserRepository userRepository;
    
    @Mock
    private DataExportRequestRepository exportRequestRepository;
    
    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;
    
    @InjectMocks
    private ConsentService consentService;
    
    private SimpleUser user;
    private UserConsent consent;
    
    @BeforeEach
    void setUp() {
        user = new SimpleUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        
        consent = new UserConsent();
        consent.setId(1L);
        consent.setUserId(1L);
        consent.setConsentType("MARKETING");
        consent.setGranted(true);
        consent.setPurpose("Marketing communications");
    }
    
    @Test
    void testGetUserConsents() {
        when(consentRepository.findByUserId(1L))
            .thenReturn(List.of(consent));
        
        List<UserConsent> result = consentService.getUserConsents(1L);
        
        assertEquals(1, result.size());
        assertEquals("MARKETING", result.get(0).getConsentType());
    }
    
    @Test
    void testUpdateConsent_New() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(consentRepository.findByUserIdAndConsentType(1L, "MARKETING"))
            .thenReturn(Optional.empty());
        when(consentRepository.save(any(UserConsent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        UserConsent result = consentService.updateConsent(
            1L, "MARKETING", true, "Marketing", "1.0", "127.0.0.1", "TestAgent"
        );
        
        assertNotNull(result);
        assertEquals("MARKETING", result.getConsentType());
        assertTrue(result.getGranted());
        verify(consentRepository, times(1)).save(any(UserConsent.class));
    }
    
    @Test
    void testUpdateConsent_Existing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(consentRepository.findByUserIdAndConsentType(1L, "MARKETING"))
            .thenReturn(Optional.of(consent));
        when(consentRepository.save(any(UserConsent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        UserConsent result = consentService.updateConsent(
            1L, "MARKETING", false, "Marketing", "1.0", "127.0.0.1", "TestAgent"
        );
        
        assertNotNull(result);
        assertFalse(result.getGranted());
        verify(consentRepository, times(1)).save(any(UserConsent.class));
    }
    
    @Test
    void testUpdateConsent_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, () -> {
            consentService.updateConsent(
                1L, "MARKETING", true, "Marketing", "1.0", "127.0.0.1", "TestAgent"
            );
        });
    }
    
    @Test
    void testHasConsent_True() {
        when(consentRepository.findActiveConsent(1L, "MARKETING"))
            .thenReturn(Optional.of(consent));
        
        boolean result = consentService.hasConsent(1L, "MARKETING");
        
        assertTrue(result);
    }
    
    @Test
    void testHasConsent_False() {
        when(consentRepository.findActiveConsent(1L, "MARKETING"))
            .thenReturn(Optional.empty());
        
        boolean result = consentService.hasConsent(1L, "MARKETING");
        
        assertFalse(result);
    }
    
    @Test
    void testGetPrivacyDashboard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(consentRepository.findByUserId(1L))
            .thenReturn(List.of(consent));
        when(exportRequestRepository.findByUserId(1L))
            .thenReturn(new ArrayList<>());
        
        java.util.Map<String, Object> result = consentService.getPrivacyDashboard(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.get("userId"));
        assertNotNull(result.get("consents"));
    }
}

