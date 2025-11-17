package com.irctc.payment.service;

import com.irctc.payment.dto.UpiPaymentRequest;
import com.irctc.payment.dto.WalletPaymentRequest;
import com.irctc.payment.entity.MobileWalletTransaction;
import com.irctc.payment.entity.UpiPaymentIntent;
import com.irctc.payment.repository.MobileWalletTransactionRepository;
import com.irctc.payment.repository.UpiPaymentIntentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileWalletIntegrationServiceTest {

    @Mock
    private UpiPaymentIntentRepository upiPaymentIntentRepository;

    @Mock
    private MobileWalletTransactionRepository mobileWalletTransactionRepository;

    @Mock
    private PaymentQrCodeService paymentQrCodeService;

    @InjectMocks
    private MobileWalletIntegrationService mobileWalletIntegrationService;

    private UpiPaymentIntent intent;
    private MobileWalletTransaction walletTransaction;

    @BeforeEach
    void setUp() {
        intent = new UpiPaymentIntent();
        intent.setId(1L);
        intent.setOrderId("ORD-1");
        intent.setAmount(BigDecimal.valueOf(500));
        intent.setCurrency("INR");
        intent.setStatus("PENDING");

        walletTransaction = new MobileWalletTransaction();
        walletTransaction.setId(5L);
        walletTransaction.setWalletReference("WALLET-1234");
        walletTransaction.setAmount(BigDecimal.valueOf(500));
        walletTransaction.setCurrency("INR");
        walletTransaction.setStatus("COMPLETED");
        walletTransaction.setProcessedAt(LocalDateTime.now());
    }

    @Test
    void shouldInitiateUpiPayment() {
        when(upiPaymentIntentRepository.save(any(UpiPaymentIntent.class))).thenReturn(intent);
        when(paymentQrCodeService.generateQr(any(String.class), any(BigDecimal.class), any(String.class)))
            .thenReturn(new com.irctc.payment.dto.QrCodeResponse());

        var request = new UpiPaymentRequest();
        request.setOrderId("ORD-1");
        request.setAmount(BigDecimal.valueOf(500));
        request.setCurrency("INR");
        request.setVpa("user@upi");

        var response = mobileWalletIntegrationService.initiateUpiPayment(request);

        assertThat(response.getOrderId()).isEqualTo("ORD-1");
    }

    @Test
    void shouldProcessWalletPayment() {
        when(mobileWalletTransactionRepository.save(any(MobileWalletTransaction.class))).thenReturn(walletTransaction);

        WalletPaymentRequest request = new WalletPaymentRequest();
        request.setAmount(BigDecimal.valueOf(500));
        request.setCurrency("INR");
        request.setWalletProvider("PAYTM");
        request.setWalletToken("token-1234");

        var response = mobileWalletIntegrationService.processWalletPayment(request);
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
    }
}

