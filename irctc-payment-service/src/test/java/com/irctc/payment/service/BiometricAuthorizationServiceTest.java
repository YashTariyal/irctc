package com.irctc.payment.service;

import com.irctc.payment.dto.BiometricPaymentAuthorizationRequest;
import com.irctc.payment.entity.BiometricAuthorizationLog;
import com.irctc.payment.repository.BiometricAuthorizationLogRepository;
import com.irctc.payment.service.client.BiometricAuthClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiometricAuthorizationServiceTest {

    @Mock
    private BiometricAuthorizationLogRepository logRepository;

    @Mock
    private BiometricAuthClient biometricAuthClient;

    @InjectMocks
    private BiometricAuthorizationService biometricAuthorizationService;

    @Test
    void shouldAuthorizePaymentWhenVerified() {
        when(biometricAuthClient.verifyBiometric(1L, "DEVICE-1", "token")).thenReturn(true);
        BiometricAuthorizationLog log = new BiometricAuthorizationLog();
        log.setId(10L);
        log.setVerificationId("BIO-1");
        log.setStatus("APPROVED");
        when(logRepository.save(any(BiometricAuthorizationLog.class))).thenReturn(log);

        BiometricPaymentAuthorizationRequest request = new BiometricPaymentAuthorizationRequest();
        request.setUserId(1L);
        request.setPaymentId(123L);
        request.setDeviceId("DEVICE-1");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setCurrency("INR");
        request.setVerificationToken("token");

        var response = biometricAuthorizationService.authorize(request);
        assertThat(response.isAuthorized()).isTrue();
        assertThat(response.getVerificationId()).isEqualTo("BIO-1");
    }

    @Test
    void shouldRejectWhenVerificationFails() {
        when(biometricAuthClient.verifyBiometric(1L, "DEVICE-1", "token")).thenReturn(false);
        BiometricAuthorizationLog log = new BiometricAuthorizationLog();
        log.setId(11L);
        log.setVerificationId("BIO-2");
        log.setStatus("REJECTED");
        when(logRepository.save(any(BiometricAuthorizationLog.class))).thenReturn(log);

        BiometricPaymentAuthorizationRequest request = new BiometricPaymentAuthorizationRequest();
        request.setUserId(1L);
        request.setPaymentId(123L);
        request.setDeviceId("DEVICE-1");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setCurrency("INR");
        request.setVerificationToken("token");

        assertThrows(com.irctc.payment.exception.CustomException.class,
            () -> biometricAuthorizationService.authorize(request));
    }
}

