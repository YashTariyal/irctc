package com.irctc.payment.gateway.impl;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.dto.PaymentResponse;
import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RazorpayGatewayTest {
    
    @InjectMocks
    private RazorpayGateway razorpayGateway;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(razorpayGateway, "keyId", "test_key_id");
        ReflectionTestUtils.setField(razorpayGateway, "keySecret", "test_key_secret");
        ReflectionTestUtils.setField(razorpayGateway, "feePercentage", 2.0);
        ReflectionTestUtils.setField(razorpayGateway, "fixedFee", 0.0);
        ReflectionTestUtils.setField(razorpayGateway, "enabled", true);
    }
    
    @Test
    void testGetGatewayName() {
        assertEquals("RAZORPAY", razorpayGateway.getGatewayName());
    }
    
    @Test
    void testIsEnabled() {
        assertTrue(razorpayGateway.isEnabled());
    }
    
    @Test
    void testIsEnabled_WhenKeyIdIsEmpty() {
        ReflectionTestUtils.setField(razorpayGateway, "keyId", "");
        assertFalse(razorpayGateway.isEnabled());
    }
    
    @Test
    void testGetTransactionFeePercentage() {
        assertEquals(2.0, razorpayGateway.getTransactionFeePercentage());
    }
    
    @Test
    void testGetFixedFee() {
        assertEquals(0.0, razorpayGateway.getFixedFee());
    }
    
    @Test
    void testProcessPayment() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(123L);
        request.setAmount(BigDecimal.valueOf(1000.00));
        request.setCurrency("INR");
        request.setPaymentMethod("CARD");
        
        PaymentResponse response = razorpayGateway.processPayment(request);
        
        assertNotNull(response);
        assertEquals("RAZORPAY", response.getGatewayName());
        assertEquals(BigDecimal.valueOf(1000.00), response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertNotNull(response.getTransactionId());
        assertNotNull(response.getGatewayFee());
        assertTrue(response.getGatewayFee().compareTo(BigDecimal.ZERO) >= 0);
    }
    
    @Test
    void testProcessRefund() {
        RefundRequest request = new RefundRequest();
        request.setOriginalTransactionId("TXN123");
        request.setGatewayTransactionId("pay_123");
        request.setRefundAmount(BigDecimal.valueOf(500.00));
        request.setReason("Test refund");
        
        RefundResponse response = razorpayGateway.processRefund(request);
        
        assertNotNull(response);
        assertEquals("RAZORPAY", response.getGatewayName());
        assertEquals("TXN123", response.getOriginalTransactionId());
        assertEquals(BigDecimal.valueOf(500.00), response.getRefundAmount());
        assertNotNull(response.getRefundId());
    }
    
    @Test
    void testVerifyPayment() {
        String transactionId = "TXN123";
        PaymentResponse response = razorpayGateway.verifyPayment(transactionId);
        
        assertNotNull(response);
        assertEquals("RAZORPAY", response.getGatewayName());
        assertEquals(transactionId, response.getTransactionId());
    }
    
    @Test
    void testSupportsCurrency() {
        assertTrue(razorpayGateway.supportsCurrency("INR"));
        assertTrue(razorpayGateway.supportsCurrency("USD"));
        assertTrue(razorpayGateway.supportsCurrency("EUR"));
        assertFalse(razorpayGateway.supportsCurrency("GBP"));
    }
    
    @Test
    void testSupportsPaymentMethod() {
        assertTrue(razorpayGateway.supportsPaymentMethod("CARD"));
        assertTrue(razorpayGateway.supportsPaymentMethod("UPI"));
        assertTrue(razorpayGateway.supportsPaymentMethod("NETBANKING"));
        assertTrue(razorpayGateway.supportsPaymentMethod("WALLET"));
        assertFalse(razorpayGateway.supportsPaymentMethod("CASH"));
    }
}

