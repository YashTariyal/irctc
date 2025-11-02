package com.irctc.payment.controller;

import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.service.SimplePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Map;

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
        SimplePayment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<SimplePayment> getPaymentByTransactionId(@PathVariable String transactionId) {
        SimplePayment payment = paymentService.getPaymentByTransactionId(transactionId)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", transactionId));
        return ResponseEntity.ok(payment);
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

    // ===== ADVANCED PAYMENT APIs =====
    
    @PostMapping("/process")
    public ResponseEntity<SimplePayment> processPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            SimplePayment payment = new SimplePayment();
            payment.setBookingId(Long.valueOf(paymentData.get("bookingId").toString()));
            payment.setAmount(Double.valueOf(paymentData.get("amount").toString()));
            payment.setCurrency(paymentData.getOrDefault("currency", "INR").toString());
            payment.setPaymentMethod(paymentData.get("paymentMethod").toString());
            payment.setStatus("PENDING");
            
            SimplePayment processedPayment = paymentService.processPayment(payment);
            return ResponseEntity.ok(processedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<SimplePayment> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        SimplePayment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        payment.setStatus(status);
        SimplePayment updatedPayment = paymentService.refundPayment(id); // Reuse existing method
        return ResponseEntity.ok(updatedPayment);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SimplePayment>> getPaymentsByStatus(@PathVariable String status) {
        List<SimplePayment> payments = paymentService.getAllPayments().stream()
                .filter(payment -> status.equalsIgnoreCase(payment.getStatus()))
                .toList();
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<List<SimplePayment>> getPaymentsByMethod(@PathVariable String paymentMethod) {
        List<SimplePayment> payments = paymentService.getAllPayments().stream()
                .filter(payment -> paymentMethod.equalsIgnoreCase(payment.getPaymentMethod()))
                .toList();
        return ResponseEntity.ok(payments);
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<List<SimplePayment>> getPaymentsByDate(@PathVariable String date) {
        List<SimplePayment> payments = paymentService.getAllPayments().stream()
                .filter(payment -> payment.getPaymentTime().toString().contains(date))
                .toList();
        return ResponseEntity.ok(payments);
    }
    
    @PostMapping("/refund/process")
    public ResponseEntity<SimplePayment> processRefund(@RequestBody Map<String, Object> refundData) {
        try {
            Long paymentId = Long.valueOf(refundData.get("paymentId").toString());
            Double refundAmount = Double.valueOf(refundData.get("refundAmount").toString());
            
            SimplePayment payment = paymentService.getPaymentById(paymentId)
                    .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", paymentId));
            
            if (payment.getAmount() < refundAmount) {
                return ResponseEntity.badRequest().body(null);
            }
            
            payment.setStatus("REFUNDED");
            SimplePayment refundedPayment = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(refundedPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/refunds")
    public ResponseEntity<List<SimplePayment>> getRefundedPayments() {
        List<SimplePayment> refunds = paymentService.getAllPayments().stream()
                .filter(payment -> "REFUNDED".equals(payment.getStatus()))
                .toList();
        return ResponseEntity.ok(refunds);
    }
    
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<SimplePayment>> getPaymentHistoryByUser(@PathVariable Long userId) {
        // For now, return all payments (in real implementation, filter by user)
        List<SimplePayment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}
