package com.irctc.booking.service;

import com.irctc.shared.events.BookingEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer.SpanInScope;

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
    private OutboxEventService outboxEventService;

    @Autowired
    private SimpleBookingService bookingService;

    @Autowired(required = false)
    private Tracer tracer;

    /**
     * Main scheduled job that runs every 30 minutes to process ticket confirmations
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes
    @SchedulerLock(name = "ticketConfirmationBatch", lockAtLeastFor = "PT30S", lockAtMostFor = "PT4M")
    public void processTicketConfirmations() {
        Span span = startSpan("booking.batch.process");
        try (SpanInScope ignored = span != null && tracer != null ? tracer.withSpan(span) : null) {
            String requestId = UUID.randomUUID().toString();
            long startTime = System.currentTimeMillis();

            String traceId = tracer != null && tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "no-trace";
            logger.info("🎫 Starting ticket confirmation batch - RequestId: {}, traceId: {}", requestId, traceId);

            try {
                int totalConfirmations = processBatchConfirmations(requestId);
                long processingTime = System.currentTimeMillis() - startTime;
                logger.info("✅ Completed ticket confirmation batch - RequestId: {}, Total: {}, Time: {}ms, traceId: {}",
                        requestId, totalConfirmations, processingTime, traceId);
            } catch (Exception e) {
                logger.error("❌ Error in ticket confirmation batch - RequestId: {}", requestId, e);
            }
        } finally {
            endSpan(span);
        }
    }

    private Span startSpan(String name) {
        if (tracer == null) return null;
        return tracer.nextSpan().name(name).start();
    }

    private void endSpan(Span span) {
        if (span != null) span.end();
    }

    /**
     * Process batch confirmations (simulated for microservice demo)
     */
    private int processBatchConfirmations(String requestId) {
        logger.info("🔄 Processing batch confirmations - RequestId: {}", requestId);
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
            Long userId = (long) (RANDOM.nextInt(1000) + 1);
            Long trainId = (long) (RANDOM.nextInt(100) + 1);
            String pnrNumber = generatePNR();
            String previousStatus = RANDOM.nextBoolean() ? "RAC" : "WAITLIST";
            logger.info("🎫 Processing confirmation #{} for user: {}, PNR: {}, Previous: {} - RequestId: {}",
                       confirmationNumber, userId, pnrNumber, previousStatus, requestId);

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

            event.setRacNumber(previousStatus.equals("RAC") ? RANDOM.nextInt(20) + 1 : null);
            event.setWaitlistNumber(previousStatus.equals("WAITLIST") ? RANDOM.nextInt(50) + 1 : null);
            event.setSourceStation("DEL");
            event.setDestinationStation("MUM");
            event.setDepartureTime("08:00");
            event.setArrivalTime("20:00");
            event.setPassengerName("Passenger " + userId);
            event.setPassengerEmail("passenger" + userId + "@example.com");
            event.setPassengerPhone("+91" + (9000000000L + userId));

            publishConfirmationEvent(event, requestId);
            logger.info("✅ Successfully processed confirmation #{} for PNR: {} - RequestId: {}",
                       confirmationNumber, pnrNumber, requestId);
        } catch (Exception e) {
            logger.error("❌ Error processing confirmation #{} - RequestId: {}", confirmationNumber, requestId, e);
        }
    }

    /**
     * Save confirmation event to outbox table (will be published by OutboxEventPublisher)
     */
    private void publishConfirmationEvent(BookingEvents.TicketConfirmationEvent event, String requestId) {
        try {
            outboxEventService.saveEvent("ticket-confirmation-events", event);
            String traceId = tracer != null && tracer.currentSpan() != null ? tracer.currentSpan().context().traceId() : "no-trace";
            logger.info("💾 Saved confirmation to outbox for user: {}, PNR: {} - RequestId: {}, traceId: {}",
                       event.getUserId(), event.getPnrNumber(), requestId, traceId);
        } catch (Exception e) {
            logger.error("❌ Error saving confirmation event to outbox - RequestId: {}", requestId, e);
        }
    }

    private String generatePNR() {
        return "PNR" + System.currentTimeMillis() + RANDOM.nextInt(1000);
    }

    public void triggerManualProcessing() {
        logger.info("🔧 Manual trigger for ticket confirmation batch processing");
        processTicketConfirmations();
    }

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
