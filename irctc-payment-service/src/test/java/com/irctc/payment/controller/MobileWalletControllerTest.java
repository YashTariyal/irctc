package com.irctc.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.payment.dto.UpiPaymentRequest;
import com.irctc.payment.dto.UpiPaymentResponse;
import com.irctc.payment.dto.WalletPaymentRequest;
import com.irctc.payment.dto.WalletPaymentResponse;
import com.irctc.payment.service.MobileWalletIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MobileWalletControllerTest {

    private MockMvc mockMvc;
    private MobileWalletIntegrationService mobileWalletIntegrationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mobileWalletIntegrationService = Mockito.mock(MobileWalletIntegrationService.class);
        MobileWalletController controller = new MobileWalletController(mobileWalletIntegrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldInitiateUpiPayment() throws Exception {
        UpiPaymentResponse response = new UpiPaymentResponse();
        response.setOrderId("ORD-1");
        when(mobileWalletIntegrationService.initiateUpiPayment(any(UpiPaymentRequest.class))).thenReturn(response);

        UpiPaymentRequest request = new UpiPaymentRequest();
        request.setOrderId("ORD-1");
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("INR");
        request.setVpa("user@upi");

        mockMvc.perform(post("/api/payments/upi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value("ORD-1"));
    }

    @Test
    void shouldProcessWalletPayment() throws Exception {
        WalletPaymentResponse response = new WalletPaymentResponse();
        response.setStatus("COMPLETED");
        when(mobileWalletIntegrationService.processWalletPayment(any(WalletPaymentRequest.class))).thenReturn(response);

        WalletPaymentRequest request = new WalletPaymentRequest();
        request.setAmount(BigDecimal.valueOf(200));
        request.setCurrency("INR");
        request.setWalletProvider("PAYTM");

        mockMvc.perform(post("/api/payments/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldGenerateQrCode() throws Exception {
        when(mobileWalletIntegrationService.generateQrCode(BigDecimal.valueOf(100), "INR"))
            .thenReturn(new com.irctc.payment.dto.QrCodeResponse());

        mockMvc.perform(get("/api/payments/qr-code")
                .param("amount", "100")
                .param("currency", "INR"))
            .andExpect(status().isOk());
    }
}

