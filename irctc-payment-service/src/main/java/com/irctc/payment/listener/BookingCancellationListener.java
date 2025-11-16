package com.irctc.payment.listener;

import com.irctc.payment.service.AutomatedRefundService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka listener for booking cancellation events to trigger automatic refunds
 */
@Component
public class BookingCancellationListener {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingCancellationListener.class);
    
    @Autowired
    private AutomatedRefundService automatedRefundService;
    
    /**
     * Listen for booking cancellation events and trigger auto-refund
     */
    @KafkaListener(topics = "booking-cancelled", groupId = "payment-service-refund-group")
    public void handleBookingCancellation(Map<String, Object> event) {
        try {
            logger.info("📥 Received booking cancellation event: {}", event);
            
            Long bookingId = Long.valueOf(event.get("bookingId").toString());
            LocalDateTime cancellationTime = LocalDateTime.now();
            LocalDateTime departureTime = null;
            String reason = "Booking cancellation";
            
            // Extract departure time if available
            if (event.containsKey("departureTime")) {
                departureTime = LocalDateTime.parse(event.get("departureTime").toString());
            } else if (event.containsKey("travelDate")) {
                // Fallback to travel date
                departureTime = LocalDateTime.parse(event.get("travelDate").toString());
            }
            
            // Extract cancellation reason if available
            if (event.containsKey("cancellationReason")) {
                reason = event.get("cancellationReason").toString();
            }
            
            // Trigger automatic refund
            automatedRefundService.autoRefundOnCancellation(
                bookingId, cancellationTime, departureTime, reason
            );
            
            logger.info("✅ Auto-refund triggered for booking: {}", bookingId);
        } catch (Exception e) {
            logger.error("Error processing booking cancellation event: {}", e.getMessage(), e);
        }
    }
}

