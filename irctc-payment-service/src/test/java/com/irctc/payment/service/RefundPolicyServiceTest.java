package com.irctc.payment.service;

import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.repository.RefundPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundPolicyServiceTest {
    
    @Mock
    private RefundPolicyRepository refundPolicyRepository;
    
    @InjectMocks
    private RefundPolicyService refundPolicyService;
    
    private RefundPolicy fullRefundPolicy;
    private RefundPolicy partialRefundPolicy;
    
    @BeforeEach
    void setUp() {
        // Full refund policy - 48 hours before
        fullRefundPolicy = new RefundPolicy();
        fullRefundPolicy.setId(1L);
        fullRefundPolicy.setName("Full Refund - 48 hours before");
        fullRefundPolicy.setHoursBeforeDeparture(48);
        fullRefundPolicy.setRefundPercentage(BigDecimal.valueOf(100));
        fullRefundPolicy.setFixedCharges(BigDecimal.ZERO);
        fullRefundPolicy.setGatewayFeeRefundable(true);
        fullRefundPolicy.setActive(true);
        fullRefundPolicy.setPriority(1);
        
        // Partial refund policy - 24 hours before
        partialRefundPolicy = new RefundPolicy();
        partialRefundPolicy.setId(2L);
        partialRefundPolicy.setName("Partial Refund - 24 hours before");
        partialRefundPolicy.setHoursBeforeDeparture(24);
        partialRefundPolicy.setRefundPercentage(BigDecimal.valueOf(75));
        partialRefundPolicy.setFixedCharges(BigDecimal.valueOf(50));
        partialRefundPolicy.setGatewayFeeRefundable(false);
        partialRefundPolicy.setActive(true);
        partialRefundPolicy.setPriority(2);
    }
    
    @Test
    void testGetApplicablePolicy_FullRefund() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(50); // 50 hours before
        
        when(refundPolicyRepository.findActivePoliciesOrderedByPriority())
            .thenReturn(Arrays.asList(fullRefundPolicy, partialRefundPolicy));
        
        Optional<RefundPolicy> policy = refundPolicyService.getApplicablePolicy(
            cancellationTime, departureTime
        );
        
        assertTrue(policy.isPresent());
        assertEquals("Full Refund - 48 hours before", policy.get().getName());
    }
    
    @Test
    void testGetApplicablePolicy_PartialRefund() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(30); // 30 hours before
        
        when(refundPolicyRepository.findActivePoliciesOrderedByPriority())
            .thenReturn(Arrays.asList(fullRefundPolicy, partialRefundPolicy));
        
        Optional<RefundPolicy> policy = refundPolicyService.getApplicablePolicy(
            cancellationTime, departureTime
        );
        
        assertTrue(policy.isPresent());
        assertEquals("Partial Refund - 24 hours before", policy.get().getName());
    }
    
    @Test
    void testGetApplicablePolicy_NoPolicy() {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(5); // Only 5 hours before
        
        when(refundPolicyRepository.findActivePoliciesOrderedByPriority())
            .thenReturn(Arrays.asList(fullRefundPolicy, partialRefundPolicy));
        
        Optional<RefundPolicy> policy = refundPolicyService.getApplicablePolicy(
            cancellationTime, departureTime
        );
        
        assertFalse(policy.isPresent());
    }
    
    @Test
    void testCalculateRefundAmount_FullRefund() {
        BigDecimal originalAmount = BigDecimal.valueOf(1000);
        BigDecimal gatewayFee = BigDecimal.valueOf(20);
        
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(
            fullRefundPolicy, originalAmount, gatewayFee
        );
        
        // 100% of 1000 = 1000, + gateway fee (20) = 1020, but capped at original amount (1000)
        // So result should be 1000 (capped)
        assertEquals(0, refundAmount.compareTo(BigDecimal.valueOf(1000)));
    }
    
    @Test
    void testCalculateRefundAmount_PartialRefund() {
        BigDecimal originalAmount = BigDecimal.valueOf(1000);
        BigDecimal gatewayFee = BigDecimal.valueOf(20);
        
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(
            partialRefundPolicy, originalAmount, gatewayFee
        );
        
        // 75% of 1000 = 750, minus fixed charges (50) = 700
        // Gateway fee not refundable, so total = 700
        assertEquals(0, refundAmount.compareTo(BigDecimal.valueOf(700)));
    }
    
    @Test
    void testCalculateRefundAmount_NoPolicy() {
        BigDecimal originalAmount = BigDecimal.valueOf(1000);
        BigDecimal gatewayFee = BigDecimal.valueOf(20);
        
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(
            null, originalAmount, gatewayFee
        );
        
        assertEquals(0, refundAmount.compareTo(BigDecimal.ZERO));
    }
    
    @Test
    void testCreatePolicy() {
        RefundPolicy newPolicy = new RefundPolicy();
        newPolicy.setName("Test Policy");
        newPolicy.setHoursBeforeDeparture(12);
        newPolicy.setRefundPercentage(BigDecimal.valueOf(50));
        
        when(refundPolicyRepository.save(any(RefundPolicy.class))).thenReturn(newPolicy);
        
        RefundPolicy created = refundPolicyService.createPolicy(newPolicy);
        
        assertNotNull(created);
        verify(refundPolicyRepository, times(1)).save(any(RefundPolicy.class));
    }
    
    @Test
    void testGetActivePolicies() {
        when(refundPolicyRepository.findActivePoliciesOrderedByPriority())
            .thenReturn(Arrays.asList(fullRefundPolicy, partialRefundPolicy));
        
        List<RefundPolicy> policies = refundPolicyService.getActivePolicies();
        
        assertEquals(2, policies.size());
        assertEquals(1, policies.get(0).getPriority()); // Higher priority first
    }
}

