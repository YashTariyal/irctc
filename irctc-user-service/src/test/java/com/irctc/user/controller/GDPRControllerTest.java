package com.irctc.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.user.dto.ConsentRequest;
import com.irctc.user.dto.DataExportResponse;
import com.irctc.user.entity.DataExportRequest;
import com.irctc.user.entity.UserConsent;
import com.irctc.user.service.ConsentService;
import com.irctc.user.service.GDPRService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GDPRControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private GDPRService gdprService;
    
    @Mock
    private ConsentService consentService;
    
    @InjectMocks
    private GDPRController gdprController;
    
    private ObjectMapper objectMapper;
    private DataExportRequest exportRequest;
    private UserConsent consent;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(gdprController)
                .setMessageConverters(converter)
                .build();
        
        exportRequest = new DataExportRequest();
        exportRequest.setId(1L);
        exportRequest.setUserId(123L);
        exportRequest.setRequestId("EXPORT_123456");
        exportRequest.setStatus("PROCESSING");
        exportRequest.setRequestedAt(LocalDateTime.now());
        
        consent = new UserConsent();
        consent.setId(1L);
        consent.setUserId(123L);
        consent.setConsentType("MARKETING");
        consent.setGranted(true);
    }
    
    @Test
    void testExportUserData() throws Exception {
        when(gdprService.exportUserData(123L)).thenReturn(exportRequest);
        
        mockMvc.perform(get("/api/users/123/export-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.requestId").value("EXPORT_123456"));
    }
    
    @Test
    void testGetExportData() throws Exception {
        when(gdprService.getExportRequestStatus("EXPORT_123456")).thenReturn(exportRequest);
        
        mockMvc.perform(get("/api/users/123/export-data/EXPORT_123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("EXPORT_123456"));
    }
    
    @Test
    void testGetExportRequests() throws Exception {
        when(gdprService.getUserExportRequests(123L))
            .thenReturn(Arrays.asList(exportRequest));
        
        mockMvc.perform(get("/api/users/123/export-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.exportRequests").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }
    
    @Test
    void testDeleteUserData() throws Exception {
        mockMvc.perform(delete("/api/users/123/data")
                .param("reason", "User requested deletion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.userId").value("123"));
    }
    
    @Test
    void testGetUserConsents() throws Exception {
        when(consentService.getUserConsents(123L))
            .thenReturn(Arrays.asList(consent));
        
        mockMvc.perform(get("/api/users/123/consents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.consents").isArray())
                .andExpect(jsonPath("$.count").value(1));
    }
    
    @Test
    void testUpdateConsent() throws Exception {
        when(consentService.updateConsent(anyLong(), anyString(), anyBoolean(), 
            any(), any(), any(), any()))
            .thenReturn(consent);
        
        ConsentRequest request = new ConsentRequest();
        request.setConsentType("MARKETING");
        request.setGranted(true);
        request.setPurpose("Marketing communications");
        request.setVersion("1.0");
        
        mockMvc.perform(put("/api/users/123/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("User-Agent", "TestAgent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consentType").value("MARKETING"))
                .andExpect(jsonPath("$.granted").value(true));
    }
    
    @Test
    void testGetPrivacyDashboard() throws Exception {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userId", 123L);
        dashboard.put("consents", Arrays.asList(consent));
        dashboard.put("consentCount", 1);
        
        when(consentService.getPrivacyDashboard(123L)).thenReturn(dashboard);
        
        mockMvc.perform(get("/api/users/123/privacy-dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.consentCount").value(1));
    }
}

