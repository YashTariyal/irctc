package com.irctc.payment.service;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.gateway.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for selecting the best payment gateway based on various criteria
 */
@Service
public class GatewaySelectorService {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewaySelectorService.class);
    
    @Autowired
    private List<PaymentGateway> paymentGateways;
    
    @Autowired
    private GatewayStatisticsService statisticsService;
    
    /**
     * Select the best gateway for a payment request
     * Selection criteria:
     * 1. Gateway must be enabled
     * 2. Gateway must support currency and payment method
     * 3. Prefer gateway with higher success rate
     * 4. Prefer gateway with lower fees
     * 5. Respect user preference if provided
     */
    public PaymentGateway selectGateway(PaymentRequest request) {
        List<PaymentGateway> availableGateways = paymentGateways.stream()
            .filter(PaymentGateway::isEnabled)
            .filter(gateway -> gateway.supportsCurrency(request.getCurrency()))
            .filter(gateway -> gateway.supportsPaymentMethod(request.getPaymentMethod()))
            .collect(Collectors.toList());
        
        if (availableGateways.isEmpty()) {
            logger.warn("No available gateways for currency: {}, payment method: {}", 
                request.getCurrency(), request.getPaymentMethod());
            throw new RuntimeException("No payment gateway available for the requested payment method");
        }
        
        // If user has a preference, try to use it
        if (request.getGatewayPreference() != null && !request.getGatewayPreference().isEmpty()) {
            PaymentGateway preferred = availableGateways.stream()
                .filter(g -> g.getGatewayName().equalsIgnoreCase(request.getGatewayPreference()))
                .findFirst()
                .orElse(null);
            
            if (preferred != null) {
                logger.info("Using preferred gateway: {}", preferred.getGatewayName());
                return preferred;
            } else {
                logger.warn("Preferred gateway {} not available, selecting best alternative", 
                    request.getGatewayPreference());
            }
        }
        
        // Select best gateway based on success rate and fees
        PaymentGateway selected = availableGateways.stream()
            .max(Comparator
                .comparingDouble((PaymentGateway g) -> calculateGatewayScore(g, request.getAmount()))
                .thenComparingDouble(g -> statisticsService.calculateSuccessRate(g.getGatewayName())))
            .orElse(availableGateways.get(0));
        
        logger.info("Selected gateway: {} for amount: {}", selected.getGatewayName(), request.getAmount());
        return selected;
    }
    
    /**
     * Calculate a score for gateway selection
     * Higher score = better choice
     * Factors: success rate (70%), fee cost (30%)
     */
    private double calculateGatewayScore(PaymentGateway gateway, BigDecimal amount) {
        double successRate = statisticsService.calculateSuccessRate(gateway.getGatewayName());
        
        // Calculate total fee
        BigDecimal fee = amount
            .multiply(BigDecimal.valueOf(gateway.getTransactionFeePercentage()))
            .divide(BigDecimal.valueOf(100))
            .add(BigDecimal.valueOf(gateway.getFixedFee()));
        
        // Normalize fee to 0-100 scale (assuming max fee is 5% of amount)
        double feeScore = 100.0 - (fee.doubleValue() / amount.doubleValue() * 100.0 * 20.0);
        if (feeScore < 0) feeScore = 0;
        
        // Weighted score: 70% success rate, 30% fee score
        return (successRate * 0.7) + (feeScore * 0.3);
    }
    
    /**
     * Get all available gateways
     */
    public List<PaymentGateway> getAvailableGateways() {
        return paymentGateways.stream()
            .filter(PaymentGateway::isEnabled)
            .collect(Collectors.toList());
    }
    
    /**
     * Get gateway by name
     */
    public PaymentGateway getGatewayByName(String gatewayName) {
        return paymentGateways.stream()
            .filter(g -> g.getGatewayName().equalsIgnoreCase(gatewayName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Gateway not found: " + gatewayName));
    }
}

