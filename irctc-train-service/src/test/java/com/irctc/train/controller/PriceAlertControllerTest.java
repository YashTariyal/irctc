package com.irctc.train.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.train.dto.PriceAlertRequest;
import com.irctc.train.dto.PriceAlertResponse;
import com.irctc.train.dto.PriceAlertUpdateRequest;
import com.irctc.train.service.PriceAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PriceAlertControllerTest {

    private MockMvc mockMvc;
    private PriceAlertService priceAlertService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        priceAlertService = Mockito.mock(PriceAlertService.class);
        PriceAlertController controller = new PriceAlertController(priceAlertService);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldCreateAlert() throws Exception {
        PriceAlertRequest request = new PriceAlertRequest();
        request.setUserId(10L);
        request.setAlertType("PRICE_DROP");
        request.setTargetPrice(BigDecimal.valueOf(900));
        request.setTravelDate(LocalDate.now().plusDays(3));

        PriceAlertResponse response = new PriceAlertResponse();
        response.setId(1L);
        response.setUserId(10L);
        response.setAlertType("PRICE_DROP");

        when(priceAlertService.createAlert(any())).thenReturn(response);

        mockMvc.perform(post("/api/trains/price-alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldListAlerts() throws Exception {
        PriceAlertResponse response = new PriceAlertResponse();
        response.setId(1L);
        response.setUserId(10L);
        when(priceAlertService.getAlertsForUser(10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/trains/price-alerts/user/{userId}", 10L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].userId").value(10L));
    }

    @Test
    void shouldUpdateAlert() throws Exception {
        PriceAlertUpdateRequest request = new PriceAlertUpdateRequest();
        request.setTargetPrice(BigDecimal.valueOf(800));

        PriceAlertResponse response = new PriceAlertResponse();
        response.setId(1L);
        response.setTargetPrice(BigDecimal.valueOf(800));

        when(priceAlertService.updateAlert(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/trains/price-alerts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.targetPrice").value(800.0));
    }
}

