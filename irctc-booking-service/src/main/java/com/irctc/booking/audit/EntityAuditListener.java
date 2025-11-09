package com.irctc.booking.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irctc.booking.audit.entity.EntityAuditLog;
import com.irctc.booking.audit.repository.EntityAuditLogRepository;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Entity Listener for automatic audit tracking
 * Tracks all CREATE, UPDATE, DELETE operations on entities
 * 
 * Note: JPA Entity Listeners cannot directly inject Spring beans.
 * We use ApplicationContextAware to access Spring beans.
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class EntityAuditListener implements ApplicationContextAware {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityAuditListener.class);
    
    private static ApplicationContext applicationContext;
    private static ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EntityAuditListener.applicationContext = applicationContext;
    }
    
    /**
     * Get EntityAuditLogRepository from Spring context
     */
    private static EntityAuditLogRepository getAuditLogRepository() {
        if (applicationContext != null) {
            try {
                return applicationContext.getBean(EntityAuditLogRepository.class);
            } catch (Exception e) {
                logger.warn("Could not get EntityAuditLogRepository from application context", e);
            }
        }
        return null;
    }
    
    /**
     * Called before entity is persisted (CREATE)
     */
    @PrePersist
    public void prePersist(Object entity) {
        // Audit will be created in postPersist
    }
    
    /**
     * Called after entity is persisted (CREATE)
     * Note: We use @PostPersist to capture the entity, but defer the database query
     * until after the transaction commits to avoid Hibernate flush issues.
     */
    @PostPersist
    public void postPersist(Object entity) {
        // Store entity for processing after transaction commits
        // This avoids Hibernate flush issues during persistence
        try {
            String entityName = entity.getClass().getSimpleName();
            Long entityId = extractEntityId(entity);
            
            if (entityId == null) {
                logger.warn("Cannot create audit log: entityId is null for {}", entityName);
                return;
            }
            
            // Schedule audit log creation after transaction commits
            scheduleAuditLogCreation(entityName, entityId, "CREATE", null, entity);
        } catch (Exception e) {
            logger.error("❌ Error scheduling audit log for CREATE operation", e);
        }
    }
    
    /**
     * Get revision number safely in a separate transaction
     * Uses TransactionTemplate to ensure proper transaction management
     */
    private Long getRevisionNumberSafely(EntityAuditLogRepository repo, String entityName, Long entityId) {
        try {
            // Use ApplicationContext to get TransactionTemplate
            org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
                applicationContext.getBean(org.springframework.transaction.support.TransactionTemplate.class);
            
            return transactionTemplate.execute(status -> {
                return repo.getNextRevisionNumber(entityName, entityId);
            });
        } catch (Exception e) {
            logger.warn("Could not get revision number in separate transaction, using default: {}", e.getMessage());
            // Fallback: return 1 if no previous audit logs exist
            return 1L;
        }
    }
    
    /**
     * Save audit log in a new transaction
     * Uses TransactionTemplate to ensure proper transaction management
     */
    private void saveAuditLogInNewTransaction(EntityAuditLogRepository repo, EntityAuditLog auditLog) {
        try {
            // Use ApplicationContext to get TransactionTemplate
            org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
                applicationContext.getBean(org.springframework.transaction.support.TransactionTemplate.class);
            
            transactionTemplate.executeWithoutResult(status -> {
                repo.save(auditLog);
            });
        } catch (Exception e) {
            logger.error("Failed to save audit log in separate transaction", e);
        }
    }
    
    /**
     * Called before entity is updated (UPDATE)
     */
    @PreUpdate
    public void preUpdate(Object entity) {
        // Store old values for comparison
        // Old values will be captured in postUpdate by reading from the last audit log
        // This is a simpler approach than storing in thread-local
    }
    
    /**
     * Called after entity is updated (UPDATE)
     */
    @PostUpdate
    public void postUpdate(Object entity) {
        try {
            String entityName = entity.getClass().getSimpleName();
            Long entityId = extractEntityId(entity);
            
            if (entityId == null) {
                logger.warn("Cannot create audit log: entityId is null for {}", entityName);
                return;
            }
            
            // Get old values from previous audit log in a separate transaction
            Map<String, Object> oldValues = getOldValuesSafely(entityName, entityId);
            
            // Schedule audit log creation
            scheduleAuditLogCreation(entityName, entityId, "UPDATE", oldValues, entity);
        } catch (Exception e) {
            logger.error("❌ Error scheduling audit log for UPDATE operation", e);
        }
    }
    
    /**
     * Get old values from previous audit log in a separate transaction
     * Uses TransactionTemplate to ensure proper transaction management
     */
    private Map<String, Object> getOldValuesSafely(String entityName, Long entityId) {
        try {
            EntityAuditLogRepository repo = getAuditLogRepository();
            if (repo == null) {
                return null;
            }
            
            // Use ApplicationContext to get TransactionTemplate
            org.springframework.transaction.support.TransactionTemplate transactionTemplate = 
                applicationContext.getBean(org.springframework.transaction.support.TransactionTemplate.class);
            
            EntityAuditLog lastAudit = transactionTemplate.execute(status -> {
                return repo.findTopByEntityNameAndEntityIdOrderByRevisionNumberDesc(entityName, entityId)
                    .orElse(null);
            });
            
            if (lastAudit != null && lastAudit.getNewValues() != null) {
                return objectMapper.readValue(lastAudit.getNewValues(), Map.class);
            }
        } catch (Exception e) {
            logger.warn("Could not parse old values from audit log", e);
        }
        return null;
    }
    
    /**
     * Called before entity is removed (DELETE)
     */
    @PreRemove
    public void preRemove(Object entity) {
        try {
            String entityName = entity.getClass().getSimpleName();
            Long entityId = extractEntityId(entity);
            
            if (entityId == null) {
                logger.warn("Cannot create audit log: entityId is null for {}", entityName);
                return;
            }
            
            // Get current state before deletion
            Map<String, Object> oldValues = serializeEntity(entity);
            
            // Schedule audit log creation
            scheduleAuditLogCreation(entityName, entityId, "DELETE", oldValues, entity);
        } catch (Exception e) {
            logger.error("❌ Error scheduling audit log for DELETE operation", e);
        }
    }
    
    /**
     * Schedule audit log creation after transaction commits
     * This version handles DELETE where newValues should be null
     */
    private void scheduleAuditLogCreation(String entityName, Long entityId, String action, 
                                         Map<String, Object> oldValues, Object entity) {
        try {
            EntityAuditLogRepository repo = getAuditLogRepository();
            if (repo == null) {
                logger.warn("EntityAuditLogRepository not available, skipping audit log");
                return;
            }
            
            // Get entity data (null for DELETE)
            Map<String, Object> entityData = null;
            if (!"DELETE".equals(action)) {
                entityData = serializeEntity(entity);
            }
            
            // Get next revision number (this will run in a separate transaction)
            Long revisionNumber = getRevisionNumberSafely(repo, entityName, entityId);
            
            EntityAuditLog auditLog = createAuditLog(
                entityName,
                entityId,
                action,
                oldValues,
                entityData,
                getCurrentUser(),
                getCurrentUsername(),
                getClientIpAddress()
            );
            auditLog.setRevisionNumber(revisionNumber);
            
            // Save in a new transaction
            saveAuditLogInNewTransaction(repo, auditLog);
            logger.debug("✅ Audit log created for {} {}: entityId={}, revision={}", 
                       entityName, action, entityId, revisionNumber);
        } catch (Exception e) {
            logger.error("❌ Error creating audit log for {} operation: {}", action, e.getMessage(), e);
        }
    }
    
    /**
     * Extract entity ID using reflection
     */
    private Long extractEntityId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(entity);
            if (id instanceof Long) {
                return (Long) id;
            } else if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        } catch (Exception e) {
            logger.debug("Could not extract entity ID: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Serialize entity to JSON map
     */
    private Map<String, Object> serializeEntity(Object entity) {
        try {
            Map<String, Object> data = new HashMap<>();
            Field[] fields = entity.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                // Skip JPA-related fields
                if (field.isAnnotationPresent(OneToMany.class) || 
                    field.isAnnotationPresent(ManyToOne.class) ||
                    field.isAnnotationPresent(ManyToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class)) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(entity);
                
                // Handle special types
                if (value != null) {
                    if (value instanceof LocalDateTime) {
                        data.put(field.getName(), value.toString());
                    } else {
                        data.put(field.getName(), value);
                    }
                }
            }
            
            return data;
        } catch (Exception e) {
            logger.error("Error serializing entity", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Create audit log entry
     */
    private EntityAuditLog createAuditLog(String entityName, Long entityId, String action,
                                         Map<String, Object> oldValues, Map<String, Object> newValues,
                                         String userId, String username, String ipAddress) {
        EntityAuditLog auditLog = new EntityAuditLog();
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setChangedBy(userId);
        auditLog.setChangedByUsername(username);
        auditLog.setIpAddress(ipAddress);
        auditLog.setChangedAt(LocalDateTime.now());
        
        try {
            if (oldValues != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newValues));
            }
        } catch (Exception e) {
            logger.error("Error serializing audit values", e);
        }
        
        return auditLog;
    }
    
    /**
     * Get current user from request context
     */
    private String getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userId = request.getHeader("X-User-Id");
                if (userId != null) {
                    return userId;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "SYSTEM";
    }
    
    /**
     * Get current username from request context
     */
    private String getCurrentUsername() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String username = request.getHeader("X-Username");
                if (username != null) {
                    return username;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "SYSTEM";
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "UNKNOWN";
    }
}

