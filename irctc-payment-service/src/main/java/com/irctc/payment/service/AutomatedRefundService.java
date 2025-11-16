package com.irctc.payment.service;

import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;
import com.irctc.payment.entity.RefundPolicy;
import com.irctc.payment.entity.RefundStatus;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.repository.RefundStatusRepository;
import com.irctc.payment.repository.SimplePaymentRepository;
import com.irctc.payment.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for automated refund processing
 */
@Service
public class AutomatedRefundService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutomatedRefundService.class);
    
    @Autowired
    private SimplePaymentRepository paymentRepository;
    
    @Autowired
    private RefundStatusRepository refundStatusRepository;
    
    @Autowired
    private RefundPolicyService refundPolicyService;
    
    @Autowired(required = false)
    private GatewaySelectorService gatewaySelectorService;
    
    @Autowired(required = false)
    private WalletService walletService;
    
    /**
     * Automatically initiate refund when booking is cancelled
     */
    @Async("refundExecutor")
    @Transactional
    public RefundStatus autoRefundOnCancellation(Long bookingId, LocalDateTime cancellationTime, 
                                                 LocalDateTime departureTime, String reason) {
        logger.info("🔄 Auto-refund initiated for booking: {}, cancellation time: {}", 
            bookingId, cancellationTime);
        
        // Find all payments for this booking
        List<SimplePayment> payments = paymentRepository.findByBookingId(bookingId);
        
        if (payments.isEmpty()) {
            logger.warn("No payments found for booking: {}", bookingId);
            return null;
        }
        
        // Process refund for the latest payment (or all payments if needed)
        SimplePayment payment = payments.get(payments.size() - 1);
        
        // Get applicable refund policy
        Optional<RefundPolicy> policyOpt = refundPolicyService.getApplicablePolicy(
            cancellationTime, departureTime
        );
        
        RefundPolicy policy = policyOpt.orElse(null);
        BigDecimal gatewayFee = payment.getGatewayFee() != null ? 
            BigDecimal.valueOf(payment.getGatewayFee()) : BigDecimal.ZERO;
        BigDecimal originalAmount = BigDecimal.valueOf(payment.getAmount());
        
        // Calculate refund amount based on policy
        BigDecimal refundAmount = refundPolicyService.calculateRefundAmount(
            policy, originalAmount, gatewayFee
        );
        
        // Ensure refundAmount is not null
        if (refundAmount == null) {
            refundAmount = BigDecimal.ZERO;
        }
        
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("No refund applicable for booking: {} based on policy", bookingId);
            return createRefundStatus(payment, refundAmount, "FAILED", 
                "No refund applicable based on policy", policy, cancellationTime);
        }
        
        // Create refund status record
        RefundStatus refundStatus = createRefundStatus(payment, refundAmount, "INITIATED", 
            reason, policy, cancellationTime);
        refundStatus = refundStatusRepository.save(refundStatus);
        
        // Process the refund
        try {
            refundStatus.setStatus("PROCESSING");
            refundStatus = refundStatusRepository.save(refundStatus);
            
            RefundResponse refundResponse = processRefund(payment, refundAmount, reason);
            
            if ("SUCCESS".equals(refundResponse.getStatus())) {
                refundStatus.setStatus("COMPLETED");
                refundStatus.setRefundId(refundResponse.getRefundId());
                refundStatus.setGatewayRefundId(refundResponse.getGatewayRefundId());
                refundStatus.setReconciliationStatus("PENDING");
                logger.info("✅ Auto-refund completed for booking: {}, refund ID: {}", 
                    bookingId, refundStatus.getRefundId());
            } else {
                refundStatus.setStatus("FAILED");
                refundStatus.setFailureReason(refundResponse.getFailureReason());
                logger.error("❌ Auto-refund failed for booking: {}, reason: {}", 
                    bookingId, refundResponse.getFailureReason());
            }
        } catch (Exception e) {
            logger.error("Error processing auto-refund for booking: {}", bookingId, e);
            refundStatus.setStatus("FAILED");
            refundStatus.setFailureReason("Error: " + e.getMessage());
        }
        
        return refundStatusRepository.save(refundStatus);
    }
    
    /**
     * Initiate refund manually
     */
    @Transactional
    public RefundStatus initiateRefund(Long paymentId, BigDecimal refundAmount, String reason,
                                      LocalDateTime cancellationTime, LocalDateTime departureTime) {
        SimplePayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        // Get applicable refund policy if cancellation time provided
        RefundPolicy policy = null;
        if (cancellationTime != null && departureTime != null) {
            Optional<RefundPolicy> policyOpt = refundPolicyService.getApplicablePolicy(
                cancellationTime, departureTime
            );
            policy = policyOpt.orElse(null);
            
            // Recalculate refund amount based on policy if not explicitly provided
            if (refundAmount == null) {
                BigDecimal gatewayFee = payment.getGatewayFee() != null ? 
                    BigDecimal.valueOf(payment.getGatewayFee()) : BigDecimal.ZERO;
                BigDecimal originalAmount = BigDecimal.valueOf(payment.getAmount());
                refundAmount = refundPolicyService.calculateRefundAmount(
                    policy, originalAmount, gatewayFee
                );
            }
        } else if (refundAmount == null) {
            // Full refund if no policy constraints
            refundAmount = BigDecimal.valueOf(payment.getAmount());
        }
        
        // Validate refund amount
        if (refundAmount.compareTo(BigDecimal.valueOf(payment.getAmount())) > 0) {
            throw new RuntimeException("Refund amount cannot exceed payment amount");
        }
        
        // Create refund status
        RefundStatus refundStatus = createRefundStatus(payment, refundAmount, "INITIATED", 
            reason, policy, cancellationTime);
        refundStatus = refundStatusRepository.save(refundStatus);
        
        // Process refund asynchronously
        processRefundAsync(refundStatus, payment, refundAmount, reason);
        
        return refundStatus;
    }
    
    /**
     * Process partial refund (e.g., one passenger from group booking)
     */
    @Transactional
    public RefundStatus initiatePartialRefund(Long paymentId, BigDecimal refundAmount, 
                                            String reason, List<Long> passengerIds) {
        SimplePayment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        
        // Validate partial refund amount
        if (refundAmount.compareTo(BigDecimal.valueOf(payment.getAmount())) > 0) {
            throw new RuntimeException("Partial refund amount cannot exceed payment amount");
        }
        
        // Create refund status for partial refund
        RefundStatus refundStatus = createRefundStatus(payment, refundAmount, "INITIATED", 
            reason, null, LocalDateTime.now());
        refundStatus.setStatus("PARTIALLY_REFUNDED");
        refundStatus = refundStatusRepository.save(refundStatus);
        
        // Process refund
        processRefundAsync(refundStatus, payment, refundAmount, reason);
        
        return refundStatus;
    }
    
    /**
     * Process refund through gateway or wallet
     */
    private RefundResponse processRefund(SimplePayment payment, BigDecimal refundAmount, String reason) {
        // Process wallet refund
        if ("WALLET".equals(payment.getGatewayName()) && walletService != null) {
            try {
                String userId = TenantContext.hasTenant() ? 
                    TenantContext.getTenantId() : "user-" + payment.getBookingId();
                
                walletService.addRefund(
                    userId,
                    refundAmount,
                    payment.getTransactionId(),
                    reason
                );
                
                RefundResponse response = new RefundResponse();
                response.setRefundId(UUID.randomUUID().toString());
                response.setGatewayName("WALLET");
                response.setOriginalTransactionId(payment.getTransactionId());
                response.setRefundAmount(refundAmount);
                response.setStatus("SUCCESS");
                response.setRefundTime(LocalDateTime.now());
                return response;
            } catch (Exception e) {
                logger.error("Error processing wallet refund: {}", e.getMessage(), e);
                RefundResponse response = new RefundResponse();
                response.setStatus("FAILED");
                response.setFailureReason("Wallet refund error: " + e.getMessage());
                return response;
            }
        }
        
        // Process refund through gateway
        if (payment.getGatewayName() != null && !payment.getGatewayName().equals("INTERNAL") 
            && !payment.getGatewayName().equals("FALLBACK") && gatewaySelectorService != null) {
            try {
                PaymentGateway gateway = gatewaySelectorService.getGatewayByName(payment.getGatewayName());
                
                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setOriginalTransactionId(payment.getTransactionId());
                refundRequest.setGatewayTransactionId(payment.getGatewayTransactionId());
                refundRequest.setRefundAmount(refundAmount);
                refundRequest.setReason(reason);
                refundRequest.setGatewayName(payment.getGatewayName());
                
                return gateway.processRefund(refundRequest);
            } catch (Exception e) {
                logger.error("Error processing gateway refund: {}", e.getMessage(), e);
                RefundResponse response = new RefundResponse();
                response.setStatus("FAILED");
                response.setFailureReason("Gateway refund error: " + e.getMessage());
                return response;
            }
        }
        
        // Simple refund without gateway
        RefundResponse response = new RefundResponse();
        response.setRefundId(UUID.randomUUID().toString());
        response.setGatewayName("INTERNAL");
        response.setOriginalTransactionId(payment.getTransactionId());
        response.setRefundAmount(refundAmount);
        response.setStatus("SUCCESS");
        response.setRefundTime(LocalDateTime.now());
        return response;
    }
    
    /**
     * Process refund asynchronously
     */
    @Async("refundExecutor")
    private void processRefundAsync(RefundStatus refundStatus, SimplePayment payment, 
                                   BigDecimal refundAmount, String reason) {
        try {
            refundStatus.setStatus("PROCESSING");
            refundStatus = refundStatusRepository.save(refundStatus);
            
            RefundResponse refundResponse = processRefund(payment, refundAmount, reason);
            
            if ("SUCCESS".equals(refundResponse.getStatus())) {
                refundStatus.setStatus("COMPLETED");
                refundStatus.setRefundId(refundResponse.getRefundId());
                refundStatus.setGatewayRefundId(refundResponse.getGatewayRefundId());
                refundStatus.setReconciliationStatus("PENDING");
            } else {
                refundStatus.setStatus("FAILED");
                refundStatus.setFailureReason(refundResponse.getFailureReason());
            }
        } catch (Exception e) {
            logger.error("Error processing refund asynchronously: {}", e.getMessage(), e);
            refundStatus.setStatus("FAILED");
            refundStatus.setFailureReason("Error: " + e.getMessage());
        } finally {
            refundStatusRepository.save(refundStatus);
        }
    }
    
    /**
     * Create refund status entity
     */
    private RefundStatus createRefundStatus(SimplePayment payment, BigDecimal refundAmount, 
                                          String status, String reason, RefundPolicy policy,
                                          LocalDateTime cancellationTime) {
        RefundStatus refundStatus = new RefundStatus();
        refundStatus.setPaymentId(payment.getId());
        refundStatus.setBookingId(payment.getBookingId());
        refundStatus.setRefundAmount(refundAmount);
        refundStatus.setStatus(status);
        refundStatus.setRefundId(UUID.randomUUID().toString());
        refundStatus.setGatewayName(payment.getGatewayName());
        refundStatus.setReason(reason);
        refundStatus.setCancellationTime(cancellationTime);
        
        if (policy != null) {
            refundStatus.setRefundPolicyApplied(policy.getName());
            BigDecimal originalAmount = BigDecimal.valueOf(payment.getAmount());
            BigDecimal percentage = refundAmount
                .divide(originalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            refundStatus.setRefundPercentage(percentage);
        }
        
        if (TenantContext.hasTenant()) {
            refundStatus.setTenantId(TenantContext.getTenantId());
        }
        
        return refundStatus;
    }
    
    /**
     * Get refund status by payment ID
     */
    public List<RefundStatus> getRefundStatusByPaymentId(Long paymentId) {
        return refundStatusRepository.findByPaymentId(paymentId);
    }
    
    /**
     * Get refund status by refund ID
     */
    public Optional<RefundStatus> getRefundStatusByRefundId(String refundId) {
        return refundStatusRepository.findByRefundId(refundId);
    }
}

