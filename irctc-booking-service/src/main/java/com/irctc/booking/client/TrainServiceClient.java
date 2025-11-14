package com.irctc.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for Train Service integration
 */
@FeignClient(name = "irctc-train-service", fallback = TrainServiceClientFallback.class)
public interface TrainServiceClient {
    
    /**
     * Get train by ID
     */
    @GetMapping("/api/trains/{id}")
    TrainResponse getTrainById(@PathVariable Long id);
    
    /**
     * Get train by train number
     */
    @GetMapping("/api/trains/number/{trainNumber}")
    TrainResponse getTrainByNumber(@PathVariable String trainNumber);
    
    /**
     * Calculate fare for a route
     */
    @GetMapping("/api/trains/{trainId}/fare")
    FareResponse calculateFare(
        @PathVariable Long trainId,
        @RequestParam String sourceStation,
        @RequestParam String destinationStation,
        @RequestParam String seatClass,
        @RequestParam(required = false) Integer passengerCount
    );
    
    /**
     * Check seat availability
     */
    @GetMapping("/api/trains/{trainId}/availability")
    AvailabilityResponse checkAvailability(
        @PathVariable Long trainId,
        @RequestParam String date,
        @RequestParam String seatClass
    );
    
    /**
     * Response DTO for Train information
     */
    class TrainResponse {
        private Long id;
        private String trainNumber;
        private String trainName;
        private String sourceStation;
        private String destinationStation;
        private Double baseFare;
        private String trainType;
        private String trainClass;
        private Integer totalSeats;
        private Integer availableSeats;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }
        public Double getBaseFare() { return baseFare; }
        public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }
        public String getTrainType() { return trainType; }
        public void setTrainType(String trainType) { this.trainType = trainType; }
        public String getTrainClass() { return trainClass; }
        public void setTrainClass(String trainClass) { this.trainClass = trainClass; }
        public Integer getTotalSeats() { return totalSeats; }
        public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
    }
    
    /**
     * Response DTO for Fare calculation
     */
    class FareResponse {
        private BigDecimal fare;
        private BigDecimal baseFare;
        private BigDecimal taxes;
        private BigDecimal serviceCharge;
        private String currency;
        private Map<String, Object> breakdown;
        
        // Getters and setters
        public BigDecimal getFare() { return fare; }
        public void setFare(BigDecimal fare) { this.fare = fare; }
        public BigDecimal getBaseFare() { return baseFare; }
        public void setBaseFare(BigDecimal baseFare) { this.baseFare = baseFare; }
        public BigDecimal getTaxes() { return taxes; }
        public void setTaxes(BigDecimal taxes) { this.taxes = taxes; }
        public BigDecimal getServiceCharge() { return serviceCharge; }
        public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Map<String, Object> getBreakdown() { return breakdown; }
        public void setBreakdown(Map<String, Object> breakdown) { this.breakdown = breakdown; }
    }
    
    /**
     * Response DTO for Seat availability
     */
    class AvailabilityResponse {
        private boolean available;
        private Integer availableSeats;
        private Integer waitlistSeats;
        private Integer racSeats;
        private String seatClass;
        
        // Getters and setters
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public Integer getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }
        public Integer getWaitlistSeats() { return waitlistSeats; }
        public void setWaitlistSeats(Integer waitlistSeats) { this.waitlistSeats = waitlistSeats; }
        public Integer getRacSeats() { return racSeats; }
        public void setRacSeats(Integer racSeats) { this.racSeats = racSeats; }
        public String getSeatClass() { return seatClass; }
        public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
    }
}

