package com.irctc.booking.eventtracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event Production Log
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface EventProductionLogRepository extends JpaRepository<EventProductionLog, Long> {
    
    /**
     * Find by event ID
     */
    Optional<EventProductionLog> findByEventId(String eventId);
    
    /**
     * Find pending events
     */
    List<EventProductionLog> findByStatusOrderByCreatedAtAsc(
        EventProductionLog.ProductionStatus status
    );
    
    /**
     * Find failed events that can be retried
     */
    @Query("SELECT e FROM EventProductionLog e WHERE e.status = 'FAILED' " +
           "AND e.retryCount < e.maxRetries ORDER BY e.createdAt ASC")
    List<EventProductionLog> findFailedEventsForRetry();
    
    /**
     * Find events by topic
     */
    List<EventProductionLog> findByTopicOrderByCreatedAtDesc(String topic);
    
    /**
     * Find events by correlation ID
     */
    List<EventProductionLog> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
    
    /**
     * Count events by status
     */
    long countByStatus(EventProductionLog.ProductionStatus status);
    
    /**
     * Mark as published
     */
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = 'PUBLISHED', " +
           "e.publishedAt = :publishedAt, e.partitionNumber = :partition, " +
           "e.offset = :offset WHERE e.id = :id")
    void markAsPublished(@Param("id") Long id, 
                        @Param("publishedAt") LocalDateTime publishedAt,
                        @Param("partition") Integer partition,
                        @Param("offset") Long offset);
    
    /**
     * Mark as publishing
     */
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = 'PUBLISHING' WHERE e.id = :id")
    void markAsPublishing(@Param("id") Long id);
    
    /**
     * Mark as failed and increment retry count
     */
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = :status, " +
           "e.retryCount = e.retryCount + 1, e.errorMessage = :errorMessage " +
           "WHERE e.id = :id")
    void markAsFailed(@Param("id") Long id, 
                     @Param("status") EventProductionLog.ProductionStatus status,
                     @Param("errorMessage") String errorMessage);
}

