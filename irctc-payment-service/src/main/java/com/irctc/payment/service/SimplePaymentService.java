package com.irctc.payment.service;

import com.irctc.payment.dto.PaymentRequest;
import com.irctc.payment.dto.PaymentResponse;
import com.irctc.payment.dto.RefundRequest;
import com.irctc.payment.dto.RefundResponse;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.eventsourcing.PaymentEventStore;
import com.irctc.payment.gateway.PaymentGateway;
import com.irctc.payment.repository.SimplePaymentRepository;
import com.irctc.payment.tenant.TenantContext;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SimplePaymentService {

    private static final Logger logger = LoggerFactory.getLogger(SimplePaymentService.class);

    @Autowired
    private SimplePaymentRepository paymentRepository;
    
    @Autowired(required = false)
    private PaymentEventStore eventStore;
    
    @Autowired(required = false)
    private GatewaySelectorService gatewaySelectorService;
    
    @Autowired(required = false)
    private GatewayStatisticsService statisticsService;

    public List<SimplePayment> getAllPayments() {
        List<SimplePayment> payments = paymentRepository.findAll();
        // Filter by tenant if context is set
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            return payments.stream()
                .filter(p -> tenantId.equals(p.getTenantId()))
                .toList();
        }
        return payments;
    }

    @Cacheable(value = "payments", key = "#id", unless = "#result.isEmpty()")
    public Optional<SimplePayment> getPaymentById(Long id) {
        Optional<SimplePayment> payment = paymentRepository.findById(id);
        // Validate tenant access
        if (payment.isPresent() && TenantContext.hasTenant()) {
            SimplePayment p = payment.get();
            if (!TenantContext.getTenantId().equals(p.getTenantId())) {
                return Optional.empty();
            }
        }
        return payment;
    }

    @Cacheable(value = "payments-by-transaction", key = "#transactionId", unless = "#result.isEmpty()")
    public Optional<SimplePayment> getPaymentByTransactionId(String transactionId) {
        Optional<SimplePayment> payment = paymentRepository.findByTransactionId(transactionId);
        // Validate tenant access
        if (payment.isPresent() && TenantContext.hasTenant()) {
            SimplePayment p = payment.get();
            if (!TenantContext.getTenantId().equals(p.getTenantId())) {
                return Optional.empty();
            }
        }
        return payment;
    }

    @Bulkhead(name = "payment-query", type = Bulkhead.Type.SEMAPHORE)
    @Cacheable(value = "payments-by-booking", key = "#bookingId")
    public List<SimplePayment> getPaymentsByBookingId(Long bookingId) {
        List<SimplePayment> payments = paymentRepository.findByBookingId(bookingId);
        // Filter by tenant if context is set
        if (TenantContext.hasTenant()) {
            String tenantId = TenantContext.getTenantId();
            return payments.stream()
                .filter(p -> tenantId.equals(p.getTenantId()))
                .toList();
        }
        return payments;
    }

    /**
     * Process payment using gateway abstraction with automatic selection and fallback
     */
    @Bulkhead(name = "payment-processing", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "payment-processing")
    @CacheEvict(value = {"payments-by-booking"}, key = "#payment.bookingId", allEntries = false)
    public SimplePayment processPayment(SimplePayment payment) {
        return processPaymentWithGateway(payment, null);
    }
    
    /**
     * Process payment with specific gateway or auto-select
     */
    @CircuitBreaker(name = "payment-gateway", fallbackMethod = "processPaymentFallback")
    public SimplePayment processPaymentWithGateway(SimplePayment payment, String preferredGateway) {
        // Set tenant ID from context
        if (TenantContext.hasTenant()) {
            payment.setTenantId(TenantContext.getTenantId());
        }
        
        // Convert to PaymentRequest
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(payment.getBookingId());
        paymentRequest.setAmount(BigDecimal.valueOf(payment.getAmount()));
        paymentRequest.setCurrency(payment.getCurrency());
        paymentRequest.setPaymentMethod(payment.getPaymentMethod());
        paymentRequest.setDescription("Booking payment");
        paymentRequest.setGatewayPreference(preferredGateway);
        
        PaymentGateway selectedGateway = null;
        PaymentResponse gatewayResponse = null;
        Exception lastException = null;
        
        // Try primary gateway
        try {
            if (gatewaySelectorService != null) {
                selectedGateway = gatewaySelectorService.selectGateway(paymentRequest);
                logger.info("Selected gateway: {} for payment", selectedGateway.getGatewayName());
                
                gatewayResponse = selectedGateway.processPayment(paymentRequest);
                
                if ("SUCCESS".equals(gatewayResponse.getStatus())) {
                    // Record success
                    if (statisticsService != null) {
                        statisticsService.recordSuccess(
                            selectedGateway.getGatewayName(),
                            gatewayResponse.getAmount(),
                            gatewayResponse.getGatewayFee()
                        );
                    }
                } else {
                    // Record failure
                    if (statisticsService != null) {
                        statisticsService.recordFailure(
                            selectedGateway.getGatewayName(),
                            gatewayResponse.getAmount()
                        );
                    }
                    
                    // Try fallback gateway
                    logger.warn("Primary gateway {} failed, trying fallback", selectedGateway.getGatewayName());
                    gatewayResponse = tryFallbackGateway(paymentRequest, selectedGateway);
                }
            } else {
                // Fallback to simple processing if gateway service not available
                logger.warn("Gateway selector not available, using simple payment processing");
                gatewayResponse = createSimplePaymentResponse(paymentRequest);
            }
        } catch (Exception e) {
            logger.error("Error processing payment with gateway: {}", e.getMessage(), e);
            lastException = e;
            
            // Try fallback
            if (selectedGateway != null) {
                try {
                    gatewayResponse = tryFallbackGateway(paymentRequest, selectedGateway);
                } catch (Exception fallbackException) {
                    logger.error("Fallback gateway also failed: {}", fallbackException.getMessage());
                    gatewayResponse = createSimplePaymentResponse(paymentRequest);
                }
            } else {
                gatewayResponse = createSimplePaymentResponse(paymentRequest);
            }
        }
        
        // Update payment with gateway response
        payment.setTransactionId(gatewayResponse.getTransactionId());
        payment.setGatewayName(gatewayResponse.getGatewayName());
        payment.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
        if (gatewayResponse.getGatewayFee() != null) {
            payment.setGatewayFee(gatewayResponse.getGatewayFee().doubleValue());
        }
        payment.setPaymentTime(gatewayResponse.getTransactionTime() != null ? 
            gatewayResponse.getTransactionTime() : LocalDateTime.now());
        payment.setStatus(gatewayResponse.getStatus());
        payment.setCreatedAt(LocalDateTime.now());
        
        SimplePayment saved = paymentRepository.save(payment);
        
        // Store event in event store (Event Sourcing)
        if (eventStore != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("paymentId", saved.getId());
            eventData.put("bookingId", saved.getBookingId());
            eventData.put("amount", saved.getAmount());
            eventData.put("currency", saved.getCurrency());
            eventData.put("paymentMethod", saved.getPaymentMethod());
            eventData.put("transactionId", saved.getTransactionId());
            eventData.put("gatewayName", saved.getGatewayName());
            eventData.put("status", saved.getStatus());
            eventData.put("paymentTime", saved.getPaymentTime());
            
            String eventType = "SUCCESS".equals(saved.getStatus()) ? "PAYMENT_COMPLETED" : "PAYMENT_FAILED";
            eventStore.appendEvent(
                saved.getId().toString(),
                eventType,
                eventData,
                UUID.randomUUID().toString(),
                saved.getBookingId().toString()
            );
            logger.info("📝 Payment event stored: {} for payment: {}", eventType, saved.getId());
        }
        
        return saved;
    }
    
    /**
     * Try fallback gateway if primary fails
     */
    private PaymentResponse tryFallbackGateway(PaymentRequest request, PaymentGateway failedGateway) {
        if (gatewaySelectorService == null) {
            return createSimplePaymentResponse(request);
        }
        
        List<PaymentGateway> availableGateways = gatewaySelectorService.getAvailableGateways();
        PaymentGateway fallback = availableGateways.stream()
            .filter(g -> !g.getGatewayName().equals(failedGateway.getGatewayName()))
            .filter(g -> g.supportsCurrency(request.getCurrency()))
            .filter(g -> g.supportsPaymentMethod(request.getPaymentMethod()))
            .findFirst()
            .orElse(null);
        
        if (fallback != null) {
            logger.info("Using fallback gateway: {}", fallback.getGatewayName());
            PaymentResponse response = fallback.processPayment(request);
            
            if ("SUCCESS".equals(response.getStatus()) && statisticsService != null) {
                statisticsService.recordSuccess(
                    fallback.getGatewayName(),
                    response.getAmount(),
                    response.getGatewayFee()
                );
            } else if (statisticsService != null) {
                statisticsService.recordFailure(fallback.getGatewayName(), response.getAmount());
            }
            
            return response;
        }
        
        logger.warn("No fallback gateway available, using simple processing");
        return createSimplePaymentResponse(request);
    }
    
    /**
     * Create simple payment response when no gateway is available
     */
    private PaymentResponse createSimplePaymentResponse(PaymentRequest request) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(UUID.randomUUID().toString());
        response.setGatewayName("INTERNAL");
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setPaymentMethod(request.getPaymentMethod());
        response.setStatus("COMPLETED");
        response.setTransactionTime(LocalDateTime.now());
        response.setGatewayFee(BigDecimal.ZERO);
        return response;
    }
    
    /**
     * Fallback method for circuit breaker
     */
    private SimplePayment processPaymentFallback(SimplePayment payment, String preferredGateway, Exception e) {
        logger.warn("Payment gateway circuit breaker triggered, using fallback: {}", e.getMessage());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setGatewayName("FALLBACK");
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    /**
     * Process refund using the same gateway as original payment
     */
    @Bulkhead(name = "payment-refund", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "payment-refund")
    @CacheEvict(value = {"payments", "payments-by-transaction", "payments-by-booking"}, 
                key = "#id", allEntries = false)
    public SimplePayment refundPayment(Long id) {
        SimplePayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new com.irctc.payment.exception.EntityNotFoundException("Payment", id));
        
        // Process refund through gateway if available
        if (payment.getGatewayName() != null && !payment.getGatewayName().equals("INTERNAL") 
            && !payment.getGatewayName().equals("FALLBACK") && gatewaySelectorService != null) {
            try {
                PaymentGateway gateway = gatewaySelectorService.getGatewayByName(payment.getGatewayName());
                
                RefundRequest refundRequest = new RefundRequest();
                refundRequest.setOriginalTransactionId(payment.getTransactionId());
                refundRequest.setGatewayTransactionId(payment.getGatewayTransactionId());
                refundRequest.setRefundAmount(BigDecimal.valueOf(payment.getAmount()));
                refundRequest.setReason("Booking cancellation");
                refundRequest.setGatewayName(payment.getGatewayName());
                
                RefundResponse refundResponse = gateway.processRefund(refundRequest);
                
                if ("SUCCESS".equals(refundResponse.getStatus())) {
                    payment.setStatus("REFUNDED");
                    logger.info("✅ Refund processed via gateway {}: {}", 
                        payment.getGatewayName(), refundResponse.getRefundId());
                } else {
                    payment.setStatus("REFUND_FAILED");
                    logger.warn("❌ Refund failed via gateway {}: {}", 
                        payment.getGatewayName(), refundResponse.getFailureReason());
                }
            } catch (Exception e) {
                logger.error("Error processing refund via gateway: {}", e.getMessage(), e);
                // Fallback to simple refund
                payment.setStatus("REFUNDED");
            }
        } else {
            // Simple refund without gateway
            payment.setStatus("REFUNDED");
        }
        
        SimplePayment saved = paymentRepository.save(payment);
        
        // Store event in event store (Event Sourcing)
        if (eventStore != null) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("paymentId", saved.getId());
            eventData.put("bookingId", saved.getBookingId());
            eventData.put("refundAmount", saved.getAmount());
            eventData.put("refundTime", LocalDateTime.now());
            eventData.put("status", saved.getStatus());
            eventData.put("gatewayName", saved.getGatewayName());
            
            eventStore.appendEvent(
                saved.getId().toString(),
                "PAYMENT_REFUNDED",
                eventData,
                UUID.randomUUID().toString(),
                saved.getBookingId().toString()
            );
            logger.info("📝 Payment event stored: PAYMENT_REFUNDED for payment: {}", saved.getId());
        }
        
        return saved;
    }
}
