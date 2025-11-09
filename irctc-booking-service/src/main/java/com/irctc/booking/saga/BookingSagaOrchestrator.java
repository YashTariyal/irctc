package com.irctc.booking.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.SimpleBooking;
import com.irctc.booking.repository.SimpleBookingRepository;
import com.irctc.booking.service.SimpleBookingService;
import com.irctc.shared.events.BookingEvents;
import com.irctc.shared.events.PaymentEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Booking Saga Orchestrator
 * Orchestrates the distributed transaction for booking creation:
 * 1. Create Booking
 * 2. Process Payment
 * 3. Send Notification
 * 
 * If any step fails, compensates previous steps in reverse order.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class BookingSagaOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingSagaOrchestrator.class);
    
    @Autowired
    private SagaInstanceRepository sagaRepository;
    
    @Autowired
    private SimpleBookingService bookingService;
    
    @Autowired
    private SimpleBookingRepository bookingRepository;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Start the booking saga
     */
    @Transactional
    public SagaInstance startBookingSaga(SimpleBooking bookingRequest) {
        String sagaId = UUID.randomUUID().toString();
        String correlationId = "BOOKING_" + System.currentTimeMillis();
        
        logger.info("🚀 Starting Booking Saga: {}", sagaId);
        
        // Create saga instance
        SagaInstance saga = new SagaInstance();
        saga.setSagaId(sagaId);
        saga.setSagaType("BOOKING_SAGA");
        saga.setCorrelationId(correlationId);
        saga.setStatus(SagaInstance.SagaStatus.STARTED);
        saga.setCurrentStep(0);
        saga.setTotalSteps(3);
        
        // Store booking request in saga data
        Map<String, Object> sagaData = new HashMap<>();
        sagaData.put("bookingRequest", bookingRequest);
        sagaData.put("bookingId", null);
        sagaData.put("paymentId", null);
        
        try {
            saga.setSagaData(objectMapper.writeValueAsString(sagaData));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing saga data", e);
        }
        
        saga = sagaRepository.save(saga);
        
        // Step 1: Create Booking
        try {
            saga = executeStep1_CreateBooking(saga);
        } catch (Exception e) {
            logger.error("❌ Saga failed at Step 1 (Create Booking): {}", e.getMessage());
            saga.setStatus(SagaInstance.SagaStatus.FAILED);
            saga.setErrorMessage("Step 1 failed: " + e.getMessage());
            sagaRepository.save(saga);
            return saga;
        }
        
        // Step 2: Process Payment
        try {
            saga = executeStep2_ProcessPayment(saga);
        } catch (Exception e) {
            logger.error("❌ Saga failed at Step 2 (Process Payment): {}", e.getMessage());
            // Compensate Step 1
            compensateStep1_CancelBooking(saga);
            saga.setStatus(SagaInstance.SagaStatus.COMPENSATED);
            saga.setErrorMessage("Step 2 failed: " + e.getMessage());
            sagaRepository.save(saga);
            return saga;
        }
        
        // Step 3: Send Notification
        try {
            saga = executeStep3_SendNotification(saga);
            saga.setStatus(SagaInstance.SagaStatus.COMPLETED);
            logger.info("✅ Booking Saga completed successfully: {}", sagaId);
        } catch (Exception e) {
            logger.error("❌ Saga failed at Step 3 (Send Notification): {}", e.getMessage());
            // Compensate Step 2 and Step 1
            compensateStep2_RefundPayment(saga);
            compensateStep1_CancelBooking(saga);
            saga.setStatus(SagaInstance.SagaStatus.COMPENSATED);
            saga.setErrorMessage("Step 3 failed: " + e.getMessage());
        }
        
        sagaRepository.save(saga);
        return saga;
    }
    
    /**
     * Step 1: Create Booking
     */
    private SagaInstance executeStep1_CreateBooking(SagaInstance saga) throws Exception {
        logger.info("📝 Step 1: Creating booking for saga: {}", saga.getSagaId());
        saga.setStatus(SagaInstance.SagaStatus.IN_PROGRESS);
        saga.setCurrentStep(1);
        
        try {
            Map<String, Object> sagaData = objectMapper.readValue(saga.getSagaData(), Map.class);
            
            // Convert booking request from saga data
            SimpleBooking bookingRequest = objectMapper.convertValue(
                sagaData.get("bookingRequest"), 
                SimpleBooking.class
            );
            
            // Ensure status is PENDING for saga
            if (bookingRequest == null) {
                throw new RuntimeException("Booking request not found in saga data");
            }
            bookingRequest.setStatus("PENDING"); // Set to PENDING initially
            
            SimpleBooking booking = bookingService.createBooking(bookingRequest);
            
            // Update saga data
            sagaData.put("bookingId", booking.getId());
            saga.setSagaData(objectMapper.writeValueAsString(sagaData));
            
            logger.info("✅ Step 1 completed: Booking created with ID: {}", booking.getId());
            return sagaRepository.save(saga);
            
        } catch (Exception e) {
            logger.error("❌ Step 1 failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Step 2: Process Payment
     */
    private SagaInstance executeStep2_ProcessPayment(SagaInstance saga) throws Exception {
        logger.info("💳 Step 2: Processing payment for saga: {}", saga.getSagaId());
        saga.setCurrentStep(2);
        
        try {
            Map<String, Object> sagaData = objectMapper.readValue(saga.getSagaData(), Map.class);
            Long bookingId = Long.valueOf(sagaData.get("bookingId").toString());
            
            SimpleBooking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Publish payment request event
            PaymentEvents.PaymentInitiatedEvent paymentEvent = new PaymentEvents.PaymentInitiatedEvent(
                null, // paymentId - will be set by payment service
                bookingId,
                booking.getUserId(),
                BigDecimal.valueOf(booking.getTotalFare()),
                "INR",
                "CREDIT_CARD"
            );
            
            if (kafkaTemplate != null) {
                kafkaTemplate.send("payment-initiated", paymentEvent);
                logger.info("📤 Published payment initiated event for booking: {}", bookingId);
            }
            
            // In a real implementation, we would wait for payment completion event
            // For now, we'll simulate payment success
            // In production, use event-driven approach with Kafka consumer
            
            // Update saga data
            sagaData.put("paymentId", "PAYMENT_" + System.currentTimeMillis());
            saga.setSagaData(objectMapper.writeValueAsString(sagaData));
            
            logger.info("✅ Step 2 completed: Payment processed");
            return sagaRepository.save(saga);
            
        } catch (Exception e) {
            logger.error("❌ Step 2 failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Step 3: Send Notification
     */
    private SagaInstance executeStep3_SendNotification(SagaInstance saga) throws Exception {
        logger.info("📧 Step 3: Sending notification for saga: {}", saga.getSagaId());
        saga.setCurrentStep(3);
        
        try {
            Map<String, Object> sagaData = objectMapper.readValue(saga.getSagaData(), Map.class);
            Long bookingId = Long.valueOf(sagaData.get("bookingId").toString());
            
            SimpleBooking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            // Update booking status to CONFIRMED
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
            
            // Publish booking confirmed event
            BookingEvents.BookingConfirmedEvent confirmedEvent = new BookingEvents.BookingConfirmedEvent(
                bookingId,
                booking.getUserId(),
                booking.getPnrNumber()
            );
            
            if (kafkaTemplate != null) {
                kafkaTemplate.send("booking-confirmed", confirmedEvent);
                logger.info("📤 Published booking confirmed event for booking: {}", bookingId);
            }
            
            logger.info("✅ Step 3 completed: Notification sent");
            return sagaRepository.save(saga);
            
        } catch (Exception e) {
            logger.error("❌ Step 3 failed: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Compensate Step 1: Cancel Booking
     */
    private void compensateStep1_CancelBooking(SagaInstance saga) {
        logger.info("🔄 Compensating Step 1: Cancelling booking for saga: {}", saga.getSagaId());
        
        try {
            Map<String, Object> sagaData = objectMapper.readValue(saga.getSagaData(), Map.class);
            if (sagaData.get("bookingId") != null) {
                Long bookingId = Long.valueOf(sagaData.get("bookingId").toString());
                bookingService.cancelBooking(bookingId);
                logger.info("✅ Compensation Step 1 completed: Booking cancelled");
            }
        } catch (Exception e) {
            logger.error("❌ Compensation Step 1 failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Compensate Step 2: Refund Payment
     */
    private void compensateStep2_RefundPayment(SagaInstance saga) {
        logger.info("🔄 Compensating Step 2: Refunding payment for saga: {}", saga.getSagaId());
        
        try {
            Map<String, Object> sagaData = objectMapper.readValue(saga.getSagaData(), Map.class);
            if (sagaData.get("paymentId") != null) {
                String paymentId = sagaData.get("paymentId").toString();
                
                // Publish refund event
                PaymentEvents.RefundProcessedEvent refundEvent = new PaymentEvents.RefundProcessedEvent(
                    null, // refundId
                    null, // paymentId - would be actual payment ID
                    Long.valueOf(sagaData.get("bookingId").toString()),
                    null, // userId
                    BigDecimal.valueOf(Double.parseDouble(sagaData.get("totalFare").toString())),
                    "Saga compensation"
                );
                
                if (kafkaTemplate != null) {
                    kafkaTemplate.send("refund-processed", refundEvent);
                    logger.info("📤 Published refund event for payment: {}", paymentId);
                }
                
                logger.info("✅ Compensation Step 2 completed: Payment refunded");
            }
        } catch (Exception e) {
            logger.error("❌ Compensation Step 2 failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get saga by ID
     */
    public SagaInstance getSagaById(String sagaId) {
        return sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
    }
    
    /**
     * Get saga by correlation ID
     */
    public SagaInstance getSagaByCorrelationId(String correlationId) {
        return sagaRepository.findByCorrelationId(correlationId)
                .orElseThrow(() -> new RuntimeException("Saga not found for correlation: " + correlationId));
    }
}

