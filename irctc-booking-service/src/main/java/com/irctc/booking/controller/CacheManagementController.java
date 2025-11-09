package com.irctc.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Cache Management Controller
 * 
 * Provides REST API for cache management operations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    @Autowired(required = false)
    private CacheManager cacheManager;

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (cacheManager == null) {
            stats.put("enabled", false);
            stats.put("message", "Cache manager not available");
            return ResponseEntity.ok(stats);
        }
        
        stats.put("enabled", true);
        Map<String, Object> cacheNames = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Map<String, Object> cacheInfo = new HashMap<>();
            cacheInfo.put("name", cacheName);
            // Note: Spring Cache doesn't provide direct size/hit rate info
            // Would need custom implementation or Redis-specific metrics
            cacheNames.put(cacheName, cacheInfo);
        });
        
        stats.put("caches", cacheNames);
        stats.put("totalCaches", cacheManager.getCacheNames().size());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear specific cache
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(@PathVariable String cacheName) {
        Map<String, String> response = new HashMap<>();
        
        if (cacheManager == null) {
            response.put("status", "error");
            response.put("message", "Cache manager not available");
            return ResponseEntity.ok(response);
        }
        
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            response.put("status", "success");
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
        } else {
            response.put("status", "error");
            response.put("message", "Cache '" + cacheName + "' not found");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear all caches
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearAllCaches() {
        Map<String, Object> response = new HashMap<>();
        
        if (cacheManager == null) {
            response.put("status", "error");
            response.put("message", "Cache manager not available");
            return ResponseEntity.ok(response);
        }
        
        int cleared = 0;
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                cleared++;
            }
        }
        
        response.put("status", "success");
        response.put("message", "Cleared " + cleared + " cache(s)");
        response.put("clearedCount", cleared);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Evict specific key from cache
     */
    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<Map<String, String>> evictCacheKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        Map<String, String> response = new HashMap<>();
        
        if (cacheManager == null) {
            response.put("status", "error");
            response.put("message", "Cache manager not available");
            return ResponseEntity.ok(response);
        }
        
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            response.put("status", "success");
            response.put("message", "Key '" + key + "' evicted from cache '" + cacheName + "'");
        } else {
            response.put("status", "error");
            response.put("message", "Cache '" + cacheName + "' not found");
        }
        
        return ResponseEntity.ok(response);
    }
}

