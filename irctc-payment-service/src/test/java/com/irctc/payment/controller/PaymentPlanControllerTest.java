package com.irctc.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.payment.dto.EmiPaymentRequest;
import com.irctc.payment.dto.EmiPaymentResponse;
import com.irctc.payment.dto.PaymentPlanRequest;
import com.irctc.payment.dto.PaymentPlanResponse;
import com.irctc.payment.service.PaymentPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentPlanControllerTest {

    private MockMvc mockMvc;
    private PaymentPlanService paymentPlanService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentPlanService = Mockito.mock(PaymentPlanService.class);
        PaymentPlanController controller = new PaymentPlanController(paymentPlanService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldCreatePlan() throws Exception {
        PaymentPlanResponse response = new PaymentPlanResponse();
        response.setId(100L);
        response.setBookingId(1L);
        when(paymentPlanService.createPlan(any(PaymentPlanRequest.class))).thenReturn(response);

        PaymentPlanRequest request = new PaymentPlanRequest();
        request.setBookingId(1L);
        request.setUserId(2L);
        request.setTotalAmount(BigDecimal.valueOf(1200));
        request.setInstallments(12);

        mockMvc.perform(post("/api/payments/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void shouldGetUserPlans() throws Exception {
        PaymentPlanResponse response = new PaymentPlanResponse();
        response.setId(100L);
        response.setBookingId(1L);
        when(paymentPlanService.getPlansByUser(2L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payments/plans/user/{userId}", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void shouldPayInstallment() throws Exception {
        EmiPaymentResponse emiResponse = new EmiPaymentResponse();
        emiResponse.setId(5L);
        emiResponse.setAmountPaid(BigDecimal.valueOf(100));
        when(paymentPlanService.recordEmiPayment(eq(5L), eq(BigDecimal.valueOf(100))))
            .thenReturn(emiResponse);

        EmiPaymentRequest request = new EmiPaymentRequest();
        request.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/payments/plans/emi/{emiId}/pay", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.amountPaid").value(100));
    }
}

