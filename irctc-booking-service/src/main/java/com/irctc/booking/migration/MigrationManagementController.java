package com.irctc.booking.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migration Management Controller
 * 
 * REST API for managing and querying database migrations
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/migrations")
public class MigrationManagementController {

    @Autowired(required = false)
    private MigrationManagementService migrationService;
    
    /**
     * Get migration status summary
     */
    @GetMapping("/status")
    public ResponseEntity<MigrationManagementService.MigrationStatusSummary> getStatus() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        MigrationManagementService.MigrationStatusSummary summary = migrationService.getStatusSummary();
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get all migrations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMigrations() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("all", migrationService.getAllMigrations());
        response.put("applied", migrationService.getAppliedMigrations());
        response.put("pending", migrationService.getPendingMigrations());
        response.put("failed", migrationService.getFailedMigrations());
        response.put("currentVersion", migrationService.getCurrentVersion());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get applied migrations
     */
    @GetMapping("/applied")
    public ResponseEntity<List<org.flywaydb.core.api.MigrationInfo>> getAppliedMigrations() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(migrationService.getAppliedMigrations());
    }
    
    /**
     * Get pending migrations
     */
    @GetMapping("/pending")
    public ResponseEntity<List<org.flywaydb.core.api.MigrationInfo>> getPendingMigrations() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(migrationService.getPendingMigrations());
    }
    
    /**
     * Get failed migrations
     */
    @GetMapping("/failed")
    public ResponseEntity<List<org.flywaydb.core.api.MigrationInfo>> getFailedMigrations() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(migrationService.getFailedMigrations());
    }
    
    /**
     * Get migration history
     */
    @GetMapping("/history")
    public ResponseEntity<List<MigrationManagementService.MigrationHistory>> getHistory() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(migrationService.getMigrationHistory());
    }
    
    /**
     * Get specific migration details
     */
    @GetMapping("/{version}")
    public ResponseEntity<MigrationManagementService.MigrationDetails> getMigrationDetails(
            @PathVariable String version) {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        MigrationManagementService.MigrationDetails details = migrationService.getMigrationDetails(version);
        if (details == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(details);
    }
    
    /**
     * Validate migrations
     */
    @PostMapping("/validate")
    public ResponseEntity<MigrationManagementService.MigrationValidationResult> validateMigrations() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(migrationService.validateMigrations());
    }
    
    /**
     * Get current migration version
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, String>> getCurrentVersion() {
        if (migrationService == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("version", migrationService.getCurrentVersion());
        
        return ResponseEntity.ok(response);
    }
}

