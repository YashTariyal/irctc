package com.irctc.booking.tenant.controller;

import com.irctc.booking.tenant.entity.Tenant;
import com.irctc.booking.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Tenant Management Controller
 * 
 * REST API for tenant management
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    
    @Autowired
    private TenantService tenantService;
    
    /**
     * Create a new tenant
     */
    @PostMapping
    public ResponseEntity<Tenant> createTenant(@Valid @RequestBody Tenant tenant) {
        Tenant created = tenantService.createTenant(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Get tenant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get tenant by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Tenant> getTenantByCode(@PathVariable String code) {
        return tenantService.getTenantByCode(code)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all tenants
     */
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }
    
    /**
     * Update tenant
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable Long id, @Valid @RequestBody Tenant tenant) {
        try {
            Tenant updated = tenantService.updateTenant(id, tenant);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete tenant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Activate tenant
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Tenant> activateTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.activateTenant(id);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Suspend tenant
     */
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Tenant> suspendTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.suspendTenant(id);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

