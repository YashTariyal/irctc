package com.irctc.payment.eventsourcing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Payment Events
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    
    /**
     * Get all events for a payment aggregate, ordered by timestamp
     */
    List<PaymentEvent> findByAggregateIdOrderByTimestampAsc(String aggregateId);
    
    /**
     * Get events by type for a payment
     */
    List<PaymentEvent> findByAggregateIdAndEventTypeOrderByTimestampAsc(
        String aggregateId, 
        String eventType
    );
    
    /**
     * Get events within a time range
     */
    List<PaymentEvent> findByAggregateIdAndTimestampBetweenOrderByTimestampAsc(
        String aggregateId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
    
    /**
     * Get events by correlation ID
     */
    List<PaymentEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * Get latest event for a payment
     */
    @Query("SELECT e FROM PaymentEvent e WHERE e.aggregateId = ?1 ORDER BY e.timestamp DESC")
    List<PaymentEvent> findLatestEventByAggregateId(String aggregateId);
    
    /**
     * Count events for a payment
     */
    long countByAggregateId(String aggregateId);
}

