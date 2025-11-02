package com.irctc.payment.service;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.SimplePaymentRepository;
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

    public List<SimplePayment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public SimplePayment processPayment(SimplePayment payment) {
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public SimplePayment refundPayment(Long id) {
        SimplePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }
}
