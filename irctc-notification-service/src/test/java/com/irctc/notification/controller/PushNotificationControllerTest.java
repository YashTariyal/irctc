package com.irctc.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.notification.dto.NotificationRequest;
import com.irctc.notification.dto.NotificationResponse;
import com.irctc.notification.dto.PushNotificationRequest;
import com.irctc.notification.dto.PushNotificationResponse;
import com.irctc.notification.entity.UserDeviceToken;
import com.irctc.notification.service.PushNotificationService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationControllerTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @InjectMocks
    private PushNotificationController pushNotificationController;
    
    private ObjectMapper objectMapper;
    
    private PushNotificationResponse pushResponse;
    private NotificationResponse notificationResponse;
    private UserDeviceToken deviceToken;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(pushNotificationController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
        
        pushResponse = new PushNotificationResponse();
        pushResponse.setNotificationId("notif_123");
        pushResponse.setMessageId("fcm_123");
        pushResponse.setStatus("SUCCESS");
        pushResponse.setSentTime(LocalDateTime.now());
        pushResponse.setSuccessCount(1);
        pushResponse.setFailureCount(0);
        
        notificationResponse = new NotificationResponse();
        notificationResponse.setChannel("PUSH");
        notificationResponse.setStatus("SUCCESS");
        notificationResponse.setSentTime(LocalDateTime.now());
        
        deviceToken = new UserDeviceToken();
        deviceToken.setId(1L);
        deviceToken.setUserId(123L);
        deviceToken.setToken("fcm_token_123");
        deviceToken.setPlatform("ANDROID");
    }
    
    @Test
    void testSendPushNotification() throws Exception {
        // Service is injected via @InjectMocks, so it's not null
        when(pushNotificationService.sendPushNotification(any(NotificationRequest.class)))
            .thenReturn(notificationResponse);
        
        NotificationRequest request = new NotificationRequest();
        request.setUserId(123L);
        request.setSubject("Test");
        request.setMessage("Test message");
        request.setNotificationType("PUSH");
        
        mockMvc.perform(post("/api/notifications/push")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("PUSH"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
    
    @Test
    void testSendRichPushNotification() throws Exception {
        when(pushNotificationService.sendRichPushNotification(any(PushNotificationRequest.class)))
            .thenReturn(pushResponse);
        
        PushNotificationRequest request = new PushNotificationRequest();
        request.setUserId(123L);
        request.setTitle("Test");
        request.setBody("Test body");
        request.setImageUrl("https://example.com/image.jpg");
        
        mockMvc.perform(post("/api/notifications/push/rich")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.successCount").value(1));
    }
    
    @Test
    void testRegisterDevice() throws Exception {
        mockMvc.perform(post("/api/notifications/push/register")
                .param("userId", "123")
                .param("token", "fcm_token_123")
                .param("platform", "ANDROID")
                .param("deviceId", "device_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
    
    @Test
    void testUnregisterDevice() throws Exception {
        mockMvc.perform(delete("/api/notifications/push/unregister")
                .param("token", "fcm_token_123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
    
    @Test
    void testGetUserDevices() throws Exception {
        when(pushNotificationService.getUserDevices(123L))
            .thenReturn(Arrays.asList(deviceToken));
        
        mockMvc.perform(get("/api/notifications/push/devices/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.devices").isArray());
    }
}

