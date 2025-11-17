package com.irctc.payment.service;

import com.irctc.payment.dto.BiometricPaymentAuthorizationRequest;
import com.irctc.payment.dto.BiometricPaymentAuthorizationResponse;
import com.irctc.payment.entity.BiometricAuthorizationLog;
import com.irctc.payment.exception.CustomException;
import com.irctc.payment.repository.BiometricAuthorizationLogRepository;
import com.irctc.payment.service.client.BiometricAuthClient;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BiometricAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(BiometricAuthorizationService.class);

    private final BiometricAuthorizationLogRepository authorizationLogRepository;
    private final BiometricAuthClient biometricAuthClient;

    public BiometricAuthorizationService(BiometricAuthorizationLogRepository authorizationLogRepository,
                                         BiometricAuthClient biometricAuthClient) {
        this.authorizationLogRepository = authorizationLogRepository;
        this.biometricAuthClient = biometricAuthClient;
    }

    @Transactional
    public BiometricPaymentAuthorizationResponse authorize(BiometricPaymentAuthorizationRequest request) {
        boolean verified = biometricAuthClient.verifyBiometric(
            request.getUserId(),
            request.getDeviceId(),
            request.getVerificationToken());

        BiometricAuthorizationLog log = new BiometricAuthorizationLog();
        log.setPaymentId(request.getPaymentId());
        log.setUserId(request.getUserId());
        log.setAmount(request.getAmount());
        log.setCurrency(request.getCurrency());
        log.setVerificationId("BIO-" + UUID.randomUUID());
        log.setStatus(verified ? "APPROVED" : "REJECTED");
        log.setMessage(verified ? "Biometric authorization successful"
            : "Biometric token failed verification");
        if (TenantContext.hasTenant()) {
            log.setTenantId(TenantContext.getTenantId());
        }

        BiometricAuthorizationLog savedLog = authorizationLogRepository.save(log);
        logger.info("Biometric authorization {} for payment {}", log.getStatus(), request.getPaymentId());

        if (!verified) {
            throw new CustomException("Biometric verification failed");
        }

        BiometricPaymentAuthorizationResponse response = new BiometricPaymentAuthorizationResponse();
        response.setAuthorized(true);
        response.setVerificationId(savedLog.getVerificationId());
        response.setMessage("Biometric authorization successful");
        response.setLogId(savedLog.getId());
        return response;
    }
}

