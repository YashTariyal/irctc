package com.irctc.payment.service;

import com.irctc.payment.dto.QrCodeResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class PaymentQrCodeService {

    public QrCodeResponse generateQr(String referenceId, BigDecimal amount, String currency) {
        String payload = "upi://pay?pa=irctc@upi&pn=IRCTC&am=" + amount + "&cu=" + currency + "&tr=" + referenceId;
        String encoded = Base64.getEncoder().encodeToString(payload.getBytes());

        QrCodeResponse response = new QrCodeResponse();
        response.setReferenceId(referenceId);
        response.setQrPayload(encoded);
        response.setAmount(amount);
        response.setCurrency(currency);
        response.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        return response;
    }

    public QrCodeResponse generateQr(BigDecimal amount, String currency) {
        return generateQr("QR-" + UUID.randomUUID(), amount, currency);
    }
}

