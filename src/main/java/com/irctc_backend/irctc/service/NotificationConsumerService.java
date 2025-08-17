package com.irctc_backend.irctc.service;

import com.irctc_backend.irctc.dto.BookingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumerService {
    
    /**
     * Email notification consumer
     */
    @KafkaListener(topics = "booking-confirmed", groupId = "email-notification-group")
    public void handleEmailNotification(BookingEvent event) {
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("Sending email to: " + event.getUserEmail());
        System.out.println("Subject: Booking Confirmed - PNR: " + event.getPnrNumber());
        System.out.println("Content: Dear " + event.getUserName() + 
            ", your booking for " + event.getTrainName() + " (" + event.getTrainNumber() + 
            ") from " + event.getSourceStation() + " to " + event.getDestinationStation() + 
            " on " + event.getJourneyDate() + " has been confirmed. PNR: " + event.getPnrNumber());
        System.out.println("==========================");
    }
    
    /**
     * SMS notification consumer
     */
    @KafkaListener(topics = "booking-confirmed", groupId = "sms-notification-group")
    public void handleSmsNotification(BookingEvent event) {
        System.out.println("=== SMS NOTIFICATION ===");
        System.out.println("Sending SMS to: " + event.getUserPhone());
        System.out.println("Message: Your booking PNR " + event.getPnrNumber() + 
            " for " + event.getTrainNumber() + " on " + event.getJourneyDate() + 
            " is confirmed. Seat: " + event.getSeatNumber() + " Coach: " + event.getCoachNumber());
        System.out.println("========================");
    }
    
    /**
     * WhatsApp notification consumer
     */
    @KafkaListener(topics = "booking-confirmed", groupId = "whatsapp-notification-group")
    public void handleWhatsAppNotification(BookingEvent event) {
        System.out.println("=== WHATSAPP NOTIFICATION ===");
        System.out.println("Sending WhatsApp message to: " + event.getUserPhone());
        System.out.println("Message: 🎉 Booking Confirmed!\n" +
            "🚂 " + event.getTrainName() + " (" + event.getTrainNumber() + ")\n" +
            "📅 Date: " + event.getJourneyDate() + "\n" +
            "📍 From: " + event.getSourceStation() + "\n" +
            "🎯 To: " + event.getDestinationStation() + "\n" +
            "💺 Seat: " + event.getSeatNumber() + "\n" +
            "🚃 Coach: " + event.getCoachNumber() + "\n" +
            "🔢 PNR: " + event.getPnrNumber() + "\n" +
            "💰 Amount: ₹" + event.getTotalFare());
        System.out.println("=============================");
    }
    
    /**
     * Booking cancelled notification consumer
     */
    @KafkaListener(topics = "booking-cancelled", groupId = "cancellation-notification-group")
    public void handleCancellationNotification(BookingEvent event) {
        System.out.println("=== CANCELLATION NOTIFICATION ===");
        System.out.println("Sending cancellation notification to: " + event.getUserEmail());
        System.out.println("Subject: Booking Cancelled - PNR: " + event.getPnrNumber());
        System.out.println("Content: Dear " + event.getUserName() + 
            ", your booking PNR " + event.getPnrNumber() + " has been cancelled successfully. " +
            "Refund will be processed within 5-7 business days.");
        System.out.println("=================================");
    }
    
    /**
     * Payment completed notification consumer
     */
    @KafkaListener(topics = "payment-completed", groupId = "payment-notification-group")
    public void handlePaymentNotification(BookingEvent event) {
        System.out.println("=== PAYMENT NOTIFICATION ===");
        System.out.println("Sending payment confirmation to: " + event.getUserEmail());
        System.out.println("Subject: Payment Successful - PNR: " + event.getPnrNumber());
        System.out.println("Content: Dear " + event.getUserName() + 
            ", payment of ₹" + event.getTotalFare() + " for PNR " + event.getPnrNumber() + 
            " has been received successfully. Your booking is now confirmed.");
        System.out.println("=============================");
    }
} 