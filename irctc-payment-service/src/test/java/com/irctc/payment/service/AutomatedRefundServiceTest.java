package com.irctc.payment.service;

import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;
import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.entity.RefundStatus;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.repository.RefundStatusRepository;
import com.irctc.payment.repository.SimplePaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomatedRefundServiceTest {
    
    @Mock
    private SimplePaymentRepository paymentRepository;
    
    @Mock
    private RefundStatusRepository refundStatusRepository;
    
    @Mock
    private RefundPolicyService refundPolicyService;
    
    @Mock
    private GatewaySelectorService gatewaySelectorService;
    
    @Mock
    private PaymentGateway paymentGateway;
    
    @InjectMocks
    private AutomatedRefundService automatedRefundService;
    
    private SimplePayment payment;
    private RefundPolicy refundPolicy;
    
    @BeforeEach
    void setUp() {
        payment = new SimplePayment();
        payment.setId(1L);
        payment.setBookingId(123L);
        payment.setAmount(1000.0);
        payment.setCurrency("INR");
        payment.setPaymentMethod("CARD");
        payment.setTransactionId("TXN123");
        payment.setGatewayName("RAZORPAY");
        payment.setGatewayTransactionId("pay_123");
        payment.setGatewayFee(20.0);
        payment.setStatus("SUCCESS");
        
        refundPolicy = new RefundPolicy();
        refundPolicy.setName("Full Refund - 48 hours before");
        refundPolicy.setRefundPercentage(BigDecimal.valueOf(100));
        refundPolicy.setFixedCharges(BigDecimal.ZERO);
        refundPolicy.setGatewayFeeRefundable(true);
    }
    
    @Test
    void testAutoRefundOnCancellation_Success() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(50);
        
        when(paymentRepository.findByBookingId(123L))
            .thenReturn(Arrays.asList(payment));
        when(refundPolicyService.getApplicablePolicy(cancellationTime, departureTime))
            .thenReturn(Optional.of(refundPolicy));
        when(refundPolicyService.calculateRefundAmount(
            any(RefundPolicy.class), any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(BigDecimal.valueOf(1020));
        when(gatewaySelectorService.getGatewayByName("RAZORPAY"))
            .thenReturn(paymentGateway);
        
        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setRefundId(UUID.randomUUID().toString());
        refundResponse.setGatewayRefundId("rfnd_123");
        refundResponse.setStatus("SUCCESS");
        refundResponse.setRefundAmount(BigDecimal.valueOf(1020));
        
        when(paymentGateway.processRefund(any(RefundRequest.class)))
            .thenReturn(refundResponse);
        when(refundStatusRepository.save(any(RefundStatus.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        RefundStatus result = automatedRefundService.autoRefundOnCancellation(
            123L, cancellationTime, departureTime, "Test cancellation"
        );
        
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getRefundId());
        verify(refundStatusRepository, atLeastOnce()).save(any(RefundStatus.class));
    }
    
    @Test
    void testAutoRefundOnCancellation_NoPayment() {
        when(paymentRepository.findByBookingId(123L))
            .thenReturn(Arrays.asList());
        
        RefundStatus result = automatedRefundService.autoRefundOnCancellation(
            123L, LocalDateTime.now(), LocalDateTime.now().plusHours(50), "Test"
        );
        
        assertNull(result);
    }
    
    @Test
    void testAutoRefundOnCancellation_NoRefundPolicy() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(5); // Too close
        
        when(paymentRepository.findByBookingId(123L))
            .thenReturn(Arrays.asList(payment));
        when(refundPolicyService.getApplicablePolicy(cancellationTime, departureTime))
            .thenReturn(Optional.empty());
        when(refundPolicyService.calculateRefundAmount(
            eq(null), any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(BigDecimal.ZERO);
        when(refundStatusRepository.save(any(RefundStatus.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        RefundStatus result = automatedRefundService.autoRefundOnCancellation(
            123L, cancellationTime, departureTime, "Test"
        );
        
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        verify(refundPolicyService, times(1)).getApplicablePolicy(cancellationTime, departureTime);
    }
    
    @Test
    void testInitiateRefund_WithPolicy() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(50);
        BigDecimal refundAmount = BigDecimal.valueOf(1000); // Capped at payment amount
        
        when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));
        when(refundPolicyService.getApplicablePolicy(cancellationTime, departureTime))
            .thenReturn(Optional.of(refundPolicy));
        when(refundPolicyService.calculateRefundAmount(
            any(RefundPolicy.class), any(BigDecimal.class), any(BigDecimal.class)))
            .thenReturn(refundAmount);
        when(refundStatusRepository.save(any(RefundStatus.class)))
            .thenAnswer(invocation -> {
                RefundStatus status = invocation.getArgument(0);
                status.setId(1L);
                return status;
            });
        
        RefundStatus result = automatedRefundService.initiateRefund(
            1L, null, "Test refund", cancellationTime, departureTime
        );
        
        assertNotNull(result);
        assertEquals("INITIATED", result.getStatus());
        verify(refundStatusRepository, atLeastOnce()).save(any(RefundStatus.class));
    }
    
    @Test
    void testInitiateRefund_WithExplicitAmount() {
        BigDecimal refundAmount = BigDecimal.valueOf(500);
        
        when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));
        when(refundStatusRepository.save(any(RefundStatus.class)))
            .thenAnswer(invocation -> {
                RefundStatus status = invocation.getArgument(0);
                status.setId(1L);
                return status;
            });
        
        RefundStatus result = automatedRefundService.initiateRefund(
            1L, refundAmount, "Test refund", null, null
        );
        
        assertNotNull(result);
        assertEquals("INITIATED", result.getStatus());
        // Note: Status will be updated to COMPLETED/FAILED asynchronously
    }
    
    @Test
    void testInitiateRefund_AmountExceedsPayment() {
        BigDecimal refundAmount = BigDecimal.valueOf(2000); // More than payment amount
        
        when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));
        
        assertThrows(RuntimeException.class, () -> {
            automatedRefundService.initiateRefund(
                1L, refundAmount, "Test refund", null, null
            );
        });
    }
    
    @Test
    void testInitiatePartialRefund() {
        BigDecimal refundAmount = BigDecimal.valueOf(500);
        List<Long> passengerIds = Arrays.asList(1L, 2L);
        
        when(paymentRepository.findById(1L))
            .thenReturn(Optional.of(payment));
        when(refundStatusRepository.save(any(RefundStatus.class)))
            .thenAnswer(invocation -> {
                RefundStatus status = invocation.getArgument(0);
                status.setId(1L);
                return status;
            });
        
        RefundStatus result = automatedRefundService.initiatePartialRefund(
            1L, refundAmount, "Partial refund", passengerIds
        );
        
        assertNotNull(result);
        assertEquals("INITIATED", result.getStatus());
        // Note: Status will be updated asynchronously
        verify(refundStatusRepository, atLeastOnce()).save(any(RefundStatus.class));
    }
    
    @Test
    void testGetRefundStatusByPaymentId() {
        RefundStatus refundStatus = new RefundStatus();
        refundStatus.setId(1L);
        refundStatus.setPaymentId(1L);
        refundStatus.setStatus("COMPLETED");
        
        when(refundStatusRepository.findByPaymentId(1L))
            .thenReturn(Arrays.asList(refundStatus));
        
        List<RefundStatus> result = automatedRefundService.getRefundStatusByPaymentId(1L);
        
        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }
}

