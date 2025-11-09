package com.irctc.booking.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tenant Context
 * 
 * Thread-local storage for current tenant information
 * 
 * @author IRCTC Development Team
 * @version 1.0.0
 */
public class TenantContext {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_CODE = new ThreadLocal<>();
    
    /**
     * Set current tenant ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
        logger.debug("Tenant ID set: {}", tenantId);
    }
    
    /**
     * Get current tenant ID
     */
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    /**
     * Set current tenant code
     */
    public static void setTenantCode(String tenantCode) {
        TENANT_CODE.set(tenantCode);
        logger.debug("Tenant code set: {}", tenantCode);
    }
    
    /**
     * Get current tenant code
     */
    public static String getTenantCode() {
        return TENANT_CODE.get();
    }
    
    /**
     * Check if tenant context is set
     */
    public static boolean hasTenant() {
        return TENANT_ID.get() != null || TENANT_CODE.get() != null;
    }
    
    /**
     * Clear tenant context
     */
    public static void clear() {
        TENANT_ID.remove();
        TENANT_CODE.remove();
        logger.debug("Tenant context cleared");
    }
    
    /**
     * Get tenant ID or throw exception if not set
     */
    public static String getTenantIdRequired() {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantId;
    }
    
    /**
     * Get tenant code or throw exception if not set
     */
    public static String getTenantCodeRequired() {
        String tenantCode = getTenantCode();
        if (tenantCode == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantCode;
    }
}

