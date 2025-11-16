package com.irctc.payment.service;

import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.repository.RefundPolicyRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing refund policies
 */
@Service
public class RefundPolicyService {
    
    private static final Logger logger = LoggerFactory.getLogger(RefundPolicyService.class);
    
    @Autowired
    private RefundPolicyRepository refundPolicyRepository;
    
    /**
     * Get applicable refund policy based on cancellation time
     */
    public Optional<RefundPolicy> getApplicablePolicy(LocalDateTime cancellationTime, LocalDateTime departureTime) {
        List<RefundPolicy> policies = getActivePolicies();
        
        // Find the first policy that applies (they are ordered by priority)
        return policies.stream()
            .filter(policy -> policy.appliesTo(cancellationTime, departureTime))
            .findFirst();
    }
    
    /**
     * Get all active refund policies
     */
    public List<RefundPolicy> getActivePolicies() {
        if (TenantContext.hasTenant()) {
            return refundPolicyRepository.findActivePoliciesByTenantOrderedByPriority(
                TenantContext.getTenantId()
            );
        }
        return refundPolicyRepository.findActivePoliciesOrderedByPriority();
    }
    
    /**
     * Get refund policy by ID
     */
    public Optional<RefundPolicy> getPolicyById(Long id) {
        return refundPolicyRepository.findById(id);
    }
    
    /**
     * Create a new refund policy
     */
    @Transactional
    public RefundPolicy createPolicy(RefundPolicy policy) {
        if (TenantContext.hasTenant()) {
            policy.setTenantId(TenantContext.getTenantId());
        }
        return refundPolicyRepository.save(policy);
    }
    
    /**
     * Update refund policy
     */
    @Transactional
    public RefundPolicy updatePolicy(Long id, RefundPolicy updatedPolicy) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Refund policy not found: " + id));
        
        policy.setName(updatedPolicy.getName());
        policy.setDescription(updatedPolicy.getDescription());
        policy.setHoursBeforeDeparture(updatedPolicy.getHoursBeforeDeparture());
        policy.setRefundPercentage(updatedPolicy.getRefundPercentage());
        policy.setFixedCharges(updatedPolicy.getFixedCharges());
        policy.setGatewayFeeRefundable(updatedPolicy.getGatewayFeeRefundable());
        policy.setActive(updatedPolicy.getActive());
        policy.setPriority(updatedPolicy.getPriority());
        
        return refundPolicyRepository.save(policy);
    }
    
    /**
     * Delete refund policy
     */
    @Transactional
    public void deletePolicy(Long id) {
        refundPolicyRepository.deleteById(id);
    }
    
    /**
     * Calculate refund amount based on policy
     */
    public BigDecimal calculateRefundAmount(RefundPolicy policy, BigDecimal originalAmount, 
                                            BigDecimal gatewayFee) {
        if (policy == null) {
            // Default: no refund
            return BigDecimal.ZERO;
        }
        return policy.calculateRefundAmount(originalAmount, gatewayFee);
    }
    
    /**
     * Initialize default refund policies if none exist
     */
    @Transactional
    public void initializeDefaultPolicies() {
        if (refundPolicyRepository.count() == 0) {
            logger.info("Initializing default refund policies");
            
            // Full refund - 48 hours before
            RefundPolicy fullRefund48h = new RefundPolicy();
            fullRefund48h.setName("Full Refund - 48 hours before");
            fullRefund48h.setDescription("100% refund if cancelled 48+ hours before departure");
            fullRefund48h.setHoursBeforeDeparture(48);
            fullRefund48h.setRefundPercentage(BigDecimal.valueOf(100));
            fullRefund48h.setFixedCharges(BigDecimal.ZERO);
            fullRefund48h.setGatewayFeeRefundable(true);
            fullRefund48h.setActive(true);
            fullRefund48h.setPriority(1);
            refundPolicyRepository.save(fullRefund48h);
            
            // 75% refund - 24 hours before
            RefundPolicy partialRefund24h = new RefundPolicy();
            partialRefund24h.setName("Partial Refund - 24 hours before");
            partialRefund24h.setDescription("75% refund if cancelled 24-48 hours before departure");
            partialRefund24h.setHoursBeforeDeparture(24);
            partialRefund24h.setRefundPercentage(BigDecimal.valueOf(75));
            partialRefund24h.setFixedCharges(BigDecimal.valueOf(50));
            partialRefund24h.setGatewayFeeRefundable(false);
            partialRefund24h.setActive(true);
            partialRefund24h.setPriority(2);
            refundPolicyRepository.save(partialRefund24h);
            
            // 50% refund - 12 hours before
            RefundPolicy partialRefund12h = new RefundPolicy();
            partialRefund12h.setName("Partial Refund - 12 hours before");
            partialRefund12h.setDescription("50% refund if cancelled 12-24 hours before departure");
            partialRefund12h.setHoursBeforeDeparture(12);
            partialRefund12h.setRefundPercentage(BigDecimal.valueOf(50));
            partialRefund12h.setFixedCharges(BigDecimal.valueOf(100));
            partialRefund12h.setGatewayFeeRefundable(false);
            partialRefund12h.setActive(true);
            partialRefund12h.setPriority(3);
            refundPolicyRepository.save(partialRefund12h);
            
            // 25% refund - less than 12 hours
            RefundPolicy minimalRefund = new RefundPolicy();
            minimalRefund.setName("Minimal Refund - Less than 12 hours");
            minimalRefund.setDescription("25% refund if cancelled less than 12 hours before departure");
            minimalRefund.setHoursBeforeDeparture(0);
            minimalRefund.setRefundPercentage(BigDecimal.valueOf(25));
            minimalRefund.setFixedCharges(BigDecimal.valueOf(200));
            minimalRefund.setGatewayFeeRefundable(false);
            minimalRefund.setActive(true);
            minimalRefund.setPriority(4);
            refundPolicyRepository.save(minimalRefund);
            
            logger.info("✅ Default refund policies initialized");
        }
    }
}

