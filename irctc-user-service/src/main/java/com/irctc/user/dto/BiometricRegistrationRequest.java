package com.irctc.user.dto;

import lombok.Data;

@Data
public class BiometricRegistrationRequest {
    private Long userId;
    private String deviceId;
    private String biometricType;
    private String biometricTemplate;
    private String publicKey;
    private String deviceInfo;
}

