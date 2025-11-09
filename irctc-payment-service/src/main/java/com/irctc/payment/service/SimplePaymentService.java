package com.irctc.payment.service;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.eventsourcing.PaymentEventStore;
import com.irctc.payment.repository.SimplePaymentRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimplePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(SimplePaymentService.class);

    @Autowired
    private SimplePaymentRepository paymentRepository;
    
    @Autowired(required = false)
    private PaymentEventStore eventStore;

    public List<SimplePayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<SimplePayment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<SimplePayment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    @Bulkhead(name = "payment-query", type = Bulkhead.Type.SEMAPHORE)
    public List<SimplePayment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Bulkhead(name = "payment-processing", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "payment-processing")
    public SimplePayment processPayment(SimplePayment payment) {
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setCreatedAt(LocalDateTime.now());
        SimplePayment saved = paymentRepository.save(payment);
        
        // Store event in event store (Event Sourcing)
        if (eventStore != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("paymentId", saved.getId());
            eventData.put("bookingId", saved.getBookingId());
            eventData.put("amount", saved.getAmount());
            eventData.put("currency", saved.getCurrency());
            eventData.put("paymentMethod", saved.getPaymentMethod());
            eventData.put("transactionId", saved.getTransactionId());
            eventData.put("status", saved.getStatus());
            eventData.put("paymentTime", saved.getPaymentTime());
            
            eventStore.appendEvent(
                saved.getId().toString(),
                "PAYMENT_COMPLETED",
                eventData,
                UUID.randomUUID().toString(),
                saved.getBookingId().toString() // Using bookingId as userId for correlation
            );
            logger.info("📝 Payment event stored: PAYMENT_COMPLETED for payment: {}", saved.getId());
        }
        
        return saved;
    }

    @Bulkhead(name = "payment-refund", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "payment-refund")
    public SimplePayment refundPayment(Long id) {
        SimplePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        payment.setStatus("REFUNDED");
        SimplePayment saved = paymentRepository.save(payment);
        
        // Store event in event store (Event Sourcing)
        if (eventStore != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("paymentId", saved.getId());
            eventData.put("bookingId", saved.getBookingId());
            eventData.put("refundAmount", saved.getAmount());
            eventData.put("refundTime", LocalDateTime.now());
            eventData.put("status", saved.getStatus());
            
            eventStore.appendEvent(
                saved.getId().toString(),
                "PAYMENT_REFUNDED",
                eventData,
                UUID.randomUUID().toString(),
                saved.getBookingId().toString()
            );
            logger.info("📝 Payment event stored: PAYMENT_REFUNDED for payment: {}", saved.getId());
        }
        
        return saved;
    }
}
