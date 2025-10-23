package com.irctc.payment.controller;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.service.SimplePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class SimplePaymentController {

    @Autowired
    private SimplePaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<SimplePayment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimplePayment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<SimplePayment> getPaymentByTransactionId(@PathVariable String transactionId) {
        return paymentService.getPaymentByTransactionId(transactionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<SimplePayment>> getPaymentsByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId));
    }

    @PostMapping
    public ResponseEntity<SimplePayment> processPayment(@RequestBody SimplePayment payment) {
        SimplePayment newPayment = paymentService.processPayment(payment);
        return ResponseEntity.ok(newPayment);
    }

    @PutMapping("/refund/{id}")
    public ResponseEntity<SimplePayment> refundPayment(@PathVariable Long id) {
        SimplePayment refundedPayment = paymentService.refundPayment(id);
        return ResponseEntity.ok(refundedPayment);
    }
}
