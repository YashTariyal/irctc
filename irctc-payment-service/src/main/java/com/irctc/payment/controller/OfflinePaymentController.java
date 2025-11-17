package com.irctc.payment.controller;

import com.irctc.payment.dto.OfflinePaymentRequest;
import com.irctc.payment.dto.OfflinePaymentResponse;
import com.irctc.payment.entity.OfflinePaymentIntent;
import com.irctc.payment.service.OfflinePaymentQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/offline")
public class OfflinePaymentController {

    private final OfflinePaymentQueueService queueService;

    public OfflinePaymentController(OfflinePaymentQueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<OfflinePaymentResponse> queuePayment(@RequestBody OfflinePaymentRequest request) {
        OfflinePaymentIntent intent = queueService.queue(request);
        return ResponseEntity.ok(map(intent));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OfflinePaymentResponse>> pendingForUser(@PathVariable Long userId) {
        List<OfflinePaymentResponse> responses = queueService.getPendingForUser(userId).stream()
            .map(this::map)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{intentId}/sync")
    public ResponseEntity<OfflinePaymentResponse> syncIntent(@PathVariable Long intentId) {
        OfflinePaymentIntent intent = queueService.processIntent(intentId);
        return ResponseEntity.ok(map(intent));
    }

    @PostMapping("/sync")
    public ResponseEntity<Integer> syncBatch() {
        return ResponseEntity.ok(queueService.processPendingBatch());
    }

    private OfflinePaymentResponse map(OfflinePaymentIntent intent) {
        OfflinePaymentResponse response = new OfflinePaymentResponse();
        response.setId(intent.getId());
        response.setUserId(intent.getUserId());
        response.setBookingId(intent.getBookingId());
        response.setAmount(intent.getAmount());
        response.setCurrency(intent.getCurrency());
        response.setPaymentMethod(intent.getPaymentMethod());
        response.setStatus(intent.getStatus());
        response.setFailureReason(intent.getFailureReason());
        response.setProcessedPaymentId(intent.getProcessedPaymentId());
        response.setQueuedAt(intent.getQueuedAt());
        response.setProcessedAt(intent.getProcessedAt());
        return response;
    }
}

