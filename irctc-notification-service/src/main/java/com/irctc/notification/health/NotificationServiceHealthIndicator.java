package com.irctc.notification.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
 * Custom Health Indicator for Notification Service
 * 
 * Checks connectivity and status of:
 * - Database
 * - Kafka
 * - Notification statistics
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class NotificationServiceHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;

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

        // Get notification statistics if possible
        if (jdbcTemplate != null) {
            try {
                Long totalNotifications = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM notifications", Long.class);
                details.put("totalNotifications", totalNotifications != null ? totalNotifications : 0);
            } catch (Exception e) {
                details.put("totalNotifications", "unknown");
            }
        }

        // Add timestamp
        details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.put("service", "irctc-notification-service");

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

