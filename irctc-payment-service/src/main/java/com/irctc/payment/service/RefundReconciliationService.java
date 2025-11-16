package com.irctc.payment.service;

import com.irctc.payment.entity.RefundStatus;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.repository.RefundStatusRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for automatic refund reconciliation with payment gateways
 */
@Service
public class RefundReconciliationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundReconciliationService.class);
    
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    
    @Autowired(required = false)
    private GatewaySelectorService gatewaySelectorService;
    
    /**
     * Reconcile a specific refund with the gateway
     */
    @Transactional
    public RefundStatus reconcileRefund(Long refundStatusId) {
        RefundStatus refundStatus = refundStatusRepository.findById(refundStatusId)
            .orElseThrow(() -> new RuntimeException("Refund status not found: " + refundStatusId));
        
        return reconcileRefund(refundStatus);
    }
    
    /**
     * Reconcile refund by refund ID
     */
    @Transactional
    public RefundStatus reconcileRefundByRefundId(String refundId) {
        RefundStatus refundStatus = refundStatusRepository.findByRefundId(refundId)
            .orElseThrow(() -> new RuntimeException("Refund not found: " + refundId));
        
        return reconcileRefund(refundStatus);
    }
    
    /**
     * Reconcile a refund status with gateway
     */
    @Transactional
    public RefundStatus reconcileRefund(RefundStatus refundStatus) {
        logger.info("🔄 Reconciling refund: {} with gateway: {}", 
            refundStatus.getRefundId(), refundStatus.getGatewayName());
        
        if (refundStatus.getGatewayName() == null || 
            refundStatus.getGatewayName().equals("INTERNAL") ||
            refundStatus.getGatewayName().equals("WALLET")) {
            // No reconciliation needed for internal/wallet refunds
            refundStatus.setReconciliationStatus("RECONCILED");
            refundStatus.setReconciledAt(LocalDateTime.now());
            return refundStatusRepository.save(refundStatus);
        }
        
        if (gatewaySelectorService == null) {
            logger.warn("Gateway selector service not available, skipping reconciliation");
            return refundStatus;
        }
        
        try {
            PaymentGateway gateway = gatewaySelectorService.getGatewayByName(refundStatus.getGatewayName());
            
            // Verify refund with gateway (this would typically call gateway API)
            // For now, we'll simulate the verification
            boolean verified = verifyRefundWithGateway(gateway, refundStatus);
            
            if (verified) {
                refundStatus.setReconciliationStatus("RECONCILED");
                refundStatus.setReconciledAt(LocalDateTime.now());
                logger.info("✅ Refund reconciled successfully: {}", refundStatus.getRefundId());
            } else {
                refundStatus.setReconciliationStatus("MISMATCH");
                logger.warn("⚠️ Refund reconciliation mismatch: {}", refundStatus.getRefundId());
            }
        } catch (Exception e) {
            logger.error("Error reconciling refund: {}", e.getMessage(), e);
            refundStatus.setReconciliationStatus("MISMATCH");
        }
        
        return refundStatusRepository.save(refundStatus);
    }
    
    /**
     * Verify refund with gateway (placeholder - would call actual gateway API)
     */
    private boolean verifyRefundWithGateway(PaymentGateway gateway, RefundStatus refundStatus) {
        // In a real implementation, this would:
        // 1. Call gateway API to verify refund status
        // 2. Compare refund amount
        // 3. Compare refund status
        // 4. Return true if everything matches
        
        // For now, we'll assume successful refunds are verified
        if ("COMPLETED".equals(refundStatus.getStatus()) && 
            refundStatus.getGatewayRefundId() != null) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Automatically reconcile pending refunds (scheduled job)
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void reconcilePendingRefunds() {
        logger.info("🔄 Starting automatic refund reconciliation");
        
        List<RefundStatus> pendingRefunds = refundStatusRepository.findByReconciliationStatus("PENDING");
        
        logger.info("Found {} pending refunds to reconcile", pendingRefunds.size());
        
        for (RefundStatus refundStatus : pendingRefunds) {
            try {
                reconcileRefund(refundStatus);
            } catch (Exception e) {
                logger.error("Error reconciling refund {}: {}", 
                    refundStatus.getRefundId(), e.getMessage(), e);
            }
        }
        
        logger.info("✅ Automatic refund reconciliation completed");
    }
    
    /**
     * Get all refunds with reconciliation status
     */
    public List<RefundStatus> getRefundsByReconciliationStatus(String reconciliationStatus) {
        return refundStatusRepository.findByReconciliationStatus(reconciliationStatus);
    }
}

