package com.irctc.user.health;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced Health Indicator for User Service
 * 
 * @author IRCTC Development Team
 * @version 2.0.0
 */
@Component
public class EnhancedUserHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedUserHealthIndicator.class);

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private EurekaClient eurekaClient;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        boolean isHealthy = true;
        Status overallStatus = Status.UP;

        Instant startTime = Instant.now();

        // Database Health Check
        Map<String, Object> dbHealth = checkDatabase();
        components.put("database", dbHealth);
        if (!Boolean.TRUE.equals(dbHealth.get("status"))) {
            isHealthy = false;
            overallStatus = Status.DOWN;
        }

        // Redis Health Check (Optional)
        Map<String, Object> redisHealth = checkRedis();
        components.put("redis", redisHealth);

        // Kafka Health Check
        Map<String, Object> kafkaHealth = checkKafka();
        components.put("kafka", kafkaHealth);
        if (!Boolean.TRUE.equals(kafkaHealth.get("status"))) {
            isHealthy = false;
            overallStatus = Status.DOWN;
        }

        // Eureka Health Check
        Map<String, Object> eurekaHealth = checkEureka();
        components.put("eureka", eurekaHealth);

        // Service Statistics
        Map<String, Object> stats = getServiceStatistics();
        components.put("statistics", stats);

        Duration checkDuration = Duration.between(startTime, Instant.now());
        details.put("checkDuration", checkDuration.toMillis() + "ms");
        details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.put("service", "irctc-user-service");
        details.put("version", "2.0.0");
        details.put("components", components);

        Health.Builder healthBuilder = isHealthy ? Health.up() : Health.down();
        return healthBuilder
                .status(overallStatus)
                .withDetails(details)
                .build();
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        dbHealth.put("status", false);
        
        if (dataSource == null) {
            dbHealth.put("status", null);
            dbHealth.put("message", "not configured");
            return dbHealth;
        }

        try {
            Instant start = Instant.now();
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(2);
                Duration duration = Duration.between(start, Instant.now());
                
                if (valid) {
                    DatabaseMetaData metaData = connection.getMetaData();
                    dbHealth.put("status", true);
                    dbHealth.put("url", metaData.getURL());
                    dbHealth.put("driverName", metaData.getDriverName());
                    dbHealth.put("responseTime", duration.toMillis() + "ms");
                    
                    if (jdbcTemplate != null) {
                        try {
                            Instant queryStart = Instant.now();
                            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM simple_users", Long.class);
                            Duration queryDuration = Duration.between(queryStart, Instant.now());
                            dbHealth.put("queryTest", "success");
                            dbHealth.put("queryTime", queryDuration.toMillis() + "ms");
                        } catch (Exception e) {
                            dbHealth.put("queryTest", "failed: " + e.getMessage());
                        }
                    }
                } else {
                    dbHealth.put("status", false);
                    dbHealth.put("message", "connection validation failed");
                }
            }
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            dbHealth.put("status", false);
            dbHealth.put("message", "error: " + e.getMessage());
        }
        
        return dbHealth;
    }

    private Map<String, Object> checkRedis() {
        Map<String, Object> redisHealth = new HashMap<>();
        redisHealth.put("status", false);
        redisHealth.put("required", false);
        
        if (redisTemplate == null) {
            redisHealth.put("status", null);
            redisHealth.put("message", "not configured");
            return redisHealth;
        }

        try {
            Instant start = Instant.now();
            var connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            Duration duration = Duration.between(start, Instant.now());
            
            redisHealth.put("status", true);
            redisHealth.put("ping", pong);
            redisHealth.put("responseTime", duration.toMillis() + "ms");
            
            try {
                String testKey = "health-check:" + System.currentTimeMillis();
                redisTemplate.opsForValue().set(testKey, "test", 1, TimeUnit.SECONDS);
                Object value = redisTemplate.opsForValue().get(testKey);
                redisTemplate.delete(testKey);
                redisHealth.put("cacheTest", value != null ? "success" : "failed");
            } catch (Exception e) {
                redisHealth.put("cacheTest", "failed: " + e.getMessage());
            }
            
            connection.close();
        } catch (Exception e) {
            logger.warn("Redis health check failed (optional)", e);
            redisHealth.put("status", false);
            redisHealth.put("message", "error: " + e.getMessage());
        }
        
        return redisHealth;
    }

    private Map<String, Object> checkKafka() {
        Map<String, Object> kafkaHealth = new HashMap<>();
        kafkaHealth.put("status", false);
        
        if (kafkaTemplate == null) {
            kafkaHealth.put("status", null);
            kafkaHealth.put("message", "not configured");
            return kafkaHealth;
        }

        try {
            Instant start = Instant.now();
            var producerFactory = kafkaTemplate.getProducerFactory();
            if (producerFactory != null) {
                Object bootstrapServers = producerFactory.getConfigurationProperties()
                    .get("bootstrap.servers");
                
                kafkaHealth.put("status", true);
                kafkaHealth.put("bootstrapServers", bootstrapServers != null ? bootstrapServers : "unknown");
                kafkaHealth.put("responseTime", Duration.between(start, Instant.now()).toMillis() + "ms");
            } else {
                kafkaHealth.put("status", false);
                kafkaHealth.put("message", "producer factory not available");
            }
        } catch (Exception e) {
            logger.error("Kafka health check failed", e);
            kafkaHealth.put("status", false);
            kafkaHealth.put("message", "error: " + e.getMessage());
        }
        
        return kafkaHealth;
    }

    private Map<String, Object> checkEureka() {
        Map<String, Object> eurekaHealth = new HashMap<>();
        eurekaHealth.put("status", false);
        
        if (eurekaClient == null) {
            eurekaHealth.put("status", null);
            eurekaHealth.put("message", "not configured");
            return eurekaHealth;
        }

        try {
            InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
            if (instanceInfo != null) {
                eurekaHealth.put("status", true);
                eurekaHealth.put("instanceId", instanceInfo.getInstanceId());
                eurekaHealth.put("appName", instanceInfo.getAppName());
                eurekaHealth.put("status", instanceInfo.getStatus().toString());
            } else {
                eurekaHealth.put("status", false);
                eurekaHealth.put("message", "instance info not available");
            }
        } catch (Exception e) {
            logger.warn("Eureka health check failed", e);
            eurekaHealth.put("status", false);
            eurekaHealth.put("message", "error: " + e.getMessage());
        }
        
        return eurekaHealth;
    }

    private Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        if (jdbcTemplate != null) {
            try {
                Long totalUsers = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simple_users", Long.class);
                stats.put("totalUsers", totalUsers != null ? totalUsers : 0);
                
                String lastUserTime = jdbcTemplate.queryForObject(
                    "SELECT MAX(created_at) FROM simple_users", String.class);
                stats.put("lastUserRegistration", lastUserTime != null ? lastUserTime : "no users yet");
            } catch (Exception e) {
                logger.debug("Failed to get service statistics", e);
                stats.put("error", "unable to retrieve statistics");
            }
        }
        
        return stats;
    }
}

