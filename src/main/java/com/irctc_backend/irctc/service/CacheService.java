package com.irctc_backend.irctc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based caching service for performance optimization
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Cache TTL constants
    private static final Duration SHORT_TTL = Duration.ofMinutes(5);
    private static final Duration MEDIUM_TTL = Duration.ofMinutes(30);
    private static final Duration LONG_TTL = Duration.ofHours(2);
    
    /**
     * Cache key prefixes
     */
    public static class Keys {
        public static final String TRAIN_SEARCH = "train:search:";
        public static final String TRAIN_DETAILS = "train:details:";
        public static final String STATION_LIST = "stations:all";
        public static final String USER_SESSION = "user:session:";
        public static final String BOOKING_DETAILS = "booking:details:";
        public static final String FARE_CALCULATION = "fare:calc:";
        public static final String SEAT_AVAILABILITY = "seat:availability:";
        public static final String LOYALTY_POINTS = "loyalty:points:";
    }
    
    /**
     * Store data in cache with default TTL
     */
    public void put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, MEDIUM_TTL);
            logger.debug("Cached data for key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to cache data for key: {}", key, e);
        }
    }
    
    /**
     * Store data in cache with custom TTL
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            logger.debug("Cached data for key: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            logger.error("Failed to cache data for key: {}", key, e);
        }
    }
    
    /**
     * Store data in cache with custom TTL in seconds
     */
    public void put(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            logger.debug("Cached data for key: {} with TTL: {} seconds", key, ttlSeconds);
        } catch (Exception e) {
            logger.error("Failed to cache data for key: {}", key, e);
        }
    }
    
    /**
     * Get data from cache
     */
    public Optional<Object> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("Cache hit for key: {}", key);
                return Optional.of(value);
            }
            logger.debug("Cache miss for key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to get data from cache for key: {}", key, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get data from cache with type casting
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && type.isInstance(value)) {
                logger.debug("Cache hit for key: {} with type: {}", key, type.getSimpleName());
                return Optional.of((T) value);
            }
            logger.debug("Cache miss for key: {} with type: {}", key, type.getSimpleName());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to get data from cache for key: {} with type: {}", key, type.getSimpleName(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            logger.error("Failed to check existence for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Delete data from cache
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            logger.debug("Deleted cache key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete cache key: {}", key, e);
        }
    }
    
    /**
     * Delete multiple keys from cache
     */
    public void delete(String... keys) {
        try {
            redisTemplate.delete(List.of(keys));
            logger.debug("Deleted cache keys: {}", List.of(keys));
        } catch (Exception e) {
            logger.error("Failed to delete cache keys: {}", List.of(keys), e);
        }
    }
    
    /**
     * Delete keys by pattern
     */
    public void deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Deleted cache keys by pattern: {} ({} keys)", pattern, keys.size());
            }
        } catch (Exception e) {
            logger.error("Failed to delete cache keys by pattern: {}", pattern, e);
        }
    }
    
    /**
     * Get TTL for a key
     */
    public long getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            logger.error("Failed to get TTL for key: {}", key, e);
            return -1;
        }
    }
    
    /**
     * Set TTL for a key
     */
    public boolean setTtl(String key, long ttlSeconds) {
        try {
            Boolean result = redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            logger.error("Failed to set TTL for key: {}", key, e);
            return false;
        }
    }
    
    /**
     * Clear all cache
     */
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            logger.info("Cleared all cache data");
        } catch (Exception e) {
            logger.error("Failed to clear all cache", e);
        }
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        try {
            return String.format("Redis cache is running. Connection: %s", 
                redisTemplate.getConnectionFactory().getConnection().ping());
        } catch (Exception e) {
            return "Redis cache is not available: " + e.getMessage();
        }
    }
}
