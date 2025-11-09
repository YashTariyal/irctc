package com.irctc.user.audit.repository;

import com.irctc.user.audit.entity.EntityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Entity Audit Logs
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Repository
public interface EntityAuditLogRepository extends JpaRepository<EntityAuditLog, Long> {
    
    /**
     * Find all audit logs for a specific entity
     */
    List<EntityAuditLog> findByEntityNameAndEntityIdOrderByRevisionNumberAsc(
        String entityName, Long entityId
    );
    
    /**
     * Find latest audit log for an entity
     */
    Optional<EntityAuditLog> findTopByEntityNameAndEntityIdOrderByRevisionNumberDesc(
        String entityName, Long entityId
    );
    
    /**
     * Find audit logs by action
     */
    List<EntityAuditLog> findByEntityNameAndEntityIdAndActionOrderByRevisionNumberAsc(
        String entityName, Long entityId, String action
    );
    
    /**
     * Find audit logs by user
     */
    List<EntityAuditLog> findByChangedByOrderByChangedAtDesc(String userId);
    
    /**
     * Find audit logs within time range
     */
    List<EntityAuditLog> findByChangedAtBetweenOrderByChangedAtDesc(
        LocalDateTime start, LocalDateTime end
    );
    
    /**
     * Get next revision number for an entity
     */
    @Query("SELECT COALESCE(MAX(e.revisionNumber), 0) + 1 FROM EntityAuditLog e " +
           "WHERE e.entityName = :entityName AND e.entityId = :entityId")
    Long getNextRevisionNumber(@Param("entityName") String entityName, @Param("entityId") Long entityId);
    
    /**
     * Set revision number for a new audit log
     */
    @Modifying
    @Query("UPDATE EntityAuditLog e SET e.revisionNumber = :revisionNumber WHERE e.id = :id")
    void setRevisionNumber(@Param("id") Long id, @Param("revisionNumber") Long revisionNumber);
    
    /**
     * Count audit logs for an entity
     */
    long countByEntityNameAndEntityId(String entityName, Long entityId);
}

