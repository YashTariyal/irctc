package com.irctc.payment.controller;

import com.irctc.payment.dto.EmiPaymentRequest;
import com.irctc.payment.dto.EmiPaymentResponse;
import com.irctc.payment.dto.PaymentPlanRequest;
import com.irctc.payment.dto.PaymentPlanResponse;
import com.irctc.payment.service.PaymentPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments/plans")
public class PaymentPlanController {

    private final PaymentPlanService paymentPlanService;

    public PaymentPlanController(PaymentPlanService paymentPlanService) {
        this.paymentPlanService = paymentPlanService;
    }

    @PostMapping
    public ResponseEntity<PaymentPlanResponse> createPlan(@RequestBody PaymentPlanRequest request) {
        return ResponseEntity.ok(paymentPlanService.createPlan(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentPlanResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(paymentPlanService.getPlan(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentPlanResponse>> getPlansForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentPlanService.getPlansByUser(userId));
    }

    @PostMapping("/emi/{emiId}/pay")
    public ResponseEntity<EmiPaymentResponse> payInstallment(@PathVariable Long emiId,
                                                             @RequestBody EmiPaymentRequest request) {
        return ResponseEntity.ok(paymentPlanService.recordEmiPayment(emiId, request.getAmount()));
    }
}

