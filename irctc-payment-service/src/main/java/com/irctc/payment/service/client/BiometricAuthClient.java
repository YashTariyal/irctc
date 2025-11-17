package com.irctc.payment.service.client;

public interface BiometricAuthClient {
    boolean verifyBiometric(Long userId, String deviceId, String verificationToken);
}

