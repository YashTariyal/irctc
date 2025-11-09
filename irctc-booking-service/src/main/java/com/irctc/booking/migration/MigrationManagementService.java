package com.irctc.booking.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Migration Management Service
 * 
 * Provides comprehensive migration management capabilities:
 * - Migration status tracking
 * - Migration history
 * - Migration validation
 * - Migration information
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@Service
public class MigrationManagementService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationManagementService.class);
    
    @Autowired(required = false)
    private Flyway flyway;
    
    @Autowired(required = false)
    private FlywayProperties flywayProperties;
    
    @PostConstruct
    public void init() {
        if (flyway == null) {
            logger.warn("⚠️  Flyway not available, migration management disabled");
        } else {
            logger.info("✅ Migration Management Service initialized");
        }
    }
    
    /**
     * Get migration information
     */
    public MigrationInfoService getMigrationInfo() {
        if (flyway == null) {
            return null;
        }
        return flyway.info();
    }
    
    /**
     * Get all migrations
     */
    public List<MigrationInfo> getAllMigrations() {
        if (flyway == null) {
            return Collections.emptyList();
        }
        
        MigrationInfoService info = flyway.info();
        return Arrays.asList(info.all());
    }
    
    /**
     * Get applied migrations
     */
    public List<MigrationInfo> getAppliedMigrations() {
        if (flyway == null) {
            return Collections.emptyList();
        }
        
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.all())
            .filter(m -> m.getState() == MigrationState.SUCCESS)
            .collect(Collectors.toList());
    }
    
    /**
     * Get pending migrations
     */
    public List<MigrationInfo> getPendingMigrations() {
        if (flyway == null) {
            return Collections.emptyList();
        }
        
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.all())
            .filter(m -> m.getState() == MigrationState.PENDING)
            .collect(Collectors.toList());
    }
    
    /**
     * Get failed migrations
     */
    public List<MigrationInfo> getFailedMigrations() {
        if (flyway == null) {
            return Collections.emptyList();
        }
        
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.all())
            .filter(m -> m.getState() == MigrationState.FAILED)
            .collect(Collectors.toList());
    }
    
    /**
     * Get current migration version
     */
    public String getCurrentVersion() {
        if (flyway == null) {
            return null;
        }
        
        MigrationInfoService info = flyway.info();
        MigrationInfo current = info.current();
        return current != null ? current.getVersion().toString() : null;
    }
    
    /**
     * Get migration status summary
     */
    public MigrationStatusSummary getStatusSummary() {
        if (flyway == null) {
            return null;
        }
        
        MigrationInfoService info = flyway.info();
        MigrationInfo[] allMigrations = info.all();
        
        MigrationStatusSummary summary = new MigrationStatusSummary();
        summary.setTotalMigrations(allMigrations.length);
        summary.setAppliedMigrations((int) Arrays.stream(allMigrations)
            .filter(m -> m.getState() == MigrationState.SUCCESS)
            .count());
        summary.setPendingMigrations((int) Arrays.stream(allMigrations)
            .filter(m -> m.getState() == MigrationState.PENDING)
            .count());
        summary.setFailedMigrations((int) Arrays.stream(allMigrations)
            .filter(m -> m.getState() == MigrationState.FAILED)
            .count());
        summary.setCurrentVersion(getCurrentVersion());
        summary.setTimestamp(LocalDateTime.now());
        
        return summary;
    }
    
    /**
     * Validate migrations
     */
    public MigrationValidationResult validateMigrations() {
        if (flyway == null) {
            MigrationValidationResult result = new MigrationValidationResult();
            result.setValid(false);
            result.setMessage("Flyway not available");
            return result;
        }
        
        try {
            flyway.validate();
            
            MigrationValidationResult result = new MigrationValidationResult();
            result.setValid(true);
            result.setMessage("All migrations are valid");
            result.setTimestamp(LocalDateTime.now());
            return result;
            
        } catch (Exception e) {
            logger.error("Migration validation failed", e);
            
            MigrationValidationResult result = new MigrationValidationResult();
            result.setValid(false);
            result.setMessage("Validation failed: " + e.getMessage());
            result.setTimestamp(LocalDateTime.now());
            return result;
        }
    }
    
    /**
     * Get migration details
     */
    public MigrationDetails getMigrationDetails(String version) {
        if (flyway == null) {
            return null;
        }
        
        MigrationInfoService info = flyway.info();
        MigrationInfo migration = Arrays.stream(info.all())
            .filter(m -> m.getVersion() != null && m.getVersion().toString().equals(version))
            .findFirst()
            .orElse(null);
        
        if (migration == null) {
            return null;
        }
        
        MigrationDetails details = new MigrationDetails();
        details.setVersion(migration.getVersion() != null ? migration.getVersion().toString() : null);
        details.setDescription(migration.getDescription());
        details.setState(migration.getState().name());
        details.setType(migration.getType().name());
        details.setInstalledOn(migration.getInstalledOn());
        details.setInstalledBy(migration.getInstalledBy());
        details.setInstalledRank(migration.getInstalledRank());
        details.setExecutionTime(migration.getExecutionTime());
        details.setScript(migration.getScript());
        details.setChecksum(migration.getChecksum());
        
        return details;
    }
    
    /**
     * Get migration history
     */
    public List<MigrationHistory> getMigrationHistory() {
        if (flyway == null) {
            return Collections.emptyList();
        }
        
        MigrationInfoService info = flyway.info();
        return Arrays.stream(info.all())
            .map(this::toMigrationHistory)
            .collect(Collectors.toList());
    }
    
    private MigrationHistory toMigrationHistory(MigrationInfo migration) {
        MigrationHistory history = new MigrationHistory();
        history.setVersion(migration.getVersion() != null ? migration.getVersion().toString() : null);
        history.setDescription(migration.getDescription());
        history.setType(migration.getType().name());
        history.setState(migration.getState().name());
        history.setInstalledOn(migration.getInstalledOn());
        history.setInstalledBy(migration.getInstalledBy());
        history.setExecutionTime(migration.getExecutionTime());
        history.setScript(migration.getScript());
        return history;
    }
    
    // Inner classes
    public static class MigrationStatusSummary {
        private int totalMigrations;
        private int appliedMigrations;
        private int pendingMigrations;
        private int failedMigrations;
        private String currentVersion;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public int getTotalMigrations() { return totalMigrations; }
        public void setTotalMigrations(int totalMigrations) { this.totalMigrations = totalMigrations; }
        public int getAppliedMigrations() { return appliedMigrations; }
        public void setAppliedMigrations(int appliedMigrations) { this.appliedMigrations = appliedMigrations; }
        public int getPendingMigrations() { return pendingMigrations; }
        public void setPendingMigrations(int pendingMigrations) { this.pendingMigrations = pendingMigrations; }
        public int getFailedMigrations() { return failedMigrations; }
        public void setFailedMigrations(int failedMigrations) { this.failedMigrations = failedMigrations; }
        public String getCurrentVersion() { return currentVersion; }
        public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class MigrationValidationResult {
        private boolean valid;
        private String message;
        private LocalDateTime timestamp;
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public static class MigrationDetails {
        private String version;
        private String description;
        private String state;
        private String type;
        private Date installedOn;
        private String installedBy;
        private Integer installedRank;
        private Integer executionTime;
        private String script;
        private Integer checksum;
        
        // Getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Date getInstalledOn() { return installedOn; }
        public void setInstalledOn(Date installedOn) { this.installedOn = installedOn; }
        public String getInstalledBy() { return installedBy; }
        public void setInstalledBy(String installedBy) { this.installedBy = installedBy; }
        public Integer getInstalledRank() { return installedRank; }
        public void setInstalledRank(Integer installedRank) { this.installedRank = installedRank; }
        public Integer getExecutionTime() { return executionTime; }
        public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
        public String getScript() { return script; }
        public void setScript(String script) { this.script = script; }
        public Integer getChecksum() { return checksum; }
        public void setChecksum(Integer checksum) { this.checksum = checksum; }
    }
    
    public static class MigrationHistory {
        private String version;
        private String description;
        private String type;
        private String state;
        private Date installedOn;
        private String installedBy;
        private Integer executionTime;
        private String script;
        
        // Getters and setters
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public Date getInstalledOn() { return installedOn; }
        public void setInstalledOn(Date installedOn) { this.installedOn = installedOn; }
        public String getInstalledBy() { return installedBy; }
        public void setInstalledBy(String installedBy) { this.installedBy = installedBy; }
        public Integer getExecutionTime() { return executionTime; }
        public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
        public String getScript() { return script; }
        public void setScript(String script) { this.script = script; }
    }
}

