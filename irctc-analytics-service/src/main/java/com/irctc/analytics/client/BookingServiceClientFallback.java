package com.irctc.analytics.client;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback implementation for Booking Service Client
 */
@Component
public class BookingServiceClientFallback implements BookingServiceClient {
    
    @Override
    public List<BookingDTO> getAllBookings() {
        return new ArrayList<>();
    }
    
    @Override
    public List<BookingDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>();
    }
    
    @Override
    public List<BookingDTO> getBookingsByUserId(Long userId) {
        return new ArrayList<>();
    }
    
    @Override
    public Map<String, Object> getBookingStatistics(LocalDate startDate, LocalDate endDate) {
        return new HashMap<>();
    }
}

