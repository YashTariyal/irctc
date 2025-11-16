package com.irctc.analytics.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Feign client for Booking Service integration
 */
@FeignClient(name = "irctc-booking-service", fallback = BookingServiceClientFallback.class)
public interface BookingServiceClient {
    
    /**
     * Get all bookings
     */
    @GetMapping("/api/bookings")
    List<BookingDTO> getAllBookings();
    
    /**
     * Get bookings by date range - uses getAllBookings and filters client-side
     * Note: In production, this should be a dedicated endpoint in booking service
     */
    default List<BookingDTO> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<BookingDTO> allBookings = getAllBookings();
        return allBookings.stream()
                .filter(b -> {
                    LocalDate bookingDate = b.getBookingDate();
                    if (bookingDate == null) return false;
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                })
                .toList();
    }
    
    /**
     * Get bookings by user ID
     */
    @GetMapping("/api/bookings/user/{userId}")
    List<BookingDTO> getBookingsByUserId(@PathVariable Long userId);
    
    /**
     * Get booking statistics
     */
    @GetMapping("/api/bookings/statistics")
    Map<String, Object> getBookingStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
    
    /**
     * Booking DTO
     */
    class BookingDTO {
        private Long id;
        private Long userId;
        private String pnrNumber;
        private String sourceStation;
        private String destinationStation;
        private String trainNumber;
        private String trainName;
        private String bookingStatus;
        private String paymentStatus;
        private Double totalAmount;
        private LocalDate travelDate;
        private LocalDate bookingDate;
        private Integer numberOfPassengers;
        private String seatClass;
        private Boolean isCancelled;
        private LocalDate cancellationDate;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getPnrNumber() { return pnrNumber; }
        public void setPnrNumber(String pnrNumber) { this.pnrNumber = pnrNumber; }
        public String getSourceStation() { return sourceStation; }
        public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }
        public String getDestinationStation() { return destinationStation; }
        public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }
        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public String getTrainName() { return trainName; }
        public void setTrainName(String trainName) { this.trainName = trainName; }
        public String getBookingStatus() { return bookingStatus; }
        public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public LocalDate getTravelDate() { return travelDate; }
        public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
        public LocalDate getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
        public Integer getNumberOfPassengers() { return numberOfPassengers; }
        public void setNumberOfPassengers(Integer numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
        public String getSeatClass() { return seatClass; }
        public void setSeatClass(String seatClass) { this.seatClass = seatClass; }
        public Boolean getIsCancelled() { return isCancelled; }
        public void setIsCancelled(Boolean isCancelled) { this.isCancelled = isCancelled; }
        public LocalDate getCancellationDate() { return cancellationDate; }
        public void setCancellationDate(LocalDate cancellationDate) { this.cancellationDate = cancellationDate; }
    }
}

