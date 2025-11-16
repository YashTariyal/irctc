package com.irctc.payment.controller;

import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.entity.RefundStatus;
import com.irctc.payment.service.AutomatedRefundService;
import com.irctc.payment.service.RefundPolicyService;
import com.irctc.payment.service.RefundReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for automated refund processing APIs
 */
@RestController
@RequestMapping("/api/payments")
public class RefundController {
    
    @Autowired
    private AutomatedRefundService automatedRefundService;
    
    @Autowired
    private RefundPolicyService refundPolicyService;
    
    @Autowired
    private RefundReconciliationService refundReconciliationService;
    
    /**
     * POST /api/payments/{id}/initiate-refund
     * Initiate refund for a payment
     */
    @PostMapping("/{id}/initiate-refund")
    public ResponseEntity<RefundStatus> initiateRefund(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> refundData) {
        
        BigDecimal refundAmount = refundData != null && refundData.containsKey("refundAmount") ?
            new BigDecimal(refundData.get("refundAmount").toString()) : null;
        
        String reason = refundData != null && refundData.containsKey("reason") ?
            refundData.get("reason").toString() : "Manual refund";
        
        LocalDateTime cancellationTime = null;
        LocalDateTime departureTime = null;
        
        if (refundData != null) {
            if (refundData.containsKey("cancellationTime")) {
                cancellationTime = LocalDateTime.parse(refundData.get("cancellationTime").toString());
            }
            if (refundData.containsKey("departureTime")) {
                departureTime = LocalDateTime.parse(refundData.get("departureTime").toString());
            }
        }
        
        RefundStatus refundStatus = automatedRefundService.initiateRefund(
            id, refundAmount, reason, cancellationTime, departureTime
        );
        
        return ResponseEntity.ok(refundStatus);
    }
    
    /**
     * GET /api/payments/{id}/refund-status
     * Get refund status for a payment
     */
    @GetMapping("/{id}/refund-status")
    public ResponseEntity<List<RefundStatus>> getRefundStatus(@PathVariable Long id) {
        List<RefundStatus> refundStatuses = automatedRefundService.getRefundStatusByPaymentId(id);
        return ResponseEntity.ok(refundStatuses);
    }
    
    /**
     * GET /api/payments/refund-status/{refundId}
     * Get refund status by refund ID
     */
    @GetMapping("/refund-status/{refundId}")
    public ResponseEntity<RefundStatus> getRefundStatusByRefundId(@PathVariable String refundId) {
        Optional<RefundStatus> refundStatus = automatedRefundService.getRefundStatusByRefundId(refundId);
        return refundStatus.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/payments/refund-policy?cancellationTime={time}&departureTime={time}
     * Get applicable refund policy based on cancellation time
     */
    @GetMapping("/refund-policy")
    public ResponseEntity<RefundPolicy> getRefundPolicy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cancellationTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime) {
        
        Optional<RefundPolicy> policy = refundPolicyService.getApplicablePolicy(
            cancellationTime, departureTime
        );
        
        return policy.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/payments/{id}/reconcile-refund
     * Reconcile refund with payment gateway
     */
    @PostMapping("/{id}/reconcile-refund")
    public ResponseEntity<RefundStatus> reconcileRefund(@PathVariable Long id) {
        RefundStatus refundStatus = refundReconciliationService.reconcileRefund(id);
        return ResponseEntity.ok(refundStatus);
    }
    
    /**
     * POST /api/payments/reconcile-refund/{refundId}
     * Reconcile refund by refund ID
     */
    @PostMapping("/reconcile-refund/{refundId}")
    public ResponseEntity<RefundStatus> reconcileRefundByRefundId(@PathVariable String refundId) {
        RefundStatus refundStatus = refundReconciliationService.reconcileRefundByRefundId(refundId);
        return ResponseEntity.ok(refundStatus);
    }
    
    /**
     * POST /api/payments/{id}/partial-refund
     * Initiate partial refund
     */
    @PostMapping("/{id}/partial-refund")
    public ResponseEntity<RefundStatus> initiatePartialRefund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> refundData) {
        
        BigDecimal refundAmount = new BigDecimal(refundData.get("refundAmount").toString());
        String reason = refundData.containsKey("reason") ?
            refundData.get("reason").toString() : "Partial refund";
        
        @SuppressWarnings("unchecked")
        List<Long> passengerIds = refundData.containsKey("passengerIds") ?
            (List<Long>) refundData.get("passengerIds") : null;
        
        RefundStatus refundStatus = automatedRefundService.initiatePartialRefund(
            id, refundAmount, reason, passengerIds
        );
        
        return ResponseEntity.ok(refundStatus);
    }
    
    /**
     * GET /api/payments/refund-policies
     * Get all active refund policies
     */
    @GetMapping("/refund-policies")
    public ResponseEntity<List<RefundPolicy>> getRefundPolicies() {
        List<RefundPolicy> policies = refundPolicyService.getActivePolicies();
        return ResponseEntity.ok(policies);
    }
    
    /**
     * POST /api/payments/refund-policies
     * Create a new refund policy
     */
    @PostMapping("/refund-policies")
    public ResponseEntity<RefundPolicy> createRefundPolicy(@RequestBody RefundPolicy policy) {
        RefundPolicy createdPolicy = refundPolicyService.createPolicy(policy);
        return ResponseEntity.ok(createdPolicy);
    }
    
    /**
     * PUT /api/payments/refund-policies/{id}
     * Update refund policy
     */
    @PutMapping("/refund-policies/{id}")
    public ResponseEntity<RefundPolicy> updateRefundPolicy(
            @PathVariable Long id,
            @RequestBody RefundPolicy policy) {
        RefundPolicy updatedPolicy = refundPolicyService.updatePolicy(id, policy);
        return ResponseEntity.ok(updatedPolicy);
    }
    
    /**
     * DELETE /api/payments/refund-policies/{id}
     * Delete refund policy
     */
    @DeleteMapping("/refund-policies/{id}")
    public ResponseEntity<Void> deleteRefundPolicy(@PathVariable Long id) {
        refundPolicyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/payments/reconciliation-status/{status}
     * Get refunds by reconciliation status
     */
    @GetMapping("/reconciliation-status/{status}")
    public ResponseEntity<List<RefundStatus>> getRefundsByReconciliationStatus(
            @PathVariable String status) {
        List<RefundStatus> refunds = refundReconciliationService.getRefundsByReconciliationStatus(status);
        return ResponseEntity.ok(refunds);
    }
}

