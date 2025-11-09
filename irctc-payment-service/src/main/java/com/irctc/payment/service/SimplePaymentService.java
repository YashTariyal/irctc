package com.irctc.payment.service;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.SimplePaymentRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimplePaymentService {

    @Autowired
    private SimplePaymentRepository paymentRepository;

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
        return paymentRepository.save(payment);
    }

    @Bulkhead(name = "payment-refund", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "payment-refund")
    public SimplePayment refundPayment(Long id) {
        SimplePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }
}
