package com.irctc.user.controller;

import com.irctc.user.dto.BiometricRegistrationRequest;
import com.irctc.user.dto.BiometricRegistrationResponse;
import com.irctc.user.dto.BiometricVerificationRequest;
import com.irctc.user.dto.BiometricVerificationResponse;
import com.irctc.user.service.BiometricAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/biometric")
public class BiometricAuthController {

    private final BiometricAuthService biometricAuthService;

    public BiometricAuthController(BiometricAuthService biometricAuthService) {
        this.biometricAuthService = biometricAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<BiometricRegistrationResponse> register(@RequestBody BiometricRegistrationRequest request) {
        return ResponseEntity.ok(biometricAuthService.registerBiometric(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<BiometricVerificationResponse> verify(@RequestBody BiometricVerificationRequest request) {
        return ResponseEntity.ok(biometricAuthService.verifyBiometric(request));
    }
}

