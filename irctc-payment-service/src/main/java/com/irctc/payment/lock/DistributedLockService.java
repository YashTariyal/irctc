package com.irctc.payment.lock;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Distributed Lock Service using Redis
 * 
 * Provides distributed locking capabilities for critical operations
 * across multiple service instances.
 * 
 * Features:
 * - Lock acquisition with timeout
 * - Automatic lock renewal
 * - Lock release
 * - Lock status checking
 * - Metrics and monitoring
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
@ConditionalOnProperty(name = "spring.data.redis.host")
public class DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_OWNER_PREFIX = "lock:owner:";
    
    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    
    @Value("${distributed-lock.default-timeout:30}")
    private long defaultTimeoutSeconds;
    
    @Value("${distributed-lock.default-wait-time:5}")
    private long defaultWaitTimeSeconds;
    
    @Value("${distributed-lock.enable-metrics:true}")
    private boolean enableMetrics;
    
    // Metrics
    private Counter lockAcquiredCounter;
    private Counter lockReleasedCounter;
    private Counter lockFailedCounter;
    private Counter lockTimeoutCounter;
    private Timer lockAcquisitionTimer;
    
    // Lua script for atomic lock acquisition
    private static final String LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == false then " +
        "  redis.call('set', KEYS[1], ARGV[1], 'EX', ARGV[2]) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";
    
    // Lua script for atomic lock release
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('del', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";
    
    // Lua script for lock renewal
    private static final String RENEW_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('expire', KEYS[1], ARGV[2]) " +
        "else " +
        "  return 0 " +
        "end";
    
    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Long> unlockScript;
    private DefaultRedisScript<Long> renewScript;
    
    @PostConstruct
    public void init() {
        if (redisTemplate == null) {
            logger.warn("RedisTemplate not available, distributed locking disabled");
            return;
        }
        
        // Initialize Lua scripts
        lockScript = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
        unlockScript = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        renewScript = new DefaultRedisScript<>(RENEW_SCRIPT, Long.class);
        
        // Initialize metrics
        if (enableMetrics && meterRegistry != null) {
            try {
                lockAcquiredCounter = Counter.builder("distributed.lock.acquired")
                    .description("Total number of locks acquired")
                    .register(meterRegistry);
                
                lockReleasedCounter = Counter.builder("distributed.lock.released")
                    .description("Total number of locks released")
                    .register(meterRegistry);
                
                lockFailedCounter = Counter.builder("distributed.lock.failed")
                    .description("Total number of failed lock acquisitions")
                    .register(meterRegistry);
                
                lockTimeoutCounter = Counter.builder("distributed.lock.timeout")
                    .description("Total number of lock timeouts")
                    .register(meterRegistry);
                
                lockAcquisitionTimer = Timer.builder("distributed.lock.acquisition.time")
                    .description("Time taken to acquire locks")
                    .register(meterRegistry);
                
                logger.info("✅ Distributed lock metrics initialized");
            } catch (Exception e) {
                logger.warn("⚠️  Failed to initialize lock metrics: {}", e.getMessage());
            }
        }
        
        logger.info("✅ DistributedLockService initialized");
    }
    
    /**
     * Acquire a distributed lock
     * 
     * @param lockKey The lock key
     * @param timeoutSeconds Lock timeout in seconds
     * @return LockHandle if acquired, null otherwise
     */
    public LockHandle acquireLock(String lockKey, long timeoutSeconds) {
        return acquireLock(lockKey, timeoutSeconds, 0);
    }
    
    /**
     * Acquire a distributed lock with wait time
     * 
     * @param lockKey The lock key
     * @param timeoutSeconds Lock timeout in seconds
     * @param waitTimeSeconds Maximum time to wait for lock (0 = no wait)
     * @return LockHandle if acquired, null otherwise
     */
    public LockHandle acquireLock(String lockKey, long timeoutSeconds, long waitTimeSeconds) {
        if (redisTemplate == null) {
            logger.warn("Redis not available, lock acquisition skipped for key: {}", lockKey);
            return null;
        }
        
        String fullLockKey = LOCK_PREFIX + lockKey;
        String lockOwner = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (waitTimeSeconds * 1000);
        
        Timer.Sample sample = enableMetrics && lockAcquisitionTimer != null 
            ? Timer.start(meterRegistry) : null;
        
        try {
            while (System.currentTimeMillis() < endTime || waitTimeSeconds == 0) {
                // Try to acquire lock using Lua script (atomic operation)
                Long result = redisTemplate.execute(
                    lockScript,
                    Collections.singletonList(fullLockKey),
                    lockOwner,
                    String.valueOf(timeoutSeconds)
                );
                
                if (result != null && result == 1) {
                    // Lock acquired
                    if (enableMetrics && lockAcquiredCounter != null) {
                        lockAcquiredCounter.increment();
                    }
                    
                    if (sample != null) {
                        sample.stop(lockAcquisitionTimer);
                    }
                    
                    logger.debug("🔒 Lock acquired: {} by owner: {}", lockKey, lockOwner);
                    return new LockHandle(lockKey, lockOwner, timeoutSeconds);
                }
                
                // Lock not available, wait and retry
                if (waitTimeSeconds > 0) {
                    try {
                        Thread.sleep(100); // Wait 100ms before retry
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // No wait time, return immediately
                    break;
                }
            }
            
            // Failed to acquire lock
            if (enableMetrics && lockFailedCounter != null) {
                lockFailedCounter.increment();
            }
            
            logger.debug("❌ Failed to acquire lock: {}", lockKey);
            return null;
            
        } catch (Exception e) {
            logger.error("Error acquiring lock: {}", lockKey, e);
            if (enableMetrics && lockFailedCounter != null) {
                lockFailedCounter.increment();
            }
            return null;
        }
    }
    
    /**
     * Release a distributed lock
     * 
     * @param lockHandle The lock handle
     * @return true if released, false otherwise
     */
    public boolean releaseLock(LockHandle lockHandle) {
        if (lockHandle == null || redisTemplate == null) {
            return false;
        }
        
        String fullLockKey = LOCK_PREFIX + lockHandle.getLockKey();
        
        try {
            // Use Lua script for atomic release
            Long result = redisTemplate.execute(
                unlockScript,
                Collections.singletonList(fullLockKey),
                lockHandle.getLockOwner()
            );
            
            boolean released = result != null && result == 1;
            
            if (released) {
                if (enableMetrics && lockReleasedCounter != null) {
                    lockReleasedCounter.increment();
                }
                logger.debug("🔓 Lock released: {} by owner: {}", 
                    lockHandle.getLockKey(), lockHandle.getLockOwner());
            } else {
                logger.warn("⚠️  Failed to release lock: {} (may have expired or been released by another owner)", 
                    lockHandle.getLockKey());
            }
            
            return released;
            
        } catch (Exception e) {
            logger.error("Error releasing lock: {}", lockHandle.getLockKey(), e);
            return false;
        }
    }
    
    /**
     * Renew a distributed lock (extend its timeout)
     * 
     * @param lockHandle The lock handle
     * @param additionalSeconds Additional seconds to add to the lock
     * @return true if renewed, false otherwise
     */
    public boolean renewLock(LockHandle lockHandle, long additionalSeconds) {
        if (lockHandle == null || redisTemplate == null) {
            return false;
        }
        
        String fullLockKey = LOCK_PREFIX + lockHandle.getLockKey();
        
        try {
            Long result = redisTemplate.execute(
                renewScript,
                Collections.singletonList(fullLockKey),
                lockHandle.getLockOwner(),
                String.valueOf(additionalSeconds)
            );
            
            boolean renewed = result != null && result == 1;
            
            if (renewed) {
                logger.debug("🔄 Lock renewed: {} for additional {} seconds", 
                    lockHandle.getLockKey(), additionalSeconds);
            } else {
                logger.warn("⚠️  Failed to renew lock: {} (may have expired)", 
                    lockHandle.getLockKey());
            }
            
            return renewed;
            
        } catch (Exception e) {
            logger.error("Error renewing lock: {}", lockHandle.getLockKey(), e);
            return false;
        }
    }
    
    /**
     * Check if a lock is currently held
     * 
     * @param lockKey The lock key
     * @return true if lock is held, false otherwise
     */
    public boolean isLocked(String lockKey) {
        if (redisTemplate == null) {
            return false;
        }
        
        String fullLockKey = LOCK_PREFIX + lockKey;
        Boolean exists = redisTemplate.hasKey(fullLockKey);
        return exists != null && exists;
    }
    
    /**
     * Get lock owner information
     * 
     * @param lockKey The lock key
     * @return Lock owner UUID or null if not locked
     */
    public String getLockOwner(String lockKey) {
        if (redisTemplate == null) {
            return null;
        }
        
        String fullLockKey = LOCK_PREFIX + lockKey;
        return redisTemplate.opsForValue().get(fullLockKey);
    }
    
    /**
     * Get remaining TTL for a lock
     * 
     * @param lockKey The lock key
     * @return Remaining TTL in seconds, or -1 if not locked
     */
    public long getLockTtl(String lockKey) {
        if (redisTemplate == null) {
            return -1;
        }
        
        String fullLockKey = LOCK_PREFIX + lockKey;
        Long ttl = redisTemplate.getExpire(fullLockKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    /**
     * Lock Handle class
     */
    public static class LockHandle {
        private final String lockKey;
        private final String lockOwner;
        private final long timeoutSeconds;
        private final long acquiredAt;
        
        public LockHandle(String lockKey, String lockOwner, long timeoutSeconds) {
            this.lockKey = lockKey;
            this.lockOwner = lockOwner;
            this.timeoutSeconds = timeoutSeconds;
            this.acquiredAt = System.currentTimeMillis();
        }
        
        public String getLockKey() {
            return lockKey;
        }
        
        public String getLockOwner() {
            return lockOwner;
        }
        
        public long getTimeoutSeconds() {
            return timeoutSeconds;
        }
        
        public long getAcquiredAt() {
            return acquiredAt;
        }
        
        public long getRemainingTimeSeconds() {
            long elapsed = (System.currentTimeMillis() - acquiredAt) / 1000;
            return Math.max(0, timeoutSeconds - elapsed);
        }
    }
}

