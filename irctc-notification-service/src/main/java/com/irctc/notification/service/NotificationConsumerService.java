package com.irctc.notification.service;

import com.irctc.external.notification.SendGridEmailService;
import com.irctc.external.notification.TwilioSmsService;
import com.irctc.shared.events.UserEvents;
import com.irctc.shared.events.BookingEvents;
import com.irctc.shared.events.PaymentEvents;
import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.eventtracking.TrackedEventConsumer;
import com.irctc.notification.eventtracking.TrackedEventConsumer.TrackedEventResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka Consumer Service for Notifications
 * All consumers include idempotency checks to prevent duplicate processing
 */
@Service
public class NotificationConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumerService.class);

    @Autowired
    private SendGridEmailService emailService;
    
    @Autowired(required = false)
    private TwilioSmsService smsService;
    
    @Autowired
    private SimpleNotificationService notificationService;
    
    @Autowired
    private TrackedEventConsumer trackedEventConsumer;

    /**
     * Handle user registration events with idempotency check
     */
    @KafkaListener(topics = "user-events", groupId = "notification-service")
    @Transactional
    public void handleUserRegistered(UserEvents.UserRegisteredEvent event,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                      @Header(KafkaHeaders.OFFSET) Long offset) {
        TrackedEventResult trackingResult = trackedEventConsumer.trackConsumption(
            topic, partition, offset, "notification-service", event
        );
        
        // Check idempotency
        if (trackingResult.isAlreadyProcessed()) {
            logger.info("⚠️ User registered event {} already processed, skipping duplicate - UserId: {}", 
                       trackingResult.getEventId(), event.getUserId());
            return;
        }
        
        logger.info("Received user registered event: {}", event.getUserId());
        trackingResult.markProcessing();
        
        try {
            // Send welcome email (with null check)
            if (emailService != null) {
                emailService.sendBookingConfirmation(
                    event.getEmail(),
                    event.getFirstName() + " " + event.getLastName(),
                    "WELCOME",
                    "Welcome to IRCTC!",
                    "Your account has been created successfully."
                ).subscribe(
                    response -> logger.info("Welcome email sent: {}", response.isSuccess()),
                    error -> logger.error("Failed to send welcome email: {}", error.getMessage())
                );
            } else {
                logger.debug("Email service not available, skipping email send");
            }
            
            // Create notification record
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("EMAIL");
            notification.setSubject("Welcome to IRCTC!");
            notification.setMessage("Your account has been created successfully.");
            notification.setStatus("SENT");
            
            logger.info("Creating notification for user: {}", event.getUserId());
            SimpleNotification createdNotification = notificationService.createNotification(notification);
            logger.info("Notification created with ID: {}", createdNotification.getId());
            
            // Mark as processed
            trackingResult.markProcessed();
            
        } catch (Exception e) {
            trackingResult.markFailed(e);
            logger.error("Error processing user registered event: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        }
    }

    /**
     * Handle user login events with idempotency check
     */
    @KafkaListener(topics = "user-events", groupId = "notification-service")
    @Transactional
    public void handleUserLogin(UserEvents.UserLoginEvent event,
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                @Header(KafkaHeaders.OFFSET) Long offset) {
        TrackedEventResult trackingResult = trackedEventConsumer.trackConsumption(
            topic, partition, offset, "notification-service", event
        );
        
        // Check idempotency
        if (trackingResult.isAlreadyProcessed()) {
            logger.info("⚠️ User login event {} already processed, skipping duplicate - UserId: {}", 
                       trackingResult.getEventId(), event.getUserId());
            return;
        }
        
        logger.info("Received user login event: {}", event.getUserId());
        trackingResult.markProcessing();
        
        try {
            // Create login notification record
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("SYSTEM");
            notification.setSubject("Login Alert");
            notification.setMessage("You logged in from " + event.getIpAddress() + " at " + event.getLoginTime());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
            // Mark as processed
            trackingResult.markProcessed();
            
        } catch (Exception e) {
            trackingResult.markFailed(e);
            logger.error("Error processing user login event: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle booking created events with idempotency check
     */
    @KafkaListener(topics = {"booking-created", "booking-events"}, groupId = "notification-service")
    @Transactional
    public void handleBookingCreated(BookingEvents.BookingCreatedEvent event,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                     @Header(KafkaHeaders.OFFSET) Long offset) {
        TrackedEventResult trackingResult = trackedEventConsumer.trackConsumption(
            topic, partition, offset, "notification-service", event
        );
        
        // Check idempotency
        if (trackingResult.isAlreadyProcessed()) {
            logger.info("⚠️ Booking created event {} already processed, skipping duplicate - BookingId: {}", 
                       trackingResult.getEventId(), event.getBookingId());
            return;
        }
        
        logger.info("Received booking created event: {}", event.getBookingId());
        trackingResult.markProcessing();
        
        try {
            // Send booking confirmation email
            emailService.sendBookingConfirmation(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getPnrNumber(),
                "Train Name", // In production, get from train service
                event.getJourneyDate().toString()
            ).subscribe(
                response -> logger.info("Booking confirmation email sent: {}", response.isSuccess()),
                error -> logger.error("Failed to send booking confirmation email: {}", error.getMessage())
            );
            
            // Send booking confirmation SMS
            if (smsService != null) {
                smsService.sendBookingConfirmationSms(
                    "+919876543210", // In production, get from user service
                    "Passenger Name", // In production, get from user service
                    event.getPnrNumber(),
                    "Train Name" // In production, get from train service
                ).subscribe(
                    response -> logger.info("Booking confirmation SMS sent: {}", response.isSuccess()),
                    error -> logger.error("Failed to send booking confirmation SMS: {}", error.getMessage())
                );
            }
            
            // Create notification record
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("BOOKING");
            notification.setSubject("Booking Confirmed");
            notification.setMessage("Your booking has been confirmed. PNR: " + event.getPnrNumber());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
            // Mark as processed
            trackingResult.markProcessed();
            
        } catch (Exception e) {
            trackingResult.markFailed(e);
            logger.error("Error processing booking created event: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle payment completed events with idempotency check
     */
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    @Transactional
    public void handlePaymentCompleted(PaymentEvents.PaymentCompletedEvent event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                       @Header(KafkaHeaders.OFFSET) Long offset) {
        TrackedEventResult trackingResult = trackedEventConsumer.trackConsumption(
            topic, partition, offset, "notification-service", event
        );
        
        // Check idempotency
        if (trackingResult.isAlreadyProcessed()) {
            logger.info("⚠️ Payment completed event {} already processed, skipping duplicate - PaymentId: {}", 
                       trackingResult.getEventId(), event.getPaymentId());
            return;
        }
        
        logger.info("Received payment completed event: {}", event.getPaymentId());
        trackingResult.markProcessing();
        
        try {
            // Send payment confirmation email
            emailService.sendPaymentConfirmation(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getAmount().toString(),
                event.getTransactionId()
            ).subscribe(
                response -> logger.info("Payment confirmation email sent: {}", response.isSuccess()),
                error -> logger.error("Failed to send payment confirmation email: {}", error.getMessage())
            );
            
            // Send payment confirmation SMS
            if (smsService != null) {
                smsService.sendPaymentConfirmationSms(
                    "+919876543210", // In production, get from user service
                    event.getAmount().toString(),
                    event.getTransactionId()
                ).subscribe(
                    response -> logger.info("Payment confirmation SMS sent: {}", response.isSuccess()),
                    error -> logger.error("Failed to send payment confirmation SMS: {}", error.getMessage())
                );
            }
            
            // Create notification record
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("PAYMENT");
            notification.setSubject("Payment Successful");
            notification.setMessage("Your payment of ₹" + event.getAmount() + " has been processed successfully.");
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
            // Mark as processed
            trackingResult.markProcessed();
            
        } catch (Exception e) {
            trackingResult.markFailed(e);
            logger.error("Error processing payment completed event: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handle booking cancelled events with idempotency check
     */
    @KafkaListener(topics = {"booking-cancelled", "booking-events"}, groupId = "notification-service")
    @Transactional
    public void handleBookingCancelled(BookingEvents.BookingCancelledEvent event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                                       @Header(KafkaHeaders.OFFSET) Long offset) {
        TrackedEventResult trackingResult = trackedEventConsumer.trackConsumption(
            topic, partition, offset, "notification-service", event
        );
        
        // Check idempotency
        if (trackingResult.isAlreadyProcessed()) {
            logger.info("⚠️ Booking cancelled event {} already processed, skipping duplicate - BookingId: {}", 
                       trackingResult.getEventId(), event.getBookingId());
            return;
        }
        
        logger.info("Received booking cancelled event: {}", event.getBookingId());
        trackingResult.markProcessing();
        
        try {
            // Send cancellation email
            emailService.sendCancellationEmail(
                "user@example.com", // In production, get from user service
                "Passenger Name", // In production, get from user service
                event.getPnrNumber(),
                event.getRefundAmount().toString()
            ).subscribe(
                response -> logger.info("Cancellation email sent: {}", response.isSuccess()),
                error -> logger.error("Failed to send cancellation email: {}", error.getMessage())
            );
            
            // Send cancellation SMS
            if (smsService != null) {
                smsService.sendCancellationSms(
                    "+919876543210", // In production, get from user service
                    event.getPnrNumber(),
                    event.getRefundAmount().toString()
                ).subscribe(
                    response -> logger.info("Cancellation SMS sent: {}", response.isSuccess()),
                    error -> logger.error("Failed to send cancellation SMS: {}", error.getMessage())
                );
            }
            
            // Create notification record
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("CANCELLATION");
            notification.setSubject("Booking Cancelled");
            notification.setMessage("Your booking has been cancelled. PNR: " + event.getPnrNumber() + 
                                  ". Refund: ₹" + event.getRefundAmount());
            notification.setStatus("SENT");
            
            notificationService.createNotification(notification);
            
            // Mark as processed
            trackingResult.markProcessed();
            
        } catch (Exception e) {
            trackingResult.markFailed(e);
            logger.error("Error processing booking cancelled event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
