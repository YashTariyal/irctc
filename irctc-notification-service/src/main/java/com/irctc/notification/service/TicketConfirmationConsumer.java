package com.irctc.notification.service;

import com.irctc.shared.events.BookingEvents;
import com.irctc.notification.entity.SimpleNotification;
import com.irctc.notification.repository.SimpleNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consumer service for handling ticket confirmation events
 * Processes confirmation events and sends multi-channel notifications
 */
@Service
public class TicketConfirmationConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketConfirmationConsumer.class);
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private SimpleNotificationRepository notificationRepository;
    
    /**
     * Consume ticket confirmation events from Kafka
     */
    @KafkaListener(topics = {"ticket-confirmation-events", "ticket-confirmation-events.DLT"}, groupId = "notification-service")
    @Transactional
    public void handleTicketConfirmation(BookingEvents.TicketConfirmationEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        String requestId = event.getRequestId() != null ? event.getRequestId() : "unknown";
        logger.info("Processing ticket confirmation event from topic={} for user: {}, PNR: {} - RequestId: {}",
                   topic, event.getUserId(), event.getPnrNumber(), requestId);

        // Simulation hook to force DLQ routing for testing
        boolean simulateDlq = Boolean.getBoolean("simulate.dlq");
        if (simulateDlq && (topic == null || !topic.endsWith(".DLT"))) {
            logger.warn("simulate.dlq enabled - throwing to route to DLT for PNR: {}", event.getPnrNumber());
            throw new RuntimeException("Simulated processing failure");
        }
        
        try {
            // Send multi-channel notifications
            sendEmailNotification(event, requestId);
            sendSmsNotification(event, requestId);
            sendPushNotification(event, requestId);
            
            // Store notification record
            storeNotificationRecord(event, requestId);
            
            logger.info("Successfully processed confirmation event for PNR: {} - RequestId: {}", 
                       event.getPnrNumber(), requestId);
            
        } catch (Exception e) {
            logger.error("Error processing confirmation event for PNR: {} - RequestId: {}", 
                        event.getPnrNumber(), requestId, e);
        }
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            String subject = "🎉 Your Ticket is Confirmed! PNR: " + event.getPnrNumber();
            String emailBody = buildEmailTemplate(event);
            
            emailService.sendEmail(
                event.getPassengerEmail(),
                subject,
                emailBody
            ).subscribe(
                response -> {
                    logger.info("Confirmation email sent successfully for PNR: {} - RequestId: {}", 
                               event.getPnrNumber(), requestId);
                },
                error -> {
                    logger.error("Failed to send confirmation email for PNR: {} - RequestId: {}, Error: {}", 
                               event.getPnrNumber(), requestId, error.getMessage());
                }
            );
            
        } catch (Exception e) {
            logger.error("Error sending email notification for PNR: {} - RequestId: {}", 
                        event.getPnrNumber(), requestId, e);
        }
    }
    
    /**
     * Send SMS notification
     */
    private void sendSmsNotification(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            String smsMessage = String.format(
                "🎉 Your ticket is CONFIRMED! PNR: %s, Train: %s, Seat: %s-%s, Date: %s. Safe journey!",
                event.getPnrNumber(),
                event.getTrainNumber(),
                event.getCoachNumber(),
                event.getSeatNumber(),
                event.getJourneyDate().toLocalDate()
            );
            
            smsService.sendSms(event.getPassengerPhone(), smsMessage)
                .subscribe(
                    response -> {
                        logger.info("Confirmation SMS sent successfully for PNR: {} - RequestId: {}", 
                                   event.getPnrNumber(), requestId);
                    },
                    error -> {
                        logger.error("Failed to send confirmation SMS for PNR: {} - RequestId: {}, Error: {}", 
                                   event.getPnrNumber(), requestId, error.getMessage());
                    }
                );
            
        } catch (Exception e) {
            logger.error("Error sending SMS notification for PNR: {} - RequestId: {}", 
                        event.getPnrNumber(), requestId, e);
        }
    }
    
    /**
     * Send push notification
     */
    private void sendPushNotification(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            String title = "🎉 Ticket Confirmed!";
            String body = String.format("Your ticket is confirmed. PNR: %s, Seat: %s-%s",
                                      event.getPnrNumber(), event.getCoachNumber(), event.getSeatNumber());
            
            Map<String, Object> data = Map.of(
                "pnr", event.getPnrNumber(),
                "trainNumber", event.getTrainNumber(),
                "seatNumber", event.getSeatNumber(),
                "coachNumber", event.getCoachNumber(),
                "journeyDate", event.getJourneyDate().toString()
            );
            
            pushNotificationService.sendPushNotification(
                event.getUserId(),
                title,
                body,
                data
            ).subscribe(
                response -> {
                    logger.info("Confirmation push notification sent for PNR: {} - RequestId: {}", 
                               event.getPnrNumber(), requestId);
                },
                error -> {
                    logger.error("Failed to send push notification for PNR: {} - RequestId: {}, Error: {}", 
                               event.getPnrNumber(), requestId, error.getMessage());
                }
            );
            
        } catch (Exception e) {
            logger.error("Error sending push notification for PNR: {} - RequestId: {}", 
                        event.getPnrNumber(), requestId, e);
        }
    }
    
    /**
     * Store notification record in database
     */
    private void storeNotificationRecord(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            SimpleNotification notification = new SimpleNotification();
            notification.setUserId(event.getUserId());
            notification.setType("TICKET_CONFIRMATION");
            notification.setSubject("Ticket Confirmed: PNR " + event.getPnrNumber());
            notification.setMessage(String.format("Your ticket for Train %s (%s) on %s is now CONFIRMED! Seat: %s-%s. PNR: %s.",
                event.getTrainName(), event.getTrainNumber(), event.getJourneyDate().toLocalDate(),
                event.getCoachNumber(), event.getSeatNumber(), event.getPnrNumber()));
            notification.setStatus("SENT");
            notification.setCreatedAt(LocalDateTime.now());
            notification.setMetadata(String.format(
                "PNR: %s, Train: %s, Seat: %s-%s, Previous Status: %s", 
                event.getPnrNumber(),
                event.getTrainNumber(),
                event.getCoachNumber(),
                event.getSeatNumber(),
                event.getPreviousStatus()
            ));
            
            notificationRepository.save(notification);
            
            logger.info("Notification record stored for PNR: {} - RequestId: {}", 
                       event.getPnrNumber(), requestId);
            
        } catch (Exception e) {
            logger.error("Error storing notification record for PNR: {} - RequestId: {}", 
                        event.getPnrNumber(), requestId, e);
        }
    }
    
    /**
     * Build comprehensive email template
     */
    private String buildEmailTemplate(BookingEvents.TicketConfirmationEvent event) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Ticket Confirmation</title>
                <style>
                    body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; line-height: 1.6; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { padding: 30px; background: #f8f9fa; }
                    .details { background: white; padding: 25px; border-radius: 10px; margin: 20px 0; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .status-badge { background: #28a745; color: white; padding: 8px 16px; border-radius: 20px; font-weight: bold; }
                    .footer { background: #e9ecef; padding: 20px; text-align: center; border-radius: 0 0 10px 10px; }
                    table { width: 100%%; border-collapse: collapse; }
                    td { padding: 10px; border-bottom: 1px solid #eee; }
                    .highlight { background: #fff3cd; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎉 Congratulations! Your Ticket is Confirmed</h1>
                    <p>Your journey is confirmed and ready to go!</p>
                </div>
                
                <div class="content">
                    <div class="details">
                        <h2>📋 Booking Details</h2>
                        <table>
                            <tr><td><strong>PNR Number:</strong></td><td><strong>%s</strong></td></tr>
                            <tr><td><strong>Train:</strong></td><td>%s - %s</td></tr>
                            <tr><td><strong>Journey Date:</strong></td><td>%s</td></tr>
                            <tr><td><strong>Route:</strong></td><td>%s to %s</td></tr>
                            <tr><td><strong>Departure:</strong></td><td>%s</td></tr>
                            <tr><td><strong>Arrival:</strong></td><td>%s</td></tr>
                        </table>
                    </div>
                    
                    <div class="details">
                        <h2>🎫 Seat Information</h2>
                        <table>
                            <tr><td><strong>Seat Number:</strong></td><td><strong>%s</strong></td></tr>
                            <tr><td><strong>Coach Number:</strong></td><td><strong>%s</strong></td></tr>
                            <tr><td><strong>Coach Type:</strong></td><td>%s</td></tr>
                            <tr><td><strong>Berth Type:</strong></td><td>Lower</td></tr>
                            <tr><td><strong>Seat Type:</strong></td><td>Window</td></tr>
                            <tr><td><strong>Quota:</strong></td><td>General</td></tr>
                        </table>
                    </div>
                    
                    <div class="details">
                        <h2>💰 Fare Details</h2>
                        <table>
                            <tr><td><strong>Total Fare:</strong></td><td><strong>₹%s</strong></td></tr>
                            <tr><td><strong>Payment Status:</strong></td><td>✅ Completed</td></tr>
                            <tr><td><strong>Confirmation Time:</strong></td><td>%s</td></tr>
                        </table>
                    </div>
                    
                    <div class="highlight">
                        <h3>📈 Status Update</h3>
                        <p><strong>Previous Status:</strong> %s</p>
                        <p><strong>Current Status:</strong> <span class="status-badge">CONFIRMED ✅</span></p>
                        <p><strong>Confirmation Reason:</strong> Seat became available</p>
                    </div>
                    
                    <div class="details">
                        <h2>📱 Important Information</h2>
                        <ul>
                            <li>Please arrive at the station at least 30 minutes before departure</li>
                            <li>Carry a valid ID proof for verification</li>
                            <li>Your PNR number is your ticket reference</li>
                            <li>For any queries, contact our customer support</li>
                        </ul>
                    </div>
                </div>
                
                <div class="footer">
                    <p><strong>Have a safe and pleasant journey! 🚂</strong></p>
                    <p>Thank you for choosing IRCTC Railway Services</p>
                    <p>For support: support@irctc.com | Phone: 139</p>
                </div>
            </body>
            </html>
            """,
            event.getPnrNumber(),
            event.getTrainNumber(),
            event.getTrainName(),
            event.getJourneyDate(),
            event.getSourceStation(),
            event.getDestinationStation(),
            event.getDepartureTime(),
            event.getArrivalTime(),
            event.getSeatNumber(),
            event.getCoachNumber(),
            event.getCoachType(),
            event.getFare(),
            event.getConfirmationTime(),
            event.getPreviousStatus()
        );
    }
}
