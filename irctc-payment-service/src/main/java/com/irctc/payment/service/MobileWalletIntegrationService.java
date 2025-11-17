package com.irctc.payment.service;

import com.irctc.payment.dto.QrCodeResponse;
import com.irctc.payment.dto.UpiPaymentRequest;
import com.irctc.payment.dto.UpiPaymentResponse;
import com.irctc.payment.dto.WalletPaymentRequest;
import com.irctc.payment.dto.WalletPaymentResponse;
import com.irctc.payment.entity.MobileWalletTransaction;
import com.irctc.payment.entity.UpiPaymentIntent;
import com.irctc.payment.repository.MobileWalletTransactionRepository;
import com.irctc.payment.repository.UpiPaymentIntentRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MobileWalletIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(MobileWalletIntegrationService.class);

    private final UpiPaymentIntentRepository upiPaymentIntentRepository;
    private final MobileWalletTransactionRepository mobileWalletTransactionRepository;
    private final PaymentQrCodeService paymentQrCodeService;

    public MobileWalletIntegrationService(UpiPaymentIntentRepository upiPaymentIntentRepository,
                                          MobileWalletTransactionRepository mobileWalletTransactionRepository,
                                          PaymentQrCodeService paymentQrCodeService) {
        this.upiPaymentIntentRepository = upiPaymentIntentRepository;
        this.mobileWalletTransactionRepository = mobileWalletTransactionRepository;
        this.paymentQrCodeService = paymentQrCodeService;
    }

    @Transactional
    public UpiPaymentResponse initiateUpiPayment(UpiPaymentRequest request) {
        UpiPaymentIntent intent = new UpiPaymentIntent();
        intent.setOrderId(request.getOrderId());
        intent.setBookingId(request.getBookingId());
        intent.setUserId(request.getUserId());
        intent.setAmount(request.getAmount());
        intent.setCurrency(request.getCurrency());
        intent.setVpa(request.getVpa());
        intent.setStatus("PENDING");
        if (TenantContext.hasTenant()) {
            intent.setTenantId(TenantContext.getTenantId());
        }

        UpiPaymentIntent savedIntent = upiPaymentIntentRepository.save(intent);
        QrCodeResponse qrCodeResponse = paymentQrCodeService.generateQr(savedIntent.getOrderId(),
            savedIntent.getAmount(), savedIntent.getCurrency());

        savedIntent.setQrPayload(qrCodeResponse.getQrPayload());
        savedIntent.setExpiresAt(qrCodeResponse.getExpiresAt());
        savedIntent.setStatus("COMPLETED");
        savedIntent.setUtr("UTR" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        savedIntent.setProcessedAt(LocalDateTime.now());
        upiPaymentIntentRepository.save(savedIntent);

        logger.info("✅ UPI payment completed for order {}", savedIntent.getOrderId());

        UpiPaymentResponse response = new UpiPaymentResponse();
        response.setIntentId(savedIntent.getId());
        response.setOrderId(savedIntent.getOrderId());
        response.setStatus(savedIntent.getStatus());
        response.setUtr(savedIntent.getUtr());
        response.setQrPayload(savedIntent.getQrPayload());
        response.setExpiresAt(savedIntent.getExpiresAt());
        response.setAmount(savedIntent.getAmount());
        response.setCurrency(savedIntent.getCurrency());
        return response;
    }

    @Transactional
    public WalletPaymentResponse processWalletPayment(WalletPaymentRequest request) {
        MobileWalletTransaction transaction = new MobileWalletTransaction();
        transaction.setWalletReference("WALLET-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        transaction.setWalletProvider(request.getWalletProvider());
        transaction.setBookingId(request.getBookingId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDeviceInfo(request.getDeviceInfo());
        transaction.setTokenLastFour(extractLastFour(request.getWalletToken()));
        transaction.setStatus("COMPLETED");
        transaction.setProcessedAt(LocalDateTime.now());
        if (TenantContext.hasTenant()) {
            transaction.setTenantId(TenantContext.getTenantId());
        }

        MobileWalletTransaction saved = mobileWalletTransactionRepository.save(transaction);
        logger.info("✅ Mobile wallet payment completed with reference {}", saved.getWalletReference());

        WalletPaymentResponse response = new WalletPaymentResponse();
        response.setTransactionId(saved.getId());
        response.setWalletReference(saved.getWalletReference());
        response.setStatus(saved.getStatus());
        response.setAmount(saved.getAmount());
        response.setCurrency(saved.getCurrency());
        response.setProcessedAt(saved.getProcessedAt());
        return response;
    }

    public QrCodeResponse generateQrCode(java.math.BigDecimal amount, String currency) {
        return paymentQrCodeService.generateQr(amount, currency);
    }

    private String extractLastFour(String token) {
        if (token == null || token.length() < 4) {
            return "****";
        }
        return token.substring(token.length() - 4);
    }
}

