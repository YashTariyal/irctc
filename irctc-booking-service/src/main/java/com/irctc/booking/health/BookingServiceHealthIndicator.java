package com.irctc.booking.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Health Indicator for Booking Service
 * 
 * Checks connectivity and status of:
 * - Database
 * - Kafka
 * - Redis
 * - Last booking timestamp
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class BookingServiceHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        // Check Database
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(2);
                details.put("database", valid ? "connected" : "disconnected");
                details.put("databaseValidation", valid);
                if (!valid) isHealthy = false;
            } catch (Exception e) {
                details.put("database", "error: " + e.getMessage());
                details.put("databaseValidation", false);
                isHealthy = false;
            }
        } else {
            details.put("database", "not configured");
        }

        // Check Kafka
        if (kafkaTemplate != null) {
            try {
                // Simple check - verify template is available
                kafkaTemplate.getProducerFactory();
                details.put("kafka", "available");
                details.put("kafkaValidation", true);
            } catch (Exception e) {
                details.put("kafka", "error: " + e.getMessage());
                details.put("kafkaValidation", false);
                isHealthy = false;
            }
        } else {
            details.put("kafka", "not configured");
        }

        // Check Redis
        if (redisTemplate != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().ping();
                details.put("redis", "connected");
                details.put("redisValidation", true);
            } catch (Exception e) {
                details.put("redis", "error: " + e.getMessage());
                details.put("redisValidation", false);
                // Redis is optional, don't fail health if Redis is down
            }
        } else {
            details.put("redis", "not configured");
        }

        // Get last booking time if possible
        if (jdbcTemplate != null) {
            try {
                String lastBookingTime = jdbcTemplate.queryForObject(
                    "SELECT MAX(created_at) FROM bookings", String.class);
                details.put("lastBookingTime", lastBookingTime != null ? lastBookingTime : "no bookings yet");
            } catch (Exception e) {
                details.put("lastBookingTime", "unknown");
            }
        }

        // Add timestamp
        details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.put("service", "irctc-booking-service");

        if (isHealthy) {
            return Health.up()
                    .withDetails(details)
                    .build();
        } else {
            return Health.down()
                    .withDetails(details)
                    .build();
        }
    }
}

