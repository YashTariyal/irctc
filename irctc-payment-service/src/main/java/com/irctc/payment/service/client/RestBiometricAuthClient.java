package com.irctc.payment.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class RestBiometricAuthClient implements BiometricAuthClient {

    private static final Logger logger = LoggerFactory.getLogger(RestBiometricAuthClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestBiometricAuthClient(RestTemplate restTemplate,
                                   @Value("${user.service.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean verifyBiometric(Long userId, String deviceId, String verificationToken) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("deviceId", deviceId);
            payload.put("biometricToken", verificationToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            Map response = restTemplate.postForEntity(
                baseUrl + "/api/auth/biometric/verify", entity, Map.class).getBody();

            return response != null && Boolean.TRUE.equals(response.get("verified"));
        } catch (Exception ex) {
            logger.warn("Biometric verification failed via user service, falling back to default denial. {}", ex.getMessage());
            return false;
        }
    }
}

