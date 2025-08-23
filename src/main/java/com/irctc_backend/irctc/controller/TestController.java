package com.irctc_backend.irctc.controller;

import com.irctc_backend.irctc.dto.BookingEvent;
import com.irctc_backend.irctc.service.NotificationService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Hidden
public class TestController {
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping("/kafka")
    public ResponseEntity<String> testKafka() {
        try {
            // Create a test booking event
            BookingEvent testEvent = new BookingEvent();
            testEvent.setEventId("test-event-123");
            testEvent.setEventType("BOOKING_CONFIRMED");
            testEvent.setBookingId(1L);
            testEvent.setPnrNumber("TEST123456");
            testEvent.setBookingStatus("CONFIRMED");
            testEvent.setPaymentStatus("PENDING");
            testEvent.setUserId(1L);
            testEvent.setUserEmail("test@example.com");
            testEvent.setUserPhone("9876543210");
            testEvent.setUserName("Test User");
            testEvent.setTrainId(1L);
            testEvent.setTrainNumber("12345");
            testEvent.setTrainName("Test Train");
            testEvent.setSourceStation("Test Source");
            testEvent.setDestinationStation("Test Destination");
            testEvent.setJourneyDate(LocalDate.now());
            testEvent.setDepartureTime("10:00:00");
            testEvent.setArrivalTime("18:00:00");
            testEvent.setPassengerName("Test Passenger");
            testEvent.setPassengerAge(25);
            testEvent.setPassengerGender("MALE");
            testEvent.setCoachNumber("A1");
            testEvent.setSeatNumber("1");
            testEvent.setCoachType("AC_FIRST_CLASS");
            testEvent.setTotalFare(new BigDecimal("1000.00"));
            testEvent.setBaseFare(new BigDecimal("900.00"));
            testEvent.setConvenienceFee(new BigDecimal("50.00"));
            testEvent.setGstAmount(new BigDecimal("50.00"));
            testEvent.setBookingDate(LocalDateTime.now());
            testEvent.setEventTimestamp(LocalDateTime.now());
            testEvent.setQuotaType("GENERAL");
            testEvent.setIsTatkal(false);
            testEvent.setBookingSource("TEST");
            
            // Test publishing to Kafka
            notificationService.publishBookingConfirmedEvent(testEvent);
            
            return ResponseEntity.ok("Kafka test event published successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Kafka test failed: " + e.getMessage());
        }
    }
} 