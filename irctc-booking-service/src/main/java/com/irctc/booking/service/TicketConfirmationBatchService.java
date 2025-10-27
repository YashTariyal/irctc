package com.irctc.booking.service;

import com.irctc.shared.events.BookingEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Ticket Confirmation Batch Processing Service
 * 
 * This service handles the automated processing of ticket confirmations
 * for RAC and Waitlist tickets when seats become available.
 * 
 * Responsibilities:
 * - Scheduled batch processing (every 30 minutes)
 * - RAC to Confirmed ticket conversion
 * - Waitlist to Confirmed ticket conversion
 * - Kafka event publishing for notifications
 * - Metrics and monitoring
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@EnableScheduling
@Transactional
public class TicketConfirmationBatchService {

    private static final Logger logger = LoggerFactory.getLogger(TicketConfirmationBatchService.class);
    private static final Random RANDOM = new Random();

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private SimpleBookingService bookingService;

    /**
     * Main scheduled job that runs every 30 minutes to process ticket confirmations
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void processTicketConfirmations() {
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        logger.info("🎫 Starting ticket confirmation batch processing - RequestId: {}", requestId);

        try {
            // Simulate processing confirmations
            int totalConfirmations = processBatchConfirmations(requestId);
            
            long processingTime = System.currentTimeMillis() - startTime;

            logger.info("✅ Completed ticket confirmation batch processing - RequestId: {}, " +
                       "Total confirmations: {}, Processing time: {}ms",
                       requestId, totalConfirmations, processingTime);

        } catch (Exception e) {
            logger.error("❌ Error in ticket confirmation batch processing - RequestId: {}", requestId, e);
        }
    }

    /**
     * Process batch confirmations (simulated for microservice demo)
     */
    private int processBatchConfirmations(String requestId) {
        logger.info("🔄 Processing batch confirmations - RequestId: {}", requestId);
        
        // Simulate finding confirmations to process
        int confirmations = RANDOM.nextInt(5) + 1; // 1-5 confirmations
        
        for (int i = 0; i < confirmations; i++) {
            processSingleConfirmation(requestId, i + 1);
        }
        
        return confirmations;
    }

    /**
     * Process a single ticket confirmation
     */
    private void processSingleConfirmation(String requestId, int confirmationNumber) {
        try {
            // Simulate ticket confirmation data
            Long userId = (long) (RANDOM.nextInt(1000) + 1);
            Long trainId = (long) (RANDOM.nextInt(100) + 1);
            String pnrNumber = generatePNR();
            String previousStatus = RANDOM.nextBoolean() ? "RAC" : "WAITLIST";
            
            logger.info("🎫 Processing confirmation #{} for user: {}, PNR: {}, Previous status: {} - RequestId: {}",
                       confirmationNumber, userId, pnrNumber, previousStatus, requestId);

            // Create confirmation event
            BookingEvents.TicketConfirmationEvent event = new BookingEvents.TicketConfirmationEvent(
                requestId,
                userId,
                trainId,
                "TRAIN" + trainId,
                "Express Train " + trainId,
                LocalDateTime.now().plusDays(RANDOM.nextInt(7) + 1),
                pnrNumber,
                "S" + (RANDOM.nextInt(10) + 1),
                "C" + (RANDOM.nextInt(5) + 1),
                "AC" + (RANDOM.nextInt(3) + 1),
                BigDecimal.valueOf(500 + RANDOM.nextInt(1000)),
                previousStatus
            );

            // Set additional details
            event.setRacNumber(previousStatus.equals("RAC") ? RANDOM.nextInt(20) + 1 : null);
            event.setWaitlistNumber(previousStatus.equals("WAITLIST") ? RANDOM.nextInt(50) + 1 : null);
            event.setSourceStation("DEL");
            event.setDestinationStation("MUM");
            event.setDepartureTime("08:00");
            event.setArrivalTime("20:00");
            event.setPassengerName("Passenger " + userId);
            event.setPassengerEmail("passenger" + userId + "@example.com");
            event.setPassengerPhone("+91" + (9000000000L + userId));

            // Publish confirmation event
            publishConfirmationEvent(event, requestId);

            logger.info("✅ Successfully processed confirmation #{} for PNR: {} - RequestId: {}",
                       confirmationNumber, pnrNumber, requestId);

        } catch (Exception e) {
            logger.error("❌ Error processing confirmation #{} - RequestId: {}", confirmationNumber, requestId, e);
        }
    }

    /**
     * Publish confirmation event to Kafka
     */
    private void publishConfirmationEvent(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            kafkaTemplate.send("ticket-confirmation-events", event);

            logger.info("📤 Published confirmation event for user: {}, PNR: {} - RequestId: {}",
                       event.getUserId(), event.getPnrNumber(), requestId);

        } catch (Exception e) {
            logger.error("❌ Error publishing confirmation event - RequestId: {}", requestId, e);
        }
    }

    /**
     * Generate a unique PNR number
     */
    private String generatePNR() {
        return "PNR" + System.currentTimeMillis() + RANDOM.nextInt(1000);
    }

    /**
     * Manual trigger for testing purposes
     */
    public void triggerManualProcessing() {
        logger.info("🔧 Manual trigger for ticket confirmation batch processing");
        processTicketConfirmations();
    }

    /**
     * Get batch processing statistics
     */
    public String getBatchStatistics() {
        return String.format("""
            📊 Batch Processing Statistics:
            - Service: Booking Service (Port 8093)
            - Last Run: %s
            - Status: Active
            - Schedule: Every 30 minutes
            - Topic: ticket-confirmation-events
            """, LocalDateTime.now());
    }
}
