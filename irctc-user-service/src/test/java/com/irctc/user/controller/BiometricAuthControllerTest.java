package com.irctc.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.user.dto.BiometricRegistrationRequest;
import com.irctc.user.dto.BiometricRegistrationResponse;
import com.irctc.user.dto.BiometricVerificationRequest;
import com.irctc.user.dto.BiometricVerificationResponse;
import com.irctc.user.service.BiometricAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BiometricAuthControllerTest {

    private MockMvc mockMvc;
    private BiometricAuthService biometricAuthService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        biometricAuthService = Mockito.mock(BiometricAuthService.class);
        BiometricAuthController controller = new BiometricAuthController(biometricAuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldRegisterBiometricProfile() throws Exception {
        BiometricRegistrationResponse response = new BiometricRegistrationResponse();
        response.setCredentialId(5L);
        response.setStatus("ACTIVE");
        when(biometricAuthService.registerBiometric(any(BiometricRegistrationRequest.class))).thenReturn(response);

        BiometricRegistrationRequest request = new BiometricRegistrationRequest();
        request.setUserId(1L);
        request.setDeviceId("device-123");
        request.setBiometricTemplate("sample");
        request.setBiometricType("FACE");

        mockMvc.perform(post("/api/auth/biometric/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.credentialId").value(5L));
    }

    @Test
    void shouldVerifyBiometricProfile() throws Exception {
        BiometricVerificationResponse response = new BiometricVerificationResponse();
        response.setVerified(true);
        response.setVerificationId("VER-123");
        when(biometricAuthService.verifyBiometric(any(BiometricVerificationRequest.class))).thenReturn(response);

        BiometricVerificationRequest request = new BiometricVerificationRequest();
        request.setUserId(1L);
        request.setDeviceId("device-123");
        request.setBiometricToken("token");

        mockMvc.perform(post("/api/auth/biometric/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").value(true));
    }
}

