package com.irctc.booking.service;

import com.irctc.booking.dto.offline.OfflineActionRequest;
import com.irctc.booking.entity.OfflineAction;
import com.irctc.booking.repository.OfflineActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class OfflineActionService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineActionService.class);
    private static final Set<String> PROCESSABLE_STATUSES = Set.of("QUEUED", "PROCESSING");

    private final OfflineActionRepository offlineActionRepository;

    public OfflineActionService(OfflineActionRepository offlineActionRepository) {
        this.offlineActionRepository = offlineActionRepository;
    }

    public OfflineAction queueAction(OfflineActionRequest request) {
        OfflineAction action = new OfflineAction();
        action.setUserId(request.getUserId());
        action.setBookingId(request.getBookingId());
        action.setActionType(request.getActionType());
        action.setPayload(request.getPayload());
        action.setStatus("QUEUED");
        action.setQueuedAt(LocalDateTime.now());
        OfflineAction saved = offlineActionRepository.save(action);
        logger.info("📥 Queued offline action {} for user {}", saved.getId(), saved.getUserId());
        return saved;
    }

    public List<OfflineAction> getPendingActions(Long userId) {
        return offlineActionRepository.findByUserIdAndStatusIn(userId, PROCESSABLE_STATUSES);
    }

    @Transactional
    public List<OfflineAction> processPendingActions(Long userId) {
        List<OfflineAction> actions;
        if (userId != null) {
            actions = offlineActionRepository.findByUserIdAndStatusIn(userId, PROCESSABLE_STATUSES);
        } else {
            actions = offlineActionRepository.findTop50ByStatusInOrderByQueuedAtAsc(PROCESSABLE_STATUSES);
        }

        for (OfflineAction action : actions) {
            try {
                action.setStatus("PROCESSING");
                offlineActionRepository.save(action);

                // In a full implementation we would replay the action (seat change, cancellation etc.).
                // For now we mark it as completed to indicate it has been acknowledged by the server.
                action.setStatus("COMPLETED");
                action.setProcessedAt(LocalDateTime.now());
                action.setFailureReason(null);
                offlineActionRepository.save(action);
                logger.info("✅ Processed offline action {} ({})", action.getId(), action.getActionType());
            } catch (Exception ex) {
                logger.error("❌ Failed to process offline action {}: {}", action.getId(), ex.getMessage());
                action.setStatus("FAILED");
                action.setFailureReason(ex.getMessage());
                offlineActionRepository.save(action);
            }
        }
        return actions;
    }
}

