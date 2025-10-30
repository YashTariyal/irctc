package com.irctc.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.OutboxEvent;
import com.irctc.booking.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Outbox Event Publisher Service
 * 
 * This service implements the Transactional Outbox Pattern to guarantee
 * event delivery even in case of failures.
 * 
 * Responsibilities:
 * - Poll the outbox table for pending events
 * - Publish events to Kafka
 * - Handle retries and failures
 * - Mark events as published/failed
 */
@Service
@EnableScheduling
public class OutboxEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxEventPublisher.class);

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Scheduled job that runs every 10 seconds to process pending outbox events
     */
    @Scheduled(fixedDelay = 10000) // 10 seconds
    @SchedulerLock(name = "outboxEventPublisher", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")
    @Transactional
    public void processOutboxEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();

            if (pendingEvents.isEmpty()) {
                return;
            }

            logger.info("📦 Found {} pending outbox events to process", pendingEvents.size());

            for (OutboxEvent event : pendingEvents) {
                try {
                    publishEvent(event);
                } catch (Exception e) {
                    handlePublishFailure(event, e);
                }
            }

        } catch (Exception e) {
            logger.error("❌ Error processing outbox events", e);
        }
    }

    /**
     * Publish a single outbox event to Kafka
     */
    private void publishEvent(OutboxEvent event) {
        try {
            // Parse payload as Object (assumes JSON string)
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);

            // Synchronously publish to Kafka within transaction
            // Using get() with timeout to wait for completion ensures transaction consistency
            try {
                kafkaTemplate.send(event.getTopic(), payload)
                        .get(5, TimeUnit.SECONDS);
                logger.info("✅ Successfully published outbox event {} to topic {}", 
                        event.getId(), event.getTopic());
                markAsPublished(event);
            } catch (Exception e) {
                logger.error("❌ Failed to publish outbox event {} to Kafka", event.getId(), e);
                handlePublishFailure(event, e);
            }

        } catch (Exception e) {
            logger.error("❌ Error parsing or publishing outbox event {}", event.getId(), e);
            handlePublishFailure(event, e);
        }
    }

    /**
     * Handle publish failure - increment retry count or mark as failed
     */
    private void handlePublishFailure(OutboxEvent event, Throwable throwable) {
        if (event.getRetryCount() >= event.getMaxRetries()) {
            logger.error("❌ Outbox event {} exceeded max retries. Marking as failed.", event.getId());
            outboxRepository.markAsFailed(event.getId());
        } else {
            String errorMessage = throwable.getMessage();
            if (errorMessage != null && errorMessage.length() > 500) {
                errorMessage = errorMessage.substring(0, 500);
            }
            outboxRepository.incrementRetryCount(event.getId(), errorMessage);
            logger.warn("⚠️ Outbox event {} failed. Retry count: {}/{}. Will retry later.",
                    event.getId(), event.getRetryCount() + 1, event.getMaxRetries());
        }
    }

    /**
     * Mark event as published
     */
    private void markAsPublished(OutboxEvent event) {
        outboxRepository.markAsPublished(event.getId(), LocalDateTime.now());
    }

    /**
     * Manually trigger outbox processing for testing
     */
    public void triggerManualProcessing() {
        logger.info("🔧 Manual trigger for outbox event processing");
        processOutboxEvents();
    }
}
