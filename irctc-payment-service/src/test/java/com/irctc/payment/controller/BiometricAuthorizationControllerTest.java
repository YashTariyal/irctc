package com.irctc.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.payment.dto.BiometricPaymentAuthorizationRequest;
import com.irctc.payment.dto.BiometricPaymentAuthorizationResponse;
import com.irctc.payment.service.BiometricAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BiometricAuthorizationControllerTest {

    private MockMvc mockMvc;
    private BiometricAuthorizationService biometricAuthorizationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        biometricAuthorizationService = Mockito.mock(BiometricAuthorizationService.class);
        BiometricAuthorizationController controller = new BiometricAuthorizationController(biometricAuthorizationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldAuthorizePayment() throws Exception {
        BiometricPaymentAuthorizationResponse response = new BiometricPaymentAuthorizationResponse();
        response.setAuthorized(true);
        response.setVerificationId("BIO-1");
        when(biometricAuthorizationService.authorize(any(BiometricPaymentAuthorizationRequest.class))).thenReturn(response);

        BiometricPaymentAuthorizationRequest request = new BiometricPaymentAuthorizationRequest();
        request.setUserId(1L);
        request.setPaymentId(10L);
        request.setDeviceId("DEVICE-1");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setCurrency("INR");
        request.setVerificationToken("token");

        mockMvc.perform(post("/api/payments/biometric-authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authorized").value(true));
    }
}

