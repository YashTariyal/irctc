package com.irctc.notification.repository;

import com.irctc.notification.entity.ScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {
    @Query("SELECT s FROM ScheduledNotification s WHERE s.status = :status AND s.scheduledTime <= :time")
    List<ScheduledNotification> findByStatusAndScheduledTimeBefore(
        @Param("status") String status, 
        @Param("time") LocalDateTime time
    );
    
    List<ScheduledNotification> findByUserId(Long userId);
    List<ScheduledNotification> findByStatus(String status);
}

