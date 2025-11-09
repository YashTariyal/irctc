package com.irctc.booking.eventsourcing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Booking Events
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface BookingEventRepository extends JpaRepository<BookingEvent, Long> {
    
    /**
     * Get all events for a booking aggregate, ordered by timestamp
     */
    List<BookingEvent> findByAggregateIdOrderByTimestampAsc(String aggregateId);
    
    /**
     * Get events by type for a booking
     */
    List<BookingEvent> findByAggregateIdAndEventTypeOrderByTimestampAsc(
        String aggregateId, 
        String eventType
    );
    
    /**
     * Get events within a time range
     */
    List<BookingEvent> findByAggregateIdAndTimestampBetweenOrderByTimestampAsc(
        String aggregateId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
    
    /**
     * Get events by correlation ID
     */
    List<BookingEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);
    
    /**
     * Get latest event for a booking
     */
    @Query("SELECT e FROM BookingEvent e WHERE e.aggregateId = ?1 ORDER BY e.timestamp DESC")
    List<BookingEvent> findLatestEventByAggregateId(String aggregateId);
    
    /**
     * Count events for a booking
     */
    long countByAggregateId(String aggregateId);
}

