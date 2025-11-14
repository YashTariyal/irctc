package com.irctc.payment.service;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.dto.PaymentResponse;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.repository.SimplePaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimplePaymentServiceGatewayTest {
    
    @Mock
    private SimplePaymentRepository paymentRepository;
    
    @Mock
    private GatewaySelectorService gatewaySelectorService;
    
    @Mock
    private GatewayStatisticsService statisticsService;
    
    @Mock
    private PaymentGateway paymentGateway;
    
    @InjectMocks
    private SimplePaymentService paymentService;
    
    private SimplePayment payment;
    private PaymentResponse gatewayResponse;
    
    @BeforeEach
    void setUp() {
        payment = new SimplePayment();
        payment.setId(1L);
        payment.setBookingId(123L);
        payment.setAmount(1000.0);
        payment.setCurrency("INR");
        payment.setPaymentMethod("CARD");
        
        gatewayResponse = new PaymentResponse();
        gatewayResponse.setTransactionId("TXN123");
        gatewayResponse.setGatewayName("RAZORPAY");
        gatewayResponse.setGatewayTransactionId("pay_123");
        gatewayResponse.setAmount(BigDecimal.valueOf(1000.0));
        gatewayResponse.setGatewayFee(BigDecimal.valueOf(20.0));
        gatewayResponse.setStatus("SUCCESS");
    }
    
    @Test
    void testProcessPaymentWithGateway_Success() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(123L);
        paymentRequest.setAmount(BigDecimal.valueOf(1000.0));
        paymentRequest.setCurrency("INR");
        paymentRequest.setPaymentMethod("CARD");
        
        when(gatewaySelectorService.selectGateway(any(PaymentRequest.class)))
            .thenReturn(paymentGateway);
        when(paymentGateway.processPayment(any(PaymentRequest.class)))
            .thenReturn(gatewayResponse);
        when(paymentRepository.save(any(SimplePayment.class)))
            .thenReturn(payment);
        
        SimplePayment result = paymentService.processPaymentWithGateway(payment, null);
        
        assertNotNull(result);
        verify(statisticsService, times(1)).recordSuccess(
            eq("RAZORPAY"), 
            any(BigDecimal.class), 
            any(BigDecimal.class)
        );
    }
    
    @Test
    void testProcessPaymentWithGateway_Fallback() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(123L);
        paymentRequest.setAmount(BigDecimal.valueOf(1000.0));
        paymentRequest.setCurrency("INR");
        paymentRequest.setPaymentMethod("CARD");
        
        PaymentResponse failedResponse = new PaymentResponse();
        failedResponse.setStatus("FAILED");
        failedResponse.setGatewayName("RAZORPAY");
        failedResponse.setAmount(BigDecimal.valueOf(1000.0));
        
        PaymentGateway fallbackGateway = mock(PaymentGateway.class);
        PaymentResponse fallbackResponse = new PaymentResponse();
        fallbackResponse.setStatus("SUCCESS");
        fallbackResponse.setGatewayName("STRIPE");
        fallbackResponse.setAmount(BigDecimal.valueOf(1000.0));
        fallbackResponse.setGatewayFee(BigDecimal.valueOf(29.0));
        fallbackResponse.setTransactionId("TXN456");
        
        when(gatewaySelectorService.selectGateway(any(PaymentRequest.class)))
            .thenReturn(paymentGateway);
        when(paymentGateway.getGatewayName()).thenReturn("RAZORPAY");
        when(paymentGateway.processPayment(any(PaymentRequest.class)))
            .thenReturn(failedResponse);
        when(gatewaySelectorService.getAvailableGateways())
            .thenReturn(java.util.Arrays.asList(paymentGateway, fallbackGateway));
        when(fallbackGateway.getGatewayName()).thenReturn("STRIPE");
        when(fallbackGateway.supportsCurrency("INR")).thenReturn(true);
        when(fallbackGateway.supportsPaymentMethod("CARD")).thenReturn(true);
        when(fallbackGateway.processPayment(any(PaymentRequest.class)))
            .thenReturn(fallbackResponse);
        when(paymentRepository.save(any(SimplePayment.class)))
            .thenReturn(payment);
        
        SimplePayment result = paymentService.processPaymentWithGateway(payment, null);
        
        assertNotNull(result);
        verify(statisticsService, times(1)).recordFailure(
            eq("RAZORPAY"), 
            any(BigDecimal.class)
        );
        verify(statisticsService, times(1)).recordSuccess(
            eq("STRIPE"), 
            any(BigDecimal.class), 
            any(BigDecimal.class)
        );
    }
    
    @Test
    void testProcessPaymentWithGateway_NoGatewayService() {
        // Use reflection to set gatewaySelectorService to null
        org.springframework.test.util.ReflectionTestUtils.setField(
            paymentService, "gatewaySelectorService", null);
        
        when(paymentRepository.save(any(SimplePayment.class)))
            .thenReturn(payment);
        
        SimplePayment result = paymentService.processPaymentWithGateway(payment, null);
        
        assertNotNull(result);
        assertEquals("INTERNAL", result.getGatewayName());
    }
}

