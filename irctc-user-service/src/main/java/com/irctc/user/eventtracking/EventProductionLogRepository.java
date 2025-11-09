package com.irctc.user.eventtracking;

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
    
    Optional<EventProductionLog> findByEventId(String eventId);
    
    List<EventProductionLog> findByStatusOrderByCreatedAtAsc(EventProductionLog.ProductionStatus status);
    
    List<EventProductionLog> findByTopicAndStatusOrderByCreatedAtAsc(String topic, EventProductionLog.ProductionStatus status);
    
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = 'PUBLISHED', " +
           "e.publishedAt = :publishedAt, e.partitionNumber = :partition, " +
           "e.offset = :offset WHERE e.id = :id")
    void markAsPublished(@Param("id") Long id,
                        @Param("publishedAt") LocalDateTime publishedAt,
                        @Param("partition") Integer partition,
                        @Param("offset") Long offset);
    
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = 'PUBLISHING' WHERE e.id = :id")
    void markAsPublishing(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE EventProductionLog e SET e.status = :status, " +
           "e.retryCount = e.retryCount + 1, e.errorMessage = :errorMessage, e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :id")
    void markAsFailed(@Param("id") Long id,
                     @Param("status") EventProductionLog.ProductionStatus status,
                     @Param("errorMessage") String errorMessage);
    
    long countByStatus(EventProductionLog.ProductionStatus status);
    long countByTopic(String topic);
}

