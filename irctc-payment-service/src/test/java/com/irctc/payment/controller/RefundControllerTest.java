package com.irctc.payment.controller;

import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.entity.RefundStatus;
import com.irctc.payment.service.AutomatedRefundService;
import com.irctc.payment.service.RefundPolicyService;
import com.irctc.payment.service.RefundReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RefundController.class)
class RefundControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AutomatedRefundService automatedRefundService;
    
    @MockBean
    private RefundPolicyService refundPolicyService;
    
    @MockBean
    private RefundReconciliationService refundReconciliationService;
    
    private RefundStatus refundStatus;
    private RefundPolicy refundPolicy;
    
    @BeforeEach
    void setUp() {
        refundStatus = new RefundStatus();
        refundStatus.setId(1L);
        refundStatus.setPaymentId(1L);
        refundStatus.setBookingId(123L);
        refundStatus.setRefundAmount(BigDecimal.valueOf(1000));
        refundStatus.setStatus("INITIATED");
        refundStatus.setRefundId("REFUND123");
        
        refundPolicy = new RefundPolicy();
        refundPolicy.setId(1L);
        refundPolicy.setName("Full Refund - 48 hours before");
        refundPolicy.setHoursBeforeDeparture(48);
        refundPolicy.setRefundPercentage(BigDecimal.valueOf(100));
    }
    
    @Test
    void testInitiateRefund() throws Exception {
        when(automatedRefundService.initiateRefund(
            eq(1L), any(), anyString(), any(), any()))
            .thenReturn(refundStatus);
        
        mockMvc.perform(post("/api/payments/1/initiate-refund")
                .header("X-Tenant-Id", "test-tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refundAmount\":1000,\"reason\":\"Test refund\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId").value("REFUND123"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }
    
    @Test
    void testGetRefundStatus() throws Exception {
        when(automatedRefundService.getRefundStatusByPaymentId(1L))
            .thenReturn(Arrays.asList(refundStatus));
        
        mockMvc.perform(get("/api/payments/1/refund-status")
                .header("X-Tenant-Id", "test-tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].refundId").value("REFUND123"))
                .andExpect(jsonPath("$[0].status").value("INITIATED"));
    }
    
    @Test
    void testGetRefundStatusByRefundId() throws Exception {
        when(automatedRefundService.getRefundStatusByRefundId("REFUND123"))
            .thenReturn(Optional.of(refundStatus));
        
        mockMvc.perform(get("/api/payments/refund-status/REFUND123")
                .header("X-Tenant-Id", "test-tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId").value("REFUND123"))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }
    
    @Test
    void testGetRefundPolicy() throws Exception {
        LocalDateTime cancellationTime = LocalDateTime.now();
        LocalDateTime departureTime = cancellationTime.plusHours(50);
        
        when(refundPolicyService.getApplicablePolicy(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Optional.of(refundPolicy));
        
        mockMvc.perform(get("/api/payments/refund-policy")
                .header("X-Tenant-Id", "test-tenant-1")
                .param("cancellationTime", cancellationTime.toString())
                .param("departureTime", departureTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Full Refund - 48 hours before"));
    }
    
    @Test
    void testReconcileRefund() throws Exception {
        refundStatus.setReconciliationStatus("RECONCILED");
        when(refundReconciliationService.reconcileRefund(1L))
            .thenReturn(refundStatus);
        
        mockMvc.perform(post("/api/payments/1/reconcile-refund")
                .header("X-Tenant-Id", "test-tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconciliationStatus").value("RECONCILED"));
    }
    
    @Test
    void testInitiatePartialRefund() throws Exception {
        when(automatedRefundService.initiatePartialRefund(
            eq(1L), any(), anyString(), anyList()))
            .thenReturn(refundStatus);
        
        mockMvc.perform(post("/api/payments/1/partial-refund")
                .header("X-Tenant-Id", "test-tenant-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refundAmount\":500,\"reason\":\"Partial refund\",\"passengerIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId").value("REFUND123"));
    }
    
    @Test
    void testGetRefundPolicies() throws Exception {
        when(refundPolicyService.getActivePolicies())
            .thenReturn(Arrays.asList(refundPolicy));
        
        mockMvc.perform(get("/api/payments/refund-policies")
                .header("X-Tenant-Id", "test-tenant-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Full Refund - 48 hours before"));
    }
}

