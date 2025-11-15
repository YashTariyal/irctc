package com.irctc.payment.service;

import com.irctc.payment.entity.GatewayStatistics;
import com.irctc.payment.repository.GatewayStatisticsRepository;
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
class GatewayStatisticsServiceTest {
    
    @Mock
    private GatewayStatisticsRepository repository;
    
    @InjectMocks
    private GatewayStatisticsService statisticsService;
    
    private GatewayStatistics existingStats;
    
    @BeforeEach
    void setUp() {
        existingStats = new GatewayStatistics();
        existingStats.setId(1L);
        existingStats.setGatewayName("RAZORPAY");
        existingStats.setTotalTransactions(100L);
        existingStats.setSuccessfulTransactions(90L);
        existingStats.setFailedTransactions(10L);
        existingStats.setTotalAmount(BigDecimal.valueOf(100000));
        existingStats.setTotalFees(BigDecimal.valueOf(2000));
    }
    
    @Test
    void testRecordSuccess() {
        when(repository.findByGatewayName("RAZORPAY"))
            .thenReturn(Optional.of(existingStats));
        when(repository.save(any(GatewayStatistics.class)))
            .thenReturn(existingStats);
        
        statisticsService.recordSuccess("RAZORPAY", 
            BigDecimal.valueOf(1000), 
            BigDecimal.valueOf(20));
        
        verify(repository, times(1)).save(any(GatewayStatistics.class));
    }
    
    @Test
    void testRecordSuccess_CreateNew() {
        when(repository.findByGatewayName("NEW_GATEWAY"))
            .thenReturn(Optional.empty());
        when(repository.save(any(GatewayStatistics.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        statisticsService.recordSuccess("NEW_GATEWAY", 
            BigDecimal.valueOf(1000), 
            BigDecimal.valueOf(20));
        
        // When creating new statistics, save is called twice:
        // 1. In getOrCreateStatistics() to create the new record
        // 2. In recordSuccess() to update the record with transaction data
        verify(repository, times(2)).save(any(GatewayStatistics.class));
    }
    
    @Test
    void testRecordFailure() {
        when(repository.findByGatewayName("RAZORPAY"))
            .thenReturn(Optional.of(existingStats));
        when(repository.save(any(GatewayStatistics.class)))
            .thenReturn(existingStats);
        
        statisticsService.recordFailure("RAZORPAY", BigDecimal.valueOf(1000));
        
        verify(repository, times(1)).save(any(GatewayStatistics.class));
    }
    
    @Test
    void testGetStatistics() {
        when(repository.findByGatewayName("RAZORPAY"))
            .thenReturn(Optional.of(existingStats));
        
        Optional<GatewayStatistics> result = statisticsService.getStatistics("RAZORPAY");
        
        assertTrue(result.isPresent());
        assertEquals("RAZORPAY", result.get().getGatewayName());
    }
    
    @Test
    void testCalculateSuccessRate() {
        when(repository.findByGatewayName("RAZORPAY"))
            .thenReturn(Optional.of(existingStats));
        
        double successRate = statisticsService.calculateSuccessRate("RAZORPAY");
        
        assertEquals(90.0, successRate);
    }
    
    @Test
    void testCalculateSuccessRate_NoTransactions() {
        GatewayStatistics emptyStats = new GatewayStatistics();
        emptyStats.setTotalTransactions(0L);
        emptyStats.setSuccessfulTransactions(0L);
        
        when(repository.findByGatewayName("EMPTY"))
            .thenReturn(Optional.of(emptyStats));
        
        double successRate = statisticsService.calculateSuccessRate("EMPTY");
        
        assertEquals(0.0, successRate);
    }
    
    @Test
    void testCalculateAverageFee() {
        when(repository.findByGatewayName("RAZORPAY"))
            .thenReturn(Optional.of(existingStats));
        
        BigDecimal averageFee = statisticsService.calculateAverageFee("RAZORPAY");
        
        assertNotNull(averageFee);
        assertTrue(averageFee.compareTo(BigDecimal.ZERO) > 0);
    }
}

