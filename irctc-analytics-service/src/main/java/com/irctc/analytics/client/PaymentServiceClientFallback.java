package com.irctc.analytics.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback implementation for Payment Service Client
 */
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {
    
    @Override
    public Map<String, Object> getPaymentOverview(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }
    
    @Override
    public List<Map<String, Object>> getDailyPaymentStats(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }
    
    @Override
    public List<PaymentDTO> getPaymentsByBookingId(Long bookingId) {
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> getRefundStatistics(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }
}

