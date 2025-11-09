package com.irctc.payment.audit.controller;

import com.irctc.payment.audit.entity.EntityAuditLog;
import com.irctc.payment.audit.service.EntityAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Entity Audit Logs
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/audit")
public class EntityAuditController {
    
    @Autowired
    private EntityAuditService auditService;
    
    /**
     * Get complete audit history for an entity
     */
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<List<EntityAuditLog>> getAuditHistory(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        List<EntityAuditLog> history = auditService.getAuditHistory(entityName, entityId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Get latest audit log for an entity
     */
    @GetMapping("/entity/{entityName}/{entityId}/latest")
    public ResponseEntity<EntityAuditLog> getLatestAuditLog(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        Optional<EntityAuditLog> auditLog = auditService.getLatestAuditLog(entityName, entityId);
        return auditLog.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get audit logs by action
     */
    @GetMapping("/entity/{entityName}/{entityId}/action/{action}")
    public ResponseEntity<List<EntityAuditLog>> getAuditLogsByAction(
            @PathVariable String entityName,
            @PathVariable Long entityId,
            @PathVariable String action) {
        List<EntityAuditLog> logs = auditService.getAuditLogsByAction(entityName, entityId, action);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get audit logs by user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EntityAuditLog>> getAuditLogsByUser(
            @PathVariable String userId) {
        List<EntityAuditLog> logs = auditService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get audit logs within time range
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<EntityAuditLog>> getAuditLogsByTimeRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<EntityAuditLog> logs = auditService.getAuditLogsByTimeRange(start, end);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * Get audit statistics
     */
    @GetMapping("/stats/{entityName}/{entityId}")
    public ResponseEntity<Map<String, Object>> getAuditStats(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevisions", auditService.getAuditLogCount(entityName, entityId));
        stats.put("createCount", auditService.getAuditLogsByAction(entityName, entityId, "CREATE").size());
        stats.put("updateCount", auditService.getAuditLogsByAction(entityName, entityId, "UPDATE").size());
        stats.put("deleteCount", auditService.getAuditLogsByAction(entityName, entityId, "DELETE").size());
        
        Optional<EntityAuditLog> latest = auditService.getLatestAuditLog(entityName, entityId);
        if (latest.isPresent()) {
            stats.put("lastModified", latest.get().getChangedAt());
            stats.put("lastModifiedBy", latest.get().getChangedByUsername());
        }
        
        return ResponseEntity.ok(stats);
    }
}

