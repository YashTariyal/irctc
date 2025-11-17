package com.irctc.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.payment.dto.OfflinePaymentRequest;
import com.irctc.payment.entity.OfflinePaymentIntent;
import com.irctc.payment.service.OfflinePaymentQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OfflinePaymentControllerTest {

    @Mock
    private OfflinePaymentQueueService queueService;

    @InjectMocks
    private OfflinePaymentController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
            .build();
    }

    @Test
    void shouldQueueOfflinePayment() throws Exception {
        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setId(1L);
        intent.setUserId(1L);
        intent.setBookingId(2L);
        intent.setAmount(BigDecimal.valueOf(100));
        intent.setCurrency("INR");
        intent.setPaymentMethod("CARD");
        intent.setStatus("QUEUED");

        when(queueService.queue(any(OfflinePaymentRequest.class))).thenReturn(intent);

        OfflinePaymentRequest request = new OfflinePaymentRequest();
        request.setUserId(1L);
        request.setBookingId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("INR");
        request.setPaymentMethod("CARD");

        mockMvc.perform(post("/api/payments/offline")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    void shouldListPendingForUser() throws Exception {
        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setId(2L);
        intent.setUserId(1L);
        intent.setBookingId(3L);
        intent.setAmount(BigDecimal.valueOf(200));
        intent.setCurrency("INR");
        intent.setPaymentMethod("CARD");
        intent.setStatus("QUEUED");

        when(queueService.getPendingForUser(1L)).thenReturn(List.of(intent));

        mockMvc.perform(get("/api/payments/offline/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(2L));
    }
}

