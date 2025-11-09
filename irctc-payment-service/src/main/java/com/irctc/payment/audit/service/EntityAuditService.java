package com.irctc.payment.audit.service;

import com.irctc.payment.audit.entity.EntityAuditLog;
import com.irctc.payment.audit.repository.EntityAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for querying entity audit logs
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class EntityAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityAuditService.class);
    
    @Autowired
    private EntityAuditLogRepository auditLogRepository;
    
    /**
     * Get complete audit history for an entity
     */
    public List<EntityAuditLog> getAuditHistory(String entityName, Long entityId) {
        return auditLogRepository.findByEntityNameAndEntityIdOrderByRevisionNumberAsc(entityName, entityId);
    }
    
    /**
     * Get latest audit log for an entity
     */
    public Optional<EntityAuditLog> getLatestAuditLog(String entityName, Long entityId) {
        return auditLogRepository.findTopByEntityNameAndEntityIdOrderByRevisionNumberDesc(entityName, entityId);
    }
    
    /**
     * Get audit logs by action
     */
    public List<EntityAuditLog> getAuditLogsByAction(String entityName, Long entityId, String action) {
        return auditLogRepository.findByEntityNameAndEntityIdAndActionOrderByRevisionNumberAsc(entityName, entityId, action);
    }
    
    /**
     * Get audit logs by user
     */
    public List<EntityAuditLog> getAuditLogsByUser(String userId) {
        return auditLogRepository.findByChangedByOrderByChangedAtDesc(userId);
    }
    
    /**
     * Get audit logs within time range
     */
    public List<EntityAuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByChangedAtBetweenOrderByChangedAtDesc(start, end);
    }
    
    /**
     * Get audit log count for an entity
     */
    public long getAuditLogCount(String entityName, Long entityId) {
        return auditLogRepository.countByEntityNameAndEntityId(entityName, entityId);
    }
}

