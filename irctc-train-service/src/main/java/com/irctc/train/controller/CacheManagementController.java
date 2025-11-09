package com.irctc.train.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache Management Controller for Train Service
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    @Autowired(required = false)
    private CacheManager cacheManager;

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
            cacheNames.put(cacheName, cacheInfo);
        });
        
        stats.put("caches", cacheNames);
        stats.put("totalCaches", cacheManager.getCacheNames().size());
        
        return ResponseEntity.ok(stats);
    }

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
}

