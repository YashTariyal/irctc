package com.irctc.train.service;

import com.irctc.train.entity.SimpleTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "spring.data.redis.host")
public class TrainCacheService {

    private static final Logger logger = LoggerFactory.getLogger(TrainCacheService.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    
    private static final String CACHE_KEY_TRAIN_SEARCH = "train:search:";
    private static final String CACHE_KEY_TRAIN_DETAILS = "train:details:";
    private static final String CACHE_KEY_ALL_TRAINS = "train:all";
    private static final String CACHE_KEY_TRAIN_BY_NUMBER = "train:number:";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache train search results
     */
    public void cacheTrainSearch(String source, String destination, List<SimpleTrain> trains) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_TRAIN_SEARCH + source + ":" + destination;
            redisTemplate.opsForValue().set(key, trains, DEFAULT_TTL);
            logger.debug("Cached train search: {} trains for route {} -> {}", trains.size(), source, destination);
        } catch (Exception e) {
            logger.error("Failed to cache train search", e);
        }
    }

    /**
     * Get cached train search results
     */
    @SuppressWarnings("unchecked")
    public Optional<List<SimpleTrain>> getCachedTrainSearch(String source, String destination) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_TRAIN_SEARCH + source + ":" + destination;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for train search: {} -> {}", source, destination);
                return Optional.of((List<SimpleTrain>) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached train search", e);
        }
        return Optional.empty();
    }

    /**
     * Cache train by ID
     */
    public void cacheTrain(Long id, SimpleTrain train) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_TRAIN_DETAILS + id;
            redisTemplate.opsForValue().set(key, train, DEFAULT_TTL);
            logger.debug("Cached train details: {}", id);
        } catch (Exception e) {
            logger.error("Failed to cache train", e);
        }
    }

    /**
     * Get cached train by ID
     */
    public Optional<SimpleTrain> getCachedTrain(Long id) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_TRAIN_DETAILS + id;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for train: {}", id);
                return Optional.of((SimpleTrain) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached train", e);
        }
        return Optional.empty();
    }

    /**
     * Cache train by number
     */
    public void cacheTrainByNumber(String trainNumber, SimpleTrain train) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_TRAIN_BY_NUMBER + trainNumber;
            redisTemplate.opsForValue().set(key, train, DEFAULT_TTL);
            logger.debug("Cached train by number: {}", trainNumber);
        } catch (Exception e) {
            logger.error("Failed to cache train by number", e);
        }
    }

    /**
     * Get cached train by number
     */
    public Optional<SimpleTrain> getCachedTrainByNumber(String trainNumber) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_TRAIN_BY_NUMBER + trainNumber;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for train number: {}", trainNumber);
                return Optional.of((SimpleTrain) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached train by number", e);
        }
        return Optional.empty();
    }

    /**
     * Invalidate cache for a specific train
     */
    public void invalidateTrain(Long id) {
        if (redisTemplate == null) return;
        
        try {
            redisTemplate.delete(CACHE_KEY_TRAIN_DETAILS + id);
            redisTemplate.delete(CACHE_KEY_ALL_TRAINS);
            logger.debug("Invalidated cache for train: {}", id);
        } catch (Exception e) {
            logger.error("Failed to invalidate train cache", e);
        }
    }

    /**
     * Clear all train caches
     */
    public void clearAllCaches() {
        if (redisTemplate == null) return;
        
        try {
            redisTemplate.delete(CACHE_KEY_ALL_TRAINS);
            logger.info("Cleared all train caches");
        } catch (Exception e) {
            logger.error("Failed to clear caches", e);
        }
    }
}

