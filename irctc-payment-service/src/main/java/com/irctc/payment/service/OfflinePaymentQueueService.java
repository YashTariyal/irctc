package com.irctc.payment.service;

import com.irctc.payment.dto.OfflinePaymentRequest;
import com.irctc.payment.entity.OfflinePaymentIntent;
import com.irctc.payment.entity.SimplePayment;
import com.irctc.payment.repository.OfflinePaymentIntentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OfflinePaymentQueueService {

    private static final Logger logger = LoggerFactory.getLogger(OfflinePaymentQueueService.class);
    private static final List<String> QUEUED_STATUSES = List.of("QUEUED", "PROCESSING");

    private final OfflinePaymentIntentRepository intentRepository;
    private final SimplePaymentService simplePaymentService;

    public OfflinePaymentQueueService(OfflinePaymentIntentRepository intentRepository,
                                      SimplePaymentService simplePaymentService) {
        this.intentRepository = intentRepository;
        this.simplePaymentService = simplePaymentService;
    }

    public OfflinePaymentIntent queue(OfflinePaymentRequest request) {
        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setUserId(request.getUserId());
        intent.setBookingId(request.getBookingId());
        intent.setAmount(request.getAmount());
        intent.setCurrency(request.getCurrency());
        intent.setPaymentMethod(request.getPaymentMethod());
        intent.setGatewayPreference(request.getGatewayPreference());
        intent.setMetadata(request.getMetadata());
        intent.setStatus("QUEUED");
        intent.setQueuedAt(LocalDateTime.now());
        OfflinePaymentIntent saved = intentRepository.save(intent);
        logger.info("📥 Queued offline payment {} for booking {}", saved.getId(), saved.getBookingId());
        return saved;
    }

    public List<OfflinePaymentIntent> getPendingForUser(Long userId) {
        return intentRepository.findByUserIdAndStatusIn(userId, QUEUED_STATUSES);
    }

    @Transactional
    public OfflinePaymentIntent processIntent(Long intentId) {
        OfflinePaymentIntent intent = intentRepository.findById(intentId)
            .orElseThrow(() -> new IllegalArgumentException("Offline payment intent not found: " + intentId));

        if ("COMPLETED".equals(intent.getStatus())) {
            return intent;
        }

        try {
            intent.setStatus("PROCESSING");
            intent.setLastAttemptAt(LocalDateTime.now());
            intentRepository.save(intent);

            SimplePayment payment = new SimplePayment();
            payment.setBookingId(intent.getBookingId());
            payment.setAmount(intent.getAmount().doubleValue());
            payment.setCurrency(intent.getCurrency());
            payment.setPaymentMethod(intent.getPaymentMethod());
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setStatus("INITIATED");
            payment.setGatewayName(intent.getGatewayPreference());

            SimplePayment processed = simplePaymentService.processPayment(payment);
            intent.setProcessedPaymentId(processed.getId());
            intent.setProcessedAt(LocalDateTime.now());

            if ("FAILED".equalsIgnoreCase(processed.getStatus())) {
                intent.setStatus("FAILED");
                intent.setFailureReason("Gateway reported failure");
            } else {
                intent.setStatus("COMPLETED");
                intent.setFailureReason(null);
            }
        } catch (Exception ex) {
            logger.error("❌ Failed to process offline payment {}: {}", intentId, ex.getMessage());
            intent.setStatus("FAILED");
            intent.setFailureReason(ex.getMessage());
        }

        return intentRepository.save(intent);
    }

    @Transactional
    public int processPendingBatch() {
        List<OfflinePaymentIntent> intents = intentRepository.findTop50ByStatusInOrderByQueuedAtAsc(List.of("QUEUED"));
        intents.forEach(intent -> {
            try {
                processIntent(intent.getId());
            } catch (Exception ex) {
                logger.error("Error processing offline intent {}: {}", intent.getId(), ex.getMessage());
            }
        });
        return intents.size();
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void scheduledSync() {
        int processed = processPendingBatch();
        if (processed > 0) {
            logger.info("🔁 Scheduled offline payment sync processed {} intents", processed);
        }
    }
}

