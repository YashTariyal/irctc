package com.irctc.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.payment.dto.GiftCardBalanceResponse;
import com.irctc.payment.dto.GiftCardPurchaseRequest;
import com.irctc.payment.dto.GiftCardPurchaseResponse;
import com.irctc.payment.dto.GiftCardRedeemRequest;
import com.irctc.payment.dto.VoucherValidationResponse;
import com.irctc.payment.service.GiftCardService;
import com.irctc.payment.service.VoucherService;
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
class GiftCardControllerTest {

    private MockMvc mockMvc;
    private GiftCardService giftCardService;
    private VoucherService voucherService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        giftCardService = Mockito.mock(GiftCardService.class);
        voucherService = Mockito.mock(VoucherService.class);
        GiftCardController controller = new GiftCardController(giftCardService, voucherService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldPurchaseGiftCard() throws Exception {
        GiftCardPurchaseResponse response = new GiftCardPurchaseResponse();
        response.setCode("GC-1234");
        when(giftCardService.purchaseGiftCard(any(GiftCardPurchaseRequest.class))).thenReturn(response);

        GiftCardPurchaseRequest request = new GiftCardPurchaseRequest();
        request.setAmount(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/gift-cards/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("GC-1234"));
    }

    @Test
    void shouldRedeemGiftCard() throws Exception {
        GiftCardBalanceResponse response = new GiftCardBalanceResponse();
        response.setBalance(BigDecimal.valueOf(300));
        when(giftCardService.redeemGiftCard(any(GiftCardRedeemRequest.class))).thenReturn(response);

        GiftCardRedeemRequest request = new GiftCardRedeemRequest();
        request.setCode("GC-1234");
        request.setAmount(BigDecimal.valueOf(200));

        mockMvc.perform(post("/api/gift-cards/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(300));
    }

    @Test
    void shouldValidateVoucher() throws Exception {
        VoucherValidationResponse response = new VoucherValidationResponse();
        response.setValid(true);
        when(voucherService.validateVoucher("SAVE20", BigDecimal.valueOf(200))).thenReturn(response);

        mockMvc.perform(get("/api/vouchers/validate")
                .param("code", "SAVE20")
                .param("orderAmount", "200"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.valid").value(true));
    }
}

