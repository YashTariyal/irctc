package com.irctc.payment.controller;

import com.irctc.payment.entity.GatewayStatistics;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.service.GatewaySelectorService;
import com.irctc.payment.service.GatewayStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GatewayController.class)
class GatewayControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GatewaySelectorService gatewaySelectorService;
    
    @MockBean
    private GatewayStatisticsService statisticsService;
    
    private PaymentGateway mockGateway;
    private GatewayStatistics mockStats;
    
    @BeforeEach
    void setUp() {
        mockGateway = mock(PaymentGateway.class);
        when(mockGateway.getGatewayName()).thenReturn("RAZORPAY");
        when(mockGateway.isEnabled()).thenReturn(true);
        when(mockGateway.getTransactionFeePercentage()).thenReturn(2.0);
        when(mockGateway.getFixedFee()).thenReturn(0.0);
        
        mockStats = new GatewayStatistics();
        mockStats.setGatewayName("RAZORPAY");
        mockStats.setTotalTransactions(100L);
        mockStats.setSuccessfulTransactions(90L);
        mockStats.setFailedTransactions(10L);
        mockStats.setTotalAmount(BigDecimal.valueOf(100000));
        mockStats.setTotalFees(BigDecimal.valueOf(2000));
    }
    
    @Test
    void testGetAvailableGateways() throws Exception {
        when(gatewaySelectorService.getAvailableGateways())
            .thenReturn(Arrays.asList(mockGateway));
        when(statisticsService.getStatistics("RAZORPAY"))
            .thenReturn(Optional.of(mockStats));
        when(statisticsService.calculateSuccessRate("RAZORPAY"))
            .thenReturn(90.0);
        when(statisticsService.calculateAverageFee("RAZORPAY"))
            .thenReturn(BigDecimal.valueOf(20.0));
        
        mockMvc.perform(get("/api/payments/gateways"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("RAZORPAY"))
            .andExpect(jsonPath("$[0].enabled").value(true));
    }
    
    @Test
    void testGetGatewayStatistics() throws Exception {
        when(statisticsService.getAllStatistics())
            .thenReturn(Arrays.asList(mockStats));
        
        mockMvc.perform(get("/api/payments/gateways/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].gatewayName").value("RAZORPAY"));
    }
    
    @Test
    void testGetGatewayStatisticsByName() throws Exception {
        when(statisticsService.getStatistics("RAZORPAY"))
            .thenReturn(Optional.of(mockStats));
        when(statisticsService.calculateSuccessRate("RAZORPAY"))
            .thenReturn(90.0);
        when(statisticsService.calculateAverageFee("RAZORPAY"))
            .thenReturn(BigDecimal.valueOf(20.0));
        
        mockMvc.perform(get("/api/payments/gateways/stats/RAZORPAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gatewayName").value("RAZORPAY"))
            .andExpect(jsonPath("$.successRate").value(90.0));
    }
    
    @Test
    void testCompareGateways() throws Exception {
        when(gatewaySelectorService.getAvailableGateways())
            .thenReturn(Arrays.asList(mockGateway));
        when(mockGateway.supportsCurrency("INR")).thenReturn(true);
        when(mockGateway.supportsPaymentMethod("CARD")).thenReturn(true);
        when(statisticsService.getStatistics("RAZORPAY"))
            .thenReturn(Optional.of(mockStats));
        when(statisticsService.calculateSuccessRate("RAZORPAY"))
            .thenReturn(90.0);
        when(statisticsService.calculateAverageFee("RAZORPAY"))
            .thenReturn(BigDecimal.valueOf(20.0));
        
        mockMvc.perform(get("/api/payments/gateways/compare")
                .param("currency", "INR")
                .param("paymentMethod", "CARD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].gatewayName").value("RAZORPAY"));
    }
}

