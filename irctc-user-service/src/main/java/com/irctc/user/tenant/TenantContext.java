package com.irctc.user.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_CODE = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
        logger.debug("Tenant ID set: {}", tenantId);
    }
    
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    public static void setTenantCode(String tenantCode) {
        TENANT_CODE.set(tenantCode);
        logger.debug("Tenant code set: {}", tenantCode);
    }
    
    public static String getTenantCode() {
        return TENANT_CODE.get();
    }
    
    public static boolean hasTenant() {
        return TENANT_ID.get() != null || TENANT_CODE.get() != null;
    }
    
    public static void clear() {
        TENANT_ID.remove();
        TENANT_CODE.remove();
        logger.debug("Tenant context cleared");
    }
    
    public static String getTenantIdRequired() {
        String tenantId = getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantId;
    }
    
    public static String getTenantCodeRequired() {
        String tenantCode = getTenantCode();
        if (tenantCode == null) {
            throw new IllegalStateException("Tenant context is not set");
        }
        return tenantCode;
    }
}

