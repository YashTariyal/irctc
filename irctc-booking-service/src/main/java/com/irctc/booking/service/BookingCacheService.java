package com.irctc.booking.service;

import com.irctc.booking.entity.SimpleBooking;
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
public class BookingCacheService {

    private static final Logger logger = LoggerFactory.getLogger(BookingCacheService.class);
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);
    
    private static final String CACHE_KEY_PNR = "booking:pnr:";
    private static final String CACHE_KEY_BOOKING_ID = "booking:id:";
    private static final String CACHE_KEY_USER_BOOKINGS = "booking:user:";

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Cache booking by PNR
     */
    public void cacheBookingByPnr(String pnrNumber, SimpleBooking booking) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_PNR + pnrNumber;
            redisTemplate.opsForValue().set(key, booking, DEFAULT_TTL);
            logger.debug("Cached booking by PNR: {}", pnrNumber);
        } catch (Exception e) {
            logger.error("Failed to cache booking by PNR", e);
        }
    }

    /**
     * Get cached booking by PNR
     */
    public Optional<SimpleBooking> getCachedBookingByPnr(String pnrNumber) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_PNR + pnrNumber;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for PNR: {}", pnrNumber);
                return Optional.of((SimpleBooking) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached booking by PNR", e);
        }
        return Optional.empty();
    }

    /**
     * Cache booking by ID
     */
    public void cacheBooking(Long id, SimpleBooking booking) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_BOOKING_ID + id;
            redisTemplate.opsForValue().set(key, booking, DEFAULT_TTL);
            logger.debug("Cached booking: {}", id);
        } catch (Exception e) {
            logger.error("Failed to cache booking", e);
        }
    }

    /**
     * Get cached booking by ID
     */
    public Optional<SimpleBooking> getCachedBooking(Long id) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_BOOKING_ID + id;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for booking: {}", id);
                return Optional.of((SimpleBooking) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached booking", e);
        }
        return Optional.empty();
    }

    /**
     * Cache user bookings
     */
    public void cacheUserBookings(Long userId, List<SimpleBooking> bookings) {
        if (redisTemplate == null) return;
        
        try {
            String key = CACHE_KEY_USER_BOOKINGS + userId;
            redisTemplate.opsForValue().set(key, bookings, DEFAULT_TTL);
            logger.debug("Cached bookings for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to cache user bookings", e);
        }
    }

    /**
     * Get cached user bookings
     */
    @SuppressWarnings("unchecked")
    public Optional<List<SimpleBooking>> getCachedUserBookings(Long userId) {
        if (redisTemplate == null) return Optional.empty();
        
        try {
            String key = CACHE_KEY_USER_BOOKINGS + userId;
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.debug("Cache hit for user bookings: {}", userId);
                return Optional.of((List<SimpleBooking>) cached);
            }
        } catch (Exception e) {
            logger.error("Failed to get cached user bookings", e);
        }
        return Optional.empty();
    }

    /**
     * Invalidate cache for a specific booking
     */
    public void invalidateBooking(Long id, String pnrNumber) {
        if (redisTemplate == null) return;
        
        try {
            redisTemplate.delete(CACHE_KEY_BOOKING_ID + id);
            redisTemplate.delete(CACHE_KEY_PNR + pnrNumber);
            logger.debug("Invalidated cache for booking: {} / {}", id, pnrNumber);
        } catch (Exception e) {
            logger.error("Failed to invalidate booking cache", e);
        }
    }

    /**
     * Invalidate user bookings cache
     */
    public void invalidateUserBookings(Long userId) {
        if (redisTemplate == null) return;
        
        try {
            redisTemplate.delete(CACHE_KEY_USER_BOOKINGS + userId);
            logger.debug("Invalidated user bookings cache: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to invalidate user bookings cache", e);
        }
    }
}

