package com.irctc.user.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Health Indicator for User Service
 * 
 * Checks connectivity and status of:
 * - Database
 * - Last user registration timestamp
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class UserServiceHealthIndicator implements HealthIndicator {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

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

        // Get last user registration time if possible
        if (jdbcTemplate != null) {
            try {
                String lastUserTime = jdbcTemplate.queryForObject(
                    "SELECT MAX(created_at) FROM simple_users", String.class);
                details.put("lastUserRegistration", lastUserTime != null ? lastUserTime : "no users yet");
            } catch (Exception e) {
                details.put("lastUserRegistration", "unknown");
            }
        }

        // Add timestamp
        details.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.put("service", "irctc-user-service");

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

