package com.irctc.booking.repository;

import com.irctc.booking.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);
    
    List<AuditLog> findByUserId(String userId);
    
    List<AuditLog> findByAction(String action);
    
    Page<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType, Pageable pageable);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<AuditLog> findAuditHistory(String entityType, Long entityId);
}

