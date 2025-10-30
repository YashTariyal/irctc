package com.irctc.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking.entity.OutboxEvent;
import com.irctc.booking.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox Event Service
 * 
 * This service handles saving events to the outbox table within the same transaction
 * as the business logic, ensuring at-least-once delivery guarantees.
 */
@Service
public class OutboxEventService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxEventService.class);

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Save an event to the outbox table within the current transaction
     * 
     * @param topic Kafka topic name
     * @param payload Event payload object (will be serialized to JSON)
     * @return Saved OutboxEvent
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveEvent(String topic, Object payload) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setTopic(topic);
            
            // Serialize payload to JSON string
            String payloadJson = objectMapper.writeValueAsString(payload);
            event.setPayload(payloadJson);
            event.setStatus(OutboxEvent.OutboxStatus.PENDING);

            OutboxEvent saved = outboxRepository.save(event);
            
            logger.info("💾 Saved event to outbox: id={}, topic={}", saved.getId(), topic);
            return saved;

        } catch (Exception e) {
            logger.error("❌ Error saving event to outbox", e);
            throw new RuntimeException("Failed to save event to outbox", e);
        }
    }
}
