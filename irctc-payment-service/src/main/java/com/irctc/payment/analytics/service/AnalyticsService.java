package com.irctc.payment.analytics.service;

import com.irctc.payment.analytics.dto.AnalyticsResponse;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.SimplePaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for payment analytics
 */
@Service
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private SimplePaymentRepository paymentRepository;
    
    @Cacheable(value = "analytics-overview", key = "#startDate.toString() + '-' + #endDate.toString()")
    public AnalyticsResponse.Overview getOverview(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<SimplePayment> payments = paymentRepository.findByPaymentTimeBetween(start, end);
        
        AnalyticsResponse.Overview overview = new AnalyticsResponse.Overview();
        overview.setTotalTransactions((long) payments.size());
        
        long successful = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
            .count();
        overview.setSuccessfulTransactions(successful);
        
        long failed = payments.stream()
            .filter(p -> "FAILED".equals(p.getStatus()))
            .count();
        overview.setFailedTransactions(failed);
        
        BigDecimal totalAmount = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
            .map(p -> BigDecimal.valueOf(p.getAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.setTotalAmount(totalAmount);
        
        BigDecimal totalFees = payments.stream()
            .filter(p -> p.getGatewayFee() != null)
            .map(p -> BigDecimal.valueOf(p.getGatewayFee()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.setTotalFees(totalFees);
        
        if (successful > 0) {
            overview.setAverageTransactionAmount(totalAmount.divide(
                BigDecimal.valueOf(successful), 2, RoundingMode.HALF_UP));
        } else {
            overview.setAverageTransactionAmount(BigDecimal.ZERO);
        }
        
        if (payments.size() > 0) {
            overview.setSuccessRate((double) successful / payments.size() * 100.0);
        } else {
            overview.setSuccessRate(0.0);
        }
        
        long refunds = payments.stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()))
            .count();
        overview.setRefundsCount(refunds);
        
        BigDecimal refundsAmount = payments.stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()))
            .map(p -> BigDecimal.valueOf(p.getAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.setRefundsAmount(refundsAmount);
        
        return overview;
    }
    
    public List<AnalyticsResponse.DailyStats> getDailyStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<SimplePayment> payments = paymentRepository.findByPaymentTimeBetween(start, end);
        
        Map<LocalDate, List<SimplePayment>> paymentsByDate = payments.stream()
            .collect(Collectors.groupingBy(p -> p.getPaymentTime().toLocalDate()));
        
        List<AnalyticsResponse.DailyStats> stats = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<SimplePayment> dayPayments = paymentsByDate.getOrDefault(current, List.of());
            
            AnalyticsResponse.DailyStats dailyStats = new AnalyticsResponse.DailyStats();
            dailyStats.setDate(current);
            dailyStats.setTransactions((long) dayPayments.size());
            
            long successful = dayPayments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                .count();
            dailyStats.setSuccessfulTransactions(successful);
            
            long failed = dayPayments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
            dailyStats.setFailedTransactions(failed);
            
            BigDecimal totalAmount = dayPayments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                .map(p -> BigDecimal.valueOf(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyStats.setTotalAmount(totalAmount);
            
            BigDecimal totalFees = dayPayments.stream()
                .filter(p -> p.getGatewayFee() != null)
                .map(p -> BigDecimal.valueOf(p.getGatewayFee()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            dailyStats.setTotalFees(totalFees);
            
            if (dayPayments.size() > 0) {
                dailyStats.setSuccessRate((double) successful / dayPayments.size() * 100.0);
            } else {
                dailyStats.setSuccessRate(0.0);
            }
            
            stats.add(dailyStats);
            current = current.plusDays(1);
        }
        
        return stats;
    }
    
    public List<AnalyticsResponse.WeeklyStats> getWeeklyStats(LocalDate startDate, LocalDate endDate) {
        // Implementation for weekly stats
        // Group by week and calculate statistics
        return List.of(); // Placeholder
    }
    
    public List<AnalyticsResponse.MonthlyStats> getMonthlyStats(LocalDate startDate, LocalDate endDate) {
        // Implementation for monthly stats
        // Group by month and calculate statistics
        return List.of(); // Placeholder
    }
    
    public List<AnalyticsResponse.GatewayPerformance> getGatewayPerformance(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<SimplePayment> payments = paymentRepository.findByPaymentTimeBetween(start, end);
        
        Map<String, List<SimplePayment>> paymentsByGateway = payments.stream()
            .filter(p -> p.getGatewayName() != null)
            .collect(Collectors.groupingBy(SimplePayment::getGatewayName));
        
        return paymentsByGateway.entrySet().stream()
            .map(entry -> {
                List<SimplePayment> gatewayPayments = entry.getValue();
                
                AnalyticsResponse.GatewayPerformance performance = new AnalyticsResponse.GatewayPerformance();
                performance.setGatewayName(entry.getKey());
                performance.setTransactions((long) gatewayPayments.size());
                
                long successful = gatewayPayments.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                    .count();
                performance.setSuccessfulTransactions(successful);
                
                long failed = gatewayPayments.stream()
                    .filter(p -> "FAILED".equals(p.getStatus()))
                    .count();
                performance.setFailedTransactions(failed);
                
                BigDecimal totalAmount = gatewayPayments.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                    .map(p -> BigDecimal.valueOf(p.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                performance.setTotalAmount(totalAmount);
                
                BigDecimal totalFees = gatewayPayments.stream()
                    .filter(p -> p.getGatewayFee() != null)
                    .map(p -> BigDecimal.valueOf(p.getGatewayFee()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                performance.setTotalFees(totalFees);
                
                if (gatewayPayments.size() > 0) {
                    performance.setSuccessRate((double) successful / gatewayPayments.size() * 100.0);
                } else {
                    performance.setSuccessRate(0.0);
                }
                
                if (successful > 0) {
                    performance.setAverageFee(totalFees.divide(
                        BigDecimal.valueOf(successful), 2, RoundingMode.HALF_UP));
                } else {
                    performance.setAverageFee(BigDecimal.ZERO);
                }
                
                return performance;
            })
            .collect(Collectors.toList());
    }
    
    public List<AnalyticsResponse.PaymentMethodStats> getPaymentMethodStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        
        List<SimplePayment> payments = paymentRepository.findByPaymentTimeBetween(start, end);
        
        Map<String, List<SimplePayment>> paymentsByMethod = payments.stream()
            .collect(Collectors.groupingBy(SimplePayment::getPaymentMethod));
        
        long totalSuccessful = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
            .count();
        
        return paymentsByMethod.entrySet().stream()
            .map(entry -> {
                List<SimplePayment> methodPayments = entry.getValue();
                
                AnalyticsResponse.PaymentMethodStats stats = new AnalyticsResponse.PaymentMethodStats();
                stats.setPaymentMethod(entry.getKey());
                stats.setTransactions((long) methodPayments.size());
                
                long successful = methodPayments.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                    .count();
                stats.setSuccessfulTransactions(successful);
                
                BigDecimal totalAmount = methodPayments.stream()
                    .filter(p -> "COMPLETED".equals(p.getStatus()) || "SUCCESS".equals(p.getStatus()))
                    .map(p -> BigDecimal.valueOf(p.getAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setTotalAmount(totalAmount);
                
                if (totalSuccessful > 0) {
                    stats.setPercentage((double) successful / totalSuccessful * 100.0);
                } else {
                    stats.setPercentage(0.0);
                }
                
                return stats;
            })
            .collect(Collectors.toList());
    }
}

