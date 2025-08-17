package com.irctc_backend.irctc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    
    private String eventId;
    private String eventType; // BOOKING_CONFIRMED, BOOKING_CANCELLED, PAYMENT_COMPLETED
    
    // Booking details
    private Long bookingId;
    private String pnrNumber;
    private String bookingStatus;
    private String paymentStatus;
    
    // User details
    private Long userId;
    private String userEmail;
    private String userPhone;
    private String userName;
    
    // Train details
    private Long trainId;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    
    // Journey details
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate journeyDate;
    private String departureTime;
    private String arrivalTime;
    
    // Passenger details
    private String passengerName;
    private Integer passengerAge;
    private String passengerGender;
    
    // Seat and coach details
    private String coachNumber;
    private String seatNumber;
    private String coachType;
    
    // Fare details
    private BigDecimal totalFare;
    private BigDecimal baseFare;
    private BigDecimal convenienceFee;
    private BigDecimal gstAmount;
    
    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookingDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventTimestamp;
    
    // Additional info
    private String quotaType;
    private Boolean isTatkal;
    private String bookingSource; // WEB, MOBILE_APP, COUNTER
} 