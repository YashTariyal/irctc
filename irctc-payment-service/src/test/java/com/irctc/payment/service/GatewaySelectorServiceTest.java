package com.irctc.payment.service;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.entity.GatewayStatistics;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.gateway.impl.PayUGateway;
import com.irctc.payment.gateway.impl.RazorpayGateway;
import com.irctc.payment.gateway.impl.StripeGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewaySelectorServiceTest {
    
    @Mock
    private GatewayStatisticsService statisticsService;
    
    @InjectMocks
    private GatewaySelectorService gatewaySelectorService;
    
    private RazorpayGateway razorpayGateway;
    private StripeGateway stripeGateway;
    private PayUGateway payUGateway;
    
    @BeforeEach
    void setUp() {
        razorpayGateway = new RazorpayGateway();
        stripeGateway = new StripeGateway();
        payUGateway = new PayUGateway();
        
        ReflectionTestUtils.setField(razorpayGateway, "keyId", "test_key");
        ReflectionTestUtils.setField(razorpayGateway, "enabled", true);
        
        ReflectionTestUtils.setField(stripeGateway, "secretKey", "test_key");
        ReflectionTestUtils.setField(stripeGateway, "enabled", true);
        
        ReflectionTestUtils.setField(payUGateway, "merchantKey", "test_key");
        ReflectionTestUtils.setField(payUGateway, "enabled", true);
        
        List<PaymentGateway> gateways = Arrays.asList(razorpayGateway, stripeGateway, payUGateway);
        ReflectionTestUtils.setField(gatewaySelectorService, "paymentGateways", gateways);
    }
    
    @Test
    void testSelectGateway_WithPreferredGateway() {
        PaymentRequest request = new PaymentRequest();
        request.setCurrency("INR");
        request.setPaymentMethod("CARD");
        request.setGatewayPreference("RAZORPAY");
        request.setAmount(BigDecimal.valueOf(1000));
        
        PaymentGateway selected = gatewaySelectorService.selectGateway(request);
        
        assertEquals("RAZORPAY", selected.getGatewayName());
    }
    
    @Test
    void testSelectGateway_AutoSelect() {
        PaymentRequest request = new PaymentRequest();
        request.setCurrency("INR");
        request.setPaymentMethod("CARD");
        request.setAmount(BigDecimal.valueOf(1000));
        
        // Mock statistics
        GatewayStatistics razorpayStats = new GatewayStatistics();
        razorpayStats.setTotalTransactions(100L);
        razorpayStats.setSuccessfulTransactions(90L);
        razorpayStats.setTotalFees(BigDecimal.valueOf(200));
        
        GatewayStatistics stripeStats = new GatewayStatistics();
        stripeStats.setTotalTransactions(100L);
        stripeStats.setSuccessfulTransactions(85L);
        stripeStats.setTotalFees(BigDecimal.valueOf(300));
        
        when(statisticsService.getStatistics("RAZORPAY"))
            .thenReturn(Optional.of(razorpayStats));
        when(statisticsService.getStatistics("STRIPE"))
            .thenReturn(Optional.of(stripeStats));
        when(statisticsService.calculateSuccessRate("RAZORPAY")).thenReturn(90.0);
        when(statisticsService.calculateSuccessRate("STRIPE")).thenReturn(85.0);
        when(statisticsService.calculateAverageFee("RAZORPAY")).thenReturn(BigDecimal.valueOf(2.0));
        when(statisticsService.calculateAverageFee("STRIPE")).thenReturn(BigDecimal.valueOf(3.0));
        
        PaymentGateway selected = gatewaySelectorService.selectGateway(request);
        
        assertNotNull(selected);
        // Should select Razorpay due to higher success rate
        assertEquals("RAZORPAY", selected.getGatewayName());
    }
    
    @Test
    void testSelectGateway_NoAvailableGateway() {
        PaymentRequest request = new PaymentRequest();
        request.setCurrency("JPY"); // Unsupported currency
        request.setPaymentMethod("CARD");
        request.setAmount(BigDecimal.valueOf(1000));
        
        assertThrows(RuntimeException.class, () -> {
            gatewaySelectorService.selectGateway(request);
        });
    }
    
    @Test
    void testGetAvailableGateways() {
        List<PaymentGateway> available = gatewaySelectorService.getAvailableGateways();
        
        assertNotNull(available);
        assertFalse(available.isEmpty());
    }
    
    @Test
    void testGetGatewayByName() {
        PaymentGateway gateway = gatewaySelectorService.getGatewayByName("RAZORPAY");
        
        assertNotNull(gateway);
        assertEquals("RAZORPAY", gateway.getGatewayName());
    }
    
    @Test
    void testGetGatewayByName_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            gatewaySelectorService.getGatewayByName("INVALID");
        });
    }
}

