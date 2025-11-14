package com.irctc.payment.service;

import com.irctc.payment.entity.GatewayStatistics;
import com.irctc.payment.repository.GatewayStatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for tracking and managing payment gateway statistics
 */
@Service
public class GatewayStatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayStatisticsService.class);
    
    @Autowired
    private GatewayStatisticsRepository statisticsRepository;
    
    /**
     * Record a successful payment transaction
     */
    @Transactional
    public void recordSuccess(String gatewayName, BigDecimal amount, BigDecimal fee) {
        GatewayStatistics stats = getOrCreateStatistics(gatewayName);
        stats.setTotalTransactions(stats.getTotalTransactions() + 1);
        stats.setSuccessfulTransactions(stats.getSuccessfulTransactions() + 1);
        stats.setTotalAmount(stats.getTotalAmount().add(amount));
        stats.setTotalFees(stats.getTotalFees().add(fee));
        stats.setLastTransactionTime(LocalDateTime.now());
        statisticsRepository.save(stats);
        
        logger.debug("Recorded success for gateway {}: Amount {}, Fee {}", gatewayName, amount, fee);
    }
    
    /**
     * Record a failed payment transaction
     */
    @Transactional
    public void recordFailure(String gatewayName, BigDecimal amount) {
        GatewayStatistics stats = getOrCreateStatistics(gatewayName);
        stats.setTotalTransactions(stats.getTotalTransactions() + 1);
        stats.setFailedTransactions(stats.getFailedTransactions() + 1);
        stats.setLastTransactionTime(LocalDateTime.now());
        statisticsRepository.save(stats);
        
        logger.debug("Recorded failure for gateway {}: Amount {}", gatewayName, amount);
    }
    
    /**
     * Get statistics for a gateway
     */
    @Cacheable(value = "gateway-statistics", key = "#gatewayName")
    public Optional<GatewayStatistics> getStatistics(String gatewayName) {
        return statisticsRepository.findByGatewayName(gatewayName);
    }
    
    /**
     * Get all gateway statistics
     */
    @Cacheable(value = "gateway-statistics", key = "'all'")
    public List<GatewayStatistics> getAllStatistics() {
        return statisticsRepository.findAll();
    }
    
    /**
     * Calculate success rate for a gateway
     */
    public double calculateSuccessRate(String gatewayName) {
        Optional<GatewayStatistics> statsOpt = getStatistics(gatewayName);
        if (statsOpt.isEmpty()) {
            return 0.0;
        }
        
        GatewayStatistics stats = statsOpt.get();
        if (stats.getTotalTransactions() == 0) {
            return 0.0;
        }
        
        return (double) stats.getSuccessfulTransactions() / stats.getTotalTransactions() * 100.0;
    }
    
    /**
     * Calculate average fee for a gateway
     */
    public BigDecimal calculateAverageFee(String gatewayName) {
        Optional<GatewayStatistics> statsOpt = getStatistics(gatewayName);
        if (statsOpt.isEmpty() || statsOpt.get().getSuccessfulTransactions() == 0) {
            return BigDecimal.ZERO;
        }
        
        GatewayStatistics stats = statsOpt.get();
        return stats.getTotalFees()
            .divide(BigDecimal.valueOf(stats.getSuccessfulTransactions()), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get or create statistics for a gateway
     */
    private GatewayStatistics getOrCreateStatistics(String gatewayName) {
        return statisticsRepository.findByGatewayName(gatewayName)
            .orElseGet(() -> {
                GatewayStatistics newStats = new GatewayStatistics();
                newStats.setGatewayName(gatewayName);
                newStats.setTotalTransactions(0L);
                newStats.setSuccessfulTransactions(0L);
                newStats.setFailedTransactions(0L);
                newStats.setTotalAmount(BigDecimal.ZERO);
                newStats.setTotalFees(BigDecimal.ZERO);
                newStats.setCreatedAt(LocalDateTime.now());
                return statisticsRepository.save(newStats);
            });
    }
}

