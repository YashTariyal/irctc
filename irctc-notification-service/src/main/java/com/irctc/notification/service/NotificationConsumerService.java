package com.irctc.notification.service;

import com.irctc.external.notification.SendGridEmailService;
import com.irctc.external.notification.TwilioSmsService;
import com.irctc.shared.events.UserEvents;
import com.irctc.shared.events.BookingEvents;
import com.irctc.shared.events.PaymentEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer Service for Notifications
 */
@Service
public class NotificationConsumerService {

    @Autowired
    private SendGridEmailService emailService;
    
    @Autowired
    private TwilioSmsService smsService;
    
    @Autowired
    private SimpleNotificationService notificationService;

    /**
     * Handle user registration events
     */
    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void handleUserRegistered(UserEvents.UserRegisteredEvent event) {
        System.out.println("Received user registered event: " + event.getUserId());
        
        try {
            // Send welcome email
            emailService.sendBookingConfirmation(
                event.getEmail(),
                event.getFirstName() + " " + event.getLastName(),
                "WELCOME",
                "Welcome to IRCTC!",
                "Your account has been created successfully."
            ).subscribe(
                response -> System.out.println("Welcome email sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send welcome email: " + error.getMessage())
            );
            
            // Create notification record
            com.irctc.notification.entity.SimpleNotification notification = 
                new com.irctc.notification.entity.SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("EMAIL");
            notification.setSubject("Welcome to IRCTC!");
            notification.setMessage("Your account has been created successfully.");
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Error processing user registered event: " + e.getMessage());
        }
    }

    /**
     * Handle user login events
     */
    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void handleUserLogin(UserEvents.UserLoginEvent event) {
        System.out.println("Received user login event: " + event.getUserId());
        
        try {
            // Create login notification record
            com.irctc.notification.entity.SimpleNotification notification = 
                new com.irctc.notification.entity.SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("SYSTEM");
            notification.setSubject("Login Alert");
            notification.setMessage("You logged in from " + event.getIpAddress() + " at " + event.getLoginTime());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Error processing user login event: " + e.getMessage());
        }
    }

    /**
     * Handle booking created events
     */
    @KafkaListener(topics = "booking-events", groupId = "notification-service")
    public void handleBookingCreated(BookingEvents.BookingCreatedEvent event) {
        System.out.println("Received booking created event: " + event.getBookingId());
        
        try {
            // Send booking confirmation email
            emailService.sendBookingConfirmation(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getPnrNumber(),
                "Train Name", // In production, get from train service
                event.getJourneyDate().toString()
            ).subscribe(
                response -> System.out.println("Booking confirmation email sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send booking confirmation email: " + error.getMessage())
            );
            
            // Send booking confirmation SMS
            smsService.sendBookingConfirmationSms(
                "+919876543210", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getPnrNumber(),
                "Train Name" // In production, get from train service
            ).subscribe(
                response -> System.out.println("Booking confirmation SMS sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send booking confirmation SMS: " + error.getMessage())
            );
            
            // Create notification record
            com.irctc.notification.entity.SimpleNotification notification = 
                new com.irctc.notification.entity.SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("BOOKING");
            notification.setSubject("Booking Confirmed");
            notification.setMessage("Your booking has been confirmed. PNR: " + event.getPnrNumber());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Error processing booking created event: " + e.getMessage());
        }
    }

    /**
     * Handle payment completed events
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentCompleted(PaymentEvents.PaymentCompletedEvent event) {
        System.out.println("Received payment completed event: " + event.getPaymentId());
        
        try {
            // Send payment confirmation email
            emailService.sendPaymentConfirmation(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getAmount().toString(),
                event.getTransactionId()
            ).subscribe(
                response -> System.out.println("Payment confirmation email sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send payment confirmation email: " + error.getMessage())
            );
            
            // Send payment confirmation SMS
            smsService.sendPaymentConfirmationSms(
                "+919876543210", // In production, get from user service
                event.getAmount().toString(),
                event.getTransactionId()
            ).subscribe(
                response -> System.out.println("Payment confirmation SMS sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send payment confirmation SMS: " + error.getMessage())
            );
            
            // Create notification record
            com.irctc.notification.entity.SimpleNotification notification = 
                new com.irctc.notification.entity.SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("PAYMENT");
            notification.setSubject("Payment Successful");
            notification.setMessage("Your payment of ₹" + event.getAmount() + " has been processed successfully.");
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Error processing payment completed event: " + e.getMessage());
        }
    }

    /**
     * Handle booking cancelled events
     */
    @KafkaListener(topics = "booking-events", groupId = "notification-service")
    public void handleBookingCancelled(BookingEvents.BookingCancelledEvent event) {
        System.out.println("Received booking cancelled event: " + event.getBookingId());
        
        try {
            // Send cancellation email
            emailService.sendCancellationEmail(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getPnrNumber(),
                event.getRefundAmount().toString()
            ).subscribe(
                response -> System.out.println("Cancellation email sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send cancellation email: " + error.getMessage())
            );
            
            // Send cancellation SMS
            smsService.sendCancellationSms(
                "+919876543210", // In production, get from user service
                event.getPnrNumber(),
                event.getRefundAmount().toString()
            ).subscribe(
                response -> System.out.println("Cancellation SMS sent: " + response.isSuccess()),
                error -> System.err.println("Failed to send cancellation SMS: " + error.getMessage())
            );
            
            // Create notification record
            com.irctc.notification.entity.SimpleNotification notification = 
                new com.irctc.notification.entity.SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("CANCELLATION");
            notification.setSubject("Booking Cancelled");
            notification.setMessage("Your booking has been cancelled. PNR: " + event.getPnrNumber() + 
                                  ". Refund: ₹" + event.getRefundAmount());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
        } catch (Exception e) {
            System.err.println("Error processing booking cancelled event: " + e.getMessage());
        }
    }
}
