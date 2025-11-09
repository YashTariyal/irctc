package com.irctc.notification.eventtracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event Consumption Log
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface EventConsumptionLogRepository extends JpaRepository<EventConsumptionLog, Long> {
    
    /**
     * Find by event ID (for idempotency check)
     */
    Optional<EventConsumptionLog> findByEventId(String eventId);
    
    /**
     * Check if event already processed
     */
    boolean existsByEventIdAndStatus(String eventId, EventConsumptionLog.ConsumptionStatus status);
    
    /**
     * Find events by status
     */
    List<EventConsumptionLog> findByStatusOrderByReceivedAtAsc(
        EventConsumptionLog.ConsumptionStatus status
    );
    
    /**
     * Mark as processing
     */
    @Modifying
    @Query("UPDATE EventConsumptionLog e SET e.status = 'PROCESSING' WHERE e.id = :id")
    void markAsProcessing(@Param("id") Long id);
    
    /**
     * Mark as processed
     */
    @Modifying
    @Query("UPDATE EventConsumptionLog e SET e.status = 'PROCESSED', " +
           "e.processedAt = :processedAt, e.processingTimeMs = :processingTime " +
           "WHERE e.id = :id")
    void markAsProcessed(@Param("id") Long id,
                        @Param("processedAt") LocalDateTime processedAt,
                        @Param("processingTime") Long processingTime);
    
    /**
     * Mark as failed and increment retry count
     */
    @Modifying
    @Query("UPDATE EventConsumptionLog e SET e.status = :status, " +
           "e.retryCount = e.retryCount + 1, e.errorMessage = :errorMessage, " +
           "e.errorStackTrace = :stackTrace WHERE e.id = :id")
    void markAsFailed(@Param("id") Long id,
                     @Param("status") EventConsumptionLog.ConsumptionStatus status,
                     @Param("errorMessage") String errorMessage,
                     @Param("stackTrace") String stackTrace);
    
    /**
     * Count events by status
     */
    long countByStatus(EventConsumptionLog.ConsumptionStatus status);
}

