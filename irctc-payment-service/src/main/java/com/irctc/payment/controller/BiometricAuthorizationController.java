package com.irctc.payment.controller;

import com.irctc.payment.dto.BiometricPaymentAuthorizationRequest;
import com.irctc.payment.dto.BiometricPaymentAuthorizationResponse;
import com.irctc.payment.service.BiometricAuthorizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class BiometricAuthorizationController {

    private final BiometricAuthorizationService biometricAuthorizationService;

    public BiometricAuthorizationController(BiometricAuthorizationService biometricAuthorizationService) {
        this.biometricAuthorizationService = biometricAuthorizationService;
    }

    @PostMapping("/biometric-authorize")
    public ResponseEntity<BiometricPaymentAuthorizationResponse> authorize(@RequestBody BiometricPaymentAuthorizationRequest request) {
        return ResponseEntity.ok(biometricAuthorizationService.authorize(request));
    }
}

