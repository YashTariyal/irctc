package com.irctc.user.service;

import com.irctc.user.dto.BiometricRegistrationRequest;
import com.irctc.user.dto.BiometricVerificationRequest;
import com.irctc.user.dto.BiometricVerificationResponse;
import com.irctc.user.entity.BiometricCredential;
import com.irctc.user.exception.CustomException;
import com.irctc.user.repository.BiometricCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BiometricAuthServiceTest {

    @Mock
    private BiometricCredentialRepository biometricCredentialRepository;

    @InjectMocks
    private BiometricAuthService biometricAuthService;

    private BiometricCredential credential;

    @BeforeEach
    void setUp() {
        credential = new BiometricCredential();
        credential.setId(1L);
        credential.setUserId(1L);
        credential.setDeviceId("device-123");
        credential.setBiometricType("FINGERPRINT");
        credential.setTemplateHash(hash("sample-template"));
        credential.setStatus("ACTIVE");
        credential.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void shouldRegisterBiometricCredential() {
        BiometricRegistrationRequest request = new BiometricRegistrationRequest();
        request.setUserId(1L);
        request.setDeviceId("device-123");
        request.setBiometricTemplate("sample-template");
        request.setBiometricType("FINGERPRINT");

        when(biometricCredentialRepository.findByUserIdAndDeviceId(1L, "device-123"))
            .thenReturn(Optional.empty());
        when(biometricCredentialRepository.save(any(BiometricCredential.class))).thenReturn(credential);

        var response = biometricAuthService.registerBiometric(request);

        assertThat(response.getCredentialId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldVerifyBiometricCredential() {
        when(biometricCredentialRepository.findByUserIdAndDeviceId(1L, "device-123"))
            .thenReturn(Optional.of(credential));
        when(biometricCredentialRepository.save(any(BiometricCredential.class))).thenReturn(credential);

        BiometricVerificationRequest request = new BiometricVerificationRequest();
        request.setUserId(1L);
        request.setDeviceId("device-123");
        request.setBiometricToken("sample-template");

        BiometricVerificationResponse response = biometricAuthService.verifyBiometric(request);
        assertThat(response.isVerified()).isTrue();
    }

    @Test
    void shouldThrowWhenCredentialMissing() {
        when(biometricCredentialRepository.findByUserIdAndDeviceId(1L, "missing-device"))
            .thenReturn(Optional.empty());

        BiometricVerificationRequest request = new BiometricVerificationRequest();
        request.setUserId(1L);
        request.setDeviceId("missing-device");
        request.setBiometricToken("token");

        assertThrows(CustomException.class, () -> biometricAuthService.verifyBiometric(request));
    }

    private BiometricRegistrationRequest createRegistrationRequest() {
        BiometricRegistrationRequest request = new BiometricRegistrationRequest();
        request.setUserId(1L);
        request.setDeviceId("device-123");
        request.setBiometricType("FINGERPRINT");
        request.setBiometricTemplate("sample-template");
        return request;
    }

    private String hash(String template) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

