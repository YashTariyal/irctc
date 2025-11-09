package com.irctc.booking.tenant;

/**
 * Tenant Aware Interface
 * 
 * Marker interface for entities that are tenant-aware
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public interface TenantAware {
    
    /**
     * Get tenant ID
     */
    String getTenantId();
    
    /**
     * Set tenant ID
     */
    void setTenantId(String tenantId);
}

