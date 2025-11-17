package com.irctc.user.dto;

import lombok.Data;

@Data
public class BiometricRegistrationResponse {
    private Long credentialId;
    private String status;
    private String deviceId;
    private String biometricType;
}

