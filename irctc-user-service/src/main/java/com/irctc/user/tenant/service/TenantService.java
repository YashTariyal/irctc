package com.irctc.user.tenant.service;

import com.irctc.user.tenant.entity.Tenant;
import com.irctc.user.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TenantService {
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    
    @Autowired
    private TenantRepository tenantRepository;
    
    public Tenant createTenant(Tenant tenant) {
        if (tenantRepository.existsByCode(tenant.getCode())) {
            throw new IllegalArgumentException("Tenant with code " + tenant.getCode() + " already exists");
        }
        Tenant saved = tenantRepository.save(tenant);
        logger.info("Created tenant: {} (ID: {})", saved.getCode(), saved.getId());
        return saved;
    }
    
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantById(Long id) {
        return tenantRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantByCode(String code) {
        return tenantRepository.findByCode(code);
    }
    
    @Transactional(readOnly = true)
    public Optional<Tenant> getActiveTenantByCode(String code) {
        return tenantRepository.findActiveByCode(code);
    }
    
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }
    
    public Tenant updateTenant(Long id, Tenant tenant) {
        Tenant existing = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        if (tenant.getName() != null) existing.setName(tenant.getName());
        if (tenant.getEmail() != null) existing.setEmail(tenant.getEmail());
        if (tenant.getPhone() != null) existing.setPhone(tenant.getPhone());
        if (tenant.getStatus() != null) existing.setStatus(tenant.getStatus());
        if (tenant.getConfiguration() != null) existing.setConfiguration(tenant.getConfiguration());
        Tenant updated = tenantRepository.save(existing);
        logger.info("Updated tenant: {} (ID: {})", updated.getCode(), updated.getId());
        return updated;
    }
    
    public void deleteTenant(Long id) {
        if (!tenantRepository.existsById(id)) {
            throw new IllegalArgumentException("Tenant not found: " + id);
        }
        tenantRepository.deleteById(id);
        logger.info("Deleted tenant: {}", id);
    }
    
    public Tenant activateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        tenant.setStatus("ACTIVE");
        return tenantRepository.save(tenant);
    }
    
    public Tenant suspendTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        tenant.setStatus("SUSPENDED");
        return tenantRepository.save(tenant);
    }
}

