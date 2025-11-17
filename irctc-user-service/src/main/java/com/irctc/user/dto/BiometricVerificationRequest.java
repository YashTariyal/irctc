package com.irctc.user.dto;

import lombok.Data;

@Data
public class BiometricVerificationRequest {
    private Long userId;
    private String deviceId;
    private String biometricToken;
}

