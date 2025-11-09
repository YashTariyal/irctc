package com.irctc.user.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for Distributed Lock Management
 * 
 * Provides endpoints for:
 * - Checking lock status
 * - Getting lock information
 * - Manual lock operations (for testing/admin)
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/locks")
@ConditionalOnProperty(name = "spring.data.redis.host")
public class DistributedLockController {

    @Autowired(required = false)
    private DistributedLockService lockService;
    
    /**
     * Check if a lock is currently held
     */
    @GetMapping("/{lockKey}/status")
    public ResponseEntity<Map<String, Object>> getLockStatus(@PathVariable String lockKey) {
        Map<String, Object> response = new HashMap<>();
        
        if (lockService == null) {
            response.put("error", "Distributed locking not available");
            return ResponseEntity.ok(response);
        }
        
        boolean isLocked = lockService.isLocked(lockKey);
        response.put("lockKey", lockKey);
        response.put("isLocked", isLocked);
        
        if (isLocked) {
            response.put("owner", lockService.getLockOwner(lockKey));
            response.put("ttl", lockService.getLockTtl(lockKey));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get lock information
     */
    @GetMapping("/{lockKey}")
    public ResponseEntity<Map<String, Object>> getLockInfo(@PathVariable String lockKey) {
        Map<String, Object> response = new HashMap<>();
        
        if (lockService == null) {
            response.put("error", "Distributed locking not available");
            return ResponseEntity.ok(response);
        }
        
        response.put("lockKey", lockKey);
        response.put("isLocked", lockService.isLocked(lockKey));
        response.put("owner", lockService.getLockOwner(lockKey));
        response.put("ttl", lockService.getLockTtl(lockKey));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Acquire a lock (for testing/admin purposes)
     */
    @PostMapping("/{lockKey}/acquire")
    public ResponseEntity<Map<String, Object>> acquireLock(
            @PathVariable String lockKey,
            @RequestParam(defaultValue = "30") long timeout) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (lockService == null) {
            response.put("error", "Distributed locking not available");
            return ResponseEntity.ok(response);
        }
        
        DistributedLockService.LockHandle lockHandle = lockService.acquireLock(lockKey, timeout);
        
        if (lockHandle != null) {
            response.put("success", true);
            response.put("lockKey", lockKey);
            response.put("owner", lockHandle.getLockOwner());
            response.put("timeout", timeout);
            response.put("acquiredAt", lockHandle.getAcquiredAt());
        } else {
            response.put("success", false);
            response.put("error", "Failed to acquire lock");
        }
        
        return ResponseEntity.ok(response);
    }
}

