package com.irctc.user.dto;

import lombok.Data;

@Data
public class BiometricVerificationResponse {
    private boolean verified;
    private String verificationId;
    private String message;
    private Long credentialId;
}

