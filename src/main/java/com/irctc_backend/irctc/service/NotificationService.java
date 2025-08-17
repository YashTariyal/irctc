package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.BookingEvent;
import com.irctc_backend.irctc.entity.Booking;
import com.irctc_backend.irctc.entity.Passenger;
import com.irctc_backend.irctc.entity.Train;
import com.irctc_backend.irctc.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {
    
    @Autowired
    private KafkaTemplate<String, BookingEvent> kafkaTemplate;
    
    private static final String BOOKING_CONFIRMED_TOPIC = "booking-confirmed";
    private static final String BOOKING_CANCELLED_TOPIC = "booking-cancelled";
    private static final String PAYMENT_COMPLETED_TOPIC = "payment-completed";
    
    /**
     * Publish booking confirmed event to Kafka
     */
    public void publishBookingConfirmedEvent(Booking booking) {
        BookingEvent event = createBookingEvent(booking, "BOOKING_CONFIRMED");
        publishBookingEvent(event, booking.getPnrNumber());
    }
    
    /**
     * Publish booking event to Kafka (for testing)
     */
    public void publishBookingConfirmedEvent(BookingEvent event) {
        publishBookingEvent(event, event.getPnrNumber());
    }
    
    /**
     * Publish booking event to Kafka
     */
    private void publishBookingEvent(BookingEvent event, String key) {
        CompletableFuture<SendResult<String, BookingEvent>> future = 
            kafkaTemplate.send(BOOKING_CONFIRMED_TOPIC, key, event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Booking confirmed event sent successfully to Kafka: " + 
                    result.getRecordMetadata().topic() + " partition: " + 
                    result.getRecordMetadata().partition() + " offset: " + 
                    result.getRecordMetadata().offset());
            } else {
                System.err.println("Failed to send booking confirmed event to Kafka: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Publish booking cancelled event to Kafka
     */
    public void publishBookingCancelledEvent(Booking booking) {
        BookingEvent event = createBookingEvent(booking, "BOOKING_CANCELLED");
        
        CompletableFuture<SendResult<String, BookingEvent>> future = 
            kafkaTemplate.send(BOOKING_CANCELLED_TOPIC, booking.getPnrNumber(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Booking cancelled event sent successfully to Kafka: " + 
                    result.getRecordMetadata().topic() + " partition: " + 
                    result.getRecordMetadata().partition() + " offset: " + 
                    result.getRecordMetadata().offset());
            } else {
                System.err.println("Failed to send booking cancelled event to Kafka: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Publish payment completed event to Kafka
     */
    public void publishPaymentCompletedEvent(Booking booking) {
        BookingEvent event = createBookingEvent(booking, "PAYMENT_COMPLETED");
        
        CompletableFuture<SendResult<String, BookingEvent>> future = 
            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, booking.getPnrNumber(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Payment completed event sent successfully to Kafka: " + 
                    result.getRecordMetadata().topic() + " partition: " + 
                    result.getRecordMetadata().partition() + " offset: " + 
                    result.getRecordMetadata().offset());
            } else {
                System.err.println("Failed to send payment completed event to Kafka: " + ex.getMessage());
            }
        });
    }
    
    /**
     * Create BookingEvent DTO from Booking entity
     */
    private BookingEvent createBookingEvent(Booking booking, String eventType) {
        User user = booking.getUser();
        Train train = booking.getTrain();
        Passenger passenger = booking.getPassenger();
        
        BookingEvent event = new BookingEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        
        // Booking details
        event.setBookingId(booking.getId());
        event.setPnrNumber(booking.getPnrNumber());
        event.setBookingStatus(booking.getStatus().toString());
        event.setPaymentStatus(booking.getPaymentStatus().toString());
        
        // User details
        event.setUserId(user.getId());
        event.setUserEmail(user.getEmail());
        event.setUserPhone(user.getPhoneNumber());
        event.setUserName(user.getFirstName() + " " + user.getLastName());
        
        // Train details
        event.setTrainId(train.getId());
        event.setTrainNumber(train.getTrainNumber());
        event.setTrainName(train.getTrainName());
        event.setSourceStation(train.getSourceStation().getStationName());
        event.setDestinationStation(train.getDestinationStation().getStationName());
        
        // Journey details
        event.setJourneyDate(booking.getJourneyDate());
        event.setDepartureTime(train.getDepartureTime().toString());
        event.setArrivalTime(train.getArrivalTime().toString());
        
        // Passenger details
        event.setPassengerName(passenger.getFirstName() + " " + passenger.getLastName());
        event.setPassengerAge(passenger.getAge());
        event.setPassengerGender(passenger.getGender().toString());
        
        // Seat and coach details
        if (booking.getSeat() != null) {
            event.setSeatNumber(booking.getSeat().getSeatNumber());
        }
        if (booking.getCoach() != null) {
            event.setCoachNumber(booking.getCoach().getCoachNumber());
            event.setCoachType(booking.getCoach().getCoachType().toString());
        }
        
        // Fare details
        event.setTotalFare(booking.getTotalFare());
        event.setBaseFare(booking.getBaseFare());
        event.setConvenienceFee(booking.getConvenienceFee());
        event.setGstAmount(booking.getGstAmount());
        
        // Timestamps
        event.setBookingDate(booking.getBookingDate());
        event.setEventTimestamp(LocalDateTime.now());
        
        // Additional info
        event.setQuotaType(booking.getQuotaType().toString());
        event.setIsTatkal(booking.getIsTatkal());
        event.setBookingSource(booking.getBookingSource());
        
        return event;
    }
} 