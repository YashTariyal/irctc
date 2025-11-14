package com.irctc.booking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback implementation for Train Service Client
 * Used when Train Service is unavailable
 */
@Component
public class TrainServiceClientFallback implements TrainServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainServiceClientFallback.class);
    
    @Override
    public TrainResponse getTrainById(Long id) {
        logger.warn("Train Service unavailable - using fallback for train ID: {}", id);
        return null;
    }
    
    @Override
    public TrainResponse getTrainByNumber(String trainNumber) {
        logger.warn("Train Service unavailable - using fallback for train number: {}", trainNumber);
        return null;
    }
    
    @Override
    public FareResponse calculateFare(Long trainId, String sourceStation, String destinationStation, 
                                     String seatClass, Integer passengerCount) {
        logger.warn("Train Service unavailable - using fallback fare calculation");
        FareResponse response = new FareResponse();
        // Use default fare calculation (can be enhanced with cached data)
        BigDecimal defaultFare = new BigDecimal("1000.00");
        response.setFare(defaultFare);
        response.setBaseFare(defaultFare);
        response.setTaxes(BigDecimal.ZERO);
        response.setServiceCharge(BigDecimal.ZERO);
        response.setCurrency("INR");
        return response;
    }
    
    @Override
    public AvailabilityResponse checkAvailability(Long trainId, String date, String seatClass) {
        logger.warn("Train Service unavailable - using fallback availability check");
        AvailabilityResponse response = new AvailabilityResponse();
        response.setAvailable(true); // Assume available in fallback
        response.setAvailableSeats(10);
        response.setWaitlistSeats(0);
        response.setRacSeats(0);
        response.setSeatClass(seatClass);
        return response;
    }
}

