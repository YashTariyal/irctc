package com.irctc.booking.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Migration Health Indicator
 * 
 * Provides health check for database migrations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Component
public class MigrationHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(MigrationHealthIndicator.class);
    
    @Autowired(required = false)
    private Flyway flyway;
    
    @Override
    public Health health() {
        if (flyway == null) {
            return Health.status(Status.DOWN)
                .withDetail("status", "Flyway not available")
                .build();
        }
        
        try {
            MigrationInfoService info = flyway.info();
            MigrationInfo[] allMigrations = info.all();
            MigrationInfo current = info.current();
            
            // Check for failed migrations
            long failedCount = Arrays.stream(allMigrations)
                .filter(m -> m.getState() == MigrationState.FAILED)
                .count();
            
            if (failedCount > 0) {
                return Health.status(Status.DOWN)
                    .withDetail("status", "Migrations have failures")
                    .withDetail("failedCount", failedCount)
                    .withDetail("currentVersion", current != null ? current.getVersion().toString() : "none")
                    .build();
            }
            
            // Check for pending migrations
            long pendingCount = Arrays.stream(allMigrations)
                .filter(m -> m.getState() == MigrationState.PENDING)
                .count();
            
            Health.Builder builder = Health.status(Status.UP)
                .withDetail("status", "All migrations applied successfully")
                .withDetail("totalMigrations", allMigrations.length)
                .withDetail("appliedMigrations", allMigrations.length - (int) pendingCount)
                .withDetail("pendingMigrations", pendingCount)
                .withDetail("currentVersion", current != null ? current.getVersion().toString() : "none");
            
            if (pendingCount > 0) {
                builder.withDetail("warning", "There are pending migrations");
            }
            
            return builder.build();
            
        } catch (Exception e) {
            logger.error("Error checking migration health", e);
            return Health.status(Status.DOWN)
                .withDetail("status", "Error checking migrations")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

