package com.irctc.user.service;

import com.irctc.user.dto.BiometricRegistrationRequest;
import com.irctc.user.dto.BiometricRegistrationResponse;
import com.irctc.user.dto.BiometricVerificationRequest;
import com.irctc.user.dto.BiometricVerificationResponse;
import com.irctc.user.entity.BiometricCredential;
import com.irctc.user.exception.CustomException;
import com.irctc.user.repository.BiometricCredentialRepository;
import com.irctc.user.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BiometricAuthService {

    private final BiometricCredentialRepository biometricCredentialRepository;

    public BiometricAuthService(BiometricCredentialRepository biometricCredentialRepository) {
        this.biometricCredentialRepository = biometricCredentialRepository;
    }

    @Transactional
    public BiometricRegistrationResponse registerBiometric(BiometricRegistrationRequest request) {
        validateRegistrationRequest(request);
        BiometricCredential credential = biometricCredentialRepository
            .findByUserIdAndDeviceId(request.getUserId(), request.getDeviceId())
            .orElseGet(BiometricCredential::new);

        credential.setUserId(request.getUserId());
        credential.setDeviceId(request.getDeviceId());
        credential.setBiometricType(request.getBiometricType());
        credential.setTemplateHash(hashTemplate(request.getBiometricTemplate()));
        credential.setPublicKey(request.getPublicKey());
        credential.setDeviceInfo(request.getDeviceInfo());
        credential.setStatus("ACTIVE");
        credential.setRegisteredAt(LocalDateTime.now());

        if (TenantContext.hasTenant()) {
            credential.setTenantId(TenantContext.getTenantId());
        }

        BiometricCredential saved = biometricCredentialRepository.save(credential);
        BiometricRegistrationResponse response = new BiometricRegistrationResponse();
        response.setCredentialId(saved.getId());
        response.setStatus(saved.getStatus());
        response.setDeviceId(saved.getDeviceId());
        response.setBiometricType(saved.getBiometricType());
        return response;
    }

    @Transactional
    public BiometricVerificationResponse verifyBiometric(BiometricVerificationRequest request) {
        BiometricCredential credential = biometricCredentialRepository
            .findByUserIdAndDeviceId(request.getUserId(), request.getDeviceId())
            .orElseThrow(() -> new CustomException("No biometric profile found for device: " + request.getDeviceId()));

        String incomingHash = hashTemplate(request.getBiometricToken());
        boolean verified = credential.getTemplateHash().equals(incomingHash);
        credential.setLastVerifiedAt(LocalDateTime.now());
        credential.setStatus(verified ? "ACTIVE" : "LOCKED");
        biometricCredentialRepository.save(credential);

        BiometricVerificationResponse response = new BiometricVerificationResponse();
        response.setVerified(verified);
        response.setCredentialId(credential.getId());
        response.setVerificationId("VER-" + UUID.randomUUID());
        response.setMessage(verified ? "Biometric verification successful" : "Biometric verification failed");
        return response;
    }

    private void validateRegistrationRequest(BiometricRegistrationRequest request) {
        if (request.getUserId() == null) {
            throw new CustomException("userId is required");
        }
        if (request.getDeviceId() == null) {
            throw new CustomException("deviceId is required");
        }
        if (request.getBiometricTemplate() == null) {
            throw new CustomException("biometricTemplate is required");
        }
        if (request.getBiometricType() == null) {
            throw new CustomException("biometricType is required");
        }
    }

    private String hashTemplate(String template) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(template.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CustomException("Failed to hash biometric template");
        }
    }
}

