package com.irctc.payment.controller;

import com.irctc.payment.dto.QrCodeResponse;
import com.irctc.payment.dto.UpiPaymentRequest;
import com.irctc.payment.dto.UpiPaymentResponse;
import com.irctc.payment.dto.WalletPaymentRequest;
import com.irctc.payment.dto.WalletPaymentResponse;
import com.irctc.payment.service.MobileWalletIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class MobileWalletController {

    private final MobileWalletIntegrationService mobileWalletIntegrationService;

    public MobileWalletController(MobileWalletIntegrationService mobileWalletIntegrationService) {
        this.mobileWalletIntegrationService = mobileWalletIntegrationService;
    }

    @PostMapping("/upi")
    public ResponseEntity<UpiPaymentResponse> initiateUpiPayment(@RequestBody UpiPaymentRequest request) {
        return ResponseEntity.ok(mobileWalletIntegrationService.initiateUpiPayment(request));
    }

    @PostMapping("/wallet")
    public ResponseEntity<WalletPaymentResponse> processWalletPayment(@RequestBody WalletPaymentRequest request) {
        return ResponseEntity.ok(mobileWalletIntegrationService.processWalletPayment(request));
    }

    @GetMapping("/qr-code")
    public ResponseEntity<QrCodeResponse> generateQr(@RequestParam BigDecimal amount,
                                                     @RequestParam(defaultValue = "INR") String currency) {
        return ResponseEntity.ok(mobileWalletIntegrationService.generateQrCode(amount, currency));
    }
}

