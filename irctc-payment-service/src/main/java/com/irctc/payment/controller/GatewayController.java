package com.irctc.payment.controller;

import com.irctc.payment.entity.GatewayStatistics;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.service.GatewaySelectorService;
import com.irctc.payment.service.GatewayStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for payment gateway management
 */
@RestController
@RequestMapping("/api/payments/gateways")
public class GatewayController {
    
    @Autowired(required = false)
    private GatewaySelectorService gatewaySelectorService;
    
    @Autowired(required = false)
    private GatewayStatisticsService statisticsService;
    
    /**
     * Get all available payment gateways
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAvailableGateways() {
        if (gatewaySelectorService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<Map<String, Object>> gateways = gatewaySelectorService.getAvailableGateways().stream()
            .map(gateway -> {
                Map<String, Object> info = new HashMap<>();
                info.put("name", gateway.getGatewayName());
                info.put("enabled", gateway.isEnabled());
                info.put("feePercentage", gateway.getTransactionFeePercentage());
                info.put("fixedFee", gateway.getFixedFee());
                
                if (statisticsService != null) {
                    GatewayStatistics stats = statisticsService.getStatistics(gateway.getGatewayName()).orElse(null);
                    if (stats != null) {
                        info.put("successRate", statisticsService.calculateSuccessRate(gateway.getGatewayName()));
                        info.put("totalTransactions", stats.getTotalTransactions());
                        info.put("averageFee", statisticsService.calculateAverageFee(gateway.getGatewayName()));
                    }
                }
                
                return info;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(gateways);
    }
    
    /**
     * Get statistics for all gateways
     */
    @GetMapping("/stats")
    public ResponseEntity<List<GatewayStatistics>> getGatewayStatistics() {
        if (statisticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        return ResponseEntity.ok(statisticsService.getAllStatistics());
    }
    
    /**
     * Get statistics for a specific gateway
     */
    @GetMapping("/stats/{gatewayName}")
    public ResponseEntity<Map<String, Object>> getGatewayStatistics(@PathVariable String gatewayName) {
        if (statisticsService == null) {
            return ResponseEntity.notFound().build();
        }
        
        GatewayStatistics stats = statisticsService.getStatistics(gatewayName)
            .orElse(null);
        
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("gatewayName", stats.getGatewayName());
        response.put("totalTransactions", stats.getTotalTransactions());
        response.put("successfulTransactions", stats.getSuccessfulTransactions());
        response.put("failedTransactions", stats.getFailedTransactions());
        response.put("successRate", statisticsService.calculateSuccessRate(gatewayName));
        response.put("totalAmount", stats.getTotalAmount());
        response.put("totalFees", stats.getTotalFees());
        response.put("averageFee", statisticsService.calculateAverageFee(gatewayName));
        response.put("lastTransactionTime", stats.getLastTransactionTime());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get gateway comparison (fees and success rates)
     */
    @GetMapping("/compare")
    public ResponseEntity<List<Map<String, Object>>> compareGateways(
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String paymentMethod) {
        
        if (gatewaySelectorService == null || statisticsService == null) {
            return ResponseEntity.ok(List.of());
        }
        
        List<PaymentGateway> gateways = gatewaySelectorService.getAvailableGateways();
        
        if (currency != null) {
            gateways = gateways.stream()
                .filter(g -> g.supportsCurrency(currency))
                .collect(Collectors.toList());
        }
        
        if (paymentMethod != null) {
            gateways = gateways.stream()
                .filter(g -> g.supportsPaymentMethod(paymentMethod))
                .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> comparison = gateways.stream()
            .map(gateway -> {
                Map<String, Object> info = new HashMap<>();
                info.put("gatewayName", gateway.getGatewayName());
                info.put("feePercentage", gateway.getTransactionFeePercentage());
                info.put("fixedFee", gateway.getFixedFee());
                
                GatewayStatistics stats = statisticsService.getStatistics(gateway.getGatewayName()).orElse(null);
                if (stats != null) {
                    info.put("successRate", statisticsService.calculateSuccessRate(gateway.getGatewayName()));
                    info.put("totalTransactions", stats.getTotalTransactions());
                    info.put("averageFee", statisticsService.calculateAverageFee(gateway.getGatewayName()));
                } else {
                    info.put("successRate", 0.0);
                    info.put("totalTransactions", 0L);
                    info.put("averageFee", 0.0);
                }
                
                return info;
            })
            .sorted((a, b) -> {
                // Sort by success rate (descending), then by fee (ascending)
                double rateA = (Double) a.get("successRate");
                double rateB = (Double) b.get("successRate");
                if (rateA != rateB) {
                    return Double.compare(rateB, rateA);
                }
                double feeA = ((Number) a.get("averageFee")).doubleValue();
                double feeB = ((Number) b.get("averageFee")).doubleValue();
                return Double.compare(feeA, feeB);
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(comparison);
    }
}

